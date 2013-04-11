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

package org.broadleafcommerce.openadmin.server.service.persistence.module;

import com.anasoft.os.daofusion.criteria.AssociationPath;
import com.anasoft.os.daofusion.criteria.FilterCriterion;
import com.anasoft.os.daofusion.criteria.NestedPropertyCriteria;
import com.anasoft.os.daofusion.criteria.PersistentEntityCriteria;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider;
import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.anasoft.os.daofusion.cto.server.CriteriaTransferObjectCountWrapper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.PersistencePerspectiveItemType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.server.cto.BaseCtoConverter;
import org.broadleafcommerce.openadmin.server.cto.FilterCriterionProviders;
import org.broadleafcommerce.openadmin.server.service.persistence.PersistenceException;
import org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager;
import org.broadleafcommerce.openadmin.server.service.persistence.module.provider.request.AddFilterPropertiesRequest;
import org.broadleafcommerce.openadmin.server.service.persistence.module.provider.request.AddSearchMappingRequest;
import org.broadleafcommerce.openadmin.server.service.persistence.module.provider.request.ExtractValueRequest;
import org.broadleafcommerce.openadmin.server.service.persistence.module.provider.PersistenceProvider;
import org.broadleafcommerce.openadmin.server.service.persistence.module.provider.request.PopulateValueRequest;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.EntityValidatorService;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * @author jfischer
 */
@Component("blBasicPersistenceModule")
@Scope("prototype")
public class BasicPersistenceModule implements PersistenceModule, RecordHelper, ApplicationContextAware, DataFormatProvider {
    
    private static final Log LOG = LogFactory.getLog(BasicPersistenceModule.class);
    
    public static final String MAIN_ENTITY_NAME_PROPERTY = "MAIN_ENTITY_NAME";

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");

    protected DecimalFormat decimalFormat;
    protected ApplicationContext applicationContext;
    protected PersistenceManager persistenceManager;

    @Resource(name = "blEntityValidatorService")
    protected EntityValidatorService entityValidatorService;

    @Resource(name="blPersistenceProviders")
    protected List<PersistenceProvider> persistenceProviders = new ArrayList<PersistenceProvider>();

    @Resource(name="blDefaultPersistenceProvider")
    protected PersistenceProvider defaultPersistenceProvider;

    public BasicPersistenceModule() {
        decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        decimalFormat.applyPattern("0.########");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean isCompatible(OperationType operationType) {
        return OperationType.BASIC == operationType || OperationType.NONDESTRUCTIVEREMOVE == operationType;
    }

    @Override
    public FieldManager getFieldManager() {
        return persistenceManager.getDynamicEntityDao().getFieldManager();
    }
    
    @Override
    public DecimalFormat getDecimalFormatter()  {
        return decimalFormat;
    }

    @Override
    public SimpleDateFormat getSimpleDateFormatter() {
        return dateFormat;
    }

    protected Map<String, FieldMetadata> filterOutCollectionMetadata(Map<String, FieldMetadata> metadata) {
        if (metadata == null) {
            return null;
        }
        Map<String, FieldMetadata> newMap = new HashMap<String, FieldMetadata>();
        for (Map.Entry<String, FieldMetadata> entry : metadata.entrySet()) {
            if (entry.getValue() instanceof BasicFieldMetadata) {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }

        return newMap;
    }

    protected Class<?> getBasicBroadleafType(SupportedFieldType fieldType) {
        Class<?> response;
        switch (fieldType) {
            case BOOLEAN:
                response = Boolean.TYPE;
                break;
            case DATE:
                response = Date.class;
                break;
            case DECIMAL:
                response = BigDecimal.class;
                break;
            case MONEY:
                response = Money.class;
                break;
            case INTEGER:
                response = Integer.TYPE;
                break;
            case UNKNOWN:
                response = null;
                break;
            default:
                response = String.class;
                break;
        }

        return response;
    }

    @Override
    public Serializable createPopulatedInstance(Serializable instance, Entity entity, Map<String, FieldMetadata> unfilteredProperties, Boolean setId) {
        Map<String, FieldMetadata> mergedProperties = filterOutCollectionMetadata(unfilteredProperties);
        FieldManager fieldManager = getFieldManager();
        boolean handled = false;
        for (PersistenceProvider persistenceProvider : persistenceProviders) {
            if (persistenceProvider.canHandlePropertyFiltering(entity, unfilteredProperties)) {
                persistenceProvider.filterProperties(new AddFilterPropertiesRequest(entity, unfilteredProperties));
                handled = true;
            }
        }
        if (!handled) {
            defaultPersistenceProvider.filterProperties(new AddFilterPropertiesRequest(entity, unfilteredProperties));
        }
        try {
            for (Property property : entity.getProperties()) {
                BasicFieldMetadata metadata = (BasicFieldMetadata) mergedProperties.get(property.getName());
                Class<?> returnType;
                if (!property.getName().contains(FieldManager.MAPFIELDSEPARATOR)) {
                    Field field = fieldManager.getField(instance.getClass(), property.getName());
                    if (field == null) {
                        LOG.debug("Unable to find a bean property for the reported property: " + property.getName() + ". Ignoring property.");
                        continue;
                    }
                    returnType = field.getType();
                } else {
                    if (metadata == null) {
                        LOG.debug("Unable to find a metadata property for the reported property: " + property.getName() + ". Ignoring property.");
                        continue;
                    }
                    returnType = getMapFieldType(instance, fieldManager, property);
                    if (returnType == null) {
                        returnType = getBasicBroadleafType(metadata.getFieldType());
                    }
                }
                if (returnType == null) {
                    throw new IllegalAccessException("Unable to determine the value type for the property ("+property.getName()+")");
                }
                String value = property.getValue();
                if (metadata != null) {
                    Boolean mutable = metadata.getMutable();
                    Boolean readOnly = metadata.getReadOnly();

                    if (metadata.getFieldType().equals(SupportedFieldType.BOOLEAN)) {
                        if (value == null) {
                            value = "false";
                        }
                    }

                    if ((mutable == null || mutable) && (readOnly == null || !readOnly)) {
                        if (value != null) {
                            handled = false;
                            for (PersistenceProvider persistenceProvider : persistenceProviders) {
                                if (persistenceProvider.canHandlePersistence(instance, property, metadata)) {
                                    persistenceProvider.populateValue(new PopulateValueRequest(instance, setId,
                                            fieldManager, property, metadata, returnType, value, persistenceManager, this));
                                    handled = true;
                                }
                            }
                            if (!handled) {
                                defaultPersistenceProvider.populateValue(new PopulateValueRequest(instance, setId,
                                        fieldManager, property, metadata, returnType, value, persistenceManager, this));
                            }
                        } else {
                            try {
                                if (fieldManager.getFieldValue(instance, property.getName()) != null && (metadata.getFieldType() != SupportedFieldType.ID || setId)) {
                                    fieldManager.setFieldValue(instance, property.getName(), null);
                                }
                            } catch (FieldNotAvailableException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                    }
                }
            }
            Map<String, Serializable> persistedEntities = fieldManager.persistMiddleEntities();
            for (Entry<String, Serializable> entry : persistedEntities.entrySet()) {
                fieldManager.setFieldValue(instance, entry.getKey(), entry.getValue());
            }
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        }
        return instance;
    }

    protected Class<?> getMapFieldType(Serializable instance, FieldManager fieldManager, Property property) {
        Class<?> returnType = null;
        Field field = fieldManager.getField(instance.getClass(), property.getName().substring(0, property.getName().indexOf(FieldManager.MAPFIELDSEPARATOR)));
        java.lang.reflect.Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[1];
            Class<?>[] entities = persistenceManager.getDynamicEntityDao().getAllPolymorphicEntitiesFromCeiling(clazz);
            if (!ArrayUtils.isEmpty(entities)) {
                returnType = entities[entities.length-1];
            }
        }
        return returnType;
    }

    @Override
    public Entity getRecord(Map<String, FieldMetadata> primaryMergedProperties, Serializable record, Map<String, FieldMetadata> alternateMergedProperties, String pathToTargetObject) {
        List<Serializable> records = new ArrayList<Serializable>(1);
        records.add(record);
        Entity[] productEntities = getRecords(primaryMergedProperties, records, alternateMergedProperties,
                pathToTargetObject);
        return productEntities[0];
    }

    @Override
    public Entity getRecord(Class<?> ceilingEntityClass, PersistencePerspective persistencePerspective, Serializable record) {
        Map<String, FieldMetadata> mergedProperties = getSimpleMergedProperties(ceilingEntityClass.getName(), persistencePerspective);
        return getRecord(mergedProperties, record, null, null);
    }

    @Override
    public Entity[] getRecords(Class<?> ceilingEntityClass, PersistencePerspective persistencePerspective, List<? extends Serializable> records) {
        Map<String, FieldMetadata> mergedProperties = getSimpleMergedProperties(ceilingEntityClass.getName(), persistencePerspective);
        return getRecords(mergedProperties, records, null, null);
    }

    @Override
    public Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective) {
        return persistenceManager.getDynamicEntityDao().getSimpleMergedProperties(entityName, persistencePerspective);
    }

    @Override
    public Entity[] getRecords(Map<String, FieldMetadata> primaryMergedProperties, List<? extends Serializable> records) {
        return getRecords(primaryMergedProperties, records, null, null);
    }

    @Override
    public Entity[] getRecords(Map<String, FieldMetadata> primaryUnfilteredMergedProperties, List<? extends Serializable> records, Map<String, FieldMetadata> alternateUnfilteredMergedProperties, String pathToTargetObject) {
        Map<String, FieldMetadata> primaryMergedProperties = filterOutCollectionMetadata(primaryUnfilteredMergedProperties);
        Map<String, FieldMetadata> alternateMergedProperties = filterOutCollectionMetadata(alternateUnfilteredMergedProperties);
        Entity[] entities = new Entity[records.size()];
        int j = 0;
        for (Serializable recordEntity : records) {
            Serializable entity;
            if (pathToTargetObject != null) {
                try {
                    entity = (Serializable) getFieldManager().getFieldValue(recordEntity, pathToTargetObject);
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }
            } else {
                entity = recordEntity;
            }
            Entity entityItem = new Entity();
            entityItem.setType(new String[]{entity.getClass().getName()});
            entities[j] = entityItem;

            List<Property> props = new ArrayList<Property>(primaryMergedProperties.size());
            extractPropertiesFromPersistentEntity(primaryMergedProperties, entity, props);
            if (alternateMergedProperties != null) {
                extractPropertiesFromPersistentEntity(alternateMergedProperties, recordEntity, props);
            }
            
            // Try to add the "main name" property. Log a debug message if we can't
            try {
                Property p = new Property();
                p.setName(MAIN_ENTITY_NAME_PROPERTY);
                String mainEntityName = (String) MethodUtils.invokeMethod(entity, "getMainEntityName");
                p.setValue(mainEntityName);
                props.add(p);
            } catch (Exception e) {
                LOG.debug(String.format("Could not execute the getMainEntityName() method for [%s]", 
                        entity.getClass().getName()), e);
            }
            
            Property[] properties = new Property[props.size()];
            properties = props.toArray(properties);
            entityItem.setProperties(properties);
            j++;
        }

        return entities;
    }

    protected void extractPropertiesFromPersistentEntity(Map<String, FieldMetadata> mergedProperties, Serializable entity, List<Property> props) {
        FieldManager fieldManager = getFieldManager();
        try {
            for (Entry<String, FieldMetadata> entry : mergedProperties.entrySet()) {
                String property = entry.getKey();
                BasicFieldMetadata metadata = (BasicFieldMetadata) entry.getValue();
                if (Class.forName(metadata.getInheritedFromType()).isAssignableFrom(entity.getClass()) || entity.getClass().isAssignableFrom(Class.forName(metadata.getInheritedFromType()))) {
                    boolean proceed = true;
                    if (property.contains(".")) {
                        StringTokenizer tokens = new StringTokenizer(property, ".");
                        Object testObject = entity;
                        while (tokens.hasMoreTokens()) {
                            String token = tokens.nextToken();
                            if (tokens.hasMoreTokens()) {
                                try {
                                    testObject = fieldManager.getFieldValue(testObject, token);
                                } catch (FieldNotAvailableException e) {
                                    proceed = false;
                                    break;
                                }
                                if (testObject == null) {
                                    Property propertyItem = new Property();
                                    propertyItem.setName(property);
                                    if (props.contains(propertyItem)) {
                                        proceed = false;
                                        break;
                                    }
                                    propertyItem.setValue(null);
                                    props.add(propertyItem);
                                    proceed = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (!proceed) {
                        continue;
                    }

                    boolean isFieldAccessible = true;
                    Object value = null;
                    try {
                        value = fieldManager.getFieldValue(entity, property);
                    } catch (FieldNotAvailableException e) {
                        isFieldAccessible = false;
                    }
                    checkField:
                    {
                        if (isFieldAccessible) {
                            Property propertyItem = new Property();
                            propertyItem.setName(property);
                            if (props.contains(propertyItem)) {
                                continue;
                            }
                            props.add(propertyItem);
                            String displayVal = null;
                            boolean handled = false;
                            for (PersistenceProvider persistenceProvider : persistenceProviders) {
                                if (persistenceProvider.canHandlePersistence(value, propertyItem, metadata)) {
                                    persistenceProvider.extractValue(
                                            new ExtractValueRequest(props, fieldManager,
                                                    metadata, value, propertyItem, displayVal, persistenceManager, this));
                                    handled = true;
                                }
                            }
                            if (!handled) {
                                defaultPersistenceProvider.extractValue(
                                        new ExtractValueRequest(props, fieldManager, metadata,
                                                value, propertyItem, displayVal, persistenceManager, this));
                            }
                            break checkField;
                        }
                        //try a direct property acquisition via reflection
                        try {
                            String strVal = null;
                            Method method;
                            try {
                                //try a 'get' prefixed mutator first
                                String temp = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
                                method = entity.getClass().getMethod(temp, new Class[]{});
                            } catch (NoSuchMethodException e) {
                                method = entity.getClass().getMethod(property, new Class[]{});
                            }
                            value = method.invoke(entity, new String[]{});
                            Property propertyItem = new Property();
                            propertyItem.setName(property);
                            if (props.contains(propertyItem)) {
                                continue;
                            }
                            props.add(propertyItem);
                            if (value == null) {
                                strVal = null;
                            } else {
                                if (Date.class.isAssignableFrom(value.getClass())) {
                                    strVal = dateFormat.format((Date) value);
                                } else if (Timestamp.class.isAssignableFrom(value.getClass())) {
                                    strVal = dateFormat.format(new Date(((Timestamp) value).getTime()));
                                } else if (Calendar.class.isAssignableFrom(value.getClass())) {
                                    strVal = dateFormat.format(((Calendar) value).getTime());
                                } else if (Double.class.isAssignableFrom(value.getClass())) {
                                    strVal = decimalFormat.format(value);
                                } else if (BigDecimal.class.isAssignableFrom(value.getClass())) {
                                    strVal = decimalFormat.format(((BigDecimal) value).doubleValue());
                                } else {
                                    strVal = value.toString();
                                }
                            }
                            propertyItem.setValue(strVal);
                        } catch (NoSuchMethodException e) {
                            LOG.debug("Unable to find a specified property in the entity: " + property);
                            //do nothing - this property is simply not in the bean
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (InvocationTargetException e) {
            throw new PersistenceException(e);
        }
    }

    protected Entity update(PersistencePackage persistencePackage, Object primaryKey) throws ServiceException {
        try {
            Entity entity = persistencePackage.getEntity();
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Class<?>[] entities = persistenceManager.getPolymorphicEntities(persistencePackage.getCeilingEntityFullyQualifiedClassname());
            Map<String, FieldMetadata> mergedProperties = persistenceManager.getDynamicEntityDao().getMergedProperties(
                persistencePackage.getCeilingEntityFullyQualifiedClassname(),
                entities,
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
            if (primaryKey == null) {
                primaryKey = getPrimaryKey(entity, mergedProperties);
            }
            Serializable instance = persistenceManager.getDynamicEntityDao().retrieve(Class.forName(entity.getType()[0]), primaryKey);
            instance = createPopulatedInstance(instance, entity, mergedProperties, false);
            boolean validated = validate(entity, instance, mergedProperties);
            if (validated) {
                instance = persistenceManager.getDynamicEntityDao().merge(instance);

                List<Serializable> entityList = new ArrayList<Serializable>(1);
                entityList.add(instance);

                return getRecords(mergedProperties, entityList, null, null)[0];
            } else {
                return entity;
            }
        } catch (Exception e) {
            LOG.error("Problem editing entity", e);
            throw new ServiceException("Problem updating entity : " + e.getMessage(), e);
        }
    }

    @Override
    public Object getPrimaryKey(Entity entity, Map<String, FieldMetadata> mergedUnfilteredProperties) {
        Map<String, FieldMetadata> mergedProperties = filterOutCollectionMetadata(mergedUnfilteredProperties);
        Object primaryKey = null;
        String idPropertyName = null;
        BasicFieldMetadata metaData = null;
        for (String property : mergedProperties.keySet()) {
            BasicFieldMetadata temp = (BasicFieldMetadata) mergedProperties.get(property);
            if (temp.getFieldType() == SupportedFieldType.ID && !property.contains(".")) {
                idPropertyName = property;
                metaData = temp;
                break;
            }
        }
        if (idPropertyName == null) {
            throw new RuntimeException("Could not find a primary key property in the passed entity with type: " + entity.getType()[0]);
        }
        for (Property property : entity.getProperties()) {
            if (property.getName().equals(idPropertyName)) {
                switch(metaData.getSecondaryType()) {
                case INTEGER:
                    primaryKey = Long.valueOf(property.getValue());
                    break;
                case STRING:
                    primaryKey = property.getValue();
                    break;
                }
                break;
            }
        }
        if (primaryKey == null) {
            throw new RuntimeException("Could not find the primary key property (" + idPropertyName + ") in the passed entity with type: " + entity.getType()[0]);
        }
        return primaryKey;
    }

    @Override
    public BaseCtoConverter getCtoConverter(PersistencePerspective persistencePerspective, CriteriaTransferObject cto, String ceilingEntityFullyQualifiedClassname, Map<String, FieldMetadata> mergedUnfilteredProperties) {
        return getCtoConverter(persistencePerspective, cto, ceilingEntityFullyQualifiedClassname, mergedUnfilteredProperties, null);
    }

    @Override
    public BaseCtoConverter getCtoConverter(PersistencePerspective persistencePerspective, CriteriaTransferObject cto, String ceilingEntityFullyQualifiedClassname, Map<String, FieldMetadata> mergedUnfilteredProperties, FilterCriterionProviders criterionProviders) {
        Map<String, FieldMetadata> mergedProperties = filterOutCollectionMetadata(mergedUnfilteredProperties);
        BaseCtoConverter ctoConverter = (BaseCtoConverter) applicationContext.getBean("blBaseCtoConverter");
        if (criterionProviders != null) {
            ctoConverter.setFilterCriterionProviders(criterionProviders);
        }
        for (String propertyId : cto.getPropertyIdSet()) {
            if (mergedProperties.containsKey(propertyId)) {
                boolean handled = false;
                for (PersistenceProvider persistenceProvider : persistenceProviders) {
                    if (persistenceProvider.canHandleSearchMapping((BasicFieldMetadata) mergedProperties.get
                            (propertyId))) {
                        persistenceProvider.addSearchMapping(
                                new AddSearchMappingRequest(persistencePerspective, cto,
                                        ceilingEntityFullyQualifiedClassname, mergedProperties, ctoConverter,
                                        propertyId, getFieldManager()));
                        handled = true;
                    }
                }
                if (!handled) {
                    defaultPersistenceProvider.addSearchMapping(
                            new AddSearchMappingRequest(persistencePerspective, cto,
                                    ceilingEntityFullyQualifiedClassname, mergedProperties, ctoConverter, propertyId, getFieldManager()));
                }
            } else {
                ctoConverter.addEmptyMapping(ceilingEntityFullyQualifiedClassname, propertyId);
            }
        }
        if (cto.getPropertyIdSet().isEmpty()) {
            ctoConverter.addEmptyMapping(ceilingEntityFullyQualifiedClassname, "");
        }
        return ctoConverter;
    }

    @Override
    public int getTotalRecords(PersistencePackage persistencePackage, CriteriaTransferObject cto, BaseCtoConverter ctoConverter) {
        PersistentEntityCriteria countCriteria = getCountCriteria(persistencePackage, cto, ctoConverter);
        try {
            return persistenceManager.getDynamicEntityDao().count(countCriteria, Class.forName(StringUtils.isEmpty(persistencePackage.getFetchTypeFullyQualifiedClassname()) ? persistencePackage.getCeilingEntityFullyQualifiedClassname() : persistencePackage.getFetchTypeFullyQualifiedClassname()));
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public PersistentEntityCriteria getCountCriteria(PersistencePackage persistencePackage, CriteriaTransferObject cto, BaseCtoConverter ctoConverter) {
        PersistentEntityCriteria countCriteria = ctoConverter.convert(new CriteriaTransferObjectCountWrapper(cto).wrap(), persistencePackage.getCeilingEntityFullyQualifiedClassname());

        Class<?>[] entities;
        try {
            entities = persistenceManager.getDynamicEntityDao().getAllPolymorphicEntitiesFromCeiling(Class.forName(persistencePackage.getCeilingEntityFullyQualifiedClassname()));
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
        boolean isArchivable = false;
        for (Class<?> entity : entities) {
            if (Status.class.isAssignableFrom(entity)) {
                isArchivable = true;
                break;
            }
        }
        if (isArchivable && !persistencePackage.getPersistencePerspective().getShowArchivedFields()) {
            SimpleFilterCriterionProvider criterionProvider = new  SimpleFilterCriterionProvider(SimpleFilterCriterionProvider.FilterDataStrategy.NONE, 0) {
                @Override
                public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                    return Restrictions.or(Restrictions.eq(targetPropertyName, 'N'), Restrictions.isNull(targetPropertyName));
                }
            };
            FilterCriterion filterCriterion = new FilterCriterion(AssociationPath.ROOT, "archiveStatus.archived", criterionProvider);
            ((NestedPropertyCriteria) countCriteria).add(filterCriterion);
        }
        return countCriteria;
    }

    @Override
    public void extractProperties(Class<?>[] inheritanceLine, Map<MergedPropertyType, Map<String, FieldMetadata>> mergedProperties, List<Property> properties) {
        extractPropertiesFromMetadata(inheritanceLine, mergedProperties.get(MergedPropertyType.PRIMARY), properties, false, MergedPropertyType.PRIMARY);
    }

    protected void extractPropertiesFromMetadata(Class<?>[] inheritanceLine, Map<String, FieldMetadata> mergedProperties, List<Property> properties, Boolean isHiddenOverride, MergedPropertyType type) {
        for (Map.Entry<String, FieldMetadata> entry : mergedProperties.entrySet()) {
            String property = entry.getKey();
            Property prop = new Property();
            FieldMetadata metadata = mergedProperties.get(property);
            prop.setName(property);
            Comparator<Property> comparator = new Comparator<Property>() {
                @Override
                public int compare(Property o1, Property o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            Collections.sort(properties, comparator);
            int pos = Collections.binarySearch(properties, prop, comparator);
            if (pos >= 0 && MergedPropertyType.MAPSTRUCTUREKEY != type && MergedPropertyType.MAPSTRUCTUREVALUE != type) {
                logWarn: {
                    if ((metadata instanceof BasicFieldMetadata) && SupportedFieldType.ID.equals(((BasicFieldMetadata) metadata).getFieldType())) {
                        //don't warn for id field collisions, but still ignore the colliding fields
                        break logWarn;
                    }
                    LOG.warn("Detected a field name collision (" + metadata.getTargetClass() + "." + property + ") during inspection for the inheritance line starting with (" + inheritanceLine[0].getName() + "). Ignoring the additional field. This can occur most commonly when using the @AdminPresentationAdornedTargetCollection and the collection type and target class have field names in common. This situation should be avoided, as the system will strip the repeated fields, which can cause unpredictable behavior.");
                }
                continue;
            }
            properties.add(prop);
            prop.setMetadata(metadata);
            if (isHiddenOverride && prop.getMetadata() instanceof BasicFieldMetadata) {
                //this only makes sense for non collection types
                ((BasicFieldMetadata) prop.getMetadata()).setVisibility(VisibilityEnum.HIDDEN_ALL);
            }
        }
    }

    @Override
    public void updateMergedProperties(PersistencePackage persistencePackage, Map<MergedPropertyType, Map<String, FieldMetadata>> allMergedProperties) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Class<?>[] entities = persistenceManager.getPolymorphicEntities(ceilingEntityFullyQualifiedClassname);
            Map<String, FieldMetadata> mergedProperties = persistenceManager.getDynamicEntityDao().getMergedProperties(
                ceilingEntityFullyQualifiedClassname,
                entities,
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
            allMergedProperties.put(MergedPropertyType.PRIMARY, mergedProperties);
        } catch (Exception e) {
            LOG.error("Problem fetching results for " + ceilingEntityFullyQualifiedClassname, e);
            throw new ServiceException("Unable to fetch results for " + ceilingEntityFullyQualifiedClassname, e);
        }
    }

    @Override
    public Entity update(PersistencePackage persistencePackage) throws ServiceException {
        return update(persistencePackage, null);
    }

    @Override
    public Entity add(PersistencePackage persistencePackage) throws ServiceException {
        try {
            Entity entity = persistencePackage.getEntity();
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Class<?>[] entities = persistenceManager.getPolymorphicEntities(persistencePackage.getCeilingEntityFullyQualifiedClassname());
            Map<String, FieldMetadata> mergedUnfilteredProperties = persistenceManager.getDynamicEntityDao().getMergedProperties(
                persistencePackage.getCeilingEntityFullyQualifiedClassname(),
                entities,
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
            Map<String, FieldMetadata> mergedProperties = filterOutCollectionMetadata(mergedUnfilteredProperties);

            String idProperty = null;
            for (String property : mergedProperties.keySet()) {
                if (((BasicFieldMetadata) mergedProperties.get(property)).getFieldType() == SupportedFieldType.ID) {
                    idProperty = property;
                    break;
                }
            }
            if (idProperty == null) {
                throw new RuntimeException("Could not find a primary key property in the passed entity with type: " + entity.getType()[0]);
            }
            Object primaryKey = null;
            try {
                primaryKey = getPrimaryKey(entity, mergedProperties);
            } catch (Exception e) {
                //do nothing
            }
            if (primaryKey == null) {
                Serializable instance = (Serializable) Class.forName(entity.getType()[0]).newInstance();
                instance = createPopulatedInstance(instance, entity, mergedProperties, false);

                boolean validated = validate(entity, instance, mergedProperties);
                if (validated) {
                    instance = persistenceManager.getDynamicEntityDao().merge(instance);
                    List<Serializable> entityList = new ArrayList<Serializable>(1);
                    entityList.add(instance);

                    return getRecords(mergedProperties, entityList, null, null)[0];
                } else {
                    //return immediately to notify up the stack
                    return entity;
                }
            } else {
                return update(persistencePackage, primaryKey);
            }
        } catch (ServiceException e) {
            LOG.error("Problem adding new entity", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Problem adding new entity", e);
            throw new ServiceException("Problem adding new entity : " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(PersistencePackage persistencePackage) throws ServiceException {
        try {
            Entity entity = persistencePackage.getEntity();
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Class<?>[] entities = persistenceManager.getPolymorphicEntities(persistencePackage.getCeilingEntityFullyQualifiedClassname());
            Map<String, FieldMetadata> mergedUnfilteredProperties = persistenceManager.getDynamicEntityDao().getMergedProperties(
                persistencePackage.getCeilingEntityFullyQualifiedClassname(),
                entities,
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
            Map<String, FieldMetadata> mergedProperties = filterOutCollectionMetadata(mergedUnfilteredProperties);
            Object primaryKey = getPrimaryKey(entity, mergedProperties);
            Serializable instance = persistenceManager.getDynamicEntityDao().retrieve(Class.forName(entity.getType()[0]), primaryKey);

            switch (persistencePerspective.getOperationTypes().getRemoveType()) {
                case NONDESTRUCTIVEREMOVE:
                    for (Property property : entity.getProperties()) {
                        String originalPropertyName = property.getName();
                        FieldManager fieldManager = getFieldManager();
                        if (fieldManager.getField(instance.getClass(), property.getName()) == null) {
                            LOG.debug("Unable to find a bean property for the reported property: " + originalPropertyName + ". Ignoring property.");
                            continue;
                        }
                        if (SupportedFieldType.FOREIGN_KEY == ((BasicFieldMetadata) mergedProperties.get(originalPropertyName)).getFieldType()) {
                            String value = property.getValue();
                            ForeignKey foreignKey = (ForeignKey) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
                            Serializable foreignInstance = persistenceManager.getDynamicEntityDao().retrieve(Class.forName(foreignKey.getForeignKeyClass()), Long.valueOf(value));
                            Collection collection = (Collection) fieldManager.getFieldValue(instance, property.getName());
                            collection.remove(foreignInstance);
                            break;
                        }
                    }
                    break;
                case BASIC:
                    persistenceManager.getDynamicEntityDao().remove(instance);
                    break;
            }
        } catch (Exception e) {
            LOG.error("Problem removing entity", e);
            throw new ServiceException("Problem removing entity : " + e.getMessage(), e);
        }
    }

    @Override
    public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException {
        Entity[] payload;
        int totalRecords;
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        if (StringUtils.isEmpty(persistencePackage.getFetchTypeFullyQualifiedClassname())) {
            persistencePackage.setFetchTypeFullyQualifiedClassname(ceilingEntityFullyQualifiedClassname);
        }
        PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
        try {
            Class<?>[] entities = persistenceManager.getDynamicEntityDao().getAllPolymorphicEntitiesFromCeiling(Class.forName(ceilingEntityFullyQualifiedClassname));
            Map<String, FieldMetadata> mergedProperties = persistenceManager.getDynamicEntityDao().getMergedProperties(
                ceilingEntityFullyQualifiedClassname,
                entities,
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
            BaseCtoConverter ctoConverter = getCtoConverter(persistencePerspective, cto, ceilingEntityFullyQualifiedClassname, mergedProperties);
            PersistentEntityCriteria queryCriteria = ctoConverter.convert(cto, ceilingEntityFullyQualifiedClassname);
            boolean isArchivable = false;
            for (Class<?> entity : entities) {
                if (Status.class.isAssignableFrom(entity)) {
                    isArchivable = true;
                    break;
                }
            }
            if (isArchivable && !persistencePerspective.getShowArchivedFields()) {
                SimpleFilterCriterionProvider criterionProvider = new  SimpleFilterCriterionProvider(SimpleFilterCriterionProvider.FilterDataStrategy.NONE, 0) {
                    @Override
                    public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                        return Restrictions.or(Restrictions.eq(targetPropertyName, 'N'), Restrictions.isNull(targetPropertyName));
                    }
                };
                FilterCriterion filterCriterion = new FilterCriterion(AssociationPath.ROOT, "archiveStatus.archived", criterionProvider);
                ((NestedPropertyCriteria) queryCriteria).add(filterCriterion);
            }
            List<Serializable> records = persistenceManager.getDynamicEntityDao().query(queryCriteria, Class.forName(persistencePackage.getFetchTypeFullyQualifiedClassname()));

            payload = getRecords(mergedProperties, records, null, null);
            totalRecords = getTotalRecords(persistencePackage, cto, ctoConverter);
        } catch (Exception e) {
            LOG.error("Problem fetching results for " + ceilingEntityFullyQualifiedClassname, e);
            throw new ServiceException("Unable to fetch results for " + ceilingEntityFullyQualifiedClassname, e);
        }

        return new DynamicResultSet(null, payload, totalRecords);
    }

    @Override
    public boolean validate(Entity entity, Serializable populatedInstance, Map<String, FieldMetadata> mergedProperties) {
        entityValidatorService.validate(entity, populatedInstance, mergedProperties);
        return !entity.isValidationFailure();
    }

    @Override
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public PersistenceModule getCompatibleModule(OperationType operationType) {
        return ((InspectHelper) persistenceManager).getCompatibleModule(operationType);
    }

    public PersistenceProvider getDefaultPersistenceProvider() {
        return defaultPersistenceProvider;
    }

    public void setDefaultPersistenceProvider(PersistenceProvider defaultPersistenceProvider) {
        this.defaultPersistenceProvider = defaultPersistenceProvider;
    }

    public List<PersistenceProvider> getPersistenceProviders() {
        return persistenceProviders;
    }

    public void setPersistenceProviders(List<PersistenceProvider> persistenceProviders) {
        this.persistenceProviders = persistenceProviders;
    }
}
