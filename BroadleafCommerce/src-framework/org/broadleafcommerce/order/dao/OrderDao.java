package org.broadleafcommerce.order.dao;

import java.util.List;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.profile.domain.Customer;

public interface OrderDao {

    public Order readOrderById(Long orderId);

    public Order maintianOrder(Order order);

    public List<Order> readOrdersForCustomer(Customer customer);

    public List<Order> readOrdersForCustomer(Long id);

    public void deleteOrderForCustomer(Order order);

    public Order readCartOrdersForCustomer(Customer customer, boolean persist);

    public List<Order> readSubmittedOrdersForCustomer(Customer customer);

    public Order readNamedOrderForCustomer(Customer customer, String name);

    public List<Order> readNamedOrdersForcustomer(Customer customer);
    
    public Order submitOrder(Order cartOrder);

    public Order create();

	public Order readOrderForCustomer(Long customerId, Long orderId);
}
