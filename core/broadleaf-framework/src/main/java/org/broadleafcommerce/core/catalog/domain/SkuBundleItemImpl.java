/*
 * #%L
 * BroadleafCommerce Framework
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
package org.broadleafcommerce.core.catalog.domain;

import org.broadleafcommerce.common.copy.CreateResponse;
import org.broadleafcommerce.common.copy.MultiTenantCopyContext;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationToOneLookup;
import org.broadleafcommerce.common.presentation.RequiredOverride;
import org.broadleafcommerce.common.presentation.ValidationConfiguration;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.core.catalog.service.dynamic.DefaultDynamicSkuPricingInvocationHandler;
import org.broadleafcommerce.core.catalog.service.dynamic.DynamicSkuPrices;
import org.broadleafcommerce.core.catalog.service.dynamic.SkuPricingConsiderationContext;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SKU_BUNDLE_ITEM")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blProducts")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.SANDBOX, skipOverlaps=true),
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_CATALOG)
})
public class SkuBundleItemImpl implements SkuBundleItem, SkuBundleItemAdminPresentation {

    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(generator = "SkuBundleItemId")
    @GenericGenerator(
        name="SkuBundleItemId",
        strategy="org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name = "segment_value", value = "SkuBundleItemImpl"),
            @Parameter(name = "entity_name", value = "org.broadleafcommerce.core.catalog.domain.SkuBundleItemImpl")
        }
    )
    @Column(name = "SKU_BUNDLE_ITEM_ID")
    @AdminPresentation(friendlyName = "SkuBundleItemImpl_ID", group = GroupName.General, visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @Column(name = "QUANTITY", nullable=false)
    @AdminPresentation(friendlyName = "bundleItemQuantity",
        group = GroupName.General, order = FieldOrder.QUANTITY,
        prominent = true, gridOrder = FieldOrder.QUANTITY,
        requiredOverride = RequiredOverride.REQUIRED)
    protected Integer quantity;

    @Column(name = "ITEM_SALE_PRICE", precision=19, scale=5)
    @AdminPresentation(friendlyName = "bundleItemSalePrice",
        group = GroupName.General, order = FieldOrder.ITEM_SALE_PRICE,
        prominent = true, gridOrder = FieldOrder.ITEM_SALE_PRICE,
        tooltip="bundleItemSalePriceTooltip", 
        fieldType = SupportedFieldType.MONEY)
    protected BigDecimal itemSalePrice;

    @ManyToOne(targetEntity = ProductBundleImpl.class, optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "PRODUCT_BUNDLE_ID", referencedColumnName = "PRODUCT_ID")
    protected ProductBundle bundle;

    @ManyToOne(targetEntity = SkuImpl.class, optional = false)
    @JoinColumn(name = "SKU_ID", referencedColumnName = "SKU_ID")
    @AdminPresentation(friendlyName = "Sku",
        group = GroupName.General, order = FieldOrder.SKU,
        prominent = true, gridOrder = FieldOrder.SKU,
        validationConfigurations = @ValidationConfiguration(validationImplementation = "blProductBundleSkuBundleItemValidator"))
    @AdminPresentationToOneLookup()
    protected Sku sku;

    /** The display order. */
    @Column(name = "SEQUENCE", precision = 10, scale = 6)
    @AdminPresentation(visibility = VisibilityEnum.HIDDEN_ALL)
    protected BigDecimal sequence;

    @Transient
    protected DynamicSkuPrices dynamicPrices = null;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
   
    public Money getDynamicSalePrice(Sku sku, BigDecimal salePrice) {   
        Money returnPrice = null;
        
        if (SkuPricingConsiderationContext.hasDynamicPricing()) {
            if (dynamicPrices != null) {
                returnPrice = dynamicPrices.getSalePrice();
            } else {
                DefaultDynamicSkuPricingInvocationHandler handler = new DefaultDynamicSkuPricingInvocationHandler(sku, salePrice);
                Sku proxy = (Sku) Proxy.newProxyInstance(sku.getClass().getClassLoader(), ClassUtils.getAllInterfacesForClass(sku.getClass()), handler);
                dynamicPrices = SkuPricingConsiderationContext.getSkuPricingService().getSkuPrices(proxy, SkuPricingConsiderationContext.getSkuPricingConsiderationContext());
                returnPrice = dynamicPrices.getSalePrice();
            }
        } else {
            if (salePrice != null) {
                returnPrice = new Money(salePrice,Money.defaultCurrency());
            }
        }
        
        return returnPrice;   
    }
    @Override
    public void setSalePrice(Money salePrice) {
        if (salePrice != null) {
            this.itemSalePrice = salePrice.getAmount();
        } else {
            this.itemSalePrice = null;
        }
    }


    @Override
    public Money getSalePrice() {
        if (itemSalePrice == null) {
            return sku.getSalePrice();
        } else {
            return getDynamicSalePrice(sku, itemSalePrice);
        }
    }

    @Override
    public Money getRetailPrice() {
         return sku.getRetailPrice();
     }

    @Override
    public ProductBundle getBundle() {
        return bundle;
    }

    @Override
    public void setBundle(ProductBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Sku getSku() {
        return sku;
    }

    @Override
    public void setSku(Sku sku) {
        this.sku = sku;
    }

    @Override
    public BigDecimal getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(BigDecimal sequence) {
        this.sequence = sequence;
    }

    @Override
    public void clearDynamicPrices() {
        dynamicPrices = null;
        sku.clearDynamicPrices();
    }

    @Override
    public <G extends SkuBundleItem> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        SkuBundleItem cloned = createResponse.getClone();
        cloned.setQuantity(quantity);
        cloned.setSalePrice(getSalePrice());
        cloned.setSequence(sequence);
        if (sku != null) {
            cloned.setSku(sku.createOrRetrieveCopyInstance(context).getClone());
        }
        if (bundle != null) {
            cloned.setBundle((ProductBundle) bundle.createOrRetrieveCopyInstance(context).getClone());
        }
        return createResponse;
    }
}
