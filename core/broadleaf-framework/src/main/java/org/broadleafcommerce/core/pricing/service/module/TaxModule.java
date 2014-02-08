/*
 * #%L
 * BroadleafCommerce Framework
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
package org.broadleafcommerce.core.pricing.service.module;

import org.broadleafcommerce.core.checkout.service.workflow.CommitTaxActivity;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.pricing.service.TaxService;
import org.broadleafcommerce.core.pricing.service.exception.TaxException;

/**
 * @deprectated use {@link TaxService} instead which should be utilized by {@link CommitTaxActivity}
 */
@Deprecated
public interface TaxModule {
    
    public String getName();
    
    public void setName(String name);
    
    public Order calculateTaxForOrder(Order order) throws TaxException;
    
}
