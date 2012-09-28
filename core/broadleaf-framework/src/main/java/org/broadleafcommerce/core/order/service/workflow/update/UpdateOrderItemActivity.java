/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.order.service.workflow.update;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.offer.service.OrderItemMergeService;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.exception.ItemNotFoundException;
import org.broadleafcommerce.core.order.service.workflow.CartOperationContext;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class UpdateOrderItemActivity extends BaseActivity {
    private static Log LOG = LogFactory.getLog(UpdateOrderItemActivity.class);
    
    @Resource(name = "blOrderService")
    protected OrderService orderService;

    @Resource(name = "blOrderItemMergeService")
    protected OrderItemMergeService orderItemMergeService;

    @Override
    public ProcessContext execute(ProcessContext context) throws Exception {
        CartOperationRequest request = ((CartOperationContext) context).getSeedData();
        OrderItemRequestDTO orderItemRequestDTO = request.getItemRequest();
        Order order = request.getOrder();

        orderItemMergeService.gatherSplitItemsInBundles(order);
        
    	OrderItem orderItem = null;
		for (OrderItem oi : order.getOrderItems()) {
			if (oi.getId().equals(orderItemRequestDTO.getOrderItemId())) {
				orderItem = oi;
			}
		}
		
        if (orderItem == null || !order.getOrderItems().contains(orderItem)) {
            throw new ItemNotFoundException("Order Item (" + orderItem.getId() + ") not found in Order (" + order.getId() + ")");
        }
        
        OrderItem itemFromOrder = order.getOrderItems().get(order.getOrderItems().indexOf(orderItem));
        
        request.setOrderItemQuantityDelta(orderItemRequestDTO.getQuantity() - itemFromOrder.getQuantity());
        
        Integer oldQuantity = itemFromOrder.getQuantity();
        itemFromOrder.setQuantity(orderItemRequestDTO.getQuantity());
        
        if (itemFromOrder instanceof BundleOrderItem) {
            Map<Sku, Integer> libraryQty = new HashMap<Sku, Integer>();
            for (DiscreteOrderItem doi : ((BundleOrderItem) itemFromOrder).getDiscreteOrderItems()) {
                if (!libraryQty.containsKey(doi.getSku())) {
                    libraryQty.put(doi.getSku(), 0);
                }
                libraryQty.put(doi.getSku(), libraryQty.get(doi.getSku()) + doi.getQuantity());
            }
            for (Map.Entry<Sku, Integer> entry : libraryQty.entrySet()) {
                entry.setValue((entry.getValue() / oldQuantity) * orderItemRequestDTO.getQuantity());
            }
            for (DiscreteOrderItem doi : ((BundleOrderItem) itemFromOrder).getDiscreteOrderItems()) {
                //put all of the new qty in the first instance of the sku (this is a split sku in a bundle)
                Integer newQty = libraryQty.get(doi.getSku());
                doi.setQuantity(newQty);
                libraryQty.put(doi.getSku(), 0);
            }
        }

        order = orderService.save(order, false);
        
        request.setAddedOrderItem(itemFromOrder);
        request.setOrder(order);
        return context;
    }

}
