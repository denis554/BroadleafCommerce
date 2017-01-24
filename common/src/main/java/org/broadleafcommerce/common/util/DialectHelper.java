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
package org.broadleafcommerce.common.util;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

/**
 * @author Jeff Fischer
 */
@Repository("blDialectHelper")
public class DialectHelper {

    public synchronized Dialect getHibernateDialect(EntityManager em) {
        SessionFactoryImplementor factory = (SessionFactoryImplementor) em.unwrap(Session.class).getSessionFactory();
        return factory.getDialect();
    }

    public boolean isOracle(EntityManager em) {
        //Since should handle other Oracle dialects as well, since they derive from Oracle8iDialect
        return getHibernateDialect(em) instanceof Oracle8iDialect;
    }

    public boolean isPostgreSql(EntityManager em) {
        //Since should handle other Postgres dialects as well, since they derive from PostgreSQL81Dialect
        return getHibernateDialect(em) instanceof PostgreSQL81Dialect;
    }

    public boolean isSqlServer(EntityManager em) {
        return getHibernateDialect(em) instanceof SQLServerDialect;
    }

    public boolean isMySql(EntityManager em) {
        return getHibernateDialect(em) instanceof MySQLDialect;
    }
}
