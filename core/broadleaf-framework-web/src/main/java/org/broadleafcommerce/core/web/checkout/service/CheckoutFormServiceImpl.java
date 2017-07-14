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
package org.broadleafcommerce.core.web.checkout.service;

import org.apache.commons.collections4.CollectionUtils;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentOption;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.FulfillmentGroupService;
import org.broadleafcommerce.core.payment.domain.OrderPayment;
import org.broadleafcommerce.core.web.checkout.model.BillingInfoForm;
import org.broadleafcommerce.core.web.checkout.model.OrderInfoForm;
import org.broadleafcommerce.core.web.checkout.model.ShippingInfoForm;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

@Service("blCheckoutFormService")
public class CheckoutFormServiceImpl implements CheckoutFormService {

    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

    @Resource(name = "blCustomerAddressService")
    protected CustomerAddressService customerAddressService;

    @Override
    public OrderInfoForm prePopulateOrderInfoForm(OrderInfoForm orderInfoForm, Order cart) {
        orderInfoForm.setEmailAddress(cart.getEmailAddress());

        return orderInfoForm;
    }

    @Override
    public ShippingInfoForm prePopulateShippingInfoForm(ShippingInfoForm shippingInfoForm, Order cart) {
        FulfillmentGroup firstShippableFulfillmentGroup = fulfillmentGroupService.getFirstShippableFulfillmentGroup(cart);

        if (firstShippableFulfillmentGroup != null) {
            //if the cart has already has fulfillment information
            if (firstShippableFulfillmentGroup.getAddress() != null) {
                shippingInfoForm.setAddress(firstShippableFulfillmentGroup.getAddress());
            } else {
                //check for a default address for the customer
                CustomerAddress defaultAddress = customerAddressService.findDefaultCustomerAddress(CustomerState.getCustomer().getId());
                if (defaultAddress != null) {
                    shippingInfoForm.setAddress(defaultAddress.getAddress());
                    shippingInfoForm.setAddressName(defaultAddress.getAddressName());
                }
            }

            FulfillmentOption fulfillmentOption = firstShippableFulfillmentGroup.getFulfillmentOption();
            if (fulfillmentOption != null) {
                shippingInfoForm.setFulfillmentOption(fulfillmentOption);
                shippingInfoForm.setFulfillmentOptionId(fulfillmentOption.getId());
            }
        }

        return shippingInfoForm;
    }

    @Override
    public BillingInfoForm prePopulateBillingInfoForm(BillingInfoForm billingInfoForm, ShippingInfoForm shippingInfoForm, Order cart) {
        Customer customer = CustomerState.getCustomer();

        if (cart.getEmailAddress() != null) {
            billingInfoForm.setEmailAddress(cart.getEmailAddress());
        } else if (customer != null && customer.getEmailAddress() != null) {
            billingInfoForm.setEmailAddress(customer.getEmailAddress());
        }

        List<OrderPayment> orderPayments = cart.getPayments();
        for (OrderPayment payment : CollectionUtils.emptyIfNull(orderPayments)) {
            boolean isCreditCardPaymentType = PaymentType.CREDIT_CARD.equals(payment.getType());
            boolean paymentHasBillingAddress = (payment.getBillingAddress() != null);

            if (isCreditCardPaymentType && paymentHasBillingAddress) {
                billingInfoForm.setAddress(payment.getBillingAddress());
            }
        }

        boolean shippingAddressUsedForBilling = addressesContentsAreEqual(shippingInfoForm.getAddress(), billingInfoForm.getAddress());
        billingInfoForm.setUseShippingAddress(shippingAddressUsedForBilling);

        return billingInfoForm;
    }

    @Override
    public void prePopulateInfoForms(ShippingInfoForm shippingInfoForm, BillingInfoForm billingInfoForm) {
        Order cart = CartState.getCart();
        prePopulateShippingInfoForm(shippingInfoForm, cart);
        prePopulateBillingInfoForm(billingInfoForm, shippingInfoForm, cart);
    }

    @Override
    public void determineIfSavedAddressIsSelected(Model model, ShippingInfoForm shippingInfoForm, BillingInfoForm billingInfoForm) {
        Customer customer = CustomerState.getCustomer();
        boolean isSavedShippingAddress = false;
        boolean isSavedBillingAddress = false;

        for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
            if (addressesContentsAreEqual(shippingInfoForm.getAddress(), customerAddress.getAddress())) {
                isSavedShippingAddress = true;
                break;
            }
        }

        for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
            if (addressesContentsAreEqual(billingInfoForm.getAddress(), customerAddress.getAddress())) {
                isSavedBillingAddress = true;
                break;
            }
        }

        model.addAttribute("isSavedShippingAddress", isSavedShippingAddress);
        model.addAttribute("isSavedBillingAddress", isSavedBillingAddress);
    }

    protected boolean addressesContentsAreEqual(Address address1, Address address2) {
        return Objects.equals(address2.getAddressLine1(), address1.getAddressLine1()) &&
                Objects.equals(address2.getAddressLine2(), address1.getAddressLine2()) &&
                Objects.equals(address2.getCity(), address1.getCity()) &&
                Objects.equals(address2.getStateProvinceRegion(), address1.getStateProvinceRegion()) &&
                Objects.equals(address2.getPostalCode(), address1.getPostalCode()) &&
                Objects.equals(address2.getIsoCountryAlpha2(), address1.getIsoCountryAlpha2()) &&
                Objects.equals(address2.getIsoCountrySubdivision(), address1.getIsoCountrySubdivision()) &&
                Objects.equals(address2.getPhonePrimary().getPhoneNumber(), address1.getPhonePrimary().getPhoneNumber());
    }
}
