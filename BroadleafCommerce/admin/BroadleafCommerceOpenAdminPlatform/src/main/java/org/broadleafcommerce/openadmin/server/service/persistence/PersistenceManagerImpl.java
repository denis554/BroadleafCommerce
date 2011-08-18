package org.broadleafcommerce.openadmin.server.service.persistence;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.openadmin.client.dto.*;
import org.broadleafcommerce.openadmin.client.service.ServiceException;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.domain.SandBoxItem;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandler;
import org.broadleafcommerce.openadmin.server.service.persistence.entitymanager.pool.SandBoxEntityManagerPoolFactoryBean;
import org.broadleafcommerce.openadmin.server.service.persistence.module.InspectHelper;
import org.broadleafcommerce.openadmin.server.service.persistence.module.PersistenceModule;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PersistenceManagerImpl implements InspectHelper, PersistenceManager, ApplicationContextAware {

	private static final Log LOG = LogFactory.getLog(PersistenceManagerImpl.class);

	protected SandBoxService sandBoxService;
	protected DynamicEntityDao dynamicEntityDao;
	protected List<CustomPersistenceHandler> customPersistenceHandlers = new ArrayList<CustomPersistenceHandler>();
	protected PersistenceModule[] modules;
	protected Map<TargetModeType, String> targetEntityManagers = new HashMap<TargetModeType, String>();
	protected TargetModeType targetMode;
	private ApplicationContext applicationContext;

	public PersistenceManagerImpl(PersistenceModule[] modules) {
		this.modules = modules;
		for (PersistenceModule module : modules) {
			module.setPersistenceManager(this);
		}
	}

	public void close() throws Exception {
		Object temp = applicationContext.getBean("&" + targetEntityManagers.get(targetMode));
		if (temp instanceof SandBoxEntityManagerPoolFactoryBean) {
			((SandBoxEntityManagerPoolFactoryBean) temp).returnObject(dynamicEntityDao.getStandardEntityManager());
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getAllPolymorphicEntitiesFromCeiling(java.lang.Class)
	 */
	@Override
	public Class<?>[] getAllPolymorphicEntitiesFromCeiling(Class<?> ceilingClass) {
		return dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(ceilingClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getPolymorphicEntities(java.lang.String)
	 */
	@Override
	public Class<?>[] getPolymorphicEntities(String ceilingEntityFullyQualifiedClassname) throws ClassNotFoundException {
		Class<?>[] entities = getAllPolymorphicEntitiesFromCeiling(Class.forName(ceilingEntityFullyQualifiedClassname));
		return entities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getSimpleMergedProperties(java.lang.String,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao,
	 * java.lang.Class)
	 */
	@Override
	public Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective, DynamicEntityDao dynamicEntityDao, Class<?>[] entityClasses) throws ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return dynamicEntityDao.getSimpleMergedProperties(entityName, persistencePerspective, dynamicEntityDao, entityClasses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getMergedClassMetadata(java.lang.Class, java.util.Map)
	 */
	@Override
	public ClassMetadata getMergedClassMetadata(final Class<?>[] entities, Map<MergedPropertyType, Map<String, FieldMetadata>> mergedProperties) throws ClassNotFoundException, IllegalArgumentException {
		ClassMetadata classMetadata = new ClassMetadata();
		PolymorphicEntity[] polyEntities = new PolymorphicEntity[entities.length];
		int j = 0;
		for (Class<?> type : entities) {
			polyEntities[j] = new PolymorphicEntity();
			polyEntities[j].setType(type.getName());
			polyEntities[j].setName(type.getSimpleName());
			j++;
		}
		classMetadata.setPolymorphicEntities(polyEntities);

		List<Property> propertiesList = new ArrayList<Property>();
		for (PersistenceModule module : modules) {
			module.extractProperties(mergedProperties, propertiesList);
		}
		/*
		 * Insert inherited fields whose order has been specified
		 */
		for (int i = 0; i < entities.length - 1; i++) {
			for (Property myProperty : propertiesList) {
				if (myProperty.getMetadata().getInheritedFromType().equals(entities[i].getName()) && myProperty.getMetadata().getPresentationAttributes().getOrder() != null) {
					for (Property property : propertiesList) {
						if (!property.getMetadata().getInheritedFromType().equals(entities[i].getName()) && property.getMetadata().getPresentationAttributes().getOrder() != null && property.getMetadata().getPresentationAttributes().getOrder() >= myProperty.getMetadata().getPresentationAttributes().getOrder()) {
							property.getMetadata().getPresentationAttributes().setOrder(property.getMetadata().getPresentationAttributes().getOrder() + 1);
						}
					}
				}
			}
		}
		Property[] properties = new Property[propertiesList.size()];
		properties = propertiesList.toArray(properties);
		Arrays.sort(properties, new Comparator<Property>() {
			public int compare(Property o1, Property o2) {
				/*
				 * First, compare properties based on order fields
				 */
				if (o1.getMetadata().getPresentationAttributes().getOrder() != null && o2.getMetadata().getPresentationAttributes().getOrder() != null) {
					return o1.getMetadata().getPresentationAttributes().getOrder().compareTo(o2.getMetadata().getPresentationAttributes().getOrder());
				} else if (o1.getMetadata().getPresentationAttributes().getOrder() != null && o2.getMetadata().getPresentationAttributes().getOrder() == null) {
					/*
					 * Always favor fields that have an order identified
					 */
					return -1;
				} else if (o1.getMetadata().getPresentationAttributes().getOrder() == null && o2.getMetadata().getPresentationAttributes().getOrder() != null) {
					/*
					 * Always favor fields that have an order identified
					 */
					return 1;
				} else if (o1.getMetadata().getPresentationAttributes().getFriendlyName() != null && o2.getMetadata().getPresentationAttributes().getFriendlyName() != null) {
					return o1.getMetadata().getPresentationAttributes().getFriendlyName().compareTo(o2.getMetadata().getPresentationAttributes().getFriendlyName());
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			}
		});
		classMetadata.setProperties(properties);

		return classMetadata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #inspect(java.lang.String,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * java.lang.String[], java.util.Map)
	 */
	@Override
	public DynamicResultSet inspect(PersistencePackage persistencePackage, Map<String, FieldMetadata> metadataOverrides) throws ServiceException, ClassNotFoundException {
		// check to see if there is a custom handler registered
		for (CustomPersistenceHandler handler : customPersistenceHandlers) {
			if (handler.canHandleInspect(persistencePackage)) {
				DynamicResultSet results = handler.inspect(persistencePackage, metadataOverrides, dynamicEntityDao, this);

				return results;
			}
		}

		Class<?>[] entities = getPolymorphicEntities(persistencePackage.getCeilingEntityFullyQualifiedClassname());
		Map<MergedPropertyType, Map<String, FieldMetadata>> allMergedProperties = new HashMap<MergedPropertyType, Map<String, FieldMetadata>>();
		for (PersistenceModule module : modules) {
			module.updateMergedProperties(persistencePackage, allMergedProperties, metadataOverrides);
		}
		ClassMetadata mergedMetadata = getMergedClassMetadata(entities, allMergedProperties);

		DynamicResultSet results = new DynamicResultSet(mergedMetadata, null, null);

		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #fetch(java.lang.String,
	 * com.anasoft.os.daofusion.cto.client.CriteriaTransferObject,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * java.lang.String[])
	 */
	@Override
	public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException {
		PersistenceModule myModule = getCompatibleModule(persistencePackage.getPersistencePerspective().getOperationTypes().getFetchType());
		return myModule.fetch(persistencePackage, cto);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #add(java.lang.String, org.broadleafcommerce.openadmin.client.dto.Entity,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * java.lang.String[])
	 */
	@Override
	public Entity add(PersistencePackage persistencePackage) throws ServiceException {
		PersistenceModule myModule = getCompatibleModule(persistencePackage.getPersistencePerspective().getOperationTypes().getAddType());
		return myModule.add(persistencePackage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #update(org.broadleafcommerce.openadmin.client.dto.Entity,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * org.broadleafcommerce.openadmin.client.dto.SandBoxInfo,
	 * java.lang.String[])
	 */
	@Override
	public Entity update(PersistencePackage persistencePackage) throws ServiceException {
        PersistenceModule myModule = getCompatibleModule(persistencePackage.getPersistencePerspective().getOperationTypes().getUpdateType());
        /*PersistencePackage savedPackage = null;
        if (!persistencePackage.getSandBoxInfo().isCommitImmediately()) {
            try {
                savedPackage = sandBoxService.saveSandBox(persistencePackage, ChangeType.UPDATE, this, (RecordHelper) myModule);
            } catch (SandBoxException e) {
                throw new ServiceException("Unable to update entity to the sandbox: " + persistencePackage.getSandBoxInfo().getSandBox(), e);
            }
        } else {
            savedPackage = persistencePackage;
        }

        Entity mergedEntity = myModule.update(savedPackage);*/
        Entity mergedEntity = myModule.update(persistencePackage);
        //return updateDirtyState(mergedEntity);
        return mergedEntity;
    }

    @Override
    public Entity updateDirtyState(Entity mergedEntity) throws ServiceException {
        try {
            Map idMetadata = dynamicEntityDao.getIdMetadata(Class.forName(mergedEntity.getType()[0]));
            Type idType = (Type) idMetadata.get("type");
            Object id = mergedEntity.findProperty((String) idMetadata.get("name")).getValue();
            if (Long.class.isAssignableFrom(idType.getReturnedClass())) {
                id = Long.valueOf(id.toString());
            }
            SandBoxItem item = sandBoxService.retrieveSandBoxItemByTemporaryId(id);
            if (item != null) {
                mergedEntity.setDirty(true);
                for (org.broadleafcommerce.openadmin.server.domain.Property persistentProperty : item.getEntity().getProperties()) {
                    if (persistentProperty.getIsDirty()) {
                        Property dtoProperty = mergedEntity.findProperty(persistentProperty.getName());
                        dtoProperty.setIsDirty(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("Unable to evaluate the dirty state for entity: " + mergedEntity.getType()[0], e);
        }

        return mergedEntity;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #remove(org.broadleafcommerce.openadmin.client.dto.Entity,
	 * org.broadleafcommerce.openadmin.client.dto.PersistencePerspective,
	 * java.lang.String[])
	 */
	@Override
	public void remove(PersistencePackage persistencePackage) throws ServiceException {
		PersistenceModule myModule = getCompatibleModule(persistencePackage.getPersistencePerspective().getOperationTypes().getRemoveType());
		myModule.remove(persistencePackage);
	}

	protected PersistenceModule getCompatibleModule(OperationType operationType) {
		PersistenceModule myModule = null;
		for (PersistenceModule module : modules) {
			if (module.isCompatible(operationType)) {
				myModule = module;
				break;
			}
		}
		if (myModule == null) {
			LOG.error("Unable to find a compatible remote service module for the operation type: " + operationType);
			throw new RuntimeException("Unable to find a compatible remote service module for the operation type: " + operationType);
		}

		return myModule;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getSandBoxService()
	 */
	@Override
	public SandBoxService getSandBoxService() {
		return sandBoxService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #setSandBoxService(org.broadleafcommerce.openadmin.server.service.
	 * SandBoxService)
	 */
	@Override
	public void setSandBoxService(SandBoxService sandBoxService) {
		this.sandBoxService = sandBoxService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getDynamicEntityDao()
	 */
	@Override
	public DynamicEntityDao getDynamicEntityDao() {
		return dynamicEntityDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #setDynamicEntityDao(org.broadleafcommerce.openadmin.server.dao.
	 * DynamicEntityDao)
	 */
	@Override
	public void setDynamicEntityDao(DynamicEntityDao dynamicEntityDao) {
		this.dynamicEntityDao = dynamicEntityDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getTargetEntityManagers()
	 */
	@Override
	public Map<TargetModeType, String> getTargetEntityManagers() {
		return targetEntityManagers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #setTargetEntityManagers(java.util.Map)
	 */
	@Override
	public void setTargetEntityManagers(Map<TargetModeType, String> targetEntityManagers) {
		this.targetEntityManagers = targetEntityManagers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getTargetMode()
	 */
	@Override
	public TargetModeType getTargetMode() {
		return targetMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #setTargetMode(java.lang.String)
	 */
	@Override
	public void setTargetMode(TargetModeType targetMode) {
		String targetManagerRef = targetEntityManagers.get(targetMode);
		EntityManager targetManager = (EntityManager) applicationContext.getBean(targetManagerRef);
		if (targetManager == null) {
			throw new RuntimeException("Unable to find a target entity manager registered with the key: " + targetMode + ". Did you add an entity manager with this key to the targetEntityManagers property?");
		}
		dynamicEntityDao.setStandardEntityManager(targetManager);
		this.targetMode = targetMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #getCustomPersistenceHandlers()
	 */
	@Override
	public List<CustomPersistenceHandler> getCustomPersistenceHandlers() {
		return customPersistenceHandlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.broadleafcommerce.openadmin.server.service.persistence.PersistenceManager
	 * #setCustomPersistenceHandlers(java.util.List)
	 */
	@Override
	public void setCustomPersistenceHandlers(List<CustomPersistenceHandler> customPersistenceHandlers) {
		this.customPersistenceHandlers = customPersistenceHandlers;
	}
}
