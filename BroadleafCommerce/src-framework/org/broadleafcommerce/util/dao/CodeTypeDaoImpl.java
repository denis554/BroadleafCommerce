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
package org.broadleafcommerce.util.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.util.domain.CodeType;
import org.springframework.stereotype.Repository;


@Repository("blCodeTypeDao")
public class CodeTypeDaoImpl implements CodeTypeDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    public CodeType create() {
        return ((CodeType) entityConfiguration.createEntityInstance(CodeType.class.getName()));
    }

    @SuppressWarnings("unchecked")
    public List<CodeType> readAllCodeTypes() {
        Query query = em.createNamedQuery("BC_READ_ALL_CODE_TYPES");
        return query.getResultList();
    }

    public void delete(CodeType codeType) {
        // TODO Auto-generated method stub
        em.remove(codeType);
    }

    @SuppressWarnings("unchecked")
    public CodeType readCodeTypeById(Long codeTypeId) {
        return (CodeType) em.find(entityConfiguration.lookupEntityClass(CodeType.class.getName()), codeTypeId);
    }

    @SuppressWarnings("unchecked")
    public CodeType readCodeTypeByKey(String key) {
        CodeType codeType = null;
        Query query = em.createNamedQuery("BC_READ_CODE_TYPE_BY_KEY");
        query.setParameter("key", key);
        List<CodeType> result = query.getResultList();
        if(result.size() > 0) {
            codeType = result.get(0);
        }
        return codeType;
    }

    public CodeType save(CodeType codeType) {
        if(codeType.getId()==null) {
            em.persist(codeType);
        }else {
            codeType = em.merge(codeType);
        }
        return codeType;
    }

}
