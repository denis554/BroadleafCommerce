package org.broadleafcommerce.admin.server.service.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.core.search.domain.SearchFacet;
import org.broadleafcommerce.core.search.service.CategorySearchFacetService;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.ValidationException;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * When deleting a {@link SearchFacet}, there needs to be a check to see if it is applied to a
 * {@link org.broadleafcommerce.core.catalog.domain.Category} already. If so, then trying to removed it should cause a
 * validation error letting the user know that the {@link SearchFacet} cannot be deleted until its reference is removed.
 * Otherwise, a Hibernate exception will be displayed to users, which will not contain meaningful information for them.
 *
 * @author Nathan Moore (nathandmoore)
 */
@Component("blSearchFacetCustomPersistenceHandler")
public class SearchFacetCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(SearchFacetCustomPersistenceHandler.class);

    @Resource(name = "blCategorySearchFacetService")
    protected CategorySearchFacetService csfService;

    @Override
    public Boolean canHandleRemove(PersistencePackage persistencePackage) {
        return persistencePackage.getCeilingEntityFullyQualifiedClassname() != null
                && persistencePackage.getCeilingEntityFullyQualifiedClassname().equals(SearchFacet.class.getName());
    }

    @Override
    public void remove(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper)
            throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(SearchFacet.class.getName(), persistencePerspective);
            Object primaryKey = helper.getPrimaryKey(entity, adminProperties);
            SearchFacet adminInstance = (SearchFacet) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);
            adminInstance = (SearchFacet) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);
            adminInstance = dynamicEntityDao.merge(adminInstance);

            if(csfService.readCategorySearchFacetsBySearchFacet(adminInstance.getId()).size() > 0) {
                // check if search facet is in use by a category (i.e., is it a CategorySearchFacet?)
                entity.addGlobalValidationError("Cannot delete this SearchFacet because it is referenced by a Category");
                throw new ValidationException(entity, "SearchFacet_error_inUse");
            } else {
                // if not, remove like normal
                // check if there is a removeType specified
                OperationType opType = persistencePackage.getPersistencePerspective().getOperationTypes().getRemoveType();
                if (opType != null) {
                    helper.getCompatibleModule(opType).remove(persistencePackage);
                } else {
                    helper.getCompatibleModule(OperationType.BASIC).remove(persistencePackage);
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to remove entity for " + entity.getType()[0], e);
        }
    }
}
