package org.broadleafcommerce.openadmin.server.service.persistence;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import org.broadleafcommerce.openadmin.client.dto.*;
import org.broadleafcommerce.openadmin.client.service.ServiceException;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface PersistenceManager {

	public abstract Class<?>[] getAllPolymorphicEntitiesFromCeiling(Class<?> ceilingClass);

	public abstract Class<?>[] getPolymorphicEntities(String ceilingEntityFullyQualifiedClassname) throws ClassNotFoundException;

	public abstract Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective, DynamicEntityDao dynamicEntityDao, Class<?>[] entityClasses) throws ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

	public abstract ClassMetadata getMergedClassMetadata(final Class<?>[] entities, Map<MergedPropertyType, Map<String, FieldMetadata>> mergedProperties) throws ClassNotFoundException, IllegalArgumentException;

	public abstract DynamicResultSet inspect(PersistencePackage persistencePackage, Map<String, FieldMetadata> metadataOverrides) throws ServiceException, ClassNotFoundException;

	public abstract DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException;

	public abstract Entity add(PersistencePackage persistencePackage) throws ServiceException;

	public abstract Entity update(PersistencePackage persistencePackage) throws ServiceException;

	public abstract void remove(PersistencePackage persistencePackage) throws ServiceException;

	public abstract SandBoxService getSandBoxService();

	public abstract void setSandBoxService(SandBoxService sandBoxService);

	public abstract DynamicEntityDao getDynamicEntityDao();

	public abstract void setDynamicEntityDao(DynamicEntityDao dynamicEntityDao);

	public abstract Map<TargetModeType, String> getTargetEntityManagers();

	public abstract void setTargetEntityManagers(Map<TargetModeType, String> targetEntityManagers);

	public abstract TargetModeType getTargetMode();

	public abstract void setTargetMode(TargetModeType targetMode);

	public abstract List<CustomPersistenceHandler> getCustomPersistenceHandlers();

	public abstract void setCustomPersistenceHandlers(List<CustomPersistenceHandler> customPersistenceHandlers);

	public abstract void close() throws Exception;

    public Entity updateDirtyState(Entity mergedEntity) throws ServiceException;

}