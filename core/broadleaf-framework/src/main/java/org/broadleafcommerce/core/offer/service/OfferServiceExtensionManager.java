/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.core.offer.service;

import org.broadleafcommerce.core.offer.domain.Offer;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Andre Azzolini (apazzolini)
 */
public class OfferServiceExtensionManager implements OfferServiceExtensionListener {
    
    protected List<OfferServiceExtensionListener> listeners = new ArrayList<OfferServiceExtensionListener>();

    @Override
    public void applyAdditionalFilters(List<Offer> offers) {
        for (OfferServiceExtensionListener listener : listeners) {
            listener.applyAdditionalFilters(offers);
        }
    }
    
    public List<OfferServiceExtensionListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<OfferServiceExtensionListener> listeners) {
        this.listeners = listeners;
    }

}
