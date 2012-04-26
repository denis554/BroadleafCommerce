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

package org.broadleafcommerce.core.web.api.endpoint.checkout;

import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.payment.domain.*;
import org.broadleafcommerce.core.payment.service.CompositePaymentService;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.payment.service.workflow.CompositePaymentResponse;
import org.broadleafcommerce.core.web.api.wrapper.*;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAXRS endpoint for exposing the checkout process as a set of RESTful services.
 * <p/>
 * User: Kelly Tisdell
 * Date: 4/10/12
 */
@Component("blRestCheckoutEndpoint")
@Scope("singleton")
@Path("/cart/checkout/")
@Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CheckoutEndpoint implements ApplicationContextAware {

    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    //This service is backed by the entire payment workflow configured in the application context.
    //It is the entry point for engaging the payment workflow
    @Resource(name="blCompositePaymentService")
    protected CompositePaymentService compositePaymentService;

    @Resource(name="blCartService")
    protected CartService cartService;

    @Resource(name="blCustomerState")
    protected CustomerState customerState;

    protected ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @POST
    @Path("payment/response")
    //This should only be called for modules that need to engage the workflow directly without doing a complete checkout.
    //e.g. PayPal for doing an authorize and retrieving the redirect: url to PayPal
    public PaymentResponseItemWrapper executePayment(@Context HttpServletRequest request, PaymentReferenceMapWrapper mapWrapper) {
        Customer customer = customerState.getCustomer(request);

        if (customer != null) {
            Order cart = cartService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
                        PaymentInfo paymentInfo = mapWrapper.getPaymentInfoWrapper().unwrap(request, context);
                        Referenced referenced = mapWrapper.getReferencedWrapper().unwrap(request, context);
                        payments.put(paymentInfo, referenced);

                        CompositePaymentResponse compositePaymentResponse = compositePaymentService.executePayment(cart, payments);
                        PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getResponseItems().get(paymentInfo);

                        PaymentResponseItemWrapper paymentResponseItemWrapper = (PaymentResponseItemWrapper) context.getBean(PaymentResponseItemWrapper.class);
                        paymentResponseItemWrapper.wrap(responseItem, request);

                        return paymentResponseItemWrapper;

                } catch (PaymentException e) {
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    @POST
    public OrderWrapper performCheckout(@Context HttpServletRequest request, List<PaymentReferenceMapWrapper> mapWrappers) {
        Customer customer = customerState.getCustomer(request);

        if (customer != null) {
            Order cart = cartService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                    if (mapWrappers != null && !mapWrappers.isEmpty()) {
                        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();

                        for (PaymentReferenceMapWrapper mapWrapper : mapWrappers) {
                            PaymentInfo paymentInfo = mapWrapper.getPaymentInfoWrapper().unwrap(request, context);
                            Referenced referenced = mapWrapper.getReferencedWrapper().unwrap(request, context);

                            payments.put(paymentInfo, referenced);
                        }

                        CheckoutResponse response = checkoutService.performCheckout(cart, payments);
                        Order order = response.getOrder();
                        OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                        wrapper.wrap(order,request);
                        return wrapper;
                    }
                } catch (CheckoutException e) {
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
}
