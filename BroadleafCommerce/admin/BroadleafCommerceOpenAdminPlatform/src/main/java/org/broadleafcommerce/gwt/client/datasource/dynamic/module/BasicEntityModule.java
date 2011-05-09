package org.broadleafcommerce.gwt.client.datasource.dynamic.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadleafcommerce.gwt.client.BLCMain;
import org.broadleafcommerce.gwt.client.datasource.Validators;
import org.broadleafcommerce.gwt.client.datasource.dynamic.AbstractDynamicDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.EntityOperationType;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.EntityServiceAsyncCallback;
import org.broadleafcommerce.gwt.client.datasource.relations.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspectiveItemType;
import org.broadleafcommerce.gwt.client.datasource.relations.operations.OperationType;
import org.broadleafcommerce.gwt.client.datasource.results.ClassMetadata;
import org.broadleafcommerce.gwt.client.datasource.results.DynamicResultSet;
import org.broadleafcommerce.gwt.client.datasource.results.Entity;
import org.broadleafcommerce.gwt.client.datasource.results.FieldMetadata;
import org.broadleafcommerce.gwt.client.datasource.results.MergedPropertyType;
import org.broadleafcommerce.gwt.client.datasource.results.PolymorphicEntity;
import org.broadleafcommerce.gwt.client.datasource.results.Property;
import org.broadleafcommerce.gwt.client.presentation.SupportedFieldType;
import org.broadleafcommerce.gwt.client.service.AbstractCallback;
import org.broadleafcommerce.gwt.client.service.AppServices;
import org.broadleafcommerce.gwt.client.service.DynamicEntityServiceAsync;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.anasoft.os.daofusion.cto.client.FilterAndSortCriteria;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtincubator.security.exception.ApplicationSecurityException;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.data.fields.DataSourceBooleanField;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceEnumField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.JSON;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

public class BasicEntityModule implements DataSourceModule {

	protected final DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy.MM.dd HH:mm:ss");
	
	protected ForeignKey currentForeignKey;
	protected AbstractDynamicDataSource dataSource;
	protected String linkedValue;
	protected DynamicEntityServiceAsync service;
	protected final String ceilingEntityFullyQualifiedClassname;
	protected PersistencePerspective persistencePerspective;
	protected Long loadLevelCount = 0L;
	protected Map<String, FieldMetadata> metadataOverrides;
	
	public BasicEntityModule(String ceilingEntityFullyQualifiedClassname, PersistencePerspective persistencePerspective, DynamicEntityServiceAsync service) {
		this(ceilingEntityFullyQualifiedClassname, persistencePerspective, service, null);
	}
	
	public BasicEntityModule(String ceilingEntityFullyQualifiedClassname, PersistencePerspective persistencePerspective, DynamicEntityServiceAsync service, Map<String, FieldMetadata> metadataOverrides) {
		this.service = service;
		this.ceilingEntityFullyQualifiedClassname = ceilingEntityFullyQualifiedClassname;
		this.persistencePerspective = persistencePerspective;
		this.metadataOverrides = metadataOverrides;
	}
	
	/**
     * Transforms the given <tt>request</tt> into
     * {@link CriteriaTransferObject} instance.
     * <p>
     * We are doing this because we can apply seamless
     * CTO-to-criteria conversions back on the server.
     */
    @SuppressWarnings("unchecked")
    public CriteriaTransferObject getCto(DSRequest request) {
        CriteriaTransferObject cto = new CriteriaTransferObject();
        
        // paging
        if (request.getStartRow() != null) {
        	cto.setFirstResult(request.getStartRow());
        	if (request.getEndRow() != null) {
        		cto.setMaxResults(request.getEndRow() - request.getStartRow());
        	}
        }
        
        try {
			// sort
			SortSpecifier[] sortBy = request.getSortBy();
			if (sortBy != null && sortBy.length > 0) {
				String sortPropertyId = sortBy[0].getField();
			    boolean sortAscending = sortBy[0].getSortDirection().equals(SortDirection.ASCENDING);            
			    FilterAndSortCriteria sortCriteria = cto.get(sortPropertyId);
			    sortCriteria.setSortAscending(sortAscending);
			}
		} catch (Exception e) {
			//do nothing
			GWT.log("WARN: Unable to set sort criteria because of an exception.", e);
		}
        
        Criteria criteria = request.getCriteria();
        String jsObj = JSON.encode(criteria.getJsObj());
        // filter
        @SuppressWarnings("rawtypes")
		Map filterData = criteria.getValues();
        Set<String> filterFieldNames = filterData.keySet();
        for (String fieldName : filterFieldNames) {
        	if (!fieldName.equals("_constructor") && !fieldName.equals("operator")) {
        		if (!fieldName.equals("criteria")) {
        			FilterAndSortCriteria filterCriteria = cto.get(fieldName);
        			filterCriteria.setFilterValue(dataSource.stripDuplicateAllowSpecialCharacters((String) filterData.get(fieldName)));
        		} else {
        			JSONValue value = JSONParser.parse(jsObj);
        			JSONObject criteriaObj = value.isObject();
        			JSONArray criteriaArray = criteriaObj.get("criteria").isArray();
        			buildCriteria(criteriaArray, cto);
        		}
        	}
        }
        if (getCurrentForeignKey() != null) {
        	FilterAndSortCriteria filterCriteria = cto.get(getCurrentForeignKey().getManyToField());
			filterCriteria.setFilterValue(getCurrentForeignKey().getCurrentValue());
        }
        
        return cto;
    }
    
    public ForeignKey getCurrentForeignKey() {
		return currentForeignKey;
	}

	public void setCurrentForeignKey(ForeignKey currentForeignKey) {
		this.currentForeignKey = currentForeignKey;
	}
	
	public String getLinkedValue() {
		return linkedValue;
	}

	public void setLinkedValue(String linkedValue) {
		this.linkedValue = linkedValue;
	}
    
    protected void buildCriteria(JSONArray criteriaArray, CriteriaTransferObject cto) {
    	if (criteriaArray != null) {
			for (int i=0; i<=criteriaArray.size()-1; i++) {
				JSONObject itemObj = criteriaArray.get(i).isObject();
				if (itemObj != null) {
					JSONValue val = itemObj.get("fieldName");
					if (val == null) {
						JSONArray array = itemObj.get("criteria").isArray();
						buildCriteria(array, cto);
					} else {
						FilterAndSortCriteria filterCriteria = cto.get(val.isString().stringValue());
						String[] items = filterCriteria.getFilterValues();
						String[] newItems = new String[items.length + 1];
						int j = 0;
						for (String item : items) {
							newItems[j] = item;
							j++;
						}
						JSONValue value = itemObj.get("value");
						JSONString strVal = value.isString();
						if (strVal != null) {
							newItems[j] = strVal.stringValue();
						} else {
							newItems[j] = value.isObject().get("value").isString().stringValue();
							/*
							 * TODO need to add special parsing for relative dates. Convert this relative
							 * value to an actual date string.
							 */
						}
						
						filterCriteria.setFilterValues(newItems);
					}
				}
			}
		}
    }
    
    public boolean isCompatible(OperationType operationType) {
    	return OperationType.ENTITY.equals(operationType) || OperationType.FOREIGNKEY.equals(operationType);
    }
    
    public void executeFetch(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
    	BLCMain.NON_MODAL_PROGRESS.startProgress();
		CriteriaTransferObject cto = getCto(request);
		service.fetch(ceilingEntityFullyQualifiedClassname, cto, persistencePerspective, customCriteria, new EntityServiceAsyncCallback<DynamicResultSet>(EntityOperationType.FETCH, requestId, request, response, dataSource) {
			public void onSuccess(DynamicResultSet result) {
				super.onSuccess(result);
				TreeNode[] recordList = buildRecords(result, null);
				response.setData(recordList);
				response.setTotalRows(result.getTotalRecords());
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
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
    
    public void executeAdd(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
    	BLCMain.NON_MODAL_PROGRESS.startProgress();
		JavaScriptObject data = request.getData();
        TreeNode record = new TreeNode(data);
        Entity entity = buildEntity(record);
        service.add(ceilingEntityFullyQualifiedClassname, entity, persistencePerspective, customCriteria, new EntityServiceAsyncCallback<Entity>(EntityOperationType.ADD, requestId, request, response, dataSource) {
			public void onSuccess(Entity result) {
				super.onSuccess(result);
				TreeNode record = (TreeNode) buildRecord(result, false);
				TreeNode[] recordList = new TreeNode[]{record};
				response.setData(recordList);
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
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
    
    public void executeUpdate(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
    	BLCMain.NON_MODAL_PROGRESS.startProgress();
		JavaScriptObject data = request.getData();
        final TreeNode record = new TreeNode(data);
        Entity entity = buildEntity(record);
		String componentId = request.getComponentId();
        if (componentId != null) {
            if (entity.getType() == null) {
            	String[] type = ((ListGrid) Canvas.getById(componentId)).getSelectedRecord().getAttributeAsStringArray("_type");
            	entity.setType(type);
            }
        }
        service.update(entity, persistencePerspective, customCriteria, new EntityServiceAsyncCallback<Entity>(EntityOperationType.UPDATE, requestId, request, response, dataSource) {
			public void onSuccess(Entity result) {
				super.onSuccess(null);
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
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
    
    public void executeRemove(final String requestId, final DSRequest request, final DSResponse response, final String[] customCriteria, final AsyncCallback<DataSource> cb) {
    	BLCMain.NON_MODAL_PROGRESS.startProgress();
		JavaScriptObject data = request.getData();
        TreeNode record = new TreeNode(data);
        Entity entity = buildEntity(record);
		String componentId = request.getComponentId();
        if (componentId != null) {
            if (entity.getType() == null) {
            	String[] type = ((ListGrid) Canvas.getById(componentId)).getSelectedRecord().getAttributeAsStringArray("_type");
            	entity.setType(type);
            }
        }
        service.remove(entity, persistencePerspective, customCriteria, new EntityServiceAsyncCallback<Void>(EntityOperationType.REMOVE, requestId, request, response, dataSource) {
			public void onSuccess(Void item) {
				super.onSuccess(null);
				if (cb != null) {
					cb.onSuccess(dataSource);
				}
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
    
    public Record buildRecord(Entity entity, Boolean updateId) {
		TreeNode record = new TreeNode();
		return updateRecord(entity, record, updateId);
	}

	public Record updateRecord(Entity entity, Record record, Boolean updateId) {
		String id = entity.findProperty(dataSource.getPrimaryKeyFieldName()).getValue();
		if (updateId) {
			id = id + "_" + loadLevelCount;
			loadLevelCount++;
		}
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
				property.getMetadata() != null && property.getMetadata().getFieldType() != null &&
				property.getMetadata().getFieldType().equals(SupportedFieldType.FOREIGN_KEY)
			) {
				record.setAttribute(attributeName, linkedValue);
			} else {
				String propertyValue;
				if (property.getName().equals(dataSource.getPrimaryKeyFieldName())) {
					record.setAttribute(dataSource.getPrimaryKeyFieldName(), id);
				} else {
					propertyValue = property.getValue();
					record.setAttribute(attributeName, propertyValue);
				}
			}
			if (property.getDisplayValue() != null) {
				record.setAttribute("__display_"+attributeName, property.getDisplayValue());
				//dataSource.getFormItemCallbackHandlerManager().setDisplayValue(attributeName, id, property.getDisplayValue());
			}
		}
		String[] entityType = entity.getType();
		record.setAttribute("_type", entityType);
		return record;
	}
    
    public TreeNode[] buildRecords(DynamicResultSet result, String[] filterOutIds) {
		List<TreeNode> recordList = new ArrayList<TreeNode>();
		int decrement = 0;
		for (Entity entity : result.getRecords()){
			if (filterOutIds == null || (filterOutIds != null && Arrays.binarySearch(filterOutIds, entity.findProperty(dataSource.getPrimaryKeyFieldName()).getValue()) < 0)) {
				TreeNode record = (TreeNode) buildRecord(entity, false);
				recordList.add(record);
			} else {
				decrement++;
			}
		}
		result.setTotalRecords(result.getTotalRecords() - decrement);
		TreeNode[] response = new TreeNode[recordList.size()];
		response = recordList.toArray(response);
		return response;
	}
    
    public Entity buildEntity(Record record) {
		Entity entity = new Entity();
		entity.setType(record.getAttributeAsStringArray("_type"));
		List<Property> properties = new ArrayList<Property>();
		String[] attributes = record.getAttributes();
		for (String attribute : attributes) {
			if (!attribute.equals("_type") && !attribute.startsWith("__") && dataSource.getField(attribute) != null) {
				Property property = new Property();
				if (record.getAttribute(attribute) != null && dataSource.getField(attribute) != null && dataSource.getField(attribute).getType().equals(FieldType.DATETIME)) {
					property.setValue(formatter.format(record.getAttributeAsDate(attribute)));
				} else if (linkedValue != null && dataSource.getField(attribute).getAttribute("fieldType") != null && SupportedFieldType.valueOf(dataSource.getField(attribute).getAttribute("fieldType")).equals(SupportedFieldType.FOREIGN_KEY)) {
					property.setValue(dataSource.stripDuplicateAllowSpecialCharacters(linkedValue));
				} else {
					property.setValue(dataSource.stripDuplicateAllowSpecialCharacters(record.getAttribute(attribute)));
				}
				property.setName(dataSource.getField(attribute).getAttribute("rawName"));
				properties.add(property);
			}
		}
		Property[] props = new Property[properties.size()];
		props = properties.toArray(props);
		entity.setProperties(props);
		
		return entity;
	}
    
    public void buildFields(final String[] customCriteria, final Boolean overrideFieldSort, final AsyncCallback<DataSource> cb) {
    	String[] overrideKeys = null;
    	FieldMetadata[] overrideValues = null;
    	if (metadataOverrides != null) {
    		overrideKeys = new String[metadataOverrides.size()];
    		overrideValues = new FieldMetadata[metadataOverrides.size()];
    		int j = 0;
    		for (String key : metadataOverrides.keySet()){
    			overrideKeys[j] = key;
    			overrideValues[j] = metadataOverrides.get(key);
    		}
    	}
		AppServices.DYNAMIC_ENTITY.inspect(ceilingEntityFullyQualifiedClassname, persistencePerspective, customCriteria, overrideKeys, overrideValues, new AbstractCallback<DynamicResultSet>() {
			
			@Override
			protected void onOtherException(Throwable exception) {
				super.onOtherException(exception);
				cb.onFailure(exception);
			}

			@Override
			protected void onSecurityException(ApplicationSecurityException exception) {
				super.onSecurityException(exception);
				cb.onFailure(exception);
			}

			public void onSuccess(DynamicResultSet result) {
				super.onSuccess(result);
				ClassMetadata metadata = result.getClassMetaData();
				filterProperties(metadata, new MergedPropertyType[]{MergedPropertyType.PRIMARY, MergedPropertyType.JOINSTRUCTURE}, overrideFieldSort);
				
				//Add a hidden field to store the polymorphic type for this entity
				DataSourceField typeField = new DataSourceTextField("_type");
				typeField.setCanEdit(false);
				typeField.setHidden(true);
				typeField.setAttribute("permanentlyHidden", true);
				dataSource.addField(typeField);
				
				for (PolymorphicEntity polymorphicEntity : metadata.getPolymorphicEntities()){
					String name = polymorphicEntity.getName();
					String type = polymorphicEntity.getType();
					dataSource.getPolymorphicEntities().put(type, name);
				}
				dataSource.setDefaultNewEntityFullyQualifiedClassname(dataSource.getPolymorphicEntities().keySet().iterator().next());
				
				cb.onSuccess(dataSource);
			}
			
		});
	}
    
    protected OperatorId[] getBasicIdOperators() {
    	return new OperatorId[]{OperatorId.CONTAINS, OperatorId.EQUALS, OperatorId.GREATER_OR_EQUAL, OperatorId.GREATER_THAN, OperatorId.NOT_EQUAL, OperatorId.LESS_OR_EQUAL, OperatorId.LESS_THAN};
    }
    
    protected OperatorId[] getBasicBooleanOperators() {
    	return new OperatorId[]{OperatorId.EQUALS, OperatorId.NOT_EQUAL, OperatorId.NOT_NULL, OperatorId.EQUALS_FIELD, OperatorId.NOT_EQUAL_FIELD};
    }
    
    protected OperatorId[] getBasicDateOperators() {
    	return new OperatorId[]{OperatorId.EQUALS, OperatorId.GREATER_OR_EQUAL, OperatorId.GREATER_THAN, OperatorId.NOT_EQUAL, OperatorId.LESS_OR_EQUAL, OperatorId.LESS_THAN, OperatorId.NOT_NULL, OperatorId.EQUALS_FIELD, OperatorId.GREATER_OR_EQUAL_FIELD, OperatorId.GREATER_THAN_FIELD, OperatorId.LESS_OR_EQUAL_FIELD, OperatorId.LESS_THAN_FIELD, OperatorId.NOT_EQUAL_FIELD};
    }
    
    protected OperatorId[] getBasicNumericOperators() {
    	return new OperatorId[]{OperatorId.EQUALS, OperatorId.GREATER_OR_EQUAL, OperatorId.GREATER_THAN, OperatorId.NOT_EQUAL, OperatorId.LESS_OR_EQUAL, OperatorId.LESS_THAN, OperatorId.NOT_NULL, OperatorId.EQUALS_FIELD, OperatorId.GREATER_OR_EQUAL_FIELD, OperatorId.GREATER_THAN_FIELD, OperatorId.LESS_OR_EQUAL_FIELD, OperatorId.LESS_THAN_FIELD, OperatorId.NOT_EQUAL_FIELD, OperatorId.IN_SET, OperatorId.NOT_IN_SET};
    }
    
    protected OperatorId[] getBasicTextOperators() {
    	return new OperatorId[]{OperatorId.CONTAINS, OperatorId.NOT_CONTAINS, OperatorId.STARTS_WITH, OperatorId.ENDS_WITH, OperatorId.NOT_STARTS_WITH, OperatorId.NOT_ENDS_WITH, OperatorId.EQUALS, OperatorId.NOT_EQUAL, OperatorId.NOT_NULL, OperatorId.EQUALS_FIELD, OperatorId.NOT_EQUAL_FIELD, OperatorId.IN_SET, OperatorId.NOT_IN_SET};
    }
    
    protected OperatorId[] getBasicEnumerationOperators() {
    	return new OperatorId[]{OperatorId.EQUALS, OperatorId.NOT_EQUAL, OperatorId.NOT_NULL, OperatorId.EQUALS_FIELD, OperatorId.NOT_EQUAL_FIELD};
    }
	
	protected void filterProperties(ClassMetadata metadata, MergedPropertyType[] includeTypes, Boolean overrideFieldSort) throws IllegalStateException {
		//sort properties based on their display name
		Property[] properties = metadata.getProperties();
		if (overrideFieldSort) {
			Arrays.sort(properties, new Comparator<Property>() {
				public int compare(Property o1, Property o2) {
					if (o1.getMetadata().getPresentationAttributes().getFriendlyName() == null && o2.getMetadata().getPresentationAttributes().getFriendlyName() == null) {
						return 0;
					} else if (o1.getMetadata().getPresentationAttributes().getFriendlyName() == null) {
						return -1;
					} else if (o2.getMetadata().getPresentationAttributes().getFriendlyName() == null) {
						return 1;
					} else {
						return o1.getMetadata().getPresentationAttributes().getFriendlyName().compareTo(o2.getMetadata().getPresentationAttributes().getFriendlyName());
					}
				}
			});
		}
		for (Property property : metadata.getProperties()) {
			String mergedPropertyType = property.getMetadata().getMergedPropertyType().toString();
			if (Arrays.binarySearch(includeTypes, MergedPropertyType.valueOf(mergedPropertyType)) >= 0) {
				String rawName = property.getName();
				String propertyName = rawName;
				String fieldType = property.getMetadata().getFieldType()==null?null:property.getMetadata().getFieldType().toString();
				String secondaryFieldType = property.getMetadata().getSecondaryType()==null?null:property.getMetadata().getSecondaryType().toString();
				Long length = property.getMetadata().getLength()==null?null:property.getMetadata().getLength().longValue();
				Boolean required = property.getMetadata().getRequired();
				if (required == null) {
					required = false;
				}
				Boolean mutable = property.getMetadata().getMutable();
				String inheritedFromType = property.getMetadata().getInheritedFromType();
				String[] availableToTypes = property.getMetadata().getAvailableToTypes();
				String foreignKeyClass = property.getMetadata().getForeignKeyClass();
				String foreignKeyProperty = property.getMetadata().getForeignKeyProperty();
				String friendlyName = property.getMetadata().getPresentationAttributes().getFriendlyName();
				if (friendlyName == null) {
					friendlyName = property.getName();
				}
				Boolean hidden = property.getMetadata().getPresentationAttributes().isHidden();
				String group = property.getMetadata().getPresentationAttributes().getGroup();
				Integer groupOrder = property.getMetadata().getPresentationAttributes().getGroupOrder();
				Boolean largeEntry = property.getMetadata().getPresentationAttributes().isLargeEntry();
				Boolean prominent = property.getMetadata().getPresentationAttributes().isProminent();
				Integer order = property.getMetadata().getPresentationAttributes().getOrder();
				String columnWidth = property.getMetadata().getPresentationAttributes().getColumnWidth();
				String[][] enumerationValues = property.getMetadata().getEnumerationValues();
				String enumerationClass = property.getMetadata().getEnumerationClass();
				if (mutable) {
					Boolean isReadOnly = property.getMetadata().getPresentationAttributes().getReadOnly();
					if (isReadOnly != null) {
						mutable = !isReadOnly;
					}
				}
				DataSourceField field;
				switch(SupportedFieldType.valueOf(fieldType)){
				case ID:
					field = new DataSourceTextField(propertyName, friendlyName);
					if (propertyName.indexOf(".") < 0) {
						field.setPrimaryKey(true);
					}
					field.setCanEdit(false);
					hidden = true;
					field.setRequired(required);
					//field.setValidOperators(getBasicIdOperators());
					break;
				case BOOLEAN:
					field = new DataSourceBooleanField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					//field.setValidOperators(getBasicBooleanOperators());
					break;
				case DATE:
					field = new DataSourceDateTimeField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					field.setRequired(required);
					//field.setValidOperators(getBasicDateOperators());
					break;
				case INTEGER:
					field = new DataSourceIntegerField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					field.setRequired(required);
					//field.setValidOperators(getBasicNumericOperators());
					break;
				case DECIMAL:
					field = new DataSourceFloatField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					field.setRequired(required);
					//field.setValidOperators(getBasicNumericOperators());
					break;
				case EMAIL:
					field = new DataSourceTextField(propertyName, friendlyName);
			        field.setValidators(Validators.EMAIL);
			        field.setCanEdit(mutable);
			        field.setRequired(required);
			        //field.setValidOperators(getBasicTextOperators());
			        break;
				case MONEY:
					field = new DataSourceFloatField(propertyName, friendlyName);
			        field.setValidators(Validators.USCURRENCY);
			        field.setCanEdit(mutable);
			        field.setRequired(required);
			        //field.setValidOperators(getBasicNumericOperators());
			        break;
				case FOREIGN_KEY:{
					field = new DataSourceTextField(propertyName, friendlyName);
					String dataSourceName = null;
					ForeignKey foreignField = (ForeignKey) persistencePerspective.getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
					if (foreignField != null && foreignField.getForeignKeyClass().equals(foreignKeyClass)) {
						dataSourceName = foreignField.getDataSourceName();
					}
					if (dataSourceName == null) {
						field.setForeignKey(foreignKeyProperty);
					} else {
						field.setForeignKey(dataSourceName+"."+foreignKeyProperty);
					}
					if (hidden == null) {
						hidden = true;
					}
					field.setRequired(required);
					//field.setValidOperators(getBasicNumericOperators());
					break;}
				case ADDITIONAL_FOREIGN_KEY:{
					field = new DataSourceTextField(propertyName, friendlyName);
					if (hidden == null) {
						hidden = true;
					}
					field.setRequired(required);
					//field.setValidOperators(getBasicNumericOperators());
					break;}
				case BROADLEAF_ENUMERATION:
					field = new DataSourceEnumField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					field.setRequired(required);
					LinkedHashMap<String,String> valueMap = new LinkedHashMap<String,String>();
					for (int j=0; j<enumerationValues.length; j++) {
						valueMap.put(enumerationValues[j][0], enumerationValues[j][1]);
					}
	        		field.setValueMap(valueMap);
	        		//field.setValidOperators(getBasicEnumerationOperators());
					break;
				default:
					field = new DataSourceTextField(propertyName, friendlyName);
					field.setCanEdit(mutable);
					field.setRequired(required);
					//field.setValidOperators(getBasicTextOperators());
					break;
				}
				if (fieldType.equals(SupportedFieldType.ID.toString())) {
					field.setHidden(true);
					field.setAttribute("permanentlyHidden", false);
				} else if (hidden != null) {
					field.setHidden(hidden);
					field.setAttribute("permanentlyHidden", hidden);
				} else if (field.getAttribute("permanentlyHidden")==null){
					field.setHidden(false);
					field.setAttribute("permanentlyHidden", false);
				}
				if (group != null) {
					field.setAttribute("formGroup", group);
				}
				if (groupOrder != null) {
					field.setAttribute("formGroupOrder", groupOrder);
				}
				if (largeEntry != null) {
					field.setAttribute("largeEntry", largeEntry);
				}
				if (prominent != null) {
					field.setAttribute("prominent", prominent);
				}
				if (order != null) {
					field.setAttribute("presentationLayerOrder", order);
				}
				if (length != null) {
					field.setLength(length.intValue());
				}
				if (columnWidth != null) {
					field.setAttribute("columnWidth", columnWidth);
				}
				if (enumerationValues != null) {
					field.setAttribute("enumerationValues", enumerationValues);
				}
				if (enumerationClass != null) {
					field.setAttribute("enumerationClass", enumerationClass);
				}
				field.setAttribute("inheritedFromType", inheritedFromType);
				field.setAttribute("availableToTypes", availableToTypes);
				field.setAttribute("fieldType", fieldType);
				field.setAttribute("secondaryFieldType", secondaryFieldType);
				field.setAttribute("mergedPropertyType", mergedPropertyType);
				field.setAttribute("rawName", rawName);
				dataSource.addField(field);
			}
		}
	}

	public void setDataSource(AbstractDynamicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getCeilingEntityFullyQualifiedClassname() {
		return ceilingEntityFullyQualifiedClassname;
	}
	
}
