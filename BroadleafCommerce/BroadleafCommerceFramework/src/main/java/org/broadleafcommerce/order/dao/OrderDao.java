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
package org.broadleafcommerce.order.dao;

import java.util.List;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.service.type.OrderStatus;
import org.broadleafcommerce.profile.domain.Customer;

public interface OrderDao {

    Order readOrderById(Long orderId);

    List<Order> readOrdersForCustomer(Customer customer, OrderStatus orderStatus);

    List<Order> readOrdersForCustomer(Long id);

    Order readNamedOrderForCustomer(Customer customer, String name);

    Order readCartForCustomer(Customer customer);

    Order save(Order order);

    void delete(Order order);

    Order submitOrder(Order cartOrder);

    Order create();

    Order createNewCartForCustomer(Customer customer);

    Order readOrderByOrderNumber(String orderNumber);

    //    removed methods
    //    List<Order> readNamedOrdersForcustomer(Customer customer);
    //
    //    Order readOrderForCustomer(Long customerId, Long orderId);
    //
    //    List<Order> readSubmittedOrdersForCustomer(Customer customer);
    //
}
