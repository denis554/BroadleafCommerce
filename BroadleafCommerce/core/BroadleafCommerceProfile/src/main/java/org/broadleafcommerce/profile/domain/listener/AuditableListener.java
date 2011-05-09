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
package org.broadleafcommerce.profile.domain.listener;

import java.lang.reflect.Field;
import java.util.Calendar;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.broadleafcommerce.common.domain.Auditable;
import org.broadleafcommerce.time.SystemTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditableListener {

    @PrePersist
    public void setAuditCreatedBy(Object entity) throws Exception {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            Field field = entity.getClass().getDeclaredField("auditable");
            field.setAccessible(true);
            if (field.isAnnotationPresent(Embedded.class)) {
            	Object auditable = field.get(entity);
            	if (auditable == null) {
            		field.set(entity, new Auditable());
            		auditable = field.get(entity);
            	}
        		Field temporalField = auditable.getClass().getDeclaredField("dateCreated");
        		Field agentField = auditable.getClass().getDeclaredField("createdBy");
        		setAuditValueTemporal(temporalField, auditable);
        		setAuditValueAgent(agentField, auditable);
            }
        }
    }
    
    @PreUpdate
    public void setAuditUpdatedBy(Object entity) throws Exception {
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            Field field = entity.getClass().getDeclaredField("auditable");
            field.setAccessible(true);
            if (field.isAnnotationPresent(Embedded.class)) {
            	Object auditable = field.get(entity);
            	if (auditable == null) {
            		field.set(entity, new Auditable());
            		auditable = field.get(entity);
            	}
        		Field temporalField = auditable.getClass().getDeclaredField("dateUpdated");
        		Field agentField = auditable.getClass().getDeclaredField("updatedBy");
        		setAuditValueTemporal(temporalField, auditable);
        		setAuditValueAgent(agentField, auditable);
            }
        }
    }
    
    protected void setAuditValueTemporal(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
    	Calendar cal = SystemTime.asCalendar();
    	field.setAccessible(true);
    	field.set(entity, cal.getTime());
    }
    
    protected void setAuditValueAgent(Field field, Object entity) throws IllegalArgumentException, IllegalAccessException {
    	try {
    		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    		if (authentication != null) {
    			Object principal = authentication.getPrincipal();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	field.setAccessible(true);
    	field.set(entity, 0L);
    }

}
