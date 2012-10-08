/*
 * Copyright 2008-2012 the original author or authors.
 *
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
 */

package org.broadleafcommerce.core.offer.service.discount.domain;

import org.broadleafcommerce.common.currency.util.BroadleafCurrencyUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.offer.domain.CandidateItemOffer;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferItemCriteria;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PromotableCandidateItemOfferImpl implements PromotableCandidateItemOffer {
	
	private static final long serialVersionUID = 1L;
	
	protected Money potentialSavings; 
    protected HashMap<OfferItemCriteria, List<PromotableOrderItem>> candidateQualifiersMap = new HashMap<OfferItemCriteria, List<PromotableOrderItem>>();
    protected List<PromotableOrderItem> candidateTargets = new ArrayList<PromotableOrderItem>();
	protected CandidateItemOffer delegate;
	protected PromotableOrderItem orderItem;
    protected int uses = 0;
	
	public PromotableCandidateItemOfferImpl(CandidateItemOffer candidateItemOffer) {
		this.delegate = candidateItemOffer;
	}
	
	@Override
    public HashMap<OfferItemCriteria, List<PromotableOrderItem>> getCandidateQualifiersMap() {
		return candidateQualifiersMap;
	}

	@Override
    public void setCandidateQualifiersMap(HashMap<OfferItemCriteria, List<PromotableOrderItem>> candidateItemsMap) {
		this.candidateQualifiersMap = candidateItemsMap;
	}
	
	@Override
    public List<PromotableOrderItem> getCandidateTargets() {
		return candidateTargets;
	}

	@Override
    public void setCandidateTargets(List<PromotableOrderItem> candidateTargets) {
		this.candidateTargets = candidateTargets;
	}

	@Override
    public Money calculateSavingsForOrderItem(PromotableOrderItem orderItem, int qtyToReceiveSavings) {
		Money savings = BroadleafCurrencyUtils.getMoney(BigDecimal.ZERO, orderItem.getDelegate().getOrder().getCurrency());
		Money salesPrice = orderItem.getPriceBeforeAdjustments(getOffer().getApplyDiscountToSalePrice());
		if (getOffer().getDiscountType().equals(OfferDiscountType.AMOUNT_OFF)) {
			//Price reduction by a fixed amount
			savings = savings.add(BroadleafCurrencyUtils.getMoney(getOffer().getValue(), orderItem.getDelegate().getOrder().getCurrency()).multiply(qtyToReceiveSavings));
		} else if (getOffer().getDiscountType().equals(OfferDiscountType.PERCENT_OFF)) {
			//Price reduction by a percent off
			BigDecimal savingsPercent = getOffer().getValue().divide(new BigDecimal(100));
			savings = savings.add(salesPrice.multiply(savingsPercent).multiply(qtyToReceiveSavings));
		} else {
			//Different price (presumably less than the normal price)
			savings = savings.add(salesPrice.multiply(qtyToReceiveSavings).subtract(BroadleafCurrencyUtils.getMoney(getOffer().getValue(), orderItem.getDelegate().getOrder().getCurrency()).multiply(qtyToReceiveSavings)));
		}
		return savings;
	}
	
	@Override
    public Money getPotentialSavings() {
		if (potentialSavings == null) {
			potentialSavings = calculatePotentialSavings();
		}
		return potentialSavings;
	}

	/**
	 * This method determines how much the customer might save using this promotion for the
	 * purpose of sorting promotions with the same priority. The assumption is that any possible
	 * target specified for BOGO style offers are of equal or lesser value. We are using
	 * a calculation based on the qualifiers here strictly for rough comparative purposes.
	 *  
	 * If two promotions have the same priority, the one with the highest potential savings
	 * will be used as the tie-breaker to determine the order to apply promotions.
	 * 
	 * This method makes a good approximation of the promotion value as determining the exact value
	 * would require all permutations of promotions to be run resulting in a costly 
	 * operation.
	 * 
	 * @return
	 */
	@Override
    public Money calculatePotentialSavings() {
		Money savings = new Money(0);
		int maxUses = calculateMaximumNumberOfUses();
		int appliedCount = 0;
		
		for (PromotableOrderItem chgItem : candidateTargets) {
			int qtyToReceiveSavings = Math.min(chgItem.getQuantity(), maxUses);
			savings = calculateSavingsForOrderItem(chgItem, qtyToReceiveSavings);

			appliedCount = appliedCount + qtyToReceiveSavings;
			if (appliedCount >= maxUses) {
				return savings;
			}
		}
		
		return savings;
	}
	
	/**
	 * Determines the maximum number of times this promotion can be used based on the
	 * ItemCriteria and promotion's maxQty setting.
	 */
	@Override
    public int calculateMaximumNumberOfUses() {		
		int maxMatchesFound = 9999; // set arbitrarily high / algorithm will adjust down

        //iterate through the target criteria and find the least amount of max uses. This will be the overall
        //max usage, since the target criteria are grouped together in "and" style.
        int numberOfUsesForThisItemCriteria = maxMatchesFound;
        for (OfferItemCriteria targetCriteria : delegate.getOffer().getTargetItemCriteria()) {
            int temp = calculateMaxUsesForItemCriteria(targetCriteria, getOffer());
            numberOfUsesForThisItemCriteria = Math.min(numberOfUsesForThisItemCriteria, temp);
        }

		maxMatchesFound = Math.min(maxMatchesFound, numberOfUsesForThisItemCriteria);
        int offerMaxUses = getOffer().getMaxUses()==0?maxMatchesFound:getOffer().getMaxUses();

		return Math.min(maxMatchesFound, offerMaxUses);
	}
	
	@Override
    public int calculateMaxUsesForItemCriteria(OfferItemCriteria itemCriteria, Offer promotion) {
		int numberOfTargets = 0;
		int numberOfUsesForThisItemCriteria = 9999;
		
		if (candidateTargets != null && itemCriteria != null) {
			for(PromotableOrderItem potentialTarget : candidateTargets) {
				numberOfTargets += potentialTarget.getQuantityAvailableToBeUsedAsTarget(promotion);
			}
			numberOfUsesForThisItemCriteria = numberOfTargets / itemCriteria.getQuantity();
		}
		
		return numberOfUsesForThisItemCriteria;
	}
	
	@Override
    public CandidateItemOffer getDelegate() {
		return delegate;
	}
	
	@Override
    public void reset() {
		delegate = null;
	}

	@Override
    public void setOrderItem(PromotableOrderItem orderItem) {
		if (orderItem != null) {
			this.orderItem = orderItem;
			delegate.setOrderItem(this.orderItem.getDelegate());
		}
	}

	@Override
    public PromotableCandidateItemOffer clone() {
		PromotableCandidateItemOfferImpl copy = new PromotableCandidateItemOfferImpl(delegate.clone());
		copy.orderItem = this.orderItem;
		return copy;
	}
	
	@Override
    public int getPriority() {
		return delegate.getPriority();
	}
	
	@Override
    public Offer getOffer() {
		return delegate.getOffer();
	}
	
	@Override
    public void setOffer(Offer offer) {
		delegate.setOffer(offer);
	}
	
	@Override
    public PromotableOrderItem getOrderItem() {
		return orderItem;
	}

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public void addUse() {
        uses++;
    }
}
