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

package org.broadleafcommerce.core.order.dao;

import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.profile.core.dao.CustomerDao;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.util.List;

@Repository("blOrderDao")
public class OrderDaoImpl implements OrderDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Resource(name = "blCustomerDao")
    protected CustomerDao customerDao;
    
    @Resource(name = "blOrderDaoExtensionManager")
    protected OrderDaoExtensionListener extensionManager;

    @Override
    public Order readOrderById(final Long orderId) {
        return em.find(OrderImpl.class, orderId);
    }

    @Override
    public Order save(final Order order) {
        return em.merge(order);
    }

    @Override
    public void delete(Order salesOrder) {
        if (!em.contains(salesOrder)) {
            salesOrder = readOrderById(salesOrder.getId());
        }
        em.remove(salesOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> readOrdersForCustomer(final Customer customer, final OrderStatus orderStatus) {
        if (orderStatus == null) {
            return readOrdersForCustomer(customer.getId());
        } else {
            final Query query = em.createNamedQuery("BC_READ_ORDERS_BY_CUSTOMER_ID_AND_STATUS");
            query.setParameter("customerId", customer.getId());
            query.setParameter("orderStatus", orderStatus.getType());
            return query.getResultList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> readOrdersForCustomer(final Long customerId) {
        final Query query = em.createNamedQuery("BC_READ_ORDERS_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    public Order readCartForCustomer(final Customer customer) {
        Order order = null;
        final Query query = em.createNamedQuery("BC_READ_ORDERS_BY_CUSTOMER_ID_AND_NAME_NULL");
        query.setParameter("customerId", customer.getId());
        query.setParameter("orderStatus", OrderStatus.IN_PROCESS.getType());
        @SuppressWarnings("rawtypes")
		final List temp = query.getResultList();
        if (temp != null && !temp.isEmpty()) {
            order = (Order) temp.get(0);
        }
        return order;
    }

    @Override
    public Order createNewCartForCustomer(Customer customer) {
        Order order = create();
        if (customer.getUsername() == null) {
            customer.setUsername(String.valueOf(customer.getId()));
            if (customerDao.readCustomerById(customer.getId()) != null) {
                throw new IllegalArgumentException("Attempting to save a customer with an id (" + customer.getId() + ") that already exists in the database. This can occur when legacy customers have been migrated to Broadleaf customers, but the batchStart setting has not been declared for id generation. In such a case, the defaultBatchStart property of IdGenerationDaoImpl (spring id of blIdGenerationDao) should be set to the appropriate start value.");
            }
            customer = customerDao.save(customer);
        }
        order.setCustomer(customer);
        order.setEmailAddress(customer.getEmailAddress());
        order.setStatus(OrderStatus.IN_PROCESS);

        //FIXME: apa extract these to i18n module
        if(BroadleafRequestContext.getBroadleafRequestContext()!=null) {
            //for integration tests there is no context set.
            order.setCurrency(BroadleafRequestContext.getBroadleafRequestContext().getBroadleafCurrency());
            order.setLocale(BroadleafRequestContext.getBroadleafRequestContext().getLocale());
        }
        if (extensionManager != null) {
            extensionManager.attachAdditionalDataToNewCart(customer, order);
        }
        
        order = save(order);
        return order;
    }

    @Override
    public Order submitOrder(final Order cartOrder) {
        cartOrder.setStatus(OrderStatus.SUBMITTED);
        return save(cartOrder);
    }

    @Override
    public Order create() {
        final Order order = ((Order) entityConfiguration.createEntityInstance("org.broadleafcommerce.core.order.domain.Order"));

        return order;
    }

    @Override
    public Order readNamedOrderForCustomer(final Customer customer, final String name) {
        final Query query = em.createNamedQuery("BC_READ_NAMED_ORDER_FOR_CUSTOMER");
        query.setParameter("customerId", customer.getId());
        query.setParameter("orderStatus", OrderStatus.NAMED.getType());
        query.setParameter("orderName", name);
        List<Order> orders = query.getResultList();
        
        //FIXME:apa if((order.getPriceList() == priceList) && (order.getLocale().getLocaleCode().equalsIgnoreCase(locale.getLocaleCode()))){
        if (orders != null && !orders.isEmpty() && extensionManager != null) {
            extensionManager.applyAdditionalOrderLookupFilter(customer, name, orders);
        }
        
        return orders == null || orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Order readOrderByOrderNumber(final String orderNumber) {
        if (orderNumber == null || "".equals(orderNumber)) {
            return null;
        }

        final Query query = em.createNamedQuery("BC_READ_ORDER_BY_ORDER_NUMBER");
        query.setParameter("orderNumber", orderNumber);
        List<Order> orders = query.getResultList();
        return orders == null || orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public Order updatePrices(Order order) {
        order = em.merge(order);
        if (order.updatePrices()) {
            order = save(order);
        }
        return order;
    }
}
