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

package org.broadleafcommerce.openadmin.server.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataFromMappingDataRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.MetadataProvider;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaAnnotationRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaXmlRequest;
import org.hibernate.mapping.Property;
import org.hibernate.type.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
@Component("blMetadata")
@Scope("prototype")
public class Metadata {

    private static final Log LOG = LogFactory.getLog(Metadata.class);

    @Resource(name="blMetadataProviders")
    protected List<MetadataProvider> metadataProviders = new ArrayList<MetadataProvider>();

    @Resource(name="blDefaultMetadataProvider")
    protected MetadataProvider defaultMetadataProvider;

    public Map<String, FieldMetadata> getFieldPresentationAttributes(Class<?> parentClass, Class<?> targetClass, DynamicEntityDao dynamicEntityDao, String prefix) {
        Map<String, FieldMetadata> attributes = new HashMap<String, FieldMetadata>();
        Field[] fields = dynamicEntityDao.getAllFields(targetClass);
        for (Field field : fields) {
            boolean foundOneOrMoreHandlers = false;
            for (MetadataProvider metadataProvider : metadataProviders) {
                if (metadataProvider.canHandleFieldForConfiguredMetadata(field)) {
                    metadataProvider.addMetadata(new AddMetadataRequest(field, parentClass, targetClass, attributes,
                            dynamicEntityDao, prefix));
                    if (!foundOneOrMoreHandlers) {
                        foundOneOrMoreHandlers = true;
                    }
                }
            }
            if (!foundOneOrMoreHandlers) {
                defaultMetadataProvider.addMetadata(new AddMetadataRequest(field, parentClass, targetClass,
                        attributes, dynamicEntityDao, prefix));
            }
        }
        return attributes;
    }

    public Map<String, FieldMetadata> overrideMetadata(Class<?>[] entities, PropertyBuilder propertyBuilder, String prefix, Boolean isParentExcluded, String ceilingEntityFullyQualifiedClassname, String configurationKey, DynamicEntityDao dynamicEntityDao) {
        Boolean classAnnotatedPopulateManyToOneFields = null;
        //go in reverse order since I want the lowest subclass override to come last to guarantee that it takes effect
        for (int i = entities.length-1;i >= 0; i--) {
            AdminPresentationClass adminPresentationClass = entities[i].getAnnotation(AdminPresentationClass.class);
            if (adminPresentationClass != null && adminPresentationClass.populateToOneFields() != PopulateToOneFieldsEnum.NOT_SPECIFIED) {
                classAnnotatedPopulateManyToOneFields = adminPresentationClass.populateToOneFields()==PopulateToOneFieldsEnum.TRUE;
                break;
            }
        }

        Map<String, FieldMetadata> mergedProperties = propertyBuilder.execute(classAnnotatedPopulateManyToOneFields);
        for (int i = entities.length-1;i >= 0; i--) {
            for (MetadataProvider metadataProvider : metadataProviders) {
                if (metadataProvider.canHandleAnnotationOverride(entities[i])) {
                    metadataProvider.overrideViaAnnotation(new OverrideViaAnnotationRequest(entities[i],
                            mergedProperties, isParentExcluded, dynamicEntityDao, prefix));
                }
                //perform any standard overrides that are not specific to a module
                defaultMetadataProvider.overrideViaAnnotation(new OverrideViaAnnotationRequest(entities[i],
                                                    mergedProperties, isParentExcluded, dynamicEntityDao, prefix));
            }
        }
        //perform any standard overrides that are not specific to a module
        defaultMetadataProvider.overrideViaXml(new OverrideViaXmlRequest(configurationKey,
                ceilingEntityFullyQualifiedClassname, prefix, isParentExcluded, mergedProperties, dynamicEntityDao));

        for (MetadataProvider metadataProvider : metadataProviders) {
            if (metadataProvider.canHandleXmlOverride(ceilingEntityFullyQualifiedClassname, configurationKey)) {
                metadataProvider.overrideViaXml(
                        new OverrideViaXmlRequest(configurationKey, ceilingEntityFullyQualifiedClassname, prefix,
                                isParentExcluded, mergedProperties, dynamicEntityDao));
            }
        }

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
            presentationAttribute.setFieldName(propertyName);
        }
        presentationAttribute.setInheritedFromType(targetClass.getName());
        presentationAttribute.setAvailableToTypes(new String[]{targetClass.getName()});
        boolean handled = false;
        for (MetadataProvider metadataProvider : metadataProviders) {
            if (metadataProvider.canHandleMappingForTypeMetadata(propertyName, componentProperties, entityType)) {
                metadataProvider.addMetadataFromMappingData(new AddMetadataFromMappingDataRequest(presentationAttribute,
                    componentProperties, type, secondaryType, entityType, propertyName, mergedPropertyType, dynamicEntityDao));
                handled = true;
            }
        }
        if (!handled) {
            defaultMetadataProvider.addMetadataFromMappingData(new AddMetadataFromMappingDataRequest(presentationAttribute,
                    componentProperties, type, secondaryType, entityType, propertyName, mergedPropertyType, dynamicEntityDao));
        }

        return presentationAttribute;
    }

    public MetadataProvider getDefaultMetadataProvider() {
        return defaultMetadataProvider;
    }

    public void setDefaultMetadataProvider(MetadataProvider defaultMetadataProvider) {
        this.defaultMetadataProvider = defaultMetadataProvider;
    }

    public List<MetadataProvider> getMetadataProviders() {
        return metadataProviders;
    }

    public void setMetadataProviders(List<MetadataProvider> metadataProviders) {
        this.metadataProviders = metadataProviders;
    }
}
