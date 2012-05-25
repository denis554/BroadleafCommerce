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

package org.broadleafcommerce.core.web.controller.order;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.order.model.FindOrderForm;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("viewOrderController")
@RequestMapping("/orders")
public class ViewOrderController {

    @Resource(name="blCustomerState")
    private CustomerState customerState;
    @Resource(name="blCartService")
    protected CartService cartService;

    @RequestMapping(method =  {RequestMethod.GET})
    public String viewOrders (ModelMap model, HttpServletRequest request) throws PricingException {
        List<Order> orders = cartService.findOrdersForCustomer(customerState.getCustomer(request), OrderStatus.SUBMITTED);
        model.addAttribute("orderList", orders);
        return "listOrders";
    }

    @RequestMapping(value="viewOrderDetails", method = {RequestMethod.GET})
    public String viewOrderDetails (ModelMap model, HttpServletRequest request, @RequestParam(required = true) String orderNumber) {
        Order order = cartService.findOrderByOrderNumber(orderNumber);
        if (order == null) {
            return "findOrderError";
        }

        model.addAttribute("order", order);
        return "viewOrderDetails";
    }

    @RequestMapping(value="viewOrderConfirmation", method = {RequestMethod.GET})
    public String viewOrderConfirmation (ModelMap model, HttpServletRequest request, @RequestParam(required = true) String orderNumber) {
        Order order = cartService.findOrderByOrderNumber(orderNumber);
        if (order == null) {
            return "findOrderError";
        }

        model.addAttribute("order", order);
        return "checkout/checkoutConfirmation";
    }

    @RequestMapping(value="findOrder", method =  {RequestMethod.GET})
    public String findOrder (ModelMap model, HttpServletRequest request) {
        model.addAttribute("findOrderForm", new FindOrderForm());
        return "findOrder";
    }

    @RequestMapping(value="findOrder", method =  {RequestMethod.POST})
    public String processFindOrder (@ModelAttribute FindOrderForm findOrderForm, ModelMap model, HttpServletRequest request) {
        Order order = cartService.findOrderByOrderNumber(findOrderForm.getOrderNumber());

        if (order == null) {
            return "findOrderError";
        }
        List<FulfillmentGroup> orderFulfillmentGroups = order.getFulfillmentGroups();
        if (orderFulfillmentGroups != null ) {
            for (FulfillmentGroup fulfillmentGroup : orderFulfillmentGroups)  {
                if (fulfillmentGroup.getAddress().getPostalCode().equals(findOrderForm.getPostalCode())) {
		            return viewOrderDetails(model, request, order.getOrderNumber());
                }
            }
        }

        return "findOrderError";
    }
}
