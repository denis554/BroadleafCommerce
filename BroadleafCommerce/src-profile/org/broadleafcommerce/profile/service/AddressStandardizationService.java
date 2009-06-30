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

import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.service.addressValidation.AddressStandarizationResponse;
import org.broadleafcommerce.profile.service.addressValidation.ServiceDownResponse;

public interface AddressStandardizationService extends ServiceDownResponse {

    public AddressStandarizationResponse standardizeAddress(Address addr);

    public void standardizeAndTokenizeAddress(Address address);

    public void tokenizeAddress(Address addr, boolean isStandardized);

    public String getUspsCharSet();

    public void setUspsCharSet(String uspsCharSet);

    public String getUspsPassword();

    public void setUspsPassword(String uspsPassword);

    public String getUspsServerName();

    public void setUspsServerName(String uspsServerName);

    public String getUspsServiceAPI();

    public void setUspsServiceAPI(String uspsServiceAPI);

    public String getUspsUserName();

    public void setUspsUserName(String uspsUserName);
}
