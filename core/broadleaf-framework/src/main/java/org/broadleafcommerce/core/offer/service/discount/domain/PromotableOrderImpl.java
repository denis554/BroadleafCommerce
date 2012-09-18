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

package org.broadleafcommerce.core.offer.service.discount.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.broadleafcommerce.common.money.BankersRounding;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.offer.domain.CandidateOrderOffer;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustment;
import org.broadleafcommerce.core.offer.service.discount.OrderItemPriceComparator;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.DynamicPriceDiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.GiftWrapOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.manipulation.BundleOrderItemSplitContainer;
import org.broadleafcommerce.core.order.service.manipulation.OrderItemSplitContainer;
import org.broadleafcommerce.core.order.service.manipulation.OrderItemVisitor;
import org.broadleafcommerce.core.order.service.manipulation.OrderItemVisitorAdapter;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.core.domain.Customer;

public class PromotableOrderImpl implements PromotableOrder {

	private static final long serialVersionUID = 1L;
	
	protected boolean totalitarianOfferApplied = false;
    protected boolean notCombinableOfferAppliedAtAnyLevel = false;
    protected boolean notCombinableOfferApplied = false;    
    protected boolean hasOrderAdjustments = false;
    protected List<OrderItemSplitContainer> splitItems = new ArrayList<OrderItemSplitContainer>();
    protected List<BundleOrderItemSplitContainer> bundleSplitItems = new ArrayList<BundleOrderItemSplitContainer>();
    protected BigDecimal adjustmentPrice;  // retailPrice with order adjustments (no item adjustments)
    protected Order delegate;
    protected List<PromotableFulfillmentGroup> fulfillmentGroups;
    protected List<PromotableOrderItem> discreteOrderItems;
    protected List<PromotableOrderItem> discountableDiscreteOrderItems;
    protected boolean currentSortParam = false;
    protected PromotableItemFactory itemFactory;
    
    public PromotableOrderImpl(Order order, PromotableItemFactory itemFactory) {
    	this.delegate = order;
    	this.itemFactory = itemFactory;
    }
    
    public void reset() {
    	delegate = null;
    	resetFulfillmentGroups();
    	resetDiscreteOrderItems();
    }
    
    @Override
    public void resetFulfillmentGroups() {
    	for (PromotableFulfillmentGroup fulfillmentGroup : fulfillmentGroups) {
    		fulfillmentGroup.reset();
    	}
    	fulfillmentGroups = null;
    }
    
    @Override
    public void resetDiscreteOrderItems() {
        if (discreteOrderItems != null) {
            for (PromotableOrderItem orderItem : discreteOrderItems) {
                orderItem.reset();
            }
            discreteOrderItems = null;
        }
        if (discountableDiscreteOrderItems != null) {
            for (PromotableOrderItem orderItem : discountableDiscreteOrderItems) {
                orderItem.reset();
            }
            discountableDiscreteOrderItems = null;
        }
    }
    
    @Override
    public void resetTotalitarianOfferApplied() {
    	totalitarianOfferApplied = false;
    	notCombinableOfferAppliedAtAnyLevel = false;
    	for (OrderAdjustment adjustment : delegate.getOrderAdjustments()) {
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
		    	for (OrderItem orderItem : delegate.getOrderItems()) {
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
		    	for (FulfillmentGroup fg : delegate.getFulfillmentGroups()) {
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
    
    @Override
    public void addOrderAdjustments(PromotableOrderAdjustment orderAdjustment) {
        if (delegate.getOrderAdjustments().size() == 0) {
            adjustmentPrice = delegate.getSubTotal().getAmount();
        }
        adjustmentPrice = adjustmentPrice.subtract(orderAdjustment.getValue().getAmount());
        delegate.getOrderAdjustments().add(orderAdjustment.getDelegate());
        if (!orderAdjustment.getDelegate().getOffer().isCombinableWithOtherOffers()) {
        	notCombinableOfferApplied = true;
        }
        resetTotalitarianOfferApplied();
        hasOrderAdjustments = true;
    }
    
    @Override
    public void removeAllAdjustments() {
        removeAllItemAdjustments();
        removeAllFulfillmentAdjustments();
        removeAllOrderAdjustments();
    }

    @Override
    public void removeAllOrderAdjustments() {
        if (delegate.getOrderAdjustments() != null) {
            for (OrderAdjustment adjustment : delegate.getOrderAdjustments()) {
                adjustment.setOrder(null);
            }
            delegate.getOrderAdjustments().clear();
        }
        adjustmentPrice = null;
    	notCombinableOfferApplied = false;
        hasOrderAdjustments = false;
        resetTotalitarianOfferApplied();
   }

    @Override
    public void removeAllItemAdjustments() {
        for (OrderItem orderItem : getDelegate().getOrderItems()) {
            orderItem.removeAllAdjustments();
            adjustmentPrice = null;
            resetTotalitarianOfferApplied();
            if (orderItem instanceof BundleOrderItem) {
                for (DiscreteOrderItem discreteOrderItem : ((BundleOrderItem) orderItem).getDiscreteOrderItems()) {
                    discreteOrderItem.setPrice(null);
                    discreteOrderItem.assignFinalPrice();
                }
            }
            orderItem.setPrice(null);
            orderItem.assignFinalPrice();
        }
        splitItems.clear();
    }

    @Override
    public void removeAllFulfillmentAdjustments() {
        for (PromotableFulfillmentGroup fulfillmentGroup : getFulfillmentGroups()) {
            fulfillmentGroup.removeAllAdjustments();
        }
    }
    
    @Override
    public Money getAdjustmentPrice() {
        return adjustmentPrice == null ? null : new Money(adjustmentPrice, delegate.getSubTotal().getCurrency(), adjustmentPrice.scale()==0? BankersRounding.DEFAULT_SCALE:adjustmentPrice.scale());
    }

    @Override
    public void setAdjustmentPrice(Money adjustmentPrice) {
        this.adjustmentPrice = Money.toAmount(adjustmentPrice);
    }
    
    @Override
    public boolean isNotCombinableOfferApplied() {
		return notCombinableOfferApplied;
	}

	@Override
    public boolean isHasOrderAdjustments() {
		return hasOrderAdjustments;
	}
	
	@Override
    public boolean isTotalitarianOfferApplied() {
		return totalitarianOfferApplied;
	}

	@Override
    public void setTotalitarianOfferApplied(boolean totalitarianOfferApplied) {
		this.totalitarianOfferApplied = totalitarianOfferApplied;
	}

	@Override
    public boolean isNotCombinableOfferAppliedAtAnyLevel() {
		return notCombinableOfferAppliedAtAnyLevel;
	}

	@Override
    public void setNotCombinableOfferAppliedAtAnyLevel(boolean notCombinableOfferAppliedAtAnyLevel) {
		this.notCombinableOfferAppliedAtAnyLevel = notCombinableOfferAppliedAtAnyLevel;
	}

	@Override
    public List<OrderItemSplitContainer> getSplitItems() {
		return splitItems;
	}

	@Override
    public void setSplitItems(List<OrderItemSplitContainer> splitItems) {
		this.splitItems = splitItems;
	}
	
	@Override
    public List<PromotableOrderItem> searchSplitItems(PromotableOrderItem key) {
		for (OrderItemSplitContainer container : splitItems) {
			if (container.getKey().equals(key.getDelegate())) {
				return container.getSplitItems();
			}
		}
		return null;
	}

    @Override
    public List<BundleOrderItem> searchBundleSplitItems(BundleOrderItem key) {
        for (BundleOrderItemSplitContainer container : bundleSplitItems) {
            if (container.getKey().equals(key)) {
                return container.getSplitItems();
            }
        }
        return null;
    }
	
	@Override
    public void removeAllCandidateOffers() {
    	removeAllCandidateOrderOffers();
        for (OrderItem orderItem : getDelegate().getOrderItems()) {
            orderItem.removeAllCandidateItemOffers();
        }

        removeAllCandidateFulfillmentGroupOffers();
    }
    
    @Override
    public void removeAllCandidateFulfillmentGroupOffers() {
    	if (getFulfillmentGroups() != null) {
            for (PromotableFulfillmentGroup fg : getFulfillmentGroups()) {
                fg.removeAllCandidateOffers();
            }
        }
    }

    @Override
    public void removeAllCandidateOrderOffers() {
        if (delegate.getCandidateOrderOffers() != null) {
            for (CandidateOrderOffer candidate : delegate.getCandidateOrderOffers()) {
                candidate.setOrder(null);
            }
            delegate.getCandidateOrderOffers().clear();
        }
    }
    
    @Override
    public boolean containsNotStackableFulfillmentGroupOffer() {
        boolean isContainsNotStackableFulfillmentGroupOffer = false;
        for (FulfillmentGroup fg : delegate.getFulfillmentGroups()) {
        	for (FulfillmentGroupAdjustment fgAdjustment : fg.getFulfillmentGroupAdjustments()) {
        		if (!fgAdjustment.getOffer().isStackable()) {
        			isContainsNotStackableFulfillmentGroupOffer = true;
        			break;
        		}
        	}
        }
        return isContainsNotStackableFulfillmentGroupOffer;
    }
    
    /*
     * Checks the order adjustment to see if it is not stackable
     */
    @Override
    public boolean containsNotStackableOrderOffer() {
        boolean isContainsNotStackableOrderOffer = false;
        for (OrderAdjustment orderAdjustment: delegate.getOrderAdjustments()) {
            if (!orderAdjustment.getOffer().isStackable()) {
                isContainsNotStackableOrderOffer = true;
                break;
            }
        }
        return isContainsNotStackableOrderOffer;
    }

    @Override
    public void removeAllAddedOfferCodes() {
        if (delegate.getAddedOfferCodes() != null) {
            delegate.getAddedOfferCodes().clear();
        }
    }
    
    @Override
    public void addCandidateOrderOffer(PromotableCandidateOrderOffer candidateOrderOffer) {
        delegate.getCandidateOrderOffers().add(candidateOrderOffer.getDelegate());
    }
    
    @Override
    public Money calculateOrderItemsCurrentPrice() {
        Money calculatedSubTotal = org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(getDelegate().getCurrency());
        for (PromotableOrderItem orderItem : getDiscountableDiscreteOrderItems()) {
            Money currentPrice = orderItem.getCurrentPrice();
            calculatedSubTotal = calculatedSubTotal.add(currentPrice.multiply(orderItem.getQuantity()));
        }
        return calculatedSubTotal;
    }
    
    @Override
    public Money calculateOrderItemsPriceWithoutAdjustments() {
        Money calculatedSubTotal = org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(getDelegate().getCurrency());
        for (OrderItem orderItem : delegate.getOrderItems()) {
            Money price = orderItem.getPriceBeforeAdjustments(true);
            calculatedSubTotal = calculatedSubTotal.add(price.multiply(orderItem.getQuantity()));
        }
        return calculatedSubTotal;
    }
    
    @Override
    public List<PromotableOrderItem> getAllSplitItems() {
    	List<PromotableOrderItem> response = new ArrayList<PromotableOrderItem>();
    	for (OrderItemSplitContainer container : getSplitItems()) {
    		response.addAll(container.getSplitItems());
    	}
    	
    	return response;
    }
    
    @Override
    public Money getSubTotal() {
		return delegate.getSubTotal();
	}
    
    @Override
    public List<PromotableFulfillmentGroup> getFulfillmentGroups() {
		if (fulfillmentGroups == null) {
			fulfillmentGroups = new ArrayList<PromotableFulfillmentGroup>();
			for (FulfillmentGroup fulfillmentGroup : delegate.getFulfillmentGroups()) {
				fulfillmentGroups.add(itemFactory.createPromotableFulfillmentGroup(fulfillmentGroup, this));
			}
		}
		return Collections.unmodifiableList(fulfillmentGroups);
	}
    
    @Override
    public void setTotalShipping(Money totalShipping) {
		delegate.setTotalShipping(totalShipping);
	}
    
    @Override
    public Money calculateOrderItemsFinalPrice(boolean includeNonTaxableItems) {
		return delegate.calculateOrderItemsFinalPrice(includeNonTaxableItems);
	}
    
    @Override
    public void setSubTotal(Money subTotal) {
		delegate.setSubTotal(subTotal);
	}
    
    @Override
    public void assignOrderItemsFinalPrice() {
		for (PromotableOrderItem orderItem : getDiscountableDiscreteOrderItems()) {
            orderItem.assignFinalPrice();
        }
        for (OrderItem orderItem : getDelegate().getOrderItems()) {
            if (orderItem instanceof BundleOrderItem) {
                orderItem.assignFinalPrice();
            }
        }
	}
    
    @Override
    public Customer getCustomer() {
		return delegate.getCustomer();
	}
    
    @Override
    public List<PromotableOrderItem> getDiscreteOrderItems() {
		if (discreteOrderItems == null) {
			discreteOrderItems = new ArrayList<PromotableOrderItem>();
			OrderItemVisitor visitor = new OrderItemVisitorAdapter() {

				@Override
				public void visit(BundleOrderItem bundleOrderItem) throws PricingException {
	                for (DiscreteOrderItem discreteOrderItem : bundleOrderItem.getDiscreteOrderItems()) {
	                    addDiscreteItem(discreteOrderItem);
	                }
				}

				@Override
				public void visit(DiscreteOrderItem discreteOrderItem) throws PricingException {
					addDiscreteItem(discreteOrderItem);
				}

				@Override
				public void visit(DynamicPriceDiscreteOrderItem dynamicPriceDiscreteOrderItem) throws PricingException {
					addDiscreteItem(dynamicPriceDiscreteOrderItem);
				}

				@Override
				public void visit(GiftWrapOrderItem giftWrapOrderItem) throws PricingException {
					addDiscreteItem(giftWrapOrderItem);
				}
				
				private void addDiscreteItem(DiscreteOrderItem discreteOrderItem) {
					PromotableOrderItem item = itemFactory.createPromotableOrderItem(discreteOrderItem, PromotableOrderImpl.this);
					item.computeAdjustmentPrice();
	                discreteOrderItems.add(item);
				}
				
			};
            //filter out the original bundle order items and replace with the split bundles
            List<OrderItem> basicOrderItems = new ArrayList<OrderItem>();
            basicOrderItems.addAll(delegate.getOrderItems());
            Iterator<OrderItem> itr = basicOrderItems.iterator();
            while (itr.hasNext()) {
                OrderItem temp = itr.next();
                if (temp instanceof BundleOrderItem) {
                    itr.remove();
                }
            }
            for (BundleOrderItemSplitContainer container : bundleSplitItems) {
                basicOrderItems.addAll(container.getSplitItems());
            }
			try {
				for (OrderItem temp : basicOrderItems) {
					temp.accept(visitor);
				}
			} catch (PricingException e) {
				throw new RuntimeException(e);
			}
		}
		
		return discreteOrderItems;
    }
	
	@Override
    public List<PromotableOrderItem> getDiscountableDiscreteOrderItems() {
		return getDiscountableDiscreteOrderItems(false);
	}
	
	@Override
    public List<PromotableOrderItem> getDiscountableDiscreteOrderItems(boolean applyDiscountToSalePrice) {
		if (discountableDiscreteOrderItems == null) {
			discountableDiscreteOrderItems = new ArrayList<PromotableOrderItem>();
			for (PromotableOrderItem orderItem : getDiscreteOrderItems()) {
				if (orderItem.getSku().isDiscountable() == null || orderItem.getSku().isDiscountable()) {
					discountableDiscreteOrderItems.add(orderItem);
				}
			}
			
			OrderItemPriceComparator priceComparator = new OrderItemPriceComparator(applyDiscountToSalePrice);
			// Sort the items so that the highest priced ones are at the top
			Collections.sort(discountableDiscreteOrderItems, priceComparator);
			
			currentSortParam = applyDiscountToSalePrice;
		}
		
		if (currentSortParam != applyDiscountToSalePrice) {
			OrderItemPriceComparator priceComparator = new OrderItemPriceComparator(applyDiscountToSalePrice);
			// Sort the items so that the highest priced ones are at the top
			Collections.sort(discountableDiscreteOrderItems, priceComparator);
			
			currentSortParam = applyDiscountToSalePrice;
		}
		
		return discountableDiscreteOrderItems;
    }

	@Override
    public Order getDelegate() {
		return delegate;
	}

    @Override
    public List<BundleOrderItemSplitContainer> getBundleSplitItems() {
        return bundleSplitItems;
    }

    @Override
    public void setBundleSplitItems(List<BundleOrderItemSplitContainer> bundleSplitItems) {
        this.bundleSplitItems = bundleSplitItems;
    }
}
