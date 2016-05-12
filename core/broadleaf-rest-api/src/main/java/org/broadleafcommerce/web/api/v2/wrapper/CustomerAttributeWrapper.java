/*
 * #%L
 * BroadleafCommerce Framework Web
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

package org.broadleafcommerce.web.api.v2.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.broadleafcommerce.profile.core.domain.CustomerAttribute;
import org.broadleafcommerce.profile.core.domain.CustomerAttributeImpl;
import org.springframework.context.ApplicationContext;

/**
 * API wrapper to wrap Customer Attributes.
 * @author Priyesh Patel
 *
 */
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
