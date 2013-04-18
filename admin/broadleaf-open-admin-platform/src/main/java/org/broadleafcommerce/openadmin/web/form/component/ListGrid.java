
/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.web.form.component;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.openadmin.web.form.entity.Field;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ListGrid {

    protected String className;
    protected String friendlyName = null;
    protected String idProperty;
    protected int order;
    
    protected Set<Field> headerFields = new TreeSet<Field>(new Comparator<Field>() {

        @Override
        public int compare(Field o1, Field o2) {
            return new CompareToBuilder()
                    .append(o1.getOrder(), o2.getOrder())
                    .append(o1.getFriendlyName(), o2.getFriendlyName())
                    .append(o1.getName(), o2.getName())
                    .toComparison();
        }
    });
    protected List<ListGridRecord> records = new ArrayList<ListGridRecord>();
    protected List<ListGridAction> toolbarActions = new ArrayList<ListGridAction>();
    
    
    // These actions will start greyed out and unable to be clicked until a specific row has been selected
    protected List<ListGridAction> rowActions = new ArrayList<ListGridAction>();
    protected int totalRecords;
    protected int startIndex;
    protected int pageSize;
    
    protected AddMethodType addMethodType;
    protected String listGridType;
    
    // The section url that maps to this particular list grid
    protected String sectionKey;

    // If this list grid is a sublistgrid, meaning it is rendered as part of a different entity, these properties
    // help identify the parent entity.
    protected String externalEntitySectionKey;
    protected String containingEntityId;
    protected String subCollectionFieldName;

    public enum Type {
        MAIN,
        INLINE,
        TO_ONE,
        BASIC,
        ADORNED,
        ADORNED_WITH_FORM,
        MAP
    }
    
    /* ************** */
    /* CUSTOM METHODS */
    /* ************** */
    
    public String getPath() {
        StringBuilder sb = new StringBuilder();
        
        if (!getSectionKey().startsWith("/")) {
            sb.append("/");
        }
        
        sb.append(getSectionKey());
        if (getContainingEntityId() != null) {
            sb.append("/").append(getContainingEntityId());
        }
        
        if (StringUtils.isNotBlank(getSubCollectionFieldName())) {
            sb.append("/").append(getSubCollectionFieldName());
        }
        
        //to-one grids need a slightly different grid URL; these need to be appended with 'select'
        //TODO: surely there's a better way to do this besides just hardcoding the 'select'?
        if (Type.TO_ONE.toString().toLowerCase().equals(listGridType)) {
            sb.append("/select");
        }
        
        return sb.toString();
    }
    
    public int getTotalPages() {
        return new Double(Math.ceil(1.0 * totalRecords / pageSize)).intValue();
    }
    
    public int getCurrentPage() {
        return startIndex / pageSize;
    }
    
    public int getCurrentStartIndex() {
        return getCurrentPage() * pageSize;
    }
    
    public boolean getHasPrevPage() {
        return getCurrentPage() > 0;
    }
    
    public int getPrevPage() {
        return (getCurrentPage() - 1) * pageSize;
    }
    
    public boolean getHasNextPage() {
        return getCurrentPage() + 1 < getTotalPages();
    }
    
    public int getNextPage() {
        return (getCurrentPage() + 1) * pageSize;
    }
    
    public void addRowAction(ListGridAction action) {
        getRowActions().add(action);
    }
    
    public void addToolbarAction(ListGridAction action) {
        getToolbarActions().add(action);
    }
    
    /**
     * This grid is sortable if there is a reorder action defined in the toolbar. If records can be reordered, then the
     * sort functionality doesn't make any sense.
     * 
     * @return
     */
    public boolean isSortable() {
        return getToolbarActions().contains(DefaultListGridActions.REORDER);
    }

    /* ************************ */
    /* CUSTOM GETTERS / SETTERS */
    /* ************************ */
    
    public void setListGridType(Type listGridType) {
        this.listGridType = listGridType.toString().toLowerCase();
    }

    /* ************************** */
    /* STANDARD GETTERS / SETTERS */
    /* ************************** */        

	public String getIdProperty() {
        return idProperty;
    }

    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = order;
    }

    public Set<Field> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(Set<Field> headerFields) {
        this.headerFields = headerFields;
    }

    public List<ListGridRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ListGridRecord> records) {
        this.records = records;
    }
    
    public List<ListGridAction> getToolbarActions() {
        return toolbarActions;
    }
    
    public void setToolbarActions(List<ListGridAction> toolbarActions) {
        this.toolbarActions = toolbarActions;
    }
    
    public List<ListGridAction> getRowActions() {
        return rowActions;
    }
    
    public void setRowActions(List<ListGridAction> rowActions) {
        this.rowActions = rowActions;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public AddMethodType getAddMethodType() {
        return addMethodType;
    }

    public void setAddMethodType(AddMethodType addMethodType) {
        this.addMethodType = addMethodType;
    }

    public String getListGridType() {
        return listGridType;
    }

    public String getContainingEntityId() {
        return containingEntityId;
    }

    public void setContainingEntityId(String containingEntityId) {
        this.containingEntityId = containingEntityId;
    }

    public String getSubCollectionFieldName() {
        return subCollectionFieldName;
    }

    public void setSubCollectionFieldName(String subCollectionFieldName) {
        this.subCollectionFieldName = subCollectionFieldName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getSectionKey() {
        return sectionKey;
    }
    
    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }
    
    public String getExternalEntitySectionKey() {
        return externalEntitySectionKey;
    }

    public void setExternalEntitySectionKey(String externalEntitySectionKey) {
        this.externalEntitySectionKey = externalEntitySectionKey;
    }
    
}
