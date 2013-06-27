/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.service.persistence.validation;

import org.apache.commons.collections.CollectionUtils;
import org.broadleafcommerce.common.presentation.ValidationConfiguration;
import org.broadleafcommerce.openadmin.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.Property;
import org.broadleafcommerce.openadmin.server.service.persistence.PersistenceException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


/**
 * This implementation validates each {@link Property} from the given {@link Entity} according to the
 * {@link ValidationConfiguration}s associated with it.
 * 
 * @author Phillip Verheyden
 * @see {@link EntityValidatorService}
 * @see {@link ValidationConfiguration}
 */
@Service("blEntityValidatorService")
public class EntityValidatorServiceImpl implements EntityValidatorService, ApplicationContextAware {
    
    @Resource(name = "blGlobalEntityPropertyValidators")
    protected List<GlobalPropertyValidator> globalEntityValidators;
    
    protected ApplicationContext applicationContext;

    @Override
    public void validate(Entity entity, Serializable instance, Map<String, FieldMetadata> propertiesMetadata) {
        //validate each individual property according to their validation configuration
        for (Property property : entity.getProperties()) {
            FieldMetadata metadata = propertiesMetadata.get(property.getName());
            if (metadata instanceof BasicFieldMetadata) {
                //First execute the global field validators
                if (CollectionUtils.isNotEmpty(globalEntityValidators)) {
                    for (GlobalPropertyValidator validator : globalEntityValidators) {
                        PropertyValidationResult result = validator.validate(entity,
                                instance,
                                propertiesMetadata,
                                (BasicFieldMetadata)metadata,
                                property.getName(),
                                property.getValue());
                        if (!result.isValid()) {
                            entity.addValidationError(property.getName(), result.getErrorMessage());
                        }
                    }
                }
                
                //Now execute the validators configured for this particular field
                Map<String, Map<String, String>> validations =
                        ((BasicFieldMetadata) metadata).getValidationConfigurations();
                for (Map.Entry<String, Map<String, String>> validation : validations.entrySet()) {
                    String validationImplementation = validation.getKey();
                    Map<String, String> configuration = validation.getValue();

                    PropertyValidator validator = null;
                    
                    //attempt bean resolution to find the validator
                    if (applicationContext.containsBean(validationImplementation)) {
                        validator = applicationContext.getBean(validationImplementation, PropertyValidator.class);
                    } 
                    
                    //not a bean, attempt to instantiate the class
                    if (validator == null) {
                        try {
                            validator = (PropertyValidator) Class.forName(validationImplementation).newInstance();
                        } catch (Exception e) {
                            //do nothing
                        }
                    }
                    
                    if (validator == null) {
                        throw new PersistenceException("Could not find validator: " + validationImplementation + 
                                " for property: " + property.getName());
                    }
                    
                    PropertyValidationResult result = validator.validate(entity,
                                                                    instance,
                                                                    propertiesMetadata,
                                                                    configuration,
                                                                    (BasicFieldMetadata)metadata,
                                                                    property.getName(),
                                                                    property.getValue());
                    if (!result.isValid()) {
                        entity.addValidationError(property.getName(), result.getErrorMessage());
                    }
                }
            }
        }
    }
    
    @Override
    public List<GlobalPropertyValidator> getGlobalEntityValidators() {
        return globalEntityValidators;
    }

    @Override
    public void setGlobalEntityValidators(List<GlobalPropertyValidator> globalEntityValidators) {
        this.globalEntityValidators = globalEntityValidators;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
