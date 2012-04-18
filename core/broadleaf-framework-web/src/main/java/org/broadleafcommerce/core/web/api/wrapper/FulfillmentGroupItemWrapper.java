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

package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a JAXB wrapper around FulfillmentGroupItem.
 *
 * User: Elbert Bautista
 * Date: 4/10/12
 */
@XmlRootElement(name = "fulfillmentGroupItem")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class FulfillmentGroupItemWrapper extends BaseWrapper implements APIWrapper<FulfillmentGroupItem> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected FulfillmentGroupWrapper fulfillmentGroup;

    @XmlElement
    protected OrderItemWrapper orderItem;

    @XmlElement
    protected Money retailPrice;

    @XmlElement
    protected Money salePrice;

    @Override
    public void wrap(FulfillmentGroupItem model, HttpServletRequest request) {
        this.id = model.getId();

        FulfillmentGroupWrapper fulfillmentGroupWrapper = (FulfillmentGroupWrapper) context.getBean(FulfillmentGroupWrapper.class.getName());
        fulfillmentGroupWrapper.wrap(model.getFulfillmentGroup(), request);
        this.fulfillmentGroup = fulfillmentGroupWrapper;

        OrderItemWrapper orderItemWrapper = (OrderItemWrapper) context.getBean(OrderItemWrapper.class.getName());
        orderItemWrapper.wrap(model.getOrderItem(), request);
        this.orderItem = orderItemWrapper;

        this.retailPrice = model.getRetailPrice();
        this.salePrice = model.getSalePrice();
    }
}
