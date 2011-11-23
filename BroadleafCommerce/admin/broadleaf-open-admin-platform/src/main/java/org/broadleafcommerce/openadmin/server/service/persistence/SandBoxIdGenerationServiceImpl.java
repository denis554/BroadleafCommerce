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

package org.broadleafcommerce.openadmin.server.service.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.openadmin.server.dao.SandBoxIdGenerationDao;
import org.broadleafcommerce.openadmin.server.domain.SandBoxIdGeneration;

import javax.persistence.OptimisticLockException;
import java.util.HashMap;
import java.util.Map;

//@Service("blSandBoxIdGenerationService")
public class SandBoxIdGenerationServiceImpl implements SandBoxIdGenerationService {

    private static final Log LOG = LogFactory.getLog(SandBoxIdGenerationServiceImpl.class);

    //@Resource(name="blSandBoxIdGenerationDao")
    protected SandBoxIdGenerationDao sandBoxIdGenerationDao;

    protected Map<String, Id> idTypeIdMap = new HashMap<String, Id>();

    public Long findNextId(String idType) {
        Id id;
        synchronized (idTypeIdMap) {
            id = idTypeIdMap.get(idType);
            if (id == null) {
                // recheck, another thread may have added this.
                id = idTypeIdMap.get(idType);
                if (id == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Getting the initial id from the database.");
                    }
                    SandBoxIdGeneration idGeneration = getCurrentIdRange(idType);
                    id = new Id(idGeneration.getBatchStart(), idGeneration.getBatchSize());
                }
                idTypeIdMap.put(idType, id);
            }
        }

        synchronized(id) {
            if (id.batchSize == 0L) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updating batch size for idType " + idType);
                }
                SandBoxIdGeneration idGeneration = getCurrentIdRange(idType);
                id.nextId = idGeneration.getBatchStart();
                id.batchSize = idGeneration.getBatchSize();
            }
            Long retId = id.nextId--;
            id.batchSize++;

            return retId;
        }
    }
    
    private SandBoxIdGeneration getCurrentIdRange(String idType) {
        SandBoxIdGeneration idGeneration = null;
        int retryCount = 0;
        boolean stale = true;
        while (stale) {
            try {
                idGeneration = sandBoxIdGenerationDao.findNextId(idType);
                stale = false;
            } catch (OptimisticLockException e) {
                //do nothing -- we will try again
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Error saving batch start for " + idType + ".  Requerying table.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to retrieve id range for " + idType, e);
            }
            if (retryCount >= 10) {
                throw new RuntimeException("Unable to retrieve id range for " + idType + ". Tried " + retryCount + " times, but the version for this entity continues to be concurrently modified.");
            }
            retryCount++;
        }
        return idGeneration;
    }

    private class Id {
        public Long nextId;
        public Long batchSize;

        public Id(Long nextId, Long batchSize) {
            this.nextId = nextId;
            this.batchSize = batchSize;
        }
    }
}
