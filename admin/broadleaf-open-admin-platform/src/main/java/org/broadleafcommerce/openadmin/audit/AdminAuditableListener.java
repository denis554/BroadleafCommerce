/*
 * #%L
 * BroadleafCommerce Open Admin Platform
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
package org.broadleafcommerce.openadmin.audit;

import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.openadmin.server.security.domain.AdminUser;

import java.lang.reflect.Field;
import java.util.Calendar;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AdminAuditableListener {

    @PrePersist
    public void setAuditCreatedBy(Object entity) throws Exception {
        setAuditCreatedBy(entity, new AdminAuditable());
    }

    @PreUpdate
    public void setAuditUpdatedBy(Object entity) throws Exception {
        setAuditUpdatedBy(entity, new AdminAuditable());
    }

    protected void setAuditValueTemporal(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
        Calendar cal = SystemTime.asCalendar();
        field.setAccessible(true);
        field.set(entity, cal.getTime());
    }

    protected void setAuditValueAgent(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
        try {
            BroadleafRequestContext context = BroadleafRequestContext.getBroadleafRequestContext();
            if (context != null && context.getAdmin() && context.getAdditionalProperties().containsKey("adminUser")) {
                field.setAccessible(true);
                field.set(entity, ((AdminUser) context.getAdditionalProperties().get("adminUser")).getId());
            }
        } catch (IllegalStateException e) {
            //do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getAuditableFieldName() {
        return "auditable";
    }

    private Field getSingleField(Class<?> clazz, String fieldName) throws IllegalStateException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsf) {
            // Try superclass
            if (clazz.getSuperclass() != null) {
                return getSingleField(clazz.getSuperclass(), fieldName);
            }

            return null;
        }
    }
}
