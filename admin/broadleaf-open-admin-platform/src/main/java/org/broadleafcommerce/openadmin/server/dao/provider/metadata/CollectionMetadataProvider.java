/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.dao.provider.metadata;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.ForeignKeyRestrictionType;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.PersistencePerspectiveItemType;
import org.broadleafcommerce.common.presentation.override.AdminPresentationCollectionOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverrides;
import org.broadleafcommerce.openadmin.client.dto.BasicCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.override.FieldMetadataOverride;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.dao.FieldInfo;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaAnnotationRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaXmlRequest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
@Component("blCollectionMetadataProvider")
@Scope("prototype")
public class CollectionMetadataProvider extends AdvancedCollectionMetadataProvider {

    private static final Log LOG = LogFactory.getLog(CollectionMetadataProvider.class);

    protected boolean canHandleFieldForConfiguredMetadata(AddMetadataRequest addMetadataRequest, Map<String, FieldMetadata> metadata) {
        AdminPresentationCollection annot = addMetadataRequest.getRequestedField().getAnnotation(AdminPresentationCollection.class);
        return annot != null;
    }

    protected boolean canHandleAnnotationOverride(OverrideViaAnnotationRequest overrideViaAnnotationRequest, Map<String, FieldMetadata> metadata) {
        AdminPresentationOverrides myOverrides = overrideViaAnnotationRequest.getRequestedEntity().getAnnotation(AdminPresentationOverrides.class);
        return myOverrides != null && !ArrayUtils.isEmpty(myOverrides.collections());
    }

    @Override
    public boolean addMetadata(AddMetadataRequest addMetadataRequest, Map<String, FieldMetadata> metadata) {
        if (!canHandleFieldForConfiguredMetadata(addMetadataRequest, metadata)) {
            return false;
        }
        AdminPresentationCollection annot = addMetadataRequest.getRequestedField().getAnnotation(AdminPresentationCollection
                .class);
        FieldInfo info = buildFieldInfo(addMetadataRequest.getRequestedField());
        FieldMetadataOverride override = constructBasicCollectionMetadataOverride(annot);
        buildCollectionMetadata(addMetadataRequest.getParentClass(), addMetadataRequest.getTargetClass(),
                metadata, info, override);
        setClassOwnership(addMetadataRequest.getParentClass(), addMetadataRequest.getTargetClass(), metadata, info);
        return true;
    }

    @Override
    public boolean overrideViaAnnotation(OverrideViaAnnotationRequest overrideViaAnnotationRequest, Map<String, FieldMetadata> metadata) {
        if (!canHandleAnnotationOverride(overrideViaAnnotationRequest, metadata)) {
            return false;
        }
        Map<String, AdminPresentationCollectionOverride> presentationCollectionOverrides = new HashMap<String, AdminPresentationCollectionOverride>();

        AdminPresentationOverrides myOverrides = overrideViaAnnotationRequest.getRequestedEntity().getAnnotation(AdminPresentationOverrides.class);
        if (myOverrides != null) {
            for (AdminPresentationCollectionOverride myOverride : myOverrides.collections()) {
                presentationCollectionOverrides.put(myOverride.name(), myOverride);
            }
        }

        for (String propertyName : presentationCollectionOverrides.keySet()) {
            for (String key : metadata.keySet()) {
                if (key.startsWith(propertyName)) {
                    buildAdminPresentationCollectionOverride(overrideViaAnnotationRequest.getPrefix(), overrideViaAnnotationRequest.getParentExcluded(), metadata, presentationCollectionOverrides, propertyName, key, overrideViaAnnotationRequest.getDynamicEntityDao());
                }
            }
        }
        return true;
    }

    @Override
    public boolean overrideViaXml(OverrideViaXmlRequest overrideViaXmlRequest, Map<String, FieldMetadata> metadata) {
        Map<String, FieldMetadataOverride> overrides = getTargetedOverride(overrideViaXmlRequest.getRequestedConfigKey(), overrideViaXmlRequest.getRequestedCeilingEntity());
        if (overrides != null) {
            for (String propertyName : overrides.keySet()) {
                final FieldMetadataOverride localMetadata = overrides.get(propertyName);
                for (String key : metadata.keySet()) {
                    if (key.equals(propertyName)) {
                        try {
                            if (metadata.get(key) instanceof BasicCollectionMetadata) {
                                BasicCollectionMetadata serverMetadata = (BasicCollectionMetadata) metadata.get(key);
                                if (serverMetadata.getTargetClass() != null)  {
                                    Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                                    Class<?> parentClass = null;
                                    if (serverMetadata.getOwningClass() != null) {
                                        parentClass = Class.forName(serverMetadata.getOwningClass());
                                    }
                                    String fieldName = serverMetadata.getFieldName();
                                    Field field = overrideViaXmlRequest.getDynamicEntityDao().getFieldManager().getField(targetClass, fieldName);
                                    Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                                    temp.put(field.getName(), serverMetadata);
                                    FieldInfo info = buildFieldInfo(field);
                                    buildCollectionMetadata(parentClass, targetClass, temp, info, localMetadata);
                                    serverMetadata = (BasicCollectionMetadata) temp.get(field.getName());
                                    metadata.put(key, serverMetadata);
                                    if (overrideViaXmlRequest.getParentExcluded()) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug("applyCollectionMetadataOverrides:Excluding " + key + "because parent is marked as excluded.");
                                        }
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
        return true;
    }

    protected void buildAdminPresentationCollectionOverride(String prefix, Boolean isParentExcluded, Map<String, FieldMetadata> mergedProperties, Map<String, AdminPresentationCollectionOverride> presentationCollectionOverrides, String propertyName, String key, DynamicEntityDao dynamicEntityDao) {
        AdminPresentationCollectionOverride override = presentationCollectionOverrides.get(propertyName);
        if (override != null) {
            AdminPresentationCollection annot = override.value();
            if (annot != null) {
                String testKey = prefix + key;
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("buildAdminPresentationCollectionOverride:Excluding " + key + "because an override annotation declared " + testKey + "to be excluded");
                    }
                    metadata.setExcluded(true);
                    return;
                }
                if ((testKey.startsWith(propertyName + ".") || testKey.equals(propertyName)) && !annot.excluded()) {
                    FieldMetadata metadata = mergedProperties.get(key);
                    if (!isParentExcluded) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("buildAdminPresentationCollectionOverride:Showing " + key + "because an override annotation declared " + testKey + " to not be excluded");
                        }
                        metadata.setExcluded(false);
                    }
                }
                if (!(mergedProperties.get(key) instanceof BasicCollectionMetadata)) {
                    return;
                }
                BasicCollectionMetadata serverMetadata = (BasicCollectionMetadata) mergedProperties.get(key);
                if (serverMetadata.getTargetClass() != null) {
                    try {
                        Class<?> targetClass = Class.forName(serverMetadata.getTargetClass());
                        Class<?> parentClass = null;
                        if (serverMetadata.getOwningClass() != null) {
                            parentClass = Class.forName(serverMetadata.getOwningClass());
                        }
                        String fieldName = serverMetadata.getFieldName();
                        Field field = dynamicEntityDao.getFieldManager().getField(targetClass, fieldName);
                        FieldMetadataOverride localMetadata = constructBasicCollectionMetadataOverride(annot);
                        //do not include the previous metadata - we want to construct a fresh metadata from the override annotation
                        Map<String, FieldMetadata> temp = new HashMap<String, FieldMetadata>(1);
                        FieldInfo info = buildFieldInfo(field);
                        buildCollectionMetadata(parentClass, targetClass, temp, info, localMetadata);
                        BasicCollectionMetadata result = (BasicCollectionMetadata) temp.get(field.getName());
                        result.setInheritedFromType(serverMetadata.getInheritedFromType());
                        result.setAvailableToTypes(serverMetadata.getAvailableToTypes());
                        mergedProperties.put(key, result);
                        if (isParentExcluded) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("buildAdminPresentationCollectionOverride:Excluding " + key + "because the parent was excluded");
                            }
                            serverMetadata.setExcluded(true);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    protected FieldMetadataOverride constructBasicCollectionMetadataOverride(AdminPresentationCollection annotColl) {
        if (annotColl != null) {
            FieldMetadataOverride override = new FieldMetadataOverride();
            override.setAddMethodType(annotColl.addType());
            override.setManyToField(annotColl.manyToField());
            override.setCustomCriteria(annotColl.customCriteria());
            override.setUseServerSideInspectionCache(annotColl.useServerSideInspectionCache());
            override.setDataSourceName(annotColl.dataSourceName());
            override.setExcluded(annotColl.excluded());
            override.setFriendlyName(annotColl.friendlyName());
            override.setReadOnly(annotColl.readOnly());
            override.setOrder(annotColl.order());
            override.setTab(annotColl.tab());
            override.setTabOrder(annotColl.tabOrder());
            override.setSecurityLevel(annotColl.securityLevel());
            override.setTargetElementId(annotColl.targetUIElementId());
            override.setAddType(annotColl.operationTypes().addType());
            override.setFetchType(annotColl.operationTypes().fetchType());
            override.setRemoveType(annotColl.operationTypes().removeType());
            override.setUpdateType(annotColl.operationTypes().updateType());
            override.setInspectType(annotColl.operationTypes().inspectType());
            override.setShowIfProperty(annotColl.showIfProperty());
            override.setCurrencyCodeField(annotColl.currencyCodeField());
            return override;
        }
        throw new IllegalArgumentException("AdminPresentationCollection annotation not found on Field");
    }

    protected void buildCollectionMetadata(Class<?> parentClass, Class<?> targetClass, Map<String, FieldMetadata> attributes, FieldInfo field, FieldMetadataOverride collectionMetadata) {
        BasicCollectionMetadata serverMetadata = (BasicCollectionMetadata) attributes.get(field.getName());

        Class<?> resolvedClass = parentClass==null?targetClass:parentClass;
        BasicCollectionMetadata metadata;
        if (serverMetadata != null) {
            metadata = serverMetadata;
        } else {
            metadata = new BasicCollectionMetadata();
        }
        metadata.setTargetClass(targetClass.getName());
        metadata.setFieldName(field.getName());
        if (collectionMetadata.getReadOnly() != null) {
            metadata.setMutable(!collectionMetadata.getReadOnly());
        }
        if (collectionMetadata.getAddMethodType() != null) {
            metadata.setAddMethodType(collectionMetadata.getAddMethodType());
        }
        if (collectionMetadata.getShowIfProperty()!=null) {
            metadata.setShowIfProperty(collectionMetadata.getShowIfProperty());
        }

        org.broadleafcommerce.openadmin.client.dto.OperationTypes dtoOperationTypes = new org.broadleafcommerce.openadmin.client.dto.OperationTypes(OperationType.BASIC, OperationType.BASIC, OperationType.BASIC, OperationType.BASIC, OperationType.BASIC);
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

        String foreignKeyName = null;
        if (serverMetadata != null) {
            foreignKeyName = ((ForeignKey) serverMetadata.getPersistencePerspective().getPersistencePerspectiveItems
                    ().get(PersistencePerspectiveItemType.FOREIGNKEY)).getManyToField();
        }
        if (!StringUtils.isEmpty(collectionMetadata.getManyToField())) {
            foreignKeyName = collectionMetadata.getManyToField();
        }
        if (foreignKeyName == null && !StringUtils.isEmpty(field.getOneToManyMappedBy())) {
            foreignKeyName = field.getOneToManyMappedBy();
        }
        if (foreignKeyName == null && !StringUtils.isEmpty(field.getManyToManyMappedBy())) {
            foreignKeyName = field.getManyToManyMappedBy();
        }
        if (StringUtils.isEmpty(foreignKeyName)) {
            throw new IllegalArgumentException("Unable to infer a ManyToOne field name for the @AdminPresentationCollection annotated field("+field.getName()+"). If not using the mappedBy property of @OneToMany or @ManyToMany, please make sure to explicitly define the manyToField property");
        }

        if (serverMetadata != null) {
            ForeignKey foreignKey = (ForeignKey) metadata.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
            foreignKey.setManyToField(foreignKeyName);
            foreignKey.setForeignKeyClass(resolvedClass.getName());
        } else {
            ForeignKey foreignKey = new ForeignKey(foreignKeyName, resolvedClass.getName(), null, ForeignKeyRestrictionType.ID_EQ);
            persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, foreignKey);
        }

        String ceiling = null;
        checkCeiling: {
            if (field.getGenericType() instanceof ParameterizedType) {
                try {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    java.lang.reflect.Type collectionType = pt.getActualTypeArguments()[0];
                    String ceilingEntityName = ((Class<?>) collectionType).getName();
                    ceiling = entityConfiguration.lookupEntityClass(ceilingEntityName).getName();
                    break checkCeiling;
                } catch (NoSuchBeanDefinitionException e) {
                    // We weren't successful at looking at entity configuration to find the type of this collection.
                    // We will continue and attempt to find it via the Hibernate annotations
                }
            }
            if (!StringUtils.isEmpty(field.getOneToManyTargetEntity()) && !void.class.getName().equals(field.getOneToManyTargetEntity())) {
                ceiling = field.getOneToManyTargetEntity();
                break checkCeiling;
            }
            if (!StringUtils.isEmpty(field.getManyToManyTargetEntity()) && !void.class.getName().equals(field.getManyToManyTargetEntity())) {
                ceiling = field.getManyToManyTargetEntity();
                break checkCeiling;
            }
        }
        if (!StringUtils.isEmpty(ceiling)) {
            metadata.setCollectionCeilingEntity(ceiling);
        }

        if (collectionMetadata.getExcluded() != null) {
            if (LOG.isDebugEnabled()) {
                if (collectionMetadata.getExcluded()) {
                    LOG.debug("buildCollectionMetadata:Excluding " + field.getName() + " because it was explicitly declared in config");
                } else {
                    LOG.debug("buildCollectionMetadata:Showing " + field.getName() + " because it was explicitly declared in config");
                }
            }
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

        if (collectionMetadata.getTab() != null) {
            metadata.setTab(collectionMetadata.getTab());
        }
        if (collectionMetadata.getTabOrder() != null) {
            metadata.setTabOrder(collectionMetadata.getTabOrder());
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

        if (collectionMetadata.getUseServerSideInspectionCache() != null) {
            persistencePerspective.setUseServerSideInspectionCache(collectionMetadata.getUseServerSideInspectionCache());
        }

        if (collectionMetadata.getCurrencyCodeField()!=null) {
            metadata.setCurrencyCodeField(collectionMetadata.getCurrencyCodeField());
        }

        attributes.put(field.getName(), metadata);
    }

    @Override
    public int getOrder() {
        return MetadataProvider.COLLECTION;
    }
}
