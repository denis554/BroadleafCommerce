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

package org.broadleafcommerce.checkout.service;

import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.AbstractModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;

/**
 * @author jfischer
 *
 */
public class DummyCreditCardModule extends AbstractModule {

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }
    
    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        return createResponse(paymentContext);
    }

    private PaymentResponseItem createResponse(PaymentContext paymentContext) {
        paymentContext.getPaymentInfo().setReferenceNumber("abc123");
        PaymentResponseItem responseItem = new PaymentResponseItemImpl();
        responseItem.setTransactionTimestamp(SystemTime.asDate());
        responseItem.setReferenceNumber(paymentContext.getPaymentInfo().getReferenceNumber());
        responseItem.setTransactionId(paymentContext.getPaymentInfo().getReferenceNumber());
        responseItem.setTransactionSuccess(true);
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());
        responseItem.setCurrency(paymentContext.getPaymentInfo().getOrder().getCurrency());
        return responseItem;
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return PaymentInfoType.CREDIT_CARD.equals(paymentType);
    }
}
