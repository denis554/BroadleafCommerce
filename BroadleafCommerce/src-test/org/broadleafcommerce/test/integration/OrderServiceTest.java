package org.broadleafcommerce.test.integration;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import org.broadleafcommerce.catalog.dao.SkuDaoJpa;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.order.dao.FullfillmentGroupDaoJpa;
import org.broadleafcommerce.order.dao.PaymentInfoDaoJpa;
import org.broadleafcommerce.order.domain.DefaultFullfillmentGroup;
import org.broadleafcommerce.order.domain.FullfillmentGroup;
import org.broadleafcommerce.order.domain.FullfillmentGroupItem;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderItem;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.order.service.OrderServiceImpl;
import org.broadleafcommerce.profile.dao.AddressDaoJpa;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.ContactInfo;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.service.ContactInfoServiceImpl;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.test.dataprovider.FullfillmentGroupDataProvider;
import org.broadleafcommerce.test.dataprovider.PaymentInfoDataProvider;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class OrderServiceTest extends BaseTest {

    private Order order = null;
    private int numOrderItems = 0;
    private FullfillmentGroup fulfillmentGroup;

    private List<OrderItem> orderItems = null;

    @Resource
    private AddressDaoJpa addressDao;
    
    @Resource
    private OrderServiceImpl soService;

    @Resource
    private CustomerService customerService;

    @Resource
    private ContactInfoServiceImpl contactService;

    @Resource
    private SkuDaoJpa skuDao;

    @Resource
    PaymentInfoDaoJpa paymentInfoDao;

    @Resource
    FullfillmentGroupDaoJpa fulfillmentGroupDao;

    @Test(groups = { "findCurrentBasketForCustomerBeforeCreation" }, dependsOnGroups = { "readCustomer1", "createContactInfo" })
    @Rollback(false)
    public void findCurrentBasketForCustomerBeforeCreation() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = soService.findCurrentBasketForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        this.order = order;
    }

    @Test(groups = { "findCurrentBasketForCustomerAfterCreation" }, dependsOnGroups = { "findCurrentBasketForCustomerBeforeCreation" })
    @Rollback(false)
    public void findCurrentBasketForCustomerAfterCreation() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = soService.findCurrentBasketForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        assert order.getId().equals(this.order.getId());
        this.order = order;
    }

    @Test(groups = { "addContactInfoToOrder" }, dependsOnGroups = { "findCurrentBasketForCustomerBeforeCreation" })
    public void addContactInfoToOrder() {
        ContactInfo contactInfo = (contactService.readContactInfoByUserId(order.getCustomer().getId())).get(0);
        assert contactInfo.getId() != null;
        Order order = soService.addContactInfoToOrder(this.order, contactInfo);
        assert order != null;
        assert order.getContactInfo() != null;
        assert order.getContactInfo().getId().equals(contactInfo.getId());
    }

    @Test(groups = { "addItemToOrder" }, dependsOnGroups = { "findCurrentBasketForCustomerAfterCreation", "createSku" })
    @Rollback(false)
    public void addItemToOrder() {
    	numOrderItems++;
        Sku sku = skuDao.readFirstSku();
        assert sku.getId() != null;
        OrderItem item = soService.addItemToOrder(order, sku, 1);
        assert item != null;
        assert item.getQuantity() == numOrderItems;
        assert item.getOrder() != null;
        assert item.getOrder().getId().equals(order.getId());
        assert item.getSku() != null;
        assert item.getSku().getId().equals(sku.getId());
    }

    @Test(groups = { "getItemsForOrder" }, dependsOnGroups = { "addItemToOrder" })
    @Rollback(false)
    public void getItemsForOrder() {
        List<OrderItem> orderItems = soService.findItemsForOrder(order);
        assert orderItems != null;
        assert orderItems.size() == numOrderItems;
        this.orderItems = orderItems;
    }

    @Test(groups = { "updateItemsInOrder" }, dependsOnGroups = { "getItemsForOrder" })
    public void updateItemsInOrder() {
        assert orderItems.size() > 0;
        OrderItem item = orderItems.get(0);
        item.setFinalPrice(10000);
        item.setQuantity(10);
        OrderItem updatedItem = soService.updateItemInOrder(order, item);
        assert updatedItem != null;
        assert updatedItem.getQuantity() == 10;
        assert updatedItem.getFinalPrice() == (updatedItem.getSku().getPrice() * updatedItem.getQuantity());
    }

    @Test(groups = { "removeItemFromOrder" }, dependsOnGroups = { "getItemsForOrder" })
    public void removeItemFromOrder() {
        assert orderItems.size() > 0;
        int startingSize = orderItems.size();
        OrderItem item = orderItems.get(0);
        assert item != null;
        soService.removeItemFromOrder(order, item);
        List<OrderItem> items = soService.findItemsForOrder(order);
        assert items != null;
        assert items.size() == startingSize - 1;
    }
    
    @Test(groups = { "addPaymentToOrder" }, dataProvider = "basicPaymentInfo", dataProviderClass = PaymentInfoDataProvider.class, dependsOnGroups = { "readCustomer1", "findCurrentBasketForCustomerAfterCreation", "createPaymentInfo" })
    @Rollback(false)
    public void addPaymentToOrder(PaymentInfo paymentInfo) {
        paymentInfo = paymentInfoDao.maintainPaymentInfo(paymentInfo);
        assert paymentInfo.getId() != null;
        PaymentInfo payment = soService.addPaymentToOrder(order, paymentInfo);
        assert payment != null;
        assert payment.getId() != null;
        assert payment.getOrder() != null;
        assert payment.getOrder().getId().equals(order.getId());
    }

    @Test(groups = { "addFullfillmentGroupToOrderFirst" }, dataProvider="basicFullfillmentGroup", dataProviderClass=FullfillmentGroupDataProvider.class, dependsOnGroups = {"createAddress", "findCurrentBasketForCustomerAfterCreation","addItemToOrder"})
    @Rollback(false)
    public void addFullfillmentGroupToOrderFirst(FullfillmentGroup fullfillmentGroup){
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);
        Address address = (addressDao.readAddressByUserId(customer.getId())).get(0);

        fullfillmentGroup.setOrderId(order.getId());
        fullfillmentGroup.setAddress(address);
    	
    	FullfillmentGroup fg = soService.addFullfillmentGroupToOrder(order, fullfillmentGroup);
    	assert fg != null;
    	assert fg.getId() != null;
    	assert fg.getAddress().equals(fullfillmentGroup.getAddress());
    	assert fg.getCost() == fullfillmentGroup.getCost();
    	assert fg.getOrderId().equals(order.getId());
    	assert fg.getMethod().equals(fullfillmentGroup.getMethod());
    	assert fg.getReferenceNumber().equals(fullfillmentGroup.getReferenceNumber());    	
    	this.fulfillmentGroup = fg;
    }
    
    @Test(groups = { "findFulFillmentGroupForOrderFirst" }, dependsOnGroups = { "findCurrentBasketForCustomerAfterCreation", "addFullfillmentGroupToOrderFirst"})
    public void findFillmentGroupForOrderFirst(){
    	FullfillmentGroup fg = soService.findFullfillmentGroupsForOrder(order).get(0);
    	assert fg != null;
    	assert fg.getId() != null;
    	assert fg.getAddress().getId().equals(fulfillmentGroup.getAddress().getId());
    	assert fg.getCost() == fulfillmentGroup.getCost();
    	assert fg.getOrderId().equals(order.getId());
    	assert fg.getMethod().equals(fulfillmentGroup.getMethod());
    	assert fg.getReferenceNumber().equals(fulfillmentGroup.getReferenceNumber());
    }

    @Test(groups = {"removeFulFillmentGroupForOrderFirst"}, dependsOnGroups = { "findCurrentBasketForCustomerAfterCreation", "addFullfillmentGroupToOrderFirst"})
    @Rollback(false)
    public void removeFulFillmentGroupForOrderFirst(){
    	List<FullfillmentGroup> groups = soService.findFullfillmentGroupsForOrder(order);
    	soService.removeFullfillmentGroupFromOrder(order, fulfillmentGroup);
    	List<FullfillmentGroup> groupsAfter = soService.findFullfillmentGroupsForOrder(order);
    	assert groups.size() == groupsAfter.size()-1;
    }
    
    @Test(groups = { "findDefaultFulFillmentGroupForOrder" }, dependsOnGroups = { "findCurrentBasketForCustomerAfterCreation", "addFullfillmentGroupToOrderFirst"})
    public void findDefaultFillmentGroupForOrder(){
    	DefaultFullfillmentGroup fg = soService.findDefaultFullfillmentGroupForOrder(order);
    	assert fg != null;
    	assert fg.getId() != null;
    	assert fg.getAddress().getId().equals(fulfillmentGroup.getAddress().getId());
    	assert fg.getCost() == fulfillmentGroup.getCost();
    	assert fg.getOrderId().equals(order.getId());
    	assert fg.getMethod().equals(fulfillmentGroup.getMethod());
    	assert fg.getReferenceNumber().equals(fulfillmentGroup.getReferenceNumber());
    }

    @Test(groups = {"removeDefaultFulFillmentGroupForOrder"}, dependsOnGroups = { "findCurrentBasketForCustomerAfterCreation", "addFullfillmentGroupToOrderFirst"})
    public void removeDefaultFulFillmentGroupForOrder(){
    	soService.removeFullfillmentGroupFromOrder(order, fulfillmentGroup);
    	try{
    		soService.findDefaultFullfillmentGroupForOrder(order);
    	}catch (NoResultException nre) {
    		assert true;
		}
    	assert false;
    }

    
    @Test(groups = { "removeItemFromOrderAfterDefaultFulfillmentGroup" }, dependsOnGroups = { "addFullfillmentGroupToOrderFirst"})
    public void removeItemFromOrderAfterFulfillmentGroups() {
        assert orderItems.size() > 0;
        OrderItem item = orderItems.get(0);
        assert item != null;
        soService.removeItemFromOrder(order, item);
        DefaultFullfillmentGroup dfg = fulfillmentGroupDao.readDefaultFullfillmentGroupForOrder(order);
        for (FullfillmentGroupItem fullfillmentGroupItem : dfg.getFullfillmentGroupItems()) {
        	assert fullfillmentGroupItem.getOrderItem().getId() != item.getId();
		}
    }
    
    
    
    
    
    
    
    @Test(groups = { "getOrdersForCustomer" }, dependsOnGroups = { "readCustomer1", "findCurrentBasketForCustomerAfterCreation" })
    public void getOrdersForCustomer() {
        String username = "customer1";
        Customer customer = customerService.readCustomerByUsername(username);
        List<Order> orders = soService.findOrdersForCustomer(customer);
        assert orders != null;
        assert orders.size() > 0;
    }


}
