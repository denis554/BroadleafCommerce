/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.core.order.domain.OrderItemAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * API wrapper to wrap Order Item Attributes.
 * @author Kelly Tisdell
 *
 */
@XmlRootElement(name = "orderItemAttribute")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class OrderItemAttributeWrapper extends BaseWrapper implements
        APIWrapper<OrderItemAttribute> {
    
    @XmlElement
    protected Long id;
    
    @XmlElement
    protected String name;
    
    @XmlElement
    protected String value;
    
    @XmlElement
    protected Long orderItemId;

    @Override
    public void wrapDetails(OrderItemAttribute model, HttpServletRequest request) {
        this.id = model.getId();
        this.name = model.getName();
        this.value = model.getValue();
        this.orderItemId = model.getOrderItem().getId();
    }
    
    @Override
    public void wrapSummary(OrderItemAttribute model, HttpServletRequest request) {
        wrapDetails(model, request);
    }

    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    
    /**
     * @return the orderItemId
     */
    public Long getOrderItemId() {
        return orderItemId;
    }

    
    /**
     * @param orderItemId the orderItemId to set
     */
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
}
