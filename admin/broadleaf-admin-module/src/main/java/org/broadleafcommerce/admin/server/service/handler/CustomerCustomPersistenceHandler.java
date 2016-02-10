/*
 * #%L
 * BroadleafCommerce Admin Module
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.broadleafcommerce.profile.core.dao.RoleDao;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @author jfischer
 */
@Component("blCustomerCustomPersistenceHandler")
public class CustomerCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(CustomerCustomPersistenceHandler.class);

    @Value("${use.email.for.site.login:true}")
    protected boolean useEmailForLogin;

    @Resource(name="blCustomerService")
    protected CustomerService customerService;
    
    @Resource(name="blRoleDao")
    protected RoleDao roleDao;

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        return persistencePackage.getCeilingEntityFullyQualifiedClassname() != null && persistencePackage.getCeilingEntityFullyQualifiedClassname().equals(Customer.class.getName());
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        return persistencePackage.getCeilingEntityFullyQualifiedClassname() != null && persistencePackage.getCeilingEntityFullyQualifiedClassname().equals(Customer.class.getName());
    }

    @Override
    public Boolean canHandleRemove(PersistencePackage persistencePackage) {
        return persistencePackage.getCeilingEntityFullyQualifiedClassname() != null && persistencePackage.getCeilingEntityFullyQualifiedClassname().equals(Customer.class.getName());
    }
    
    @Override
    public Entity add(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity  = persistencePackage.getEntity();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Customer adminInstance = (Customer) Class.forName(entity.getType()[0]).newInstance();
            adminInstance.setId(customerService.findNextCustomerId());
            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(Customer.class.getName(), persistencePerspective);
            adminInstance = (Customer) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);

            if (useEmailForLogin) {
                adminInstance.setUsername(adminInstance.getEmailAddress());
            }
            
            Entity errorEntity = validateUniqueUsername(entity, adminInstance);
            if (errorEntity != null) {
                return errorEntity;
            }
            
            adminInstance = dynamicEntityDao.merge(adminInstance);
            customerService.createRegisteredCustomerRoles(adminInstance);

            Entity adminEntity = helper.getRecord(adminProperties, adminInstance, null, null);

            return adminEntity;
        } catch (Exception e) {
            LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to add entity for " + entity.getType()[0], e);
        }
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(Customer.class.getName(), persistencePerspective);
            Object primaryKey = helper.getPrimaryKey(entity, adminProperties);
            Customer adminInstance = (Customer) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);

            String passwordBefore = adminInstance.getPassword();
            adminInstance.setPassword(null);
            adminInstance = (Customer) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);
            adminInstance.setPassword(passwordBefore);

            if (useEmailForLogin) {
                adminInstance.setUsername(adminInstance.getEmailAddress());
            }

            adminInstance = customerService.saveCustomer(adminInstance);
            Entity adminEntity = helper.getRecord(adminProperties, adminInstance, null, null);

            return adminEntity;

        } catch (Exception e) {
            throw new ServiceException("Unable to update entity for " + entity.getType()[0], e);
        }
    }
    
    /**
     * Validates that a Customer does not have their username duplicated
     * 
     * @param entity
     * @param adminInstance
     * @return the original entity with a validation error on it or null if no validation failure
     */
    protected Entity validateUniqueUsername(Entity entity, Customer adminInstance) {
        if (customerService.readCustomerByUsername(adminInstance.getUsername()) != null) {
            entity.addValidationError("emailAddress", "nonUniqueUsernameError");
            return entity;
        }
        return null;
    }
    
    @Override
    public void remove(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
    	Entity entity = persistencePackage.getEntity();
    	try {
            Long entityId = Long.parseLong(entity.findProperty("id").getValue());
            if (entityId != null) {
                roleDao.removeCustomerRolesByCustomerId(entityId);
                Customer adminInstance = customerService.readCustomerById(entityId);
                List<CustomerAddress> addressList = adminInstance.getCustomerAddresses();
                for (CustomerAddress address : addressList) {
                    if (Status.class.isAssignableFrom(address.getClass())) {
                        ((Status)address).setArchived('Y');
                    }
                }
                adminInstance = dynamicEntityDao.merge(adminInstance);
                if (Status.class.isAssignableFrom(adminInstance.getClass())) {
                    ((Status) adminInstance).setArchived('Y');
                    customerService.saveCustomer(adminInstance);
                    return;
                }
            }


			helper.getCompatibleModule(OperationType.BASIC).remove(persistencePackage);
		} catch (Exception e) {
			LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to remove entity for " + entity.getType()[0], e);
		}
    }
}
