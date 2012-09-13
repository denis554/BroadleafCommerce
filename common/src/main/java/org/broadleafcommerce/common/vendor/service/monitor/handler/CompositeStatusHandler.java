/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.common.vendor.service.monitor.handler;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.common.vendor.service.monitor.StatusHandler;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;

public class CompositeStatusHandler implements StatusHandler {

    protected List<StatusHandler> handlers = new ArrayList<StatusHandler>();

    public void handleStatus(String serviceName, ServiceStatusType status) {
        for (StatusHandler statusHandler : handlers) {
            statusHandler.handleStatus(serviceName, status);
        }
    }

    public List<StatusHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<StatusHandler> handlers) {
        this.handlers = handlers;
    }

}
