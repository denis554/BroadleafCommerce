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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.profile.domain.CustomerRole;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("blUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource(name="blCustomerService")
    protected CustomerService customerService;

    @Resource(name="blRoleService")
    protected RoleService roleService;

    protected boolean forcePasswordChange = false;

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        org.broadleafcommerce.profile.domain.Customer customer = customerService.readCustomerByUsername(username);
        if (customer == null) {
            throw new UsernameNotFoundException("The customer was not found");
        }

        User returnUser = null;

        boolean pwChangeRequired = customer.isPasswordChangeRequired();
        List<GrantedAuthority> grantedAuthorities = createGrantedAuthorities(roleService.findCustomerRolesByCustomerId(customer.getId()));
        if (pwChangeRequired) {
            if (forcePasswordChange) {
                returnUser = new User(username, customer.getPassword(), true, true, !customer.isPasswordChangeRequired(), true, grantedAuthorities.toArray(new GrantedAuthority[0]));
            } else {
                grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_PASSWORD_CHANGE_REQUIRED"));
                returnUser = new User(username, customer.getPassword(), true, true, true, true, grantedAuthorities.toArray(new GrantedAuthority[0]));
            }
        } else {
            returnUser = new User(username, customer.getPassword(), true, true, !customer.isPasswordChangeRequired(), true, grantedAuthorities.toArray(new GrantedAuthority[0]));
        }
        return returnUser;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    protected List<GrantedAuthority> createGrantedAuthorities(List<CustomerRole> customerRoles) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        for (CustomerRole role : customerRoles) {
            grantedAuthorities.add(new GrantedAuthorityImpl(role.getRoleName()));
        }
        return grantedAuthorities;
    }
}
