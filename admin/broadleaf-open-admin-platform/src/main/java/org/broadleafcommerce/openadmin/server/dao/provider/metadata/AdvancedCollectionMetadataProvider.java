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
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationMap;
import org.broadleafcommerce.openadmin.client.dto.CollectionMetadata;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataFromFieldTypeRequest;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * @author Jeff Fischer
 */
public class AdvancedCollectionMetadataProvider extends MetadataProviderAdapter {

    @Override
    public boolean canHandleFieldForTypeMetadata(Field field) {
        AdminPresentationMap map = field.getAnnotation(AdminPresentationMap.class);
        AdminPresentationCollection collection = field.getAnnotation(AdminPresentationCollection.class);
        return map != null || collection != null;
    }

    @Override
    public void addMetadataFromFieldType(AddMetadataFromFieldTypeRequest addMetadataFromFieldTypeRequest) {
        CollectionMetadata fieldMetadata = (CollectionMetadata) addMetadataFromFieldTypeRequest.getPresentationAttribute();
        if (StringUtils.isEmpty(fieldMetadata.getCollectionCeilingEntity())) {
            ParameterizedType listType = (ParameterizedType) addMetadataFromFieldTypeRequest.getRequestedField().getGenericType();
            Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
            fieldMetadata.setCollectionCeilingEntity(listClass.getName());
        }
        if (addMetadataFromFieldTypeRequest.getTargetClass() != null) {
            if (StringUtils.isEmpty(fieldMetadata.getInheritedFromType())) {
                fieldMetadata.setInheritedFromType(addMetadataFromFieldTypeRequest.getTargetClass().getName());
            }
            if (ArrayUtils.isEmpty(fieldMetadata.getAvailableToTypes())) {
                fieldMetadata.setAvailableToTypes(new String[]{addMetadataFromFieldTypeRequest.getTargetClass().getName()});
            }
        }
        addMetadataFromFieldTypeRequest.getRequestedProperties().put(addMetadataFromFieldTypeRequest.getRequestedPropertyName(), fieldMetadata);
    }
}
