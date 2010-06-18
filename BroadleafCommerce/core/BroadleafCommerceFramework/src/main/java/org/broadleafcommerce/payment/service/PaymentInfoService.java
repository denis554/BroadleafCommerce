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
package org.broadleafcommerce.payment.service;

import java.util.List;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.payment.domain.PaymentInfo;
import org.broadleafcommerce.payment.domain.PaymentLog;
import org.broadleafcommerce.payment.domain.PaymentResponseItem;

public interface PaymentInfoService {

    public PaymentInfo save(PaymentInfo paymentInfo);

    public PaymentResponseItem save(PaymentResponseItem paymentResponseItem);

    public PaymentLog save(PaymentLog log);

    public PaymentInfo readPaymentInfoById(Long paymentId);

    public List<PaymentInfo> readPaymentInfosForOrder(Order order);

    public PaymentInfo create();

    public void delete(PaymentInfo paymentInfo);

    public PaymentResponseItem createResponseItem();

    public PaymentLog createLog();

}
