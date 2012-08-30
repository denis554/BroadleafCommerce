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

package org.broadleafcommerce.openadmin.client.datasource.dynamic.module;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtincubator.security.exception.ApplicationSecurityException;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.tree.TreeNode;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.operation.EntityOperationType;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.operation.EntityServiceAsyncCallback;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.CriteriaTransferObject;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.MapStructure;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.common.presentation.client.PersistencePerspectiveItemType;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.client.service.AbstractCallback;
import org.broadleafcommerce.openadmin.client.service.AppServices;
import org.broadleafcommerce.openadmin.client.service.DynamicEntityServiceAsync;
import org.broadleafcommerce.openadmin.client.setup.AsyncCallbackAdapter;

/**
 * 
 * @author jfischer
 *
 */
public class MapStructureClientModule extends BasicClientEntityModule {

	protected ListGrid associatedGrid;
	
    public MapStructureClientModule(String ceilingEntityFullyQualifiedClassname, String fetchTypeFullyQualifiedClassname, PersistencePerspective persistencePerspective, DynamicEntityServiceAsync service, ListGrid associatedGrid) {
        super(ceilingEntityFullyQualifiedClassname, fetchTypeFullyQualifiedClassname, persistencePerspective, service);
        this.associatedGrid = associatedGrid;
    }
    
	public MapStructureClientModule(String ceilingEntityFullyQualifiedClassname, PersistencePerspective persistencePerspective, DynamicEntityServiceAsync service, ListGrid associatedGrid) {
		super(ceilingEntityFullyQualifiedClassname, persistencePerspective, service);
		this.associatedGrid = associatedGrid;
	}

	@Override
	public void executeFetch(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
		CriteriaTransferObject criteriaTransferObject = getCto(request);
		final String parentCategoryId = criteriaTransferObject.get(criteriaTransferObject.getPropertyIdSet().iterator().next()).getFilterValues()[0];
		service.fetch(new PersistencePackage(ceilingEntityFullyQualifiedClassname, fetchTypeFullyQualifiedClassname, null, persistencePerspective, customCriteria, BLCMain.csrfToken), criteriaTransferObject, new EntityServiceAsyncCallback<DynamicResultSet>(EntityOperationType.FETCH, requestId, request, response, dataSource) {
			public void onSuccess(DynamicResultSet result) {
				super.onSuccess(result);
				TreeNode[] recordList = buildRecords(result, null);
				MapStructure mapStructure = (MapStructure) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.MAPSTRUCTURE);
				for (TreeNode node : recordList) {
					node.setAttribute("symbolicId", parentCategoryId);
					node.setAttribute("priorKey", node.getAttribute(mapStructure.getKeyPropertyName()));
				}
				response.setData(recordList);
				response.setTotalRows(result.getTotalRecords());
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
				dataSource.processResponse(requestId, response);
			}
		});
	}	
	
	@Override
	public void executeUpdate(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
		JavaScriptObject data = request.getData();
        final ListGridRecord temp = new ListGridRecord(data);
        Entity entity = buildEntity(temp, request);
        //final ListGridRecord record = associatedGrid.getSelectedRecord();
    	//Entity entity = buildEntity(record, request);
    	//for (Property property : tempEntity.getProperties()) {
    		//entity.findProperty(property.getName()).setValue(property.getValue());
    	//}
        //String componentId = request.getComponentId();
        //if (componentId != null) {
            //if (entity.getType() == null) {
            	//String[] type = ((ListGrid) Canvas.getById(componentId)).getSelectedRecord().getAttributeAsStringArray("_type");
            	//entity.setType(type);
            //}
        //}
		service.update(new PersistencePackage(ceilingEntityFullyQualifiedClassname, entity, persistencePerspective, customCriteria, BLCMain.csrfToken), new EntityServiceAsyncCallback<Entity>(EntityOperationType.UPDATE, requestId, request, response, dataSource) {
			public void onSuccess(Entity result) {
				super.onSuccess(result);
                if (processResult(result, requestId, response, dataSource)) {
                    ListGridRecord myRecord = (ListGridRecord) updateRecord(result, (Record) temp, false);
                    ListGridRecord[] recordList = new ListGridRecord[]{myRecord};
                    response.setData(recordList);
                    response.setTotalRows(1);
                    /*
                     * An update can result in the removal of a value, which would make the cache out-of-sync
                     * with the database. Refresh the cache to make sure the display values are accurate.
                     */
                    response.setInvalidateCache(true);
                    if (cb != null) {
                        cb.onSuccess(dataSource);
                    }
                    dataSource.processResponse(requestId, response);
                }
			}
			
			@Override
			protected void onSecurityException(ApplicationSecurityException exception) {
				super.onSecurityException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onOtherException(Throwable exception) {
				super.onOtherException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onError(EntityOperationType opType, String requestId, DSRequest request, DSResponse response, Throwable caught) {
				super.onError(opType, requestId, request, response, caught);
				if (cb != null) {
					cb.onFailure(caught);
				}
			}
		});
	}
	
	@Override
	public void executeRemove(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
		JavaScriptObject data = request.getData();
        final ListGridRecord temp = new ListGridRecord(data);
        Entity entity = buildEntity(temp, request);
//        final ListGridRecord record = associatedGrid.getSelectedRecord();
//    	Entity entity = buildEntity(record, request);
//    	for (Property property : tempEntity.getProperties()) {
//    		entity.findProperty(property.getName()).setValue(property.getValue());
//    	}
//        String componentId = request.getComponentId();
//        if (componentId != null) {
//            if (entity.getType() == null) {
//            	String[] type = ((ListGrid) Canvas.getById(componentId)).getSelectedRecord().getAttributeAsStringArray("_type");
//            	entity.setType(type);
//            }
//        }
        service.remove(new PersistencePackage(ceilingEntityFullyQualifiedClassname, entity, persistencePerspective, customCriteria, BLCMain.csrfToken), new EntityServiceAsyncCallback<Void>(EntityOperationType.REMOVE, requestId, request, response, dataSource) {
			public void onSuccess(Void item) {
				super.onSuccess(null);
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
				response.setInvalidateCache(true);
				dataSource.processResponse(requestId, response);
			}
			
			@Override
			protected void onSecurityException(ApplicationSecurityException exception) {
				super.onSecurityException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onOtherException(Throwable exception) {
				super.onOtherException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onError(EntityOperationType opType, String requestId, DSRequest request, DSResponse response, Throwable caught) {
				super.onError(opType, requestId, request, response, caught);
				if (cb != null) {
					cb.onFailure(caught);
				}
			}
		});
	}
	
	@Override
	public Record updateRecord(Entity entity, Record record, Boolean updateId) {
		for (Property property : entity.getProperties()){
			String attributeName = property.getName();
			if (
				property.getValue() != null && 
				dataSource.getField(attributeName).getType().equals(FieldType.DATETIME)
			) {
				record.setAttribute(attributeName, formatter.parse(property.getValue()));
			} else if (
				dataSource.getField(attributeName).getType().equals(FieldType.BOOLEAN)
			) {
				if (property.getValue() == null) {
					record.setAttribute(attributeName, false);
				} else {
					String lower = property.getValue().toLowerCase();
					if (lower.equals("y") || lower.equals("yes") || lower.equals("true") || lower.equals("1")) {
						record.setAttribute(attributeName, true);
					} else {
						record.setAttribute(attributeName, false);
					}
				}
			} else if (
				property.getMetadata() != null && ((BasicFieldMetadata) property.getMetadata()).getFieldType() != null &&
				((BasicFieldMetadata) property.getMetadata()).getFieldType().equals(SupportedFieldType.FOREIGN_KEY)
			) {
				record.setAttribute(attributeName, linkedValue);
			} else {
				String propertyValue;
				propertyValue = property.getValue();
				record.setAttribute(attributeName, propertyValue);
			}
			if (property.getDisplayValue() != null) {
				record.setAttribute("__display_"+attributeName, property.getDisplayValue());
			}
            if (property.getIsDirty()) {
                record.setAttribute("_hilite", "listGridDirtyPropertyHilite");
                record.setAttribute("__dirty_"+attributeName, true);
            }
		}
		String[] entityType = entity.getType();
		record.setAttribute("_type", entityType);
		return record;
	}
	
	@Override
	public void executeAdd(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
		BLCMain.NON_MODAL_PROGRESS.startProgress();
		JavaScriptObject data = request.getData();
        TreeNode record = new TreeNode(data);
        Entity entity = buildEntity(record, request);
        service.add(new PersistencePackage(ceilingEntityFullyQualifiedClassname, entity, persistencePerspective, customCriteria, BLCMain.csrfToken), new EntityServiceAsyncCallback<Entity>(EntityOperationType.ADD, requestId, request, response, dataSource) {
			public void onSuccess(Entity result) {
				super.onSuccess(result);
                if (processResult(result, requestId, response, dataSource)) {
                    TreeNode record = (TreeNode) buildRecord(result, false);
                    TreeNode[] recordList = new TreeNode[]{record};
                    response.setData(recordList);
                    /*
                     * If the key is a duplicate, it can result in the deletion of the old value
                     * and the creation of a new value, which can result in a new id for the retured
                     * value. Therefore, we need to invalidate the cache to make sure the displayed
                     * values are correct.
                     */
                    response.setInvalidateCache(true);
                    if (cb != null) {
                        cb.onSuccess(dataSource);
                    }
                    dataSource.processResponse(requestId, response);
                }
			}
			
			@Override
			protected void onSecurityException(ApplicationSecurityException exception) {
				super.onSecurityException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onOtherException(Throwable exception) {
				super.onOtherException(exception);
				if (cb != null) {
					cb.onFailure(exception);
				}
			}

			@Override
			protected void onError(EntityOperationType opType, String requestId, DSRequest request, DSResponse response, Throwable caught) {
				super.onError(opType, requestId, request, response, caught);
				if (cb != null) {
					cb.onFailure(caught);
				}
			}
		});
	}

	@Override
	public void buildFields(final String[] customCriteria, final Boolean overrideFieldSort, final AsyncCallback<DataSource> cb) {
        AppServices.DYNAMIC_ENTITY.inspect(new PersistencePackage(ceilingEntityFullyQualifiedClassname, null, persistencePerspective, customCriteria, BLCMain.csrfToken), new AbstractCallback<DynamicResultSet>() {

            @Override
            protected void onOtherException(Throwable exception) {
                super.onOtherException(exception);
                if (cb != null) {
                    cb.onFailure(exception);
                }
            }

            @Override
            protected void onSecurityException(ApplicationSecurityException exception) {
                super.onSecurityException(exception);
                if (cb != null) {
                    cb.onFailure(exception);
                }
            }

            public void onSuccess(DynamicResultSet result) {
                super.onSuccess(result);
                ClassMetadata metadata = result.getClassMetaData();
                filterProperties(metadata, new MergedPropertyType[]{MergedPropertyType.MAPSTRUCTUREKEY, MergedPropertyType.MAPSTRUCTUREVALUE}, overrideFieldSort, ((AsyncCallbackAdapter) cb).getDataSourceSetupManager());

                DataSourceField symbolicIdField = new DataSourceTextField("symbolicId");
                symbolicIdField.setCanEdit(false);
                symbolicIdField.setHidden(true);
                symbolicIdField.setAttribute("rawName", "symbolicId");
                dataSource.addField(symbolicIdField);

                DataSourceField priorKeyField = new DataSourceTextField("priorKey");
                priorKeyField.setCanEdit(false);
                priorKeyField.setHidden(true);
                priorKeyField.setAttribute("rawName", "priorKey");
                dataSource.addField(priorKeyField);

                //Add a hidden field to store the polymorphic type for this entity
                DataSourceField typeField = new DataSourceTextField("_type");
                typeField.setCanEdit(false);
                typeField.setHidden(true);
                typeField.setAttribute("rawName", "_type");
                dataSource.addField(typeField);
                dataSource.setPolymorphicEntityTree(metadata.getPolymorphicEntities());
                dataSource.setDefaultNewEntityFullyQualifiedClassname(dataSource.getPolymorphicEntities().keySet().iterator().next());

                if (cb != null) {
                    cb.onSuccess(dataSource);
                }
            }
        });
	}
	
	@Override
	public boolean isCompatible(OperationType operationType) {
    	return OperationType.MAP.equals(operationType);
    }
}
