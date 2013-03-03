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

package org.broadleafcommerce.openadmin.server.dao;

import org.hibernate.ejb.Ejb3Configuration;

import javax.persistence.spi.PersistenceUnitInfo;
import java.util.HashMap;

/**
 * 
 * @author jfischer
 *
 */
public class EJB3ConfigurationDaoImpl implements EJB3ConfigurationDao {

    private Ejb3Configuration configuration = null;

    protected PersistenceUnitInfo persistenceUnitInfo;

    public Ejb3Configuration getConfiguration() {
        synchronized(this) {
            if (configuration == null) {
                Ejb3Configuration temp = new Ejb3Configuration();
                configuration = temp.configure(persistenceUnitInfo, new HashMap());
                configuration.getHibernateConfiguration().buildSessionFactory();
            }
        }
        return configuration;
    }

    public PersistenceUnitInfo getPersistenceUnitInfo() {
        return persistenceUnitInfo;
    }

    public void setPersistenceUnitInfo(PersistenceUnitInfo persistenceUnitInfo) {
        this.persistenceUnitInfo = persistenceUnitInfo;
    }
    
}
