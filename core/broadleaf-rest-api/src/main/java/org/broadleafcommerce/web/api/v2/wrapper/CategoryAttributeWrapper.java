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

import org.broadleafcommerce.core.catalog.domain.CategoryAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * @author Phillip Verheyden
 */
@XmlRootElement(name = "categoryAttribute")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CategoryAttributeWrapper extends BaseWrapper implements APIWrapper<CategoryAttribute>{

    @XmlElement
    protected Long id;

    @XmlElement
    protected Long categoryId;

    @XmlElement
    protected String attributeName;

    @XmlElement
    protected String attributeValue;

    @Override
    public void wrapDetails(CategoryAttribute model, HttpServletRequest request) {
        this.id = model.getId();
        this.categoryId = model.getCategory().getId();
        this.attributeName = model.getName();
        this.attributeValue = model.getValue();
    }

    @Override
    public void wrapSummary(CategoryAttribute model, HttpServletRequest request) {
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
     * @return the categoryId
     */
    public Long getCategoryId() {
        return categoryId;
    }

    
    /**
     * @param categoryId the categoryId to set
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    
    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    
    /**
     * @param attributeName the attributeName to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    
    /**
     * @return the attributeValue
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    
    /**
     * @param attributeValue the attributeValue to set
     */
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}
