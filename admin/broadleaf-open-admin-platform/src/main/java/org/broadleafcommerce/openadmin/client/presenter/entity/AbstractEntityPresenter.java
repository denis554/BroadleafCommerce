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

package org.broadleafcommerce.openadmin.client.presenter.entity;


import com.google.gwt.event.shared.HandlerManager;

/**
 * 
 * @author jfischer
 *
 */
public abstract class AbstractEntityPresenter implements EntityPresenter {
    
    protected HandlerManager eventBus;
    
    protected String defaultItemId;
    
    public HandlerManager getEventBus() {
        return eventBus;
    }

    public void setEventBus(HandlerManager eventBus) {
        this.eventBus = eventBus;
    }

    public String getDefaultItemId() {
        return defaultItemId;
    }

    public void setDefaultItemId(String defaultItemId) {
        this.defaultItemId = defaultItemId;
    }
}
