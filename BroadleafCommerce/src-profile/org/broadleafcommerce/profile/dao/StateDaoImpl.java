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
package org.broadleafcommerce.profile.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.Country;
import org.broadleafcommerce.profile.domain.State;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blStateDao")
public class StateDaoImpl implements StateDao {

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    @SuppressWarnings("unchecked")
    public State findStateByAbbreviation(String abbreviation) {
        return (State) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.profile.domain.State"), abbreviation);
    }

    @SuppressWarnings("unchecked")
    public List<State> findStates() {
        Query query = em.createNamedQuery("BC_FIND_STATES");
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Country findCountryByShortName(String shortName) {
        return (Country) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.profile.domain.Country"), shortName);
    }

    @SuppressWarnings("unchecked")
    public List<Country> findCountries() {
        Query query = em.createNamedQuery("BC_FIND_COUNTRIES");
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    public String getQueryCacheableKey() {
        return queryCacheableKey;
    }

    public void setQueryCacheableKey(String queryCacheableKey) {
        this.queryCacheableKey = queryCacheableKey;
    }

    public State create() {
        return (State) entityConfiguration.createEntityInstance(State.class.getName());
    }

}
