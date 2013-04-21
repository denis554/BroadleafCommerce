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

import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataFromFieldTypeRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataFromMappingDataRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.AddMetadataRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaAnnotationRequest;
import org.broadleafcommerce.openadmin.server.dao.provider.metadata.request.OverrideViaXmlRequest;
import org.springframework.core.Ordered;

import java.util.Map;

/**
 * Classes implementing this interface are capable of manipulating metadata resulting from the inspection
 * phase for the admin. Providers are typically added in response to new admin presentation annotation support.
 * Implementers should generally extend <tt>MetadataProviderAdapter</tt>.
 *
 * @author Jeff Fischer
 */
public interface MetadataProvider extends Ordered {

    //standard ordering constants for BLC providers
    public static final int BASIC = 1000;
    public static final int COLLECTION = 2000;
    public static final int ADORNED_TARGET = 3000;
    public static final int MAP = 4000;
    public static final int MAP_FIELD = 5000;

    /**
     * Contribute to metadata inspection for the <tt>Field</tt> instance in the request. Implementations should
     * add values to the metadata parameter.
     *
     * @param addMetadataRequest contains the requested field and support classes.
     * @param metadata implementations should add metadata for the requested field here
     * @return whether or not this implementation adjusted metadata
     */
    boolean addMetadata(AddMetadataRequest addMetadataRequest, Map<String, FieldMetadata> metadata);

    /**
     * Contribute to metadata inspection for the entity in the request. Implementations should override values
     * in the metadata parameter.
     *
     * @param overrideViaAnnotationRequest contains the requested entity and support classes.
     * @param metadata implementations should override metadata here
     * @return whether or not this implementation adjusted metadata
     */
     boolean overrideViaAnnotation(OverrideViaAnnotationRequest overrideViaAnnotationRequest, Map<String, FieldMetadata> metadata);

    /**
     * Contribute to metadata inspection for the ceiling entity and config key. Implementations should override
     * values in the metadata parameter.
     *
     * @param overrideViaXmlRequest contains the requested config key, ceiling entity and support classes.
     * @param metadata implementations should override metadata here
     * @return whether or not this implementation adjusted metadata
     */
    boolean overrideViaXml(OverrideViaXmlRequest overrideViaXmlRequest, Map<String, FieldMetadata> metadata);

    /**
     * Contribute to metadata inspection using Hibernate column information. Implementations should impact values
     * in the metadata parameter.
     *
     * @param addMetadataFromMappingDataRequest contains the requested Hibernate type and support classes.
     * @param metadata implementations should impact values for the metadata for the field here
     * @return whether or not this implementation adjusted metadata
     */
    boolean addMetadataFromMappingData(AddMetadataFromMappingDataRequest addMetadataFromMappingDataRequest, FieldMetadata metadata);

    /**
     * Contribute to metadata inspection for the <tt>Field</tt> instance in the request. Implementations should
     * add values to the metadata parameter. This is metadata based on the field type.
     *
     * @param addMetadataFromFieldTypeRequest contains the requested field, property name and support classes.
     * @param metadata implementations should add values for the field here
     * @return whether or not this implementation adjusted metadata
     */
    boolean addMetadataFromFieldType(AddMetadataFromFieldTypeRequest addMetadataFromFieldTypeRequest, Map<String, FieldMetadata> metadata);

}
