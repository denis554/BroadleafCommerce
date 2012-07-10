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

package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.payment.domain.AmountItem;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a JAXB wrapper around PaymentInfo.
 * <p/>
 * User: Elbert Bautista
 * Date: 4/26/12
 */
@XmlRootElement(name = "paymentInfo")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class PaymentInfoWrapper extends BaseWrapper implements APIWrapper<PaymentInfo>, APIUnwrapper<PaymentInfo> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected Long orderId;

    @XmlElement
    protected String type;

    @XmlElement
    protected AddressWrapper address;

    @XmlElement
    protected PhoneWrapper phone;

    @XmlElement(name = "element")
    @XmlElementWrapper(name = "additionalFields")
    protected List<MapElementWrapper> additionalFields;

    @XmlElement
    protected Money amount;

    @XmlElement(name = "amountItem")
    @XmlElementWrapper(name = "amountItems")
    protected List<AmountItemWrapper> amountItems;

    @XmlElement
    protected String customerIpAddress;

    @XmlElement
    protected String referenceNumber;

    @Override
    public void wrap(PaymentInfo model, HttpServletRequest request) {
        this.id = model.getId();

        if (model.getOrder() != null) {
            this.orderId = model.getOrder().getId();
        }

        if (model.getType() != null) {
            this.type = model.getType().getType();
        }

        if (model.getAddress() != null) {
            AddressWrapper addressWrapper = (AddressWrapper) context.getBean(AddressWrapper.class.getName());
            addressWrapper.wrap(model.getAddress(), request);
            this.address = addressWrapper;
        }

        if (model.getPhone() != null) {
            PhoneWrapper phoneWrapper = (PhoneWrapper) context.getBean(PhoneWrapper.class.getName());
            phoneWrapper.wrap(model.getPhone(), request);
            this.phone = phoneWrapper;
        }

        if (model.getAdditionalFields() != null && !model.getAdditionalFields().isEmpty()) {
            List<MapElementWrapper> mapElementWrappers = new ArrayList<MapElementWrapper>();
            for (String key : model.getAdditionalFields().keySet()) {
                MapElementWrapper mapElementWrapper = new MapElementWrapper();
                mapElementWrapper.setKey(key);
                mapElementWrapper.setValue(model.getAdditionalFields().get(key));
                mapElementWrappers.add(mapElementWrapper);
            }
            this.additionalFields = mapElementWrappers;
        }

        this.amount = model.getAmount();

        if (model.getAmountItems() != null) {
            List<AmountItemWrapper> wrappers = new ArrayList<AmountItemWrapper>();
            for (AmountItem amountItem : model.getAmountItems()) {
                AmountItemWrapper amountItemWrapper = (AmountItemWrapper) context.getBean(AmountItemWrapper.class.getName());
                amountItemWrapper.wrap(amountItem, request);
                wrappers.add(amountItemWrapper);
            }
            this.amountItems = wrappers;
        }

        this.customerIpAddress = model.getCustomerIpAddress();
        this.referenceNumber = model.getReferenceNumber();
    }

    @Override
    public PaymentInfo unwrap(HttpServletRequest request, ApplicationContext context) {
        PaymentInfoService paymentInfoService = (PaymentInfoService) context.getBean("blPaymentInfoService");
        PaymentInfo paymentInfo = paymentInfoService.create();

        OrderService orderService = (OrderService) context.getBean("blOrderService");
        Order order = orderService.findOrderById(this.orderId);
        paymentInfo.setOrder(order);

        paymentInfo.setType(PaymentInfoType.getInstance(this.type));

        if (this.address != null) {
            paymentInfo.setAddress(this.address.unwrap(request, context));
        }

        if (this.phone != null) {
            paymentInfo.setPhone(this.phone.unwrap(request, context));
        }

        if (this.additionalFields != null && !this.additionalFields.isEmpty()) {
            Map<String, String> fields = new HashMap<String, String>();
            for (MapElementWrapper mapElementWrapper : this.additionalFields) {
                fields.put(mapElementWrapper.getKey(), mapElementWrapper.getValue().toString());
            }

            paymentInfo.setAdditionalFields(fields);
        }

        paymentInfo.setAmount(this.amount);
        paymentInfo.setCustomerIpAddress(this.customerIpAddress);
        paymentInfo.setReferenceNumber(this.referenceNumber);

        return paymentInfo;
    }
}
