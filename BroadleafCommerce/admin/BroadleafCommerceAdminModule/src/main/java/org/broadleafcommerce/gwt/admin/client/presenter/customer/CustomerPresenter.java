package org.broadleafcommerce.gwt.admin.client.presenter.customer;

import java.util.HashMap;
import java.util.Map;

import org.broadleafcommerce.gwt.admin.client.AdminModule;
import org.broadleafcommerce.gwt.admin.client.datasource.customer.ChallengeQuestionListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.datasource.customer.CustomerListDataSourceFactory;
import org.broadleafcommerce.gwt.admin.client.view.customer.CustomerDisplay;
import org.broadleafcommerce.gwt.admin.client.view.customer.PasswordUpdateDialog;
import org.broadleafcommerce.gwt.client.BLCMain;
import org.broadleafcommerce.gwt.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.gwt.client.datasource.dynamic.operation.AsyncCallbackAdapter;
import org.broadleafcommerce.gwt.client.event.NewItemCreatedEvent;
import org.broadleafcommerce.gwt.client.event.NewItemCreatedEventHandler;
import org.broadleafcommerce.gwt.client.presenter.entity.DynamicEntityPresenter;
import org.broadleafcommerce.gwt.client.reflection.Instantiable;
import org.broadleafcommerce.gwt.client.view.dynamic.dialog.EntitySearchDialog;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class CustomerPresenter extends DynamicEntityPresenter implements Instantiable {
	
	protected PasswordUpdateDialog passwordUpdateDialog = new PasswordUpdateDialog();
	
	@Override
	protected void changeSelection(final Record selectedRecord) {
		getDisplay().getUpdateLoginButton().enable();
	}
	
	@Override
	protected void addClicked() {
		Map<String, Object> initialValues = new HashMap<String, Object>();
		initialValues.put("username", AdminModule.ADMINMESSAGES.usernameDefault());
		initialValues.put("_type", new String[]{((DynamicEntityDataSource) display.getListDisplay().getGrid().getDataSource()).getDefaultNewEntityFullyQualifiedClassname()});
		BLCMain.ENTITY_ADD.editNewRecord(AdminModule.ADMINMESSAGES.newCustomerTitle(), (DynamicEntityDataSource) display.getListDisplay().getGrid().getDataSource(), initialValues, new NewItemCreatedEventHandler() {
			public void onNewItemCreated(NewItemCreatedEvent event) {
				Criteria myCriteria = new Criteria();
				myCriteria.addCriteria("username", event.getRecord().getAttribute("username"));
				display.getListDisplay().getGrid().fetchData(myCriteria);
			}
		}, "90%", null, null);
	}

	@Override
	public void bind() {
		super.bind();
		getDisplay().getUpdateLoginButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
					passwordUpdateDialog.updatePassword(display.getListDisplay().getGrid().getSelectedRecord());
				}
			}
		});
	}

	@Override
	public void go(final Canvas container) {
		BLCMain.MODAL_PROGRESS.startProgress(new Timer() {
			public void run() {
				if (loaded) {
					CustomerPresenter.super.go(container);
					return;
				}
				CustomerListDataSourceFactory.createDataSource("customerDS", new AsyncCallbackAdapter() {
					public void onSuccess(final DataSource top) {
						setupDisplayItems(top);
						((ListGridDataSource) top).setupGridFields(new String[]{"username", "firstName", "lastName", "emailAddress"}, new Boolean[]{true, true, true, true});
							
						ChallengeQuestionListDataSourceFactory.createDataSource("challengeQuestionDS", new AsyncCallbackAdapter() {
							public void onSuccess(final DataSource challengeQuestionDS) {
								((ListGridDataSource) challengeQuestionDS).resetPermanentFieldVisibility(
									"question"
								);
								final EntitySearchDialog challengeQuestionSearchView = new EntitySearchDialog((ListGridDataSource) challengeQuestionDS);
								
								((DynamicEntityDataSource) top).
								getFormItemCallbackHandlerManager().addSearchFormItemCallback(
									"challengeQuestion", 
									challengeQuestionSearchView, 
									AdminModule.ADMINMESSAGES.challengeQuestionSearchPrompt(), 
									display.getDynamicFormDisplay()
								);
								CustomerPresenter.super.go(container);
							}
						});
					}
				});
			}
		});
	}

	@Override
	public CustomerDisplay getDisplay() {
		return (CustomerDisplay) display;
	}
	
}
