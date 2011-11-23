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

package org.broadleafcommerce.profile.core.service;

import org.broadleafcommerce.openadmin.server.security.util.PasswordChange;
import org.broadleafcommerce.openadmin.server.security.util.PasswordReset;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.handler.PasswordUpdatedHandler;
import org.broadleafcommerce.profile.core.service.listener.PostRegistrationObserver;

import java.util.List;

public interface CustomerService {

    public Customer saveCustomer(Customer customer);

    public Customer saveCustomer(Customer customer, boolean register);

    public Customer registerCustomer(Customer customer, String password, String passwordConfirm);

    public Customer readCustomerByUsername(String customerName);

    public Customer readCustomerByEmail(String emailAddress);

    public Customer changePassword(PasswordChange passwordChange);

    public Customer readCustomerById(Long userId);

    public Customer createCustomer();

    /**
     * Returns a <code>Customer</code> by first looking in the database, otherwise creating a new non-persisted <code>Customer</code>
     * @param customerId the id of the customer to lookup
     * @return either a <code>Customer</code> from the database if it exists, or a new non-persisted <code>Customer</code>
     */
    public Customer createCustomerFromId(Long customerId);

    public void addPostRegisterListener(PostRegistrationObserver postRegisterListeners);

    public void removePostRegisterListener(PostRegistrationObserver postRegisterListeners);
    
    public Customer resetPassword(PasswordReset passwordReset);
	
    public List<PasswordUpdatedHandler> getPasswordResetHandlers();

	public void setPasswordResetHandlers(List<PasswordUpdatedHandler> passwordResetHandlers);
	
	public List<PasswordUpdatedHandler> getPasswordChangedHandlers();

	public void setPasswordChangedHandlers(List<PasswordUpdatedHandler> passwordChangedHandlers);
	
}
