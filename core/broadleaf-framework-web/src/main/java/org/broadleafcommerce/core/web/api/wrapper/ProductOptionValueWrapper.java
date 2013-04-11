/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.web.api.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.ProductOptionValue;

@XmlRootElement(name = "productOptionAllowedValue")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ProductOptionValueWrapper extends BaseWrapper implements
        APIWrapper<ProductOptionValue> {

    @XmlElement
    protected Long id;
    
    @XmlElement
    protected String attributeValue;
    
    @XmlElement
    protected Long displayOrder;
    
    @XmlElement
    protected Money priceAdjustment;
    
    @XmlElement
    protected Long productOptionId;
    
    @Override
    public void wrap(ProductOptionValue model, HttpServletRequest request) {
        this.id = model.getId();
        this.attributeValue = model.getAttributeValue();
        this.displayOrder = model.getDisplayOrder();
        this.priceAdjustment = model.getPriceAdjustment();
        this.productOptionId = model.getProductOption().getId();
    }

}
