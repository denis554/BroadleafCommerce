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
package org.broadleafcommerce.core.order.dao;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.broadleafcommerce.core.order.domain.GiftWrapOrderItem;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemImpl;
import org.broadleafcommerce.core.order.domain.PersonalMessage;
import org.broadleafcommerce.core.order.service.type.OrderItemType;
import org.broadleafcommerce.persistence.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blOrderItemDao")
public class OrderItemDaoImpl implements OrderItemDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    public OrderItem save(final OrderItem orderItem) {
        return em.merge(orderItem);
    }

    public OrderItem readOrderItemById(final Long orderItemId) {
        return em.find(OrderItemImpl.class, orderItemId);
    }

    public void delete(OrderItem orderItem) {
    	if (!em.contains(orderItem)) {
    		orderItem = readOrderItemById(orderItem.getId());
    	}
        if (GiftWrapOrderItem.class.isAssignableFrom(orderItem.getClass())) {
            final GiftWrapOrderItem giftItem = (GiftWrapOrderItem) orderItem;
            for (OrderItem wrappedItem : giftItem.getWrappedItems()) {
                wrappedItem.setGiftWrapOrderItem(null);
                wrappedItem = save(wrappedItem);
            }
        }
        em.remove(orderItem);
    }

    public OrderItem create(final OrderItemType orderItemType) {
        final OrderItem item = (OrderItem) entityConfiguration.createEntityInstance(orderItemType.getType());
        item.setOrderItemType(orderItemType);
        return item;
    }
    
    public PersonalMessage createPersonalMessage() {
        PersonalMessage personalMessage = (PersonalMessage) entityConfiguration.createEntityInstance(PersonalMessage.class.getName());
        return personalMessage;
    }

    public OrderItem saveOrderItem(final OrderItem orderItem) {
        return em.merge(orderItem);
    }
}
