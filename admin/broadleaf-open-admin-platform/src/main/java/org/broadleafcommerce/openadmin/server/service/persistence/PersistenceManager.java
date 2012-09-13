/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.service.persistence;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;

import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface PersistenceManager {

	public abstract Class<?>[] getAllPolymorphicEntitiesFromCeiling(Class<?> ceilingClass);

	public abstract Class<?>[] getPolymorphicEntities(String ceilingEntityFullyQualifiedClassname) throws ClassNotFoundException;

	public abstract Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective) throws ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException;

	public abstract ClassMetadata getMergedClassMetadata(Class<?>[] entities, Map<MergedPropertyType, Map<String, FieldMetadata>> mergedProperties) throws ClassNotFoundException, IllegalArgumentException;

	public abstract DynamicResultSet inspect(PersistencePackage persistencePackage) throws ServiceException, ClassNotFoundException;

	public abstract DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException;

	public abstract Entity add(PersistencePackage persistencePackage) throws ServiceException;

	public abstract Entity update(PersistencePackage persistencePackage) throws ServiceException;

	public abstract void remove(PersistencePackage persistencePackage) throws ServiceException;

	public abstract DynamicEntityDao getDynamicEntityDao();

	public abstract void setDynamicEntityDao(DynamicEntityDao dynamicEntityDao);

	public abstract Map<String, String> getTargetEntityManagers();

	public abstract void setTargetEntityManagers(Map<String, String> targetEntityManagers);

	public abstract TargetModeType getTargetMode();

	public abstract void setTargetMode(TargetModeType targetMode);

	public abstract List<CustomPersistenceHandler> getCustomPersistenceHandlers();

	public abstract void setCustomPersistenceHandlers(List<CustomPersistenceHandler> customPersistenceHandlers);

    public abstract Class<?>[] getUpDownInheritance(Class<?> testClass);

    public abstract Class<?>[] getUpDownInheritance(String testClassname) throws ClassNotFoundException;

	//public abstract void close() throws Exception;

}