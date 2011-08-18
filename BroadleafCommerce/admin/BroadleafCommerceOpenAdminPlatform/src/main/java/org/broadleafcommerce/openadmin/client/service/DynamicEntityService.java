/*
 * Copyright 2008-2009 the original author or authors.
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
package org.broadleafcommerce.openadmin.client.service;

import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.springframework.security.access.annotation.Secured;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.google.gwt.user.client.rpc.RemoteService;
import com.gwtincubator.security.exception.ApplicationSecurityException;

/**
 * 
 * @author jfischer
 *
 */
public interface DynamicEntityService extends RemoteService {
    
	@Secured("PERMISSION_DEFAULT")
	public DynamicResultSet inspect(PersistencePackage persistencePackage, String[] metadataOverrideKeys, FieldMetadata[] metadataOverrideValues) throws ServiceException, ApplicationSecurityException;
	
	@Secured("PERMISSION_DEFAULT")
	public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException, ApplicationSecurityException;
    
	@Secured("PERMISSION_DEFAULT")
	public Entity add(PersistencePackage persistencePackage) throws ServiceException, ApplicationSecurityException;
    
	@Secured("PERMISSION_DEFAULT")
    public Entity update(PersistencePackage persistencePackage) throws ServiceException, ApplicationSecurityException;
    
	@Secured("PERMISSION_DEFAULT")
    public void remove(PersistencePackage persistencePackage) throws ServiceException, ApplicationSecurityException;
    
}
