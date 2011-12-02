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

package org.broadleafcommerce.openadmin.server.service.persistence.module;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.persistence.EntityConfiguration;
import org.hibernate.mapping.PersistentClass;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * @author jfischer
 *
 */
public class FieldManager {
	
	private static final Log LOG = LogFactory.getLog(FieldManager.class);

    protected EntityConfiguration entityConfiguration;
	protected DynamicEntityDao dynamicEntityDao;
	protected List<SortableValue> middleFields = new ArrayList<SortableValue>(5);

    public FieldManager(EntityConfiguration entityConfiguration, DynamicEntityDao dynamicEntityDao) {
        this.entityConfiguration = entityConfiguration;
        this.dynamicEntityDao = dynamicEntityDao;
    }

	public static Field getSingleField(Class<?> clazz, String fieldName) throws IllegalStateException {
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

    public Field getField(Class<?> clazz, String fieldName) throws IllegalStateException {
		StringTokenizer tokens = new StringTokenizer(fieldName, ".");
        Field field = null;

        while (tokens.hasMoreTokens()) {
            String propertyName = tokens.nextToken();
            field = getSingleField(clazz, propertyName);
            if (field != null && tokens.hasMoreTokens()) {
            	Class<?>[] entities = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(field.getType());
            	if (entities.length > 0) {
	            	PersistentClass persistentClass = dynamicEntityDao.getPersistentClass(entities[0].getName());
	            	if (persistentClass != null) {
		            	Class<?> entityClass;
						try {
							entityClass = entityConfiguration.lookupEntityClass(field.getType().getName());
							clazz = entityClass;
						} catch (Exception e) {
		            		clazz = entities[0];
						}
	            	} else {
	            		clazz = field.getType();
	            	}
            	} else {
                    //may be an embedded class - try the class directly
                    clazz = field.getType();
                }
            } else {
            	break;
            }
        }
        
        if (field != null) {
        	field.setAccessible(true);
        }
        return field;
	}
	
	public Object getFieldValue(Object bean, String fieldName) throws IllegalAccessException {
		StringTokenizer tokens = new StringTokenizer(fieldName, ".");
        Class<?> componentClass = bean.getClass();
        Field field = null;
        Object value = bean;

        while (tokens.hasMoreTokens()) {
            field = getSingleField(componentClass, tokens.nextToken());
            field.setAccessible(true);
            value = field.get(value);
            if (value != null) {
                componentClass = value.getClass();
            } else {
            	break;
            }
        }

        return value;

    }
	
	public Object setFieldValue(Object bean, String fieldName, Object newValue) throws IllegalAccessException, InstantiationException {
		StringTokenizer tokens = new StringTokenizer(fieldName, ".");
        Class<?> componentClass = bean.getClass();
        Field field = null;
        Object value = bean;
        
        int count = tokens.countTokens();
        int j=0;
        while (tokens.hasMoreTokens()) {
            field = getSingleField(componentClass, tokens.nextToken());
            field.setAccessible(true);
            if (j == count - 1) {
            	field.set(value, newValue);
            } else {
	            Object myValue = field.get(value);
	            if (myValue != null) {
	                componentClass = myValue.getClass();
	                value = myValue;
	            } else {
	            	//consult the entity configuration manager to see if there is a user
	            	//configured entity for this class
					try {
						Object newEntity = entityConfiguration.createEntityInstance(field.getType().getName());
						SortableValue val = new SortableValue((Serializable) newEntity, j);
						middleFields.add(val);
	            		field.set(value, newEntity);
	            		componentClass = newEntity.getClass();
	            		value = newEntity;
					} catch (Exception e) {
						//Use the most extended type based on the field type
	            		Class<?>[] entities = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(field.getType());
                        if (!ArrayUtils.isEmpty(entities)) {
                            Object newEntity = entities[0].newInstance();
                            SortableValue val = new SortableValue((Serializable) newEntity, j);
                            middleFields.add(val);
                            field.set(value, newEntity);
                            componentClass = newEntity.getClass();
                            value = newEntity;
                            LOG.info("Unable to find a reference to ("+field.getType().getName()+") in the EntityConfigurationManager. Using the most extended form of this class identified as ("+entities[0].getName()+")");
                        } else {
                            //Just use the field type
                            Object newEntity = field.getType().newInstance();
                            field.set(value, newEntity);
                            componentClass = newEntity.getClass();
                            value = newEntity;
                            LOG.info("Unable to find a reference to ("+field.getType().getName()+") in the EntityConfigurationManager. Using the type of this class.");
                        }
					}
	            }
            }
            j++;
        }
        
        return value;

    }
	
	public void persistMiddleEntities() {
		Collections.sort(middleFields);
		for (SortableValue val : middleFields) {
			dynamicEntityDao.persist(val.entity);
		}
	}

	public EntityConfiguration getEntityConfiguration() {
		return entityConfiguration;
	}
	
	private class SortableValue implements Comparable<SortableValue> {
		
		private Integer pos;
		private Serializable entity;
		private Class<?> entityClass;
		
		public SortableValue(Serializable entity, Integer pos) {
			this.entity = entity;
			this.pos = pos;
			this.entityClass = entity.getClass();
		}

		public int compareTo(SortableValue o) {
			return pos.compareTo(o.pos) * -1;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (entityClass == null ? 0 : entityClass.hashCode());
			result = prime * result + (pos == null ? 0 : pos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SortableValue other = (SortableValue) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (entityClass == null) {
				if (other.entityClass != null)
					return false;
			} else if (!entityClass.equals(other.entityClass))
				return false;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			return true;
		}

		private FieldManager getOuterType() {
			return FieldManager.this;
		}

	}
	
}
