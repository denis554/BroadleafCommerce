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
package org.broadleafcommerce.profile.cache;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.cache.engine.CacheFactoryException;
import org.broadleafcommerce.profile.cache.engine.HydratedAnnotationManager;
import org.broadleafcommerce.profile.cache.engine.HydratedCacheEventListenerFactory;
import org.broadleafcommerce.profile.cache.engine.HydratedCacheManager;
import org.broadleafcommerce.profile.cache.engine.HydrationDescriptor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @author jfischer
 *
 */
public class HydratedSetup {
	
	private static final Log LOG = LogFactory.getLog(HydratedSetup.class);

	public static void populateFromCache(Object entity) {
		HydratedCacheManager manager = HydratedCacheEventListenerFactory.getConfiguredManager();
		HydrationDescriptor descriptor = ((HydratedAnnotationManager) manager).getHydrationDescriptor(entity);
		if (!MapUtils.isEmpty(descriptor.getHydratedMutators())) {
			Method[] idMutators = descriptor.getIdMutators();
			String cacheRegion = descriptor.getCacheRegion();
			for (String field : descriptor.getHydratedMutators().keySet()) {
				try {
					Serializable entityId = (Serializable) idMutators[0].invoke(entity);
					Object hydratedItem = manager.getHydratedCacheElementItem(cacheRegion, entity.getClass().getName(), entityId, field);
					if (hydratedItem == null) {
						Method factoryMethod = entity.getClass().getMethod(descriptor.getHydratedMutators().get(field).getFactoryMethod(), new Class[]{});
						Object fieldVal = factoryMethod.invoke(entity);
						manager.addHydratedCacheElementItem(cacheRegion, entity.getClass().getName(), entityId, field, fieldVal);
						hydratedItem = fieldVal;
					}
					descriptor.getHydratedMutators().get(field).getMutators()[1].invoke(entity, hydratedItem);
				} catch (InvocationTargetException e) {
					if (e.getTargetException() != null && e.getTargetException() instanceof CacheFactoryException) {
						LOG.warn("Unable to setup the hydrated cache for an entity. " + e.getTargetException().getMessage());
					} else {
						throw new RuntimeException("There was a problem while replacing a hydrated cache item - field("+field+") : entity("+entity.getClass().getName()+')', e);
					}
				} catch (Exception e) {
					throw new RuntimeException("There was a problem while replacing a hydrated cache item - field("+field+") : entity("+entity.getClass().getName()+')', e);
				}
			}
		}
	}

}
