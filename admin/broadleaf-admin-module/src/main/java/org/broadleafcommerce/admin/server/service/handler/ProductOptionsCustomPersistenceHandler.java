/*
 * #%L
 * BroadleafCommerce Admin Module
 * %%
 * Copyright (C) 2009 - 2015 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.admin.server.service.handler;

import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.core.catalog.domain.ProductOption;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 
 *  This class is used to prevent updates to Product Options if "Use in Sku generation" is true but no "Allowed Values" 
 *  have been set.
 * 
 *  @author Nathan Moore (nathanmoore)
 *  
 */
@Component("blProductOptionsCustomPersistenceHandler")
public class ProductOptionsCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            Class testClass = Class.forName(ceilingEntityFullyQualifiedClassname);
            return ProductOption.class.isAssignableFrom(testClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();

            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(ProductOption.class.getName(), persistencePerspective);

            Object primaryKey = helper.getPrimaryKey(entity, adminProperties);
            ProductOption adminInstance = (ProductOption) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);

            adminInstance = (ProductOption) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);
            // validate "Use in Sku generation"
            if (needsAllowedValue(adminInstance)) {
                String errorMessage = "Must add at least 1 Allowed Value when Product Option is used in Sku generation";
                entity.addValidationError("useInSkuGeneration", errorMessage);
                return entity;
            }
            
            adminInstance = (ProductOption) dynamicEntityDao.merge(adminInstance);
            return helper.getRecord(adminProperties, adminInstance, null, null);

        } catch (Exception e) {
            throw new ServiceException("Unable to update entity for " + entity.getType()[0], e);
        }
    }

    /**
     * This function checks if a Product Option's "Use in sku generation" field is set to true 
     * without any allowed values set. This is what we are trying to prevent from happening. 
     * If "Use in sku generation" is true and there are no Allowed Values, the functions returns true.
     * 
     * @param adminInstance: The Product Option being validated
     * @return boolean: Default is false. Returns whether the Product Option needs any Allowed Values .
     */
    protected boolean needsAllowedValue(ProductOption adminInstance) {
        // validate "Use in Sku generation"
        // Check if "use in sku generation" is true and that there are no allowed values set
        if (adminInstance.getUseInSkuGeneration() && adminInstance.getAllowedValues().isEmpty()) {
            return true;
        }
        // Else either there are allowed values and/or "use in sku generation" is false
        return false;
    }
}
