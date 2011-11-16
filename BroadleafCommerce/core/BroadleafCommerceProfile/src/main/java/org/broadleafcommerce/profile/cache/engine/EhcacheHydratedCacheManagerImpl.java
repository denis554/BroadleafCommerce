/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.profile.cache.engine;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.CacheKey;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class EhcacheHydratedCacheManagerImpl implements CacheEventListener, HydratedCacheManager, HydratedAnnotationManager {

    private static final Log LOG = LogFactory.getLog(EhcacheHydratedCacheManagerImpl.class);
    private static final EhcacheHydratedCacheManagerImpl MANAGER = new EhcacheHydratedCacheManagerImpl();

    public static EhcacheHydratedCacheManagerImpl getInstance() {
        return MANAGER;
    }

    private Map<String, HydrationDescriptor> hydrationDescriptors = Collections.synchronizedMap(new HashMap(100));
    private Map<String, List<String>> cacheMembersByEntity = Collections.synchronizedMap(new HashMap<String, List<String>>(100));
    private Cache heap;

    private EhcacheHydratedCacheManagerImpl()  {
        if (CacheManager.getInstance().cacheExists("hydrated-cache")) {
            heap = CacheManager.getInstance().getCache("hydrated-cache");
        } else {
            CacheConfiguration config = new CacheConfiguration("hydrated-cache", 0).eternal(true).overflowToDisk(false).maxElementsInMemory(100000);
            Cache cache = new Cache(config);
            CacheManager.create().addCache(cache);
            heap = cache;
        }
    }

    @Override
	public HydrationDescriptor getHydrationDescriptor(Object entity) {
    	if (hydrationDescriptors.containsKey(entity.getClass().getName())) {
    		return hydrationDescriptors.get(entity.getClass().getName());
    	}
    	HydrationDescriptor descriptor = new HydrationDescriptor();
    	Class<?> topEntityClass = getTopEntityClass(entity);
    	HydrationScanner scanner = new HydrationScanner(topEntityClass, entity.getClass());
    	scanner.init();
    	descriptor.setHydratedMutators(scanner.getCacheMutators());
    	Map<String, Method[]> mutators = scanner.getIdMutators();
    	if (mutators.size() != 1) {
    		throw new RuntimeException("Broadleaf Commerce Hydrated Cache currently only supports entities with a single @Id annotation.");
    	}
    	Method[] singleMutators = mutators.values().iterator().next();
    	descriptor.setIdMutators(singleMutators);
    	String cacheRegion = scanner.getCacheRegion();
    	if (cacheRegion == null || "".equals(cacheRegion)) {
    		cacheRegion = topEntityClass.getName();
    	}
    	descriptor.setCacheRegion(cacheRegion);
    	hydrationDescriptors.put(entity.getClass().getName(), descriptor);
    	return descriptor;
    }

	protected Class<?> getTopEntityClass(Object entity) {
    	Class<?> myClass = entity.getClass();
    	Class<?> superClass = entity.getClass().getSuperclass();
    	while (superClass != null && superClass.getName().startsWith("org.broadleaf")) {
    		myClass = superClass;
    		superClass = superClass.getSuperclass();
    	}
    	return myClass;
    }

    @Override
    public Object getHydratedCacheElementItem(String cacheRegion, String cacheName, Serializable elementKey, String elementItemName) {
    	Object response = null;
    	Element element;
    	String myKey = cacheRegion + '_' + cacheName + '_' + elementItemName + '_' + elementKey;
    	element = heap.get(myKey);
    	if (element != null) {
    		response = element.getObjectValue();
    	}
    	return response;
    }

    @Override
    public void addHydratedCacheElementItem(String cacheRegion, String cacheName, Serializable elementKey, String elementItemName, Object elementValue) {
    	String heapKey = cacheRegion + '_' + cacheName + '_' + elementItemName + '_' + elementKey;
        String nameKey = cacheRegion + '_' + cacheName + '_' + elementKey;
        Element element = new Element(heapKey, elementValue);
    	if (!cacheMembersByEntity.containsKey(nameKey)) {
    		List<String> myMembers = new ArrayList<String>(50);
    		myMembers.add(elementItemName);
    		cacheMembersByEntity.put(nameKey, myMembers);
    	} else {
    		List<String> myMembers = cacheMembersByEntity.get(nameKey);
    		myMembers.add(elementItemName);
    	}
    	heap.put(element);
    }

    @Override
    public void dispose() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Disposing of all hydrated cache members");
        }
    	hydrationDescriptors.clear();
    }

    protected void removeCache(String cacheRegion, Serializable key) {
        String cacheName = cacheRegion;
        if (key instanceof CacheKey) {
    		cacheName = ((CacheKey) key).getEntityOrRoleName();
    		key = ((CacheKey) key).getKey();
    	}
    	String nameKey = cacheRegion + '_' + cacheName + '_' + key;
    	if (cacheMembersByEntity.containsKey(nameKey)) {
    		String[] members = new String[cacheMembersByEntity.get(nameKey).size()];
    		members = cacheMembersByEntity.get(nameKey).toArray(members);
    		for (String myMember : members) {
    			String itemKey = cacheRegion + '_' + cacheName + '_' + myMember + '_' + key;
    			heap.remove(itemKey);
    		}
    		cacheMembersByEntity.remove(nameKey);
    	}
    }
    
    protected void removeAll(String cacheName) {
    	//do nothing
    }

    @Override
	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
		removeCache(arg0.getName(), arg1.getKey());
	}

    @Override
	public void notifyElementExpired(Ehcache arg0, Element arg1) {
		removeCache(arg0.getName(), arg1.getKey());
	}

    @Override
	public void notifyElementPut(Ehcache arg0, Element arg1) throws CacheException {
		//do nothing
	}

    @Override
	public void notifyElementRemoved(Ehcache arg0, Element arg1) throws CacheException {
		removeCache(arg0.getName(), arg1.getKey());
	}

    @Override
	public void notifyElementUpdated(Ehcache arg0, Element arg1) throws CacheException {
		removeCache(arg0.getName(), arg1.getKey());
	}

    @Override
	public void notifyRemoveAll(Ehcache arg0) {
		removeAll(arg0.getName());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return this;
	}

}
