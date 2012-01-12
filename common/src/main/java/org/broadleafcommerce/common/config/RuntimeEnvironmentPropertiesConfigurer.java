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

package org.broadleafcommerce.common.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * A property resource configurer that chooses the property file at runtime
 * based on the runtime environment.
 * <p>
 * Used for choosing properties files based on the current runtime environment,
 * allowing for movement of the same application between multiple runtime
 * environments without rebuilding.
 * <p>
 * The property replacement semantics of this implementation are identical to
 * PropertyPlaceholderConfigurer, from which this class inherits. <code>
 * <pre>
 * &lt;bean id=&quot;propertyConfigurator&quot; class=&quot;frilista.framework.RuntimeEnvironmentPropertiesConfigurer&quot;&gt;
 *        &lt;property name=&quot;propertyLocation&quot; value=&quot;/WEB-INF/runtime-properties/&quot; /&gt;
 *        &lt;property name=&quot;environments&quot;&gt;
 *        &lt;set&gt;
 *            &lt;value&gt;production&lt;/value&gt;
 *            &lt;value&gt;staging&lt;/value&gt;
 *            &lt;value&gt;integration&lt;/value&gt;
 *            &lt;value&gt;development&lt;/value&gt;
 *        &lt;/set&gt;
 *        &lt;/property&gt;
 *        &lt;property name=&quot;defaultEnvironment&quot; value=&quot;development&quot;/&gt;
 * &lt;/bean&gt;
 * </code> </pre> The keys of the environment specific properties files are
 * compared to ensure that each property file defines the complete set of keys,
 * in order to avoid environment-specific failures.
 * <p>
 * An optional RuntimeEnvironmentKeyResolver implementation can be provided,
 * allowing for customization of how the runtime environment is determined. If
 * no implementation is provided, a default of
 * SystemPropertyRuntimeEnvironmentKeyResolver is used (which uses the system
 * property 'runtime.environment')
 * @author <a href="mailto:chris.lee.9@gmail.com">Chris Lee</a>
 */
public class RuntimeEnvironmentPropertiesConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    private static final Log LOG = LogFactory.getLog(RuntimeEnvironmentPropertiesConfigurer.class);

    protected String defaultEnvironment;
    protected RuntimeEnvironmentKeyResolver keyResolver;
    protected Set<String> environments = Collections.emptySet();
    protected Set<Resource> propertyLocations;
    protected StringValueResolver stringValueResolver;

    public RuntimeEnvironmentPropertiesConfigurer() {
        // EMPTY
    }

    public void afterPropertiesSet() throws IOException {
        if (!environments.contains(defaultEnvironment)) {
            throw new AssertionError("Default environment '" + defaultEnvironment + "' not listed in environment list");
        }

        if (keyResolver == null) {
            keyResolver = new SystemPropertyRuntimeEnvironmentKeyResolver();
        }

        String environment = determineEnvironment();

        Resource[] propertiesLocation = createPropertiesResource(environment);
        Resource[] commonLocation = createCommonResource();
        ArrayList<Resource> allLocations = new ArrayList<Resource>();
        for (Resource resource : propertiesLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        for (Resource resource : commonLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        setLocations(allLocations.toArray(new Resource[] {}));

        validateProperties();
    }

    protected boolean compareProperties(Properties props1, Properties props2, String envInner) throws IOException {
        Set<Object> outerKeys = props1.keySet();
        boolean missingKeys = false;
        for (Object keyObj : outerKeys) {
            String key = (String) keyObj;
            if (!props2.containsKey(key)) {
                missingKeys = true;
                LOG.info("Property file mismatch: " + key + " missing from environment " + envInner + ".   Make sure that the a property placeholder (at least) is defined in each environment (e.g. myproperty=?).");
            }
        }

        return missingKeys;
    }

    protected Resource[] createPropertiesResource(String environment) throws IOException {
        String fileName = environment.toString().toLowerCase() + ".properties";
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative(fileName);
            index++;
        }
        return resources;
    }

    protected Resource[] createCommonResource() throws IOException {
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative("common.properties");
            index++;
        }
        return resources;
    }

    protected String determineEnvironment() {
        String environment = keyResolver.resolveRuntimeEnvironmentKey();

        if (environment == null) {
            LOG.warn("Unable to determine runtime environment, using default environment '" + defaultEnvironment + "'");
            return defaultEnvironment;
        }

        return environment.toLowerCase();
    }

    protected void validateProperties() throws IOException {
        boolean missingKeys = false;
        for (String envOuter : environments) {
            for (String envInner : environments) {
                if (!envOuter.equals(envInner)) {
                    Properties resource1 = mergeProperties(createPropertiesResource(envOuter));

                    Properties resource2 = mergeProperties(createPropertiesResource(envInner));

                    missingKeys |= compareProperties(resource1, resource2, envInner);
                }
            }
        }

        // BP:  Removing exception.   Having "fake" properties is no better than having none.
        // Info messages will provide information on what properties are different by environment.
        /*if (missingKeys) {
            throw new AssertionError("Missing runtime properties keys (log entries above have details)");
        }*/
    }

    protected Properties mergeProperties(Resource[] locations) throws IOException {
        Properties props = new Properties();
        for (Resource resource : locations) {
            if (resource.exists()) {
                props = new Properties(props);
                props.load(resource.getInputStream());
            } else {
                LOG.warn("Unable to locate resource: " + resource.getFilename());
            }
        }
        return props;
    }

    @Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
		stringValueResolver = new PlaceholderResolvingStringValueResolver(props);
	}

    /**
     * Sets the default environment name, used when the runtime environment
     * cannot be determined.
     */
    public void setDefaultEnvironment(String defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    public void setKeyResolver(RuntimeEnvironmentKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    /**
     * Sets the allowed list of runtime environments
     */
    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    /**
     * Sets the directory from which to read environment-specific properties
     * files; note that it must end with a '/'
     */
    public void setPropertyLocations(Set<Resource> propertyLocations) {
        this.propertyLocations = propertyLocations;
    }

    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper("${", "}", ":", true);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		public String resolveStringValue(String strVal) throws BeansException {
			String value = this.helper.replacePlaceholders(strVal, this.resolver);
			return (value.equals("") ? null : value);
		}
	}

	private class PropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

		private final Properties props;

		private PropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		public String resolvePlaceholder(String placeholderName) {
			return RuntimeEnvironmentPropertiesConfigurer.this.resolvePlaceholder(placeholderName, props, 1);
		}
	}

    public StringValueResolver getStringValueResolver() {
        return stringValueResolver;
    }
}
