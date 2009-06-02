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
package org.broadleafcommerce.test.integration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

@ContextConfiguration(locations = { "classpath:/applicationContext.xml", "classpath:/applicationContext-test.xml"})
public abstract class BaseTest extends AbstractTransactionalTestNGSpringContextTests {

    /*
     * TODO each extension of this BaseTest is getting a datasource set via autowiring. However, with the
     * new blSecurePU persistence unit, we will need to specify different datasources for the 2 persistence
     * units. Spring does not like this, since autowiring for this class expects a single DS to be defined. Therefore,
     * we need to override in xml the datasource being set. Possibly, we need to find a convenient way to do this without
     * specifying every test class in xml. Not a priority, but will come up later as we add test cases that involve
     * any of the secure payment info classes (i.e. CreditCardPaymentInfo).
     */

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    public EntityManager getEntityManager() {
        if (em == null) {
            em = ((EntityManagerFactory) applicationContext.getBean("entityManagerFactory")).createEntityManager();
        }
        return em;
    }

    @BeforeClass
    public void setup() {
        getEntityManager();
    }
}
