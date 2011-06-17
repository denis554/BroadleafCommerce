package org.broadleafcommerce.core.offer.service.discount.domain;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.core.offer.domain.CandidateFulfillmentGroupOffer;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.order.domain.BundleOrderItemImpl;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupFee;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.PersonalMessage;
import org.broadleafcommerce.core.order.service.manipulation.DiscreteOrderItemDecorator;
import org.broadleafcommerce.core.order.service.type.FulfillmentGroupStatusType;
import org.broadleafcommerce.core.order.service.type.FulfillmentGroupType;
import org.broadleafcommerce.money.Money;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Phone;

public class PromotableFulfillmentGroupImpl implements PromotableFulfillmentGroup {

	private static final long serialVersionUID = 1L;
	
	protected Money adjustmentPrice;  // retailPrice with adjustments
	protected FulfillmentGroup delegate;
	protected PromotableOrder order;
	protected PromotableItemFactory itemFactory;
	
	public PromotableFulfillmentGroupImpl(FulfillmentGroup fulfillmentGroup, PromotableOrder order, PromotableItemFactory itemFactory) {
		this.delegate = fulfillmentGroup;
		this.order = order;
		this.itemFactory = itemFactory;
	}
	
	public void reset() {
		delegate = null;
	}
	
	public FulfillmentGroup getDelegate() {
		return delegate;
	}
	
	public List<PromotableOrderItem> getDiscountableDiscreteOrderItems() {
        List<PromotableOrderItem> discreteOrderItems = new ArrayList<PromotableOrderItem>();
        for (FulfillmentGroupItem fgItem : delegate.getFulfillmentGroupItems()) {
        	OrderItem orderItem = fgItem.getOrderItem();
            if (orderItem instanceof BundleOrderItemImpl) {
                BundleOrderItemImpl bundleOrderItem = (BundleOrderItemImpl)orderItem;
                for (DiscreteOrderItem discreteOrderItem : bundleOrderItem.getDiscreteOrderItems()) {
                    if (discreteOrderItem.getSku().isDiscountable()) {
                        discreteOrderItems.add(itemFactory.createPromotableOrderItem(discreteOrderItem, order));
                    }
                }
            } else {
                DiscreteOrderItem discreteOrderItem = (DiscreteOrderItem)orderItem;
                if (discreteOrderItem.getSku().isDiscountable()) {
                	//use the decorator patter to return the quantity for this fgItem, not the quantity on the discrete order item
                    discreteOrderItems.add(itemFactory.createPromotableOrderItem(new DiscreteOrderItemDecorator(discreteOrderItem, fgItem.getQuantity()), order));
                }
            }
        }
        return discreteOrderItems;
    }
	
	/*
     * Adds the adjustment to the order item's adjustment list and discounts the order item's adjustment
     * price by the value of the adjustment.
     */
    public void addFulfillmentGroupAdjustment(PromotableFulfillmentGroupAdjustment fulfillmentGroupAdjustment) {
        if (delegate.getFulfillmentGroupAdjustments().size() == 0) {
            adjustmentPrice = delegate.getRetailShippingPrice();
        }
        adjustmentPrice = adjustmentPrice.subtract(fulfillmentGroupAdjustment.getValue());
        delegate.getFulfillmentGroupAdjustments().add(fulfillmentGroupAdjustment.getDelegate());
        order.resetTotalitarianOfferApplied();
    }

    public void removeAllAdjustments() {
        delegate.removeAllAdjustments();
        order.resetTotalitarianOfferApplied();
        adjustmentPrice = null;
    }
    
    public Money getPriceBeforeAdjustments(boolean allowSalesPrice) {
        Money currentPrice = null;
        if (delegate.getSaleShippingPrice() != null && allowSalesPrice) {
            currentPrice = delegate.getSaleShippingPrice();
        } else {
            currentPrice = delegate.getRetailShippingPrice();
        }
        return currentPrice;
    }
    
    public Money getAdjustmentPrice() {
        return adjustmentPrice == null ? null : adjustmentPrice;
    }

    public void setAdjustmentPrice(Money adjustmentPrice) {
        this.adjustmentPrice = adjustmentPrice;
    }
    
    public Money getRetailShippingPrice() {
		return delegate.getRetailShippingPrice();
	}

	public Money getSaleShippingPrice() {
		return delegate.getSaleShippingPrice();
	}
	
	public void removeAllCandidateOffers() {
		delegate.removeAllCandidateOffers();
	}
	
	public Money getShippingPrice() {
		return delegate.getShippingPrice();
	}
	
	public void setShippingPrice(Money shippingPrice) {
		delegate.setShippingPrice(shippingPrice);
	}
	
	public void addCandidateFulfillmentGroupOffer(PromotableCandidateFulfillmentGroupOffer candidateOffer) {
		delegate.addCandidateFulfillmentGroupOffer(candidateOffer.getDelegate());
	}
    
    // FulfillmentGroup methods

	public Long getId() {
		return delegate.getId();
	}

	public void setId(Long id) {
		delegate.setId(id);
	}

	public Order getOrder() {
		return delegate.getOrder();
	}

	public void setOrder(Order order) {
		delegate.setOrder(order);
	}

	public Address getAddress() {
		return delegate.getAddress();
	}

	public void setAddress(Address address) {
		delegate.setAddress(address);
	}

	public Phone getPhone() {
		return delegate.getPhone();
	}

	public void setPhone(Phone phone) {
		delegate.setPhone(phone);
	}

	public List<FulfillmentGroupItem> getFulfillmentGroupItems() {
		return delegate.getFulfillmentGroupItems();
	}

	public void setFulfillmentGroupItems(
			List<FulfillmentGroupItem> fulfillmentGroupItems) {
		delegate.setFulfillmentGroupItems(fulfillmentGroupItems);
	}

	public void addFulfillmentGroupItem(
			FulfillmentGroupItem fulfillmentGroupItem) {
		delegate.addFulfillmentGroupItem(fulfillmentGroupItem);
	}

	public String getMethod() {
		return delegate.getMethod();
	}

	public void setMethod(String fulfillmentMethod) {
		delegate.setMethod(fulfillmentMethod);
	}

	public String getReferenceNumber() {
		return delegate.getReferenceNumber();
	}

	public void setReferenceNumber(String referenceNumber) {
		delegate.setReferenceNumber(referenceNumber);
	}

	public FulfillmentGroupType getType() {
		return delegate.getType();
	}

	public void setType(FulfillmentGroupType type) {
		delegate.setType(type);
	}

	public List<CandidateFulfillmentGroupOffer> getCandidateFulfillmentGroupOffers() {
		return delegate.getCandidateFulfillmentGroupOffers();
	}

	public void setCandidateFulfillmentGroupOffer(
			List<CandidateFulfillmentGroupOffer> candidateOffers) {
		delegate.setCandidateFulfillmentGroupOffer(candidateOffers);
	}

	public List<FulfillmentGroupAdjustment> getFulfillmentGroupAdjustments() {
		return delegate.getFulfillmentGroupAdjustments();
	}

	public void setFulfillmentGroupAdjustments(
			List<FulfillmentGroupAdjustment> fulfillmentGroupAdjustments) {
		delegate
				.setFulfillmentGroupAdjustments(fulfillmentGroupAdjustments);
	}

	public Money getCityTax() {
		return delegate.getCityTax();
	}

	public void setCityTax(Money cityTax) {
		delegate.setCityTax(cityTax);
	}

	public Money getCountyTax() {
		return delegate.getCountyTax();
	}

	public void setCountyTax(Money countyTax) {
		delegate.setCountyTax(countyTax);
	}

	public Money getStateTax() {
		return delegate.getStateTax();
	}

	public void setStateTax(Money stateTax) {
		delegate.setStateTax(stateTax);
	}

	public Money getDistrictTax() {
		return delegate.getDistrictTax();
	}

	public void setDistrictTax(Money districtTax) {
		delegate.setDistrictTax(districtTax);
	}

	public Money getCountryTax() {
		return delegate.getCountryTax();
	}

	public void setCountryTax(Money countryTax) {
		delegate.setCountryTax(countryTax);
	}

	public Money getTotalTax() {
		return delegate.getTotalTax();
	}

	public void setTotalTax(Money totalTax) {
		delegate.setTotalTax(totalTax);
	}

	public String getDeliveryInstruction() {
		return delegate.getDeliveryInstruction();
	}

	public void setDeliveryInstruction(String deliveryInstruction) {
		delegate.setDeliveryInstruction(deliveryInstruction);
	}

	public PersonalMessage getPersonalMessage() {
		return delegate.getPersonalMessage();
	}

	public void setPersonalMessage(PersonalMessage personalMessage) {
		delegate.setPersonalMessage(personalMessage);
	}

	public boolean isPrimary() {
		return delegate.isPrimary();
	}

	public void setPrimary(boolean primary) {
		delegate.setPrimary(primary);
	}

	public Money getMerchandiseTotal() {
		return delegate.getMerchandiseTotal();
	}

	public void setMerchandiseTotal(Money merchandiseTotal) {
		delegate.setMerchandiseTotal(merchandiseTotal);
	}

	public Money getTotal() {
		return delegate.getTotal();
	}

	public void setTotal(Money orderTotal) {
		delegate.setTotal(orderTotal);
	}

	public FulfillmentGroupStatusType getStatus() {
		return delegate.getStatus();
	}

	public List<FulfillmentGroupFee> getFulfillmentGroupFees() {
		return delegate.getFulfillmentGroupFees();
	}

	public void setFulfillmentGroupFees(
			List<FulfillmentGroupFee> fulfillmentGroupFees) {
		delegate.setFulfillmentGroupFees(fulfillmentGroupFees);
	}

	public void addFulfillmentGroupFee(FulfillmentGroupFee fulfillmentGroupFee) {
		delegate.addFulfillmentGroupFee(fulfillmentGroupFee);
	}

	public void removeAllFulfillmentGroupFees() {
		delegate.removeAllFulfillmentGroupFees();
	}

	public Boolean isShippingPriceTaxable() {
		return delegate.isShippingPriceTaxable();
	}

	public void setIsShippingPriceTaxable(Boolean isShippingPriceTaxable) {
		delegate.setIsShippingPriceTaxable(isShippingPriceTaxable);
	}

	public String getService() {
		return delegate.getService();
	}

	public void setService(String service) {
		delegate.setService(service);
	}

	public List<DiscreteOrderItem> getDiscreteOrderItems() {
		return delegate.getDiscreteOrderItems();
	}

	public Money getFulfillmentGroupAdjustmentsValue() {
		return delegate.getFulfillmentGroupAdjustmentsValue();
	}
	
}
