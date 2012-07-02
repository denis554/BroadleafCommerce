/*
 * Copyright 2012 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.offer.domain.OfferCode;
import org.broadleafcommerce.core.offer.service.OfferService;
import org.broadleafcommerce.core.offer.service.exception.OfferMaxUseExceededException;
import org.broadleafcommerce.core.order.dao.OrderDao;
import org.broadleafcommerce.core.order.domain.NullOrderFactory;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.call.MergeCartResponse;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.exception.AddToCartException;
import org.broadleafcommerce.core.order.service.exception.RemoveFromCartException;
import org.broadleafcommerce.core.order.service.exception.UpdateCartException;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.order.service.workflow.CartOperationContext;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.payment.dao.PaymentInfoDao;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.pricing.service.PricingService;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.workflow.SequenceProcessor;
import org.broadleafcommerce.core.workflow.WorkflowException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

/**
 * @author apazzolini
 */
@Service("blOrderService")
public class OrderServiceImpl implements OrderService {
    private static final Log LOG = LogFactory.getLog(OrderServiceImpl.class);
	
	@Resource(name = "blPaymentInfoDao")
    protected PaymentInfoDao paymentInfoDao;
	
    @Resource(name = "blOrderDao")
    protected OrderDao orderDao;

    @Resource(name = "blNullOrderFactory")
    protected NullOrderFactory nullOrderFactory;    
    
    @Resource(name = "blPricingService")
    protected PricingService pricingService;
    
    @Resource(name = "blOrderItemService")
    protected OrderItemService orderItemService;
    
    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;
    
    @Resource(name = "blOfferService")
    protected OfferService offerService;
    
    @Resource(name = "blAddItemWorkflow")
    protected SequenceProcessor addItemWorkflow;
    
    @Resource(name = "blUpdateItemWorkflow")
    protected SequenceProcessor updateItemWorkflow;
    
    @Resource(name = "blRemoveItemWorkflow")
    protected SequenceProcessor removeItemWorkflow;
    
    // This field is static for legacy testing support.
    protected static boolean automaticallyMergeLikeItems = true;

	@Override
	public Order createNewCartForCustomer(Customer customer) {
        return orderDao.createNewCartForCustomer(customer);
	}
	
	@Override
    public Order createNamedOrderForCustomer(String name, Customer customer) {
        Order namedOrder = orderDao.create();
        namedOrder.setCustomer(customer);
        namedOrder.setName(name);
        namedOrder.setStatus(OrderStatus.NAMED);
        return orderDao.save(namedOrder); // No need to price here
    }

	@Override
    public Order findNamedOrderForCustomer(String name, Customer customer) {
        return orderDao.readNamedOrderForCustomer(customer, name);
    }

	@Override
	public Order findOrderById(Long orderId) {
        return orderDao.readOrderById(orderId);
	}

	@Override
	public Order getNullOrder() {
    	return nullOrderFactory.getNullOrder();
	}

	@Override
	public Order findCartForCustomer(Customer customer) {
        return orderDao.readCartForCustomer(customer);
	}

	@Override
	public List<Order> findOrdersForCustomer(Customer customer) {
        return orderDao.readOrdersForCustomer(customer.getId());
	}

	@Override
	public List<Order> findOrdersForCustomer(Customer customer, OrderStatus status) {
        return orderDao.readOrdersForCustomer(customer, status);
	}

	@Override
	public Order findOrderByOrderNumber(String orderNumber) {
        return orderDao.readOrderByOrderNumber(orderNumber);
	}

	@Override
	public List<PaymentInfo> findPaymentInfosForOrder(Order order) {
        return paymentInfoDao.readPaymentInfosForOrder(order);
	}

	@Override
	public Order save(Order order, Boolean priceOrder) throws PricingException {
        if (priceOrder) {
            order = pricingService.executePricing(order);
        }
        return orderDao.save(order);
	}

	@Override
	public void cancelOrder(Order order) {
        orderDao.delete(order);
	}

	@Override
	public Order addOfferCode(Order order, OfferCode offerCode, boolean priceOrder) throws PricingException, OfferMaxUseExceededException {
        if( !order.getAddedOfferCodes().contains(offerCode)) {
            if (! offerService.verifyMaxCustomerUsageThreshold(order.getCustomer(), offerCode.getOffer())) {
                throw new OfferMaxUseExceededException("The customer has used this offer code more than the maximum allowed number of times.");
            }
            order.getAddedOfferCodes().add(offerCode);
            order = save(order, priceOrder);
        }
        return order;	
    }

	@Override
	public Order removeOfferCode(Order order, OfferCode offerCode, boolean priceOrder) throws PricingException {
        order.getAddedOfferCodes().remove(offerCode);
        order = save(order, priceOrder);
        return order;	
    }

	@Override
	public Order removeAllOfferCodes(Order order, boolean priceOrder) throws PricingException {
		 order.getAddedOfferCodes().clear();
		 order = save(order, priceOrder);
		 return order;	
	}

	@Override
	public boolean getAutomaticallyMergeLikeItems() {
		return automaticallyMergeLikeItems;
	}

	@Override
	@SuppressWarnings("static-access")
	public void setAutomaticallyMergeLikeItems(boolean automaticallyMergeLikeItems) {
		this.automaticallyMergeLikeItems = automaticallyMergeLikeItems;
	}

	@Override
	public MergeCartResponse mergeCart(Customer customer, Order anonymousCart) throws PricingException {
		throw new UnsupportedOperationException();
	}
    
    @Override
    public Order addItem(Long orderId, OrderItemRequestDTO orderItemRequestDTO, boolean priceOrder) throws AddToCartException {
    	try {
    		CartOperationRequest cartOpRequest = new CartOperationRequest(findOrderById(orderId), orderItemRequestDTO, priceOrder);
    		CartOperationContext context = (CartOperationContext) addItemWorkflow.doActivities(cartOpRequest);
    		return context.getSeedData().getOrder();
    	} catch (WorkflowException e) {
    		throw new AddToCartException("Could not add to cart", e.getCause());
    	}
    }

	@Override
	public Order updateItemQuantity(Long orderId, OrderItemRequestDTO orderItemRequestDTO, boolean priceOrder) throws UpdateCartException, RemoveFromCartException {
		if (orderItemRequestDTO.getQuantity() == 0) {
			return removeItem(orderId, orderItemRequestDTO.getOrderItemId(), priceOrder);
		}
		
    	try {
    		CartOperationRequest cartOpRequest = new CartOperationRequest(findOrderById(orderId), orderItemRequestDTO, priceOrder);
    		CartOperationContext context = (CartOperationContext) updateItemWorkflow.doActivities(cartOpRequest);
    		return context.getSeedData().getOrder();
    	} catch (WorkflowException e) {
    		throw new UpdateCartException("Could not update cart quantity", e.getCause());
    	}
	}

	@Override
	public Order removeItem(Long orderId, Long orderItemId, boolean priceOrder) throws RemoveFromCartException {
    	try {
    		OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
    		orderItemRequestDTO.setOrderItemId(orderItemId);
    		CartOperationRequest cartOpRequest = new CartOperationRequest(findOrderById(orderId), orderItemRequestDTO, priceOrder);
    		CartOperationContext context = (CartOperationContext) removeItemWorkflow.doActivities(cartOpRequest);
    		return context.getSeedData().getOrder();
    	} catch (WorkflowException e) {
    		throw new RemoveFromCartException("Could not remove from cart", e.getCause());
    	}
	}
}
