/*
 * Copyright 2008-2009 the original author or authors.
 *
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
 */

package org.broadleafcommerce.openadmin.web.handler;

import org.broadleafcommerce.common.web.BLCAbstractHandlerMapping;
import org.broadleafcommerce.openadmin.server.security.domain.AdminModule;
import org.broadleafcommerce.openadmin.server.security.domain.AdminSection;
import org.broadleafcommerce.openadmin.server.security.service.AdminNavigationService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * This handler mapping works with the AdminSection entity to determine if a section has been configured for
 * the passed in URL.
 *
 * If the URL matches a valid AdminSection then this mapping returns the handler configured via the
 * controllerName property or blAdminModulesController by default.
 *
 * @author elbertbautista
 * @since 2.1
 */
public class AdminNavigationHandlerMapping extends BLCAbstractHandlerMapping {

    private String controllerName="blAdminModulesController";

    @Resource(name = "blAdminNavigationService")
    private AdminNavigationService adminNavigationService;

    public static final String CURRENT_ADMIN_MODULES_ATTRIBUTE_NAME = "currentAdminModules";
    public static final String CURRENT_ADMIN_SECTION_ATTRIBUTE_NAME = "currentAdminSection";

    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        //TODO: BLCRequestContext should be refactored to be used in the admin as well
        AdminSection adminSection = adminNavigationService.findAdminSectionByURI(getRequestURIWithoutContext(request));
        if (adminSection != null) {
            request.setAttribute(CURRENT_ADMIN_SECTION_ATTRIBUTE_NAME, adminSection);
            request.setAttribute(CURRENT_ADMIN_MODULES_ATTRIBUTE_NAME, adminSection.getModules());
            if (adminSection.getDisplayController() != null) {
                return adminSection.getDisplayController();
            }

            return controllerName;
        } else {
            return  null;
        }

    }

    public String getRequestURIWithoutContext(HttpServletRequest request) {
        String requestURIWithoutContext;

        if (request.getContextPath() != null) {
            requestURIWithoutContext = request.getRequestURI().substring(request.getContextPath().length());
        } else {
            requestURIWithoutContext = request.getRequestURI();
        }

        // Remove JSESSION-ID or other modifiers
        int pos = requestURIWithoutContext.indexOf(";");
        if (pos >= 0) {
            requestURIWithoutContext = requestURIWithoutContext.substring(0,pos);
        }

        return requestURIWithoutContext;
    }

}
