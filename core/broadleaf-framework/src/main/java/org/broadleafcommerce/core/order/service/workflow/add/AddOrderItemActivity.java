/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.order.service.workflow.add;

import org.broadleafcommerce.common.dao.GenericEntityDao;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductBundle;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.DiscreteOrderItemRequest;
import org.broadleafcommerce.core.order.service.call.NonDiscreteOrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.call.OrderItemRequest;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.call.ProductBundleOrderItemRequest;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;

import javax.annotation.Resource;

public class AddOrderItemActivity extends BaseActivity<ProcessContext<CartOperationRequest>> {
    
    @Resource(name = "blOrderService")
    protected OrderService orderService;
    
    @Resource(name = "blOrderItemService")
    protected OrderItemService orderItemService;
    
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Resource(name = "blGenericEntityDao")
    protected GenericEntityDao genericEntityDao;

    @Override
    public ProcessContext<CartOperationRequest> execute(ProcessContext<CartOperationRequest> context) throws Exception {
        CartOperationRequest request = context.getSeedData();
        OrderItemRequestDTO orderItemRequestDTO = request.getItemRequest();

        // Order has been verified in a previous activity -- the values in the request can be trusted
        Order order = request.getOrder();
        
        Sku sku = null;
        if (orderItemRequestDTO.getSkuId() != null) {
            sku = catalogService.findSkuById(orderItemRequestDTO.getSkuId());
        }
        
        Product product = null;
        if (orderItemRequestDTO.getProductId() != null) {
            product = catalogService.findProductById(orderItemRequestDTO.getProductId());
        }
        
        Category category = null;
        if (orderItemRequestDTO.getCategoryId() != null) {
            category = catalogService.findCategoryById(orderItemRequestDTO.getCategoryId());
        } 

        if (category == null && product != null) {
            category = product.getDefaultCategory();
        }

        OrderItem item;
        if (orderItemRequestDTO instanceof NonDiscreteOrderItemRequestDTO) {
            NonDiscreteOrderItemRequestDTO ndr = (NonDiscreteOrderItemRequestDTO) orderItemRequestDTO;
            OrderItemRequest itemRequest = new OrderItemRequest();
            itemRequest.setQuantity(ndr.getQuantity());
            itemRequest.setRetailPriceOverride(ndr.getOverrideRetailPrice());
            itemRequest.setSalePriceOverride(ndr.getOverrideSalePrice());
            itemRequest.setItemName(ndr.getItemName());
            itemRequest.setOrder(order);
            item = orderItemService.createOrderItem(itemRequest);
        } else if (product == null || !(product instanceof ProductBundle)) {
            DiscreteOrderItemRequest itemRequest = new DiscreteOrderItemRequest();
            itemRequest.setCategory(category);
            itemRequest.setProduct(product);
            itemRequest.setSku(sku);
            itemRequest.setQuantity(orderItemRequestDTO.getQuantity());
            itemRequest.setItemAttributes(orderItemRequestDTO.getItemAttributes());
            itemRequest.setOrder(order);
            itemRequest.setSalePriceOverride(orderItemRequestDTO.getOverrideSalePrice());
            itemRequest.setRetailPriceOverride(orderItemRequestDTO.getOverrideRetailPrice());
            item = orderItemService.createDiscreteOrderItem(itemRequest);
        } else {
            ProductBundleOrderItemRequest bundleItemRequest = new ProductBundleOrderItemRequest();
            bundleItemRequest.setCategory(category);
            bundleItemRequest.setProductBundle((ProductBundle) product);
            bundleItemRequest.setSku(sku);
            bundleItemRequest.setQuantity(orderItemRequestDTO.getQuantity());
            bundleItemRequest.setItemAttributes(orderItemRequestDTO.getItemAttributes());
            bundleItemRequest.setName(product.getName());
            bundleItemRequest.setOrder(order);
            bundleItemRequest.setSalePriceOverride(orderItemRequestDTO.getOverrideSalePrice());
            bundleItemRequest.setRetailPriceOverride(orderItemRequestDTO.getOverrideRetailPrice());
            item = orderItemService.createBundleOrderItem(bundleItemRequest, false);
        }

        if (orderItemRequestDTO.getParentOrderItemId() != null) {
            OrderItem parent = orderItemService.readOrderItemById(orderItemRequestDTO.getParentOrderItemId());
            item.setParentOrderItem(parent);
        }
        
        order.getOrderItems().add(item);
        request.setOrderItem(item);

        if (!request.isPriceOrder()) {
            //persist the newly created order if we're not going through the pricing flow. This helps with proper
            //fulfillment group association
            genericEntityDao.persist(item);
        }

        return context;
    }

}
