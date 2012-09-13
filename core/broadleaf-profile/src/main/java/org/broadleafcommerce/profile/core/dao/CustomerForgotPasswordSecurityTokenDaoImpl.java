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

package org.broadleafcommerce.profile.core.dao;


import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.profile.core.domain.CustomerForgotPasswordSecurityToken;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 
 * @author bpolster
 *
 */
@Repository("blCustomerForgotPasswordSecurityTokenDao")
public class CustomerForgotPasswordSecurityTokenDaoImpl implements CustomerForgotPasswordSecurityTokenDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Override
    public CustomerForgotPasswordSecurityToken readToken(String token) {
        return (CustomerForgotPasswordSecurityToken) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.profile.core.domain.CustomerForgotPasswordSecurityToken"), token);        
    }

    @Override
    public CustomerForgotPasswordSecurityToken saveToken(CustomerForgotPasswordSecurityToken token) {
        return em.merge(token);
    }
}
