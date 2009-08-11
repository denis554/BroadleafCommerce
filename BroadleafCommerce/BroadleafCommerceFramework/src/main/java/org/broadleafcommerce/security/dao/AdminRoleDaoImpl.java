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
package org.broadleafcommerce.security.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.security.domain.AdminRole;
import org.springframework.stereotype.Repository;

@Repository("blAdminRoleDao")
public class AdminRoleDaoImpl implements AdminRoleDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    public void deleteAdminRole(AdminRole role) {
    	if (!em.contains(role)) {
    		role = readAdminRoleById(role.getId());
    	}
        em.remove(role);
    }

    @SuppressWarnings("unchecked")
    public AdminRole readAdminRoleById(Long id) {
        return (AdminRole) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.security.domain.AdminRole"), id);
    }

    public AdminRole saveAdminRole(AdminRole role) {
        return em.merge(role);
    }

    @SuppressWarnings("unchecked")
    public List<AdminRole> readAllAdminRoles() {
        Query query = em.createNamedQuery("BC_READ_ALL_ADMIN_ROLES");
        List<AdminRole> roles = query.getResultList();
        return roles;
    }
}
