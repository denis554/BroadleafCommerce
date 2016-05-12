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
package org.broadleafcommerce.core.rest.api.v2.wrapper;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.domain.TaxDetail;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "taxDetail")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class TaxDetailWrapper extends BaseWrapper implements APIWrapper<TaxDetail> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected BroadleafEnumerationTypeWrapper taxType;

    @XmlElement
    protected Money amount;

    @XmlElement
    protected BigDecimal rate;

    @XmlElement
    protected String currency;

    @XmlElement
    protected String jurisdictionName;

    @XmlElement
    protected String taxName;

    @XmlElement
    protected String region;

    @XmlElement
    protected String country;

    @Override
    public void wrapDetails(TaxDetail model, HttpServletRequest request) {
        this.id = model.getId();
        if (model.getType() != null) {
            this.taxType = (BroadleafEnumerationTypeWrapper) context.getBean(BroadleafEnumerationTypeWrapper.class.getName());
            this.taxType.wrapDetails(model.getType(), request);
        }
        this.amount = model.getAmount();
        this.rate = model.getRate();
        if (model.getCurrency() != null) {
            this.currency = model.getCurrency().getCurrencyCode();
        }
        this.jurisdictionName = model.getJurisdictionName();
        this.taxName = model.getTaxName();
        this.region = model.getRegion();
        this.country = model.getCountry();
    }

    @Override
    public void wrapSummary(TaxDetail model, HttpServletRequest request) {
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
     * @return the taxType
     */
    public BroadleafEnumerationTypeWrapper getTaxType() {
        return taxType;
    }

    
    /**
     * @param taxType the taxType to set
     */
    public void setTaxType(BroadleafEnumerationTypeWrapper taxType) {
        this.taxType = taxType;
    }

    
    /**
     * @return the amount
     */
    public Money getAmount() {
        return amount;
    }

    
    /**
     * @param amount the amount to set
     */
    public void setAmount(Money amount) {
        this.amount = amount;
    }

    
    /**
     * @return the rate
     */
    public BigDecimal getRate() {
        return rate;
    }

    
    /**
     * @param rate the rate to set
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    
    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    
    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    
    /**
     * @return the jurisdictionName
     */
    public String getJurisdictionName() {
        return jurisdictionName;
    }

    
    /**
     * @param jurisdictionName the jurisdictionName to set
     */
    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    
    /**
     * @return the taxName
     */
    public String getTaxName() {
        return taxName;
    }

    
    /**
     * @param taxName the taxName to set
     */
    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    
    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    
    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    
    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    
    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

}
