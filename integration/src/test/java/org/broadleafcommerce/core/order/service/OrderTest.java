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

package org.broadleafcommerce.core.order.service;

import org.broadleafcommerce.core.catalog.dao.SkuDao;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.exception.AddToCartException;
import org.broadleafcommerce.core.pricing.service.ShippingRateService;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import javax.annotation.Resource;

@SuppressWarnings("deprecation")
public class OrderTest extends OrderBaseTest {

    private Long orderId = null;
    private int numOrderItems = 0;
    private Long fulfillmentGroupId;
    private Long bundleOrderItemId;

    @Resource(name = "blOrderItemService")
    private OrderItemService orderItemService;
    
    @Resource
    private SkuDao skuDao;
    
    @Resource
    private ShippingRateService shippingRateService;
    
    @Test(groups = { "createCartForCustomer" }, dependsOnGroups = { "readCustomer", "createPhone" })
    @Transactional
    @Rollback(false)
    public void createCartForCustomer() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = orderService.createNewCartForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        this.orderId = order.getId();
    }

    @Test(groups = { "findCurrentCartForCustomer" }, dependsOnGroups = { "readCustomer", "createPhone", "createCartForCustomer" })
    @Transactional
    @Rollback(false)
    public void findCurrentCartForCustomer() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = orderService.findCartForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        this.orderId = order.getId();
    }

    @Test(groups = { "addItemToOrder" }, dependsOnGroups = { "findCurrentCartForCustomer", "createSku" })
    @Rollback(false)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addItemToOrder() throws PricingException, AddToCartException {
        numOrderItems++;
        Sku sku = skuDao.readFirstSku();
        Order order = orderService.findOrderById(orderId);
        assert order != null;
        assert sku.getId() != null;
        
        OrderItemRequestDTO itemRequest = new OrderItemRequestDTO();
        itemRequest.setQuantity(1);
        itemRequest.setSkuId(sku.getId());
        order = orderService.addItem(orderId, itemRequest, true);
        
//        assert item != null;
//        assert item.getQuantity() == numOrderItems;
//        assert item.getSku() != null;
//        assert item.getSku().equals(sku);
    }
    
}
