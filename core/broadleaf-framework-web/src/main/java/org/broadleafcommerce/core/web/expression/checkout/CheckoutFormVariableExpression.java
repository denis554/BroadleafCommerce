/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.web.expression.checkout;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.web.expression.BroadleafVariableExpression;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.FulfillmentGroupService;
import org.broadleafcommerce.core.web.checkout.stage.CheckoutStageType;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.core.web.order.service.CartStateService;
import org.broadleafcommerce.presentation.condition.ConditionalOnTemplating;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Chris Kittrell (ckittrell)
 */
@Component("blCheckoutFormVariableExpression")
@ConditionalOnTemplating
public class CheckoutFormVariableExpression implements BroadleafVariableExpression {

    @Resource(name = "blCartStateService")
    protected CartStateService cartStateService;

    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

    @Override
    public String getName() {
        return "checkoutForm";
    }

    public boolean isPreviousStage(String currentStage, String activeStage) {
        CheckoutStageType currentStageType = CheckoutStageType.getInstance(currentStage);
        CheckoutStageType activeStageType = CheckoutStageType.getInstance(activeStage);

        return currentStageType.compareTo(activeStageType) < 0;
    }

    public boolean isActiveStage(String currentStage, String activeStage) {
        CheckoutStageType currentStageType = CheckoutStageType.getInstance(currentStage);
        CheckoutStageType activeStageType = CheckoutStageType.getInstance(activeStage);

        return currentStageType.compareTo(activeStageType) == 0;
    }

    public boolean isLaterStage(String currentStage, String activeStage) {
        CheckoutStageType currentStageType = CheckoutStageType.getInstance(currentStage);
        CheckoutStageType activeStageType = CheckoutStageType.getInstance(activeStage);

        return currentStageType.compareTo(activeStageType) > 0;
    }

    public boolean shouldShowShippingInfoStage() {
        Order cart = CartState.getCart();

        int numShippableFulfillmentGroups = fulfillmentGroupService.calculateNumShippableFulfillmentGroups(cart);

        return numShippableFulfillmentGroups > 0;
    }

    public boolean shouldShowBillingInfoStage() {
        return !cartStateService.orderContainsThirdPartyPayment()
                && !cartStateService.orderContainsUnconfirmedCreditCard();
    }

    /**
     * Toggle the Payment Info Section based on what payments were applied to the order
     *  (e.g. Third Party Account (i.e. PayPal Express) or Gift Cards/Customer Credit)
     */
    public boolean shouldShowAllPaymentMethods() {
        Money orderTotalAfterAppliedPayments = CartState.getCart().getTotalAfterAppliedPayments();
        boolean totalCoveredByAppliedPayments = (orderTotalAfterAppliedPayments != null && orderTotalAfterAppliedPayments.isZero());

        return !cartStateService.orderContainsThirdPartyPayment()
                && !cartStateService.orderContainsUnconfirmedCreditCard()
                && !totalCoveredByAppliedPayments;
    }

}
