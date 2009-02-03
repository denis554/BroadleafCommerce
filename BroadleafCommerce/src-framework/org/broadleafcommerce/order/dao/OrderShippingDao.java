package org.broadleafcommerce.order.dao;

import java.util.List;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderShipping;

public interface OrderShippingDao {

	public OrderShipping readOrderShippingById(Long shippingId);
	
	public OrderShipping maintainOrderShipping(OrderShipping shipping);
	
	public List<OrderShipping> readOrderShippingForOrder(Order order);
}
