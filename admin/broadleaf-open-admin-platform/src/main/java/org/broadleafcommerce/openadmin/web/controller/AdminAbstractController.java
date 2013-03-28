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

package org.broadleafcommerce.openadmin.web.controller;

import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.broadleafcommerce.openadmin.server.security.service.AdminNavigationService;

import javax.annotation.Resource;

/**
 * An abstract controller that provides convenience methods and resource declarations for the Admin
 *
 * Operations that are shared between all admin controllers belong here.
 *
 * @see org.broadleafcommerce.openadmin.web.handler.AdminNavigationHandlerMapping
 * @author elbertbautista
 * @author apazzolini
 */
public abstract class AdminAbstractController extends BroadleafAbstractController {

    @Resource(name = "blAdminNavigationService")
    protected AdminNavigationService adminNavigationService;

}
