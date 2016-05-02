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

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.util.xml.ISO8601DateAdapter;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.inventory.service.type.InventoryType;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This is a JAXB wrapper to wrap Sku.
 * <p/>
 * User: Kelly Tisdell
 * Date: 4/10/12
 */
@XmlRootElement(name = "sku")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SkuWrapper extends BaseWrapper implements APIWrapper<Sku>, APIUnwrapper<Sku> {

    @XmlElement
    protected Long id;

    @XmlElement
    @XmlJavaTypeAdapter(ISO8601DateAdapter.class)
    protected Date activeStartDate;

    @XmlElement
    @XmlJavaTypeAdapter(ISO8601DateAdapter.class)
    protected Date activeEndDate;

    @XmlElement
    protected String name;

    @XmlElement
    protected Boolean active;

    @XmlElement
    protected String inventoryType;

    @XmlElement
    protected String description;

    @XmlElement
    protected Money retailPrice;

    @XmlElement
    protected Money salePrice;

    @XmlElement
    protected WeightWrapper weight;

    @XmlElement
    protected DimensionWrapper dimension;

    @Override
    public void wrapDetails(Sku model, HttpServletRequest request) {
        this.id = model.getId();
        this.activeStartDate = model.getActiveStartDate();
        this.activeEndDate = model.getActiveEndDate();
        this.name = model.getName();
        this.description = model.getDescription();
        this.retailPrice = model.getRetailPrice();
        this.salePrice = model.getSalePrice();
        this.active = model.isActive();
        if (model.getInventoryType() != null) {
            this.inventoryType = model.getInventoryType().getType();
        }

        if (model.getWeight() != null) {
            weight = (WeightWrapper) context.getBean(WeightWrapper.class.getName());
            weight.wrapDetails(model.getWeight(), request);
        }

        if (model.getDimension() != null) {
            dimension = (DimensionWrapper) context.getBean(DimensionWrapper.class.getName());
            dimension.wrapDetails(model.getDimension(), request);
        }
    }

    /**
     * restful method to convert the wrapper in a domain object.
     * None of its basic type or object fields are assumed. Of those child object present, 
     * no database presence is assumed. No "calculated" (i.e. active) fields are processed.
     */
    @Override
    public Sku unwrap(HttpServletRequest request, ApplicationContext context) {
        CatalogService catalogService = (CatalogService) context.getBean("blCatalogService");
        Sku sku = catalogService.createSku();

        sku.setId(this.id);
        sku.setName(this.name);
        sku.setActiveEndDate(this.activeEndDate);
        sku.setActiveStartDate(this.activeStartDate);
        sku.setInventoryType(InventoryType.getInstance(this.inventoryType));
        sku.setRetailPrice(this.retailPrice);
        sku.setSalePrice(this.salePrice);
        sku.setDimension(this.getDimension().unwrap(request, context));
        sku.setWeight(this.getWeight().unwrap(request, context));
        return sku;
    }

    @Override
    public void wrapSummary(Sku model, HttpServletRequest request) {
        wrapDetails(model, request);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getActiveStartDate() {
        return activeStartDate;
    }

    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    public Date getActiveEndDate() {
        return activeEndDate;
    }

    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(String inventoryType) {
        this.inventoryType = inventoryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Money getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(Money retailPrice) {
        this.retailPrice = retailPrice;
    }

    public Money getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Money salePrice) {
        this.salePrice = salePrice;
    }

    public WeightWrapper getWeight() {
        return weight;
    }

    public void setWeight(WeightWrapper weight) {
        this.weight = weight;
    }

    public DimensionWrapper getDimension() {
        return dimension;
    }

    public void setDimension(DimensionWrapper dimension) {
        this.dimension = dimension;
    }
}
