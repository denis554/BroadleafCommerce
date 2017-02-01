/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.common.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Adds beans of type {@link FrameworkCommonPropertySource} and {@link ProfileAwarePropertySource} to the current environment.
 * If no Spring profile is active, this will default to {@code "development"} and add that profile to {@link ConfigurableEnvironment#getActiveProfiles()}
 * 
 * <p>
 * This is specifically designed to execute <i>prior</i> to the {@link PropertySourcesPlaceholderConfigurer} because all property sources must be
 * added to the environment prior to executing that post-processor in order for all {@literal @}Value placeholders to be resolved correctly.
 * 
 * @author Jeff Fischer
 * @author Phillip Verheyden (phillipuniverse)
 * @see {@link ProfileAwarePropertySource}
 * @see {@link FrameworkCommonPropertySource}
 */
public class ProfileAwarePropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {
    
    /**
     * A -D argument representing a path to a file that overrides all of the other properties resolved from internal property files
     */
    public static final String PROPERTY_OVERRIDES_PROPERTY = "property-override";
    public static final String DEPRECATED_RUNTIME_ENVIRONMENT_KEY = "runtime.environment";

    private static final Log LOG = LogFactory.getLog(ProfileAwarePropertiesBeanFactoryPostProcessor.class);
    
    protected Environment environment;
    
    protected BeanFactory beanFactory;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;

        // Manually looking up these beans because you can't @Autowire inside of BeanFactoryPostProcessor
        List<FrameworkCommonPropertySource> frameworkSources = new ArrayList<>(beanFactory.getBeansOfType(FrameworkCommonPropertySource.class).values());
        Collections.sort(frameworkSources, AnnotationAwareOrderComparator.INSTANCE);
        
        String lastAddedResourceName = null;
        // first add all of the framework property sources which should be 'common.properties'
        for (FrameworkCommonPropertySource source : frameworkSources) {
            String configLocation = source.getClasspathFolder();
            
            Resource commonProp = createClasspathResource(configLocation, "common", null);
            if (commonProp.exists()) {
                lastAddedResourceName = addToEnvironment(env, Arrays.asList(commonProp), lastAddedResourceName);
            }
        }
        
        String deprecatedRuntimeEnvironment = env.getProperty(DEPRECATED_RUNTIME_ENVIRONMENT_KEY);
        if (ArrayUtils.isEmpty(env.getActiveProfiles()) && StringUtils.isNotBlank(deprecatedRuntimeEnvironment)) {
            LOG.warn("The use of -Druntime.environment is deprecated in favor of Spring Profiles and will be removed in a future release. To specify a profile as a -D argument use -Dspring.profiles.active as a drop-in replacement"
                + " for -Druntime.environment. Adding " + deprecatedRuntimeEnvironment + " to the list of active Spring profiles.");
            env.addActiveProfile(deprecatedRuntimeEnvironment);
        }
        
        // Manually looking up these beans because you can't @Autowire inside of BeanFactoryPostProcessor
        List<ProfileAwarePropertySource> profileAwareSources = new ArrayList<>(beanFactory.getBeansOfType(ProfileAwarePropertySource.class).values());
        Collections.sort(profileAwareSources, AnnotationAwareOrderComparator.INSTANCE);
        
        List<Resource> commonSharedResources = new ArrayList<>();
        List<Resource> commonResources = new ArrayList<>();
        List<Resource> profileSpecificSharedResources = new ArrayList<>();
        List<Resource> profileSpecificResources = new ArrayList<>();
        for (ProfileAwarePropertySource source : profileAwareSources) {
            // then add all of the user property sources
            String configLocation = source.getClasspathFolder();
            
            Resource commonSharedProp = createClasspathResource(configLocation, "common", "shared");
            commonSharedResources.add(commonSharedProp);
            Resource commonProp = createClasspathResource(configLocation, "common", null);
            commonResources.add(commonProp);
            
            String[] activeProfiles = env.getActiveProfiles();
            if (ArrayUtils.isNotEmpty(activeProfiles)) {
                for (String profile : activeProfiles) {
                    Resource profileSpecificSharedProps = createClasspathResource(configLocation, profile, "shared");
                    profileSpecificSharedResources.add(profileSpecificSharedProps);
                   
                    Resource profileSpecificProps = createClasspathResource(configLocation, profile, null);
                    profileSpecificResources.add(profileSpecificProps);
                }
            } else {
                String[] defaultProfiles = env.getDefaultProfiles();
                
                for (String defaultProfile : defaultProfiles) {
                    Resource profileSpecificSharedProps = createClasspathResource(configLocation, defaultProfile, "shared");
                    profileSpecificSharedResources.add(profileSpecificSharedProps);
                   
                    Resource profileSpecificProps = createClasspathResource(configLocation, defaultProfile, null);
                    profileSpecificResources.add(profileSpecificProps);
                    
                }
                
                String deprecatedDefaultProfile = getDeprecatedDefaultProfileKey();
                if (!ArrayUtils.contains(defaultProfiles, deprecatedDefaultProfile)) {
                    Resource developmentSharedProps = createClasspathResource(configLocation, deprecatedDefaultProfile, "shared");
                    profileSpecificSharedResources.add(developmentSharedProps);
                   
                    Resource developmentProps = createClasspathResource(configLocation, deprecatedDefaultProfile, null);
                    profileSpecificResources.add(developmentProps);
                    
                    boolean deprecatedDefaultProfileFound = developmentSharedProps.exists() || developmentProps.exists();
                    
                    if (deprecatedDefaultProfileFound) {
                        LOG.warn("The usage of " + getDeprecatedDefaultProfileKey() + ".properties is deprecated and will be removed in a future release. Use Spring's default profile properties of 'default.properties'."
                            + " Alternatively, set the 'spring.profiles.default' system property with -Dspring.profiles.default=development to change the default profile name that Spring runs in.");
                        env.setDefaultProfiles(ArrayUtils.add(defaultProfiles, deprecatedDefaultProfile));
                    }
                }
            }
        }
        
        lastAddedResourceName = addToEnvironment(env, commonSharedResources, lastAddedResourceName);
        lastAddedResourceName = addToEnvironment(env, commonResources, lastAddedResourceName);
        lastAddedResourceName = addToEnvironment(env, profileSpecificSharedResources, lastAddedResourceName);
        lastAddedResourceName = addToEnvironment(env, profileSpecificResources, lastAddedResourceName);
        
        // At the very end of all of it, look at the property-override location
        String overrideFileLocation = env.getProperty(PROPERTY_OVERRIDES_PROPERTY);
        if (StringUtils.isNotBlank(overrideFileLocation)) {
            Resource overrideFileResource = new FileSystemResource(overrideFileLocation);
            addToEnvironment(env, Arrays.asList(overrideFileResource), lastAddedResourceName);
        }
    }
    
    protected Resource createClasspathResource(String rootLocation, String propertyName, String suffix) {
        suffix = (suffix == null) ? "" : "-" + suffix;
        String fileName = propertyName + suffix + ".properties";
        return new ClassPathResource(FilenameUtils.concat(rootLocation, fileName));
    }
    
    /**
     * <p>
     * Adds the specified <b>resource</b> as a {@link PropertySource} to the given <b>environment</b> at the order from <b>addBeforeResourceName</b>. If the
     * <b>resource</b> does not exist (meaning {@code resource.exists() == false}) then this immediately returns <b>addBeforeResourceName</b>
     * 
     * <p>
     * If <b>addBeforeResourceName</b> is null, the given <b>resource</b> will be added last via {@link MutablePropertySources#addLast(PropertySource)}.
     */
    protected String addToEnvironment(ConfigurableEnvironment environment, List<Resource> resources, String addBeforeResourceName) {
        try {
            for (Resource resource : resources) {
                if (!resource.exists()) {
                    LOG.debug(resource.getDescription() + " does not exist, skipping adding to the Environment");
                    continue;
                }
                PropertySource<?> props = new ResourcePropertySource(resource);
    
                if (addBeforeResourceName == null) {
                    environment.getPropertySources().addLast(props);
                    LOG.debug("Added property source " + props.getName() + " to the environment");
                } else {
                    environment.getPropertySources().addBefore(addBeforeResourceName, props);
                    LOG.debug("Added property source " + props.getName() + " to the environment with a higher priority than " + addBeforeResourceName);
                }
                
                addBeforeResourceName = props.getName();
            }
            return addBeforeResourceName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getDeprecatedDefaultProfileKey() {
        return "development";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
