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
package org.broadleafcommerce.gwt.admin.client.datasource.catalog.category;

import org.broadleafcommerce.gwt.admin.client.datasource.CeilingEntities;
import org.broadleafcommerce.gwt.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.client.datasource.DataSourceFactory;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.BasicEntityModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.JoinStructureModule;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.JoinStructure;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationTypes;
import org.broadleafcommerce.gwt.client.service.AppServices;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;

/**
 * 
 * @author jfischer
 *
 */
public class AllProductsDataSourceFactory implements DataSourceFactory {

	public static final String symbolName = "allChildCategories";
	public static final String linkedObjectPath = "categoryProductXref.category";
	public static final String linkedIdProperty = "id";
	public static final String targetObjectPath = "categoryProductXref.product";
	public static final String targetIdProperty = "id";
	public static final String sortField = "displayOrder";
	public static ListGridDataSource dataSource = null;
	
	public void createDataSource(String name, OperationTypes operationTypes, Object[] additionalItems, AsyncCallback<DataSource> cb) {
		if (dataSource == null) {
			PersistencePerspective persistencePerspective = new PersistencePerspective(operationTypes, new String[]{}, new ForeignKey[]{});
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.JOINSTRUCTURE, new JoinStructure(symbolName, linkedObjectPath, linkedIdProperty, targetObjectPath, targetIdProperty, EntityImplementations.CATEGORY_PRODUCT, sortField, true));
			DataSourceModule[] modules = new DataSourceModule[]{
				new BasicEntityModule(CeilingEntities.PRODUCT, persistencePerspective, AppServices.DYNAMIC_ENTITY),
				new JoinStructureModule(CeilingEntities.PRODUCT, persistencePerspective, AppServices.DYNAMIC_ENTITY)
			};
			dataSource = new ListGridDataSource(name, persistencePerspective, AppServices.DYNAMIC_ENTITY, modules);
			dataSource.buildFields(null, false, cb);
		} else {
			if (cb != null) {
				cb.onSuccess(dataSource);
			}
		}
	}

	/*public void createDataSource(String name, AsyncCallback<DataSource> cb) {
		OperationTypes operationTypes = new OperationTypes(OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.JOINSTRUCTURE, OperationType.ENTITY);
		createDataSource(name, operationTypes, cb);
	}
	
	public void createDataSource(String name, OperationTypes operationTypes, AsyncCallback<DataSource> cb) {
		
	}*/

}
