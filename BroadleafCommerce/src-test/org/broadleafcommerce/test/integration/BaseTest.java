package org.broadleafcommerce.test.integration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

@ContextConfiguration(locations = { "classpath:/applicationContext.xml", "classpath:/applicationContext-test.xml"})
public abstract class BaseTest extends AbstractTransactionalTestNGSpringContextTests {

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    protected EntityManager emUser;

    public EntityManager getEntityManager() {
        if (emUser == null) {
        	emUser = ((EntityManagerFactory) applicationContext.getBean("entityManagerFactory")).createEntityManager();
        }
        return emUser;
    }

    @BeforeClass
    public void setup() {
        getEntityManager();
    }
}
