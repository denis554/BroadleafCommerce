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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.broadleafcommerce.common.money.BankersRounding;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.offer.domain.CandidateFulfillmentGroupOffer;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferRule;
import org.broadleafcommerce.core.offer.service.discount.CandidatePromotionItems;
import org.broadleafcommerce.core.offer.service.discount.FulfillmentGroupOfferPotential;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableCandidateFulfillmentGroupOffer;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableFulfillmentGroup;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableFulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrder;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderItem;
import org.broadleafcommerce.core.offer.service.type.OfferRuleType;
import org.springframework.stereotype.Service;

/**
 * 
 * @author jfischer
 *
 */
@Service("blFulfillmentGroupOfferProcessor")
public class FulfillmentGroupOfferProcessorImpl extends OrderOfferProcessorImpl implements FulfillmentGroupOfferProcessor {

	@Override
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
	
	@Override
    public void calculateFulfillmentGroupTotal(PromotableOrder order) {
		Money totalShipping = org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(0D,order.getDelegate().getCurrency());
		for (PromotableFulfillmentGroup fulfillmentGroupMember : order.getFulfillmentGroups()) {
			PromotableFulfillmentGroup fulfillmentGroup = fulfillmentGroupMember;
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
	
    @Override
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
			        if(potential.getTotalSavings().getAmount()==BankersRounding.zeroAmount()) 
			        {
			            potential.setTotalSavings(new Money(candidate.getFulfillmentGroup().getDelegate().getOrder().getCurrency().getCurrencyCode()));
			        }
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
		Money regularOrderDiscountShippingTotal = org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(0D,order.getDelegate().getCurrency());
		regularOrderDiscountShippingTotal = regularOrderDiscountShippingTotal.add(order.calculateOrderItemsPriceWithoutAdjustments());
		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
			regularOrderDiscountShippingTotal = regularOrderDiscountShippingTotal.add(fg.getAdjustmentPrice());
		}
		
		Money discountOrderRegularShippingTotal = org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(0D,order.getDelegate().getCurrency());
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
			initializeSplitItems(order);
		}
		return fgOfferApplied;
	}
	
	protected void applyFulfillmentGroupOffer(PromotableCandidateFulfillmentGroupOffer fulfillmentGroupOffer) {
        FulfillmentGroupAdjustment fulfillmentGroupAdjustment = offerDao.createFulfillmentGroupAdjustment();
        fulfillmentGroupAdjustment.init(fulfillmentGroupOffer.getFulfillmentGroup().getDelegate(), fulfillmentGroupOffer.getOffer(), fulfillmentGroupOffer.getOffer().getName());
        PromotableFulfillmentGroupAdjustment promotableFulfillmentGroupAdjustment = promotableItemFactory.createPromotableFulfillmentGroupAdjustment(fulfillmentGroupAdjustment, fulfillmentGroupOffer.getFulfillmentGroup());
        //add to adjustment
        fulfillmentGroupOffer.getFulfillmentGroup().addFulfillmentGroupAdjustment(promotableFulfillmentGroupAdjustment);
    }
	
	@Override
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
