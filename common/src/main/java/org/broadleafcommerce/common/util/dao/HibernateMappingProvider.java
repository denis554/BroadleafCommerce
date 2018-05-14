/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
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
package org.broadleafcommerce.common.util.dao;

import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.mapping.PersistentClass;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * <p>
 * Hibernate SPI implementor that harvests metadata about all of the entity classes
 *
 * <p>
 * This is registered within META-INF/services/org.hibernate.boot.spi.SessionFactoryBuilderFactory and listens
 * to the session factory being created with all of the metadata
 *
 * @author Jeff Fischer
 */
public class HibernateMappingProvider implements SessionFactoryBuilderFactory {

    private static final Map<String, PersistentClass> metadataMap = new ConcurrentHashMap<>();

    public HibernateMappingProvider() {
        // empty constructor for normal operation
    }

    /**
     * Initialize with seed data independent of {@link #getSessionFactoryBuilder(MetadataImplementor, SessionFactoryBuilderImplementor)}
     * @param metadataMap seed data
     */
    public HibernateMappingProvider(Map<String, PersistentClass> metadataMap) {
        metadataMap.putAll(metadataMap);
    }

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(MetadataImplementor metadata, SessionFactoryBuilderImplementor defaultBuilder) {
        // TODO: separate these out into which metadata they are from; for instance, the blSecurePU vs the blPU
        // TODO: check entity scanning works with entity bindings and not just classes listed in the persistence.xml
        Collection<PersistentClass> classes = metadata.getEntityBindings();
        classes.forEach(clazz -> metadataMap.put(clazz.getClassName(), clazz));
        return defaultBuilder;
    }

    /**
     * Returns the underlying Hibernate metadata about a given entity class across all available persistence units
     *
     * @param entityClass FQN of a Hibernate entity
     * @return the Hibernate metadata for that class, or null if there is no mapping
     */
    @Nullable
    public static PersistentClass getMapping(String entityClass) {
        return metadataMap.get(entityClass);
    }

    /**
     * Retrieves all Hibernate metadata for all entities
     * @return all of the tracked {@link PersistentClass} across all registered persistence units
     */
    @NonNull
    public static Collection<PersistentClass> getAllMappings() {
        return metadataMap.values();
    }

}
