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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

    @Override
    public void deleteAdminRole(AdminRole role) {
        em.remove(role);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AdminRole readAdminRoleById(Long id) {
        return (AdminRole) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.security.domain.AdminRole"), id);
    }

    @Override
    public AdminRole saveAdminRole(AdminRole role) {
        if (role.getId() == null) {
            em.persist(role);
        } else {
            role = em.merge(role);
        }
        return role;
    }

}
