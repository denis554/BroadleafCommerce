package org.broadleafcommerce.openadmin.server.dao;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.enumeration.domain.DataDrivenEnumerationValueImpl;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationAdornedTargetCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationDataDrivenEnumeration;
import org.broadleafcommerce.common.presentation.AdminPresentationMap;
import org.broadleafcommerce.common.presentation.AdminPresentationMapKey;
import org.broadleafcommerce.common.presentation.AdminPresentationToOneLookup;
import org.broadleafcommerce.common.presentation.ConfigurationItem;
import org.broadleafcommerce.common.presentation.OptionFilterParamType;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.RequiredOverride;
import org.broadleafcommerce.common.presentation.ValidationConfiguration;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.ForeignKeyRestrictionType;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.PersistencePerspectiveItemType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.UnspecifiedBooleanType;
import org.broadleafcommerce.common.presentation.override.AdminPresentationAdornedTargetCollectionOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationCollectionOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationDataDrivenEnumerationOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationMapOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverrides;
import org.broadleafcommerce.common.presentation.override.AdminPresentationToOneLookupOverride;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetList;
import org.broadleafcommerce.openadmin.client.dto.BasicCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.MapMetadata;
import org.broadleafcommerce.openadmin.client.dto.MapStructure;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.SimpleValueMapStructure;
import org.broadleafcommerce.openadmin.client.dto.override.FieldMetadataOverride;
import org.broadleafcommerce.openadmin.client.dto.visitor.MetadataVisitorAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.FieldManager;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;
import org.hibernate.type.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jeff Fischer
 */
@Component("blMetadata")
@Scope("prototype")
public class Metadata {

    protected Map<String, Map<String, FieldMetadataOverride>> metadataOverrides;

    @Resource(name="blMetadataOverrides")
    public void setMetadataOverrides(Map metadataOverrides) {
        try {
            this.metadataOverrides = metadataOverrides;
        } catch (Throwable e) {
            throw new IllegalArgumentException(
                    "Unable to assign metadataOverrides. You are likely using an obsolete spring application context " +
                    "configuration for this value. Please utilize the xmlns:mo=\"http://schema.broadleafcommerce.org/mo\" namespace " +
                    "and http://schema.broadleafcommerce.org/mo http://schema.broadleafcommerce.org/mo/mo.xsd schemaLocation " +
                    "in the xml schema config for your app context. This will allow you to use the appropriate <mo:override> element to configure your overrides.", e);
        }
    }

    public Map<String, FieldMetadata> getFieldPresentationAttributes(Class<?> targetClass, DynamicEntityDao dynamicEntityDao) {
        Map<String, FieldMetadata> attributes = new HashMap<String, FieldMetadata>();
        Field[] fields = dynamicEntityDao.getAllFields(targetClass);
        for (Field field : fields) {
            AdminPresentation annot = field.getAnnotation(AdminPresentation.class);
            AdminPresentationCollection annotColl = field.getAnnotation(AdminPresentationCollection.class);
            AdminPresentationAdornedTargetCollection adornedTargetCollection = field.getAnnotation(AdminPresentationAdornedTargetCollection.class);
            AdminPresentationMap map = field.getAnnotation(AdminPresentationMap.class);
            if (annot != null) {
                FieldMetadataOverride override = constructBasicMetadataOverride(field);
                buildBasicMetadata(targetClass, attributes, field, override, dynamicEntityDao);
            } else if (annotColl != null) {
                FieldMetadataOverride override = constructBasicCollectionMetadataOverride(field);
                buildCollectionMetadata(targetClass, attributes, field, override);
            } else if (adornedTargetCollection != null) {
                FieldMetadataOverride override = constructAdornedTargetCollectionMetadataOverride(field);
                buildAdornedTargetCollectionMetadata(targetClass, attributes, field, override);
            } else if (map != null) {
                FieldMetadataOverride override = constructMapMetadataOverride(field);
                buildMapMetadata(targetClass, attributes, field, override, dynamicEntityDao);
            } else {
                BasicFieldMetadata metadata = new BasicFieldMetadata();
                metadata.setName(field.getName());
                metadata.setExcluded(false);
                attributes.put(field.getName(), metadata);
            }
        }
        return attributes;
    }
    
    public Map<String, FieldMetadata> overrideMetadata(Class<?>[] entities, PropertyBuilder propertyBuilder, String prefix, Boolean isParentExcluded, String ceilingEntityFullyQualifiedClassname, String configurationKey, DynamicEntityDao dynamicEntityDao) {
        Boolean classAnnotatedPopulateManyToOneFields = null;
        Map<String, AdminPresentationOverride> presentationOverrides = new HashMap<String, AdminPresentationOverride>();
        Map<String, AdminPresentationToOneLookupOverride> presentationToOneLookupOverrides = new HashMap<String, AdminPresentationToOneLookupOverride>();
        Map<String, AdminPresentationDataDrivenEnumerationOverride> presentationDataDrivenEnumerationOverrides = new HashMap<String, AdminPresentationDataDrivenEnumerationOverride>();
        Map<String, AdminPresentationMapOverride> presentationMapOverrides = new HashMap<String, AdminPresentationMapOverride>();
        Map<String, AdminPresentationCollectionOverride> presentationCollectionOverrides = new HashMap<String, AdminPresentationCollectionOverride>();
        Map<String, AdminPresentationAdornedTargetCollectionOverride> presentationAdornedTargetCollectionOverrides = new HashMap<String, AdminPresentationAdornedTargetCollectionOverride>();

        //go in reverse order since I want the lowest subclass override to come last to guarantee that it takes effect
        for (int i = entities.length-1;i >= 0; i--) {
            AdminPresentationOverrides myOverrides = entities[i].getAnnotation(AdminPresentationOverrides.class);
            if (myOverrides != null) {
                for (AdminPresentationOverride myOverride : myOverrides.value()) {
                    presentationOverrides.put(myOverride.name(), myOverride);
                }
                for (AdminPresentationToOneLookupOverride myOverride : myOverrides.toOneLookups()) {
                    presentationToOneLookupOverrides.put(myOverride.name(), myOverride);
                }
                for (AdminPresentationMapOverride myOverride : myOverrides.maps()) {
                    presentationMapOverrides.put(myOverride.name(), myOverride);
                }
                for (AdminPresentationCollectionOverride myOverride : myOverrides.collections()) {
                    presentationCollectionOverrides.put(myOverride.name(), myOverride);
                }
                for (AdminPresentationAdornedTargetCollectionOverride myOverride : myOverrides.adornedTargetCollections()) {
                    presentationAdornedTargetCollectionOverrides.put(myOverride.name(), myOverride);
                }
                for (AdminPresentationDataDrivenEnumerationOverride myOverride : myOverrides.dataDrivenEnums()) {
                    presentationDataDrivenEnumerationOverrides.put(myOverride.name(), myOverride);
                }
            }
            AdminPresentationClass adminPresentationClass = entities[i].getAnnotation(AdminPresentationClass.class);
            if (adminPresentationClass != null && classAnnotatedPopulateManyToOneFields == null && adminPresentationClass.populateToOneFields() != PopulateToOneFieldsEnum.NOT_SPECIFIED) {
                classAnnotatedPopulateManyToOneFields = adminPresentationClass.populateToOneFields()==PopulateToOneFieldsEnum.TRUE;
            }
        }

        Map<String, FieldMetadata> mergedProperties = propertyBuilder.execute(classAnnotatedPopulateManyToOneFields);
        for (String propertyName : presentationOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationOverride(prefix, isParentExcluded, mergedProperties, presentationOverrides, propertyName, key, dynamicEntityDao);
                }
            }
        }
        for (String propertyName : presentationToOneLookupOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationToOneLookupOverride(mergedProperties, presentationToOneLookupOverrides, propertyName, key);
                }
            }
        }
        for (String propertyName : presentationDataDrivenEnumerationOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationDataDrivenEnumerationOverride(mergedProperties, presentationDataDrivenEnumerationOverrides, propertyName, key, dynamicEntityDao);
                }
            }
        }
        for (String propertyName : presentationCollectionOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationCollectionOverride(prefix, isParentExcluded, mergedProperties, presentationCollectionOverrides, propertyName, key);
                }
            }
        }
        for (String propertyName : presentationAdornedTargetCollectionOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationAdornedTargetCollectionOverride(prefix, isParentExcluded, mergedProperties, presentationAdornedTargetCollectionOverrides, propertyName, key);
                }
            }
        }
        for (String propertyName : presentationAdornedTargetCollectionOverrides.keySet()) {
            for (String key : mergedProperties.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationMapOverride(prefix, isParentExcluded, mergedProperties, presentationMapOverrides, propertyName, key);
                }
            }
        }

        applyMetadataOverrides(ceilingEntityFullyQualifiedClassname, configurationKey, prefix, isParentExcluded, mergedProperties, dynamicEntityDao);
        applyCollectionMetadataOverrides(ceilingEntityFullyQualifiedClassname, configurationKey, prefix, isParentExcluded, mergedProperties, dynamicEntityDao);
        applyAdornedTargetCollectionMetadataOverrides(ceilingEntityFullyQualifiedClassname, configurationKey, prefix, isParentExcluded, mergedProperties, dynamicEntityDao);
        applyMapMetadataOverrides(ceilingEntityFullyQualifiedClassname, configurationKey, prefix, isParentExcluded, mergedProperties, dynamicEntityDao);

        return mergedProperties;
    }

    public FieldMetadata getFieldMetadata(
        String prefix,
        String propertyName,
        List<Property> componentProperties,
        SupportedFieldType type,
        Type entityType,
        Class<?> targetClass,
        FieldMetadata presentationAttribute,
        MergedPropertyType mergedPropertyType,
        DynamicEntityDao dynamicEntityDao
    ) {
        return getFieldMetadata(prefix, propertyName, componentProperties, type, null, entityType, targetClass, presentationAttribute, mergedPropertyType, dynamicEntityDao);
    }

    public FieldMetadata getFieldMetadata(
        String prefix,
        final String propertyName,
        final List<Property> componentProperties,
        final SupportedFieldType type,
        final SupportedFieldType secondaryType,
        final Type entityType,
        Class<?> targetClass,
        final FieldMetadata presentationAttribute,
        final MergedPropertyType mergedPropertyType,
        final DynamicEntityDao dynamicEntityDao
    ) {
        if (presentationAttribute.getTargetClass() == null) {
            presentationAttribute.setTargetClass(targetClass.getName());
        }
        presentationAttribute.setInheritedFromType(targetClass.getName());
        presentationAttribute.setAvailableToTypes(new String[]{targetClass.getName()});
        presentationAttribute.accept(new MetadataVisitorAdapter() {
            @Override
            public void visit(BasicFieldMetadata metadata) {
                BasicFieldMetadata fieldMetadata = (BasicFieldMetadata) presentationAttribute;
                fieldMetadata.setFieldType(type);
                fieldMetadata.setSecondaryType(secondaryType);
                if (entityType != null && !entityType.isCollectionType()) {
                    Column column = null;
                    for (Property property : componentProperties) {
                        if (property.getName().equals(propertyName)) {
                            column = (Column) property.getColumnIterator().next();
                            break;
                        }
                    }
                    if (column != null) {
                        fieldMetadata.setLength(column.getLength());
                        fieldMetadata.setScale(column.getScale());
                        fieldMetadata.setPrecision(column.getPrecision());
                        fieldMetadata.setRequired(!column.isNullable());
                        fieldMetadata.setUnique(column.isUnique());
                    }
                    fieldMetadata.setForeignKeyCollection(false);
                } else {
                    fieldMetadata.setForeignKeyCollection(true);
                }
                fieldMetadata.setMutable(true);
                fieldMetadata.setMergedPropertyType(mergedPropertyType);
                if (SupportedFieldType.BROADLEAF_ENUMERATION.equals(type)) {
                    try {
                        setupBroadleafEnumeration(fieldMetadata.getBroadleafEnumeration(), fieldMetadata, dynamicEntityDao);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void visit(BasicCollectionMetadata metadata) {
                //do nothing
            }

            @Override
            public void visit(AdornedTargetCollectionMetadata metadata) {
                //do nothing
            }

            @Override
            public void visit(MapMetadata metadata) {
                //do nothing
            }
        });

        return presentationAttribute;
    }

    protected FieldMetadataOverride constructMapMetadataOverride(Field field) {
        AdminPresentationMap map = field.getAnnotation(AdminPresentationMap.class);
        if (map != null) {
            FieldMetadataOverride override = new FieldMetadataOverride();
            override.setConfigurationKey(map.configurationKey());
            override.setDeleteEntityUponRemove(map.deleteEntityUponRemove());
            override.setKeyClass(map.keyClass().getName());
            override.setKeyPropertyFriendlyName(map.keyPropertyFriendlyName());
            if (!ArrayUtils.isEmpty(map.keys())) {
                String[][] keys = new String[map.keys().length][2];
                for (int j=0;j<keys.length;j++){
                    keys[j][0] = map.keys()[j].keyName();
                    keys[j][1] = map.keys()[j].friendlyKeyName();
                }
                override.setKeys(keys);
            }
            override.setMapKeyOptionEntityClass(map.mapKeyOptionEntityClass().getName());
            override.setMapKeyOptionEntityDisplayField(map.mapKeyOptionEntityDisplayField());
            override.setMapKeyOptionEntityValueField(map.mapKeyOptionEntityValueField());
            override.setMediaField(map.mediaField());
            override.setSimpleValue(map.isSimpleValue());
            override.setValueClass(map.valueClass().getName());
            override.setValuePropertyFriendlyName(map.valuePropertyFriendlyName());
            override.setCustomCriteria(map.customCriteria());
            override.setDataSourceName(map.dataSourceName());
            override.setExcluded(map.excluded());
            override.setFriendlyName(map.friendlyName());
            override.setMutable(!map.readOnly());
            override.setOrder(map.order());
            override.setSecurityLevel(map.securityLevel());
            override.setTargetElementId(map.targetUIElementId());
            override.setAddType(map.operationTypes().addType());
            override.setFetchType(map.operationTypes().fetchType());
            override.setRemoveType(map.operationTypes().removeType());
            override.setUpdateType(map.operationTypes().updateType());
            override.setInspectType(map.operationTypes().inspectType());
            return override;
        }
        throw new IllegalArgumentException("AdminPresentationMap annotation not found on field");
    }

    protected FieldMetadataOverride constructAdornedTargetCollectionMetadataOverride(Field field) {
        AdminPresentationAdornedTargetCollection adornedTargetCollection = field.getAnnotation(AdminPresentationAdornedTargetCollection.class);
        if (adornedTargetCollection != null) {
            FieldMetadataOverride override = new FieldMetadataOverride();
            override.setConfigurationKey(adornedTargetCollection.configurationKey());
            override.setGridVisibleFields(adornedTargetCollection.gridVisibleFields());
            override.setIgnoreAdornedProperties(adornedTargetCollection.ignoreAdornedProperties());
            override.setMaintainedAdornedTargetFields(adornedTargetCollection.maintainedAdornedTargetFields());
            override.setParentObjectIdProperty(adornedTargetCollection.parentObjectIdProperty());
            override.setParentObjectProperty(adornedTargetCollection.parentObjectProperty());
            override.setSortAscending(adornedTargetCollection.sortAscending());
            override.setSortProperty(adornedTargetCollection.sortProperty());
            override.setTargetObjectIdProperty(adornedTargetCollection.targetObjectIdProperty());
            override.setTargetObjectProperty(adornedTargetCollection.targetObjectProperty());
            override.setCustomCriteria(adornedTargetCollection.customCriteria());
            override.setDataSourceName(adornedTargetCollection.dataSourceName());
            override.setExcluded(adornedTargetCollection.excluded());
            override.setFriendlyName(adornedTargetCollection.friendlyName());
            override.setMutable(!adornedTargetCollection.readOnly());
            override.setOrder(adornedTargetCollection.order());
            override.setSecurityLevel(adornedTargetCollection.securityLevel());
            override.setTargetElementId(adornedTargetCollection.targetUIElementId());
            override.setAddType(adornedTargetCollection.operationTypes().addType());
            override.setFetchType(adornedTargetCollection.operationTypes().fetchType());
            override.setRemoveType(adornedTargetCollection.operationTypes().removeType());
            override.setUpdateType(adornedTargetCollection.operationTypes().updateType());
            override.setInspectType(adornedTargetCollection.operationTypes().inspectType());
            return override;
        }
        throw new IllegalArgumentException("AdminPresentationAdornedTargetCollection annotation not found on field.");
    }

    protected FieldMetadataOverride constructBasicCollectionMetadataOverride(Field field) {
        AdminPresentationCollection annotColl = field.getAnnotation(AdminPresentationCollection.class);
        if (annotColl != null) {
            FieldMetadataOverride override = new FieldMetadataOverride();
            override.setAddMethodType(annotColl.addType());
            override.setConfigurationKey(annotColl.configurationKey());
            override.setManyToField(annotColl.manyToField());
            override.setCustomCriteria(annotColl.customCriteria());
            override.setDataSourceName(annotColl.dataSourceName());
            override.setExcluded(annotColl.excluded());
            override.setFriendlyName(annotColl.friendlyName());
            override.setMutable(!annotColl.readOnly());
            override.setOrder(annotColl.order());
            override.setSecurityLevel(annotColl.securityLevel());
            override.setTargetElementId(annotColl.targetUIElementId());
            override.setAddType(annotColl.operationTypes().addType());
            override.setFetchType(annotColl.operationTypes().fetchType());
            override.setRemoveType(annotColl.operationTypes().removeType());
            override.setUpdateType(annotColl.operationTypes().updateType());
            override.setInspectType(annotColl.operationTypes().inspectType());
            return override;
        }
        throw new IllegalArgumentException("AdminPresentationCollection annotation not found on Field");
    }

    protected FieldMetadataOverride constructBasicMetadataOverride(Field field) {
        AdminPresentation annot = field.getAnnotation(AdminPresentation.class);
        if (annot != null) {
            FieldMetadataOverride override = new FieldMetadataOverride();
            override.setBroadleafEnumeration(annot.broadleafEnumeration());
            override.setColumnWidth(annot.columnWidth());
            override.setExplicitFieldType(annot.fieldType());
            override.setFieldType(annot.fieldType());
            override.setGroup(annot.group());
            override.setGroupCollapsed(annot.groupCollapsed());
            override.setGroupOrder(annot.groupOrder());
            override.setHelpText(annot.helpText());
            override.setHint(annot.hint());
            override.setLargeEntry(annot.largeEntry());
            override.setFriendlyName(annot.friendlyName());
            override.setSecurityLevel(annot.securityLevel());
            override.setOrder(annot.order());
            override.setVisibility(annot.visibility());
            override.setProminent(annot.prominent());
            override.setReadOnly(annot.readOnly());
            if (annot.validationConfigurations().length != 0) {
                ValidationConfiguration[] configurations = annot.validationConfigurations();
                for (ValidationConfiguration configuration : configurations) {
                    ConfigurationItem[] items = configuration.configurationItems();
                    Map<String, String> itemMap = new HashMap<String, String>();
                    for (ConfigurationItem item : items) {
                        itemMap.put(item.itemName(), item.itemValue());
                    }
                    override.getValidationConfigurations().put(configuration.validationImplementation(), itemMap);
                }
            }
            if (annot.requiredOverride()!= RequiredOverride.IGNORED) {
                override.setRequiredOverride(annot.requiredOverride()==RequiredOverride.REQUIRED);
            }
            override.setExcluded(annot.excluded());
            override.setTooltip(annot.tooltip());

            //the following annotations are complimentary to AdminPresentation
            AdminPresentationToOneLookup toOneLookup = field.getAnnotation(AdminPresentationToOneLookup.class);
            if (toOneLookup != null) {
                override.setExplicitFieldType(SupportedFieldType.ADDITIONAL_FOREIGN_KEY);
                override.setFieldType(SupportedFieldType.ADDITIONAL_FOREIGN_KEY);
                override.setLookupDisplayProperty(toOneLookup.lookupDisplayProperty());
                override.setLookupParentDataSourceName(toOneLookup.lookupParentDataSourceName());
                override.setTargetDynamicFormDisplayId(toOneLookup.targetDynamicFormDisplayId());
            }

            AdminPresentationDataDrivenEnumeration dataDrivenEnumeration = field.getAnnotation(AdminPresentationDataDrivenEnumeration.class);
            if (dataDrivenEnumeration != null) {
                override.setExplicitFieldType(SupportedFieldType.DATA_DRIVEN_ENUMERATION);
                override.setFieldType(SupportedFieldType.DATA_DRIVEN_ENUMERATION);
                override.setOptionCanEditValues(dataDrivenEnumeration.optionCanEditValues());
                override.setOptionDisplayFieldName(dataDrivenEnumeration.optionDisplayFieldName());
                if (!ArrayUtils.isEmpty(dataDrivenEnumeration.optionFilterParams())) {
                    Serializable[][] params = new Serializable[dataDrivenEnumeration.optionFilterParams().length][3];
                    for (int j=0;j<params.length;j++) {
                        params[j][0] = dataDrivenEnumeration.optionFilterParams()[j].param();
                        params[j][1] = dataDrivenEnumeration.optionFilterParams()[j].value();
                        params[j][2] = dataDrivenEnumeration.optionFilterParams()[j].paramType();
                    }
                    override.setOptionFilterValues(params);
                }
                override.setOptionListEntity(dataDrivenEnumeration.optionListEntity().getName());
                override.setOptionValueFieldName(dataDrivenEnumeration.optionValueFieldName());
            }
            return override;
        }
        throw new IllegalArgumentException("AdminPresentation annotation not found on field");
    }

    protected void buildBasicMetadata(Class<?> targetClass, Map<String, FieldMetadata> attributes, Field field, FieldMetadataOverride basicFieldMetadata, DynamicEntityDao dynamicEntityDao) {
        BasicFieldMetadata serverMetadata = (BasicFieldMetadata) attributes.get(field.getName());

        BasicFieldMetadata metadata;
        if (serverMetadata != null) {
            metadata = serverMetadata;
        } else {
            metadata = new BasicFieldMetadata();
        }

        metadata.setName(field.getName());
        metadata.setTargetClass(targetClass.getName());

        if (basicFieldMetadata.getFriendlyName() != null) {
            metadata.setFriendlyName(basicFieldMetadata.getFriendlyName());
        }
        if (basicFieldMetadata.getSecurityLevel() != null) {
            metadata.setSecurityLevel(basicFieldMetadata.getSecurityLevel());
        }
        if (basicFieldMetadata.getVisibility() != null) {
            metadata.setVisibility(basicFieldMetadata.getVisibility());
        }
        if (basicFieldMetadata.getOrder() != null) {
            metadata.setOrder(basicFieldMetadata.getOrder());
        }
        if (basicFieldMetadata.getExplicitFieldType() != null) {
            metadata.setExplicitFieldType(basicFieldMetadata.getExplicitFieldType());
        }
        if (metadata.getExplicitFieldType()==SupportedFieldType.ADDITIONAL_FOREIGN_KEY) {
            //this is a lookup - exclude the fields on this OneToOne or ManyToOne field
            metadata.setExcluded(true);
        } else {
            if (basicFieldMetadata.getExcluded()!=null) {
                metadata.setExcluded(basicFieldMetadata.getExcluded());
            }
        }
        if (basicFieldMetadata.getGroup()!=null) {
            metadata.setGroup(basicFieldMetadata.getGroup());
        }
        if (basicFieldMetadata.getGroupOrder()!=null) {
            metadata.setGroupOrder(basicFieldMetadata.getGroupOrder());
        }
        if (basicFieldMetadata.getGroupCollapsed()!=null) {
            metadata.setGroupCollapsed(basicFieldMetadata.getGroupCollapsed());
        }
        if (basicFieldMetadata.isLargeEntry()!=null) {
            metadata.setLargeEntry(basicFieldMetadata.isLargeEntry());
        }
        if (basicFieldMetadata.isProminent()!=null) {
            metadata.setProminent(basicFieldMetadata.isProminent());
        }
        if (basicFieldMetadata.getColumnWidth()!=null) {
            metadata.setColumnWidth(basicFieldMetadata.getColumnWidth());
        }
        if (basicFieldMetadata.getBroadleafEnumeration()!=null) {
            metadata.setBroadleafEnumeration(basicFieldMetadata.getBroadleafEnumeration());
        }
        if (!StringUtils.isEmpty(metadata.getBroadleafEnumeration()) && metadata.getFieldType()==SupportedFieldType.BROADLEAF_ENUMERATION) {
            try {
                setupBroadleafEnumeration(metadata.getBroadleafEnumeration(), metadata, dynamicEntityDao);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (basicFieldMetadata.getReadOnly()!=null) {
            metadata.setReadOnly(basicFieldMetadata.getReadOnly());
        }
        if (basicFieldMetadata.getTooltip()!=null) {
            metadata.setTooltip(basicFieldMetadata.getTooltip());
        }
        if (basicFieldMetadata.getHelpText()!=null) {
            metadata.setHelpText(basicFieldMetadata.getHelpText());
        }
        if (basicFieldMetadata.getHint()!=null) {
            metadata.setHint(basicFieldMetadata.getHint());
        }
        if (basicFieldMetadata.getLookupDisplayProperty()!=null) {
            metadata.setLookupDisplayProperty(basicFieldMetadata.getLookupDisplayProperty());
        }
        if (basicFieldMetadata.getLookupParentDataSourceName()!=null) {
            metadata.setLookupParentDataSourceName(basicFieldMetadata.getLookupParentDataSourceName());
        }
        if (basicFieldMetadata.getTargetDynamicFormDisplayId()!=null) {
            metadata.setTargetDynamicFormDisplayId(basicFieldMetadata.getTargetDynamicFormDisplayId());
        }
        if (basicFieldMetadata.getOptionListEntity()!=null) {
            metadata.setOptionListEntity(basicFieldMetadata.getOptionListEntity());
        }
        if (metadata.getOptionListEntity() != null && metadata.getOptionListEntity().equals(DataDrivenEnumerationValueImpl.class.getName())) {
            metadata.setOptionValueFieldName("key");
            metadata.setOptionDisplayFieldName("display");
        } else {
            if (basicFieldMetadata.getOptionValueFieldName()!=null) {
                metadata.setOptionValueFieldName(basicFieldMetadata.getOptionValueFieldName());
            }
            if (basicFieldMetadata.getOptionDisplayFieldName()!=null) {
                metadata.setOptionDisplayFieldName(basicFieldMetadata.getOptionDisplayFieldName());
            }
        }
        if (!StringUtils.isEmpty(metadata.getOptionListEntity()) && (StringUtils.isEmpty(metadata.getOptionValueFieldName()) || StringUtils.isEmpty(metadata.getOptionDisplayFieldName()))) {
            throw new IllegalArgumentException("Problem setting up data driven enumeration for ("+field.getName()+"). The optionListEntity, optionValueFieldName and optionDisplayFieldName properties must all be included if not using DataDrivenEnumerationValueImpl as the optionListEntity.");
        }
        if (basicFieldMetadata.getOptionFilterValues() != null) {
            String[][] options = new String[basicFieldMetadata.getOptionFilterValues().length][3];
            int j = 0;
            for (Serializable[] option : basicFieldMetadata.getOptionFilterValues()) {
                options[j][0] = String.valueOf(option[0]);
                options[j][1] = String.valueOf(option[1]);
                options[j][2] = String.valueOf(option[2]);
            }
            metadata.setOptionFilterParams(options);
        }
        if (!StringUtils.isEmpty(metadata.getOptionListEntity())) {
            buildDataDrivenList(metadata, dynamicEntityDao);
        }
        if (basicFieldMetadata.getRequiredOverride()!=null) {
            metadata.setRequiredOverride(basicFieldMetadata.getRequiredOverride());
        }
        if (basicFieldMetadata.getValidationConfigurations()!=null) {
            metadata.setValidationConfigurations(basicFieldMetadata.getValidationConfigurations());
        }

        attributes.put(field.getName(), metadata);
    }

    protected void buildMapMetadata(Class<?> targetClass, Map<String, FieldMetadata> attributes, Field field, FieldMetadataOverride map, DynamicEntityDao dynamicEntityDao) {
        MapMetadata serverMetadata = (MapMetadata) attributes.get(field.getName());

        MapMetadata metadata;
        if (serverMetadata != null) {
            metadata = serverMetadata;
        } else {
            metadata = new MapMetadata();
        }
        if (map.isMutable() != null) {
            metadata.setMutable(map.isMutable());
        }

        metadata.setTargetClass(targetClass.getName());
        org.broadleafcommerce.openadmin.client.dto.OperationTypes dtoOperationTypes = new org.broadleafcommerce.openadmin.client.dto.OperationTypes();
        if (map.getAddType() != null) {
            dtoOperationTypes.setAddType(map.getAddType());
        }
        if (map.getRemoveType() != null) {
            dtoOperationTypes.setRemoveType(map.getRemoveType());
        }
        if (map.getFetchType() != null) {
            dtoOperationTypes.setFetchType(map.getFetchType());
        }
        if (map.getInspectType() != null) {
            dtoOperationTypes.setInspectType(map.getInspectType());
        }
        if (map.getUpdateType() != null) {
            dtoOperationTypes.setUpdateType(map.getUpdateType());
        }

        //don't allow additional non-persistent properties or additional foreign keys for an advanced collection datasource - they don't make sense in this context
        PersistencePerspective persistencePerspective;
        if (serverMetadata != null) {
            persistencePerspective = metadata.getPersistencePerspective();
            persistencePerspective.setOperationTypes(dtoOperationTypes);
        } else {
            persistencePerspective = new PersistencePerspective(dtoOperationTypes, new String[]{}, new ForeignKey[]{});
            metadata.setPersistencePerspective(persistencePerspective);
        }
        if (map.getConfigurationKey() != null) {
            persistencePerspective.setConfigurationKey(map.getConfigurationKey());
        }

        String parentObjectClass = targetClass.getName();
        Map idMetadata = dynamicEntityDao.getIdMetadata(targetClass);
        String parentObjectIdField = (String) idMetadata.get("name");

        String keyClassName = null;
        if (serverMetadata != null) {
            keyClassName = ((MapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).getKeyClassName();
        }
        if (map.getKeyClass() != null && !void.class.getName().equals(map.getKeyClass())) {
            keyClassName = map.getKeyClass();
        }
        if (keyClassName == null) {
            java.lang.reflect.Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
                if (!ArrayUtils.isEmpty(dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(clazz))) {
                    throw new IllegalArgumentException("Key class for AdminPresentationMap was determined to be a JPA managed type. Only primitive types for the key type are currently supported.");
                }
                keyClassName = clazz.getName();
            }
        }
        if (keyClassName == null) {
            keyClassName = String.class.getName();
        }

        String keyPropertyName = "key";
        String keyPropertyFriendlyName = null;
        if (serverMetadata != null) {
            keyPropertyFriendlyName = ((MapStructure) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).getKeyPropertyFriendlyName();
        }
        if (map.getKeyPropertyFriendlyName() != null) {
            keyPropertyFriendlyName = map.getKeyPropertyFriendlyName();
        }
        Boolean deleteEntityUponRemove = null;
        if (serverMetadata != null) {
            deleteEntityUponRemove = ((MapStructure) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).getDeleteValueEntity();
        }
        if (map.isDeleteEntityUponRemove() != null) {
            deleteEntityUponRemove = map.isDeleteEntityUponRemove();
        }
        String valuePropertyName = "value";
        String valuePropertyFriendlyName = null;
        if (serverMetadata != null) {
            MapStructure structure = (MapStructure) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE);
            if (structure instanceof SimpleValueMapStructure) {
                valuePropertyFriendlyName = ((SimpleValueMapStructure) structure).getValuePropertyFriendlyName();
            } else {
                valuePropertyFriendlyName = "";
            }
        }
        if (map.getValuePropertyFriendlyName()!=null) {
            valuePropertyFriendlyName = map.getValuePropertyFriendlyName();
        }
        if (map.getMediaField() != null) {
            metadata.setMediaField(map.getMediaField());
        }

        if (map.getValueClass() != null && !void.class.getName().equals(map.getValueClass())) {
            metadata.setValueClassName(map.getValueClass());
        }
        if (metadata.getValueClassName() == null) {
            java.lang.reflect.Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[1];
                Class<?>[] entities = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(clazz);
                if (!ArrayUtils.isEmpty(entities)) {
                    metadata.setValueClassName(entities[entities.length-1].getName());
                }
            }
        }
        if (metadata.getValueClassName() == null) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany != null && !StringUtils.isEmpty(manyToMany.targetEntity().getName())) {
                metadata.setValueClassName(manyToMany.mappedBy());
            }
        }
        if (metadata.getValueClassName() == null) {
            metadata.setValueClassName(String.class.getName());
        }

        Boolean simpleValue = null;
        if (map.getSimpleValue()!= null && map.getSimpleValue()!=UnspecifiedBooleanType.UNSPECIFIED) {
            simpleValue = map.getSimpleValue()==UnspecifiedBooleanType.TRUE;
        }
        if (simpleValue==null) {
            java.lang.reflect.Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[1];
                Class<?>[] entities = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(clazz);
                simpleValue = ArrayUtils.isEmpty(entities);
            }
        }
        if (simpleValue==null) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany != null && !StringUtils.isEmpty(manyToMany.targetEntity().getName())) {
                simpleValue = false;
            }
        }
        if (simpleValue == null) {
            throw new IllegalArgumentException("Unable to infer if the value for the map is of a complex or simple type based on any parameterized type or ManyToMany annotation. Please explicitly set the isSimpleValue property.");
        }
        metadata.setSimpleValue(simpleValue);

        if (map.getKeys() != null) {
            metadata.setKeys(map.getKeys());
        }

        if (map.getMapKeyOptionEntityClass()!=null) {
            if (!void.class.getName().equals(map.getMapKeyOptionEntityClass())) {
                metadata.setMapKeyOptionEntityClass(map.getMapKeyOptionEntityClass());
            } else {
                metadata.setMapKeyOptionEntityClass("");
            }
        }

        if (map.getMapKeyOptionEntityDisplayField() != null) {
            metadata.setMapKeyOptionEntityDisplayField(map.getMapKeyOptionEntityDisplayField());
        }
        if (map.getMapKeyOptionEntityValueField()!=null) {
            metadata.setMapKeyOptionEntityValueField(map.getMapKeyOptionEntityValueField());
        }

        if (ArrayUtils.isEmpty(metadata.getKeys()) && (StringUtils.isEmpty(metadata.getMapKeyOptionEntityClass()) || StringUtils.isEmpty(metadata.getMapKeyOptionEntityValueField()) || StringUtils.isEmpty(metadata.getMapKeyOptionEntityDisplayField()))) {
            throw new IllegalArgumentException("Could not ascertain method for generating key options for the annotated map ("+field.getName()+"). Must specify either an array of AdminPresentationMapKey values for the keys property, or utilize the mapOptionKeyClass, mapOptionKeyDisplayField and mapOptionKeyValueField properties");
        }

        if (serverMetadata != null) {
            ForeignKey foreignKey = (ForeignKey) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
            foreignKey.setManyToField(parentObjectIdField);
            foreignKey.setForeignKeyClass(parentObjectClass);
            if (metadata.isSimpleValue()) {
                SimpleValueMapStructure mapStructure = (SimpleValueMapStructure) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE);
                mapStructure.setKeyClassName(keyClassName);
                mapStructure.setKeyPropertyName(keyPropertyName);
                mapStructure.setKeyPropertyFriendlyName(keyPropertyFriendlyName);
                mapStructure.setValueClassName(metadata.getValueClassName());
                mapStructure.setValuePropertyName(valuePropertyName);
                mapStructure.setValuePropertyFriendlyName(valuePropertyFriendlyName);
                mapStructure.setMapProperty(field.getName());
            } else {
                MapStructure mapStructure = (MapStructure) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE);
                mapStructure.setKeyClassName(keyClassName);
                mapStructure.setKeyPropertyName(keyPropertyName);
                mapStructure.setKeyPropertyFriendlyName(keyPropertyFriendlyName);
                mapStructure.setValueClassName(metadata.getValueClassName());
                mapStructure.setMapProperty(field.getName());
                mapStructure.setDeleteValueEntity(deleteEntityUponRemove);
            }
        } else {
            ForeignKey foreignKey = new ForeignKey(parentObjectIdField, parentObjectClass);
            persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, foreignKey);
            MapStructure mapStructure;
            if (metadata.isSimpleValue()) {
                mapStructure = new SimpleValueMapStructure(keyClassName, keyPropertyName, keyPropertyFriendlyName, metadata.getValueClassName(), valuePropertyName, valuePropertyFriendlyName, field.getName());
            } else {
                mapStructure = new MapStructure(keyClassName, keyPropertyName, keyPropertyFriendlyName, metadata.getValueClassName(), field.getName(), deleteEntityUponRemove);
            }
            persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.MAPSTRUCTURE, mapStructure);
        }

        if (map.getExcluded() != null) {
            metadata.setExcluded(map.getExcluded());
        }
        if (map.getFriendlyName() != null) {
            metadata.setFriendlyName(map.getFriendlyName());
        }
        if (map.getSecurityLevel() != null) {
            metadata.setSecurityLevel(map.getSecurityLevel());
        }
        if (map.getOrder() != null) {
            metadata.setOrder(map.getOrder());
        }

        if (map.getTargetElementId() != null) {
            metadata.setTargetElementId(map.getTargetElementId());
        }

        if (map.getDataSourceName() != null) {
            metadata.setDataSourceName(map.getDataSourceName());
        }

        if (map.getCustomCriteria() != null) {
            metadata.setCustomCriteria(map.getCustomCriteria());
        }

        attributes.put(field.getName(), metadata);
    }

    protected void buildAdornedTargetCollectionMetadata(Class<?> targetClass, Map<String, FieldMetadata> attributes, Field field, FieldMetadataOverride adornedTargetCollectionMetadata) {
        AdornedTargetCollectionMetadata serverMetadata = (AdornedTargetCollectionMetadata) attributes.get(field.getName());

        AdornedTargetCollectionMetadata metadata;
        if (serverMetadata != null) {
            metadata = serverMetadata;
        } else {
            metadata = new AdornedTargetCollectionMetadata();
        }
        metadata.setTargetClass(targetClass.getName());

        if (adornedTargetCollectionMetadata.isMutable() != null) {
            metadata.setMutable(adornedTargetCollectionMetadata.isMutable());
        }

        org.broadleafcommerce.openadmin.client.dto.OperationTypes dtoOperationTypes = new org.broadleafcommerce.openadmin.client.dto.OperationTypes();
        if (adornedTargetCollectionMetadata.getAddType() != null) {
            dtoOperationTypes.setAddType(adornedTargetCollectionMetadata.getAddType());
        }
        if (adornedTargetCollectionMetadata.getRemoveType() != null) {
            dtoOperationTypes.setRemoveType(adornedTargetCollectionMetadata.getRemoveType());
        }
        if (adornedTargetCollectionMetadata.getFetchType() != null) {
            dtoOperationTypes.setFetchType(adornedTargetCollectionMetadata.getFetchType());
        }
        if (adornedTargetCollectionMetadata.getInspectType() != null) {
            dtoOperationTypes.setInspectType(adornedTargetCollectionMetadata.getInspectType());
        }
        if (adornedTargetCollectionMetadata.getUpdateType() != null) {
            dtoOperationTypes.setUpdateType(adornedTargetCollectionMetadata.getUpdateType());
        }

        //don't allow additional non-persistent properties or additional foreign keys for an advanced collection datasource - they don't make sense in this context
        PersistencePerspective persistencePerspective;
        if (serverMetadata != null) {
            persistencePerspective = metadata.getPersistencePerspective();
            persistencePerspective.setOperationTypes(dtoOperationTypes);
        } else {
            persistencePerspective = new PersistencePerspective(dtoOperationTypes, new String[]{}, new ForeignKey[]{});
            metadata.setPersistencePerspective(persistencePerspective);
        }
        if (adornedTargetCollectionMetadata.getConfigurationKey() != null) {
            persistencePerspective.setConfigurationKey(adornedTargetCollectionMetadata.getConfigurationKey());
        }

        //try to inspect the JPA annotation
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        String parentObjectProperty = null;
        if (serverMetadata != null) {
            parentObjectProperty = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getLinkedObjectPath();
        }
        if (!StringUtils.isEmpty(adornedTargetCollectionMetadata.getParentObjectProperty())) {
            parentObjectProperty = adornedTargetCollectionMetadata.getParentObjectProperty();
        }
        if (parentObjectProperty == null && oneToMany != null && !StringUtils.isEmpty(oneToMany.mappedBy())) {
            parentObjectProperty = oneToMany.mappedBy();
        }
        if (parentObjectProperty == null && manyToMany != null && !StringUtils.isEmpty(manyToMany.mappedBy())) {
            parentObjectProperty = manyToMany.mappedBy();
        }
        if (StringUtils.isEmpty(parentObjectProperty)) {
            throw new IllegalArgumentException("Unable to infer a parentObjectProperty for the @AdminPresentationAdornedTargetCollection annotated field("+field.getName()+"). If not using the mappedBy property of @OneToMany or @ManyToMany, please make sure to explicitly define the parentObjectProperty property");
        }

        String sortProperty = null;
        if (serverMetadata != null) {
            sortProperty = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getSortField();
        }
        if (!StringUtils.isEmpty(adornedTargetCollectionMetadata.getSortProperty())) {
            sortProperty = adornedTargetCollectionMetadata.getSortProperty();
        }

        String ceiling = null;
        checkCeiling: {
            if (oneToMany != null && oneToMany.targetEntity() != void.class) {
                ceiling = oneToMany.targetEntity().getName();
                break checkCeiling;
            }
            if (manyToMany != null && manyToMany.targetEntity() != void.class) {
                ceiling = manyToMany.targetEntity().getName();
                break checkCeiling;
            }
        }
        if (!StringUtils.isEmpty(ceiling)) {
            metadata.setCollectionCeilingEntity(ceiling);
        }
        metadata.setParentObjectClass(targetClass.getName());
        if (adornedTargetCollectionMetadata.getMaintainedAdornedTargetFields() != null) {
            metadata.setMaintainedAdornedTargetFields(adornedTargetCollectionMetadata.getMaintainedAdornedTargetFields());
        }
        if (adornedTargetCollectionMetadata.getGridVisibleFields() != null) {
            metadata.setGridVisibleFields(adornedTargetCollectionMetadata.getGridVisibleFields());
        }
        String parentObjectIdProperty = null;
        if (serverMetadata != null) {
            parentObjectIdProperty = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getLinkedIdProperty();
        }
        if (adornedTargetCollectionMetadata.getParentObjectIdProperty()!=null) {
            parentObjectIdProperty = adornedTargetCollectionMetadata.getParentObjectIdProperty();
        }
        String targetObjectProperty = null;
        if (serverMetadata != null) {
            targetObjectProperty = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getTargetObjectPath();
        }
        if (adornedTargetCollectionMetadata.getTargetObjectProperty()!=null) {
            targetObjectProperty = adornedTargetCollectionMetadata.getTargetObjectProperty();
        }
        String targetObjectIdProperty = null;
        if (serverMetadata != null) {
            targetObjectIdProperty = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getTargetIdProperty();
        }
        if (adornedTargetCollectionMetadata.getTargetObjectIdProperty()!=null) {
            targetObjectIdProperty = adornedTargetCollectionMetadata.getTargetObjectIdProperty();
        }
        Boolean isAscending = true;
        if (serverMetadata != null) {
            isAscending = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getSortAscending();
        }
        if (adornedTargetCollectionMetadata.isSortAscending()!=null) {
            isAscending = adornedTargetCollectionMetadata.isSortAscending();
        }

        if (serverMetadata != null) {
            AdornedTargetList adornedTargetList = (AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST);
            adornedTargetList.setCollectionFieldName(field.getName());
            adornedTargetList.setLinkedObjectPath(parentObjectProperty);
            adornedTargetList.setLinkedIdProperty(parentObjectIdProperty);
            adornedTargetList.setTargetObjectPath(targetObjectProperty);
            adornedTargetList.setTargetIdProperty(targetObjectIdProperty);
            adornedTargetList.setAdornedTargetEntityClassname(ceiling);
            adornedTargetList.setSortField(sortProperty);
            adornedTargetList.setSortAscending(isAscending);
        } else {
            AdornedTargetList adornedTargetList = new AdornedTargetList(field.getName(), parentObjectProperty, parentObjectIdProperty, targetObjectProperty, targetObjectIdProperty, ceiling, sortProperty, isAscending);
            persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.ADORNEDTARGETLIST, adornedTargetList);
        }

        if (adornedTargetCollectionMetadata.getExcluded() != null) {
            metadata.setExcluded(adornedTargetCollectionMetadata.getExcluded());
        }
        if (adornedTargetCollectionMetadata.getFriendlyName() != null) {
            metadata.setFriendlyName(adornedTargetCollectionMetadata.getFriendlyName());
        }
        if (adornedTargetCollectionMetadata.getSecurityLevel() != null) {
            metadata.setSecurityLevel(adornedTargetCollectionMetadata.getSecurityLevel());
        }
        if (adornedTargetCollectionMetadata.getOrder() != null) {
            metadata.setOrder(adornedTargetCollectionMetadata.getOrder());
        }

        if (!StringUtils.isEmpty(adornedTargetCollectionMetadata.getTargetElementId())) {
            metadata.setTargetElementId(adornedTargetCollectionMetadata.getTargetElementId());
        }

        if (!StringUtils.isEmpty(adornedTargetCollectionMetadata.getDataSourceName())) {
            metadata.setDataSourceName(adornedTargetCollectionMetadata.getDataSourceName());
        }

        if (adornedTargetCollectionMetadata.getCustomCriteria() != null) {
            metadata.setCustomCriteria(adornedTargetCollectionMetadata.getCustomCriteria());
        }

        if (adornedTargetCollectionMetadata.isIgnoreAdornedProperties() != null) {
            metadata.setIgnoreAdornedProperties(adornedTargetCollectionMetadata.isIgnoreAdornedProperties());
        }

        attributes.put(field.getName(), metadata);
    }

    protected void buildCollectionMetadata(Class<?> targetClass, Map<String, FieldMetadata> attributes, Field field, FieldMetadataOverride collectionMetadata) {
        BasicCollectionMetadata serverMetadata = (BasicCollectionMetadata) attributes.get(field.getName());

        BasicCollectionMetadata metadata;
        if (serverMetadata != null) {
            metadata = serverMetadata;
        } else {
            metadata = new BasicCollectionMetadata();
        }
        metadata.setTargetClass(targetClass.getName());
        metadata.setCollectionFieldName(field.getName());
        if (collectionMetadata.isMutable() != null) {
            metadata.setMutable(collectionMetadata.isMutable());
        }
        if (collectionMetadata.getAddMethodType() != null) {
            metadata.setAddMethodType(collectionMetadata.getAddMethodType());
        }

        org.broadleafcommerce.openadmin.client.dto.OperationTypes dtoOperationTypes = new org.broadleafcommerce.openadmin.client.dto.OperationTypes();
        if (collectionMetadata.getAddType() != null) {
            dtoOperationTypes.setAddType(collectionMetadata.getAddType());
        }
        if (collectionMetadata.getRemoveType() != null) {
            dtoOperationTypes.setRemoveType(collectionMetadata.getRemoveType());
        }
        if (collectionMetadata.getFetchType() != null) {
            dtoOperationTypes.setFetchType(collectionMetadata.getFetchType());
        }
        if (collectionMetadata.getInspectType() != null) {
            dtoOperationTypes.setInspectType(collectionMetadata.getInspectType());
        }
        if (collectionMetadata.getUpdateType() != null) {
            dtoOperationTypes.setUpdateType(collectionMetadata.getUpdateType());
        }

        if (AddMethodType.LOOKUP == metadata.getAddMethodType()) {
            dtoOperationTypes.setRemoveType(OperationType.NONDESTRUCTIVEREMOVE);
        }

        //don't allow additional non-persistent properties or additional foreign keys for an advanced collection datasource - they don't make sense in this context
        PersistencePerspective persistencePerspective;
        if (serverMetadata != null) {
            persistencePerspective = metadata.getPersistencePerspective();
            persistencePerspective.setOperationTypes(dtoOperationTypes);
        } else {
            persistencePerspective = new PersistencePerspective(dtoOperationTypes, new String[]{}, new ForeignKey[]{});
            metadata.setPersistencePerspective(persistencePerspective);
        }
        if (collectionMetadata.getConfigurationKey() != null) {
            persistencePerspective.setConfigurationKey(collectionMetadata.getConfigurationKey());
        }

        String foreignKeyName = null;
        //try to inspect the JPA annotation
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        if (serverMetadata != null) {
            foreignKeyName = ((ForeignKey) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY)).getManyToField();
        }
        if (!StringUtils.isEmpty(collectionMetadata.getManyToField())) {
            foreignKeyName = collectionMetadata.getManyToField();
        }
        if (foreignKeyName == null && oneToMany != null && !StringUtils.isEmpty(oneToMany.mappedBy())) {
            foreignKeyName = oneToMany.mappedBy();
        }
        if (foreignKeyName == null && manyToMany != null && !StringUtils.isEmpty(manyToMany.mappedBy())) {
            foreignKeyName = manyToMany.mappedBy();
        }
        if (StringUtils.isEmpty(foreignKeyName)) {
            throw new IllegalArgumentException("Unable to infer a ManyToOne field name for the @AdminPresentationCollection annotated field("+field.getName()+"). If not using the mappedBy property of @OneToMany or @ManyToMany, please make sure to explicitly define the manyToField property");
        }

        if (serverMetadata != null) {
            ForeignKey foreignKey = (ForeignKey) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
            foreignKey.setManyToField(foreignKeyName);
            foreignKey.setForeignKeyClass(targetClass.getName());
        } else {
            ForeignKey foreignKey = new ForeignKey(foreignKeyName, targetClass.getName(), null, ForeignKeyRestrictionType.ID_EQ);
            persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, foreignKey);
        }

        String ceiling = null;
        checkCeiling: {
            if (oneToMany != null && oneToMany.targetEntity() != void.class) {
                ceiling = oneToMany.targetEntity().getName();
                break checkCeiling;
            }
            if (manyToMany != null && manyToMany.targetEntity() != void.class) {
                ceiling = manyToMany.targetEntity().getName();
                break checkCeiling;
            }
        }
        if (!StringUtils.isEmpty(ceiling)) {
            metadata.setCollectionCeilingEntity(ceiling);
        }

        if (collectionMetadata.getExcluded() != null) {
            metadata.setExcluded(collectionMetadata.getExcluded());
        }
        if (collectionMetadata.getFriendlyName() != null) {
            metadata.setFriendlyName(collectionMetadata.getFriendlyName());
        }
        if (collectionMetadata.getSecurityLevel() != null) {
            metadata.setSecurityLevel(collectionMetadata.getSecurityLevel());
        }
        if (collectionMetadata.getOrder() != null) {
            metadata.setOrder(collectionMetadata.getOrder());
        }

        if (!StringUtils.isEmpty(collectionMetadata.getTargetElementId())) {
            metadata.setTargetElementId(collectionMetadata.getTargetElementId());
        }

        if (!StringUtils.isEmpty(collectionMetadata.getDataSourceName())) {
            metadata.setDataSourceName(collectionMetadata.getDataSourceName());
        }

        if (collectionMetadata.getCustomCriteria() != null) {
            metadata.setCustomCriteria(collectionMetadata.getCustomCriteria());
        }

        attributes.put(field.getName(), metadata);
    }

    protected Map<String, FieldMetadataOverride> getTargetedOverride(String configurationKey, String ceilingEntityFullyQualifiedClassname, OverrideType overrideType) {
        if (metadataOverrides != null && configurationKey != null && ceilingEntityFullyQualifiedClassname != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(configurationKey);
            sb.append("_");
            sb.append(ceilingEntityFullyQualifiedClassname);
            sb.append("_");
            sb.append(overrideType);

            return metadataOverrides.get(sb.toString());
        }
        return null;
    }

    protected void applyMapMetadataOverrides(String ceilingEntityFullyQualifiedClassname, String configurationKey, String prefix, final Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, DynamicEntityDao dynamicEntityDao) {
        Map<String, FieldMetadataOverride> overrides = getTargetedOverride(configurationKey, ceilingEntityFullyQualifiedClassname, OverrideType.MAP);
        if (overrides != null) {
            for (String propertyName : overrides.keySet()) {
                final FieldMetadataOverride localMetadata = overrides.get(propertyName);
                Boolean excluded = localMetadata.getExcluded();
                if (excluded == null) {
                    excluded = false;
                }
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        metadata.setExcluded(true);
                        continue;
                    }
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            metadata.setExcluded(false);
                        }
                    }
                    if (key.equals(propertyName)) {
                        try {
                            if (mergedProperties.get(key) instanceof MapMetadata) {
                                MapMetadata serverMetadata = (MapMetadata) mergedProperties.get(key);
                                if (serverMetadata.getTargetClass() != null) {
                                    Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                                    String fieldName = ((MapStructure) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).getMapProperty();
                                    Field field = dynamicEntityDao.getFieldManager().getField(targetClass, fieldName);
                                    Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                                    temp.put(field.getName(), serverMetadata);
                                    buildMapMetadata(targetClass, temp, field, localMetadata, null);
                                    serverMetadata = (MapMetadata) temp.get(field.getName());
                                    mergedProperties.put(key, serverMetadata);
                                    if (isParentExcluded) {
                                        serverMetadata.setExcluded(true);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    protected void applyAdornedTargetCollectionMetadataOverrides(String ceilingEntityFullyQualifiedClassname, String configurationKey, String prefix, final Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, DynamicEntityDao dynamicEntityDao) {
        Map<String, FieldMetadataOverride> overrides = getTargetedOverride(configurationKey, ceilingEntityFullyQualifiedClassname, OverrideType.ADORNEDTARGETCOLLECTION);
        if (overrides != null) {
            for (String propertyName : overrides.keySet()) {
                final FieldMetadataOverride localMetadata = overrides.get(propertyName);
                Boolean excluded = localMetadata.getExcluded();
                if (excluded == null) {
                    excluded = false;
                }
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        metadata.setExcluded(true);
                        continue;
                    }
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            metadata.setExcluded(false);
                        }
                    }
                    if (key.equals(propertyName)) {
                        try {
                            if (mergedProperties.get(key) instanceof AdornedTargetCollectionMetadata) {
                                AdornedTargetCollectionMetadata serverMetadata = (AdornedTargetCollectionMetadata) mergedProperties.get(key);
                                if (serverMetadata.getTargetClass() != null) {
                                    Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                                    String fieldName = ((AdornedTargetList) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).getCollectionFieldName();
                                    Field field = dynamicEntityDao.getFieldManager().getField(targetClass, fieldName);
                                    Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                                    temp.put(field.getName(), serverMetadata);
                                    buildAdornedTargetCollectionMetadata(targetClass, temp, field, localMetadata);
                                    serverMetadata = (AdornedTargetCollectionMetadata) temp.get(field.getName());
                                    mergedProperties.put(key, serverMetadata);
                                    if (isParentExcluded) {
                                        serverMetadata.setExcluded(true);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    protected void applyCollectionMetadataOverrides(String ceilingEntityFullyQualifiedClassname, String configurationKey, String prefix, final Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, DynamicEntityDao dynamicEntityDao) {
        Map<String, FieldMetadataOverride> overrides = getTargetedOverride(configurationKey, ceilingEntityFullyQualifiedClassname, OverrideType.COLLECTION);
        if (overrides != null) {
            for (String propertyName : overrides.keySet()) {
                final FieldMetadataOverride localMetadata = overrides.get(propertyName);
                Boolean excluded = localMetadata.getExcluded();
                if (excluded == null) {
                    excluded = false;
                }
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        metadata.setExcluded(true);
                        continue;
                    }
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            metadata.setExcluded(false);
                        }
                    }
                    if (key.equals(propertyName)) {
                        try {
                            if (mergedProperties.get(key) instanceof BasicCollectionMetadata) {
                                BasicCollectionMetadata serverMetadata = (BasicCollectionMetadata) mergedProperties.get(key);
                                if (serverMetadata.getTargetClass() != null)  {
                                    Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                                    String fieldName = serverMetadata.getCollectionFieldName();
                                    Field field = dynamicEntityDao.getFieldManager().getField(targetClass, fieldName);
                                    Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                                    temp.put(field.getName(), serverMetadata);
                                    buildCollectionMetadata(targetClass, temp, field, localMetadata);
                                    serverMetadata = (BasicCollectionMetadata) temp.get(field.getName());
                                    mergedProperties.put(key, serverMetadata);
                                    if (isParentExcluded) {
                                        serverMetadata.setExcluded(true);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    protected void applyMetadataOverrides(String ceilingEntityFullyQualifiedClassname, String configurationKey, String prefix, final Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, DynamicEntityDao dynamicEntityDao) {
        Map<String, FieldMetadataOverride> overrides = getTargetedOverride(configurationKey, ceilingEntityFullyQualifiedClassname, OverrideType.BASIC);
        if (overrides != null) {
            for (String propertyName : overrides.keySet()) {
                final FieldMetadataOverride localMetadata = overrides.get(propertyName);
                Boolean excluded = localMetadata.getExcluded();
                if (excluded == null) {
                    excluded = false;
                }
                for (String key : mergedProperties.keySet()) {
                    String testKey = prefix + key;
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        metadata.setExcluded(true);
                        continue;
                    }
                    if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !excluded) {
                        FieldMetadata metadata = mergedProperties.get(key);
                        if (!isParentExcluded) {
                            metadata.setExcluded(false);
                        }
                    }
                    if (key.equals(propertyName)) {
                        try {
                            if (mergedProperties.get(key) instanceof BasicFieldMetadata) {
                                BasicFieldMetadata serverMetadata = (BasicFieldMetadata) mergedProperties.get(key);
                                if (serverMetadata.getTargetClass() != null) {
                                    Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                                    String fieldName = serverMetadata.getName();
                                    Field field = dynamicEntityDao.getFieldManager().getField(targetClass, fieldName);
                                    Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                                    temp.put(field.getName(), serverMetadata);
                                    buildBasicMetadata(targetClass, temp, field, localMetadata, dynamicEntityDao);
                                    serverMetadata = (BasicFieldMetadata) temp.get(field.getName());
                                    mergedProperties.put(key, serverMetadata);
                                    if (isParentExcluded) {
                                        serverMetadata.setExcluded(true);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    protected void buildDataDrivenList(BasicFieldMetadata metadata, DynamicEntityDao dynamicEntityDao) {
        try {
            Criteria criteria = dynamicEntityDao.createCriteria(Class.forName(metadata.getOptionListEntity()));
            if (metadata.getOptionListEntity().equals(DataDrivenEnumerationValueImpl.class.getName())) {
                criteria.add(Restrictions.eq("hidden", false));
            }
            for (String[] param : metadata.getOptionFilterParams()) {
                Criteria current = criteria;
                String key = param[0];
                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    for (int j=0;j<parts.length-1;j++){
                        current = current.createCriteria(parts[j], parts[j]);
                    }
                }
                current.add(Restrictions.eq(key, convertType(param[1], OptionFilterParamType.valueOf(param[2]))));
            }
            List results = criteria.list();
            String[][] enumerationValues = new String[results.size()][2];
            int j = 0;
            for (Object param : results) {
                enumerationValues[j][1] = String.valueOf(dynamicEntityDao.getFieldManager().getFieldValue(param, metadata.getOptionDisplayFieldName()));
                enumerationValues[j][0] = String.valueOf(dynamicEntityDao.getFieldManager().getFieldValue(param, metadata.getOptionValueFieldName()));
                j++;
            }
            if (!CollectionUtils.isEmpty(results) && metadata.getOptionListEntity().equals(DataDrivenEnumerationValueImpl.class.getName())) {
                metadata.setOptionCanEditValues((Boolean) dynamicEntityDao.getFieldManager().getFieldValue(results.get(0), "type.modifiable"));
            }
            metadata.setEnumerationValues(enumerationValues);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Object convertType(String value, OptionFilterParamType type) {
        Object response;
        switch (type) {
            case BIGDECIMAL:
               response = new BigDecimal(value);
               break;
            case BOOLEAN:
               response = Boolean.parseBoolean(value);
               break;
            case DOUBLE:
               response = Double.parseDouble(value);
               break;
            case FLOAT:
               response = Float.parseFloat(value);
               break;
            case INTEGER:
               response = Integer.parseInt(value);
               break;
            case LONG:
               response = Long.parseLong(value);
               break;
            default:
               response = value;
               break;
        }

        return response;
    }

    protected void setupBroadleafEnumeration(String broadleafEnumerationClass, BasicFieldMetadata fieldMetadata, DynamicEntityDao dynamicEntityDao) {
        try {
            Map<String, String> enumVals = new TreeMap<String, String>();
            Class<?> broadleafEnumeration = Class.forName(broadleafEnumerationClass);
            Method typeMethod = broadleafEnumeration.getMethod("getType");
            Method friendlyTypeMethod = broadleafEnumeration.getMethod("getFriendlyType");
            Field types = dynamicEntityDao.getFieldManager().getField(broadleafEnumeration, "TYPES");
            if (types != null) {
                Map typesMap = (Map) types.get(null);
                for (Object value : typesMap.values()) {
                    enumVals.put((String) friendlyTypeMethod.invoke(value), (String) typeMethod.invoke(value));
                }
            } else {
                Field[] fields = dynamicEntityDao.getAllFields(broadleafEnumeration);
                for (Field field : fields) {
                    boolean isStatic = Modifier.isStatic(field.getModifiers());
                    if (isStatic && field.getType().isAssignableFrom(broadleafEnumeration)){
                        enumVals.put((String) friendlyTypeMethod.invoke(field.get(null)), (String) typeMethod.invoke(field.get(null)));
                    }
                }
            }
            String[][] enumerationValues = new String[enumVals.size()][2];
            int j = 0;
            for (String key : enumVals.keySet()) {
                enumerationValues[j][0] = enumVals.get(key);
                enumerationValues[j][1] = key;
                j++;
            }
            fieldMetadata.setEnumerationValues(enumerationValues);
            fieldMetadata.setEnumerationClass(broadleafEnumerationClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void buildAdminPresentationOverride(String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationOverride> presentationOverrides, String propertyName, String key, DynamicEntityDao dynamicEntityDao) {
        AdminPresentationOverride override = presentationOverrides.get(propertyName);
        if (override != null) {
            AdminPresentation annot = override.value();
            if (annot != null) {
                String testKey = prefix + key;
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    metadata.setExcluded(true);
                    return;
                }
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (!isParentExcluded) {
                        metadata.setExcluded(false);
                    }
                }
                if (!(mergedProperties.get(key) instanceof BasicFieldMetadata)) {
                    return;
                }
                BasicFieldMetadata metadata = (BasicFieldMetadata) mergedProperties.get(key);
                metadata.setFriendlyName(annot.friendlyName());
                metadata.setSecurityLevel(annot.securityLevel());
                metadata.setVisibility(annot.visibility());
                metadata.setOrder(annot.order());
                metadata.setExplicitFieldType(annot.fieldType());
                if (annot.fieldType() != SupportedFieldType.UNKNOWN) {
                    metadata.setFieldType(annot.fieldType());
                }
                metadata.setGroup(annot.group());
                metadata.setGroupCollapsed(annot.groupCollapsed());
                metadata.setGroupOrder(annot.groupOrder());
                metadata.setLargeEntry(annot.largeEntry());
                metadata.setProminent(annot.prominent());
                metadata.setColumnWidth(annot.columnWidth());
                if (!StringUtils.isEmpty(annot.broadleafEnumeration()) && !annot.broadleafEnumeration().equals(metadata.getBroadleafEnumeration())) {
                    metadata.setBroadleafEnumeration(annot.broadleafEnumeration());
                    try {
                        setupBroadleafEnumeration(annot.broadleafEnumeration(), metadata, dynamicEntityDao);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                metadata.setReadOnly(annot.readOnly());
                metadata.setExcluded(isParentExcluded || annot.excluded());
                metadata.setTooltip(annot.tooltip());
                metadata.setHelpText(annot.helpText());
                metadata.setHint(annot.hint());
                metadata.setRequiredOverride(annot.requiredOverride()== RequiredOverride.IGNORED?null:annot.requiredOverride()==RequiredOverride.REQUIRED);
                if (annot.validationConfigurations().length != 0) {
                    ValidationConfiguration[] configurations = annot.validationConfigurations();
                    for (ValidationConfiguration configuration : configurations) {
                        ConfigurationItem[] items = configuration.configurationItems();
                        Map<String, String> itemMap = new HashMap<String, String>();
                        for (ConfigurationItem item : items) {
                            itemMap.put(item.itemName(), item.itemValue());
                        }
                        metadata.getValidationConfigurations().put(configuration.validationImplementation(), itemMap);
                    }
                }
            }
        }
    }

    protected void buildAdminPresentationMapOverride(String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationMapOverride> presentationMapOverrides, String propertyName, String key) {
        AdminPresentationMapOverride override = presentationMapOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationMap annot = override.value();
            if (annot != null) {
                String testKey = prefix + key;
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    metadata.setExcluded(true);
                    return;
                }
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (!isParentExcluded) {
                        metadata.setExcluded(false);
                    }
                }
                if (!(mergedProperties.get(key) instanceof MapMetadata)) {
                    return;
                }
                MapMetadata metadata = (MapMetadata) mergedProperties.get(key);
                metadata.setFriendlyName(annot.friendlyName());
                metadata.setSecurityLevel(annot.securityLevel());
                metadata.setMutable(!annot.readOnly());
                metadata.setOrder(annot.order());
                metadata.setTargetElementId(annot.targetUIElementId());
                metadata.setDataSourceName(annot.dataSourceName());
                metadata.setCustomCriteria(annot.customCriteria());
                if (!StringUtils.isEmpty(annot.configurationKey())) {
                    metadata.getPersistencePerspective().setConfigurationKey(annot.configurationKey());
                }
                if (!void.class.equals(annot.keyClass())) {
                    ((MapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).setKeyClassName(annot.keyClass().getName());
                }
                ((MapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).setKeyPropertyFriendlyName(annot.keyPropertyFriendlyName());
                if (!void.class.equals(annot.valueClass())) {
                    ((MapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).setValueClassName(annot.valueClass().getName());
                }
                if (annot.isSimpleValue()!=UnspecifiedBooleanType.UNSPECIFIED) {
                    metadata.setSimpleValue(annot.isSimpleValue()==UnspecifiedBooleanType.TRUE);
                }
                if (metadata.isSimpleValue()) {
                    ((MapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).setDeleteValueEntity(annot.deleteEntityUponRemove());
                }
                if (!metadata.isSimpleValue()) {
                    ((SimpleValueMapStructure) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE)).setValuePropertyFriendlyName(annot.valuePropertyFriendlyName());
                }
                metadata.setMediaField(annot.mediaField());
                if (!ArrayUtils.isEmpty(annot.keys())) {
                    String[][] keys = new String[annot.keys().length][2];
                    int j = 0;
                    for (AdminPresentationMapKey mapKey : annot.keys()) {
                        keys[j][0] = mapKey.keyName();
                        keys[j][1] = mapKey.friendlyKeyName();
                        j++;
                    }
                    metadata.setKeys(keys);
                }
                if (!void.class.equals(annot.mapKeyOptionEntityClass())) {
                    metadata.setMapKeyOptionEntityClass(annot.mapKeyOptionEntityClass().getName());
                }
                metadata.setMapKeyOptionEntityDisplayField(annot.mapKeyOptionEntityDisplayField());
                metadata.setMapKeyOptionEntityValueField(annot.mapKeyOptionEntityValueField());
            }
        }
    }

    protected void buildAdminPresentationAdornedTargetCollectionOverride(String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationAdornedTargetCollectionOverride> presentationAdornedTargetCollectionOverrides, String propertyName, String key) {
        AdminPresentationAdornedTargetCollectionOverride override = presentationAdornedTargetCollectionOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationAdornedTargetCollection annot = override.value();
            if (annot != null) {
                String testKey = prefix + key;
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    metadata.setExcluded(true);
                    return;
                }
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (!isParentExcluded) {
                        metadata.setExcluded(false);
                    }
                }
                if (!(mergedProperties.get(key) instanceof AdornedTargetCollectionMetadata)) {
                    return;
                }
                AdornedTargetCollectionMetadata metadata = (AdornedTargetCollectionMetadata) mergedProperties.get(key);
                metadata.setCustomCriteria(annot.customCriteria());
                metadata.setMutable(!annot.readOnly());
                metadata.setDataSourceName(annot.dataSourceName());
                metadata.setFriendlyName(annot.friendlyName());
                metadata.setOrder(annot.order());
                metadata.setSecurityLevel(annot.securityLevel());
                if (!StringUtils.isEmpty(annot.configurationKey())) {
                    metadata.getPersistencePerspective().setConfigurationKey(annot.configurationKey());
                }
                if (!StringUtils.isEmpty(annot.parentObjectProperty())) {
                    ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setLinkedObjectPath(annot.parentObjectProperty());
                }
                ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setLinkedIdProperty(annot.parentObjectIdProperty());
                ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setTargetObjectPath(annot.targetObjectProperty());
                ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setTargetIdProperty(annot.targetObjectIdProperty());
                metadata.setMaintainedAdornedTargetFields(annot.maintainedAdornedTargetFields());
                metadata.setGridVisibleFields(annot.gridVisibleFields());
                String sortProperty;
                if (StringUtils.isEmpty(annot.sortProperty())) {
                    sortProperty = null;
                } else {
                    sortProperty = annot.sortProperty();
                }
                ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setSortField(sortProperty);
                ((AdornedTargetList) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.ADORNEDTARGETLIST)).setSortAscending(annot.sortAscending());
                metadata.setIgnoreAdornedProperties(annot.ignoreAdornedProperties());
                metadata.setTargetElementId(annot.targetUIElementId());
            }
        }
    }

    protected void buildAdminPresentationCollectionOverride(String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationCollectionOverride> presentationCollectionOverrides, String propertyName, String key) {
        AdminPresentationCollectionOverride override = presentationCollectionOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationCollection annot = override.value();
            if (annot != null) {
                String testKey = prefix + key;
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    metadata.setExcluded(true);
                    return;
                }
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (!isParentExcluded) {
                        metadata.setExcluded(false);
                    }
                }
                if (!(mergedProperties.get(key) instanceof BasicCollectionMetadata)) {
                    return;
                }
                BasicCollectionMetadata metadata = (BasicCollectionMetadata) mergedProperties.get(key);
                metadata.setCustomCriteria(annot.customCriteria());
                metadata.setMutable(!annot.readOnly());
                metadata.setAddMethodType(annot.addType());
                metadata.setDataSourceName(annot.dataSourceName());
                metadata.setFriendlyName(annot.friendlyName());
                metadata.setOrder(annot.order());
                metadata.setSecurityLevel(annot.securityLevel());
                if (!StringUtils.isEmpty(annot.manyToField())) {
                    ((ForeignKey) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY)).setManyToField(annot.manyToField());
                }
                metadata.setTargetElementId(annot.targetUIElementId());
                if (!StringUtils.isEmpty(annot.configurationKey())) {
                    metadata.getPersistencePerspective().setConfigurationKey(annot.configurationKey());
                }
            }
        }
    }

    protected void buildAdminPresentationToOneLookupOverride(Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationToOneLookupOverride> presentationOverrides, String propertyName, String key) {
        AdminPresentationToOneLookupOverride override = presentationOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationToOneLookup annot = override.value();
            if (annot != null) {
                if (!(mergedProperties.get(key) instanceof BasicFieldMetadata)) {
                    return;
                }
                BasicFieldMetadata metadata = (BasicFieldMetadata) mergedProperties.get(key);
                metadata.setFieldType(SupportedFieldType.ADDITIONAL_FOREIGN_KEY);
                metadata.setExplicitFieldType(SupportedFieldType.ADDITIONAL_FOREIGN_KEY);
                metadata.setLookupDisplayProperty(annot.lookupDisplayProperty());
                metadata.setLookupParentDataSourceName(annot.lookupParentDataSourceName());
                metadata.setTargetDynamicFormDisplayId(annot.targetDynamicFormDisplayId());
            }
        }
    }

    protected void buildAdminPresentationDataDrivenEnumerationOverride(Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationDataDrivenEnumerationOverride> presentationOverrides, String propertyName, String key, DynamicEntityDao dynamicEntityDao) {
        AdminPresentationDataDrivenEnumerationOverride override = presentationOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationDataDrivenEnumeration annot = override.value();
            if (annot != null) {
                if (!(mergedProperties.get(key) instanceof BasicFieldMetadata)) {
                    return;
                }
                BasicFieldMetadata metadata = (BasicFieldMetadata) mergedProperties.get(key);
                metadata.setFieldType(SupportedFieldType.DATA_DRIVEN_ENUMERATION);
                metadata.setExplicitFieldType(SupportedFieldType.DATA_DRIVEN_ENUMERATION);
                metadata.setOptionListEntity(annot.optionListEntity().getName());
                if (metadata.getOptionListEntity().equals(DataDrivenEnumerationValueImpl.class.getName())) {
                    metadata.setOptionValueFieldName("key");
                    metadata.setOptionDisplayFieldName("display");
                } else if (metadata.getOptionListEntity() == null && (StringUtils.isEmpty(metadata.getOptionValueFieldName()) || StringUtils.isEmpty(metadata.getOptionDisplayFieldName()))) {
                    throw new IllegalArgumentException("Problem setting up data driven enumeration for ("+propertyName+"). The optionListEntity, optionValueFieldName and optionDisplayFieldName properties must all be included if not using DataDrivenEnumerationValueImpl as the optionListEntity.");
                } else {
                    metadata.setOptionValueFieldName(annot.optionValueFieldName());
                    metadata.setOptionDisplayFieldName(annot.optionDisplayFieldName());
                }
                if (!ArrayUtils.isEmpty(annot.optionFilterParams())) {
                    String[][] params = new String[annot.optionFilterParams().length][3];
                    for (int j=0;j<params.length;j++) {
                        params[j][0] = annot.optionFilterParams()[j].param();
                        params[j][1] = annot.optionFilterParams()[j].value();
                        params[j][2] = String.valueOf(annot.optionFilterParams()[j].paramType());
                    }
                    metadata.setOptionFilterParams(params);
                }
                if (!StringUtils.isEmpty(metadata.getOptionListEntity())) {
                    buildDataDrivenList(metadata, dynamicEntityDao);
                }
            }
        }
    }

}
