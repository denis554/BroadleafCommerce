/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.broadleafcommerce.openadmin.web.form.component;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.util.TypedPredicate;
import org.broadleafcommerce.openadmin.dto.SectionCrumb;
import org.broadleafcommerce.openadmin.web.form.entity.Field;
import org.broadleafcommerce.openadmin.web.rulebuilder.dto.DataWrapper;
import org.broadleafcommerce.openadmin.web.rulebuilder.dto.FieldWrapper;

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
    protected boolean isSortable;

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

    protected List<ListGridActionGroup> toolbarActionGroups = new ArrayList<ListGridActionGroup>();
    protected List<ListGridActionGroup> rowActionGroups = new ArrayList<ListGridActionGroup>();

    // These actions will start greyed out and unable to be clicked until a specific row has been selected
    protected List<ListGridAction> modalRowActions = new ArrayList<ListGridAction>();
    protected int totalRecords;
    protected int startIndex;
    protected int pageSize;
    protected Boolean canFilterAndSort;
    protected Boolean isReadOnly;
    protected Boolean hideIdColumn;
    
    protected AddMethodType addMethodType;
    protected String listGridType;
    protected String selectType;

    protected String selectizeUrl;

    // The section url that maps to this particular list grid
    protected String sectionKey;

    // The list of all section keys that have been traversed to arrive at this ListGrid (including the current one), in order
    // of occurrence
    protected List<SectionCrumb> sectionCrumbs = new ArrayList<SectionCrumb>();

    // If this list grid is a sublistgrid, meaning it is rendered as part of a different entity, these properties
    // help identify the parent entity.
    protected String externalEntitySectionKey;
    protected String containingEntityId;
    protected String subCollectionFieldName;
    protected String pathOverride;
    protected String searchFieldsTemplateOverride;
    protected String templateOverride;

    public enum Type {
        MAIN,
        TO_ONE,
        BASIC,
        ADORNED,
        ADORNED_WITH_FORM,
        MAP,
        TRANSLATION,
        ASSET,
        WORKFLOW,
        TREE,
        ASSET_GRID,
        ASSET_GRID_FOLDER
    }

    public enum SelectType {
        SINGLE_SELECT,
        MULTI_SELECT,
        SELECTIZE,
        NONE
    }

    /* Filter Builder required Fields */
    protected String fieldBuilder;
    protected DataWrapper dataWrapper;
    protected String json;
    protected String jsonFieldName;
    protected FieldWrapper fieldWrapper;

    /* ************** */
    /* CUSTOM METHODS */
    /* ************** */
    
    public String getPath() {
        if (StringUtils.isNotBlank(pathOverride)) {
            return pathOverride;
        }
                
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

    public String getSectionCrumbRepresentation() {
        StringBuilder sb = new StringBuilder();
        if (!sectionCrumbs.isEmpty()) {
           sb.append("?sectionCrumbs=");
        }
        int index = 0;
        for (SectionCrumb section : sectionCrumbs) {
            sb.append(section.getSectionIdentifier());
            sb.append("--");
            sb.append(section.getSectionId());
            if (index < sectionCrumbs.size()-1) {
                sb.append(",");
            }
            index++;
        }
        return sb.toString();
    }

    /**
     * Grabs a filtered list of toolbar actions filtered by whether or not they match the same readonly state as the listgrid
     * and are thus shown on the screen
     */
    @SuppressWarnings("unchecked")
    public List<ListGridAction> getActiveToolbarActions() {
        return (List<ListGridAction>) CollectionUtils.select(getToolbarActions(), new TypedPredicate<ListGridAction>() {
            
            @Override
            public boolean eval(ListGridAction action) {
                return action.getForListGridReadOnly().equals(getIsReadOnly());
            }
        });
    }
    
    /**
     * Grabs a filtered list of row actions filtered by whether or not they match the same readonly state as the listgrid
     * and are thus shown on the screen
     */
    @SuppressWarnings("unchecked")
    public List<ListGridAction> getActiveRowActions() {
        return (List<ListGridAction>) CollectionUtils.select(getRowActions(), new TypedPredicate<ListGridAction>() {
            
            @Override
            public boolean eval(ListGridAction action) {
                return action.getForListGridReadOnly().equals(getIsReadOnly());
            }
        });
    }

    /**
     * Grabs a filtered list of toolbar action groupss filtered by whether or not they match the same readonly state as the listgrid
     * and are thus shown on the screen
     */
    @SuppressWarnings("unchecked")
    public List<ListGridAction> getActiveToolbarActionGroups() {
        return (List<ListGridAction>) CollectionUtils.select(getToolbarActionGroups(), new TypedPredicate<ListGridActionGroup>() {

            @Override
            public boolean eval(ListGridActionGroup actionGroup) {
                boolean result = false;
                for (ListGridAction action : actionGroup.getListGridActions()) {
                    if (action.getForListGridReadOnly().equals(getIsReadOnly())) {
                        result = true;
                    }
                }
                return result;
            }
        });
    }

    /**
     * Grabs a filtered list of row action groupss filtered by whether or not they match the same readonly state as the listgrid
     * and are thus shown on the screen
     */
    @SuppressWarnings("unchecked")
    public List<ListGridAction> getActiveRowActionGroups() {
        return (List<ListGridAction>) CollectionUtils.select(getRowActionGroups(), new TypedPredicate<ListGridActionGroup>() {

            @Override
            public boolean eval(ListGridActionGroup actionGroup) {
                boolean result = false;
                for (ListGridAction action : actionGroup.getListGridActions()) {
                    if (action.getForListGridReadOnly().equals(getIsReadOnly())) {
                        result = true;
                    }
                }
                return result;
            }
        });
    }

    /**
     * Grabs a filtered list of row actions filtered by whether or not they match the same readonly state as the listgrid
     * and are thus shown on the screen
     */
    @SuppressWarnings("unchecked")
    public List<ListGridAction> getActiveModalRowActions() {
        return (List<ListGridAction>) CollectionUtils.select(getModalRowActions(), new TypedPredicate<ListGridAction>() {

            @Override
            public boolean eval(ListGridAction action) {
                return action.getForListGridReadOnly().equals(getIsReadOnly());
            }
        });
    }
    
    public void addRowAction(ListGridAction action) {
        getRowActions().add(action);
    }

    public void addModalRowAction(ListGridAction action) {
        getModalRowActions().add(action);
    }
    
    public void addToolbarAction(ListGridAction action) {
        getToolbarActions().add(action);
    }
    
    public void removeAllToolbarActions() {
        getToolbarActions().clear();
    }
    
    public void removeAllRowActions() {
        getRowActions().clear();
    }

    public void addToolbarActionGroup(ListGridActionGroup actionGroup) {
        getToolbarActionGroups().add(actionGroup);
    }

    public void removeAllToolbarActionGroups() {
        getToolbarActionGroups().clear();
    }

    public void addRowActionGroup(ListGridActionGroup actionGroup) {
        getRowActionGroups().add(actionGroup);
    }

    public void removeAllRowActionGroups() {
        getRowActionGroups().clear();
    }

    public void removeAllModalRowActions() {
        getModalRowActions().clear();
    }
    
    public ListGridAction findToolbarAction(String actionId) {
        for (ListGridAction action : getToolbarActions()) {
            if (action.getActionId().equals(actionId)) {
                return action;
            }
        }
        for (ListGridActionGroup actionGroup : getToolbarActionGroups()) {
            for (ListGridAction action : actionGroup.getListGridActions()) {
                if (action.getActionId().equals(actionId)) {
                    return action;
                }
            }
        }
        return null;
    }
    
    public ListGridAction findRowAction(String actionId) {
        for (ListGridAction action : getRowActions()) {
            if (action.getActionId().equals(actionId)) {
                return action;
            }
        }
        for (ListGridActionGroup actionGroup : getRowActionGroups()) {
            for (ListGridAction action : actionGroup.getListGridActions()) {
                if (action.getActionId().equals(actionId)) {
                    return action;
                }
            }
        }
        return null;
    }

    public ListGridAction findModalRowAction(String actionId) {
        for (ListGridAction action : getModalRowActions()) {
            if (action.getActionId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }
    
    /**
     * This grid is sortable if there is a reorder action defined in the toolbar. If records can be reordered, then the
     * sort functionality doesn't make any sense.
     * 
     * Also, map structures are currently unsortable.
     * 
     * @return
     */
    public boolean isSortable() {
        return this.isSortable || Type.MAP.toString().toLowerCase().equals(getListGridType());
    }

    /* ************************ */
    /* CUSTOM GETTERS / SETTERS */
    /* ************************ */
    
    public void setListGridType(Type listGridType) {
        this.listGridType = listGridType.toString().toLowerCase();
    }
    
    /**
     * Allows for completely custom types other than the ones defined {@link Type} to assign unique handlers to on the JS
     * side
     * @param listGridType
     */
    public void setListGridTypeString(String listGridType) {
        this.listGridType = listGridType;
    }

    public void setSelectType(SelectType selectType) {
        this.selectType = selectType.toString().toLowerCase();
    }

    public void setSelectTypeString(String selectType) {
        this.selectType = selectType;
    }
    
    public Boolean getCanFilterAndSort() {
        return (canFilterAndSort == null ? true : canFilterAndSort);
    }

    public Boolean getIsReadOnly() {
        return isReadOnly == null ? false : isReadOnly;
    }
    
    public Boolean getClickable() {
        return !"none".equals(selectType);
    }
    
    public Boolean getHideIdColumn() {
        return hideIdColumn == null ? false : hideIdColumn;
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

    public boolean getIsSortable() {
        return isSortable;
    }

    public void setIsSortable(boolean isSortable) {
        this.isSortable = isSortable;
    }

    public Set<Field> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(Set<Field> headerFields) {
        this.headerFields = headerFields;
    }

    public Field findHeaderField(String name) {
        if (name == null) {
            return null;
        }

        for (Field headerField : getHeaderFields()) {
            if (name.equals(headerField.getName())) {
                return headerField;
            }
        }
        return null;
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

    public List<ListGridActionGroup> getToolbarActionGroups() {
        return toolbarActionGroups;
    }

    public void setToolbarActionGroups(List<ListGridActionGroup> toolbarActionGroups) {
        this.toolbarActionGroups = toolbarActionGroups;
    }

    public List<ListGridActionGroup> getRowActionGroups() {
        return rowActionGroups;
    }

    public void setRowActionGroups(List<ListGridActionGroup> rowActionGroups) {
        this.rowActionGroups = rowActionGroups;
    }

    public List<ListGridAction> getModalRowActions() {
        return modalRowActions;
    }

    public void setModalRowActions(List<ListGridAction> modalRowActions) {
        this.modalRowActions = modalRowActions;
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
    
    public void setCanFilterAndSort(Boolean canFilterAndSort) {
        this.canFilterAndSort = canFilterAndSort;
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

    public String getSelectType() {
        return selectType;
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

    public String getSelectizeUrl() {
        return selectizeUrl;
    }

    public void setSelectizeUrl(String selectizeUrl) {
        this.selectizeUrl = selectizeUrl;
    }
    
    public String getExternalEntitySectionKey() {
        return externalEntitySectionKey;
    }

    public void setExternalEntitySectionKey(String externalEntitySectionKey) {
        this.externalEntitySectionKey = externalEntitySectionKey;
    }

    public String getPathOverride() {
        return pathOverride;
    }

    public void setPathOverride(String pathOverride) {
        this.pathOverride = pathOverride;
    }

    public String getSearchFieldsTemplateOverride() {
        return searchFieldsTemplateOverride;
    }

    public void setSearchFieldsTemplateOverride(String searchFieldsTemplateOverride) {
        this.searchFieldsTemplateOverride = searchFieldsTemplateOverride;
    }

    public String getTemplateOverride() {
        return templateOverride;
    }

    public void setTemplateOverride(String templateOverride) {
        this.templateOverride = templateOverride;
    }

    public void setIsReadOnly(Boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public void setHideIdColumn(Boolean hideIdColumn) {
        this.hideIdColumn = hideIdColumn;
    }

    public List<SectionCrumb> getSectionCrumbs() {
        return sectionCrumbs;
    }

    public void setSectionCrumbs(List<SectionCrumb> sectionCrumbs) {
        if (sectionCrumbs == null) {
            this.sectionCrumbs.clear();
            return;
        }
        this.sectionCrumbs = sectionCrumbs;
    }

    public String getFieldBuilder() {
        return fieldBuilder;
    }

    public void setFieldBuilder(String fieldBuilder) {
        this.fieldBuilder = fieldBuilder;
    }

    public FieldWrapper getFieldWrapper() {
        return fieldWrapper;
    }

    public void setFieldWrapper(FieldWrapper fieldWrapper) {
        this.fieldWrapper = fieldWrapper;
    }

    public DataWrapper getDataWrapper() {
        return dataWrapper;
    }

    public void setDataWrapper(DataWrapper dataWrapper) {
        this.dataWrapper = dataWrapper;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getJsonFieldName() {
        return jsonFieldName;
    }

    public void setJsonFieldName(String jsonFieldName) {
        this.jsonFieldName = jsonFieldName;
    }
}
