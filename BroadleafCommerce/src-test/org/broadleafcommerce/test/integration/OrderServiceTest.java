package org.broadleafcommerce.test.integration;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.dao.SkuDao;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.order.dao.PaymentInfoDao;
import org.broadleafcommerce.order.domain.FulfillmentGroup;
import org.broadleafcommerce.order.domain.FulfillmentGroupImpl;
import org.broadleafcommerce.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderItem;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.order.service.OrderService;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.service.CustomerAddressService;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.test.dataprovider.FulfillmentGroupDataProvider;
import org.broadleafcommerce.test.dataprovider.PaymentInfoDataProvider;
import org.broadleafcommerce.util.money.Money;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class OrderServiceTest extends BaseTest {

    private Long orderId = null;
    private int numOrderItems = 0;
    private Long fulfillmentGroupId;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private OrderService orderService;

    @Resource
    private CustomerService customerService;

    @Resource
    private SkuDao skuDao;

    @Resource
    private PaymentInfoDao paymentInfoDao;

    @Test(groups = { "findCurrentCartForCustomerBeforeCreation" }, dependsOnGroups = { "readCustomer1", "createPhone" })
    @Rollback(false)
    public void findCurrentCartForCustomerBeforeCreation() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = orderService.findCartForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        this.orderId = order.getId();
    }

    @Test(groups = { "findCurrentCartForCustomerAfterCreation" }, dependsOnGroups = { "findCurrentCartForCustomerBeforeCreation" })
    @Rollback(false)
    public void findCurrentCartForCustomerAfterCreation() {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);

        Order order = orderService.findCartForCustomer(customer);
        assert order != null;
        assert order.getId() != null;
        assert order.getId().equals(orderId);
        this.orderId = order.getId();
    }

    @Test(groups = { "addItemToOrder" }, dependsOnGroups = { "findCurrentCartForCustomerAfterCreation", "createSku" })
    @Rollback(false)
    public void addItemToOrder() {
        numOrderItems++;
        Sku sku = skuDao.readFirstSku();
        Order order = orderService.findOrderById(orderId);
        assert order != null;
        assert sku.getId() != null;
        OrderItem item = orderService.addSkuToOrder(order, sku, 1);
        assert item != null;
        assert item.getQuantity() == numOrderItems;
        //assert item.getOrder() != null;
        //assert item.getOrder().getId().equals(order.getId());
        assert item.getSku() != null;
        assert item.getSku().getId().equals(sku.getId());

    }

    @Test(groups = { "getItemsForOrder" }, dependsOnGroups = { "addItemToOrder" })
    @Rollback(false)
    public void getItemsForOrder() {
        Order order = orderService.findOrderById(orderId);
        List<OrderItem> orderItems = order.getOrderItems();
        assert orderItems != null;
        assert orderItems.size() == numOrderItems;
    }

    @Test(groups = { "updateItemsInOrder" }, dependsOnGroups = { "getItemsForOrder" })
    public void updateItemsInOrder() {
        Order order = orderService.findOrderById(orderId);
        List<OrderItem> orderItems = order.getOrderItems();
        assert orderItems.size() > 0;
        OrderItem item = orderItems.get(0);
        item.setPrice(new Money(BigDecimal.valueOf(10000)));
        item.setQuantity(10);
        OrderItem updatedItem = orderService.updateItemInOrder(order, item);
        assert updatedItem != null;
        assert updatedItem.getQuantity() == 10;
        // assert updatedItem.getFinalPrice() == (updatedItem.getSku().getPrice() * updatedItem.getQuantity());
    }

    @Test(groups = { "removeItemFromOrder" }, dependsOnGroups = { "getItemsForOrder" })
    public void removeItemFromOrder() {
        Order order = orderService.findOrderById(orderId);
        List<OrderItem> orderItems = order.getOrderItems();
        assert orderItems.size() > 0;
        int startingSize = orderItems.size();
        OrderItem item = orderItems.get(0);
        assert item != null;
        order = orderService.removeItemFromOrder(order, item);
        //List<OrderItem> items = orderService.findItemsForOrder(order);
        List<OrderItem> items = order.getOrderItems();
        assert items != null;
        assert items.size() == startingSize - 1;
    }

    @Test(groups = { "addPaymentToOrder" }, dataProvider = "basicPaymentInfo", dataProviderClass = PaymentInfoDataProvider.class, dependsOnGroups = { "readCustomer1", "findCurrentCartForCustomerAfterCreation", "createPaymentInfo" })
    @Rollback(false)
    public void addPaymentToOrder(PaymentInfo paymentInfo) {
        Order order = orderService.findOrderById(orderId);
        paymentInfo = paymentInfoDao.maintainPaymentInfo(paymentInfo);
        assert paymentInfo.getId() != null;
        PaymentInfo payment = orderService.addPaymentToOrder(order, paymentInfo);
        assert payment != null;
        assert payment.getId() != null;
        assert payment.getOrder() != null;
        assert payment.getOrder().getId().equals(order.getId());
    }

    @Test(groups = "addFulfillmentGroupToOrderFirst", dataProvider = "basicFulfillmentGroup", dataProviderClass = FulfillmentGroupDataProvider.class, dependsOnGroups = { "createCustomerAddress", "findCurrentCartForCustomerAfterCreation", "addItemToOrder" })
    @Rollback(false)
    public void addFulfillmentGroupToOrderFirst(FulfillmentGroup fulfillmentGroup) {
        String userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);
        //Address address = (customerAddressDao.readActiveCustomerAddressesByCustomerId(customer.getId())).get(0).getAddress();
        Address address = customerAddressService.readActiveCustomerAddressesByCustomerId(customer.getId()).get(0).getAddress();
        Order order = orderService.findOrderById(orderId);

        fulfillmentGroup.setOrderId(order.getId());
        fulfillmentGroup.setAddress(address);

        FulfillmentGroup fg = orderService.addFulfillmentGroupToOrder(order, fulfillmentGroup);
        assert fg != null;
        assert fg.getId() != null;
        assert fg.getAddress().equals(fulfillmentGroup.getAddress());
        assert fg.getRetailPrice().equals(fulfillmentGroup.getRetailPrice());
        assert fg.getOrderId() == order.getId();
        assert fg.getMethod().equals(fulfillmentGroup.getMethod());
        assert fg.getReferenceNumber().equals(fulfillmentGroup.getReferenceNumber());
        this.fulfillmentGroupId = fg.getId();
    }

    @Test(groups = { "findFulFillmentGroupForOrderFirst" }, dependsOnGroups = { "findCurrentCartForCustomerAfterCreation", "addFulfillmentGroupToOrderFirst" })
    public void findFillmentGroupForOrderFirst() {
        //FulfillmentGroup fg = orderService.findFulfillmentGroupsForOrder(order).get(0);
        Order order = orderService.findOrderById(orderId);
        FulfillmentGroup fg = order.getFulfillmentGroups().get(0);
        assert fg != null;
        assert fg.getId() != null;
        FulfillmentGroup fulfillmentGroup = em.find(FulfillmentGroupImpl.class, fulfillmentGroupId);
        assert fg.getAddress().getId().equals(fulfillmentGroup.getAddress().getId());
        assert fg.getRetailPrice().equals(fulfillmentGroup.getRetailPrice());
        assert fg.getOrderId().longValue() == order.getId().longValue();
        assert fg.getMethod().equals(fulfillmentGroup.getMethod());
        assert fg.getReferenceNumber().equals(fulfillmentGroup.getReferenceNumber());
    }
    /*
    @Test(groups = { "removeFulFillmentGroupForOrderFirst" }, dependsOnGroups = { "findCurrentCartForCustomerAfterCreation", "addFulfillmentGroupToOrderFirst" })
    public void removeFulFillmentGroupForOrderFirst() {
        int beforeRemove = orderService.findFulfillmentGroupsForOrder(order).size();
        FulfillmentGroup fulfillmentGroup = em.find(FulfillmentGroupImpl.class, fulfillmentGroupId);
        orderService.removeFulfillmentGroupFromOrder(order, fulfillmentGroup);
        int afterRemove = orderService.findFulfillmentGroupsForOrder(order).size();
        assert (beforeRemove - afterRemove) == 1;
    }
     */
    @Test(groups = { "findDefaultFulFillmentGroupForOrder" }, dependsOnGroups = { "findCurrentCartForCustomerAfterCreation", "addFulfillmentGroupToOrderFirst" })
    public void findDefaultFillmentGroupForOrder() {
        Order order = orderService.findOrderById(orderId);
        FulfillmentGroupImpl fg = orderService.findDefaultFulfillmentGroupForOrder(order);
        assert fg != null;
        assert fg.getId() != null;
        FulfillmentGroup fulfillmentGroup = em.find(FulfillmentGroupImpl.class, fulfillmentGroupId);
        assert fg.getAddress().getId().equals(fulfillmentGroup.getAddress().getId());
        assert fg.getRetailPrice().equals(fulfillmentGroup.getRetailPrice());
        assert fg.getOrderId().longValue() == order.getId().longValue();
        assert fg.getMethod().equals(fulfillmentGroup.getMethod());
        assert fg.getReferenceNumber().equals(fulfillmentGroup.getReferenceNumber());
    }
    /*
    @Test(groups = { "removeDefaultFulFillmentGroupForOrder" }, dependsOnGroups = { "findCurrentCartForCustomerAfterCreation", "addFulfillmentGroupToOrderFirst" })
    public void removeDefaultFulFillmentGroupForOrder() {
        int beforeRemove = orderService.findFulfillmentGroupsForOrder(order).size();
        orderService.removeFulfillmentGroupFromOrder(order, fulfillmentGroup);
        int afterRemove = orderService.findFulfillmentGroupsForOrder(order).size();
        assert (beforeRemove - afterRemove) == 1;
    }
     */
    @Test(groups = { "removeItemFromOrderAfterDefaultFulfillmentGroup" }, dependsOnGroups = { "addFulfillmentGroupToOrderFirst" })
    public void removeItemFromOrderAfterFulfillmentGroups() {
        Order order = orderService.findOrderById(orderId);
        List<OrderItem> orderItems = order.getOrderItems();
        assert orderItems.size() > 0;
        OrderItem item = orderItems.get(0);
        assert item != null;
        orderService.removeItemFromOrder(order, item);
        FulfillmentGroupImpl fg = orderService.findDefaultFulfillmentGroupForOrder(order);
        for (FulfillmentGroupItem fulfillmentGroupItem : fg.getFulfillmentGroupItems()) {
            assert fulfillmentGroupItem.getOrderItem().getId() != item.getId();
        }
    }

    @Test(groups = { "getOrdersForCustomer" }, dependsOnGroups = { "readCustomer1", "findCurrentCartForCustomerAfterCreation" })
    public void getOrdersForCustomer() {
        String username = "customer1";
        Customer customer = customerService.readCustomerByUsername(username);
        List<Order> orders = orderService.findOrdersForCustomer(customer);
        assert orders != null;
        assert orders.size() > 0;
    }
}
