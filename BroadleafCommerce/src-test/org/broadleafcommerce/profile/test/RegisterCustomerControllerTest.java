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

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.profile.test.dataprovider.RegisterCustomerDataProvider;
import org.broadleafcommerce.profile.web.controller.RegisterCustomerController;
import org.broadleafcommerce.profile.web.form.CustomerRegistrationForm;
import org.broadleafcommerce.test.integration.BaseTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.testng.annotations.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class RegisterCustomerControllerTest extends BaseTest {
    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @Resource
    private RegisterCustomerController registerCustomerController;

    @Resource
    private CustomerService customerService;

    @Test(groups = "createCustomerFromController", dataProvider = "setupCustomerControllerData", dataProviderClass = RegisterCustomerDataProvider.class)
    @Rollback(false)
    public void createCustomerFromController(CustomerRegistrationForm registerCustomer) {
        BindingResult errors = new BeanPropertyBindingResult(registerCustomer, "registerCustomer");
        //MockHttpServletRequest request = new MockHttpServletRequest();

        GreenMail greenMail = new GreenMail(
                new ServerSetup[] {
                        new ServerSetup(30000, "127.0.0.1", ServerSetup.PROTOCOL_SMTP)
                }
        );
        greenMail.start();
        //registerCustomerController.saveCustomer(registerCustomer, errors, request);
        greenMail.stop();

        assert(errors.getErrorCount() == 0);
        //Customer customerFromDb = customerService.readCustomerByUsername(registerCustomer.getUsername());
        //assert(customerFromDb != null);
    }

    @Test(groups = "viewRegisterCustomerFromController")
    public void viewRegisterCustomerFromController() {
        String view = registerCustomerController.viewForm();
        assert (view.equals("registerCustomer"));
    }

}
