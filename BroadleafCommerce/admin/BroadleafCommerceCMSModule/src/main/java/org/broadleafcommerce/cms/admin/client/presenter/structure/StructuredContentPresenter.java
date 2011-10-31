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

package org.broadleafcommerce.cms.admin.client.presenter.structure;

import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.FetchDataEvent;
import com.smartgwt.client.widgets.events.FetchDataHandler;
import com.smartgwt.client.widgets.form.events.FilterChangedEvent;
import com.smartgwt.client.widgets.form.events.FilterChangedHandler;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;
import com.smartgwt.client.widgets.form.fields.FormItem;
import org.broadleafcommerce.cms.admin.client.datasource.structure.CustomerListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.OrderItemListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.ProductListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.RequestDTOListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentItemCriteriaListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentTypeFormListDataSource;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentTypeFormListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentTypeSearchListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.TimeDTOListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.presenter.HtmlEditingPresenter;
import org.broadleafcommerce.cms.admin.client.view.structure.StructuredContentDisplay;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.dto.OperationType;
import org.broadleafcommerce.openadmin.client.dto.OperationTypes;
import org.broadleafcommerce.openadmin.client.presenter.entity.FormItemCallback;
import org.broadleafcommerce.openadmin.client.reflection.Instantiable;
import org.broadleafcommerce.openadmin.client.setup.AsyncCallbackAdapter;
import org.broadleafcommerce.openadmin.client.setup.NullAsyncCallbackAdapter;
import org.broadleafcommerce.openadmin.client.setup.PresenterSetupItem;
import org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.EntitySearchDialog;
import org.broadleafcommerce.openadmin.client.view.dynamic.form.FormOnlyView;
import org.broadleafcommerce.openadmin.client.view.dynamic.form.RichTextCanvasItem;

/**
 * 
 * @author jfischer
 *
 */
public class StructuredContentPresenter extends HtmlEditingPresenter implements Instantiable {

    protected HandlerRegistration saveButtonHandlerRegistration;
    protected HandlerRegistration refreshButtonHandlerRegistration;
    protected HandlerRegistration ruleSaveButtonHandlerRegistration;
    protected HandlerRegistration ruleRefreshButtonHandlerRegistration;
    protected Record currentStructuredContentRecord;
    protected String currentStructuredContentId;
    protected Integer currentStructuredContentPos;
    protected boolean isFetched = false;
    protected StructuredContentPresenterInitializer initializer;
    protected StructuredContentPresenterExtractor extractor;

	@Override
	protected void removeClicked() {
		display.getListDisplay().getGrid().removeSelectedData(new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                destroyContentTypeForm();
                formPresenter.disable();
		        display.getListDisplay().getRemoveButton().disable();
            }
        }, null);
	}

    protected void destroyContentTypeForm() {
        Canvas legacyForm = ((FormOnlyView) getDisplay().getDynamicFormDisplay().getFormOnlyDisplay()).getMember("contentTypeForm");
        if (legacyForm != null) {
            legacyForm.destroy();
        }
    }

    protected void enableRules() {
        getDisplay().getAddItemButton().setDisabled(false);
        getDisplay().getCustomerFilterBuilder().setDisabled(false);
        getDisplay().getCustomerLabel().setBaseStyle("normalLabel");
        getDisplay().getOrderItemLabel().setBaseStyle("normalLabel");
        getDisplay().getProductFilterBuilder().setDisabled(false);
        getDisplay().getProductLabel().setBaseStyle("normalLabel");
        getDisplay().getRequestFilterBuilder().setDisabled(false);
        getDisplay().getRequestLabel().setBaseStyle("normalLabel");
        getDisplay().getTimeFilterBuilder().setDisabled(false);
        getDisplay().getTimeLabel().setBaseStyle("normalLabel");
    }

    protected void disableRules() {
        getDisplay().getAddItemButton().setDisabled(true);
        getDisplay().getCustomerFilterBuilder().setDisabled(true);
        getDisplay().getCustomerLabel().setBaseStyle("disabledLabel");
        getDisplay().getOrderItemLabel().setBaseStyle("disabledLabel");
        getDisplay().getProductFilterBuilder().setDisabled(true);
        getDisplay().getProductLabel().setBaseStyle("disabledLabel");
        getDisplay().getRequestFilterBuilder().setDisabled(true);
        getDisplay().getRequestLabel().setBaseStyle("disabledLabel");
        getDisplay().getTimeFilterBuilder().setDisabled(true);
        getDisplay().getTimeLabel().setBaseStyle("disabledLabel");
    }

    @Override
    protected void addClicked() {
        initialValues.put("priority", 5);
        super.addClicked();
	}

    @Override
	protected void changeSelection(final Record selectedRecord) {
        if (!selectedRecord.getAttributeAsBoolean("lockedFlag")) {
            getDisplay().getListDisplay().getRemoveButton().enable();
            getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().enable();
            enableRules();
            initializer.initSection(selectedRecord, false);
        } else {
            getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().disable();
            getDisplay().getListDisplay().getRemoveButton().disable();
            disableRules();
            initializer.initSection(selectedRecord, true);
        }
        currentStructuredContentRecord = selectedRecord;
        currentStructuredContentId = getPresenterSequenceSetupManager().getDataSource("structuredContentDS").getPrimaryKeyValue(currentStructuredContentRecord);
        currentStructuredContentPos = getDisplay().getListDisplay().getGrid().getRecordIndex(currentStructuredContentRecord);
        loadContentTypeForm(selectedRecord);
	}

    protected void loadContentTypeForm(final Record selectedRecord) {
        //load the page template form
        BLCMain.NON_MODAL_PROGRESS.startProgress();
        StructuredContentTypeFormListDataSourceFactory.createDataSource("contentTypeFormDS", new String[]{"constructForm", selectedRecord.getAttribute("structuredContentType")}, new AsyncCallbackAdapter() {
            @Override
            public void onSetupSuccess(final DataSource dataSource) {
                destroyContentTypeForm();
                final FormOnlyView formOnlyView = new FormOnlyView(dataSource, true, true, false);
                formOnlyView.getForm().addItemChangedHandler(new ItemChangedHandler() {
                    public void onItemChanged(ItemChangedEvent event) {
                        getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                        getDisplay().getStructuredContentSaveButton().enable();
                    }
                });
                formOnlyView.setID("contentTypeForm");
                formOnlyView.setOverflow(Overflow.VISIBLE);
                ((FormOnlyView) getDisplay().getDynamicFormDisplay().getFormOnlyDisplay()).addMember(formOnlyView);
                ((StructuredContentTypeFormListDataSource) dataSource).setCustomCriteria(new String[]{"constructForm", selectedRecord.getAttribute("id")});
                BLCMain.NON_MODAL_PROGRESS.startProgress();
                formOnlyView.getForm().fetchData(new Criteria(), new DSCallback() {
                    @Override
                    public void execute(DSResponse response, Object rawData, DSRequest request) {
                        if (!selectedRecord.getAttributeAsBoolean("lockedFlag")) {
                            formOnlyView.getForm().enable();
                        }
                        for (FormItem formItem : formOnlyView.getForm().getFields()) {
                            if (formItem instanceof RichTextCanvasItem) {
                                formItem.setValue(formOnlyView.getForm().getValue(formItem.getFieldName()));
                            }

                        }
                    }
                });
            }
        });
    }

    @Override
	public void bind() {
		super.bind();
        formPresenter.getSaveButtonHandlerRegistration().removeHandler();
        formPresenter.getRefreshButtonHandlerRegistration().removeHandler();
        refreshButtonHandlerRegistration = getDisplay().getDynamicFormDisplay().getRefreshButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
                    refresh();
				}
			}
        });
        ruleRefreshButtonHandlerRegistration = getDisplay().getDynamicFormDisplay().getRefreshButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
                    refresh();
				}
			}
        });
        saveButtonHandlerRegistration = getDisplay().getDynamicFormDisplay().getSaveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                //save the regular entity form and the page template form
                if (event.isLeftButtonDown()) {
                    save();
                }
            }
        });
        ruleSaveButtonHandlerRegistration = getDisplay().getStructuredContentSaveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                //save the regular entity form and the page template form
                if (event.isLeftButtonDown()) {
                    save();
                }
            }
        });
        display.getListDisplay().getGrid().addFetchDataHandler(new FetchDataHandler() {
            @Override
            public void onFilterData(FetchDataEvent event) {
                destroyContentTypeForm();
            }
        });
        getDisplay().getListDisplay().getGrid().addFetchDataHandler(new FetchDataHandler() {
            @Override
            public void onFilterData(FetchDataEvent event) {
                isFetched = true;
            }
        });
        getDisplay().getAddItemButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (event.isLeftButtonDown()) {
                    final ItemBuilderDisplay display = getDisplay().addItemBuilder(getPresenterSequenceSetupManager().getDataSource("scOrderItemDS"));
                    bindItemBuilderEvents(display);
                    display.setDirty(true);
                    getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                    getDisplay().getStructuredContentSaveButton().enable();
                }
            }
        });
        for (ItemBuilderDisplay itemBuilder : getDisplay().getItemBuilderViews()) {
            bindItemBuilderEvents(itemBuilder);
        }
        getDisplay().getCustomerFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
            public void onFilterChanged(FilterChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
            }
        });
        getDisplay().getProductFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
            public void onFilterChanged(FilterChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
            }
        });
        getDisplay().getRequestFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
            public void onFilterChanged(FilterChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
            }
        });
        getDisplay().getTimeFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
            public void onFilterChanged(FilterChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
            }
        });
	}

    protected void save() {
        extractor.applyData(currentStructuredContentRecord);
    }

    protected void refresh() {
        getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().reset();
        FormOnlyView legacyForm = (FormOnlyView) ((FormOnlyView) getDisplay().getDynamicFormDisplay().getFormOnlyDisplay()).getMember("contentTypeForm");
        if (legacyForm != null) {
            legacyForm.getForm().reset();
        }
        getDisplay().getDynamicFormDisplay().getSaveButton().disable();
        getDisplay().getStructuredContentSaveButton().disable();
    }

    protected void bindItemBuilderEvents(final ItemBuilderDisplay display) {
        display.getRemoveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (getDisplay().getItemBuilderViews().size() > 1) {
                    extractor.removeItemQualifer(display);
                }
            }
        });
        display.getRawItemForm().addItemChangedHandler(new ItemChangedHandler() {
            public void onItemChanged(ItemChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
                display.setDirty(true);
            }
        });
        display.getItemForm().addItemChangedHandler(new ItemChangedHandler() {
            public void onItemChanged(ItemChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
                display.setDirty(true);
            }
        });
        display.getItemFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
            public void onFilterChanged(FilterChangedEvent event) {
                getDisplay().getDynamicFormDisplay().getSaveButton().enable();
                getDisplay().getStructuredContentSaveButton().enable();
                display.setDirty(true);
            }
        });
    }

	public void setup() {
        super.setup();
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("structuredContentDS", new StructuredContentListDataSourceFactory(), new NullAsyncCallbackAdapter()));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("scCustomerDS", new CustomerListDataSourceFactory(), new AsyncCallbackAdapter() {
            public void onSetupSuccess(DataSource result) {
                ((DynamicEntityDataSource) result).permanentlyShowFields("id");
            }
        }));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("scProductDS", new ProductListDataSourceFactory(), new NullAsyncCallbackAdapter()));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("timeDTODS", new TimeDTOListDataSourceFactory(), new NullAsyncCallbackAdapter()));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("requestDTODS", new RequestDTOListDataSourceFactory(), new NullAsyncCallbackAdapter()));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("scOrderItemDS", new OrderItemListDataSourceFactory(), new AsyncCallbackAdapter() {
            public void onSetupSuccess(DataSource result) {
                ((DynamicEntityDataSource) result).permanentlyShowFields("product.id", "category.id", "sku.id");
            }
        }));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("structuredContentTypeSearchDS", new StructuredContentTypeSearchListDataSourceFactory(), new OperationTypes(OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY, OperationType.ENTITY), new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				ListGridDataSource structuredContentTypeDataSource = (ListGridDataSource) result;
				structuredContentTypeDataSource.resetPermanentFieldVisibility(
					"name","description"
				);
				EntitySearchDialog structuredContentTypeSearchView = new EntitySearchDialog(structuredContentTypeDataSource, true);
                setupDisplayItems(
                    getPresenterSequenceSetupManager().getDataSource("structuredContentDS"),
                    getPresenterSequenceSetupManager().getDataSource("scCustomerDS"),
                    getPresenterSequenceSetupManager().getDataSource("timeDTODS"),
                    getPresenterSequenceSetupManager().getDataSource("requestDTODS"),
                    getPresenterSequenceSetupManager().getDataSource("scOrderItemDS"),
                    getPresenterSequenceSetupManager().getDataSource("scProductDS")
                );
				getPresenterSequenceSetupManager().getDataSource("structuredContentDS").
				getFormItemCallbackHandlerManager().addSearchFormItemCallback(
                        "structuredContentType",
                        structuredContentTypeSearchView,
                        "Structured Content Type Search",
                        getDisplay().getDynamicFormDisplay(),
                        new FormItemCallback() {
                            @Override
                            public void execute(FormItem formItem) {
                                if (currentStructuredContentRecord != null && BLCMain.ENTITY_ADD.getHidden()) {
                                    destroyContentTypeForm();
                                    loadContentTypeForm(currentStructuredContentRecord);
                                }
                            }
                        }
                );
                ((ListGridDataSource) getPresenterSequenceSetupManager().getDataSource("structuredContentDS")).setupGridFields(new String[]{"locked", "structuredContentType_Grid", "contentName", "locale", "offlineFlag"});
			}
		}));
        getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("scItemCriteriaDS", new StructuredContentItemCriteriaListDataSourceFactory(), new AsyncCallbackAdapter() {
            public void onSetupSuccess(DataSource result) {
                initializer = new StructuredContentPresenterInitializer(StructuredContentPresenter.this, (DynamicEntityDataSource) result, getPresenterSequenceSetupManager().getDataSource("scOrderItemDS"));
                extractor = new StructuredContentPresenterExtractor(StructuredContentPresenter.this);
            }
        }));
	}

	@Override
	public StructuredContentDisplay getDisplay() {
		return (StructuredContentDisplay) display;
	}
	
}
