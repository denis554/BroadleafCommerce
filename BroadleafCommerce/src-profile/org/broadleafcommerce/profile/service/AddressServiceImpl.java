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

import javax.annotation.Resource;

import org.broadleafcommerce.profile.dao.AddressDao;
import org.broadleafcommerce.profile.domain.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("blAddressService")
public class AddressServiceImpl implements AddressService {

    @Resource
    protected AddressDao addressDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public Address saveAddress(Address address) {
        return addressDao.save(address);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Address readAddressById(Long addressId) {
        return addressDao.readAddressById(addressId);
    }
}