/*
 * Copyright 2012 the original author or authors.
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

package org.broadleafcommerce.core.pricing.service.fulfillment.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.vendor.service.exception.ShippingPriceException;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.FulfillmentOption;
import org.broadleafcommerce.core.order.fulfillment.domain.BandedPriceFulfillmentOption;
import org.broadleafcommerce.core.order.fulfillment.domain.FulfillmentPriceBand;
import org.broadleafcommerce.core.order.service.type.FulfillmentBandResultAmountType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Used in conjunction with {@link BandedPriceFulfillmentOption}. If 2 bands are configured equal to each other (meaning, there are
 * 2 {@link FulfillmentPriceBand}s that have the same retail price minimum, this will choose the cheaper of the 2</p>
 * <p>If the retail total does not fall within a configured price band, the total cost of fulfillment is zero</p>
 * 
 * @author Phillip Verheyden
 * @see {@link BandedPriceFulfillmentOption}, {@link FulfillmentPriceBand}
 */
public class BandedPriceFulfillmentPricingProvider implements FulfillmentPricingProvider {

    protected static final Log LOG = LogFactory.getLog(BandedPriceFulfillmentPricingProvider.class);

    @Override
    public boolean canCalculateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup, FulfillmentOption option) {
        return (option instanceof BandedPriceFulfillmentOption);
    }

    @Override
    public FulfillmentGroup calculateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup) throws ShippingPriceException {
        if (fulfillmentGroup.getFulfillmentGroupItems().size() == 0) {
            LOG.warn("fulfillment group (" + fulfillmentGroup.getId() + ") does not contain any fulfillment group items. Unable to price banded shipping");
            fulfillmentGroup.setShippingPrice(Money.ZERO);
            fulfillmentGroup.setSaleShippingPrice(Money.ZERO);
            fulfillmentGroup.setRetailShippingPrice(Money.ZERO);
            return fulfillmentGroup;
        }

        if (canCalculateCostForFulfillmentGroup(fulfillmentGroup, fulfillmentGroup.getFulfillmentOption())) {
            //In this case, the estimation logic is the same as calculation logic. Call the estimation service to get the prices.
            HashSet<FulfillmentOption> options = new HashSet<FulfillmentOption>();
            options.add(fulfillmentGroup.getFulfillmentOption());
            FulfillmentEstimationResponse response = estimateCostForFulfillmentGroup(fulfillmentGroup, options);
            fulfillmentGroup.setSaleShippingPrice(response.getFulfillmentOptionPrices().get(fulfillmentGroup.getFulfillmentOption()));
            fulfillmentGroup.setRetailShippingPrice(response.getFulfillmentOptionPrices().get(fulfillmentGroup.getFulfillmentOption()));
            fulfillmentGroup.setShippingPrice(response.getFulfillmentOptionPrices().get(fulfillmentGroup.getFulfillmentOption()));

            return fulfillmentGroup;
        }

        throw new ShippingPriceException("An unsupported FulfillmentOption was passed to the calculateCostForFulfillmentGroup method");
    }

    @Override
    public FulfillmentEstimationResponse estimateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup, Set<FulfillmentOption> options) throws ShippingPriceException {

        //Set up the response object
        FulfillmentEstimationResponse res = new FulfillmentEstimationResponse();
        HashMap<BandedPriceFulfillmentOption, Money> shippingPrices = new HashMap<BandedPriceFulfillmentOption, Money>();
        res.setFulfillmentOptionPrices(shippingPrices);

        for (FulfillmentOption option : options) {
            if (canCalculateCostForFulfillmentGroup(fulfillmentGroup, option)) {
                BandedPriceFulfillmentOption bandedPriceFulfillmentOption = (BandedPriceFulfillmentOption)option;
                List<FulfillmentPriceBand> bands = bandedPriceFulfillmentOption.getBands();
                if (bands == null || bands.isEmpty()) {
                    //Something is misconfigured. There are no bands associated with this fulfillment option
                    throw new IllegalStateException("There were no Fulfillment Price Bands configred for a BandedPriceFulfillmentOption with ID: "
                            + bandedPriceFulfillmentOption.getId());
                }

                //Calculate the amount that the band will be applied to
                BigDecimal retailTotal = BigDecimal.ZERO;
                BigDecimal flatTotal = BigDecimal.ZERO;
                for (FulfillmentGroupItem fulfillmentGroupItem : fulfillmentGroup.getFulfillmentGroupItems()) {
                    
                    //If this item has a Sku associated with it which also has a flat rate for this fulfillment option, don't add it to the retail
                    //total but instead tack it onto the final rate
                    boolean addToRetailTotal = true;
                    if (option.getUseFlatRates()) {
                        Sku sku = null;
                        if (fulfillmentGroupItem.getOrderItem() instanceof DiscreteOrderItem) {
                            sku = ((DiscreteOrderItem)fulfillmentGroupItem.getOrderItem()).getSku();
                        } else if (fulfillmentGroupItem.getOrderItem() instanceof BundleOrderItem) {
                            sku = ((BundleOrderItem)fulfillmentGroupItem.getOrderItem()).getSku();
                        }
                        
                        if (sku != null) {
                            BigDecimal rate = sku.getFulfillmentFlatRates().get(option);
                            if (rate != null) {
                                addToRetailTotal = false;
                                flatTotal = flatTotal.add(rate);
                            }
                        }
                    }
                    
                    if (addToRetailTotal) {
                        BigDecimal price = (fulfillmentGroupItem.getRetailPrice() != null) ? fulfillmentGroupItem.getRetailPrice().getAmount().multiply(BigDecimal.valueOf(fulfillmentGroupItem.getQuantity())) : null;
                        if (price == null) {
                            price = fulfillmentGroupItem.getOrderItem().getRetailPrice().getAmount().multiply(BigDecimal.valueOf(fulfillmentGroupItem.getQuantity()));
                        }
                        retailTotal = retailTotal.add(price);
                    }
                }

                BigDecimal lowestFulfillmentAmount = BigDecimal.ZERO;
                BigDecimal lowestFulfillmentBandRetailMinimum = BigDecimal.ZERO;
                for (FulfillmentPriceBand band : bands) {
                    BigDecimal bandRetailPriceMinimumAmount = band.getRetailPriceMinimumAmount();
                    if (retailTotal.compareTo(bandRetailPriceMinimumAmount) >= 0) {
                        //So far, we've found a potential match
                        //Now, determine if this is a percentage or actual amount
                        FulfillmentBandResultAmountType resultAmountType = band.getResultAmountType();
                        BigDecimal bandFulfillmentPrice = null;
                        if (FulfillmentBandResultAmountType.RATE.equals(resultAmountType)) {
                            bandFulfillmentPrice = band.getResultAmount();
                        } else if (FulfillmentBandResultAmountType.PERCENTAGE.equals(resultAmountType)) {
                            //Since this is a percentage, we calculate the result amount based on retailTotal and the band percentage
                            bandFulfillmentPrice = retailTotal.multiply(band.getResultAmount());
                        } else {
                            LOG.warn("Unknown FulfillmentBandResultAmountType: " + resultAmountType.getType() + " Should be RATE or PERCENTAGE. Ignoring.");
                        }
                        
                        if (bandFulfillmentPrice != null) {
                            //If there is a duplicate price band (meaning, 2 price bands are configured with the same miniumum retail price)
                            //then the lowest fulfillment amount should only be updated if the result of the current band being looked at
                            //is cheaper
                            if (lowestFulfillmentBandRetailMinimum.equals(bandRetailPriceMinimumAmount)) {
                                if (bandFulfillmentPrice.compareTo(lowestFulfillmentAmount) <= 0) {
                                    lowestFulfillmentAmount = bandFulfillmentPrice;
                                    lowestFulfillmentBandRetailMinimum = bandRetailPriceMinimumAmount;
                                }
                            } else if (bandRetailPriceMinimumAmount.compareTo(lowestFulfillmentBandRetailMinimum) > 0) {
                                lowestFulfillmentAmount = bandFulfillmentPrice;
                                lowestFulfillmentBandRetailMinimum = bandRetailPriceMinimumAmount;
                            }
                            
                        }
                    }
                }
                
                //add the flat rate amount calculated on the Sku
                lowestFulfillmentAmount = lowestFulfillmentAmount.add(flatTotal);

                shippingPrices.put(bandedPriceFulfillmentOption, new Money(lowestFulfillmentAmount));
            }
        }

        return res;
    }

}
