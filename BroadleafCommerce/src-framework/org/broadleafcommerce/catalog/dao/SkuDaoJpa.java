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
package org.broadleafcommerce.catalog.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blSkuDao")
public class SkuDaoJpa implements SkuDao {

    @PersistenceContext(unitName="blPU")
    private EntityManager em;

    @Resource
    private EntityConfiguration entityConfiguration;

    public Sku save(Sku sku) {
        if (sku.getId() == null) {
            em.persist(sku);
        } else {
            sku = em.merge(sku);
        }
        return sku;
    }

    @SuppressWarnings("unchecked")
    public Sku readSkuById(Long skuId) {
        return (Sku) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.catalog.domain.Sku"), skuId);
    }

    public Sku readFirstSku() {
        Query query = em.createNamedQuery("BC_READ_FIRST_SKU");
        return (Sku) query.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Sku> readAllSkus() {
        Query query = em.createNamedQuery("BC_READ_ALL_SKUS");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Sku> readSkusById(List<Long> ids) {
        Query query = em.createNamedQuery("BC_READ_SKUS_BY_ID");
        query.setParameter("skuIds", ids);
        return query.getResultList();
    }
}
