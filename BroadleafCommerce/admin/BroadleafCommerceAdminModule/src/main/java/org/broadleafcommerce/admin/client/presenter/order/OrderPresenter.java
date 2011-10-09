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

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import org.broadleafcommerce.admin.client.datasource.order.*;
import org.broadleafcommerce.admin.client.view.order.OrderDisplay;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.presenter.entity.DynamicEntityPresenter;
import org.broadleafcommerce.openadmin.client.presenter.entity.SubPresentable;
import org.broadleafcommerce.openadmin.client.presenter.entity.SubPresenter;
import org.broadleafcommerce.openadmin.client.presenter.structure.CreateBasedListStructurePresenter;
import org.broadleafcommerce.openadmin.client.presenter.structure.SimpleMapStructurePresenter;
import org.broadleafcommerce.openadmin.client.reflection.Instantiable;
import org.broadleafcommerce.openadmin.client.setup.AsyncCallbackAdapter;
import org.broadleafcommerce.openadmin.client.setup.NullAsyncCallbackAdapter;
import org.broadleafcommerce.openadmin.client.setup.PresenterSetupItem;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.EntitySearchDialog;
import org.broadleafcommerce.openadmin.client.view.dynamic.form.DynamicFormDisplay;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class OrderPresenter extends DynamicEntityPresenter implements Instantiable {
	
	protected OrderItemPresenter orderItemPresenter;
	protected SubPresentable fulfillmentGroupPresenter;
	protected SubPresentable paymentInfoPresenter;
	protected SubPresentable additionalPaymentAttributesPresenter;
	protected SubPresentable offerCodePresenter;
	protected SubPresentable orderAdjustmentPresenter;
	protected SubPresentable orderItemAdjustmentPresenter;
	protected SubPresentable fulfillmentGroupAdjustmentPresenter;
	protected SubPresentable feesPresenter;
	protected HashMap<String, Object> library = new HashMap<String, Object>();
	
	@Override
	protected void changeSelection(final Record selectedRecord) {
		orderItemPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderDS"), null);
		fulfillmentGroupPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderDS"), null);
		paymentInfoPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderDS"), null);
		offerCodePresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderDS"), null);
		orderAdjustmentPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderDS"), null);
	}
	
	@Override
	public void bind() {
		super.bind();
		orderItemPresenter.bind();
		fulfillmentGroupPresenter.bind();
		paymentInfoPresenter.bind();
		additionalPaymentAttributesPresenter.bind();
		offerCodePresenter.bind();
		orderAdjustmentPresenter.bind();
		orderItemAdjustmentPresenter.bind();
		fulfillmentGroupAdjustmentPresenter.bind();
		feesPresenter.bind();
		selectionChangedHandlerRegistration.removeHandler();
		display.getListDisplay().getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selectedRecord = event.getSelectedRecord();
				if (event.getState()) {
					if (!selectedRecord.equals(lastSelectedRecord)) {
						lastSelectedRecord = selectedRecord;
						if (selectedRecord.getAttributeAsStringArray("_type") == null){
							formPresenter.disable();
							display.getListDisplay().getRemoveButton().disable();
						} else {
							formPresenter.setStartState();
							getPresenterSequenceSetupManager().getDataSource("orderDS").resetPermanentFieldVisibilityBasedOnType(selectedRecord.getAttributeAsStringArray("_type"));
							display.getDynamicFormDisplay().getFormOnlyDisplay().buildFields(display.getListDisplay().getGrid().getDataSource(), false, false, false);
							display.getDynamicFormDisplay().getFormOnlyDisplay().getForm().editRecord(selectedRecord);
							display.getListDisplay().getRemoveButton().enable();
						}
						changeSelection(selectedRecord);
					}
				}
			}
		});
		getDisplay().getPaymentInfoDisplay().getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selectedRecord = event.getSelectedRecord();
				if (event.getState()) {
					additionalPaymentAttributesPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("paymentInfoDS"), null);
				}
			}
		});
		getDisplay().getOrderItemsDisplay().getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selectedRecord = event.getSelectedRecord();
				if (event.getState()) {
					orderItemAdjustmentPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("orderItemDS"), null);
					feesPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("discreteOrderItemFeePriceDS"), null);
				}
			}
		});
		getDisplay().getFulfillmentGroupDisplay().getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selectedRecord = event.getSelectedRecord();
				if (event.getState()) {
					fulfillmentGroupAdjustmentPresenter.load(selectedRecord, getPresenterSequenceSetupManager().getDataSource("fulfillmentGroupDS"), null);
				}
			}
		});
		setReadOnly(true);
	}
	
	@Override
	public OrderDisplay getDisplay() {
		return (OrderDisplay) display;
	}
	
	@Override
	protected void addClicked() {
		//do nothing
	}

	public void setup() {
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("orderDS", new OrderListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource top) {
				setupDisplayItems(top);
				((ListGridDataSource) top).setupGridFields(new String[]{"customer.firstName", "customer.lastName", "name", "orderNumber", "status"});
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("orderItemDS", new OrderItemListDataSourceFactory(), null, new Object[]{}, new NullAsyncCallbackAdapter()));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("bundleOrderItemDS", new BundledOrderItemListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				orderItemPresenter = new OrderItemPresenter(((OrderDisplay) getDisplay()).getOrderItemsDisplay());
				orderItemPresenter.setDataSource((ListGridDataSource) getPresenterSequenceSetupManager().getDataSource("orderItemDS"), new String[]{"name", "quantity", "price", "retailPrice", "salePrice"}, new Boolean[]{false, false, false, false, false});
				((OrderItemPresenter) orderItemPresenter).setExpansionDataSource((ListGridDataSource) result, new String[]{"name", "quantity", "price", "retailPrice", "salePrice"}, new Boolean[]{false, false, false, false, false});
				orderItemPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("countryDS", new CountryListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				((ListGridDataSource) result).resetPermanentFieldVisibility(
					"abbreviation",
					"name"
				);
				EntitySearchDialog countrySearchView = new EntitySearchDialog((ListGridDataSource) result);
				getPresenterSequenceSetupManager().getDataSource("orderDS").
				getFormItemCallbackHandlerManager().addSearchFormItemCallback(
					"address.country", 
					countrySearchView,
                    BLCMain.getMessageManager().getString("countrySearchPrompt"),
					(DynamicFormDisplay) ((OrderDisplay) getDisplay()).getFulfillmentGroupDisplay()
				);
				library.put("countrySearchView", countrySearchView);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("stateDS", new StateListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				((ListGridDataSource) result).resetPermanentFieldVisibility(
					"abbreviation",
					"name"
				);
				EntitySearchDialog stateSearchView = new EntitySearchDialog((ListGridDataSource) result);
				getPresenterSequenceSetupManager().getDataSource("orderDS").
				getFormItemCallbackHandlerManager().addSearchFormItemCallback(
					"address.state", 
					stateSearchView,
                    BLCMain.getMessageManager().getString("stateSearchPrompt"),
					(DynamicFormDisplay) ((OrderDisplay) getDisplay()).getFulfillmentGroupDisplay()
				);
				library.put("stateSearchView", stateSearchView);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("fulfillmentGroupDS", new FulfillmentGroupListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				fulfillmentGroupPresenter = new SubPresenter(((OrderDisplay) getDisplay()).getFulfillmentGroupDisplay());
				fulfillmentGroupPresenter.setDataSource((ListGridDataSource) result, new String[]{"referenceNumber", "method", "service", "shippingPrice", "status", "address.postalCode"}, new Boolean[]{false, false, false, false, false, false});
				fulfillmentGroupPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("paymentInfoDS", new PaymentInfoListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				paymentInfoPresenter = new SubPresenter(((OrderDisplay) getDisplay()).getPaymentInfoDisplay());
				paymentInfoPresenter.setDataSource((ListGridDataSource) result, new String[]{"referenceNumber", "type", "amount"}, new Boolean[]{false, false, false});
				paymentInfoPresenter.setReadOnly(true);
				
				((DynamicEntityDataSource) result).
				getFormItemCallbackHandlerManager().addSearchFormItemCallback(
					"address.country", 
					(EntitySearchDialog) library.get("countrySearchView"),
                    BLCMain.getMessageManager().getString("countrySearchPrompt"),
					(DynamicFormDisplay) ((OrderDisplay) getDisplay()).getFulfillmentGroupDisplay()
				);
				((DynamicEntityDataSource) result).
				getFormItemCallbackHandlerManager().addSearchFormItemCallback(
					"address.state", 
					(EntitySearchDialog) library.get("stateSearchView"),
                    BLCMain.getMessageManager().getString("stateSearchPrompt"),
					(DynamicFormDisplay) ((OrderDisplay) getDisplay()).getFulfillmentGroupDisplay()
				);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("paymentAdditionalAttributesDS", new PaymentAdditionalAttributesDataSourceFactory(this), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				Map<String, Object> initialValues = new HashMap<String, Object>();
				initialValues.put("key", BLCMain.getMessageManager().getString("paymentAttributeKeyDefault"));
				initialValues.put("value", BLCMain.getMessageManager().getString("paymentAttributeValueDefault"));
				additionalPaymentAttributesPresenter = new SimpleMapStructurePresenter(((OrderDisplay) getDisplay()).getAdditionalAttributesDisplay(), initialValues);
				additionalPaymentAttributesPresenter.setDataSource((ListGridDataSource) result, new String[]{"key", "value"}, new Boolean[]{true, true});
				additionalPaymentAttributesPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("offerCodeDS", new OfferCodeListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				offerCodePresenter = new SubPresenter(((OrderDisplay) getDisplay()).getOfferCodeDisplay());
				offerCodePresenter.setDataSource((ListGridDataSource) result, new String[]{"offerCode", "startDate", "endDate", "offer.name", "offer.type", "offer.value"}, new Boolean[]{false, false, false, false, false, false});
				offerCodePresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("orderAdjustmentDS", new OrderAdjustmentListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				orderAdjustmentPresenter = new CreateBasedListStructurePresenter(((OrderDisplay) getDisplay()).getOrderAdjustmentDisplay(), BLCMain.getMessageManager().getString("newOrderAdjustmentTitle"));
				orderAdjustmentPresenter.setDataSource((ListGridDataSource) result, new String[]{"reason", "value", "offer.name", "offer.type"}, new Boolean[]{false, false, false, false});
				orderAdjustmentPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("orderItemAdjustmentDS", new OrderItemAdjustmentListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				orderItemAdjustmentPresenter = new CreateBasedListStructurePresenter(((OrderDisplay) getDisplay()).getOrderItemAdjustmentDisplay(), BLCMain.getMessageManager().getString("newOrderItemAdjustmentTitle"));
				orderItemAdjustmentPresenter.setDataSource((ListGridDataSource) result, new String[]{"reason", "value", "offer.type"}, new Boolean[]{false, false, false});
				orderItemAdjustmentPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("fulfillmentGroupAdjustmentDS", new FulfillmentGroupAdjustmentListDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				fulfillmentGroupAdjustmentPresenter = new CreateBasedListStructurePresenter(((OrderDisplay) getDisplay()).getFulfillmentGroupAdjustmentDisplay(), BLCMain.getMessageManager().getString("newFGAdjustmentTitle"));
				fulfillmentGroupAdjustmentPresenter.setDataSource((ListGridDataSource) result, new String[]{"reason", "value", "offer.type"}, new Boolean[]{false, false, false});
				fulfillmentGroupAdjustmentPresenter.setReadOnly(true);
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("discreteOrderItemFeePriceDS", new DiscreteOrderItemFeePriceDataSourceFactory(), null, new Object[]{}, new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				feesPresenter = new CreateBasedListStructurePresenter(((OrderDisplay) getDisplay()).getOrderItemFeeDisplay(), BLCMain.getMessageManager().getString("newOrderItemFeeTitle"));
				feesPresenter.setDataSource((ListGridDataSource) result, new String[]{"name", "amount", "reportingCode"}, new Boolean[]{false, false, false});
				feesPresenter.setReadOnly(true);
			}
		}));
	}
	
}
