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

package org.broadleafcommerce.admin.client.presenter.customer;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import org.broadleafcommerce.admin.client.datasource.customer.ChallengeQuestionListDataSourceFactory;
import org.broadleafcommerce.admin.client.datasource.customer.CustomerListDataSourceFactory;
import org.broadleafcommerce.admin.client.view.customer.CustomerDisplay;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.callback.ItemEditedHandler;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.ListGridDataSource;
import org.broadleafcommerce.openadmin.client.dto.*;
import org.broadleafcommerce.openadmin.client.callback.ItemEdited;
import org.broadleafcommerce.openadmin.client.presenter.entity.DynamicEntityPresenter;
import org.broadleafcommerce.openadmin.client.reflection.Instantiable;
import org.broadleafcommerce.openadmin.client.service.AbstractCallback;
import org.broadleafcommerce.openadmin.client.service.AppServices;
import org.broadleafcommerce.openadmin.client.setup.AsyncCallbackAdapter;
import org.broadleafcommerce.openadmin.client.setup.PresenterSetupItem;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.EntitySearchDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class CustomerPresenter extends DynamicEntityPresenter implements Instantiable {
	
	@Override
	protected void changeSelection(final Record selectedRecord) {
		getDisplay().getUpdateLoginButton().enable();
	}
	
	@Override
	protected void addClicked() {
		Map<String, Object> initialValues = new HashMap<String, Object>();
		initialValues.put("username", BLCMain.getMessageManager().getString("usernameDefault"));
		initialValues.put("_type", new String[]{getPresenterSequenceSetupManager().getDataSource("customerDS").getDefaultNewEntityFullyQualifiedClassname()});
		BLCMain.ENTITY_ADD.editNewRecord(BLCMain.getMessageManager().getString("newCustomerTitle"), getPresenterSequenceSetupManager().getDataSource("customerDS"), initialValues, new ItemEditedHandler() {
			public void onItemEdited(ItemEdited event) {
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
					SC.confirm(BLCMain.getMessageManager().getString("confirmResetPassword"), new BooleanCallback() {
						public void execute(Boolean value) {
							if (value) {
								BLCMain.NON_MODAL_PROGRESS.startProgress();
								
								PersistencePerspective tempPerspective = new PersistencePerspective();
			            		OperationTypes opTypes = new OperationTypes();
			            		opTypes.setUpdateType(OperationType.ENTITY);
			            		tempPerspective.setOperationTypes(opTypes);
			            		
								final Entity entity = new Entity();
			            		Property prop = new Property();
			            		prop.setName("username");
			            		prop.setValue(display.getListDisplay().getGrid().getSelectedRecord().getAttribute("username"));
			            		entity.setProperties(new Property[]{prop});
			            		entity.setType(new String[]{"org.broadleafcommerce.profile.core.domain.Customer"});
			            		
			            		AppServices.DYNAMIC_ENTITY.update(new PersistencePackage("org.broadleafcommerce.profile.core.domain.Customer", entity, tempPerspective, new String[]{"passwordUpdate"}), new AbstractCallback<Entity>() {
									public void onSuccess(Entity arg0) {
										BLCMain.NON_MODAL_PROGRESS.stopProgress();
										SC.say(BLCMain.getMessageManager().getString("resetPasswordSuccessful"));
									}	
			            		}); 
							}
						}
					});
				}
			}
		});
	}

	public void setup() {
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("customerDS", new CustomerListDataSourceFactory(), new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource top) {
				setupDisplayItems(top);
				((ListGridDataSource) top).setupGridFields(new String[]{"username", "firstName", "lastName", "emailAddress"}, new Boolean[]{true, true, true, true});
			}
		}));
		getPresenterSequenceSetupManager().addOrReplaceItem(new PresenterSetupItem("challengeQuestionDS", new ChallengeQuestionListDataSourceFactory(), new AsyncCallbackAdapter() {
			public void onSetupSuccess(DataSource result) {
				((ListGridDataSource) result).resetPermanentFieldVisibility("question");
                final EntitySearchDialog challengeQuestionSearchView = new EntitySearchDialog((ListGridDataSource) result);
                getPresenterSequenceSetupManager().getDataSource("customerDS").
                getFormItemCallbackHandlerManager().addSearchFormItemCallback(
                    "challengeQuestion",
                    challengeQuestionSearchView,
                    BLCMain.getMessageManager().getString("challengeQuestionSearchPrompt"),
                    display.getDynamicFormDisplay()
                );
			}
		}));
	}

	@Override
	public CustomerDisplay getDisplay() {
		return (CustomerDisplay) display;
	}
	
}
