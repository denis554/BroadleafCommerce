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
package org.broadleafcommerce.openadmin.client.presenter.structure;

import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.presenter.entity.AbstractSubPresentable;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.MapStructureEntityEditDialog;
import org.broadleafcommerce.openadmin.client.view.dynamic.grid.GridStructureDisplay;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class MapStructurePresenter extends AbstractSubPresentable {

	protected MapStructureEntityEditDialog entityEditDialog;
	protected String entityEditDialogTitle;
	protected Map<String, Object> initialValues = new HashMap<String, Object>(10);
	protected String[] gridFields;

    public MapStructurePresenter(GridStructureDisplay display, MapStructureEntityEditDialog entityEditDialog, String[] availableToTypes, String entityEditDialogTitle, Map<String, Object> initialValues) {
		super(display, availableToTypes);
		this.entityEditDialog = entityEditDialog;
		this.entityEditDialogTitle = entityEditDialogTitle;
        if (initialValues != null) {
		    this.initialValues.putAll(initialValues);
        }
	}

    public MapStructurePresenter(GridStructureDisplay display, MapStructureEntityEditDialog entityEditDialog, String entityEditDialogTitle, Map<String, Object> initialValues) {
		this(display, entityEditDialog, null, entityEditDialogTitle, initialValues);
	}

    public MapStructurePresenter(GridStructureDisplay display, MapStructureEntityEditDialog entityEditDialog, String[] availableToTypes, String entityEditDialogTitle) {
		this(display, entityEditDialog, availableToTypes, entityEditDialogTitle, null);
	}

    public MapStructurePresenter(GridStructureDisplay display, MapStructureEntityEditDialog entityEditDialog, String entityEditDialogTitle) {
		this(display, entityEditDialog, null, entityEditDialogTitle, null);
	}

    public void setDataSource(ListGridDataSource dataSource, String[] gridFields, Boolean[] editable) {
		display.getGrid().setDataSource(dataSource);
		dataSource.setAssociatedGrid(display.getGrid());
		dataSource.setupGridFields(gridFields, editable);
		this.gridFields = gridFields;
	}
	
	public void bind() {
        if (display.getCanEdit()) {
            display.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
                public void onDataArrived(DataArrivedEvent event) {
                    display.getRemoveButton().disable();
                }
            });
            display.getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
                public void onSelectionChanged(SelectionEvent event) {
                    if (event.getState()) {
                        display.getRemoveButton().enable();
                    } else {
                        display.getRemoveButton().disable();
                    }
                }
            });
            display.getRemoveButton().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    if (event.isLeftButtonDown()) {
                        display.getGrid().removeData(display.getGrid().getSelectedRecord(), new DSCallback() {
                            public void execute(DSResponse response, Object rawData, DSRequest request) {
                                display.getRemoveButton().disable();
                            }
                        });
                    }
                }
            });
            display.getAddButton().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    if (event.isLeftButtonDown()) {
                        DynamicEntityDataSource dataSource = (DynamicEntityDataSource) display.getGrid().getDataSource();
                        initialValues.put("symbolicId", dataSource.getCompatibleModule(dataSource.getPersistencePerspective().getOperationTypes().getAddType()).getLinkedValue());
                        String[] type = associatedRecord.getAttributeAsStringArray("_type");
                        if (type == null) {
                            type = new String[] {((DynamicEntityDataSource) display.getGrid().getDataSource()).getDefaultNewEntityFullyQualifiedClassname()};
                        }
                        initialValues.put("_type", type);
                        entityEditDialog.editNewRecord(entityEditDialogTitle, (DynamicEntityDataSource) display.getGrid().getDataSource(), initialValues, null, null, gridFields, null);
                    }
                }
            });
            display.getGrid().addCellDoubleClickHandler(new CellDoubleClickHandler() {
                @Override
                public void onCellDoubleClick(CellDoubleClickEvent cellDoubleClickEvent) {
                    entityEditDialog.editRecord(entityEditDialogTitle, (DynamicEntityDataSource) display.getGrid().getDataSource(), display.getGrid().getSelectedRecord(), null, null, gridFields, null);
                }
            });
        }
	}

}
