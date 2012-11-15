/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.dao;

import net.entropysoft.transmorph.cache.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.presentation.AdminPresentationAdornedTargetCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationMap;
import org.broadleafcommerce.common.presentation.client.PersistencePerspectiveItemType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetList;
import org.broadleafcommerce.openadmin.client.dto.BasicCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassTree;
import org.broadleafcommerce.openadmin.client.dto.CollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.MapMetadata;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.visitor.MetadataVisitorAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.FieldManager;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
@Component("blDynamicEntityDao")
@Scope("prototype")
public class DynamicEntityDaoImpl extends BaseHibernateCriteriaDao<Serializable> implements DynamicEntityDao {
	
	private static final Log LOG = LogFactory.getLog(DynamicEntityDaoImpl.class);
    protected static final Object LOCK_OBJECT = new Object();
    protected static final Map<String,Map<String, FieldMetadata>> METADATA_CACHE = new LRUMap<String, Map<String, FieldMetadata>>(1000);
    protected static final Map<Class<?>, Class<?>[]> POLYMORPHIC_ENTITY_CACHE = new LRUMap<Class<?>, Class<?>[]>(1000);
	
    protected EntityManager standardEntityManager;

    @Resource(name="blMetadata")
    protected Metadata metadata;

    @Resource(name="blEJB3ConfigurationDao")
    protected EJB3ConfigurationDao ejb3ConfigurationDao;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

	@Override
	public Class<? extends Serializable> getEntityClass() {
		throw new IllegalArgumentException("Must supply the entity class to query and count method calls! Default entity not supported!");
	}
	
	@Override
    public Serializable persist(Serializable entity) {
		standardEntityManager.persist(entity);
		standardEntityManager.flush();
		return entity;
	}
	
	@Override
    public Serializable merge(Serializable entity) {
		Serializable response = standardEntityManager.merge(entity);
		standardEntityManager.flush();
		return response;
	}
	
	@Override
    public void flush() {
		standardEntityManager.flush();
	}
	
	@Override
    public void detach(Serializable entity) {
		standardEntityManager.detach(entity);
	}
	
	@Override
    public void refresh(Serializable entity) {
		standardEntityManager.refresh(entity);
	}
 	
	@Override
    public Serializable retrieve(Class<?> entityClass, Object primaryKey) {
		return (Serializable) standardEntityManager.find(entityClass, primaryKey);
	}
	
	@Override
    public void remove(Serializable entity) {
        boolean isArchivable = Status.class.isAssignableFrom(entity.getClass());
        if (isArchivable) {
            ((Status) entity).setArchived('Y');
            merge(entity);
        } else {
            standardEntityManager.remove(entity);
            standardEntityManager.flush();
        }
	}
	
	@Override
    public void clear() {
		standardEntityManager.clear();
	}

    @Override
    public PersistentClass getPersistentClass(String targetClassName) {
		return ejb3ConfigurationDao.getConfiguration().getClassMapping(targetClassName);
	}
	
	/* (non-Javadoc)
	 * @see org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao#getAllPolymorphicEntitiesFromCeiling(java.lang.Class)
	 */
	@Override
    public Class<?>[] getAllPolymorphicEntitiesFromCeiling(Class<?> ceilingClass) {
        Class<?>[] cache;
        synchronized(LOCK_OBJECT) {
            cache = POLYMORPHIC_ENTITY_CACHE.get(ceilingClass);
            if (cache == null) {
                List<Class<?>> entities = new ArrayList<Class<?>>();
                for (Object item : getSessionFactory().getAllClassMetadata().values()) {
                    ClassMetadata metadata = (ClassMetadata) item;
                    Class<?> mappedClass = metadata.getMappedClass(EntityMode.POJO);
                    if (mappedClass != null && ceilingClass.isAssignableFrom(mappedClass)) {
                        entities.add(mappedClass);
                    }
                }
                /*
                 * Sort entities with the most derived appearing first
                 */
                Class<?>[] sortedEntities = new Class<?>[entities.size()];
                List<Class<?>> stageItems = new ArrayList<Class<?>>();
                stageItems.add(ceilingClass);
                int j = 0;
                while (j < sortedEntities.length) {
                    List<Class<?>> newStageItems = new ArrayList<Class<?>>();
                    boolean topLevelClassFound = false;
                    for (Class<?> stageItem : stageItems) {
                        Iterator<Class<?>> itr = entities.iterator();
                        while(itr.hasNext()) {
                            Class<?> entity = itr.next();
                            checkitem: {
                                if (ArrayUtils.contains(entity.getInterfaces(), stageItem) || entity.equals(stageItem)) {
                                    topLevelClassFound = true;
                                    break checkitem;
                                }
                                
                                if (topLevelClassFound) {
                                    continue;
                                }
                                
                                if (entity.getSuperclass().equals(stageItem)) {
                                    break checkitem;
                                }
                                
                                continue;
                            }
                            sortedEntities[j] = entity;
                            itr.remove();
                            j++;
                            newStageItems.add(entity);
                        }
                    }
                    if (newStageItems.isEmpty()) {
                        throw new IllegalArgumentException("There was a gap in the inheritance hierarchy for (" + ceilingClass.getName() + ")");
                    }
                    stageItems = newStageItems;
                }
                ArrayUtils.reverse(sortedEntities);
                cache = sortedEntities;
                POLYMORPHIC_ENTITY_CACHE.put(ceilingClass, sortedEntities);
            }
        }

        return cache;
	}

    protected void addClassToTree(Class<?> clazz, ClassTree tree) {
        Class<?> testClass;
        try {
            testClass = Class.forName(tree.getFullyQualifiedClassname());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (clazz.equals(testClass)) {
            return;
        }
        if (clazz.getSuperclass().equals(testClass)) {
            ClassTree myTree = new ClassTree(clazz.getName());
            createClassTreeFromAnnotation(clazz, myTree);
            tree.setChildren((ClassTree[]) ArrayUtils.add(tree.getChildren(), myTree));
        } else {
            for (ClassTree child : tree.getChildren()) {
                addClassToTree(clazz, child);
            }
        }
    }

    protected void createClassTreeFromAnnotation(Class<?> clazz, ClassTree myTree) {
        AdminPresentationClass classPresentation = clazz.getAnnotation(AdminPresentationClass.class);
        if (classPresentation != null) {
            String friendlyName = classPresentation.friendlyName();
            if (!StringUtils.isEmpty(friendlyName)) {
                myTree.setFriendlyName(friendlyName);
            }
        }
    }

    @Override
    public ClassTree getClassTree(Class<?>[] polymorphicClasses) {
        String ceilingClass = null;
        for (Class<?> clazz : polymorphicClasses) {
            AdminPresentationClass classPresentation = clazz.getAnnotation(AdminPresentationClass.class);
            if (classPresentation != null) {
               String ceilingEntity = classPresentation.ceilingDisplayEntity();
                if (!StringUtils.isEmpty(ceilingEntity)) {
                    ceilingClass = ceilingEntity;
                    break;
                }
            }
        }
        if (ceilingClass != null) {
            int pos = -1;
            int j = 0;
            for (Class<?> clazz : polymorphicClasses) {
                if (clazz.getName().equals(ceilingClass)) {
                    pos = j;
                    break;
                }
                j++;
            }
            if (pos >= 0) {
                Class<?>[] temp = new Class<?>[pos + 1];
                System.arraycopy(polymorphicClasses, 0, temp, 0, j + 1);
                polymorphicClasses = temp;
            }
        }
        
        ClassTree classTree = null;
        if (!ArrayUtils.isEmpty(polymorphicClasses)) {
            Class<?> topClass = polymorphicClasses[polymorphicClasses.length-1];
            classTree = new ClassTree(topClass.getName());
            createClassTreeFromAnnotation(topClass, classTree);
            for (int j=polymorphicClasses.length-1; j >= 0; j--) {
                addClassToTree(polymorphicClasses[j], classTree);
            }
            classTree.finalizeStructure(1);
        }
        return classTree;
    }

    @Override
    public ClassTree getClassTreeFromCeiling(Class<?> ceilingClass) {
        Class<?>[] sortedEntities = getAllPolymorphicEntitiesFromCeiling(ceilingClass);
        return getClassTree(sortedEntities);
    }
	
	@Override
    public Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective) {
        Class<?>[] entityClasses;
        try {
            entityClasses = getAllPolymorphicEntitiesFromCeiling(Class.forName(entityName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (!ArrayUtils.isEmpty(entityClasses)) {
            return getMergedProperties(
                entityName,
                entityClasses,
                (ForeignKey) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY),
                persistencePerspective.getAdditionalNonPersistentProperties(),
                persistencePerspective.getAdditionalForeignKeys(),
                MergedPropertyType.PRIMARY,
                persistencePerspective.getPopulateToOneFields(),
                persistencePerspective.getIncludeFields(),
                persistencePerspective.getExcludeFields(),
                persistencePerspective.getConfigurationKey(),
                ""
            );
        } else {
            Map<String, FieldMetadata> mergedProperties = new HashMap<String, FieldMetadata>();
            Class<?> targetClass;
            try {
                targetClass = Class.forName(entityName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            Map<String, FieldMetadata> attributesMap = metadata.getFieldPresentationAttributes(targetClass, this);

            for (String property : attributesMap.keySet()) {
                FieldMetadata presentationAttributes = attributesMap.get(property);
                if (!presentationAttributes.getExcluded()) {
                    Field field = FieldManager.getSingleField(targetClass, property);
                    if (!Modifier.isStatic(field.getModifiers())) {
                        if (field.getAnnotation(AdminPresentationCollection.class) == null && field.getAnnotation(AdminPresentationAdornedTargetCollection.class) == null && field.getAnnotation(AdminPresentationMap.class) == null) {
                            buildProperty(targetClass, null, new ForeignKey[]{}, MergedPropertyType.PRIMARY, null, mergedProperties, null, "", property, null, false, 0, presentationAttributes, ((BasicFieldMetadata) presentationAttributes).getExplicitFieldType(), field.getType());
                        } else {
                            CollectionMetadata fieldMetadata = (CollectionMetadata) presentationAttributes;
                            if (StringUtils.isEmpty(fieldMetadata.getCollectionCeilingEntity()) && field.getAnnotation(AdminPresentationMap.class) == null) {
                                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                                Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                                fieldMetadata.setCollectionCeilingEntity(listClass.getName());
                            }
                            mergedProperties.put(property, fieldMetadata);
                        }
                    }
                }
            }

            return mergedProperties;
        }
	}

    @Override
    public Map<String, FieldMetadata> getMergedProperties(
		String ceilingEntityFullyQualifiedClassname,
		Class<?>[] entities,
		ForeignKey foreignField,
		String[] additionalNonPersistentProperties,
		ForeignKey[] additionalForeignFields,
		MergedPropertyType mergedPropertyType,
		Boolean populateManyToOneFields,
		String[] includeFields,
		String[] excludeFields,
        String configurationKey,
		String prefix
	) {
        Map<String, FieldMetadata> mergedProperties = getMergedPropertiesRecursively(
            ceilingEntityFullyQualifiedClassname,
            entities,
            foreignField,
            additionalNonPersistentProperties,
            additionalForeignFields,
            mergedPropertyType,
            populateManyToOneFields,
            includeFields,
            excludeFields,
            configurationKey,
            new ArrayList<Class<?>>(),
            prefix,
            false
        );

        final List<String> removeKeys = new ArrayList<String>();

        for (final String key : mergedProperties.keySet()) {
            if (mergedProperties.get(key).getExcluded() != null && mergedProperties.get(key).getExcluded()) {
                mergedProperties.get(key).accept(new MetadataVisitorAdapter() {
                    @Override
                    public void visit(BasicFieldMetadata metadata) {
                        removeKeys.add(key);
                    }

                    @Override
                    public void visit(AdornedTargetCollectionMetadata metadata) {
                        removeKeys.add(key);
                    }

                    @Override
                    public void visit(BasicCollectionMetadata metadata) {
                        removeKeys.add(key);
                    }

                    @Override
                    public void visit(MapMetadata metadata) {
                        removeKeys.add(key);
                    }
                });
            }
        }

        for (String removeKey : removeKeys) {
            mergedProperties.remove(removeKey);
        }

        return mergedProperties;
    }

	protected Map<String, FieldMetadata> getMergedPropertiesRecursively(
        final String ceilingEntityFullyQualifiedClassname,
        final Class<?>[] entities,
        final ForeignKey foreignField,
        final String[] additionalNonPersistentProperties,
        final ForeignKey[] additionalForeignFields,
        final MergedPropertyType mergedPropertyType,
        final Boolean populateManyToOneFields,
        final String[] includeFields,
        final String[] excludeFields,
        final String configurationKey,
        final List<Class<?>> parentClasses,
        final String prefix,
        final Boolean isParentExcluded
    ) {
        PropertyBuilder propertyBuilder = new PropertyBuilder() {
            @Override
            public Map<String, FieldMetadata> execute(Boolean overridePopulateManyToOne) {
                Map<String, FieldMetadata> mergedProperties = new HashMap<String, FieldMetadata>();
                Boolean classAnnotatedPopulateManyToOneFields;
                if (overridePopulateManyToOne != null) {
                    classAnnotatedPopulateManyToOneFields = overridePopulateManyToOne;
                } else {
                    classAnnotatedPopulateManyToOneFields = populateManyToOneFields;
                }

                buildPropertiesFromPolymorphicEntities(
                    entities,
                    foreignField,
                    additionalNonPersistentProperties,
                    additionalForeignFields,
                    mergedPropertyType,
                    classAnnotatedPopulateManyToOneFields,
                    includeFields,
                    excludeFields,
                    configurationKey,
                    ceilingEntityFullyQualifiedClassname,
                    mergedProperties,
                    parentClasses,
                    prefix,
                    isParentExcluded
                );

                return mergedProperties;
            }
        };

        Map<String, FieldMetadata> mergedProperties = metadata.overrideMetadata(entities, propertyBuilder, prefix, isParentExcluded, ceilingEntityFullyQualifiedClassname, configurationKey, this);
        applyIncludesAndExcludes(includeFields, excludeFields, prefix, isParentExcluded, mergedProperties);
        applyForeignKeyPrecedence(foreignField, additionalForeignFields, mergedProperties);

        return mergedProperties;
	}

    protected void applyForeignKeyPrecedence(ForeignKey foreignField, ForeignKey[] additionalForeignFields, Map<String, FieldMetadata> mergedProperties) {
        for (String key : mergedProperties.keySet()) {
            boolean isForeign = false;
            if (foreignField != null) {
                isForeign = foreignField.getManyToField().equals(key);
            }
            if (!isForeign && !ArrayUtils.isEmpty(additionalForeignFields)) {
                for (ForeignKey foreignKey : additionalForeignFields) {
                    isForeign = foreignKey.getManyToField().equals(key);
                    if (isForeign) {
                        break;
                    }
                }
            }
            if (isForeign) {
                FieldMetadata metadata = mergedProperties.get(key);
                metadata.setExcluded(false);
            }
        }
    }

    protected void applyIncludesAndExcludes(String[] includeFields, String[] excludeFields, String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties) {
        //check includes
        if (!ArrayUtils.isEmpty(includeFields)) {
            for (String include : includeFields) {
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if (!(testKey.startsWith(include + ".") || testKey.equals(include))) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        LOG.debug("applyIncludesAndExcludes:Excluding " + key + " because this field did not appear in the explicit includeFields list");
                        metadata.setExcluded(true);
                    } else {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            LOG.debug("applyIncludesAndExcludes:Showing " + key + " because this field appears in the explicit includeFields list");
                            metadata.setExcluded(false);
                        }
                    }
                }
            }
        } else if (!ArrayUtils.isEmpty(excludeFields)) {
            //check excludes
            for (String exclude : excludeFields) {
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if (testKey.startsWith(exclude + ".") || testKey.equals(exclude)) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        LOG.debug("applyIncludesAndExcludes:Excluding " + key + " because this field appears in the explicit excludeFields list");
                        metadata.setExcluded(true);
                    } else {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            LOG.debug("applyIncludesAndExcludes:Showing " + key + " because this field did not appear in the explicit excludeFields list");
                            metadata.setExcluded(false);
                        }
                    }
                }
            }
        }
    }

    protected String pad(String s, int length, char pad) {
        StringBuilder buffer = new StringBuilder(s);
        while (buffer.length() < length) {
            buffer.insert(0, pad);
        }
        return buffer.toString();
    }

    protected String getCacheKey(ForeignKey foreignField, String[] additionalNonPersistentProperties, ForeignKey[] additionalForeignFields, MergedPropertyType mergedPropertyType, Boolean populateManyToOneFields, Class<?> clazz, String configurationKey, Boolean isParentExcluded) {
        StringBuilder sb = new StringBuilder(150);
        sb.append(clazz.hashCode());
        sb.append(foreignField==null?"":foreignField.toString());
        sb.append(configurationKey);
        sb.append(isParentExcluded);
        if (additionalNonPersistentProperties != null) {
            for (String prop : additionalNonPersistentProperties) {
                sb.append(prop);
            }
        }
        if (additionalForeignFields != null) {
            for (ForeignKey key : additionalForeignFields) {
                sb.append(key.toString());
            }
        }
        sb.append(mergedPropertyType);
        sb.append(populateManyToOneFields);

        String digest;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(sb.toString().getBytes());
            BigInteger number = new BigInteger(1,messageDigest);
            digest = number.toString(16);
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return pad(digest, 32, '0');
    }

	protected void buildPropertiesFromPolymorphicEntities(
		Class<?>[] entities,
		ForeignKey foreignField, 
		String[] additionalNonPersistentProperties, 
		ForeignKey[] additionalForeignFields, 
		MergedPropertyType mergedPropertyType, 
		Boolean populateManyToOneFields, 
		String[] includeFields, 
		String[] excludeFields,
        String configurationKey,
        String ceilingEntityFullyQualifiedClassname,
		Map<String, FieldMetadata> mergedProperties, 
        List<Class<?>> parentClasses,
		String prefix,
        Boolean isParentExcluded
	) {
		for (Class<?> clazz : entities) {
            String cacheKey = getCacheKey(foreignField, additionalNonPersistentProperties, additionalForeignFields, mergedPropertyType, populateManyToOneFields, clazz, configurationKey, isParentExcluded);

            Map<String, FieldMetadata> cacheData;
            synchronized(LOCK_OBJECT) {
                cacheData = METADATA_CACHE.get(cacheKey);
                if (cacheData == null) {
                    Map<String, FieldMetadata> props = getPropertiesForEntityClass(
                        clazz,
                        foreignField,
                        additionalNonPersistentProperties,
                        additionalForeignFields,
                        mergedPropertyType,
                        populateManyToOneFields,
                        includeFields,
                        excludeFields,
                        configurationKey,
                        ceilingEntityFullyQualifiedClassname,
                        parentClasses,
                        prefix,
                        isParentExcluded
                    );
                    //first check all the properties currently in there to see if my entity inherits from them
                    for (Class<?> clazz2 : entities) {
                        if (!clazz2.getName().equals(clazz.getName())) {
                            for (Map.Entry<String, FieldMetadata> entry : props.entrySet()) {
                                FieldMetadata metadata = entry.getValue();
                                try {
                                    if (Class.forName(metadata.getInheritedFromType()).isAssignableFrom(clazz2)) {
                                        String[] both = (String[]) ArrayUtils.addAll(metadata.getAvailableToTypes(), new String[]{clazz2.getName()});
                                        metadata.setAvailableToTypes(both);
                                    }
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    METADATA_CACHE.put(cacheKey, props);
                    cacheData = props;
                }
            }
            //clone the metadata before passing to the system
            Map<String, FieldMetadata> clonedCache = new HashMap<String, FieldMetadata>(cacheData.size());
            for (Map.Entry<String, FieldMetadata> entry : cacheData.entrySet()) {
                clonedCache.put(entry.getKey(), entry.getValue().cloneFieldMetadata());
            }
            mergedProperties.putAll(clonedCache);
        }
    }

    @Override
    public Field[] getAllFields(Class<?> targetClass) {
		Field[] allFields = new Field[]{};
		boolean eof = false;
		Class<?> currentClass = targetClass;
		while (!eof) {
			Field[] fields = currentClass.getDeclaredFields();
			allFields = (Field[]) ArrayUtils.addAll(allFields, fields);
			if (currentClass.getSuperclass() != null) {
				currentClass = currentClass.getSuperclass();
			} else {
				eof = true;
			}
		}
		
		return allFields;
	}

    @Override
    public Map<String, FieldMetadata> getPropertiesForPrimitiveClass(
		String propertyName,
		String friendlyPropertyName,
		Class<?> targetClass,
		Class<?> parentClass,
		MergedPropertyType mergedPropertyType
	) {
		Map<String, FieldMetadata> fields = new HashMap<String, FieldMetadata>();
        BasicFieldMetadata presentationAttribute = new BasicFieldMetadata();
		presentationAttribute.setFriendlyName(friendlyPropertyName);
		if (String.class.isAssignableFrom(targetClass)) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.STRING);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.STRING, null, parentClass, presentationAttribute, mergedPropertyType, this));
		} else if (Boolean.class.isAssignableFrom(targetClass)) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.BOOLEAN);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.BOOLEAN, null, parentClass, presentationAttribute, mergedPropertyType, this));
		} else if (Date.class.isAssignableFrom(targetClass)) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.DATE);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.DATE, null, parentClass, presentationAttribute, mergedPropertyType, this));
		} else if (Money.class.isAssignableFrom(targetClass)) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.MONEY);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.MONEY, null, parentClass, presentationAttribute, mergedPropertyType, this));
		} else if (
				Byte.class.isAssignableFrom(targetClass) ||
				Integer.class.isAssignableFrom(targetClass) ||
				Long.class.isAssignableFrom(targetClass) ||
				Short.class.isAssignableFrom(targetClass)
			) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.INTEGER);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.INTEGER, null, parentClass, presentationAttribute, mergedPropertyType, this));
		} else if (
				Double.class.isAssignableFrom(targetClass) ||
				BigDecimal.class.isAssignableFrom(targetClass)
			) {
			presentationAttribute.setExplicitFieldType(SupportedFieldType.DECIMAL);
			presentationAttribute.setVisibility(VisibilityEnum.VISIBLE_ALL);
			fields.put(propertyName, metadata.getFieldMetadata("", propertyName, null, SupportedFieldType.DECIMAL, null, parentClass, presentationAttribute, mergedPropertyType, this));
		}
		((BasicFieldMetadata) fields.get(propertyName)).setLength(255);
        ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyCollection(false);
        ((BasicFieldMetadata) fields.get(propertyName)).setRequired(true);
        ((BasicFieldMetadata) fields.get(propertyName)).setUnique(true);
        ((BasicFieldMetadata) fields.get(propertyName)).setScale(100);
        ((BasicFieldMetadata) fields.get(propertyName)).setPrecision(100);

		return fields;
	}

    protected SessionFactory getSessionFactory() {
        return ((HibernateEntityManager) standardEntityManager).getSession().getSessionFactory();
    }

    @Override
    public Map<String, Object> getIdMetadata(Class<?> entityClass) {
        Map<String, Object> response = new HashMap<String, Object>();
        SessionFactory sessionFactory = getSessionFactory();
        ClassMetadata metadata = sessionFactory.getClassMetadata(entityClass);
        String idProperty = metadata.getIdentifierPropertyName();
        response.put("name", idProperty);
        Type idType = metadata.getIdentifierType();
        response.put("type", idType);

        return response;
    }

    @Override
    public List<String> getPropertyNames(Class<?> entityClass) {
        ClassMetadata metadata = getSessionFactory().getClassMetadata(entityClass);
        List<String> propertyNames = new ArrayList<String>();
        Collections.addAll(propertyNames, metadata.getPropertyNames());
        return propertyNames;
    }

    @Override
    public List<Type> getPropertyTypes(Class<?> entityClass) {
        ClassMetadata metadata = getSessionFactory().getClassMetadata(entityClass);
        List<Type> propertyTypes = new ArrayList<Type>();
        Collections.addAll(propertyTypes, metadata.getPropertyTypes());
        return propertyTypes;
    }

	protected Map<String, FieldMetadata> getPropertiesForEntityClass(
		Class<?> targetClass,
		ForeignKey foreignField,
		String[] additionalNonPersistentProperties,
		ForeignKey[] additionalForeignFields,
		MergedPropertyType mergedPropertyType,
		Boolean populateManyToOneFields,
		String[] includeFields,
		String[] excludeFields,
        String configurationKey,
        String ceilingEntityFullyQualifiedClassname,
        List<Class<?>> parentClasses,
		String prefix,
        Boolean isParentExcluded
	) {
		Map<String, FieldMetadata> presentationAttributes = metadata.getFieldPresentationAttributes(targetClass, this);
        if (isParentExcluded) {
            for (String key : presentationAttributes.keySet()) {
                LOG.debug("getPropertiesForEntityClass:Excluding " + key + " because parent is excluded.");
                presentationAttributes.get(key).setExcluded(true);
            }
        }

        Map idMetadata = getIdMetadata(targetClass);
		Map<String, FieldMetadata> fields = new HashMap<String, FieldMetadata>();
		String idProperty = (String) idMetadata.get("name");
		List<String> propertyNames = getPropertyNames(targetClass);
		propertyNames.add(idProperty);
		Type idType = (Type) idMetadata.get("type");
        List<Type> propertyTypes = getPropertyTypes(targetClass);
		propertyTypes.add(idType);

		PersistentClass persistentClass = getPersistentClass(targetClass.getName());
		Iterator testIter = persistentClass.getPropertyIterator();
        List<Property> propertyList = new ArrayList<Property>();

		//check the properties for problems
		while(testIter.hasNext()) {
			Property property = (Property) testIter.next();
			if (property.getName().contains(".")) {
				throw new IllegalArgumentException("Properties from entities that utilize a period character ('.') in their name are incompatible with this system. The property name in question is: (" + property.getName() + ") from the class: (" + targetClass.getName() + ")");
			}
            propertyList.add(property);
		}

		buildProperties(
			targetClass,
			foreignField,
			additionalForeignFields,
			additionalNonPersistentProperties,
			mergedPropertyType,
			presentationAttributes,
			propertyList,
			fields,
			propertyNames,
			propertyTypes,
			idProperty,
			populateManyToOneFields,
			includeFields,
			excludeFields,
            configurationKey,
            ceilingEntityFullyQualifiedClassname,
            parentClasses,
			prefix,
            isParentExcluded
		);
		BasicFieldMetadata presentationAttribute = new BasicFieldMetadata();
		presentationAttribute.setExplicitFieldType(SupportedFieldType.STRING);
		presentationAttribute.setVisibility(VisibilityEnum.HIDDEN_ALL);
		if (!ArrayUtils.isEmpty(additionalNonPersistentProperties)) {
            Class<?>[] entities = getAllPolymorphicEntitiesFromCeiling(targetClass);
			for (String additionalNonPersistentProperty : additionalNonPersistentProperties) {
                if (StringUtils.isEmpty(prefix) || (!StringUtils.isEmpty(prefix) && additionalNonPersistentProperty.startsWith(prefix))) {
                    String myAdditionalNonPersistentProperty = additionalNonPersistentProperty;
                    //get final property if this is a dot delimited property
                    int finalDotPos = additionalNonPersistentProperty.lastIndexOf('.');
                    if (finalDotPos >= 0) {
                        myAdditionalNonPersistentProperty = myAdditionalNonPersistentProperty.substring(finalDotPos + 1, myAdditionalNonPersistentProperty.length());
                    }
                    //check all the polymorphic types on this target class to see if the end property exists
                    Field testField = null;
                    Method testMethod = null;
                    for (Class<?> clazz : entities) {
                        try {
                            testMethod = clazz.getMethod(myAdditionalNonPersistentProperty);
                            if (testMethod != null) {
                                break;
                            }
                        } catch (NoSuchMethodException e) {
                            //do nothing - method does not exist
                        }
                        testField = getFieldManager().getField(clazz, myAdditionalNonPersistentProperty);
                        if (testField != null) {
                            break;
                        }
                    }
                    //if the property exists, add it to the metadata for this class
                    if (testField != null || testMethod != null) {
                        fields.put(additionalNonPersistentProperty, metadata.getFieldMetadata(prefix, additionalNonPersistentProperty, propertyList, SupportedFieldType.STRING, null, targetClass, presentationAttribute, mergedPropertyType, this));
                    }
                }
			}
		}

		return fields;
	}

	protected void buildProperties(
		Class<?> targetClass,
		ForeignKey foreignField,
		ForeignKey[] additionalForeignFields,
		String[] additionalNonPersistentProperties,
		MergedPropertyType mergedPropertyType,
		Map<String, FieldMetadata> presentationAttributes,
		List<Property> componentProperties,
		Map<String, FieldMetadata> fields,
		List<String> propertyNames,
		List<Type> propertyTypes,
		String idProperty,
		Boolean populateManyToOneFields,
		String[] includeFields,
		String[] excludeFields,
        String configurationKey,
        String ceilingEntityFullyQualifiedClassname,
        List<Class<?>> parentClasses,
		String prefix,
        Boolean isParentExcluded
	) {
		int j = 0;
		for (String propertyName : propertyNames) {
			final Type type = propertyTypes.get(j);
			boolean isPropertyForeignKey = testForeignProperty(foreignField, prefix, propertyName);
			int additionalForeignKeyIndexPosition = findAdditionalForeignKeyIndex(additionalForeignFields, prefix, propertyName);
			j++;
            Field myField = FieldManager.getSingleField(targetClass, propertyName);
			if (
					!type.isAnyType() && !type.isCollectionType() ||
					isPropertyForeignKey ||
					additionalForeignKeyIndexPosition >= 0 ||
					presentationAttributes.containsKey(propertyName)
			) {
                if (myField != null && (
                        myField.getAnnotation(AdminPresentationCollection.class) != null ||
                        myField.getAnnotation(AdminPresentationAdornedTargetCollection.class) != null ||
                        myField.getAnnotation(AdminPresentationMap.class) != null)
                ) {
                    final CollectionMetadata fieldMetadata = (CollectionMetadata) presentationAttributes.get(propertyName);
                    fieldMetadata.setInheritedFromType(targetClass.getName());
                    fieldMetadata.setAvailableToTypes(new String[]{targetClass.getName()});
                    fields.put(propertyName, fieldMetadata);
                    fieldMetadata.accept(new MetadataVisitorAdapter() {
                        @Override
                        public void visit(AdornedTargetCollectionMetadata metadata) {
                            if (StringUtils.isEmpty(fieldMetadata.getCollectionCeilingEntity())) {
                                fieldMetadata.setCollectionCeilingEntity(type.getReturnedClass().getName());
                                AdornedTargetList targetList = ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST));
                                targetList.setAdornedTargetEntityClassname(metadata.getCollectionCeilingEntity());
                            }
                        }

                        @Override
                        public void visit(BasicCollectionMetadata metadata) {
                            //do nothing
                        }

                        @Override
                        public void visit(MapMetadata metadata) {
                            //do nothing
                        }
                    });
                } else {
                    FieldMetadata presentationAttribute = presentationAttributes.get(propertyName);
                    Boolean amIExcluded = isParentExcluded || !testPropertyInclusion(presentationAttribute);
                    Boolean includeField = testPropertyRecursion(prefix, parentClasses, propertyName, targetClass, ceilingEntityFullyQualifiedClassname);

                    SupportedFieldType explicitType = null;
                    if (presentationAttribute != null && presentationAttribute instanceof BasicFieldMetadata) {
                        explicitType = ((BasicFieldMetadata) presentationAttribute).getExplicitFieldType();
                    }
                    Class<?> returnedClass = type.getReturnedClass();
                    checkProp: {
                        if (type.isComponentType() && includeField) {
                            buildComponentProperties(
                                targetClass,
                                foreignField,
                                additionalForeignFields,
                                additionalNonPersistentProperties,
                                mergedPropertyType,
                                fields,
                                idProperty,
                                populateManyToOneFields,
                                includeFields,
                                excludeFields,
                                configurationKey,
                                ceilingEntityFullyQualifiedClassname,
                                propertyName,
                                type,
                                returnedClass,
                                parentClasses,
                                amIExcluded,
                                prefix
                            );
                            break checkProp;
                        }
                        /*
                         * Currently we do not support ManyToOne fields whose class type is the same
                         * as the target type, since this forms an infinite loop and will cause a stack overflow.
                         */
                        if (
                            type.isEntityType() &&
                            !returnedClass.isAssignableFrom(targetClass) &&
                            populateManyToOneFields &&
                            includeField
                        ) {
                            buildEntityProperties(
                                fields,
                                foreignField,
                                additionalForeignFields,
                                additionalNonPersistentProperties,
                                populateManyToOneFields,
                                includeFields,
                                excludeFields,
                                configurationKey,
                                ceilingEntityFullyQualifiedClassname,
                                propertyName,
                                returnedClass,
                                targetClass,
                                parentClasses,
                                prefix,
                                amIExcluded
                            );
                            break checkProp;
                        }
                    }
                    //Don't include this property if it failed manyToOne inclusion and is not a specified foreign key
                    if (includeField || isPropertyForeignKey || additionalForeignKeyIndexPosition >= 0) {
                        buildProperty(
                            targetClass,
                            foreignField,
                            additionalForeignFields,
                            mergedPropertyType,
                            componentProperties,
                            fields,
                            idProperty,
                            prefix,
                            propertyName,
                            type,
                            isPropertyForeignKey,
                            additionalForeignKeyIndexPosition,
                            presentationAttribute,
                            explicitType,
                            returnedClass
                        );
                    }
                }
			}
		}
	}

	protected void buildProperty(
		Class<?> targetClass, 
		ForeignKey foreignField, 
		ForeignKey[] additionalForeignFields, 
		MergedPropertyType mergedPropertyType, 
		List<Property> componentProperties,
		Map<String, FieldMetadata> fields, 
		String idProperty, 
		String prefix,
		String propertyName, 
		Type type, 
		boolean isPropertyForeignKey, 
		int additionalForeignKeyIndexPosition, 
		FieldMetadata presentationAttribute,
		SupportedFieldType explicitType, 
		Class<?> returnedClass
	) {
        if (
            explicitType != null &&
            explicitType != SupportedFieldType.UNKNOWN &&
            explicitType != SupportedFieldType.BOOLEAN &&
            explicitType != SupportedFieldType.INTEGER &&
            explicitType != SupportedFieldType.DATE &&
            explicitType != SupportedFieldType.STRING &&
            explicitType != SupportedFieldType.MONEY &&
            explicitType != SupportedFieldType.DECIMAL &&
            explicitType != SupportedFieldType.FOREIGN_KEY &&
            explicitType != SupportedFieldType.ADDITIONAL_FOREIGN_KEY
        ) {
            fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, explicitType, type, targetClass, presentationAttribute, mergedPropertyType, this));
        } else if (
            explicitType != null &&
            explicitType == SupportedFieldType.BOOLEAN
            ||
            returnedClass.equals(Boolean.class) ||
            returnedClass.equals(Character.class)
        ) {
			fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.BOOLEAN, type, targetClass, presentationAttribute, mergedPropertyType, this));
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.INTEGER
            ||
            returnedClass.equals(Byte.class) ||
            returnedClass.equals(Short.class) ||
            returnedClass.equals(Integer.class) ||
            returnedClass.equals(Long.class)
		) {
			if (propertyName.equals(idProperty)) {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.ID, SupportedFieldType.INTEGER, type, targetClass, presentationAttribute, mergedPropertyType, this));
			} else {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.INTEGER, type, targetClass, presentationAttribute, mergedPropertyType, this));
			}
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.DATE
            ||
            returnedClass.equals(Calendar.class) ||
            returnedClass.equals(Date.class) ||
            returnedClass.equals(Timestamp.class)
		) {
			fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.DATE, type, targetClass, presentationAttribute, mergedPropertyType, this));
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.STRING
            ||
            returnedClass.equals(String.class)
		) {
			if (propertyName.equals(idProperty)) {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.ID, SupportedFieldType.STRING, type, targetClass, presentationAttribute, mergedPropertyType, this));
			} else {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.STRING, type, targetClass, presentationAttribute, mergedPropertyType, this));
			}
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.MONEY
            ||
            returnedClass.equals(Money.class)
        ) {
			fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.MONEY, type, targetClass, presentationAttribute, mergedPropertyType, this));
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.DECIMAL
            ||
            returnedClass.equals(Double.class) ||
            returnedClass.equals(BigDecimal.class)
		) {
			fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.DECIMAL, type, targetClass, presentationAttribute, mergedPropertyType, this));
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.FOREIGN_KEY
            ||
            foreignField != null &&
            isPropertyForeignKey
        ) {
			ClassMetadata foreignMetadata;
            String foreignKeyClass;
            String lookupDisplayProperty;
            if (foreignField == null) {
                Class<?>[] entities = getAllPolymorphicEntitiesFromCeiling(type.getReturnedClass());
                foreignMetadata = getSessionFactory().getClassMetadata(entities[entities.length-1]);
                foreignKeyClass = entities[entities.length-1].getName();
                lookupDisplayProperty = ((BasicFieldMetadata) presentationAttribute).getLookupDisplayProperty();
            } else {
                try {
                    foreignMetadata = getSessionFactory().getClassMetadata(Class.forName(foreignField.getForeignKeyClass()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                foreignKeyClass = foreignField.getForeignKeyClass();
                lookupDisplayProperty = foreignField.getDisplayValueProperty();
            }
            Class<?> foreignResponseType = foreignMetadata.getIdentifierType().getReturnedClass();
			if (foreignResponseType.equals(String.class)) {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.FOREIGN_KEY, SupportedFieldType.STRING, type, targetClass, presentationAttribute, mergedPropertyType, this));
			} else {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.FOREIGN_KEY, SupportedFieldType.INTEGER, type, targetClass, presentationAttribute, mergedPropertyType, this));
			}
			((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyProperty(foreignMetadata.getIdentifierPropertyName());
            ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyClass(foreignKeyClass);
            ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyDisplayValueProperty(lookupDisplayProperty);
		} else if (
            explicitType != null &&
            explicitType == SupportedFieldType.ADDITIONAL_FOREIGN_KEY
            ||
            additionalForeignFields != null &&
            additionalForeignKeyIndexPosition >= 0
        ) {
            if (!type.isEntityType()) {
                throw new IllegalArgumentException("Only ManyToOne and OneToOne fields can be marked as a SupportedFieldType of ADDITIONAL_FOREIGN_KEY");
            }
			ClassMetadata foreignMetadata;
            String foreignKeyClass;
            String lookupDisplayProperty;
            if (additionalForeignKeyIndexPosition < 0) {
                Class<?>[] entities = getAllPolymorphicEntitiesFromCeiling(type.getReturnedClass());
                foreignMetadata = getSessionFactory().getClassMetadata(entities[entities.length-1]);
                foreignKeyClass = entities[entities.length-1].getName();
                lookupDisplayProperty = ((BasicFieldMetadata) presentationAttribute).getLookupDisplayProperty();
            } else {
                try {
                    foreignMetadata = getSessionFactory().getClassMetadata(Class.forName(additionalForeignFields[additionalForeignKeyIndexPosition].getForeignKeyClass()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                foreignKeyClass = additionalForeignFields[additionalForeignKeyIndexPosition].getForeignKeyClass();
                lookupDisplayProperty = additionalForeignFields[additionalForeignKeyIndexPosition].getDisplayValueProperty();
            }
            Class<?> foreignResponseType = foreignMetadata.getIdentifierType().getReturnedClass();
			if (foreignResponseType.equals(String.class)) {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.ADDITIONAL_FOREIGN_KEY, SupportedFieldType.STRING, type, targetClass, presentationAttribute, mergedPropertyType, this));
			} else {
				fields.put(propertyName, metadata.getFieldMetadata(prefix, propertyName, componentProperties, SupportedFieldType.ADDITIONAL_FOREIGN_KEY, SupportedFieldType.INTEGER, type, targetClass, presentationAttribute, mergedPropertyType, this));
			}
            ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyProperty(foreignMetadata.getIdentifierPropertyName());
            ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyClass(foreignKeyClass);
            ((BasicFieldMetadata) fields.get(propertyName)).setForeignKeyDisplayValueProperty(lookupDisplayProperty);
		}
		//return type not supported - just skip this property
	}

	protected Boolean testPropertyRecursion(String prefix, List<Class<?>> parentClasses, String propertyName, Class<?> targetClass, String ceilingEntityFullyQualifiedClassname) {
        Boolean includeField = true;
        if (!StringUtils.isEmpty(prefix)) {
            Field testField = getFieldManager().getField(targetClass, propertyName);
            if (testField == null) {
                Class<?>[] entities;
                try {
                    entities = getAllPolymorphicEntitiesFromCeiling(Class.forName(ceilingEntityFullyQualifiedClassname));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                for (Class<?> clazz : entities) {
                    testField = getFieldManager().getField(clazz, propertyName);
                    if (testField != null) {
                        break;
                    }
                }
                String testProperty = prefix + propertyName;
                if (testField == null) {
                    testField = getFieldManager().getField(targetClass, testProperty);
                }
                if (testField == null) {
                    for (Class<?> clazz : entities) {
                        testField = getFieldManager().getField(clazz, testProperty);
                        if (testField != null) {
                            break;
                        }
                    }
                }
            }
            if (testField != null) {
                Class<?> testType = testField.getType();
                for (Class<?> parentClass : parentClasses) {
                    if (parentClass.isAssignableFrom(testType) || testType.isAssignableFrom(parentClass)) {
                        includeField = false;
                        break;
                    }
                }
                if (includeField && (targetClass.isAssignableFrom(testType) || testType.isAssignableFrom(targetClass))) {
                    includeField = false;
                }
            }
        }
		return includeField;
	}

    public Boolean testPropertyInclusion(FieldMetadata presentationAttribute) {
        return !(presentationAttribute != null && ((presentationAttribute.getExcluded() != null && presentationAttribute.getExcluded()) || (presentationAttribute.getChildrenExcluded() != null && presentationAttribute.getChildrenExcluded())));
    }

    protected boolean testForeignProperty(ForeignKey foreignField, String prefix, String propertyName) {
		boolean isPropertyForeignKey = false;
		if (foreignField != null) {
			isPropertyForeignKey = foreignField.getManyToField().equals(prefix + propertyName);
		}
		return isPropertyForeignKey;
	}

	protected int findAdditionalForeignKeyIndex(ForeignKey[] additionalForeignFields, String prefix, String propertyName) {
		int additionalForeignKeyIndexPosition = -1;
		if (additionalForeignFields != null) {
			additionalForeignKeyIndexPosition = Arrays.binarySearch(additionalForeignFields, new ForeignKey(prefix + propertyName, null, null), new Comparator<ForeignKey>() {
				@Override
                public int compare(ForeignKey o1, ForeignKey o2) {
					return o1.getManyToField().compareTo(o2.getManyToField());
				}
			});
		}
		return additionalForeignKeyIndexPosition;
	}

	protected void buildEntityProperties(
		Map<String, FieldMetadata> fields, 
		ForeignKey foreignField, 
		ForeignKey[] additionalForeignFields, 
		String[] additionalNonPersistentProperties, 
		Boolean populateManyToOneFields, 
		String[] includeFields, 
		String[] excludeFields,
        String configurationKey,
        String ceilingEntityFullyQualifiedClassname,
		String propertyName, 
		Class<?> returnedClass, 
		Class<?> targetClass, 
        List<Class<?>> parentClasses,
		String prefix,
        Boolean isParentExcluded
	) {
		Class<?>[] polymorphicEntities = getAllPolymorphicEntitiesFromCeiling(returnedClass);
        List<Class<?>> clonedParentClasses = new ArrayList<Class<?>>();
        for (Class<?> parentClass : parentClasses) {
            clonedParentClasses.add(parentClass);
        }
        clonedParentClasses.add(targetClass);
		Map<String, FieldMetadata> newFields = getMergedPropertiesRecursively(
            ceilingEntityFullyQualifiedClassname,
            polymorphicEntities,
            foreignField,
            additionalNonPersistentProperties,
            additionalForeignFields,
            MergedPropertyType.PRIMARY, 
            populateManyToOneFields,
            includeFields,
            excludeFields,
            configurationKey,
            clonedParentClasses,
            prefix + propertyName + '.',
            isParentExcluded
        );
		for (FieldMetadata newMetadata : newFields.values()) {
			newMetadata.setInheritedFromType(targetClass.getName());
			newMetadata.setAvailableToTypes(new String[]{targetClass.getName()});
		}
		Map<String, FieldMetadata> convertedFields = new HashMap<String, FieldMetadata>(newFields.size());
		for (Map.Entry<String, FieldMetadata> key : newFields.entrySet()) {
			convertedFields.put(propertyName + '.' + key.getKey(), key.getValue());
		}
		fields.putAll(convertedFields);
	}

	protected void buildComponentProperties(
		Class<?> targetClass, 
		ForeignKey foreignField, 
		ForeignKey[] additionalForeignFields, 
		String[] additionalNonPersistentProperties, 
		MergedPropertyType mergedPropertyType,
		Map<String, FieldMetadata> fields, 
		String idProperty, 
		Boolean populateManyToOneFields, 
		String[] includeFields, 
		String[] excludeFields,
        String configurationKey,
        String ceilingEntityFullyQualifiedClassname,
		String propertyName, 
		Type type, 
		Class<?> returnedClass,
        List<Class<?>> parentClasses,
        Boolean isParentExcluded,
        String prefix
	) {
		String[] componentProperties = ((ComponentType) type).getPropertyNames();
		List<String> componentPropertyNames = Arrays.asList(componentProperties);
		Type[] componentTypes = ((ComponentType) type).getSubtypes();
		List<Type> componentPropertyTypes = Arrays.asList(componentTypes);
		Map<String, FieldMetadata> componentPresentationAttributes = metadata.getFieldPresentationAttributes(returnedClass, this);
        if (isParentExcluded) {
            for (String key : componentPresentationAttributes.keySet()) {
                LOG.debug("buildComponentProperties:Excluding " + key + " because the parent was excluded");
                componentPresentationAttributes.get(key).setExcluded(true);
            }
        }
		PersistentClass persistentClass = getPersistentClass(targetClass.getName());
        Property property;
        try {
            property = persistentClass.getProperty(propertyName);
        } catch (MappingException e) {
            property = persistentClass.getProperty(prefix + propertyName);
        }
		Iterator componentPropertyIterator = ((org.hibernate.mapping.Component) property.getValue()).getPropertyIterator();
        List<Property> componentPropertyList = new ArrayList<Property>();
        while(componentPropertyIterator.hasNext()) {
            componentPropertyList.add((Property) componentPropertyIterator.next());
        }
		Map<String, FieldMetadata> newFields = new HashMap<String, FieldMetadata>();
		buildProperties(
            targetClass,
			foreignField, 
			additionalForeignFields, 
			additionalNonPersistentProperties,
			mergedPropertyType,  
			componentPresentationAttributes, 
			componentPropertyList,
			newFields, 
			componentPropertyNames, 
			componentPropertyTypes, 
			idProperty, 
			populateManyToOneFields,
			includeFields,
			excludeFields,
            configurationKey,
            ceilingEntityFullyQualifiedClassname,
            parentClasses,
			propertyName + ".",
            isParentExcluded
		);
		Map<String, FieldMetadata> convertedFields = new HashMap<String, FieldMetadata>();
		for (String key : newFields.keySet()) {
			convertedFields.put(propertyName + "." + key, newFields.get(key));
		}
		fields.putAll(convertedFields);
	}

	@Override
	public EntityManager getStandardEntityManager() {
		return standardEntityManager;
	}

	@Override
    public void setStandardEntityManager(EntityManager entityManager) {
		this.standardEntityManager = entityManager;
	}

	public EJB3ConfigurationDao getEjb3ConfigurationDao() {
		return ejb3ConfigurationDao;
	}

	public void setEjb3ConfigurationDao(EJB3ConfigurationDao ejb3ConfigurationDao) {
		this.ejb3ConfigurationDao = ejb3ConfigurationDao;
	}

    @Override
    public FieldManager getFieldManager() {
        return new FieldManager(entityConfiguration, this);
    }

    @Override
    public EntityConfiguration getEntityConfiguration() {
        return entityConfiguration;
    }

    @Override
    public void setEntityConfiguration(EntityConfiguration entityConfiguration) {
        this.entityConfiguration = entityConfiguration;
    }

}
