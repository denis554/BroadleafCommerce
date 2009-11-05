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
package org.broadleafcommerce.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.broadleafcommerce.extensibility.context.MergeClassPathXMLApplicationContext;
import org.broadleafcommerce.extensibility.context.StandardConfigLocations;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(transactionManager = "blTransactionManager", defaultRollback = true)
@TestExecutionListeners(inheritListeners = false, value = {MergeDependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, MergeTransactionalTestExecutionListener.class})
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

	private static MergeClassPathXMLApplicationContext mergeContext = null;
	
	public static MergeClassPathXMLApplicationContext getContext() {
		try {
			if (mergeContext == null) {
				String[] contexts = StandardConfigLocations.retrieveAll(StandardConfigLocations.TESTCONTEXTTYPE);
				String[] allContexts = new String[contexts.length + 2];
				System.arraycopy(contexts, 0, allContexts, 0, contexts.length);
				allContexts[allContexts.length-2] = "bl-applicationContext-test.xml";
				allContexts[allContexts.length-1] = "bl-applicationContext-test-security.xml";
				mergeContext = new MergeClassPathXMLApplicationContext(allContexts, new String[]{});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return mergeContext;
	}
	
	@PersistenceContext(unitName = "blPU")
    protected EntityManager em;

}
