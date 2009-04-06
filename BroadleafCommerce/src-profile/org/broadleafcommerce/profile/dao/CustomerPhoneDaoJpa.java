package org.broadleafcommerce.profile.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.CustomerPhone;
import org.springframework.stereotype.Repository;

@Repository("customerPhoneDao")
public class CustomerPhoneDaoJpa implements CustomerPhoneDao {

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @PersistenceContext(unitName = "blPU")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<CustomerPhone> readActiveCustomerPhonesByCustomerId(Long customerId) {
        Query query = em.createNamedQuery("BC_READ_ACTIVE_CUSTOMER_PHONES_BY_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public CustomerPhone maintainCustomerPhone(CustomerPhone customerPhone) {
        if (customerPhone.getId() == null) {
            em.persist(customerPhone);
        } else {
            customerPhone = em.merge(customerPhone);
        }
        return customerPhone;
    }

    @SuppressWarnings("unchecked")
    public CustomerPhone readCustomerPhoneByIdAndCustomerId(Long customerPhoneId, Long customerId) {
        Query query = em.createNamedQuery("BC_READ_CUSTOMER_PHONE_BY_ID_AND_CUSTOMER_ID");
        query.setParameter("customerId", customerId);
        query.setParameter("customerPhoneId", customerPhoneId);
        List<CustomerPhone> customerPhones = query.getResultList();
        return customerPhones.isEmpty() ? null : customerPhones.get(0);
    }

    public void makeCustomerPhoneDefault(Long customerPhoneId, Long customerId) {
        List<CustomerPhone> customerPhones = readActiveCustomerPhonesByCustomerId(customerId);
        for (CustomerPhone customerPhone : customerPhones) {
            customerPhone.getPhone().setDefault(customerPhone.getId().equals(customerPhoneId));
            em.merge(customerPhone);
        }
    }

    public void deleteCustomerPhoneByIdAndCustomerId(Long customerPhoneId, Long customerId) {
        // TODO: determine if hard delete or deactivate, and consider throwing exception if read fails
        CustomerPhone customerPhone = readCustomerPhoneByIdAndCustomerId(customerPhoneId, customerId);
        em.remove(customerPhone.getId());
    }
}
