package org.broadleafcommerce.gwt.client.datasource.order;

import org.broadleafcommerce.gwt.client.datasource.CeilingEntities;
import org.broadleafcommerce.gwt.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.BasicEntityModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationTypes;
import org.broadleafcommerce.gwt.client.service.AppServices;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;

public class FulfillmentGroupListDataSourceFactory {

	public static final String foreignKeyName = "order";
	public static ListGridDataSource dataSource = null;
	
	public static void createDataSource(String name, AsyncCallback<DataSource> cb) {
		if (dataSource == null) {
			OperationTypes operationTypes = new OperationTypes(OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY);
			PersistencePerspective persistencePerspective = new PersistencePerspective(operationTypes, new String[]{}, new ForeignKey[]{new ForeignKey("address.state", EntityImplementations.STATE, null), new ForeignKey("address.country", EntityImplementations.COUNTRY, null)});
			persistencePerspective.addPersistencePerspectiveItem(PersistencePerspectiveItemType.FOREIGNKEY, new ForeignKey(foreignKeyName, EntityImplementations.ORDER, null));
			persistencePerspective.setPopulateManyToOneFields(true);
			persistencePerspective.setExcludeFields(
				new String[]{
					"order", 
					"address.state",
					"address.country"
				}
			);
			DataSourceModule[] modules = new DataSourceModule[]{
				new BasicEntityModule(CeilingEntities.FULFILLMENT_GROUP, persistencePerspective, AppServices.DYNAMIC_ENTITY)
			};
			dataSource = new ListGridDataSource(CeilingEntities.FULFILLMENT_GROUP, name, persistencePerspective, AppServices.DYNAMIC_ENTITY, modules);
			dataSource.buildFields(null, cb);
		} else {
			if (cb != null) {
				cb.onSuccess(dataSource);
			}
		}
	}

}
