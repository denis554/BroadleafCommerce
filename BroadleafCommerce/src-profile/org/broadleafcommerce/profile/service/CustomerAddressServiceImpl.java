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
package org.broadleafcommerce.profile.service;

import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.profile.dao.CustomerAddressDao;
import org.broadleafcommerce.profile.domain.CustomerAddress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("blCustomerAddressService")
public class CustomerAddressServiceImpl implements CustomerAddressService {

    @Resource
    protected CustomerAddressDao customerAddressDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddress saveCustomerAddress(CustomerAddress customerAddress) {
        // if parameter address is set as default, unset all other default addresses
        List<CustomerAddress> activeCustomerAddresses = readActiveCustomerAddressesByCustomerId(customerAddress.getCustomer().getId());
        if (activeCustomerAddresses.size() == 0) {
            customerAddress.getAddress().setDefault(true);
        } else {
            if (customerAddress.getAddress().isDefault()) {
                for (CustomerAddress activeCustomerAddress : activeCustomerAddresses) {
                    if (activeCustomerAddress.getId() != customerAddress.getId() && activeCustomerAddress.getAddress().isDefault()) {
                        activeCustomerAddress.getAddress().setDefault(false);
                        customerAddressDao.save(activeCustomerAddress);
                    }
                }
            }
        }
        return customerAddressDao.save(customerAddress);
    }

    public List<CustomerAddress> readActiveCustomerAddressesByCustomerId(Long customerId) {
        return customerAddressDao.readActiveCustomerAddressesByCustomerId(customerId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddress readCustomerAddressById(Long customerAddressId) {
        return customerAddressDao.readCustomerAddressById(customerAddressId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void makeCustomerAddressDefault(Long customerAddressId, Long customerId) {
        customerAddressDao.makeCustomerAddressDefault(customerAddressId, customerId);
    }

    public void deleteCustomerAddressById(Long customerAddressId){
        customerAddressDao.deleteCustomerAddressById(customerAddressId);
    }

    @Override
    public CustomerAddress findDefaultCustomerAddress(Long customerId) {
        return customerAddressDao.findDefaultCustomerAddress(customerId);
    }

    @Override
    public CustomerAddress create() {
        return customerAddressDao.create();
    }
}