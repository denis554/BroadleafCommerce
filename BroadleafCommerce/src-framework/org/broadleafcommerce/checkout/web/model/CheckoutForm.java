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
package org.broadleafcommerce.checkout.web.model;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.payment.CreditCardType;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;

public class CheckoutForm {
    private String firstName;
    private String lastName;
    private String emailAddress;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    private Address shippingAddress;
    private Address billingAddress;
    private String creditCardNumber;
    private String creditCardCvvCode;
    private String creditCardExpMonth;
    private String creditCardExpYear;
    private String primaryPhoneNumber;
    private String secondaryPhoneNumber;
    private String selectedCreditCardType;

    public String getSelectedCreditCardType() {
        return selectedCreditCardType;
    }

    public void setSelectedCreditCardType(String selectedCreditCardType) {
        this.selectedCreditCardType = selectedCreditCardType;
    }

    public List<CreditCardType> getApprovedCreditCardTypes() {
        List<CreditCardType> approvedCCTypes = new ArrayList<CreditCardType>();
        approvedCCTypes.add(CreditCardType.VISA);
        approvedCCTypes.add(CreditCardType.MASTERCARD);
        approvedCCTypes.add(CreditCardType.AMEX);
        return approvedCCTypes;
    }

    public Address getShippingAddress() {
        return shippingAddress == null ? new AddressImpl() : shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress == null ? new AddressImpl() : billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
        this.primaryPhoneNumber = primaryPhoneNumber;
    }

    public String getSecondaryPhoneNumber() {
        return secondaryPhoneNumber;
    }

    public void setSecondaryPhoneNumber(String secondaryPhoneNumber) {
        this.secondaryPhoneNumber = secondaryPhoneNumber;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardCvvCode() {
        return creditCardCvvCode;
    }

    public void setCreditCardCvvCode(String creditCardCvvCode) {
        this.creditCardCvvCode = creditCardCvvCode;
    }

    public String getCreditCardExpMonth() {
        return creditCardExpMonth;
    }

    public void setCreditCardExpMonth(String creditCardExpMonth) {
        this.creditCardExpMonth = creditCardExpMonth;
    }

    public String getCreditCardExpYear() {
        return creditCardExpYear;
    }

    public void setCreditCardExpYear(String creditCardExpYear) {
        this.creditCardExpYear = creditCardExpYear;
    }

}
