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
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspectiveItemType;
import org.broadleafcommerce.openadmin.client.presenter.entity.AbstractSubPresentable;
import org.broadleafcommerce.openadmin.client.view.dynamic.grid.GridStructureDisplay;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class CreateBasedListStructurePresenter extends AbstractSubPresentable {

	protected String editDialogTitle;
	protected Map<String, Object> initialValues;
	
	public CreateBasedListStructurePresenter(GridStructureDisplay display, String[] availableToTypes, String editDialogTitle) {
		this(display, availableToTypes, editDialogTitle, new HashMap<String, Object>());
	}
	
	public CreateBasedListStructurePresenter(GridStructureDisplay display, String[] availableToTypes, String editDialogTitle, Map<String, Object> initialValues) {
		super(display, availableToTypes);
		this.editDialogTitle = editDialogTitle;
		this.initialValues = initialValues;
	}

    public void setDataSource(ListGridDataSource dataSource, String[] gridFields, Boolean[] editable) {
		display.getGrid().setDataSource(dataSource);
		dataSource.setAssociatedGrid(display.getGrid());
		dataSource.setupGridFields(gridFields, editable);
    }
	
	public void bind() {
		display.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			public void onDataArrived(DataArrivedEvent event) {
				display.getRemoveButton().disable();
			}
		});
		display.getGrid().addEditCompleteHandler(new EditCompleteHandler() {
			public void onEditComplete(EditCompleteEvent event) {
				display.getGrid().deselectAllRecords();
				setStartState();
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
					DynamicEntityDataSource ds = (DynamicEntityDataSource) display.getGrid().getDataSource();
					ForeignKey foreignKey = (ForeignKey) ds.getPersistencePerspective().getPersistencePerspectiveItems().get(PersistencePerspectiveItemType.FOREIGNKEY);
					initialValues.put(foreignKey.getManyToField(), abstractDynamicDataSource.getPrimaryKeyValue(associatedRecord));
					String[] type = new String[] {((DynamicEntityDataSource) display.getGrid().getDataSource()).getDefaultNewEntityFullyQualifiedClassname()};
					initialValues.put("_type", type);
					BLCMain.ENTITY_ADD.editNewRecord(editDialogTitle, ds, initialValues, null, null, null, null);
				}
			}
		});
	}
}
