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
package org.broadleafcommerce.order.dao;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.order.domain.FulfillmentGroup;
import org.broadleafcommerce.order.domain.FulfillmentGroupImpl;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.service.type.FulfillmentGroupType;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blFulfillmentGroupDao")
public class FulfillmentGroupDaoImpl implements FulfillmentGroupDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    public FulfillmentGroup save(FulfillmentGroup fulfillmentGroup) {
        return em.merge(fulfillmentGroup);
    }

    @SuppressWarnings("unchecked")
    public FulfillmentGroup readFulfillmentGroupById(Long fulfillmentGroupId) {
        return (FulfillmentGroup) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.order.domain.FulfillmentGroup"), fulfillmentGroupId);
    }

    public FulfillmentGroupImpl readDefaultFulfillmentGroupForOrder(Order order) {
        Query query = em.createNamedQuery("BC_READ_DEFAULT_FULFILLMENT_GROUP_BY_ORDER_ID");
        query.setParameter("orderId", order.getId());
        FulfillmentGroupImpl result;
        try {
            result = (FulfillmentGroupImpl) query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }

        return result;
    }

    public void delete(FulfillmentGroup fulfillmentGroup) {
    	if (!em.contains(fulfillmentGroup)) {
    		fulfillmentGroup = readFulfillmentGroupById(fulfillmentGroup.getId());
    	}
        em.remove(fulfillmentGroup);
    }

    public FulfillmentGroup createDefault() {
        FulfillmentGroup fg = ((FulfillmentGroup) entityConfiguration.createEntityInstance("org.broadleafcommerce.order.domain.FulfillmentGroup"));
        fg.setPrimary(true);
        fg.setType(FulfillmentGroupType.SHIPPING);
        return fg;
    }

    public FulfillmentGroup create() {
        FulfillmentGroup fg =  ((FulfillmentGroup) entityConfiguration.createEntityInstance("org.broadleafcommerce.order.domain.FulfillmentGroup"));
        fg.setType(FulfillmentGroupType.SHIPPING);
        return fg;
    }
}