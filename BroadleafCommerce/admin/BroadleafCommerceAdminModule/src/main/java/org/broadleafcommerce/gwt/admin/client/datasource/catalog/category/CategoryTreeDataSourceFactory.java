package org.broadleafcommerce.gwt.admin.client.datasource.catalog.category;

import org.broadleafcommerce.gwt.admin.client.datasource.CeilingEntities;
import org.broadleafcommerce.gwt.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.admin.client.datasource.catalog.category.module.CategoryTreeEntityModule;
import org.broadleafcommerce.gwt.admin.client.datasource.catalog.category.module.CategoryTreeJoinStructureModule;
import org.broadleafcommerce.gwt.client.datasource.DataSourceFactory;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.JoinStructure;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationTypes;
import org.broadleafcommerce.gwt.client.service.AppServices;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;

public class CategoryTreeDataSourceFactory implements DataSourceFactory {

	public static final String hasChildrenProperty = "hasAllChildCategories";
	public static final String foreignKeyName = "allParentCategories";
	public static final String defaultParentCategoryForeignKey = "defaultParentCategory";
	public static CategoryTreeDataSource dataSource = null;

	public void createDataSource(String name, OperationTypes operationTypes, Object[] additionalItems, AsyncCallback<DataSource> cb) {
		if (dataSource == null) {
			operationTypes = new OperationTypes(OperationType.JOINSTRUCTURE, OperationType.FOREIGNKEY, OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY);
			PersistencePerspective persistencePerspective = new PersistencePerspective(operationTypes, new String[] {hasChildrenProperty}, new ForeignKey[]{new ForeignKey(defaultParentCategoryForeignKey, EntityImplementations.CATEGORY, null)});
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, new ForeignKey(foreignKeyName, EntityImplementations.CATEGORY, null));
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.JOINSTRUCTURE, new JoinStructure(foreignKeyName, "categoryXrefPK.category", "id", "categoryXrefPK.subCategory", "id", EntityImplementations.CATEGORY_XREF, "displayOrder", true));
			DataSourceModule[] modules = new DataSourceModule[]{
				new CategoryTreeEntityModule(CeilingEntities.CATEGORY, persistencePerspective, AppServices.DYNAMIC_ENTITY),
				new CategoryTreeJoinStructureModule(CeilingEntities.CATEGORY, persistencePerspective, AppServices.DYNAMIC_ENTITY)
			};
			dataSource = new CategoryTreeDataSource(name, persistencePerspective, AppServices.DYNAMIC_ENTITY, modules, (String) additionalItems[0], (String) additionalItems[1]);
			dataSource.buildFields(null, false, cb);
		} else {
			if (cb != null) {
				cb.onSuccess(dataSource);
			}
		}
	}

}
