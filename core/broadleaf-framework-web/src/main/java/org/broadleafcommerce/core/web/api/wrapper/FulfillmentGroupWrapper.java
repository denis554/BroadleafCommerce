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
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.FulfillmentGroupItemRequest;
import org.broadleafcommerce.core.order.service.call.FulfillmentGroupRequest;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a JAXB wrapper around FulfillmentGroup.
 *
 * User: Elbert Bautista
 * Date: 4/10/12
 */
@XmlRootElement(name = "fulfillmentGroup")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class FulfillmentGroupWrapper extends BaseWrapper implements APIWrapper<FulfillmentGroup>, APIUnwrapper<FulfillmentGroupRequest> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected Long orderId;

    @XmlElement
    protected Money total;

    @XmlElement
    protected AddressWrapper address;

    @XmlElement
    protected PhoneWrapper phone;

    @XmlElement(name = "fulfillmentGroupItem")
    @XmlElementWrapper(name = "fulfillmentGroupItems")
    protected List<FulfillmentGroupItemWrapper> fulfillmentGroupItems;

    @Override
    public void wrap(FulfillmentGroup model, HttpServletRequest request) {
        this.id = model.getId();
        this.total = model.getTotal();

        if (model.getOrder() != null) {
            this.orderId = model.getOrder().getId();
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

        List<FulfillmentGroupItem> fgs = model.getFulfillmentGroupItems();
        if (fgs != null && !fgs.isEmpty()) {
            List<FulfillmentGroupItemWrapper> fulfillmentGroupItemWrappers = new ArrayList<FulfillmentGroupItemWrapper>();
            for (FulfillmentGroupItem fgi : fgs) {
                FulfillmentGroupItemWrapper fulfillmentGroupItemWrapper = (FulfillmentGroupItemWrapper) context.getBean(FulfillmentGroupItemWrapper.class.getName());
                fulfillmentGroupItemWrapper.wrap(fgi, request);
                fulfillmentGroupItemWrappers.add(fulfillmentGroupItemWrapper);
            }
            this.fulfillmentGroupItems = fulfillmentGroupItemWrappers;
        }

    }

    @Override
    public FulfillmentGroupRequest unwrap(HttpServletRequest request, ApplicationContext appContext) {
        FulfillmentGroupRequest fulfillmentGroupRequest = new FulfillmentGroupRequest();

        List<FulfillmentGroupItemRequest> fulfillmentGroupItemRequests = new ArrayList<FulfillmentGroupItemRequest>();
        for (FulfillmentGroupItemWrapper wrapper : this.fulfillmentGroupItems) {
            fulfillmentGroupItemRequests.add(wrapper.unwrap(request, appContext));
        }

        fulfillmentGroupRequest.setFulfillmentGroupItemRequests(fulfillmentGroupItemRequests);

        OrderService orderService = (OrderService) appContext.getBean("blCartService");
        Order order = orderService.findOrderById(this.orderId);
        if (order != null) {
            fulfillmentGroupRequest.setOrder(order);
        }

        if (this.address != null) {
            fulfillmentGroupRequest.setAddress(this.address.unwrap(request, appContext));
        }

        if (this.phone != null) {
            fulfillmentGroupRequest.setPhone(this.phone.unwrap(request, appContext));
        }

        return fulfillmentGroupRequest;
    }
}
