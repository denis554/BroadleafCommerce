package org.broadleafcommerce.admin.server.service.handler;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.broadleafcommerce.admin.client.datasource.catalog.category.CategoryListDataSourceFactory;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.JoinStructure;
import org.broadleafcommerce.openadmin.client.dto.OperationType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspectiveItemType;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;

/**
 * @author Jeff Fischer
 */
public class ChildCategoriesCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        return (!ArrayUtils.isEmpty(persistencePackage.getCustomCriteria()) && persistencePackage.getCustomCriteria()[0].equals(CategoryListDataSourceFactory.customCriteria));
    }

    @Override
    public Entity add(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        JoinStructure joinStructure = (JoinStructure) persistencePackage.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.JOINSTRUCTURE);
        String targetPath = joinStructure.getTargetObjectPath() + "." + joinStructure.getTargetIdProperty();
        String linkedPath = joinStructure.getLinkedObjectPath() + "." + joinStructure.getLinkedIdProperty();
        
        Long parentId = Long.parseLong(persistencePackage.getEntity().findProperty(linkedPath).getValue());
        Long childId = Long.parseLong(persistencePackage.getEntity().findProperty(targetPath).getValue());
        
        Category parent = (Category) dynamicEntityDao.retrieve(CategoryImpl.class, parentId);
        Category child = (Category) dynamicEntityDao.retrieve(CategoryImpl.class, childId);
        
        if (parent.getAllChildCategories().contains(child)) {
            throw new ServiceException("Add unsuccessful. Cannot add a duplicate child category.");
        }

        checkParents(child, parent);
        
        return helper.getCompatibleModule(OperationType.JOINSTRUCTURE).add(persistencePackage);
    }
    
    protected void checkParents(Category child, Category parent) throws ServiceException {
        if (child.getId().equals(parent.getId())) {
            throw new ServiceException("Add unsuccessful. Cannot add a category to itself.");
        }
        for (Category category : parent.getAllParentCategories()) {
            if (!CollectionUtils.isEmpty(category.getAllParentCategories())) {
                checkParents(child, category);
            }
        }
    }
 }
