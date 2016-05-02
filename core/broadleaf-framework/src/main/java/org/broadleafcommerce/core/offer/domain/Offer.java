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
package org.broadleafcommerce.core.offer.domain;

import org.broadleafcommerce.common.copy.MultiTenantCloneable;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.offer.service.type.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Offer extends Status, Serializable,MultiTenantCloneable<Offer> {

    public void setId(Long id);

    public Long getId();

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public OfferType getType();

    public void setType(OfferType offerType);

    public OfferDiscountType getDiscountType();

    public void setDiscountType(OfferDiscountType type);

    public BigDecimal getValue();

    public void setValue(BigDecimal value);

    public int getPriority();

    public void setPriority(Integer priority);

    public Date getStartDate();

    public void setStartDate(Date startDate);

    public Date getEndDate();

    public void setEndDate(Date endDate);

    /**
     * @deprecated
     * Use isCombinable instead.
     * @return
     */
    @Deprecated
    public boolean isStackable();

    /**
     * @deprecated
     * calls {@link #setCombinableWithOtherOffers(boolean)}
     * @param stackable
     */
    @Deprecated
    public void setStackable(boolean stackable);

    public String getTargetSystem();

    public void setTargetSystem(String targetSystem);

    public boolean getApplyDiscountToSalePrice();

    public void setApplyDiscountToSalePrice(boolean applyToSalePrice);

    @Deprecated
    public String getAppliesToOrderRules();

    @Deprecated
    public void setAppliesToOrderRules(String appliesToRules);

    @Deprecated
    public String getAppliesToCustomerRules();

    @Deprecated
    public void setAppliesToCustomerRules(String appliesToCustomerRules);

    @Deprecated
    public boolean isApplyDiscountToMarkedItems();

    @Deprecated
    public void setApplyDiscountToMarkedItems(boolean applyDiscountToMarkedItems);

    public OfferItemRestrictionRuleType getOfferItemQualifierRuleType();

    public void setOfferItemQualifierRuleType(OfferItemRestrictionRuleType restrictionRuleType);

    public OfferItemRestrictionRuleType getOfferItemTargetRuleType();

    public void setOfferItemTargetRuleType(OfferItemRestrictionRuleType restrictionRuleType);

    public Boolean getQualifiersCanBeQualifiers();

    public void setQualifiersCanBeQualifiers(Boolean qualifiersCanBeQualifiers);

    public Boolean getQualifiersCanBeTargets();

    public void setQualifiersCanBeTargets(Boolean qualifiersCanBeTargets);

    /**
     * Returns false if this offer is not combinable with other offers of the same type.
     * For example, if this is an Item offer it could be combined with other Order or FG offers
     * but it cannot be combined with other Item offers.
     * 
     * @return
     */
    @Deprecated
    public boolean isCombinableWithOtherOffers();

    @Deprecated
    public void setCombinableWithOtherOffers(boolean combinableWithOtherOffers);

    public Boolean getCombinableWithOrderOffers();

    public void setCombinableWithOrderOffers(Boolean combinableWithOrderOffers);

    public Boolean getCombinableWithItemOffers();

    public void setCombinableWithItemOffers(Boolean combinableWithItemOffers);

    public Boolean getCombinableWithItemOffersImpactingOtherItems();

    public void setCombinableWithItemOffersImpactingOtherItems(Boolean combinableWithItemOffersImpactingOtherItems);

    public Boolean getCombinableWithShippingOffers();

    public void setCombinableWithShippingOffers(Boolean combinableWithShippingOffers);

    public StackabilityType getStackableWithOtherOffers();

    public void setStackableWithOtherOffers(StackabilityType stackableWithOtherOffers);

    /**
     * Returns true if the offer system should automatically add this offer for consideration (versus requiring a code or 
     * other delivery mechanism).    This does not guarantee that the offer will qualify.   All rules associated with this
     * offer must still pass.   A true value here just means that the offer will be considered.
     * 
     * For backwards compatibility, if the underlying property is null, this method will check the 
     * {@link #getDeliveryType()} method and return true if that value is set to AUTOMATIC.    
     * 
     * If still null, this value will return false.
     * 
     * @return
     */
    public boolean getRequiresCode();

    /**
     * Sets whether or not this offer should be automatically considered for consideration (versus requiring a code or 
     * other delivery mechanism).
     * @see #getRequiresCode()
     */
    public void setRequiresCode(boolean requiresCode);

    /**
     * @deprecated Replaced by isAutomaticallyApplied property.   In prior versions of Broadleaf deliveryType was used to 
     * differentiate "automatic" orders from those requiring a code.   If the underlying property is null, 
     * this method will return a delivery type based on the isAutomatic property. 
     * @return
     */
    @Deprecated
    public OfferDeliveryType getDeliveryType();

    /**
     * @deprecated Replaced by setAutomaticallyApplied(boolean val).
     * @param deliveryType
     */
    @Deprecated
    public void setDeliveryType(OfferDeliveryType deliveryType);

    /**
     * Returns the maximum number of times that this offer
     * can be used by the same customer.   This field
     * tracks the number of times the offer can be used
     * and not how many times it is applied.
     *
     * 0 or null indicates unlimited usage per customer.
     *
     * @return
     */
    public Long getMaxUsesPerCustomer();

    /**
     * Sets the maximum number of times that this offer
     * can be used by the same customer.  Intended as a transient
     * field that gets derived from the other persisted max uses fields
     * including maxUsesPerOrder and maxUsesPerCustomer.
     *
     * 0 or null indicates unlimited usage.
     *
     * @param maxUses
     */
    public void setMaxUsesPerCustomer(Long maxUses);

    /**
     * Indicates that there is no limit to how many times a customer can use this offer. By default this is true if
     * {@link #getMaxUsesPerCustomer()} == 0
     */
    public boolean isUnlimitedUsePerCustomer();
    
    /**
     * Whether or not this offer has limited use in an order. By default this is true if {@link #getMaxUsesPerCustomer()} > 0
     */
    public boolean isLimitedUsePerCustomer();

    /**
     * Returns the maximum number of times that this offer
     * can be used in the current order.
     *
     * 0 indicates unlimited usage.
     *
     * @deprecated use {@link #getMaxUsesPerOrder()} directly instead
     */
    @Deprecated
    public int getMaxUses() ;

    /**
     * Sets the maximum number of times that this offer
     * can be used in the current order.
     *
     * 0 indicates unlimited usage.
     *
     * @deprecated use {@link #setMaxUsesPerOrder(int)} directly instead
     */
    @Deprecated
    public void setMaxUses(int maxUses) ;

    /**
     * Returns the maximum number of times that this offer
     * can be used in the current order.
     *
     * 0 indicates unlimited usage.
     */
    public int getMaxUsesPerOrder();

    /**
     * Sets the maximum number of times that this offer
     * can be used in the current order.
     *
     * 0 indicates unlimited usage.
     *
     * @param maxUses
     */
    public void setMaxUsesPerOrder(int maxUsesPerOrder);
    
    /**
     * Indicates that there is no limit to how many times this offer can be applied to the order. By default this is true if
     * {@link #getMaxUsesPerOrder()} == 0
     */
    public boolean isUnlimitedUsePerOrder();
    
    /**
     * Whether or not this offer has limited use in an order. By default this is true if {@link #getMaxUsesPerOrder()} > 0
     */
    public boolean isLimitedUsePerOrder();
    
    /**
     * @deprecated replaced by the {@link OfferAudit} table
     */
    @Deprecated
    public int getUses() ;

    /**
     * @deprecated replaced by the {@link OfferAudit} table
     */
    @Deprecated
    public void setUses(int uses) ;

    //    /**
    //     * @deprecated use {@link #getQualifyingItemCriteriaXref()} instead
    //     * @return
    //     */
    //    @Deprecated
    //    public Set<OfferItemCriteria> getQualifyingItemCriteria();
    //
    //    /**
    //     * @deprecated use {@link #setQualifyingItemCriteriaXref(java.util.Set)} instead
    //     * @param qualifyingItemCriteria
    //     */
    //    @Deprecated
    //    public void setQualifyingItemCriteria(Set<OfferItemCriteria> qualifyingItemCriteria);

    Set<OfferQualifyingCriteriaXref> getQualifyingItemCriteriaXref();

    void setQualifyingItemCriteriaXref(Set<OfferQualifyingCriteriaXref> qualifyingItemCriteriaXref);

    //    /**
    //     * @deprecated use {@link #getTargetItemCriteriaXref()} instead
    //     * @return
    //     */
    //    @Deprecated
    //    public Set<OfferItemCriteria> getTargetItemCriteria();
    //
    //    /**
    //     * @deprecated use {@link #setTargetItemCriteriaXref(java.util.Set)} instead
    //     * @param targetItemCriteria
    //     */
    //    @Deprecated
    //    public void setTargetItemCriteria(Set<OfferItemCriteria> targetItemCriteria);

    Set<OfferTargetCriteriaXref> getTargetItemCriteriaXref();

    void setTargetItemCriteriaXref(Set<OfferTargetCriteriaXref> targetItemCriteriaXref);
    
    public Boolean isTotalitarianOffer();

    public void setTotalitarianOffer(Boolean totalitarianOffer);

    Map<String, OfferOfferRuleXref> getOfferMatchRulesXref();

    void setOfferMatchRulesXref(Map<String, OfferOfferRuleXref> offerMatchRulesXref);
    
    public Boolean getTreatAsNewFormat();

    public void setTreatAsNewFormat(Boolean treatAsNewFormat);
    
    /**
     * Indicates the amount of items that must be purchased for this offer to
     * be considered for this order.
     * 
     * The system will find all qualifying items for the given offer and sum their prices before
     * any discounts are applied to make the determination.  
     * 
     * If the sum of the qualifying items is not greater than this value the offer is 
     * not considered by the offer processing algorithm.
     * @return
     */
    public Money getQualifyingItemSubTotal();
    
    public void setQualifyingItemSubTotal(Money qualifyingItemSubtotal);

    public Money getOrderMinSubTotal();

    public void setOrderMinSubTotal(Money orderMinSubTotal);

    public void setMarketingMessage(String marketingMessage);

    public String getMarketingMessage();

    /**
     * Returns the offer codes that can be used to retrieve this Offer. These codes would be used in situations where
     * this Offer is not automatically considered (meaning {@link Offer#getRequiresCode()} is false}
     */
    public List<OfferCode> getOfferCodes();
    
    /**
     * Sets the offer codes that can be used to retrieve this Offer. These codes would be used in situations where
     * this Offer is not automatically considered (meaning {@link Offer#getRequiresCode()} is false}
     */
    public void setOfferCodes(List<OfferCode> offerCodes);

    public Boolean getRequiresRelatedTargetAndQualifiers();

    public void setRequiresRelatedTargetAndQualifiers(Boolean requiresRelatedTargetAndQualifiers);

}
