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
package org.broadleafcommerce.openadmin.server.security.remote;

import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.openadmin.client.datasource.dynamic.operation.EntityOperationType;
import org.broadleafcommerce.openadmin.client.service.AdminSecurityService;
import org.broadleafcommerce.openadmin.client.service.ServiceException;
import org.broadleafcommerce.openadmin.security.SecurityConfig;
import org.broadleafcommerce.openadmin.server.security.domain.AdminPermission;
import org.broadleafcommerce.openadmin.server.security.domain.AdminRole;
import org.broadleafcommerce.openadmin.server.security.domain.AdminUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

/**
 * 
 * @author jfischer
 *
 */
@Service("blAdminSecurityRemoteService")
public class AdminSecurityServiceRemote implements AdminSecurityService  {
	
	private static final String ANONYMOUS_USER_NAME = "roleAnonymous";
	
	@Resource(name="blAdminSecurityService")
	protected org.broadleafcommerce.openadmin.server.security.service.AdminSecurityService securityService;
	
	private List<SecurityConfig> securityConfigs;
	
	public org.broadleafcommerce.openadmin.client.security.AdminUser getAdminUser() {
		SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null && !auth.getName().equals(ANONYMOUS_USER_NAME)) {     
                User temp = (User) auth.getPrincipal();
                AdminUser adminUser = securityService.readAdminUserByUserName(temp.getUsername());
                
                org.broadleafcommerce.openadmin.client.security.AdminUser response = new org.broadleafcommerce.openadmin.client.security.AdminUser();
                for (AdminRole role : adminUser.getAllRoles()) {
                	response.getRoles().add(role.getName());
                	for (AdminPermission permission : role.getAllPermissions()) {
                		response.getPermissions().add(permission.getName());
                	}
                }
                response.setUserName(adminUser.getLogin());
                return response;
            }
        }

        return null;
	}
	
	public void securityCheck(String ceilingEntityFullyQualifiedName, EntityOperationType  operationType) throws ServiceException {
		if (securityConfigs != null) {
			for (SecurityConfig sc : securityConfigs){
				if (ceilingEntityFullyQualifiedName != null && 
						ceilingEntityFullyQualifiedName.equals(sc.getCeilingEntityFullyQualifiedName()) &&
						operationType != null &&
						sc.getRequiredTypes().contains(operationType)){
					
					boolean authorized = false;
					org.broadleafcommerce.openadmin.client.security.AdminUser adminUser = getAdminUser();
					checkAuthorization: {
						for (String role : sc.getRoles()) {
							if (adminUser.getRoles() != null && adminUser.getRoles().contains(role)) {
								authorized = true;
								break checkAuthorization;
							}
						}
						for (String permission : sc.getPermissions()){
							if (adminUser.getPermissions() != null && adminUser.getPermissions().contains(permission)){
								authorized = true;
								break checkAuthorization;
							}
						}
					}
					
					if (!authorized){
						throw new ServiceException("Security Check Failed: AdminSecurityServiceRemote");
					}
					
					break;
				}
			}	
		}
	}

	public List<SecurityConfig> getSecurityConfigs() {
		return securityConfigs;
	}

	public void setSecurityConfigs(List<SecurityConfig> securityConfigs) {
		this.securityConfigs = securityConfigs;
	}

}
