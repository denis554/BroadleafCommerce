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
package org.broadleafcommerce.profile.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.profile.domain.CustomerPhone;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blCustomerPhoneDao")
public class CustomerPhoneDaoImpl implements CustomerPhoneDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @SuppressWarnings("unchecked")
    public List<CustomerPhone> readActiveCustomerPhonesByCustomerId(Long customerId) {
        Query query = em.createNamedQuery("BC_READ_ACTIVE_CUSTOMER_PHONES_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public CustomerPhone save(CustomerPhone customerPhone) {
        if (customerPhone.getId() == null) {
            em.persist(customerPhone);
        } else {
            customerPhone = em.merge(customerPhone);
        }
        return customerPhone;
    }

    @SuppressWarnings("unchecked")
    public CustomerPhone readCustomerPhoneById(Long customerPhoneId) {
        return (CustomerPhone) em.find(entityConfiguration.lookupEntityClass(CustomerPhone.class.getName()), customerPhoneId);
    }

    public void makeCustomerPhoneDefault(Long customerPhoneId, Long customerId) {
        List<CustomerPhone> customerPhones = readActiveCustomerPhonesByCustomerId(customerId);
        for (CustomerPhone customerPhone : customerPhones) {
            customerPhone.getPhone().setDefault(customerPhone.getId().equals(customerPhoneId));
            em.merge(customerPhone);
        }
    }

    public void deleteCustomerPhoneById(Long customerPhoneId) {
        CustomerPhone customerPhone = readCustomerPhoneById(customerPhoneId);
        if (customerPhone != null) {
            em.remove(customerPhone);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CustomerPhone findDefaultCustomerPhone(Long customerId) {
        Query query = em.createNamedQuery("BC_FIND_DEFAULT_PHONE_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        List<CustomerPhone> customerPhones = query.getResultList();
        return customerPhones.isEmpty() ? null : customerPhones.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CustomerPhone> readAllCustomerPhonesByCustomerId(Long customerId) {
        Query query = em.createNamedQuery("BC_READ_ALL_CUSTOMER_PHONES_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public CustomerPhone create() {
        return (CustomerPhone) entityConfiguration.createEntityInstance(CustomerPhone.class.getName());
    }
}
