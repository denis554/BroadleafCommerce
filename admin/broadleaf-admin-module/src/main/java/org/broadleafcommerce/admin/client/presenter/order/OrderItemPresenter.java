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

package org.broadleafcommerce.admin.client.presenter.order;

import java.util.Arrays;

import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import org.broadleafcommerce.admin.client.view.order.OrderItemDisplay;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.AbstractDynamicDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.PresentationLayerAssociatedDataSource;
import org.broadleafcommerce.openadmin.client.dto.ClassTree;
import org.broadleafcommerce.openadmin.client.presenter.entity.DynamicFormPresenter;
import org.broadleafcommerce.openadmin.client.presenter.entity.SubPresentable;

/**
 * 
 * @author jfischer
 *
 */
public class OrderItemPresenter extends DynamicFormPresenter implements SubPresentable {

	protected OrderItemDisplay display;
	
	protected Record associatedRecord;
	protected AbstractDynamicDataSource abstractDynamicDataSource;
	protected Boolean disabled = false;
    protected String[] availableToTypes;
	
	public OrderItemPresenter(OrderItemDisplay display, String[] availableToTypes) {
		super(display);
		this.display = display;
        this.availableToTypes = availableToTypes;
	}
	
	public void setDataSource(ListGridDataSource dataSource, String[] gridFields, Boolean[] editable) {
		display.getGrid().setDataSource(dataSource);
		dataSource.setAssociatedGrid(display.getGrid());
		dataSource.setupGridFields(gridFields, editable);
		display.getFormOnlyDisplay().buildFields(dataSource, true, false, false);
	}
	
	public void setExpansionDataSource(ListGridDataSource dataSource, String[] gridFields, Boolean[] editable) {
		display.getExpansionGrid().setDataSource(dataSource);
		dataSource.setAssociatedGrid(display.getExpansionGrid());
		dataSource.setupGridFields(gridFields, editable);
	}
	
	public void setStartState() {
		if (!disabled) {
			super.setStartState();
			display.getAddButton().enable();
			display.getGrid().enable();
			display.getRemoveButton().disable();
		}
	}
	
	public void enable() {
		disabled = false;
		super.enable();
		display.getAddButton().enable();
		display.getGrid().enable();
		display.getRemoveButton().enable();
		display.getToolbar().enable();
	}
	
	public void disable() {
		disabled = true;
		super.disable();
		display.getAddButton().disable();
		display.getGrid().disable();
		display.getRemoveButton().disable();
		display.getToolbar().disable();
	}
	
	public void setReadOnly(Boolean readOnly) {
		if (readOnly) {
			disable();
			display.getGrid().enable();
		} else {
			enable();
		}
	}

    @Override
    public boolean load(Record associatedRecord, AbstractDynamicDataSource associatedDataSource) {
        return load(associatedRecord, associatedDataSource, null);
    }

    @Override
    public boolean load(Record associatedRecord, AbstractDynamicDataSource abstractDynamicDataSource, final DSCallback cb) {
		this.associatedRecord = associatedRecord;
		this.abstractDynamicDataSource = abstractDynamicDataSource;
        ClassTree classTree = abstractDynamicDataSource.getPolymorphicEntityTree();
        String[] types = associatedRecord.getAttributeAsStringArray("_type");
        boolean shouldLoad = availableToTypes == null;
        if (types != null && types.length > 0) {
            if (availableToTypes != null) {
                if (Arrays.binarySearch(availableToTypes, types[0]) >= 0) {
                    shouldLoad = true;
                } else {
                    ClassTree myTypeResult = classTree.find(types[0]);
                    if (myTypeResult != null) {
                        for (String availableType : availableToTypes) {
                            ClassTree availableTypeResult = classTree.find(availableType);
                            if (availableTypeResult.getLeft() < myTypeResult.getLeft() && availableTypeResult.getRight() > myTypeResult.getRight()) {
                                shouldLoad = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        //((OrderItemView) display).setVisible(shouldLoad);

        if (shouldLoad) {
            String id = abstractDynamicDataSource.getPrimaryKeyValue(associatedRecord);
            ((PresentationLayerAssociatedDataSource) display.getGrid().getDataSource()).loadAssociatedGridBasedOnRelationship(id, new DSCallback() {
                public void execute(DSResponse response, Object rawData, DSRequest request) {
                    setStartState();
                    if (cb != null) {
                        cb.execute(response, rawData, request);
                    }
                }
            });
        }

        return shouldLoad;
	}
	
	public void bind() {
		super.bind();
		display.getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				if (event.getState()) {
					display.getRemoveButton().enable();
					((DynamicEntityDataSource) display.getGrid().getDataSource()).resetPermanentFieldVisibilityBasedOnType(event.getSelectedRecord().getAttributeAsStringArray("_type"));
					display.getFormOnlyDisplay().buildFields(display.getGrid().getDataSource(),false, false, false);
					display.getFormOnlyDisplay().getForm().editRecord(event.getRecord());
					display.getFormOnlyDisplay().getForm().enable();
				} else {
					display.getRemoveButton().disable();
				}
			}
		});
		display.getExpansionGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				if (event.getState()) {
					//display.getRemoveButton().enable();
					((DynamicEntityDataSource) display.getExpansionGrid().getDataSource()).resetPermanentFieldVisibilityBasedOnType(event.getSelectedRecord().getAttributeAsStringArray("_type"));
					display.getFormOnlyDisplay().buildFields(display.getExpansionGrid().getDataSource(),false, false, false);
					display.getFormOnlyDisplay().getForm().editRecord(event.getRecord());
					display.getFormOnlyDisplay().getForm().enable();
				} else {
					//display.getRemoveButton().disable();
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
	}
}
