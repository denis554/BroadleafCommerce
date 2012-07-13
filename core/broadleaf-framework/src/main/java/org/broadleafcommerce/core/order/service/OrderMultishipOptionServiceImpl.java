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

import org.broadleafcommerce.core.order.dao.OrderMultishipOptionDao;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderMultishipOption;
import org.broadleafcommerce.core.order.domain.OrderMultishipOptionImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Andre Azzolini (apazzolini)
 */
@Service("blOrderMultishipOptionService")
public class OrderMultishipOptionServiceImpl implements OrderMultishipOptionService {

    @Resource(name = "blOrderMultishipOptionDao")
    OrderMultishipOptionDao orderMultishipOptionDao;

	@Override
	public OrderMultishipOption save(OrderMultishipOption orderMultishipOption) {
        return orderMultishipOptionDao.save(orderMultishipOption);
    }

	@Override
	public List<OrderMultishipOption> findOrderMultishipOptions(Long orderId) {
        return orderMultishipOptionDao.readOrderMultishipOptions(orderId);
    }
    
	@Override
	public OrderMultishipOption create() {
        return orderMultishipOptionDao.create();
    }
	
	@Override
	public List<OrderMultishipOption> generateMultishipOptions(Order order) {
		List<OrderMultishipOption> orderMultishipOptions = new ArrayList<OrderMultishipOption>();
		for (DiscreteOrderItem discreteOrderItem : order.getDiscreteOrderItems()) {
			for (int i = 0; i < discreteOrderItem.getQuantity(); i++) {
				OrderMultishipOption orderMultishipOption = new OrderMultishipOptionImpl();
				orderMultishipOption.setOrder(order);
				orderMultishipOption.setOrderItem(discreteOrderItem);
				orderMultishipOptions.add(orderMultishipOption);
			}
		}
		
		return orderMultishipOptions;
	}
}
