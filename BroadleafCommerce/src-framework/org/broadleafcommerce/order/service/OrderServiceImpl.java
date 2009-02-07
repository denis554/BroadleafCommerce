package org.broadleafcommerce.order.service;

import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.dao.SellableItemDao;
import org.broadleafcommerce.catalog.domain.SellableItem;
import org.broadleafcommerce.order.dao.OrderDao;
import org.broadleafcommerce.order.dao.OrderItemDao;
import org.broadleafcommerce.order.dao.OrderPaymentDao;
import org.broadleafcommerce.order.dao.OrderShippingDao;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderItem;
import org.broadleafcommerce.order.domain.OrderPayment;
import org.broadleafcommerce.order.domain.OrderShipping;
import org.broadleafcommerce.profile.dao.AddressDao;
import org.broadleafcommerce.profile.dao.ContactInfoDao;
import org.broadleafcommerce.profile.dao.CustomerDao;
import org.broadleafcommerce.profile.domain.ContactInfo;
import org.broadleafcommerce.profile.domain.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private OrderPaymentDao orderPaymentDao;

    @Resource
    private OrderShippingDao orderShippingDao;

    @Resource
    private SellableItemDao sellableItemDao;

    @Resource
    private ContactInfoDao contactInfoDao;

    @Resource
    private CustomerDao customerDao;

    @Resource
    private AddressDao addressDao;

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order createOrderForCustomer(Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        return maintainOrder(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order addContactInfoToOrder(Order order, ContactInfo contactInfo){
        if(contactInfo.getId() == null){
            contactInfoDao.maintainContactInfo(contactInfo);
        }
        order.setContactInfo(contactInfo);
        return maintainOrder(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderPayment addPaymentToOrder(Order order, OrderPayment payment) {
        payment.setOrder(order);
        if(payment.getAddress()!= null && payment.getAddress().getId() == null){
            payment.setAddress(addressDao.maintainAddress(payment.getAddress()));
        }
        return orderPaymentDao.maintainOrderPayment(payment);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderShipping addShippingToOrder(Order order, OrderShipping shipping) {
        shipping.setOrder(order);
        if(shipping.getAddress() != null && shipping.getAddress().getId() == null){
            shipping.setAddress(addressDao.maintainAddress(shipping.getAddress()));
        }
        return orderShippingDao.maintainOrderShipping(shipping);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order calculateOrderTotal(Order order) {
        double total = 0;
        List<OrderItem> orderItemList = orderItemDao.readOrderItemsForOrder(order);
        for(OrderItem item : orderItemList){
            total += item.getFinalPrice();
        }

        List<OrderShipping> shippingList = orderShippingDao.readOrderShippingForOrder(order);
        for(OrderShipping shipping : shippingList){
            total += shipping.getCost();
        }
        order.setOrderTotal(total);
        return order;
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void cancelOrder(Order order) {
        orderDao.deleteOrderForCustomer(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order confirmOrder(Order order) {
        // TODO Other actions needed to complete order.  Code below is only a start.
        return orderDao.submitOrder(order);
    }

    @Override
    public List<OrderItem> getItemsForOrder(Order order) {
        List<OrderItem> result = orderItemDao.readOrderItemsForOrder(order);
        for (OrderItem oi : result) {
            oi.getSellableItem().getItemAttributes();
        }
        return result;
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderItem addItemToOrder(Order order, SellableItem item, int quantity){
        OrderItem orderItem = null;
        List<OrderItem> orderItems = orderItemDao.readOrderItemsForOrder(order);
        for (OrderItem orderItem2 : orderItems) {
            if(orderItem2.getSellableItem().getId().equals(item.getId()))
                orderItem = orderItem2;
        }
        if(orderItem == null)
            orderItem = new OrderItem();
        orderItem.setSellableItem(item);
        orderItem.setQuantity(orderItem.getQuantity()+quantity);
        orderItem.setOrder(order);
        return maintainOrderItem(orderItem);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order removeItemFromOrder(Order order, OrderItem item) {
        orderItemDao.deleteOrderItem(item);
        calculateOrderTotal(order);
        return order;
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderItem updateItemInOrder(Order order, OrderItem item) {
        // This isn't quite right.  It will need to be changed later to reflect
        // the exact requirements we want.
        // item.setQuantity(quantity);
        item.setOrder(order);
        return maintainOrderItem(item);
    }

    @Override
    public List<Order> getOrdersForCustomer(Customer customer) {
        return orderDao.readOrdersForCustomer(customer);
    }

    @Override
    public Order getCurrentBasketForCustomer(Customer customer) {
        return orderDao.readBasketOrderForCustomer(customer);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order createOrderForCustomer(long customerId){
        Customer customer = customerDao.readCustomerById(customerId);
        return createOrderForCustomer(customer);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderItem addItemToOrder(Long orderId, Long itemId, int quantity) {
        Order order = orderDao.readOrderById(orderId);
        SellableItem si = sellableItemDao.readSellableItemById(itemId);
        return this.addItemToOrder(order, si, quantity);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderPayment addPaymentToOrder(Long orderId, Long paymentId) {
        Order order = orderDao.readOrderById(orderId);
        OrderPayment sop = orderPaymentDao.readOrderPaymentById(paymentId);
        return this.addPaymentToOrder(order, sop);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderShipping addShippingToOrder(Long orderId, Long shippingId) {
        Order order = orderDao.readOrderById(orderId);
        OrderShipping shipping = orderShippingDao.readOrderShippingById(shippingId);
        return this.addShippingToOrder(order, shipping);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order calculateOrderTotal(Long orderId) {
        Order order = orderDao.readOrderById(orderId);
        return this.calculateOrderTotal(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void cancelOrder(Long orderId) {
        Order order = orderDao.readOrderById(orderId);
        this.cancelOrder(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order confirmOrder(Long orderId) {
        Order order = orderDao.readOrderById(orderId);
        return this.confirmOrder(order);
    }

    @Override
    public List<OrderItem> getItemsForOrder(Long orderId) {
        Order order = orderDao.readOrderById(orderId);
        return this.getItemsForOrder(order);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderDao.readOrderById(orderId);
        OrderItem item = orderItemDao.readOrderItemById(itemId);
        return this.removeItemFromOrder(order, item);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OrderItem updateItemInOrder(Long orderId, Long itemId, int quantity, double finalPrice) {
        Order order = orderDao.readOrderById(orderId);
        OrderItem item = orderItemDao.readOrderItemById(itemId);
        item.setQuantity(quantity);
        item.setFinalPrice(finalPrice);
        return this.updateItemInOrder(order, item);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order addContactInfoToOrder(Long orderId, Long contactId){
        Order order = orderDao.readOrderById(orderId);
        ContactInfo ci = contactInfoDao.readContactInfoById(contactId);
        return this.addContactInfoToOrder(order, ci);

    }

    @Override
    public List<Order> getOrdersForCustomer(Long userId) {
        return orderDao.readOrdersForCustomer(userId);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Order getCurrentBasketForUserId(Long userId) {
        return orderDao.readBasketOrderForCustomer(customerDao.readCustomerById(userId));
    }

    private Order maintainOrder(Order order){
        calculateOrderTotal(order);
        return orderDao.maintianOrder(order);
    }

    private OrderItem maintainOrderItem(OrderItem orderItem){
        orderItem.setFinalPrice(orderItem.getQuantity() * orderItem.getSellableItem().getPrice());
        OrderItem returnedOrderItem = orderItemDao.maintainOrderItem(orderItem);
        maintainOrder(orderItem.getOrder());
        return returnedOrderItem;
    }

}
