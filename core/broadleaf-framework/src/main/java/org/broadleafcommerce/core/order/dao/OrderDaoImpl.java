/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.core.order.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.common.util.StreamCapableTransactionalOperationAdapter;
import org.broadleafcommerce.common.util.StreamingTransactionCapableUtil;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;
import org.broadleafcommerce.core.order.domain.OrderLock;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.OrderPayment;
import org.broadleafcommerce.core.payment.domain.PaymentTransaction;
import org.broadleafcommerce.profile.core.dao.CustomerDao;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.hibernate.ejb.QueryHints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository("blOrderDao")
public class OrderDaoImpl implements OrderDao {

    private static final Log LOG = LogFactory.getLog(OrderDaoImpl.class);
    private static final String ORDER_LOCK_KEY = UUID.randomUUID().toString();

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Resource(name = "blCustomerDao")
    protected CustomerDao customerDao;

    @Resource(name = "blOrderDaoExtensionManager")
    protected OrderDaoExtensionManager extensionManager;

    @Resource(name = "blStreamingTransactionCapableUtil")
    protected StreamingTransactionCapableUtil transUtil;

    @Value("${order.lock.time.to.live:-1}")
    protected long orderLockTimeToLive = -1L;

    @Value("${order.lock.session.affinity:true}")
    protected boolean orderLockSessionAffinity = true;

    protected String orderLockKey;

    @PostConstruct
    public void init() {
        orderLockKey = orderLockSessionAffinity?ORDER_LOCK_KEY:"NO_KEY";
    }
    
    @Override
    public Order readOrderById(final Long orderId) {
        return em.find(OrderImpl.class, orderId);
    }

    @Override
    public Order readOrderById(final Long orderId, boolean refresh) {
        Order order = readOrderById(orderId);
        if (refresh) {
            em.refresh(order);
        }
        return order;
    }

    @Override
    public Order save(final Order order) {
        Order response = em.merge(order);
        //em.flush();
        return response;
    }

    @Override
    public void delete(Order salesOrder) {
        if (!em.contains(salesOrder)) {
            salesOrder = readOrderById(salesOrder.getId());
        }

        //need to null out the reference to the Order for all the OrderPayments
        //as they are not deleted but Archived.
        for (OrderPayment payment : salesOrder.getPayments()) {
            payment.setOrder(null);
            payment.setArchived('Y');
            for (PaymentTransaction transaction : payment.getTransactions()) {
                transaction.setArchived('Y');
            }
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
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.setHint(QueryHints.HINT_CACHE_REGION, "query.Order");
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
                throw new IllegalArgumentException("Attempting to save a customer with an id (" + customer.getId() + ") " +
                        "that already exists in the database. This can occur when legacy customers have been migrated to " +
                        "Broadleaf customers, but the batchStart setting has not been declared for id generation. In " +
                        "such a case, the defaultBatchStart property of IdGenerationDaoImpl (spring id of " +
                        "blIdGenerationDao) should be set to the appropriate start value.");
            }
            customer = customerDao.save(customer);
        }
        order.setCustomer(customer);
        order.setEmailAddress(customer.getEmailAddress());
        order.setStatus(OrderStatus.IN_PROCESS);

        if (BroadleafRequestContext.getBroadleafRequestContext() != null) {
            order.setCurrency(BroadleafRequestContext.getBroadleafRequestContext().getBroadleafCurrency());
            order.setLocale(BroadleafRequestContext.getBroadleafRequestContext().getLocale());
        }

        if (extensionManager != null) {
            extensionManager.getProxy().attachAdditionalDataToNewCart(customer, order);
        }
        
        order = save(order);

        if (extensionManager != null) {
            extensionManager.getProxy().processPostSaveNewCart(customer, order);
        }

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
    @SuppressWarnings("unchecked")
    public Order readNamedOrderForCustomer(final Customer customer, final String name) {
        final Query query = em.createNamedQuery("BC_READ_NAMED_ORDER_FOR_CUSTOMER");
        query.setParameter("customerId", customer.getId());
        query.setParameter("orderStatus", OrderStatus.NAMED.getType());
        query.setParameter("orderName", name);
        List<Order> orders = query.getResultList();
        
        // Filter out orders that don't match the current locale (if one is set)
        if (BroadleafRequestContext.getBroadleafRequestContext() != null) {
            ListIterator<Order> iter = orders.listIterator();
            while (iter.hasNext()) {
                Locale locale = BroadleafRequestContext.getBroadleafRequestContext().getLocale();
                Order order = iter.next();
                if (locale != null && !locale.equals(order.getLocale())) {
                    iter.remove();
                }
            }
        }
            
        // Apply any additional filters that extension modules have registered
        if (orders != null && !orders.isEmpty() && extensionManager != null) {
            extensionManager.getProxy().applyAdditionalOrderLookupFilter(customer, name, orders);
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

    @Override
    public boolean acquireLock(Order order) {
        // First, we'll see if there's a record of a lock for this order
        Query q = em.createNamedQuery("BC_ORDER_LOCK_READ");
        q.setParameter("orderId", order.getId());
        q.setParameter("key", orderLockKey);
        q.setHint(QueryHints.HINT_CACHEABLE, false);
        Long count = (Long) q.getSingleResult();
        
        if (count == 0L) {
            // If there wasn't a lock, we'll try to create one. It's possible that another thread is attempting the
            // same thing at the same time, so we might get a constraint violation exception here. That's ok. If we 
            // successfully inserted a record, that means that we are the owner of the lock right now.
            try {
                OrderLock ol = (OrderLock) entityConfiguration.createEntityInstance(OrderLock.class.getName());
                ol.setOrderId(order.getId());
                ol.setLocked(true);
                ol.setKey(orderLockKey);
                ol.setLastUpdated(System.currentTimeMillis());
                em.persist(ol);
                return true;
            } catch (EntityExistsException e) {
                return false;
            }
        }

        // We weren't successful in creating a lock, which means that there was some previously created lock
        // for this order. We'll attempt to update the status from unlocked to locked. If that is successful,
        // we acquired the lock. 
        q = em.createNamedQuery("BC_ORDER_LOCK_ACQUIRE");
        q.setParameter("orderId", order.getId());
        q.setParameter("currentTime", System.currentTimeMillis());
        q.setParameter("key", orderLockKey);
        q.setParameter("timeout", orderLockTimeToLive==-1L?orderLockTimeToLive:System.currentTimeMillis() - orderLockTimeToLive);
        q.setHint(QueryHints.HINT_CACHEABLE, false);
        int rowsAffected = q.executeUpdate();

        return rowsAffected == 1;
    }

    @Override
    public boolean releaseLock(final Order order) {
        final boolean[] response = {false};
        try {
            transUtil.runTransactionalOperation(new StreamCapableTransactionalOperationAdapter() {
                @Override
                public void execute() throws Throwable {
                    Query q = em.createNamedQuery("BC_ORDER_LOCK_RELEASE");
                    q.setParameter("orderId", order.getId());
                    q.setParameter("key", orderLockKey);
                    q.setHint(QueryHints.HINT_CACHEABLE, false);
                    int rowsAffected = q.executeUpdate();
                    response[0] = rowsAffected == 1;
                }

                @Override
                public boolean shouldRetryOnTransactionLockAcquisitionFailure() {
                    return true;
                }
            }, RuntimeException.class);
        } catch (RuntimeException e) {
            LOG.error(String.format("Could not release order lock (%s)", order.getId()), e);
        }
        return response[0];
    }
}
