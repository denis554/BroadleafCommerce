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

import org.broadleafcommerce.gwt.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.gwt.client.BLCMain;
import org.broadleafcommerce.gwt.client.datasource.dynamic.TreeGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.module.DataSourceModule;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.EntityOperationType;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.EntityServiceAsyncCallback;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.results.Entity;
import org.broadleafcommerce.gwt.client.service.DynamicEntityServiceAsync;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.google.gwt.core.client.JavaScriptObject;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * 
 * @author jfischer
 *
 */
public class CategoryTreeDataSource extends TreeGridDataSource {

	/**
	 * @param name
	 * @param persistencePerspective
	 * @param service
	 * @param modules
	 * @param rootId
	 * @param rootName
	 */
	public CategoryTreeDataSource(String name, PersistencePerspective persistencePerspective, DynamicEntityServiceAsync service, DataSourceModule[] modules, String rootId, String rootName) {
		super(name, persistencePerspective, service, modules, rootId, rootName);
	}

	@Override
	protected void executeFetch(final String requestId, final DSRequest request, final DSResponse response) {
		CriteriaTransferObject criteriaTransferObject = getCompatibleModule(OperationType.ENTITY).getCto(request);
		String parentCategoryId = criteriaTransferObject.get(CategoryTreeDataSourceFactory.foreignKeyName).getFilterValues()[0];
		boolean hasChildren = true;
		if (parentCategoryId != null) {
			TreeNode parentNode = ((TreeGrid) associatedGrid).getTree().findById(parentCategoryId);
			if (parentNode != null) {
				hasChildren = Boolean.parseBoolean(parentNode.getAttribute(CategoryTreeDataSourceFactory.hasChildrenProperty));
			}
		}
		/*
		 * Allows us to do a quick fetch that does not go back to the server. This is for
		 * cosmetic purposes in the tree view. By quickly retrieving a zero record list for
		 * tree nodes, we can immediately update the tree display for those nodes and cause the removal
		 * of their expand GUI element. 
		 */
        if (hasChildren && parentCategoryId != null) {
        	super.executeFetch(requestId, request, response);
        } else if (parentCategoryId == null) {
        	TreeNode node = new TreeNode();
        	node.setAttribute(getPrimaryKeyFieldName(), getRootId());
        	node.setAttribute("name", getRootName());
        	node.setAttribute(CategoryTreeDataSourceFactory.hasChildrenProperty, String.valueOf(hasChildren));
        	node.setAttribute("_type", new String[] {EntityImplementations.CATEGORY});
        	TreeNode[] recordList = new TreeNode[]{node};
        	response.setData(recordList);
        	response.setTotalRows(0);
        	processResponse(requestId, response);
        } else {
        	TreeNode[] recordList = new TreeNode[]{};
			response.setData(recordList);
			response.setTotalRows(0);
			processResponse(requestId, response);
        }
	}
	
	@Override
	protected void executeAdd(final String requestId, final DSRequest request, final DSResponse response) {
		BLCMain.NON_MODAL_PROGRESS.startProgress();
		setLinkedValue(getPrimaryKeyValue(getAssociatedGrid().getSelectedRecord()));
		JavaScriptObject data = request.getData();
        final TreeNode newRecord = new TreeNode(data);
        persistencePerspective.getOperationTypes().setAddType(OperationType.ENTITY);
        final DataSourceModule entityModule = getCompatibleModule(OperationType.ENTITY);
    	Entity entity = entityModule.buildEntity(newRecord);
    	//Add the new category entity
		service.add(entityModule.getCeilingEntityFullyQualifiedClassname(), entity, persistencePerspective, null, new EntityServiceAsyncCallback<Entity>(EntityOperationType.ADD, requestId, request, response, this) {
			public void onSuccess(Entity result) {
				super.onSuccess(result);
				TreeNode record = (TreeNode) entityModule.buildRecord(result, true);
				TreeNode[] recordList = new TreeNode[]{record};
				response.setData(recordList);
				
				persistencePerspective.getOperationTypes().setAddType(OperationType.JOINSTRUCTURE);
				DataSourceModule joinModule = getCompatibleModule(OperationType.JOINSTRUCTURE);
				Entity entity = joinModule.buildEntity(record);
				//Add the join table entry for the new category as well
	        	service.add(joinModule.getCeilingEntityFullyQualifiedClassname(), entity, persistencePerspective, null, new EntityServiceAsyncCallback<Entity>(EntityOperationType.ADD, "temp" + requestId, request, response, CategoryTreeDataSource.this) {
					public void onSuccess(Entity result) {
						super.onSuccess(result);
						processResponse(requestId, response);
					}
				});
			}
		});
	}

}
