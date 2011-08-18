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
package org.broadleafcommerce.openadmin.server.security.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.openadmin.server.security.domain.AdminUser;
import org.broadleafcommerce.persistence.EntityConfiguration;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author jfischer
 *
 */
@Repository("blAdminUserDao")
public class AdminUserDaoImpl implements AdminUserDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    public void deleteAdminUser(AdminUser user) {
    	if (!em.contains(user)) {
        	user = (AdminUser) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.openadmin.server.security.domain.AdminUser"), user.getId());
    	}
        em.remove(user);
    }

    public AdminUser readAdminUserById(Long id) {
        return (AdminUser) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.openadmin.server.security.domain.AdminUser"), id);
    }

    public AdminUser saveAdminUser(AdminUser user) {
        return em.merge(user);
    }

    @SuppressWarnings("unchecked")
    public AdminUser readAdminUserByUserName(String userName) {
        Query query = em.createNamedQuery("BC_READ_ADMIN_USER_BY_USERNAME");
        query.setParameter("userName", userName);
        List<AdminUser> users = query.getResultList();
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<AdminUser> readAllAdminUsers() {
        Query query = em.createNamedQuery("BC_READ_ALL_ADMIN_USERS");
        List<AdminUser> users = query.getResultList();
        return users;
    }
}
