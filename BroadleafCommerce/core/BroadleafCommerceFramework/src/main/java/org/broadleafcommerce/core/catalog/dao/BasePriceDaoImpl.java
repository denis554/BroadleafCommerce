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
package org.broadleafcommerce.core.catalog.dao;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.broadleafcommerce.config.EntityConfiguration;
import org.broadleafcommerce.core.catalog.domain.BasePrice;
import org.springframework.stereotype.Repository;

@Repository("blBasePriceDao")
public class BasePriceDaoImpl implements BasePriceDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    public BasePrice save(BasePrice basePrice) {
        return em.merge(basePrice);
    }

    public BasePrice readBasePriceById(Long basePriceId) {
        return (BasePrice) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.core.catalog.domain.BasePrice"), basePriceId);
    }
}
