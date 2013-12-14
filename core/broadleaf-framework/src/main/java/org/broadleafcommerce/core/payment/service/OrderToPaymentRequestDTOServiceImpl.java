/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */

package org.broadleafcommerce.core.payment.service;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.payment.domain.OrderPayment;
import org.broadleafcommerce.core.payment.domain.PaymentTransaction;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerPhone;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blOrderToPaymentRequestDTOService")
public class OrderToPaymentRequestDTOServiceImpl implements OrderToPaymentRequestDTOService {

    /**
     * This translates an Order of PaymentType.CREDIT_CARD into a PaymentRequestDTO.
     * This method assumes that this translation will apply to a final payment.
     * That is, the transaction amount that is set, will be order.getTotalAfterAppliedPayments();
     * It assumes that all other payments (e.g. gift cards/account credit) have already
     * been applied to the order if necessary.
     *
     * @param order
     * @param paymentType
     * @return
     */
    @Override
    public PaymentRequestDTO translateOrder(Order order) {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO()
                .orderId(order.getId().toString())
                .orderCurrencyCode(order.getCurrency().getCurrencyCode());

        populateCustomerInfo(order, requestDTO);
        populateShipTo(order, requestDTO);
        populateBillTo(order, requestDTO);
        populateTotals(order, requestDTO);
        populateDefaultLineItemsAndSubtotal(order, requestDTO);

        return requestDTO;
    }

    @Override
    public PaymentRequestDTO translatePaymentTransaction(Money transactionAmount, PaymentTransaction paymentTransaction) {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO()
                .transactionTotal(transactionAmount.toString());

        //Copy Additional Fields from PaymentTransaction into the Request DTO.
        //This will contain any gateway specific information needed to perform actions on this transaction
        Map<String, Serializable> additionalFields = paymentTransaction.getAdditionalFields();
        for (String key : additionalFields.keySet()) {
            requestDTO.additionalField(key, additionalFields.get(key));
        }

        return requestDTO;
    }

    protected void populateTotals(Order order, PaymentRequestDTO requestDTO) {
        requestDTO.transactionTotal(order.getTotalAfterAppliedPayments().toString());
    }

    protected void populateCustomerInfo(Order order, PaymentRequestDTO requestDTO) {
        Customer customer = order.getCustomer();
        String phoneNumber = null;
        if (customer.getCustomerPhones() != null && !customer.getCustomerPhones().isEmpty()) {
            for (CustomerPhone phone : customer.getCustomerPhones()) {
                if (phone.getPhone().isDefault()) {
                    phoneNumber =  phone.getPhone().getPhoneNumber();
                }
            }
        }

        requestDTO.customer()
                .customerId(customer.getId().toString())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmailAddress())
                .phone(phoneNumber);

    }

    protected void populateShipTo(Order order, PaymentRequestDTO requestDTO) {
        List<FulfillmentGroup> fgs = order.getFulfillmentGroups();
        if (fgs != null && fgs.size() > 0) {
            FulfillmentGroup defaultFg = fgs.get(0);
            if (defaultFg.getAddress() != null) {
                Address fgAddress = defaultFg.getAddress();
                String stateAbbr = null;
                String countryAbbr = null;
                String phone = null;

                if (fgAddress.getState() != null) {
                    stateAbbr = fgAddress.getState().getAbbreviation();
                }

                if (fgAddress.getCountry() != null) {
                    countryAbbr = fgAddress.getCountry().getAbbreviation();
                }

                if (fgAddress.getPhonePrimary() != null) {
                    phone = fgAddress.getPhonePrimary().getPhoneNumber();
                }

                requestDTO.shipTo()
                        .addressFirstName(fgAddress.getFirstName())
                        .addressLastName(fgAddress.getLastName())
                        .addressCompanyName(fgAddress.getCompanyName())
                        .addressLine1(fgAddress.getAddressLine1())
                        .addressLine2(fgAddress.getAddressLine2())
                        .addressCityLocality(fgAddress.getCity())
                        .addressStateRegion(stateAbbr)
                        .addressPostalCode(fgAddress.getPostalCode())
                        .addressCountryCode(countryAbbr)
                        .addressPhone(phone)
                        .addressEmail(fgAddress.getEmailAddress());
            }
        }
    }

    protected void populateBillTo(Order order,
                                  PaymentRequestDTO requestDTO) {
        List<OrderPayment> payments = order.getPayments();
        for (OrderPayment payment : payments) {
            if (PaymentType.CREDIT_CARD.equals(payment.getType())) {
                Address billAddress = payment.getBillingAddress();
                String stateAbbr = null;
                String countryAbbr = null;
                String phone = null;

                if (billAddress.getState() != null) {
                    stateAbbr = billAddress.getState().getAbbreviation();
                }

                if (billAddress.getCountry() != null) {
                    countryAbbr = billAddress.getCountry().getAbbreviation();
                }

                if (billAddress.getPhonePrimary() != null) {
                    phone = billAddress.getPhonePrimary().getPhoneNumber();
                }

                requestDTO.billTo()
                        .addressFirstName(billAddress.getFirstName())
                        .addressLastName(billAddress.getLastName())
                        .addressCompanyName(billAddress.getCompanyName())
                        .addressLine1(billAddress.getAddressLine1())
                        .addressLine2(billAddress.getAddressLine2())
                        .addressCityLocality(billAddress.getCity())
                        .addressStateRegion(stateAbbr)
                        .addressPostalCode(billAddress.getPostalCode())
                        .addressCountryCode(countryAbbr)
                        .addressPhone(phone)
                        .addressEmail(billAddress.getEmailAddress());
            }
        }
    }



    /**
     * IMPORTANT:
     * If you would like to pass Line Item information to a payment gateway
     * so that it shows up on the hosted site, you will need to override this method and
     * construct line items to conform to the requirements of that particular gateway:
     *
     * For Example: The Paypal Express Checkout NVP API validates that the order subtotal that you pass in,
     * add up to the amount of the line items that you pass in. So,
     * In that case you will need to take into account any additional fees, promotions,
     * credits, gift cards, etc... that are applied to the payment and add them
     * as additional line items with a negative amount when necessary.
     *
     * Each gateway that accepts line item information may require you to construct
     * this differently. Please consult the module documentation on how it should
     * be properly constructed.
     *
     * In this default implementation, just the subtotal is set, without any line item details.
     *
     * @param order
     * @param requestDTO
     */
    protected void populateDefaultLineItemsAndSubtotal(Order order, PaymentRequestDTO requestDTO) {
        requestDTO.orderSubtotal(order.getSubTotal().toString());
    }



}
