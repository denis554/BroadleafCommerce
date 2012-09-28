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

package org.broadleafcommerce.core.payment.service.module;

import java.util.HashMap;
import java.util.Map;

import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;


public class PaymentResponseImpl implements PaymentResponse {

    protected Map<PaymentInfo, PaymentResponseItem> responses = new HashMap<PaymentInfo, PaymentResponseItem>();

    public void addPaymentResponseItem(PaymentInfo paymentInfo, PaymentResponseItem paymentResponseItem) {
        responses.put(paymentInfo, paymentResponseItem);
    }

    public PaymentResponseItem getPaymentResponseItem(PaymentInfo paymentInfo) {
        return responses.get(paymentInfo);
    }

    public Map<PaymentInfo, PaymentResponseItem> getResponseItems() {
        return responses;
    }
}
