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

package org.broadleafcommerce.core.offer.service.processor;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.broadleafcommerce.core.offer.domain.CandidateFulfillmentGroupOffer;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferRule;
import org.broadleafcommerce.core.offer.service.discount.CandidatePromotionItems;
import org.broadleafcommerce.core.offer.service.discount.FulfillmentGroupOfferPotential;
import org.broadleafcommerce.core.offer.service.discount.domain.*;
import org.broadleafcommerce.core.offer.service.type.OfferRuleType;
import org.broadleafcommerce.common.money.Money;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 
 * @author jfischer
 *
 */
@Service("blFulfillmentGroupOfferProcessor")
public class FulfillmentGroupOfferProcessorImpl extends OrderOfferProcessorImpl implements FulfillmentGroupOfferProcessor {

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.core.offer.service.processor.FulfillmentGroupOfferProcessor#filterFulfillmentGroupLevelOffer(org.broadleafcommerce.core.order.domain.Order, java.util.List, java.util.List, org.broadleafcommerce.core.offer.domain.Offer)
	 */
	public void filterFulfillmentGroupLevelOffer(PromotableOrder order, List<PromotableCandidateFulfillmentGroupOffer> qualifiedFGOffers, Offer offer) {
		for (PromotableFulfillmentGroup fulfillmentGroup : order.getFulfillmentGroups()) {
			boolean fgLevelQualification = false;
			fgQualification: {
				//handle legacy fields in addition to the 1.5 order rule field
	            if(couldOfferApplyToOrder(offer, order, fulfillmentGroup)) {
	            	fgLevelQualification = true;
	                break fgQualification;
	            }
	            for (PromotableOrderItem discreteOrderItem : order.getDiscountableDiscreteOrderItems()) {
	            	if(couldOfferApplyToOrder(offer, order, discreteOrderItem, fulfillmentGroup)) {
	            		fgLevelQualification = true;
	            		break fgQualification;
	                }
	            }
	    	}
			if (fgLevelQualification) {
				fgLevelQualification = false;
				//handle 1.5 FG field
	            if(couldOfferApplyToFulfillmentGroup(offer, fulfillmentGroup)) {
	            	fgLevelQualification = true;
	            }
			}
			//Item Qualification - new for 1.5!
			if (fgLevelQualification) {
				CandidatePromotionItems candidates = couldOfferApplyToOrderItems(offer, fulfillmentGroup.getDiscountableDiscreteOrderItems());
				if (candidates.isMatchedQualifier()) {
					PromotableCandidateFulfillmentGroupOffer candidateOffer = createCandidateFulfillmentGroupOffer(offer, qualifiedFGOffers, fulfillmentGroup);
					candidateOffer.getCandidateQualifiersMap().putAll(candidates.getCandidateQualifiersMap());
				}
			}
		}
	}
	
	public void calculateFulfillmentGroupTotal(PromotableOrder order) {
		Money totalShipping = new Money(0D);
		for (PromotableFulfillmentGroup fulfillmentGroupMember : order.getFulfillmentGroups()) {
			PromotableFulfillmentGroup fulfillmentGroup = (PromotableFulfillmentGroup) fulfillmentGroupMember;
			if (fulfillmentGroup.getAdjustmentPrice() != null) {
	            fulfillmentGroup.setShippingPrice(fulfillmentGroup.getAdjustmentPrice());
	        } else if (fulfillmentGroup.getSaleShippingPrice() != null) {
	            fulfillmentGroup.setShippingPrice(fulfillmentGroup.getSaleShippingPrice());
	        } else {
	            fulfillmentGroup.setShippingPrice(fulfillmentGroup.getRetailShippingPrice());
	        }
			totalShipping = totalShipping.add(fulfillmentGroup.getShippingPrice());
		}
        order.setTotalShipping(totalShipping);
	}
	
	protected boolean couldOfferApplyToFulfillmentGroup(Offer offer, PromotableFulfillmentGroup fulfillmentGroup) {
        boolean appliesToItem = false;
        OfferRule rule = offer.getOfferMatchRules().get(OfferRuleType.FULFILLMENT_GROUP.getType());
        if (rule != null && rule.getMatchRule() != null) {
            HashMap<String, Object> vars = new HashMap<String, Object>();
            vars.put("fulfillmentGroup", fulfillmentGroup.getDelegate());
            Boolean expressionOutcome = executeExpression(rule.getMatchRule(), vars);
            if (expressionOutcome != null && expressionOutcome) {
                appliesToItem = true;
            }
        } else {
            appliesToItem = true;
        }

        return appliesToItem;
    }
	
	protected PromotableCandidateFulfillmentGroupOffer createCandidateFulfillmentGroupOffer(Offer offer, List<PromotableCandidateFulfillmentGroupOffer> qualifiedFGOffers, PromotableFulfillmentGroup fulfillmentGroup) {
		CandidateFulfillmentGroupOffer candidateOffer = offerDao.createCandidateFulfillmentGroupOffer();
		candidateOffer.setFulfillmentGroup(fulfillmentGroup.getDelegate());
		candidateOffer.setOffer(offer);
		PromotableCandidateFulfillmentGroupOffer promotableCandidateFulfillmentGroupOffer = promotableItemFactory.createPromotableCandidateFulfillmentGroupOffer(candidateOffer, fulfillmentGroup);
		fulfillmentGroup.addCandidateFulfillmentGroupOffer(promotableCandidateFulfillmentGroupOffer);
		qualifiedFGOffers.add(promotableCandidateFulfillmentGroupOffer);
		
		return promotableCandidateFulfillmentGroupOffer;
	}
	
	/**
     * Private method that takes a list of sorted CandidateOrderOffers and determines if each offer can be
     * applied based on the restrictions (stackable and/or combinable) on that offer.  OrderAdjustments
     * are create on the Order for each applied CandidateOrderOffer.  An offer with stackable equals false
     * cannot be applied to an Order that already contains an OrderAdjustment.  An offer with combinable
     * equals false cannot be applied to the Order if the Order already contains an OrderAdjustment.
     *
     * @param qualifiedFGOffers a sorted list of CandidateOrderOffer
     * @param order the Order to apply the CandidateOrderOffers
     * @return true if order offer applied; otherwise false
     */
    @SuppressWarnings("unchecked")
	public boolean applyAllFulfillmentGroupOffers(List<PromotableCandidateFulfillmentGroupOffer> qualifiedFGOffers, PromotableOrder order) {
    	Map<FulfillmentGroupOfferPotential, List<PromotableCandidateFulfillmentGroupOffer>> offerMap = new HashMap<FulfillmentGroupOfferPotential, List<PromotableCandidateFulfillmentGroupOffer>>();
    	for (PromotableCandidateFulfillmentGroupOffer candidate : qualifiedFGOffers) {
    		FulfillmentGroupOfferPotential potential = new FulfillmentGroupOfferPotential();
    		potential.setOffer(candidate.getOffer());
    		if (offerMap.get(potential) == null) {
    			offerMap.put(potential, new ArrayList<PromotableCandidateFulfillmentGroupOffer>());
    		}
    		offerMap.get(potential).add(candidate);
    	}
    	List<FulfillmentGroupOfferPotential> potentials = new ArrayList<FulfillmentGroupOfferPotential>();
		for (FulfillmentGroupOfferPotential potential : offerMap.keySet()) {
            List<PromotableCandidateFulfillmentGroupOffer> fgOffers = offerMap.get(potential);
            Collections.sort(fgOffers, new ReverseComparator(new BeanComparator("discountedAmount", new NullComparator())));
		    Collections.sort(fgOffers, new BeanComparator("priority", new NullComparator()));

            if (potential.getOffer().getMaxUses() > 0 && fgOffers.size() > potential.getOffer().getMaxUses()) {
                for (int j=potential.getOffer().getMaxUses();j<fgOffers.size();j++) {
                    fgOffers.remove(j);
                }
            }
			for (PromotableCandidateFulfillmentGroupOffer candidate : fgOffers) {
				potential.setTotalSavings(potential.getTotalSavings().add(candidate.getFulfillmentGroup().getPriceBeforeAdjustments(candidate.getOffer().getApplyDiscountToSalePrice()).subtract(candidate.getDiscountedPrice())));
				potential.setPriority(candidate.getOffer().getPriority());
			}

			potentials.add(potential);
		}
		
		// Sort fg potentials by priority and discount
		Collections.sort(potentials, new BeanComparator("totalSavings", Collections.reverseOrder()));
	    Collections.sort(potentials, new BeanComparator("priority"));
	    potentials = removeTrailingNotCombinableFulfillmentGroupOffers(potentials);
	    
    	boolean fgOfferApplied = false;
    	for (FulfillmentGroupOfferPotential potential : potentials) {
    		Offer offer = potential.getOffer();
    		if (offer.getTreatAsNewFormat() == null || !offer.getTreatAsNewFormat()) {
    			if ((offer.isStackable()) || !fgOfferApplied) {
    				boolean alreadyContainsNotCombinableOfferAtAnyLevel = order.isNotCombinableOfferAppliedAtAnyLevel();
    				List<PromotableCandidateFulfillmentGroupOffer> candidates = offerMap.get(potential);
    				for (PromotableCandidateFulfillmentGroupOffer candidate : candidates) {
    					applyFulfillmentGroupOffer(candidate);
    					fgOfferApplied = true;
    				}
    				if (!offer.isCombinableWithOtherOffers() || alreadyContainsNotCombinableOfferAtAnyLevel) {
    					fgOfferApplied = compareAndAdjustFulfillmentGroupOffers(order, fgOfferApplied);
    					if (fgOfferApplied) {
    						break;
    					}
    				}
    			}
    		} else {
    			if (!order.containsNotStackableFulfillmentGroupOffer() || !fgOfferApplied) {
    				boolean alreadyContainsTotalitarianOffer = order.isTotalitarianOfferApplied();
    				List<PromotableCandidateFulfillmentGroupOffer> candidates = offerMap.get(potential);
    				for (PromotableCandidateFulfillmentGroupOffer candidate : candidates) {
    					applyFulfillmentGroupOffer(candidate);
    					fgOfferApplied = true;
    				}
                	if (
                		(offer.isTotalitarianOffer() != null && offer.isTotalitarianOffer()) ||
                		alreadyContainsTotalitarianOffer
                	) {
                		fgOfferApplied = compareAndAdjustFulfillmentGroupOffers(order, fgOfferApplied);
                		if (fgOfferApplied) {
                    		break;
                    	}
                	} else if (!offer.isCombinableWithOtherOffers()) {
                		break;
                	}
        		}
    		}
    	}
        
        return fgOfferApplied;
    }

	protected boolean compareAndAdjustFulfillmentGroupOffers(PromotableOrder order, boolean fgOfferApplied) {
		Money regularOrderDiscountShippingTotal = new Money(0D);
		regularOrderDiscountShippingTotal = regularOrderDiscountShippingTotal.add(order.calculateOrderItemsPriceWithoutAdjustments());
		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
			regularOrderDiscountShippingTotal = regularOrderDiscountShippingTotal.add(fg.getAdjustmentPrice());
		}
		
		Money discountOrderRegularShippingTotal = new Money(0D);
		discountOrderRegularShippingTotal = discountOrderRegularShippingTotal.add(order.getSubTotal());
		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
			discountOrderRegularShippingTotal = discountOrderRegularShippingTotal.add(fg.getPriceBeforeAdjustments(true));
		}
		
		if (discountOrderRegularShippingTotal.lessThan(regularOrderDiscountShippingTotal)) {
			// order/item offer is better; remove totalitarian fulfillment group offer and process other fg offers
			order.removeAllFulfillmentAdjustments();
		    fgOfferApplied = false;
		} else {
			// totalitarian fg offer is better; remove all order/item offers
			order.removeAllOrderAdjustments();
			order.removeAllItemAdjustments();
			gatherCart(order);
			initializeSplitItems(order, order.getDiscountableDiscreteOrderItems());
		}
		return fgOfferApplied;
	}
	
	protected void applyFulfillmentGroupOffer(PromotableCandidateFulfillmentGroupOffer fulfillmentGroupOffer) {
        FulfillmentGroupAdjustment fulfillmentGroupAdjustment = offerDao.createFulfillmentGroupAdjustment();
        fulfillmentGroupAdjustment.init(((PromotableFulfillmentGroup) fulfillmentGroupOffer.getFulfillmentGroup()).getDelegate(), fulfillmentGroupOffer.getOffer(), fulfillmentGroupOffer.getOffer().getName());
        PromotableFulfillmentGroupAdjustment promotableFulfillmentGroupAdjustment = promotableItemFactory.createPromotableFulfillmentGroupAdjustment(fulfillmentGroupAdjustment, (PromotableFulfillmentGroup) fulfillmentGroupOffer.getFulfillmentGroup());
        //add to adjustment
        fulfillmentGroupOffer.getFulfillmentGroup().addFulfillmentGroupAdjustment(promotableFulfillmentGroupAdjustment);
    }
	
	public List<FulfillmentGroupOfferPotential> removeTrailingNotCombinableFulfillmentGroupOffers(List<FulfillmentGroupOfferPotential> candidateOffers) {
        List<FulfillmentGroupOfferPotential> remainingCandidateOffers = new ArrayList<FulfillmentGroupOfferPotential>();
        int offerCount = 0;
        for (FulfillmentGroupOfferPotential candidateOffer : candidateOffers) {
            if (offerCount == 0) {
                remainingCandidateOffers.add(candidateOffer);
            } else {
            	boolean treatAsNewFormat = false;
            	if (candidateOffer.getOffer().getTreatAsNewFormat() != null && candidateOffer.getOffer().getTreatAsNewFormat()) {
            		treatAsNewFormat = true;
            	}
            	if ((!treatAsNewFormat && candidateOffer.getOffer().isCombinableWithOtherOffers()) || (treatAsNewFormat && (candidateOffer.getOffer().isTotalitarianOffer() == null || !candidateOffer.getOffer().isTotalitarianOffer()))) {
                    remainingCandidateOffers.add(candidateOffer);
                }
            }
            offerCount++;
        }
        return remainingCandidateOffers;
    }
}
