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

import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.domain.CustomerImpl;
import org.broadleafcommerce.profile.domain.State;
import org.broadleafcommerce.profile.domain.StateImpl;
import org.broadleafcommerce.profile.service.AddressStandardizationServiceImpl;
import org.broadleafcommerce.profile.service.addressValidation.AddressStandarizationResponse;
import org.broadleafcommerce.test.integration.BaseTest;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class USPSAddressStandardizationServiceTest extends BaseTest {
    @Resource
    private AddressStandardizationServiceImpl addressStandardizationService;
    boolean uspsError = true;

    protected void addressVerificationSetUp() throws Exception {
        addressStandardizationService = new AddressStandardizationServiceImpl();
        addressStandardizationService.setUspsCharSet("UTF-8");
        addressStandardizationService.setUspsServiceAPI("/ShippingAPITest.dll");
        addressStandardizationService.setUspsServerName("testing.shippingapis.com");
        // TODO: Testing server user name and password
        addressStandardizationService.setUspsUserName("482CREDE3966");
        addressStandardizationService.setUspsPassword("338MC69CR570");

        /*
         * AddressStandardAbbreviations abbr = new AddressStandardAbbreviations(); abbr.setAbbreviationPropertyFile(new ClassPathResource("address.abbreviations.properties", USPSAddressStandardizationServiceTest.class)); uspsService.setAbbreviations(abbr);
         */
        super.setup();
    }

    private Address getValidAddress() {
        // TODO: For USPS test server, only certain addresses would work. Rest will throw an error
        Customer customer = new CustomerImpl();
        customer.setId(new Long(7427));
        customer.setUsername("customer1");
        customer.setPassword("customer1");
        Address addr = new AddressImpl();
        addr.setAddressLine1("6406 Ivy Lane");
        addr.setCity("Greenbelt");
        State state = new StateImpl();
        state.setAbbreviation("MD");
        addr.setState(state);

        return addr;
    }

    @Test(groups = { "testBadAddress" })
    @Rollback(false)
    public void testBadAddress() {
        try {
            addressVerificationSetUp();
            Address testAddress = getValidAddress();
            testAddress.setPostalCode("70057");
            State state = new StateImpl();
            state.setAbbreviation("CL");
            testAddress.setState(state);

            AddressStandarizationResponse standardizedResponse = addressStandardizationService.standardizeAddress(testAddress);
            if (standardizedResponse.isErrorDetected()) {
                logger.debug("USPS address verification Failed. Please check the address and try again");
                assert true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test(groups = { "testSuccessfulAddress" })
    @Rollback(false)
    public void testSuccessfulAddress() {
        try {
            addressVerificationSetUp();
            Address testAddress = getValidAddress();
            AddressStandarizationResponse standardizedResponse = addressStandardizationService.standardizeAddress(testAddress);
            logger.debug("Get ZipCode: " + standardizedResponse.getAddress().getPostalCode());
            if (!standardizedResponse.isErrorDetected()) {
                assert true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

    }
}
