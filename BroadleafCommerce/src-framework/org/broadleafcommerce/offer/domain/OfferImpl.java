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
package org.broadleafcommerce.offer.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.broadleafcommerce.offer.service.type.OfferDeliveryType;
import org.broadleafcommerce.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.offer.service.type.OfferType;
import org.broadleafcommerce.util.money.Money;

@Entity
@Table(name = "BLC_OFFER")
public class OfferImpl implements Serializable, Offer {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "OFFER_ID")
    private Long id;

    @Column(name = "OFFER_NAME")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFFER_TYPE")
    private OfferType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "OFFER_DISCOUNT_TYPE")
    private OfferDiscountType discountType;

    @Column(name = "OFFER_VALUE")
    private BigDecimal value;

    @Column(name = "OFFER_PRIORITY")
    private int priority;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "STACKABLE")
    private boolean stackable;

    @Column(name = "TARGET_SYSTEM")
    private String targetSystem;

    @Column(name = "APPLY_TO_SALE_PRICE")
    private boolean applyToSalePrice;

    @Column(name = "APPLIES_TO_RULES")
    private String appliesToOrderRules;

    @Column(name = "APPLIES_WHEN_RULES")
    private String appliesToCustomerRules;

    @Column(name = "APPLY_OFFER_TO_MARKED_ITEMS")
    private boolean applyDiscountToMarkedItems;

    @Column(name = "COMBINABLE_WITH_OTHER_OFFERS")
    private boolean combinableWithOtherOffers;  // no offers can be applied on top of this offer; if false, stackable has to be false also

    @Enumerated(EnumType.STRING)
    @Column(name = "OFFER_DELIVERY_TYPE")
    private OfferDeliveryType deliveryType;

    @Column(name = "MAX_USES")
    private int maxUses;

    @Column(name = "USES")
    private int uses;

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

    public OfferType getType() {
        return type;
    }

    public void setType(OfferType type) {
        this.type = type;
    }

    public OfferDiscountType getDiscountType() {
        return discountType;

    }

    @Override
    public void setDiscountType(OfferDiscountType discountType) {
        this.discountType = discountType;

    }

    public Money getValue() {
        return value == null ? null : new Money(value);
    }

    public void setValue(Money value) {
        this.value = Money.toAmount(value);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns true if this offer can be stacked on top of another offer.  Stackable is evaluated
     * against offers with the same offer type.
     *
     * @return true if stackable, otherwise false
     */
    public boolean isStackable() {
        return stackable;
    }

    /**
     * Sets the stackable value for this offer.
     *
     * @param stackable
     */
    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    @Override
    public boolean getApplyDiscountToSalePrice() {
        return applyToSalePrice;
    }

    @Override
    public void setApplyDiscountToSalePrice(boolean applyToSalePrice) {
        this.applyToSalePrice=applyToSalePrice;

    }

    public String getAppliesToOrderRules() {
        return appliesToOrderRules;
    }

    public void setAppliesToOrderRules(String appliesToOrderRules) {
        this.appliesToOrderRules = appliesToOrderRules;
    }

    public String getAppliesToCustomerRules() {
        return appliesToCustomerRules;
    }

    public void setAppliesToCustomerRules(String appliesToCustomerRules) {
        this.appliesToCustomerRules = appliesToCustomerRules;
    }

    public boolean isApplyDiscountToMarkedItems() {
        return applyDiscountToMarkedItems;
    }

    public void setApplyDiscountToMarkedItems(boolean applyDiscountToMarkedItems) {
        this.applyDiscountToMarkedItems = applyDiscountToMarkedItems;
    }

    /**
     * Returns true if this offer can be combined with other offers in the order.
     *
     * @return true if combinableWithOtherOffers, otherwise false
     */
    public boolean isCombinableWithOtherOffers() {
        return combinableWithOtherOffers;
    }

    /**
     * Sets the combinableWithOtherOffers value for this offer.
     *
     * @param combinableWithOtherOffers
     */
    public void setCombinableWithOtherOffers(boolean combinableWithOtherOffers) {
        this.combinableWithOtherOffers = combinableWithOtherOffers;
    }

    public OfferDeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(OfferDeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }



}
