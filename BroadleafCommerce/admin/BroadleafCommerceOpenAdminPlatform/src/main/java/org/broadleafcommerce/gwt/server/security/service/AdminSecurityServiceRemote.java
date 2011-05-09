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
package org.broadleafcommerce.gwt.server.security.service;

import javax.annotation.Resource;

import org.broadleafcommerce.gwt.server.security.domain.AdminUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service("blAdminSecurityRemoteService")
public class AdminSecurityServiceRemote  {
	
	private static final String ANONYMOUS_USER_NAME = "roleAnonymous";
	
	@Resource(name="blAdminSecurityService")
	protected org.broadleafcommerce.gwt.server.security.service.AdminSecurityService securityService;

	public AdminUser getAdminUser() {
		SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null && !auth.getName().equals(ANONYMOUS_USER_NAME)) {     
                User temp = (User) auth.getPrincipal();
                AdminUser response = securityService.readAdminUserByUserName(temp.getUsername());
                return response;
            }
        }

        return null;
	}

}
