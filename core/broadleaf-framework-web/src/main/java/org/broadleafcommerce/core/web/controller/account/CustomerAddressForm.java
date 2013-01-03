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

package org.broadleafcommerce.core.web.controller.account;

import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.AddressImpl;
import org.broadleafcommerce.profile.core.domain.Phone;
import org.broadleafcommerce.profile.core.domain.PhoneImpl;

import java.io.Serializable;

public class CustomerAddressForm implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Address address = new AddressImpl();
    protected Phone phonePrimary = new PhoneImpl();
	protected String addressName;
	protected Long customerAddressId;
	
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public String getAddressName() {
		return addressName;
	}
	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}
	public Long getCustomerAddressId() {
		return customerAddressId;
	}
	public void setCustomerAddressId(Long customerAddressId) {
		this.customerAddressId = customerAddressId;
	}
    public Phone getPhonePrimary() {
        return phonePrimary;
    }
    public void setPhonePrimary(Phone phonePrimary) {
        this.phonePrimary = phonePrimary;
    }
}
