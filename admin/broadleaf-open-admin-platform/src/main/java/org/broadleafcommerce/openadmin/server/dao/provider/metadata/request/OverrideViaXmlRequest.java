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

package org.broadleafcommerce.openadmin.server.dao.provider.metadata.request;

import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;

import java.util.Map;

/**
 * Contains the requested config key, ceiling entity, metadata and support classes.
 *
 * @author Jeff Fischer
 */
public class OverrideViaXmlRequest {

    private final String requestedConfigKey;
    private final String requestedCeilingEntity;
    private final String prefix;
    private final Boolean parentExcluded;
    private final Map<String, FieldMetadata> requestedMetadata;
    private final DynamicEntityDao dynamicEntityDao;

    public OverrideViaXmlRequest(String requestedConfigKey, String requestedCeilingEntity, String prefix, Boolean parentExcluded, Map<String, FieldMetadata> requestedMetadata, DynamicEntityDao dynamicEntityDao) {
        this.requestedConfigKey = requestedConfigKey;
        this.requestedCeilingEntity = requestedCeilingEntity;
        this.prefix = prefix;
        this.parentExcluded = parentExcluded;
        this.requestedMetadata = requestedMetadata;
        this.dynamicEntityDao = dynamicEntityDao;
    }

    public String getRequestedConfigKey() {
        return requestedConfigKey;
    }

    public String getRequestedCeilingEntity() {
        return requestedCeilingEntity;
    }

    public String getPrefix() {
        return prefix;
    }

    public Boolean getParentExcluded() {
        return parentExcluded;
    }

    public Map<String, FieldMetadata> getRequestedMetadata() {
        return requestedMetadata;
    }

    public DynamicEntityDao getDynamicEntityDao() {
        return dynamicEntityDao;
    }
}
