package org.broadleafcommerce.gwt.admin.client.presenter.promotion.offer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.broadleafcommerce.gwt.admin.client.AdminModule;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.CustomerListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.FulfillmentGroupListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.OfferItemCriteriaListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.OfferListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.OrderItemListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.promotion.offer.OrderListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.view.promotion.offer.ItemBuilderDisplay;
import org.broadleafcommerce.gwt.admin.client.view.promotion.offer.OfferDisplay;
import org.broadleafcommerce.gwt.client.BLCMain;
import org.broadleafcommerce.gwt.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.AsyncCallbackAdapter;
import org.broadleafcommerce.gwt.client.event.NewItemCreatedEvent;
import org.broadleafcommerce.gwt.client.event.NewItemCreatedEventHandler;
import org.broadleafcommerce.gwt.client.presenter.entity.DynamicEntityPresenter;
import org.broadleafcommerce.gwt.client.reflection.Instantiable;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseMoveEvent;
import com.smartgwt.client.widgets.events.MouseMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.form.events.FilterChangedEvent;
import com.smartgwt.client.widgets.form.events.FilterChangedHandler;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

public class OfferPresenter extends DynamicEntityPresenter implements Instantiable {
	
	protected ListGridDataSource entityDataSource;
	protected ListGridDataSource orderItemDataSource;
	protected Window currentHelp = null;
	protected DynamicEntityDataSource offerItemCriteriaDataSource;
	protected OfferPresenterInitializer initializer;
	protected OfferPresenterExtractor extractor;
	
	@Override
	protected void changeSelection(final Record selectedRecord) {
		BLCMain.MASTERVIEW.clearStatus();
		getDisplay().getAdvancedButton().setSelected(false);
		getDisplay().getAdvancedButton().enable();
		rebindFormItems(selectedRecord);
		getDisplay().getDeliveryTypeRadio().enable();
		getDisplay().getCustomerRuleRadio().enable();
		getDisplay().getOrderRuleRadio().enable();
		
		String sectionType = getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().getField("type").getValue().toString();
		initializer.initSectionBasedOnType(sectionType, selectedRecord);
	}
	
	protected void rebindFormItems(final Record selectedRecord) {
		//Since the form is built dynamically each time the grid selection changes, we have to re-bind the event
		getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().getField("type").addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String eventValue = event.getValue().toString();
				initializer.initSectionBasedOnType(eventValue, selectedRecord);
			}
		});
		FormItem endDate = getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().getField("endDate");
		if (endDate != null) {
			endDate.addChangedHandler(new ChangedHandler() {
				@SuppressWarnings("deprecation")
				public void onChanged(ChangedEvent event) {
					Date myDate = (Date) event.getValue();
					myDate.setHours(23);
					myDate.setMinutes(59);
					myDate.setSeconds(59);
					event.getItem().setValue(myDate);
				}
			});
		}
	}
	
	@Override
	protected void addClicked() {
		Map<String, Object> initialValues = new HashMap<String, Object>();
		initialValues.put("name", AdminModule.ADMINMESSAGES.offerNameDefault());
		initialValues.put("type", "ORDER_ITEM");
		initialValues.put("value", 0);
		initialValues.put("deliveryType", "AUTOMATIC");
		initialValues.put("_type", new String[]{((DynamicEntityDataSource) getDisplay().getListDisplay().getGrid().getDataSource()).getDefaultNewEntityFullyQualifiedClassname()});
		BLCMain.ENTITY_ADD.editNewRecord(AdminModule.ADMINMESSAGES.newOfferTitle(), (DynamicEntityDataSource) getDisplay().getListDisplay().getGrid().getDataSource(), initialValues, new NewItemCreatedEventHandler() {
			public void onNewItemCreated(NewItemCreatedEvent event) {
				Criteria myCriteria = new Criteria();
				myCriteria.addCriteria("name", event.getRecord().getAttribute("name"));
				display.getListDisplay().getGrid().fetchData(myCriteria);
			}
		}, "90%", new String[]{"name"}, null);
	}

	@Override
	public void bind() {
		super.bind();
		getDisplay().getStepFGForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getStepItemForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getStepBogoForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getOrderCombineForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getRawCustomerForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getRawOrderForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getRawFGForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getRestrictForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getCustomerObtainForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getWhichCustomerForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getOrderForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getReceiveFromAnotherPromoForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getQualifyForAnotherPromoForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getReceiveFromAnotherPromoTargetForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getQualifyForAnotherPromoTargetForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getStepFGCombineForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
			}
		});
		getDisplay().getAdvancedButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (((ToolStripButton) event.getSource()).getSelected()) {
					entityDataSource.resetPermanentFieldVisibilityBasedOnType(getDisplay().getListDisplay().getGrid().getSelectedRecord().getAttributeAsStringArray("_type"));
					entityDataSource.permanentlyHideFields("deliveryType", "offerItemQualifierRuleType", "offerItemTargetRuleType", "uses", "targetItemCriteria.id", "targetItemCriteria.receiveQuantity", "targetItemCriteria.requiresQuantity", "targetItemCriteria.orderItemMatchRule");
					getDisplay().getAdvancedItemCriteria().setVisible(true);
					getDisplay().getAdvancedItemCriteriaTarget().setVisible(true);
					getDisplay().getRestrictionSectionView().setVisible(true);
				} else {
					entityDataSource.resetVisibilityOnly("name", "description", "type", "discountType", "value", "priority", "startDate", "endDate");
					getDisplay().getAdvancedItemCriteria().setVisible(false);
					getDisplay().getAdvancedItemCriteriaTarget().setVisible(false);
					getDisplay().getRestrictionSectionView().setVisible(false);
				}
				@SuppressWarnings("rawtypes")
				Map values = getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().getValues();
				Object[] keys = values.keySet().toArray();
				for (Object key : keys) {
					if (key.toString().equals("__ref")) {
						values.remove(key);
					}
				}
				getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().buildFields(entityDataSource, true, true, true);
				getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().editRecord(getDisplay().getListDisplay().getGrid().getSelectedRecord());
				getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().setValues(values);
				rebindFormItems(display.getListDisplay().getGrid().getSelectedRecord());
			}
		});
		selectionChangedHandlerRegistration.removeHandler();
		getDisplay().getListDisplay().getGrid().addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selectedRecord = event.getSelectedRecord();
				if (event.getState()) {
					if (!selectedRecord.equals(lastSelectedRecord)) {
						lastSelectedRecord = selectedRecord;
						if (selectedRecord.getAttributeAsStringArray("_type") == null){
							formPresenter.disable();
							getDisplay().getListDisplay().getRemoveButton().disable();
						} else {
							formPresenter.setStartState();
							entityDataSource.resetVisibilityOnly("name", "description", "type", "discountType", "value", "priority", "startDate", "endDate");
							getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().buildFields(getDisplay().getListDisplay().getGrid().getDataSource(), true, true, true);
							getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().editRecord(selectedRecord);
							getDisplay().getListDisplay().getRemoveButton().enable();
						}
						changeSelection(selectedRecord);
					}
				}
			}
		});
		formPresenter.getSaveButtonHandlerRegistration().removeHandler();
		getDisplay().getDynamicFormDisplay().getSaveButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
					extractor.applyData(getDisplay().getListDisplay().getGrid().getSelectedRecord());
				}
			}
        });
		getDisplay().getHelpButtonType().addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				if (currentHelp == null) {
					currentHelp = createHelpWin(
							AdminModule.ADMINMESSAGES.offerObtainSettingsHelpTitle(), 
							AdminModule.ADMINMESSAGES.offerObtainSettingsHelpContent(),
							true, 300, 200, getDisplay().getHelpButtonType().getAbsoluteLeft() + 26, getDisplay().getHelpButtonType().getAbsoluteTop()
					);
					currentHelp.show();
				}
			}
		});
		getDisplay().getHelpButtonType().addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				if (currentHelp != null) {
					currentHelp.destroy();
					currentHelp = null;
				}
			}
		});
		getDisplay().getDeliveryTypeRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String deliveryType = event.getValue().toString();
				initializer.initDeliveryType(deliveryType);
			}
		});
		getDisplay().getCustomerRuleRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String customerRule = event.getValue().toString();
				initializer.initCustomerRule(customerRule);
			}
		});
		getDisplay().getFgRuleRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String fgRule = event.getValue().toString();
				initializer.initFGRule(fgRule);
			}
		});
		getDisplay().getItemRuleRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String itemRule = event.getValue().toString();
				initializer.initItemRule(itemRule);
			}
		});
		getDisplay().getOrderRuleRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String orderRule = event.getValue().toString();
				initializer.initOrderRule(orderRule);
			}
		});
		getDisplay().getHelpButtonBogo().addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				if (currentHelp == null) {
					currentHelp = createHelpWin(
							AdminModule.ADMINMESSAGES.bogoHelpTitle(), 
							AdminModule.ADMINMESSAGES.bogoHelpContent(),
							true, 300, 200, getDisplay().getHelpButtonBogo().getAbsoluteLeft() + 26, getDisplay().getHelpButtonBogo().getAbsoluteTop()
					);
					currentHelp.show();
				}
			}
		});
		getDisplay().getHelpButtonBogo().addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				if (currentHelp != null) {
					currentHelp.destroy();
					currentHelp = null;
				}
			}
		});
		getDisplay().getBogoRadio().addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				String bogoRule = event.getValue().toString();
				initializer.initBogoRule(bogoRule);
			}
		});
		getDisplay().getAddItemButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
					final ItemBuilderDisplay display = getDisplay().addItemBuilder(orderItemDataSource);
					bindItemBuilderEvents(display);
					display.setDirty(true);
					getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				}
			}
		});
		for (final ItemBuilderDisplay display : getDisplay().getItemBuilderViews()) {
			bindItemBuilderEvents(display);
		}
		getDisplay().getCustomerFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
			public void onFilterChanged(FilterChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getRawCustomerTextArea().setAttribute("dirty", true);
			}
		});
		getDisplay().getOrderFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
			public void onFilterChanged(FilterChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getRawOrderTextArea().setAttribute("dirty", true);
			}
		});
		getDisplay().getFulfillmentGroupFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
			public void onFilterChanged(FilterChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getRawFGTextArea().setAttribute("dirty", true);
			}
		});
		getDisplay().getTargetItemBuilder().getRawItemForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getTargetItemBuilder().setDirty(true);
			}
		});
		getDisplay().getTargetItemBuilder().getItemForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getTargetItemBuilder().setDirty(true);
			}
		});
		getDisplay().getTargetItemBuilder().getItemFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
			public void onFilterChanged(FilterChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				getDisplay().getTargetItemBuilder().setDirty(true);
			}
		});
	}

	protected void bindItemBuilderEvents(final ItemBuilderDisplay display) {
		display.getRemoveButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				extractor.removeItemQualifer(display);
			}
		});
		display.getRawItemForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				display.setDirty(true);
			}
		});
		display.getItemForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				display.setDirty(true);
			}
		});
		display.getItemFilterBuilder().addFilterChangedHandler(new FilterChangedHandler() {
			public void onFilterChanged(FilterChangedEvent event) {
				getDisplay().getDynamicFormDisplay().getSaveButton().enable();
				display.setDirty(true);
			}
		});
	}

	@Override
	public void go(final Canvas container) {
		BLCMain.MODAL_PROGRESS.startProgress(new Timer() {
			public void run() {
				if (loaded) {
					OfferPresenter.super.go(container);
					return;
				}
				OfferListDataSourceFactory.createDataSource("offerDS", new AsyncCallbackAdapter() {
					public void onSuccess(final DataSource top) {
						entityDataSource = (ListGridDataSource) top;
						OrderListDataSourceFactory.createDataSource("offerOrderDS", new AsyncCallbackAdapter() {
							public void onSuccess(final DataSource offerOrderDS) {
								OrderItemListDataSourceFactory.createDataSource("offerOrderItemDS", new AsyncCallbackAdapter() {
									public void onSuccess(final DataSource offerOrderItemDS) {
										orderItemDataSource = (ListGridDataSource) offerOrderItemDS;
										((DynamicEntityDataSource) offerOrderItemDS).permanentlyShowFields("product.id", "category.id", "sku.id");
										FulfillmentGroupListDataSourceFactory.createDataSource("offerFGDS", new AsyncCallbackAdapter() {
											public void onSuccess(final DataSource offerFGDS) {
												CustomerListDataSourceFactory.createDataSource("offerCustomerDS", new AsyncCallbackAdapter() {
													public void onSuccess(final DataSource offerCustomerDS) {
														((DynamicEntityDataSource) offerCustomerDS).permanentlyShowFields("id");
														((ListGridDataSource) top).permanentlyHideFields("appliesToOrderRules", "appliesToCustomerRules", "appliesToFulfillmentGroupRules", "id");
														((ListGridDataSource) top).resetVisibilityOnly("name", "description", "type", "discountType", "value", "priority", "startDate", "endDate");
														setupDisplayItems(top, offerOrderDS, offerOrderItemDS, offerFGDS, offerCustomerDS);
														((ListGridDataSource) top).setupGridFields(new String[]{"name"}, new Boolean[]{true});
														
														OfferItemCriteriaListDataSourceFactory.createDataSource("offerItemCriteriaDS", new AsyncCallbackAdapter() {
															public void onSuccess(final DataSource offerItemCriteriaDS) {
																offerItemCriteriaDataSource = (DynamicEntityDataSource) offerItemCriteriaDS;
																initializer = new OfferPresenterInitializer(OfferPresenter.this, offerItemCriteriaDataSource, orderItemDataSource);
																extractor = new OfferPresenterExtractor(OfferPresenter.this);
																OfferPresenter.super.go(container);
															}
														});
													}
												});
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
	}

	@Override
	public OfferDisplay getDisplay() {
		return (OfferDisplay) display;
	}
	
	public Window createHelpWin(String title, String content, boolean autoSizing, int width, int height, int left, int top) {  
        Label label = new Label(content);  
        label.setWidth100();  
        label.setHeight100();  
        label.setPadding(5);  
        label.setValign(VerticalAlignment.TOP);  
  
        Window window = new Window();  
        window.setAutoSize(autoSizing);  
        window.setTitle(title);  
        window.setWidth(width);  
        window.setHeight(height);
        window.setLeft(left); 
        window.setCanDragReposition(true);  
        window.setCanDragResize(true);  
        window.addItem(label); 
        window.setShowCloseButton(false);
        window.setShowMinimizeButton(false);
        window.setTop(top - window.getHeight());
  
        return window;  
    }
 
}
