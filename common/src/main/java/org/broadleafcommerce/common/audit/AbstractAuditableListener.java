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
package org.broadleafcommerce.common.audit;

import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.common.util.BLCFieldUtils;

import java.lang.reflect.Field;
import java.util.Calendar;

import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 * Implements behavior shared by auditable listener implementations
 *
 * @author Chris Kittrell (ckittrell)
 */
public abstract class AbstractAuditableListener {

    /**
     * Method that will be invoked in a registered listener to set the entity's creation data.
     *  In most cases, calling {@link AbstractAuditableListener#setAuditCreatedBy(Object, Class)} should suffice.
     *
     * @param entity
     * @return
     */
    public abstract void setAuditCreatedBy(Object entity) throws Exception;

    /**
     * Method that will be invoked in a registered listener to set the entity's update data.
     *  In most cases, calling {@link AbstractAuditableListener#setAuditUpdatedBy(Object, Class)} should suffice.
     *
     * @param entity
     * @return
     */
    public abstract void setAuditUpdatedBy(Object entity) throws Exception;

    /**
     * Method that sets the user-related data.
     *
     * @param field
     * @param entity
     * @return
     */
    protected abstract void setAuditValueAgent(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the value of the dateCreated, createdBy, and dateUpdated fields.
     *
     * @param entity
     * @param auditableClass
     * @return
     */
    public void setAuditCreatedBy(Object entity, Class auditableClass) throws Exception {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            Field field = BLCFieldUtils.getSingleField(entity.getClass(), getAuditableFieldName());
            field.setAccessible(true);
            if (field.isAnnotationPresent(Embedded.class)) {
                Object auditable = field.get(entity);
                if (auditable == null) {
                    field.set(entity, auditableClass);
                    auditable = field.get(entity);
                }
                Field temporalCreatedField = auditable.getClass().getDeclaredField("dateCreated");
                Field temporalUpdatedField = auditable.getClass().getDeclaredField("dateUpdated");
                Field agentField = auditable.getClass().getDeclaredField("createdBy");
                setAuditValueTemporal(temporalCreatedField, auditable);
                setAuditValueTemporal(temporalUpdatedField, auditable);
                setAuditValueAgent(agentField, auditable);
            }
        }
    }

    /**
     * Sets the value of the dateUpdated and updatedBy fields.
     *
     * @param entity
     * @param auditableClass
     * @return
     */
    public void setAuditUpdatedBy(Object entity, Class auditableClass) throws Exception {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            Field field = BLCFieldUtils.getSingleField(entity.getClass(), getAuditableFieldName());
            field.setAccessible(true);
            if (field.isAnnotationPresent(Embedded.class)) {
                Object auditable = field.get(entity);
                if (auditable == null) {
                    field.set(entity, auditableClass);
                    auditable = field.get(entity);
                }
                Field temporalField = auditable.getClass().getDeclaredField("dateUpdated");
                Field agentField = auditable.getClass().getDeclaredField("updatedBy");
                setAuditValueTemporal(temporalField, auditable);
                setAuditValueAgent(agentField, auditable);
            }
        }
    }

    /**
     * Used to set the timestamp for dateCreated and dateUpdated.
     *
     * @param field
     * @param entity
     * @return
     */
    protected void setAuditValueTemporal(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
        Calendar cal = SystemTime.asCalendar();
        field.setAccessible(true);
        field.set(entity, cal.getTime());
    }

    /**
     * Gathers the auditable field name.
     *  The major purpose of this method is to provide a hook point for extensions to declare a different field name.
     *
     * @return the name of the auditable field
     */
    protected String getAuditableFieldName() {
        return "auditable";
    }
    
}
