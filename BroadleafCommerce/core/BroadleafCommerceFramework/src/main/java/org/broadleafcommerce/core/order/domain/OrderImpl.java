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
package org.broadleafcommerce.core.order.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.broadleafcommerce.core.offer.domain.CandidateOrderOffer;
import org.broadleafcommerce.core.offer.domain.CandidateOrderOfferImpl;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferCode;
import org.broadleafcommerce.core.offer.domain.OfferCodeImpl;
import org.broadleafcommerce.core.offer.domain.OfferImpl;
import org.broadleafcommerce.core.offer.domain.OfferInfo;
import org.broadleafcommerce.core.offer.domain.OfferInfoImpl;
import org.broadleafcommerce.core.offer.domain.OrderAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderAdjustmentImpl;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustment;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfoImpl;
import org.broadleafcommerce.gwt.client.presentation.SupportedFieldType;
import org.broadleafcommerce.money.Money;
import org.broadleafcommerce.presentation.AdminPresentation;
import org.broadleafcommerce.profile.common.domain.Auditable;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.broadleafcommerce.profile.core.domain.listener.AuditableListener;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyManyToMany;

@Entity
@EntityListeners(value = { AuditableListener.class })
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_ORDER")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
public class OrderImpl implements Order {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "OrderId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "OrderId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "OrderImpl", allocationSize = 50)
    @Column(name = "ORDER_ID")
    protected Long id;

    @Embedded
    protected Auditable auditable = new Auditable();

    @Column(name = "NAME")
    @Index(name="ORDER_NAME_INDEX", columnNames={"NAME"})
    @AdminPresentation(friendlyName="Order Name", group="Order", order=1, prominent=true)
    protected String name;

    @ManyToOne(targetEntity = CustomerImpl.class, optional=false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    @Index(name="ORDER_CUSTOMER_INDEX", columnNames={"CUSTOMER_ID"})
    protected Customer customer;

    @Column(name = "ORDER_STATUS")
    @Index(name="ORDER_STATUS_INDEX", columnNames={"ORDER_STATUS"})
    @AdminPresentation(friendlyName="Order Status", group="Order", order=2, prominent=true)
    protected String status;

    @Column(name = "CITY_TAX")
    @AdminPresentation(friendlyName="Order City Tax", group="Order", order=4, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal cityTax;

    @Column(name = "COUNTY_TAX")
    @AdminPresentation(friendlyName="Order County Tax", group="Order", order=5, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal countyTax;

    @Column(name = "STATE_TAX")
    @AdminPresentation(friendlyName="Order State Tax", group="Order", order=6, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal stateTax;
    
    @Column(name = "DISTRICT_TAX")
    @AdminPresentation(friendlyName="Order District Tax", group="Order", order=7, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal districtTax;

    @Column(name = "COUNTRY_TAX")
    @AdminPresentation(friendlyName="Order Country Tax", group="Order", order=8, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal countryTax;

    @Column(name = "TOTAL_TAX")
    @AdminPresentation(friendlyName="Order Total Tax", group="Order", order=9, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal totalTax;

    @Column(name = "TOTAL_SHIPPING")
    @AdminPresentation(friendlyName="Order Total Shipping", group="Order", order=10, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal totalShipping;

    @Column(name = "ORDER_SUBTOTAL")
    @AdminPresentation(friendlyName="Order Subtotal", group="Order", order=3, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal subTotal;

    @Column(name = "ORDER_TOTAL")
    @AdminPresentation(friendlyName="Order Total", group="Order", order=11, fieldType=SupportedFieldType.MONEY)
    protected BigDecimal total;

    @Column(name = "SUBMIT_DATE")
    @AdminPresentation(friendlyName="Order Submit Date", group="Order", order=12)
    protected Date submitDate;

    @Column(name = "ORDER_NUMBER")
    @Index(name="ORDER_NUMBER_INDEX", columnNames={"ORDER_NUMBER"})
    @AdminPresentation(friendlyName="Order Number", group="Order", order=3, prominent=true)
    private String orderNumber;

    @Column(name = "EMAIL_ADDRESS")
    @Index(name="ORDER_EMAIL_INDEX", columnNames={"EMAIL_ADDRESS"})
    @AdminPresentation(friendlyName="Order Email Address", group="Order", order=13)
    protected String emailAddress;

    @Transient
    protected BigDecimal adjustmentPrice;  // retailPrice with order adjustments (no item adjustments)

    @OneToMany(mappedBy = "order", targetEntity = OrderItemImpl.class, cascade = {CascadeType.ALL})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<OrderItem> orderItems = new ArrayList<OrderItem>();

    @OneToMany(mappedBy = "order", targetEntity = FulfillmentGroupImpl.class, cascade = {CascadeType.ALL})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    @OrderBy("id")
    protected List<FulfillmentGroup> fulfillmentGroups = new ArrayList<FulfillmentGroup>();

    @OneToMany(mappedBy = "order", targetEntity = OrderAdjustmentImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<OrderAdjustment> orderAdjustments = new ArrayList<OrderAdjustment>();

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = OfferCodeImpl.class)
    @JoinTable(name = "BLC_ORDER_OFFER_CODE_XREF", joinColumns = @JoinColumn(name = "ORDER_ID", referencedColumnName = "ORDER_ID"), inverseJoinColumns = @JoinColumn(name = "OFFER_CODE_ID", referencedColumnName = "OFFER_CODE_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<OfferCode> addedOfferCodes = new ArrayList<OfferCode>();

    @OneToMany(mappedBy = "order", targetEntity = CandidateOrderOfferImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<CandidateOrderOffer> candidateOrderOffers = new ArrayList<CandidateOrderOffer>();

    @OneToMany(mappedBy = "order", targetEntity = PaymentInfoImpl.class, cascade = {CascadeType.ALL})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<PaymentInfo> paymentInfos = new ArrayList<PaymentInfo>();

    @ManyToMany(targetEntity=OfferInfoImpl.class)
    @JoinTable(name = "BLC_ADDITIONAL_OFFER_INFO", inverseJoinColumns = @JoinColumn(name = "OFFER_INFO_ID", referencedColumnName = "OFFER_INFO_ID"))
    @MapKeyManyToMany(joinColumns = {@JoinColumn(name = "OFFER_ID") }, targetEntity=OfferImpl.class)
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected Map<Offer, OfferInfo> additionalOfferInformation = new HashMap<Offer, OfferInfo>();

    @Transient
    protected boolean totalitarianOfferApplied = false;
    
    @Transient
    protected boolean notCombinableOfferAppliedAtAnyLevel = false;
    
    @Transient
    @Deprecated
    protected boolean markedForOffer;

    @Transient
    protected boolean notCombinableOfferApplied = false;    
    
    @Transient
    protected boolean hasOrderAdjustments = false;
    
    @Transient
    protected List<OrderItem> splitItems = new ArrayList<OrderItem>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Auditable getAuditable() {
        return auditable;
    }

    public void setAuditable(Auditable auditable) {
        this.auditable = auditable;
    }

    public Money getSubTotal() {
        return subTotal == null ? null : new Money(subTotal);
    }

    public void setSubTotal(Money subTotal) {
        this.subTotal = Money.toAmount(subTotal);
    }

    public Money calculateOrderItemsCurrentPrice() {
        Money calculatedSubTotal = new Money();
        for (OrderItem orderItem : orderItems) {
            Money currentPrice = orderItem.getCurrentPrice();
            calculatedSubTotal = calculatedSubTotal.add(currentPrice.multiply(orderItem.getQuantity()));
        }
        return calculatedSubTotal;
    }

    public Money calculateOrderItemsFinalPrice() {
        Money calculatedSubTotal = new Money();
        for (OrderItem orderItem : orderItems) {
            Money price = orderItem.getPrice();
            calculatedSubTotal = calculatedSubTotal.add(price.multiply(orderItem.getQuantity()));
        }
        return calculatedSubTotal;
    }
    
    public Money calculateOrderItemsPriceWithoutAdjustments() {
        Money calculatedSubTotal = new Money();
        for (OrderItem orderItem : orderItems) {
            Money price = orderItem.getPriceBeforeAdjustments(true);
            calculatedSubTotal = calculatedSubTotal.add(price.multiply(orderItem.getQuantity()));
        }
        return calculatedSubTotal;
    }

    /**
     * Assigns a final price to all the order items
     */
    public void assignOrderItemsFinalPrice() {
        for (OrderItem orderItem : orderItems) {
            orderItem.assignFinalPrice();
        }
    }

    public Money getTotal() {
        return total == null ? null : new Money(total);
    }

    public void setTotal(Money orderTotal) {
        this.total = Money.toAmount(orderTotal);
    }

    public Money getRemainingTotal() {
        Money myTotal = getTotal();
        if (myTotal == null) {
            return null;
        }
        Money totalPayments = new Money(BigDecimal.ZERO);
        for (PaymentInfo pi : getPaymentInfos()) {
            if (pi.getAmount() != null) {
                totalPayments = totalPayments.add(pi.getAmount());
            }
        }
        return myTotal.subtract(totalPayments);
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return OrderStatus.getInstance(status);
    }

    public void setStatus(OrderStatus status) {
        this.status = status.getType();
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public List<FulfillmentGroup> getFulfillmentGroups() {
        return fulfillmentGroups;
    }

    public void setFulfillmentGroups(List<FulfillmentGroup> fulfillmentGroups) {
        this.fulfillmentGroups = fulfillmentGroups;
    }

    public void setCandidateOrderOffers(List<CandidateOrderOffer> candidateOrderOffers) {
        this.candidateOrderOffers = candidateOrderOffers;
    }

    public void addCandidateOrderOffer(CandidateOrderOffer candidateOrderOffer) {
        candidateOrderOffers.add(candidateOrderOffer);
    }

    public List<CandidateOrderOffer> getCandidateOrderOffers() {
        return candidateOrderOffers;
    }

    public void removeAllCandidateOffers() {
    	removeAllCandidateOrderOffers();
        if (getOrderItems() != null) {
            for (OrderItem item : getOrderItems()) {
                item.removeAllCandidateItemOffers();
            }
        }

        removeAllCandidateFulfillmentGroupOffers();
    }
    
    public void removeAllCandidateFulfillmentGroupOffers() {
    	if (getFulfillmentGroups() != null) {
            for (FulfillmentGroup fg : getFulfillmentGroups()) {
                fg.removeAllCandidateOffers();
            }
        }
    }

    public void removeAllCandidateOrderOffers() {
        if (candidateOrderOffers != null) {
            candidateOrderOffers.clear();
        }
    }

    @Deprecated
    public boolean isMarkedForOffer() {
        return markedForOffer;
    }

    @Deprecated
    public void setMarkedForOffer(boolean markedForOffer) {
        this.markedForOffer = markedForOffer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getCityTax() {
        return cityTax == null ? null : new Money(cityTax);
    }

    public void setCityTax(Money cityTax) {
        this.cityTax = Money.toAmount(cityTax);
    }

    public Money getCountyTax() {
        return countyTax == null ? null : new Money(countyTax);
    }

    public void setCountyTax(Money countyTax) {
        this.countyTax = Money.toAmount(countyTax);
    }

    public Money getStateTax() {
        return stateTax == null ? null : new Money(stateTax);
    }

    public void setStateTax(Money stateTax) {
        this.stateTax = Money.toAmount(stateTax);
    }
    
    public Money getDistrictTax() {
        return districtTax == null ? null : new Money(districtTax);
    }

    public void setDistrictTax(Money districtTax) {
        this.districtTax = Money.toAmount(districtTax);
    }

    public Money getCountryTax() {
        return countryTax == null ? null : new Money(countryTax);
    }

    public void setCountryTax(Money countryTax) {
        this.countryTax = Money.toAmount(countryTax);
    }

    public Money getTotalTax() {
        return totalTax == null ? null : new Money(totalTax);
    }

    public void setTotalTax(Money totalTax) {
        this.totalTax = Money.toAmount(totalTax);
    }

    public Money getTotalShipping() {
        return totalShipping == null ? null : new Money(totalShipping);
    }

    public void setTotalShipping(Money totalShipping) {
        this.totalShipping = Money.toAmount(totalShipping);
    }

    public List<PaymentInfo> getPaymentInfos() {
        return paymentInfos;
    }

    public void setPaymentInfos(List<PaymentInfo> paymentInfos) {
        this.paymentInfos = paymentInfos;
    }

    public boolean hasCategoryItem(String categoryName) {
        for (OrderItem orderItem : orderItems) {
            if(orderItem.isInCategory(categoryName)) {
                return true;
            }
        }
        return false;
    }

    public List<OrderAdjustment> getOrderAdjustments() {
        return Collections.unmodifiableList(this.orderAdjustments);
    }

    public void addOrderAdjustments(OrderAdjustment orderAdjustment) {
        if (this.orderAdjustments.size() == 0) {
            adjustmentPrice = getSubTotal().getAmount();
        }
        adjustmentPrice = adjustmentPrice.subtract(orderAdjustment.getValue().getAmount());
        this.orderAdjustments.add(orderAdjustment);
        if (!orderAdjustment.getOffer().isCombinableWithOtherOffers()) {
        	notCombinableOfferApplied = true;
        }
        resetTotalitarianOfferApplied();
        hasOrderAdjustments = true;
    }
    
    public void resetTotalitarianOfferApplied() {
    	totalitarianOfferApplied = false;
    	notCombinableOfferAppliedAtAnyLevel = false;
    	for (OrderAdjustment adjustment : orderAdjustments) {
    		if (adjustment.getOffer().isTotalitarianOffer() != null && adjustment.getOffer().isTotalitarianOffer()) {
    			totalitarianOfferApplied = true;
    			break;
    		}
    		if (!adjustment.getOffer().isCombinableWithOtherOffers()) {
    			notCombinableOfferAppliedAtAnyLevel = true;
    			break;
    		}
    	}
    	if (!totalitarianOfferApplied || !notCombinableOfferAppliedAtAnyLevel) {
    		check: {
		    	for (OrderItem orderItem : orderItems) {
		    		for (OrderItemAdjustment adjustment : orderItem.getOrderItemAdjustments()) {
		    			if (adjustment.getOffer().isTotalitarianOffer() != null && adjustment.getOffer().isTotalitarianOffer()) {
		    				totalitarianOfferApplied = true;
		    				break check;
		    			}
		    			if (!adjustment.getOffer().isCombinableWithOtherOffers()) {
		        			notCombinableOfferAppliedAtAnyLevel = true;
		        			break check;
		        		}
		    		}
		    	}
    		}
    	}
    	if (!totalitarianOfferApplied || !notCombinableOfferAppliedAtAnyLevel) {
    		check: {
		    	for (FulfillmentGroup fg : fulfillmentGroups) {
		    		for (FulfillmentGroupAdjustment adjustment : fg.getFulfillmentGroupAdjustments()) {
		    			if (adjustment.getOffer().isTotalitarianOffer() != null && adjustment.getOffer().isTotalitarianOffer()) {
		    				totalitarianOfferApplied = true;
		    				break check;
		    			}
		    			if (!adjustment.getOffer().isCombinableWithOtherOffers()) {
		        			notCombinableOfferAppliedAtAnyLevel = true;
		        			break check;
		        		}
		    		}
		    	}
    		}
    	}
    }

    public void removeAllAdjustments() {
        removeAllItemAdjustments();
        removeAllFulfillmentAdjustments();
        removeAllOrderAdjustments();
    }

    public void removeAllOrderAdjustments() {
        if (orderAdjustments != null) {
            orderAdjustments.clear();
        }
        adjustmentPrice = null;
    	notCombinableOfferApplied = false;
        hasOrderAdjustments = false;
        resetTotalitarianOfferApplied();
   }

    public void removeAllItemAdjustments() {
        for (OrderItem orderItem: orderItems) {
            orderItem.removeAllAdjustments();
        }
        splitItems.clear();
    }

    public void removeAllFulfillmentAdjustments() {
        for (FulfillmentGroup fulfillmentGroup : fulfillmentGroups) {
            fulfillmentGroup.removeAllAdjustments();
        }
    }

    protected void setOrderAdjustments(List<OrderAdjustment> orderAdjustments) {
        this.orderAdjustments = orderAdjustments;
    }

    public Money getAdjustmentPrice() {
        return adjustmentPrice == null ? null : new Money(adjustmentPrice);
    }

    public void setAdjustmentPrice(Money adjustmentPrice) {
        this.adjustmentPrice = Money.toAmount(adjustmentPrice);
    }

    /*
     * Checks the order adjustment to see if it is not stackable
     */
    public boolean containsNotStackableOrderOffer() {
        boolean isContainsNotStackableOrderOffer = false;
        for (OrderAdjustment orderAdjustment: orderAdjustments) {
            if (!orderAdjustment.getOffer().isStackable()) {
                isContainsNotStackableOrderOffer = true;
                break;
            }
        }
        return isContainsNotStackableOrderOffer;
    }
    
    public boolean containsNotStackableFulfillmentGroupOffer() {
        boolean isContainsNotStackableFulfillmentGroupOffer = false;
        for (FulfillmentGroup fg : fulfillmentGroups) {
        	for (FulfillmentGroupAdjustment fgAdjustment : fg.getFulfillmentGroupAdjustments()) {
        		if (!fgAdjustment.getOffer().isStackable()) {
        			isContainsNotStackableFulfillmentGroupOffer = true;
        			break;
        		}
        	}
        }
        return isContainsNotStackableFulfillmentGroupOffer;
    }

    public List<DiscreteOrderItem> getDiscreteOrderItems() {
        List<DiscreteOrderItem> discreteOrderItems = new ArrayList<DiscreteOrderItem>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem instanceof BundleOrderItemImpl) {
                BundleOrderItemImpl bundleOrderItem = (BundleOrderItemImpl)orderItem;
                for (DiscreteOrderItem discreteOrderItem : bundleOrderItem.getDiscreteOrderItems()) {
                    discreteOrderItems.add(discreteOrderItem);
                }
            } else {
                DiscreteOrderItem discreteOrderItem = (DiscreteOrderItem)orderItem;
                discreteOrderItems.add(discreteOrderItem);
            }
        }
        return discreteOrderItems;
    }

    public List<DiscreteOrderItem> getDiscountableDiscreteOrderItems() {
        List<DiscreteOrderItem> discreteOrderItems = new ArrayList<DiscreteOrderItem>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem instanceof BundleOrderItemImpl) {
                BundleOrderItemImpl bundleOrderItem = (BundleOrderItemImpl)orderItem;
                for (DiscreteOrderItem discreteOrderItem : bundleOrderItem.getDiscreteOrderItems()) {
                    if (discreteOrderItem.getSku().isDiscountable()) {
                        discreteOrderItems.add(discreteOrderItem);
                    }
                }
            } else {
                DiscreteOrderItem discreteOrderItem = (DiscreteOrderItem)orderItem;
                if (discreteOrderItem.getSku().isDiscountable()) {
                    discreteOrderItems.add(discreteOrderItem);
                }
            }
        }
        return discreteOrderItems;
    }

    public List<OfferCode> getAddedOfferCodes() {
        return addedOfferCodes;
    }

    public void addAddedOfferCode(OfferCode addedOfferCode) {
        addedOfferCodes.add(addedOfferCode);
    }

    public void removeAllAddedOfferCodes() {
        if (addedOfferCodes != null) {
            addedOfferCodes.clear();
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getFulfillmentStatus() {
        return null;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Map<Offer, OfferInfo> getAdditionalOfferInformation() {
        return additionalOfferInformation;
    }

    public void setAdditionalOfferInformation(Map<Offer, OfferInfo> additionalOfferInformation) {
        this.additionalOfferInformation = additionalOfferInformation;
    }

    public Money getItemAdjustmentsValue() {
        Money itemAdjustmentsValue = new Money(0);
        for (OrderItem orderItem : orderItems) {
            itemAdjustmentsValue = itemAdjustmentsValue.add(orderItem.getAdjustmentValue().multiply(orderItem.getQuantity()));
        }
        return itemAdjustmentsValue;
    }

    public Money getOrderAdjustmentsValue() {
        Money orderAdjustmentsValue = new Money(0);
        for (OrderAdjustment orderAdjustment : orderAdjustments) {
            orderAdjustmentsValue = orderAdjustmentsValue.add(orderAdjustment.getValue());
        }
        return orderAdjustmentsValue;
    }

    public Money getTotalAdjustmentsValue() {
        Money totalAdjustmentsValue = getItemAdjustmentsValue();
        totalAdjustmentsValue = totalAdjustmentsValue.add(getOrderAdjustmentsValue());
        return totalAdjustmentsValue;
    }

    public boolean isNotCombinableOfferApplied() {
		return notCombinableOfferApplied;
	}

	public boolean isHasOrderAdjustments() {
		return hasOrderAdjustments;
	}
	
	public boolean updatePrices() {
        boolean updated = false;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.updatePrices()) {
                updated = true;
            }
        }
        return updated;
    }

	public boolean isTotalitarianOfferApplied() {
		return totalitarianOfferApplied;
	}

	public void setTotalitarianOfferApplied(boolean totalitarianOfferApplied) {
		this.totalitarianOfferApplied = totalitarianOfferApplied;
	}

	public boolean isNotCombinableOfferAppliedAtAnyLevel() {
		return notCombinableOfferAppliedAtAnyLevel;
	}

	public void setNotCombinableOfferAppliedAtAnyLevel(boolean notCombinableOfferAppliedAtAnyLevel) {
		this.notCombinableOfferAppliedAtAnyLevel = notCombinableOfferAppliedAtAnyLevel;
	}

	public List<OrderItem> getSplitItems() {
		return splitItems;
	}

	public void setSplitItems(List<OrderItem> splitItems) {
		this.splitItems = splitItems;
	}

	public boolean equals(Object obj) {
	   	if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
        OrderImpl other = (OrderImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (customer == null) {
            if (other.customer != null)
                return false;
        } else if (!customer.equals(other.customer))
            return false;
        Date myDateCreated = auditable != null ? auditable.getDateCreated() : null;
        Date otherDateCreated = other.auditable != null ? other.auditable.getDateCreated() : null;
        if (myDateCreated == null) {
            if (otherDateCreated != null)
                return false;
        } else if (!myDateCreated.equals(otherDateCreated))
            return false;
        return true;
    }

    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((customer == null) ? 0 : customer.hashCode());
        Date myDateCreated = auditable != null ? auditable.getDateCreated() : null;
        result = prime * result + ((myDateCreated == null) ? 0 : myDateCreated.hashCode());
        return result;
    }

}
