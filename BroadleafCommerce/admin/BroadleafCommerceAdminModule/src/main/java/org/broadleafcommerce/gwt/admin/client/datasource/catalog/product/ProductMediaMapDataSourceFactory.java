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
package org.broadleafcommerce.gwt.admin.client.datasource.catalog.product;

import java.util.LinkedHashMap;

import org.broadleafcommerce.gwt.admin.client.datasource.CeilingEntities;
import org.broadleafcommerce.gwt.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.admin.client.presenter.catalog.product.OneToOneProductSkuPresenter;
import org.broadleafcommerce.gwt.client.datasource.DataSourceFactory;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ComplexValueMapStructureDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.MapStructureModule;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.MapStructure;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationTypes;
import org.broadleafcommerce.gwt.client.service.AppServices;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;

/**
 * 
 * @author jfischer
 *
 */
public class ProductMediaMapDataSourceFactory implements DataSourceFactory {
	
	public static final MapStructure MAPSTRUCTURE = new MapStructure(String.class.getName(), "key", "Key", EntityImplementations.MEDIA, "productMedia", true);
	public static ComplexValueMapStructureDataSource dataSource = null;
	
	protected OneToOneProductSkuPresenter presenter;
	
	public ProductMediaMapDataSourceFactory(OneToOneProductSkuPresenter presenter) {
		this.presenter = presenter;
	}
	
	@SuppressWarnings("unchecked")
	public void createDataSource(String name, OperationTypes operationTypes, Object[] additionalItems, AsyncCallback<DataSource> cb) {
		if (dataSource == null) {
			operationTypes = new OperationTypes(OperationType.MAPSTRUCTURE, OperationType.MAPSTRUCTURE, OperationType.MAPSTRUCTURE, OperationType.MAPSTRUCTURE, OperationType.MAPSTRUCTURE);
			PersistencePerspective persistencePerspective = new PersistencePerspective(operationTypes, new String[]{}, new ForeignKey[]{});
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, new ForeignKey("id", EntityImplementations.PRODUCT, null));
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.MAPSTRUCTURE, MAPSTRUCTURE);
			DataSourceModule[] modules = new DataSourceModule[]{
				new MapStructureModule(CeilingEntities.PRODUCT, persistencePerspective, AppServices.DYNAMIC_ENTITY, presenter.getDisplay().getMediaDisplay().getGrid())
			};
			dataSource = new ComplexValueMapStructureDataSource(name, persistencePerspective, AppServices.DYNAMIC_ENTITY, modules, (LinkedHashMap<String, String>) additionalItems[0]);
			dataSource.buildFields(null, false, cb);
		} else {
			if (cb != null) {
				cb.onSuccess(dataSource);
			}
		}
	}

}
