package org.broadleafcommerce.gwt.client.datasource.dynamic;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.gwt.client.datasource.ForeignKey;
import org.broadleafcommerce.gwt.client.datasource.JoinTable;
import org.broadleafcommerce.gwt.client.datasource.results.RemoveType;
import org.broadleafcommerce.gwt.client.service.DynamicEntityServiceAsync;

import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class ListGridDataSource extends DynamicEntityDataSource {

	protected ListGrid associatedGrid;
	
	/**
	 * @param ceilingEntityFullyQualifiedClassname
	 * @param name
	 * @param service
	 * @param foreignFields
	 * @param removeType
	 * @param additionalNonPersistentProperties
	 */
	public ListGridDataSource(String ceilingEntityFullyQualifiedClassname, ForeignKey[] foreignFields, String name, DynamicEntityServiceAsync service, RemoveType removeType, String[] additionalNonPersistentProperties) {
		super(ceilingEntityFullyQualifiedClassname, foreignFields, name, service, removeType, additionalNonPersistentProperties);
	}

	/**
	 * @param ceilingEntityFullyQualifiedClassname
	 * @param joinTable
	 * @param name
	 * @param service
	 * @param removeType
	 * @param additionalNonPersistentProperties
	 */
	public ListGridDataSource(String ceilingEntityFullyQualifiedClassname, JoinTable joinTable, String name, DynamicEntityServiceAsync service, RemoveType removeType, String[] additionalNonPersistentProperties) {
		super(ceilingEntityFullyQualifiedClassname, joinTable, name, service, removeType, additionalNonPersistentProperties);
	}

	public ListGrid getAssociatedGrid() {
		return associatedGrid;
	}

	public void setAssociatedGrid(ListGrid associatedGrid) {
		this.associatedGrid = associatedGrid;
	}
	
	public void setupFields() {
		DataSourceField[] fields = getFields();
		ListGridField[] gridFields = new ListGridField[fields.length];
        int j = 0;
        List<DataSourceField> prominentFields = new ArrayList<DataSourceField>();
        for (DataSourceField field : fields) {
        	if (field.getAttributeAsBoolean("prominent")) {
        		prominentFields.add(field);
        	}
        }
        int availableSlots = 4;
        for (DataSourceField field : prominentFields) {
        	gridFields[j] = new ListGridField(field.getName(), field.getTitle(), j==0?200:150);
        	if (j == 0) {
        		gridFields[j].setFrozen(true);
        	}
        	gridFields[j].setHidden(false);
        	j++;
        	availableSlots--;
        }
        if (availableSlots > 0) {
	        for (DataSourceField field : fields) {
	        	if (!prominentFields.contains(field)) {
	        		gridFields[j] = new ListGridField(field.getName(), field.getTitle(), j==0?200:150);
	        		if (field.getAttributeAsBoolean("permanentlyHidden") || availableSlots <= 0) {
		        		gridFields[j].setHidden(true);
		        		gridFields[j].setCanHide(false);
		        	} else {
		        		if (j == 0) {
			        		gridFields[j].setFrozen(true);
			        	}
		        		availableSlots--;
		        	}
	        		j++;
	        	}
	        }
        }
        getAssociatedGrid().setFields(gridFields);
	}
}
