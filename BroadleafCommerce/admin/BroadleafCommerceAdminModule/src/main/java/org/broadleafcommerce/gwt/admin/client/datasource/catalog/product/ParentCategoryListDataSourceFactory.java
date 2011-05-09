package org.broadleafcommerce.gwt.admin.client.datasource.catalog.product;

import org.broadleafcommerce.gwt.admin.client.datasource.CeilingEntities;
import org.broadleafcommerce.gwt.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.BasicEntityModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.JoinStructureModule;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.JoinStructure;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationTypes;
import org.broadleafcommerce.gwt.client.service.AppServices;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;

public class ParentCategoryListDataSourceFactory {

	public static final String defaultCategoryForeignKey = "defaultCategory";
	public static final String symbolName = "allParentCategories";
	public static final String linkedObjectPath = "categoryProductXref.product";
	public static final String linkedIdProperty = "id";
	public static final String targetObjectPath = "categoryProductXref.category";
	public static final String targetIdProperty = "id";
	public static final String sortField = "displayOrder";
	public static ListGridDataSource dataSource = null;
	
	public static void createDataSource(String name, AsyncCallback<DataSource> cb) {
		OperationTypes operationTypes = new OperationTypes(OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.ENTITY);
		createDataSource(name, operationTypes, cb);
	}
	
	public static void createDataSource(String name, OperationTypes operationTypes, AsyncCallback<DataSource> cb) {
		if (dataSource == null) {
			PersistencePerspective persistencePerspective = new PersistencePerspective(operationTypes, new String[]{}, new ForeignKey[]{new ForeignKey(defaultCategoryForeignKey, EntityImplementations.CATEGORY, null)});
			JoinStructure joinStructure = new JoinStructure(symbolName, linkedObjectPath, linkedIdProperty, targetObjectPath, targetIdProperty, EntityImplementations.CATEGORY_PRODUCT, sortField, true);
			joinStructure.setInverse(true);
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.JOINSTRUCTURE, joinStructure);
			DataSourceModule[] modules = new DataSourceModule[]{
				new BasicEntityModule(CeilingEntities.CATEGORY, persistencePerspective, AppServices.DYNAMIC_ENTITY),
				new JoinStructureModule(CeilingEntities.CATEGORY, persistencePerspective, AppServices.DYNAMIC_ENTITY)
			};
			dataSource = new ListGridDataSource(name, persistencePerspective, AppServices.DYNAMIC_ENTITY, modules);
			dataSource.buildFields(null, false, cb);
		} else {
			if (cb != null) {
				cb.onSuccess(dataSource);
			}
		}
	}

}
