package org.broadleafcommerce.test.integration;

import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.dao.SellableItemDaoJpa;
import org.broadleafcommerce.catalog.domain.SellableItem;
import org.broadleafcommerce.order.dao.OrderPaymentDaoJpa;
import org.broadleafcommerce.order.dao.OrderShippingDaoJpa;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderItem;
import org.broadleafcommerce.order.domain.OrderPayment;
import org.broadleafcommerce.order.domain.OrderShipping;
import org.broadleafcommerce.order.service.OrderServiceImpl;
import org.broadleafcommerce.profile.domain.ContactInfo;
import org.broadleafcommerce.profile.domain.User;
import org.broadleafcommerce.profile.service.ContactInfoServiceImpl;
import org.broadleafcommerce.profile.service.UserService;
import org.broadleafcommerce.test.dataprovider.OrderPaymentDataProvider;
import org.broadleafcommerce.test.dataprovider.OrderShippingDataProvider;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class OrderServiceTest extends BaseTest {

	private Order order = null;
	
	private List<OrderItem> orderItems = null;
	
	@Resource
	private OrderServiceImpl soService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private ContactInfoServiceImpl contactService;

	
	@Resource
	private SellableItemDaoJpa sellableItemDao;
	
	@Resource 
	OrderPaymentDaoJpa orderPaymentDao;
	
	@Resource
	OrderShippingDaoJpa shippingDao;
	
	@Test(groups={"createOrderForUserFromObj"}, dependsOnGroups={"readUser1", "createContactInfo"})
	@Rollback(false)
	public void createOrderForUserFromObj(){
		String userName = "user1";
		User user = userService.readUserByUsername(userName);
		
		Order order = soService.createOrderForUser(user);
		assert order != null;
		assert order.getId() != null;
		this.order = order;
	}
	
	@Test(groups={"addContactInfoToOrderFromObj"}, dependsOnGroups={"createOrderForUserFromObj"})
	public void addContactInfoToOrderFromObj(){
		ContactInfo contactInfo = (contactService.readContactInfoByUserId(order.getUser().getId())).get(0);
		assert contactInfo.getId() != null;
		Order order = soService.addContactInfoToOrder(this.order, contactInfo);
		assert order != null;
		assert order.getContactInfo() != null;
		assert order.getContactInfo().getId().equals(contactInfo.getId());
	}
	
	@Test(groups={"addItemToOrderFromObj"}, dependsOnGroups={"createOrderForUserFromObj","createSellableItem"})
	@Rollback(false)
	public void addItemToOrderFromObj(){
		SellableItem sellableItem = sellableItemDao.readFirstSellableItem();
		assert sellableItem.getId() != null;
		OrderItem item = soService.addItemToOrder(order, sellableItem, 1);
		assert item != null;
		assert item.getQuantity() == 1;
		assert item.getOrder() != null;
		assert item.getOrder().getId().equals(order.getId());
		assert item.getSellableItem() != null;
		assert item.getSellableItem().getId().equals(sellableItem.getId());
	}
	
	@Test(groups={"addPaymentToOrderFromObj"}, dataProvider="basicOrderPayment",dataProviderClass=OrderPaymentDataProvider.class,dependsOnGroups={"readUser1","createOrderForUserFromObj","createOrderPayment"})
	@Rollback(false)
	public void addPaymentToOrderFromObj(OrderPayment orderPayment){
		orderPayment = orderPaymentDao.maintainOrderPayment(orderPayment);
		assert orderPayment.getId() != null;
		OrderPayment payment = soService.addPaymentToOrder(order, orderPayment);
		assert payment != null;
		assert payment.getId() != null;
		assert payment.getOrder() != null;
		assert payment.getOrder().getId().equals(order.getId());
	}
	
	@Test(groups={"addShippingToOrderByObj"}, dataProvider="basicOrderShipping",dataProviderClass=OrderShippingDataProvider.class, dependsOnGroups={"createOrderForUserFromObj","createOrderShipping"})
	@Rollback(false)
	public void addShippingToOrderByObj(OrderShipping orderShipping){
		orderShipping = shippingDao.maintainOrderShipping(orderShipping);
		assert orderShipping.getId() != null;
		OrderShipping shipping = soService.addShippingToOrder(order, orderShipping);
		assert shipping != null;
		assert shipping.getId() != null;
		assert shipping.getOrder() != null;
		assert shipping.getOrder().getId().equals(order.getId());
	}
	
	@Test(groups={"createOrderForUserFromId"}, dependsOnGroups={"readUser1", "createContactInfo"})
	public void createOrderForUserFromId(){
		String userName = "user1";
		User user = userService.readUserByUsername(userName);
		
		Order order = soService.createOrderForUser(user.getId());
		assert order != null;
		assert order.getId() != null;
	}
	
	@Test(groups={"addContactInfoToOrderFromId"}, dependsOnGroups={"createOrderForUserFromObj","createContactInfo"})
	public void addContactInfoToOrderFromId(){
		ContactInfo contactInfo = (contactService.readContactInfoByUserId(order.getUser().getId())).get(0);
		assert contactInfo.getId() != null;
		Order order = soService.addContactInfoToOrder(this.order.getId(), contactInfo.getId());
		assert order != null;
		assert order.getContactInfo() != null;
		assert order.getContactInfo().getId().equals(contactInfo.getId());
	}
	
	@Test(groups={"addItemToOrderFromId"}, dependsOnGroups={"createOrderForUserFromObj", "createSellableItem"})
	public void addItemToOrderFromId(){
		SellableItem sellableItem = sellableItemDao.readFirstSellableItem();
		assert sellableItem.getId() != null;
		assert order != null;
		OrderItem item = soService.addItemToOrder(order.getId(), sellableItem.getId(), 1);
		assert item != null;
		assert item.getQuantity() == 1;
		assert item.getOrder() != null;
		assert item.getOrder().getId().equals(order.getId());
		assert item.getSellableItem() != null;
		assert item.getSellableItem().getId().equals(sellableItem.getId());
	}
	
	@Test(groups={"addPaymentToOrderFromId"}, dataProvider="basicOrderPayment",dataProviderClass=OrderPaymentDataProvider.class, dependsOnGroups={"createOrderForUserFromObj", "createOrderPayment"})
	public void addPaymentToOrderFromId(OrderPayment orderPayment){
		orderPayment = orderPaymentDao.maintainOrderPayment(orderPayment);
		assert orderPayment.getId() != null;
		OrderPayment payment = soService.addPaymentToOrder(order.getId(), orderPayment.getId());
		assert payment != null;
		assert payment.getId() != null;
		assert payment.getOrder() != null;
		assert payment.getOrder().getId().equals(order.getId());
	}
	
	@Test(groups={"addShippingToOrderById"}, dataProvider="basicOrderShipping",dataProviderClass=OrderShippingDataProvider.class, dependsOnGroups={"createOrderForUserFromObj"})
	public void addShippingToOrderById(OrderShipping orderShipping){
		orderShipping = shippingDao.maintainOrderShipping(orderShipping);
		assert orderShipping.getId() != null;
		OrderShipping shipping = soService.addShippingToOrder(order.getId(), orderShipping.getId());
		assert shipping != null;
		assert shipping.getId() != null;
		assert shipping.getOrder() != null;
		assert shipping.getOrder().getId().equals(order.getId());
	}
	
	@Test(groups={"getItemsForOrderFromObj"}, dependsOnGroups={"addItemToOrderFromObj"})
	@Rollback(false)
	public void getItemsForOrderFromObj(){
		List<OrderItem> orderItems = soService.getItemsForOrder(order);
		assert orderItems != null;
		assert orderItems.size() > 0;
		this.orderItems = orderItems;
	}

	@Test(groups={"getItemsForOrderFromId"}, dependsOnGroups={"addItemToOrderFromObj"})
	public void getItemsForOrderFromId(){
		List<OrderItem> orderItems = soService.getItemsForOrder(order.getId());
		assert orderItems != null;
		assert orderItems.size() > 0;
	}
	
	@Test(groups={"getOrdersForUserFromObj"}, dependsOnGroups={"readUser1","createOrderForUserFromObj"})
	public void getOrdersForUserFromObj(){
		String userName = "user1";
		User user = userService.readUserByUsername(userName);

		List<Order> orders = soService.getOrdersForUser(user);

		assert orders != null;
		assert orders.size() > 0;
	}
	
	@Test(groups={"getOrdersForUserFromId"}, dependsOnGroups={"readUser1","createOrderForUserFromObj"})
	public void getOrdersForUserFromId(){
		String userName = "user1";
		User user = userService.readUserByUsername(userName);

		List<Order> orders = soService.getOrdersForUser(user.getId());

		assert orders != null;
		assert orders.size() > 0;
	}
	
	@Test(groups={"updateItemsInOrderByObj"}, dependsOnGroups={"getItemsForOrderFromObj"})
	public void updateItemsInOrderByObj(){
		assert orderItems.size() > 0;
		OrderItem item = orderItems.get(0);
		item.setFinalPrice(10000);
		item.setQuantity(10);
		OrderItem updatedItem = soService.updateItemInOrder(order, item);
		assert updatedItem != null;
		assert updatedItem.getQuantity() == 10;
		assert updatedItem.getFinalPrice() == (updatedItem.getSellableItem().getPrice() * updatedItem.getQuantity());
	}
	
	@Test(groups={"removeItemFromOrderByObj"}, dependsOnGroups={"getItemsForOrderFromObj"})
	public void removeItemFromOrderByObj(){
		assert orderItems.size() > 0;
		int startingSize = orderItems.size();
		OrderItem item = orderItems.get(0);
		assert item != null;
		soService.removeItemFromOrder(order, item);
		List<OrderItem> items = soService.getItemsForOrder(order);
		assert items != null;
		assert items.size() == startingSize - 1;
	}
	
	@Test(groups={"removeItemFromOrderById"}, dependsOnGroups={"getItemsForOrderFromObj"})
	public void removeItemFromOrderById(){
		assert orderItems.size() > 0;
		int startingSize = orderItems.size();
		OrderItem item = orderItems.get(0);
		assert item != null;
		soService.removeItemFromOrder(order.getId(), item.getId());
		List<OrderItem> items = soService.getItemsForOrder(order);
		assert items != null;
		assert items.size() == startingSize - 1;
	}
}
