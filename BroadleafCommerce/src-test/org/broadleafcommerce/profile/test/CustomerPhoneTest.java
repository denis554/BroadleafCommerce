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
package org.broadleafcommerce.profile.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.domain.CustomerPhone;
import org.broadleafcommerce.profile.service.CustomerPhoneService;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.profile.test.dataprovider.CustomerPhoneDataProvider;
import org.broadleafcommerce.test.integration.BaseTest;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class CustomerPhoneTest extends BaseTest {
    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    List<Long> customerPhoneIds = new ArrayList<Long>();
    String userName = new String();
    Long userId;

    @Resource
    private CustomerPhoneService customerPhoneService;

    @Resource
    private CustomerService customerService;

    @Test(groups = "createCustomerPhone", dataProvider = "setupCustomerPhone", dataProviderClass = CustomerPhoneDataProvider.class, dependsOnGroups = "readCustomer1")
    @Rollback(false)
    public void createCustomerPhone(CustomerPhone customerPhone) {
        userName = "customer1";
        Customer customer = customerService.readCustomerByUsername(userName);
        assert customerPhone.getId() == null;
        customerPhone.setCustomerId(customer.getId());
        customerPhone = customerPhoneService.saveCustomerPhone(customerPhone);
        assert customer.getId() == customerPhone.getCustomerId();
        userId = customerPhone.getCustomerId();
    }

    @Test(groups = "readCustomerPhone", dependsOnGroups = "createCustomerPhone")
    public void readCustomerPhoneByUserId() {
        List<CustomerPhone> customerPhoneList = customerPhoneService.readActiveCustomerPhonesByCustomerId(userId);
        for (CustomerPhone customerPhone : customerPhoneList) {
            assert customerPhone != null;
        }
    }
}
