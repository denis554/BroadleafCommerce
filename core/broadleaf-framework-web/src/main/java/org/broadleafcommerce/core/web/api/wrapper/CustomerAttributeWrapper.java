/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */

package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.profile.core.domain.CustomerAttribute;
import org.broadleafcommerce.profile.core.domain.CustomerAttributeImpl;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * API wrapper to wrap Customer Attributes.
 * @author Priyesh Patel
 * 
 * @deprecated - use {@link com.broadleafcommerce.core.rest.api.v2.wrapper.CustomerAttributeWrapper}
 *
 */
@Deprecated
@XmlRootElement(name = "customerAttribute")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CustomerAttributeWrapper extends BaseWrapper implements
        APIWrapper<CustomerAttribute>, APIUnwrapper<CustomerAttribute> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected String name;

    @XmlElement
    protected String value;

    @XmlElement
    protected Long customerId;

    @Override
    public void wrapDetails(CustomerAttribute model, HttpServletRequest request) {
        this.id = model.getId();
        this.name = model.getName();
        this.value = model.getValue();
        this.customerId = model.getCustomer().getId();
    }

    @Override
    public void wrapSummary(CustomerAttribute model, HttpServletRequest request) {
        wrapDetails(model, request);
    }

    @Override
    public CustomerAttribute unwrap(HttpServletRequest request, ApplicationContext context) {
        CustomerAttribute attribute = new CustomerAttributeImpl();
        attribute.setId(id);
        attribute.setName(name);
        attribute.setValue(value);
        return attribute;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}
