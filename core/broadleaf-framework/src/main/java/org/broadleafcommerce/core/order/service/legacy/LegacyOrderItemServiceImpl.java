/*
 * Copyright 2008-2009 the original author or authors.
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

package org.broadleafcommerce.core.order.service.legacy;

import org.broadleafcommerce.core.catalog.service.dynamic.DynamicSkuPrices;
import org.broadleafcommerce.core.catalog.service.dynamic.DynamicSkuPricingService;
import org.broadleafcommerce.core.order.dao.OrderItemDao;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItemFeePrice;
import org.broadleafcommerce.core.order.domain.GiftWrapOrderItem;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.order.domain.OrderItemAttributeImpl;
import org.broadleafcommerce.core.order.domain.PersonalMessage;
import org.broadleafcommerce.core.order.service.OrderItemServiceImpl;
import org.broadleafcommerce.core.order.service.call.legacy.LegacyBundleOrderItemRequest;
import org.broadleafcommerce.core.order.service.call.legacy.LegacyDiscreteOrderItemRequest;
import org.broadleafcommerce.core.order.service.call.legacy.LegacyGiftWrapOrderItemRequest;
import org.broadleafcommerce.core.order.service.call.legacy.LegacyOrderItemRequest;
import org.broadleafcommerce.core.order.service.type.OrderItemType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

@Service("blLegacyOrderItemService")
public class LegacyOrderItemServiceImpl extends OrderItemServiceImpl implements LegacyOrderItemService {

    @Resource(name="blOrderItemDao")
    protected OrderItemDao orderItemDao;
    
    @Resource(name="blDynamicSkuPricingService")
    protected DynamicSkuPricingService dynamicSkuPricingService;

    public OrderItem readOrderItemById(final Long orderItemId) {
        return orderItemDao.readOrderItemById(orderItemId);
    }

    public OrderItem saveOrderItem(final OrderItem orderItem) {
        return orderItemDao.saveOrderItem(orderItem);
    }
    
    protected void populateDiscreteOrderItem(DiscreteOrderItem item, LegacyOrderItemRequest itemRequest) {
        item.setSku(itemRequest.getSku());
        item.setQuantity(itemRequest.getQuantity());
        item.setCategory(itemRequest.getCategory());
        item.setProduct(itemRequest.getProduct());
        
        if (itemRequest.getItemAttributes() != null && itemRequest.getItemAttributes().size() > 0) {
            Map<String,OrderItemAttribute> orderItemAttributes = new HashMap<String,OrderItemAttribute>();
            item.setOrderItemAttributes(orderItemAttributes);
            
            for (String key : itemRequest.getItemAttributes().keySet()) {
                String value = itemRequest.getItemAttributes().get(key);
                OrderItemAttribute attribute = new OrderItemAttributeImpl();
                attribute.setName(key);
                attribute.setValue(value);
                attribute.setOrderItem(item);
                orderItemAttributes.put(key, attribute);
            }
        }
    }

    public DiscreteOrderItem createDiscreteOrderItem(final LegacyDiscreteOrderItemRequest itemRequest) {
        final DiscreteOrderItem item = (DiscreteOrderItem) orderItemDao.create(OrderItemType.DISCRETE);
        populateDiscreteOrderItem(item, itemRequest);
        item.setBaseSalePrice(itemRequest.getSku().getSalePrice());
        item.setBaseRetailPrice(itemRequest.getSku().getRetailPrice());
        item.setDiscreteOrderItemFeePrices(itemRequest.getDiscreteOrderItemFeePrices());
        for (DiscreteOrderItemFeePrice feePrice : item.getDiscreteOrderItemFeePrices()) {
            feePrice.setDiscreteOrderItem(item);
        }

        item.updatePrices();
        item.assignFinalPrice();
        item.setPersonalMessage(itemRequest.getPersonalMessage());

        return item;
    }

    public DiscreteOrderItem createDiscreteOrderItem(final LegacyOrderItemRequest itemRequest) {
        final DiscreteOrderItem item = (DiscreteOrderItem) orderItemDao.create(OrderItemType.DISCRETE);
        populateDiscreteOrderItem(item, itemRequest);
        item.setBaseSalePrice(itemRequest.getSku().getSalePrice());
        item.setBaseRetailPrice(itemRequest.getSku().getRetailPrice());
        item.updatePrices();
        item.assignFinalPrice();
        item.setPersonalMessage(itemRequest.getPersonalMessage());

        return item;
    }
    
    public DiscreteOrderItem createDynamicPriceDiscreteOrderItem(final LegacyDiscreteOrderItemRequest itemRequest, @SuppressWarnings("rawtypes") HashMap skuPricingConsiderations) {
        final DiscreteOrderItem item = (DiscreteOrderItem) orderItemDao.create(OrderItemType.EXTERNALLY_PRICED);
        populateDiscreteOrderItem(item, itemRequest);

        DynamicSkuPrices prices = dynamicSkuPricingService.getSkuPrices(itemRequest.getSku(), skuPricingConsiderations);
        item.setBaseRetailPrice(prices.getRetailPrice());
        item.setBaseSalePrice(prices.getSalePrice());
        item.setSalePrice(prices.getSalePrice());
        item.setRetailPrice(prices.getRetailPrice());
        item.setDiscreteOrderItemFeePrices(itemRequest.getDiscreteOrderItemFeePrices());
        for (DiscreteOrderItemFeePrice fee : itemRequest.getDiscreteOrderItemFeePrices()) {
        	item.setSalePrice(item.getSalePrice().add(fee.getAmount()));
        	item.setRetailPrice(item.getRetailPrice().add(fee.getAmount()));
        }

        item.assignFinalPrice();
        item.setPersonalMessage(itemRequest.getPersonalMessage());

        return item;
    }

    public GiftWrapOrderItem createGiftWrapOrderItem(final LegacyGiftWrapOrderItemRequest itemRequest) {
        final GiftWrapOrderItem item = (GiftWrapOrderItem) orderItemDao.create(OrderItemType.GIFTWRAP);
        item.setSku(itemRequest.getSku());
        item.setQuantity(itemRequest.getQuantity());
        item.setCategory(itemRequest.getCategory());
        item.setProduct(itemRequest.getProduct());
        item.setBaseSalePrice(itemRequest.getSku().getSalePrice());
        item.setBaseRetailPrice(itemRequest.getSku().getRetailPrice());
        item.setDiscreteOrderItemFeePrices(itemRequest.getDiscreteOrderItemFeePrices());
        item.updatePrices();
        item.assignFinalPrice();
        item.getWrappedItems().addAll(itemRequest.getWrappedItems());
        for (OrderItem orderItem : item.getWrappedItems()) {
            orderItem.setGiftWrapOrderItem(item);
        }

        return item;
    }

    public BundleOrderItem createBundleOrderItem(final LegacyBundleOrderItemRequest itemRequest) {
        final BundleOrderItem item = (BundleOrderItem) orderItemDao.create(OrderItemType.BUNDLE);
        item.setQuantity(itemRequest.getQuantity());
        item.setCategory(itemRequest.getCategory());
        item.setName(itemRequest.getName());
        item.setBundleOrderItemFeePrices(itemRequest.getBundleOrderItemFeePrices());

        for (LegacyDiscreteOrderItemRequest discreteItemRequest : itemRequest.getDiscreteOrderItems()) {
            DiscreteOrderItem discreteOrderItem;
            if (discreteItemRequest instanceof LegacyGiftWrapOrderItemRequest) {
                discreteOrderItem = createGiftWrapOrderItem((LegacyGiftWrapOrderItemRequest) discreteItemRequest);
            } else {
                discreteOrderItem = createDiscreteOrderItem(discreteItemRequest);
            }
            discreteOrderItem.setBundleOrderItem(item);
            item.getDiscreteOrderItems().add(discreteOrderItem);
            item.assignFinalPrice();
        }

        return item;
    }

    public void delete(final OrderItem item) {
        orderItemDao.delete(item);
    }
    
    public PersonalMessage createPersonalMessage() {
        return orderItemDao.createPersonalMessage();
    }
}
