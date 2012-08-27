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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a JAXB wrapper around OrderItem.
 * For simplicity and most use cases, this wrapper only serializes attributes of <code>DiscreteOrderItem</code>
 * This wrapper should be extended for BundledOrderItems etc...
 *
 * User: Elbert Bautista
 * Date: 4/10/12
 */
@XmlRootElement(name = "orderItem")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class OrderItemWrapper extends BaseWrapper implements APIWrapper<OrderItem> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected String name;

    @XmlElement
    protected Integer quantity;

    @XmlElement
    protected Money retailPrice;

    @XmlElement
    protected Money salePrice;

    @XmlElement
    protected CategoryWrapper category;

    @XmlElement
    protected Long orderId;

    @XmlElement
    protected SkuWrapper sku;

    @XmlElement
    protected ProductWrapper product;
    
    @XmlElement(name = "orderItemAttribute")
    @XmlElementWrapper(name = "orderItemAttributes")
    protected List<OrderItemAttributeWrapper> orderItemAttributes;

    @Override
    public void wrap(OrderItem model, HttpServletRequest request) {
        this.id = model.getId();
        this.name = model.getName();
        this.quantity = model.getQuantity();
        this.retailPrice = model.getRetailPrice();
        this.salePrice = model.getSalePrice();

        if (model.getCategory() != null) {
            CategoryWrapper categoryWrapper = (CategoryWrapper) context.getBean(CategoryWrapper.class.getName());
            categoryWrapper.wrap(model.getCategory(), request);
            this.category = categoryWrapper;
        }

        this.orderId = model.getOrder().getId();
        
        Map<String, OrderItemAttribute> itemAttributes = model.getOrderItemAttributes();
        if (itemAttributes != null && ! itemAttributes.isEmpty()) {
        	this.orderItemAttributes = new ArrayList<OrderItemAttributeWrapper>();
        	Set<String> keys = itemAttributes.keySet();
        	for (String key : keys) {
        		OrderItemAttributeWrapper orderItemAttributeWrapper = 
        				(OrderItemAttributeWrapper) context.getBean(OrderItemAttributeWrapper.class.getName());
        		orderItemAttributeWrapper.wrap(itemAttributes.get(key), request);
        		this.orderItemAttributes.add(orderItemAttributeWrapper);
        	}
        }
        
        if (model instanceof DiscreteOrderItem) {
            DiscreteOrderItem doi = (DiscreteOrderItem) model;

            SkuWrapper skuWrapper = (SkuWrapper) context.getBean(SkuWrapper.class.getName());
            skuWrapper.wrap(doi.getSku(), request);
            this.sku = skuWrapper;
            
            ProductWrapper productWrapper = (ProductWrapper) context.getBean(ProductWrapper.class.getName());
            productWrapper.wrap(doi.getProduct(), request);
            this.product = productWrapper;
        }
    }
}
