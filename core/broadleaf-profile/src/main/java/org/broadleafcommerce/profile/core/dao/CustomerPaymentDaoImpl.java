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

package org.broadleafcommerce.profile.core.dao;

import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.profile.core.domain.CustomerPayment;
import org.broadleafcommerce.profile.core.domain.CustomerPaymentImpl;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository("blCustomerPaymentDao")
public class CustomerPaymentDaoImpl implements CustomerPaymentDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Override
    public List<CustomerPayment> readCustomerPaymentsByCustomerId(Long customerId) {
        Query query = em.createNamedQuery("BC_READ_CUSTOMER_PAYMENTS_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    public CustomerPayment save(CustomerPayment customerPayment) {
        return em.merge(customerPayment);
    }

    @Override
    public CustomerPayment readCustomerPaymentById(Long customerPaymentId) {
        return (CustomerPayment) em.find(CustomerPaymentImpl.class, customerPaymentId);
    }

    @Override
    public void deleteCustomerPaymentById(Long customerPaymentId) {
        CustomerPayment customerPayment = readCustomerPaymentById(customerPaymentId);
        if (customerPayment != null) {
            em.remove(customerPayment);
        }
    }

    @Override
    public CustomerPayment create() {
        return (CustomerPayment) entityConfiguration.createEntityInstance(CustomerPayment.class.getName());
    }

}
