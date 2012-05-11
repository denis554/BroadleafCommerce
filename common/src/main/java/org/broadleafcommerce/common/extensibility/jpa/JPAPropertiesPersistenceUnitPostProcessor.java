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

package org.broadleafcommerce.common.extensibility.jpa;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *   This class allows us to override Persistence Unit properties on a per-environment basis. Spring's
 *   PersistenceUnitManager allows us to pass in a list of PersistenceUnitPostProcessors.  This class will allow us to override
 *   or add custom JPA properties to the Persistence Unit.  This is useful when different environments have different requirements
 *   for JPA properties.  The best example of this is SQL Dialect.  You may want to use a dialect such as Oracle for production
 *   and test environments, and perhaps HSQLDB for local and integration testing.  You can set the dialect property using the
 *   <code>RuntimeEnvironmentPropertiesConfigurer</code>.  The keys will be the same in each environment, but the values
 *   would be defined the environment-specific properties files.  If you want the property to be added only to certain environments,
 *   add the value "null" to the properties file.  For example:
 *
 *
 *   <bean id="blPersistenceUnitManager" class="org.broadleafcommerce.profile.extensibility.jpa.MergePersistenceUnitManager">
 *       <property name="persistenceXmlLocations">
 *           <list>
 *               <value>classpath*:/META-INF/mycompany_persistence.xml</value>
 *           </list>
 *        </property>
 *        <property name="persistenceUnitPostProcessors">
 *            <list>
 *                <bean class="org.broadleafcommerce.common.extensibility.jpa.JPAPropertiesPersistenceUnitPostProcessor">
 *                    <property name="persistenceUnitProperties">
 *                        <map>
 *                            <!-- Notice that the value will be replaced by property substitution from an environment
 *                                 specific file. Also note that the Persistence Unit name is prepended to the key and value to allow for configuration of
 *                                 multiple Persistence Units. This needs to be keyed this way in the properties file. The prepended persistence 
 *                                 unit name will be removed from the property name when it is added to the Persistence Unit. -->
 *                            <entry key="blPU.hibernate.dialect" value="${blPU.hibernate.dialect}"/>
 *                        </map>
 *                    </property>
 *                </bean>
 *            </list>
 *        </property>
 *    </bean>
 *
 */
public class JPAPropertiesPersistenceUnitPostProcessor implements
        org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor {

    private Map<String, String> persistenceUnitProperties;
    
    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        if (persistenceUnitProperties != null) {
            String puName = pui.getPersistenceUnitName() + ".";
            Set<String> keys = persistenceUnitProperties.keySet();
            Properties props = pui.getProperties();

            for (String key : keys) {
                if (key.startsWith(puName)){
                    String value = persistenceUnitProperties.get(key);
                    String newKey = key.substring(puName.length());
                    if ("null".equalsIgnoreCase(value)){
                        props.remove(newKey);
                    } else if (value != null && ! "".equals(value)) {
                        props.put(newKey, value);
                    }
                }
            }
            pui.setProperties(props);
        }
    }
    
    public void setPersistenceUnitProperties(Map<String, String> properties) {
        this.persistenceUnitProperties = properties;
    }
}
