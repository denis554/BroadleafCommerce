package org.broadleafcommerce.order.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.dao.CategoryDao;
import org.broadleafcommerce.catalog.dao.ProductDao;
import org.broadleafcommerce.catalog.dao.SkuDao;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.offer.dao.OfferDao;
import org.broadleafcommerce.offer.domain.Offer;
import org.broadleafcommerce.order.dao.FulfillmentGroupDao;
import org.broadleafcommerce.order.dao.FulfillmentGroupItemDao;
import org.broadleafcommerce.order.dao.OrderDao;
import org.broadleafcommerce.order.dao.PaymentInfoDao;
import org.broadleafcommerce.order.domain.BundleOrderItem;
import org.broadleafcommerce.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.order.domain.FulfillmentGroup;
import org.broadleafcommerce.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderItem;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.order.service.call.BundleOrderItemRequest;
import org.broadleafcommerce.order.service.call.DiscreteOrderItemRequest;
import org.broadleafcommerce.order.service.exception.ItemNotFoundException;
import org.broadleafcommerce.order.service.type.FulfillmentGroupType;
import org.broadleafcommerce.order.service.type.OrderStatus;
import org.broadleafcommerce.pricing.service.advice.PricingExecutionManager;
import org.broadleafcommerce.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Resource
    protected OrderDao orderDao;

    @Resource
    protected PaymentInfoDao paymentInfoDao;

    @Resource
    protected FulfillmentGroupDao fulfillmentGroupDao;

    @Resource
    protected FulfillmentGroupItemDao fulfillmentGroupItemDao;

    @Resource
    protected OfferDao offerDao;

    @Resource
    protected PricingExecutionManager pricingExecutionManager;

    @Resource
    protected OrderItemService orderItemService;

    @Resource
    protected SkuDao skuDao;

    @Resource
    protected ProductDao productDao;

    @Resource
    protected CategoryDao categoryDao;

    protected boolean rollupOrderItems = true;

    @Override
    public Order createNamedOrderForCustomer(String name, Customer customer) {
        Order namedOrder = orderDao.create();
        namedOrder.setCustomer(customer);
        namedOrder.setName(name);
        namedOrder.setStatus(OrderStatus.NAMED);
        return persistOrder(namedOrder);
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderDao.readOrderById(orderId);
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
    public Order findNamedOrderForCustomer(String name, Customer customer) {
        return orderDao.readNamedOrderForCustomer(customer, name);
    }

    @Override
    public FulfillmentGroup findDefaultFulfillmentGroupForOrder(Order order) {
        FulfillmentGroup fg = fulfillmentGroupDao.readDefaultFulfillmentGroupForOrder(order);

        return fg;
    }

    @Override
    public OrderItem addSkuToOrder(Long orderId, Long skuId, Long productId, Long categoryId, Integer quantity) throws PricingException {
        /*
         * TODO add to test
         */
        Order order = findOrderById(orderId);
        Sku sku = skuDao.readSkuById(skuId);
        Product product;
        if (productId != null) {
            product = productDao.readProductById(productId);
        } else {
            product = null;
        }
        Category category;
        if (categoryId != null) {
            category = categoryDao.readCategoryById(categoryId);
        } else {
            category = null;
        }

        DiscreteOrderItemRequest itemRequest = new DiscreteOrderItemRequest();
        itemRequest.setCategory(category);
        itemRequest.setProduct(product);
        itemRequest.setQuantity(quantity);
        itemRequest.setSku(sku);

        return addDiscreteItemToOrder(order, itemRequest);
    }

    @Override
    public OrderItem addDiscreteItemToOrder(Order order, DiscreteOrderItemRequest itemRequest) throws PricingException {
        DiscreteOrderItem item = orderItemService.createDiscreteOrderItem(itemRequest);
        return addOrderItemToOrder(order, item);
    }

    @Override
    public OrderItem addBundleItemToOrder(Order order, BundleOrderItemRequest itemRequest) throws PricingException {
        BundleOrderItem item = orderItemService.createBundleOrderItem(itemRequest);
        return addOrderItemToOrder(order, item);
    }

    public Order removeItemFromOrder(Long orderId, Long itemId) throws PricingException {
        Order order = findOrderById(orderId);
        OrderItem orderItem = orderItemService.readOrderItemById(itemId);

        return removeItemFromOrder(order, orderItem);
    }

    @Override
    public Order removeItemFromOrder(Order order, OrderItem item) throws PricingException {
        removeOrderItemFromFullfillmentGroup(order, item);
        OrderItem itemFromOrder = order.getOrderItems().remove(order.getOrderItems().indexOf(item));
        order = updateOrder(order);
        orderItemService.delete(itemFromOrder);
        return order;
    }

    @Override
    public PaymentInfo addPaymentToOrder(Order order, PaymentInfo payment) {
        payment.setOrder(order);
        order.getPaymentInfos().add(payment);
        order = persistOrder(order);
        return order.getPaymentInfos().get(order.getPaymentInfos().indexOf(payment));
    }

    @Override
    public FulfillmentGroup addFulfillmentGroupToOrder(Order order, FulfillmentGroup fulfillmentGroup) throws PricingException {
        FulfillmentGroup dfg =  findDefaultFulfillmentGroupForOrder(order);
        if (dfg == null) {
            dfg = createDefaultFulfillmentGroup(order, fulfillmentGroup.getAddress());
            order.getFulfillmentGroups().add(dfg);
            order = updateOrder(order);
            return order.getFulfillmentGroups().get(order.getFulfillmentGroups().indexOf(dfg));
        }
        if (dfg.equals(fulfillmentGroup)) {
            // API user is trying to re-add the default fulfillment group to the same order
            fulfillmentGroup.setType(FulfillmentGroupType.DEFAULT);
            order.getFulfillmentGroups().remove(dfg);
            order.getFulfillmentGroups().add(fulfillmentGroup);
            order = updateOrder(order);
            fulfillmentGroupDao.delete(dfg);
            return order.getFulfillmentGroups().get(order.getFulfillmentGroups().indexOf(fulfillmentGroup));
        } else {
            // API user is adding a new fulfillment group to the order
            fulfillmentGroup.setOrder(order);
            // 1) For each item in the new fulfillment group
            for (FulfillmentGroupItem fgItem : fulfillmentGroup.getFulfillmentGroupItems()) {
                // 2) Find the item's existing fulfillment group
                for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
                    // 3) remove item from it's existing fulfillment
                    // group
                    fg.getFulfillmentGroupItems().remove(fgItem);
                    fulfillmentGroupItemDao.delete(fgItem);
                }
            }
            order.getFulfillmentGroups().add(fulfillmentGroup);
            order = updateOrder(order);
            return order.getFulfillmentGroups().get(order.getFulfillmentGroups().indexOf(fulfillmentGroup));
        }
    }

    @Override
    public FulfillmentGroup addItemToFulfillmentGroup(OrderItem item, FulfillmentGroup fulfillmentGroup, int quantity) throws PricingException {
        Order order = item.getOrder();
        if (fulfillmentGroup.getId() == null) {
            // API user is trying to add an item to a fulfillment group not created
            fulfillmentGroup = addFulfillmentGroupToOrder(order, fulfillmentGroup);
        }
        // API user is trying to add an item to an existing fulfillment group
        // Steps are
        // 1) Find the item's existing fulfillment group
        for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
            Iterator<FulfillmentGroupItem> itr = fg.getFulfillmentGroupItems().iterator();
            while(itr.hasNext()) {
                FulfillmentGroupItem fgItem = itr.next();
                if (fgItem.getOrderItem().equals(item)) {
                    // 2) remove item from it's existing fulfillment group
                    itr.remove();
                    fulfillmentGroupItemDao.delete(fgItem);
                }
            }
        }
        FulfillmentGroupItem fgi = createFulfillmentGroupItemFromOrderItem(item, fulfillmentGroup, quantity);

        // 3) add the item to the new fulfillment group
        //TODO why are we only adding when the fulfillmentgroup type is null???
        //if (fulfillmentGroup.getType() == null) {
        fulfillmentGroup.addFulfillmentGroupItem(fgi);
        //}
        order = updateOrder(order);

        return order.getFulfillmentGroups().get(order.getFulfillmentGroups().indexOf(fulfillmentGroup));
    }

    @Override
    public Order addOfferToOrder(Order order, String offerCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OrderItem updateItemInOrder(Order order, OrderItem item) throws ItemNotFoundException, PricingException {
        // This isn't quite right. It will need to be changed later to reflect
        // the exact requirements we want.
        // item.setQuantity(quantity);
        // item.setOrder(order);
        if (!order.getOrderItems().contains(item)) {
            throw new ItemNotFoundException("Order Item (" + item.getId() + ") not found in Order (" + order.getId() +")");
        }
        OrderItem itemFromOrder = order.getOrderItems().get(order.getOrderItems().indexOf(item));
        itemFromOrder.setAppliedItemOffers(item.getAppliedItemOffers());
        itemFromOrder.setCandidateItemOffers(item.getCandidateItemOffers());
        itemFromOrder.setCategory(item.getCategory());
        itemFromOrder.setPersonalMessage(item.getPersonalMessage());
        itemFromOrder.setQuantity(item.getQuantity());

        order = updateOrder(order);

        return itemFromOrder;
    }

    @Override
    public List<OrderItem> updateItemsInOrder(Order order, List<OrderItem> orderItems) throws ItemNotFoundException, PricingException {
        ArrayList<OrderItem> response = new ArrayList<OrderItem>();
        for (OrderItem orderItem : orderItems) {
            OrderItem responseItem = updateItemInOrder(order, orderItem);
            response.add(responseItem);
        }
        return orderItems;
    }

    @Override
    public void removeAllFulfillmentGroupsFromOrder(Order order) throws PricingException {
        if (order.getFulfillmentGroups() != null) {
            for (Iterator<FulfillmentGroup> iterator = order.getFulfillmentGroups().iterator(); iterator.hasNext();) {
                FulfillmentGroup fulfillmentGroup = iterator.next();
                iterator.remove();
                fulfillmentGroupDao.delete(fulfillmentGroup);
            }
            updateOrder(order);
        }
    }

    @Override
    public void removeFulfillmentGroupFromOrder(Order order, FulfillmentGroup fulfillmentGroup) throws PricingException {
        order.getFulfillmentGroups().remove(fulfillmentGroup);
        fulfillmentGroupDao.delete(fulfillmentGroup);
        updateOrder(order);
    }

    @Override
    public Order removeOfferFromOrder(Order order, Offer offer) throws PricingException {
        order.getCandidateOffers().remove(offer);
        offerDao.delete(offer);
        order = updateOrder(order);
        return order;
    }

    @Override
    public Order removeAllOffersFromOrder(Order order) throws PricingException {
        Iterator<Offer> itr = order.getCandidateOffers().iterator();
        while(itr.hasNext()) {
            Offer offer = itr.next();
            itr.remove();
            offerDao.delete(offer);
        }
        order = updateOrder(order);
        return order;
    }

    @Override
    public void removeNamedOrderForCustomer(String name, Customer customer) {
        Order namedOrder = findNamedOrderForCustomer(name, customer);
        orderDao.delete(namedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Order confirmOrder(Order order) {
        // TODO Other actions needed to complete order
        // (such as calling something to make sure the order is fulfilled
        // somehow).
        // Code below is only a start.
        return orderDao.submitOrder(order);
    }

    @Override
    public void cancelOrder(Order order) {
        orderDao.delete(order);
    }

    @Override
    public List<PaymentInfo> readPaymentInfosForOrder(Order order) {
        return paymentInfoDao.readPaymentInfosForOrder(order);
    }

    public boolean isRollupOrderItems() {
        return rollupOrderItems;
    }

    public void setRollupOrderItems(boolean rollupOrderItems) {
        this.rollupOrderItems = rollupOrderItems;
    }

    protected OrderItem addOrderItemToOrder(Order order, OrderItem newOrderItem) throws PricingException {
        OrderItem addedItem;
        List<OrderItem> orderItems = order.getOrderItems();
        boolean containsItem = orderItems.contains(newOrderItem);
        if (rollupOrderItems && containsItem) {
            OrderItem itemFromOrder = orderItems.get(orderItems.indexOf(newOrderItem));
            itemFromOrder.setQuantity(itemFromOrder.getQuantity() + newOrderItem.getQuantity());
            addedItem = itemFromOrder;
        } else {
            if (containsItem) {
                OrderItem itemFromOrder = orderItems.get(orderItems.indexOf(newOrderItem));
                itemFromOrder.setQuantity(newOrderItem.getQuantity());
                addedItem = itemFromOrder;
            } else {
                orderItems.add(newOrderItem);
                newOrderItem.setOrder(order);
                addedItem = newOrderItem;
            }
        }

        //don't worry about fulfillment groups, since the phase for adding items occurs before shipping arrangements

        order = updateOrder(order);

        return order.getOrderItems().get(order.getOrderItems().indexOf(addedItem));
    }

    protected Order updateOrder(Order order) throws PricingException {
        pricingExecutionManager.executePricing(order);
        return orderDao.save(order);
    }

    protected Order persistOrder(Order order) {
        return orderDao.save(order);
    }

    protected FulfillmentGroup createDefaultFulfillmentGroup(Order order, Address address) {
        FulfillmentGroup newFg = fulfillmentGroupDao.createDefault();
        newFg.setOrder(order);
        newFg.setType(FulfillmentGroupType.DEFAULT);
        newFg.setAddress(address);

        return newFg;
    }

    protected FulfillmentGroupItem createFulfillmentGroupItemFromOrderItem(OrderItem orderItem, FulfillmentGroup fulfillmentGroup, int quantity) {
        FulfillmentGroupItem fgi = fulfillmentGroupItemDao.create();
        fgi.setFulfillmentGroup(fulfillmentGroup);
        fgi.setOrderItem(orderItem);
        fgi.setQuantity(quantity);
        return fgi;
    }

    protected void removeOrderItemFromFullfillmentGroup(Order order, OrderItem orderItem) {
        List<FulfillmentGroup> fulfillmentGroups = order.getFulfillmentGroups();
        for (FulfillmentGroup fulfillmentGroup : fulfillmentGroups) {
            Iterator<FulfillmentGroupItem> itr = fulfillmentGroup.getFulfillmentGroupItems().iterator();
            while(itr.hasNext()) {
                FulfillmentGroupItem fulfillmentGroupItem = itr.next();
                if(fulfillmentGroupItem.getOrderItem().equals(orderItem)) {
                    itr.remove();
                    fulfillmentGroupItemDao.delete(fulfillmentGroupItem);
                }
            }
        }
    }

    protected DiscreteOrderItemRequest createDiscreteOrderItemRequest(DiscreteOrderItem discreteOrderItem) {
        DiscreteOrderItemRequest itemRequest = new DiscreteOrderItemRequest();
        itemRequest.setCategory(discreteOrderItem.getCategory());
        itemRequest.setProduct(discreteOrderItem.getProduct());
        itemRequest.setQuantity(discreteOrderItem.getQuantity());
        itemRequest.setSku(discreteOrderItem.getSku());
        return itemRequest;
    }

    protected BundleOrderItemRequest createBundleOrderItemRequest(BundleOrderItem bundleOrderItem, List<DiscreteOrderItemRequest> discreteOrderItemRequests) {
        BundleOrderItemRequest bundleOrderItemRequest = new BundleOrderItemRequest();
        bundleOrderItemRequest.setCategory(bundleOrderItem.getCategory());
        bundleOrderItemRequest.setName(bundleOrderItem.getName());
        bundleOrderItemRequest.setQuantity(bundleOrderItem.getQuantity());
        bundleOrderItemRequest.setDiscreteOrderItems(discreteOrderItemRequests);
        return bundleOrderItemRequest;
    }

    public OrderDao getOrderDao() {
        return orderDao;
    }

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public PaymentInfoDao getPaymentInfoDao() {
        return paymentInfoDao;
    }

    public void setPaymentInfoDao(PaymentInfoDao paymentInfoDao) {
        this.paymentInfoDao = paymentInfoDao;
    }

    public FulfillmentGroupDao getFulfillmentGroupDao() {
        return fulfillmentGroupDao;
    }

    public void setFulfillmentGroupDao(FulfillmentGroupDao fulfillmentGroupDao) {
        this.fulfillmentGroupDao = fulfillmentGroupDao;
    }

    public FulfillmentGroupItemDao getFulfillmentGroupItemDao() {
        return fulfillmentGroupItemDao;
    }

    public void setFulfillmentGroupItemDao(FulfillmentGroupItemDao fulfillmentGroupItemDao) {
        this.fulfillmentGroupItemDao = fulfillmentGroupItemDao;
    }

    public PricingExecutionManager getPricingExecutionManager() {
        return pricingExecutionManager;
    }

    public void setPricingExecutionManager(PricingExecutionManager pricingExecutionManager) {
        this.pricingExecutionManager = pricingExecutionManager;
    }

    public OrderItemService getOrderItemService() {
        return orderItemService;
    }

    public void setOrderItemService(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

}
