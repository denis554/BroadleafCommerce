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
/**
 * 
 */
package org.broadleafcommerce.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Main configuration class for the broadleaf-common module
 * 
 * @author Phillip Verheyden (phillipuniverse)
 */
@Configuration
public class BroadleafCommonConfig {

    /**
     * Used in order to hydrate beans that have {@literal @}Value annotations on their properties.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer blPropertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pppc = new PropertySourcesPlaceholderConfigurer();
        pppc.setIgnoreUnresolvablePlaceholders(true);
        return pppc;
    }
    
    @Bean
    public static ProfileAwarePropertiesBeanFactoryPostProcessor blPropertiesPostProcessor() {
        return new ProfileAwarePropertiesBeanFactoryPostProcessor();
    }
    
    @Bean
    public static FrameworkCommonPropertySource blCommonProperties() {
        return new FrameworkCommonPropertySource("config/bc/", FrameworkCommonPropertySource.BROADLEAF_COMMON_ORDER);
    }
    
    @Bean
    public static ProfileAwarePropertySource blDefaultRuntimeProperties() {
        return new ProfileAwarePropertySource("runtime-properties");
    }
    
    /**
     * Other enterprise/mulititenant modules override this adapter to provide one that supports dynamic filtration
     */
    @Bean
    @ConditionalOnMissingBean(name = "blJpaVendorAdapter")
    public JpaVendorAdapter blJpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

}
