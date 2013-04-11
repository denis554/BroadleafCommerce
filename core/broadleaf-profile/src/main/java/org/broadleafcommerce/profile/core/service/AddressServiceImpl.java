/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.profile.core.service;

import javax.annotation.Resource;

import org.broadleafcommerce.profile.core.dao.AddressDao;
import org.broadleafcommerce.profile.core.domain.Address;
import org.springframework.stereotype.Service;

@Service("blAddressService")
public class AddressServiceImpl implements AddressService {

    @Resource(name="blAddressDao")
    protected AddressDao addressDao;

    public Address saveAddress(Address address) {
        return addressDao.save(address);
    }

    public Address readAddressById(Long addressId) {
        return addressDao.readAddressById(addressId);
    }

    public Address create() {
        return addressDao.create();
    }

    public void delete(Address address) {
        addressDao.delete(address);
    }
}