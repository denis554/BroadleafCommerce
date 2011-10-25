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
package org.broadleafcommerce.openadmin.client.setup;

import java.util.HashMap;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.History;
import com.smartgwt.client.widgets.Canvas;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.presenter.entity.EntityPresenter;
import org.broadleafcommerce.openadmin.client.reflection.AsyncClient;
import org.broadleafcommerce.openadmin.client.security.AdminUser;
import org.broadleafcommerce.openadmin.client.security.SecurityManager;
import org.broadleafcommerce.openadmin.client.service.AbstractCallback;
import org.broadleafcommerce.openadmin.client.service.AppServices;
import org.broadleafcommerce.openadmin.client.view.Display;
import org.broadleafcommerce.openadmin.client.view.UIFactory;

/**
 * 
 * @author jfischer
 *
 */
public class AppController implements ValueChangeHandler<String> {

	private static AppController controller = null;

	public static AppController getInstance() {
		if (controller == null) {
			AppController.controller = new AppController();
		}
		return AppController.controller;
	}

	private final HandlerManager eventBus = new HandlerManager(null);
	private Canvas container;
	private UIFactory uiFactory = new UIFactory();
	private HashMap<String, String[]> pages;

	private AppController() {
		bind();
	}

	private void bind() {
		History.addValueChangeHandler(this);
	}

    public void go(final Canvas container, HashMap<String, String[]> pages) {
        go(container, pages, false);
    }

	public void go(final Canvas container, HashMap<String, String[]> pages, boolean reset) {
		this.pages = pages;
		this.container = container;

		if ("".equals(History.getToken()) || reset) {
			for (String sectionTitle : pages.keySet()){
				if (SecurityManager.getInstance().isUserAuthorizedToViewSection(pages.get(sectionTitle)[0])){
					History.newItem(sectionTitle);
					break;
				}
			}
		} else {
			History.fireCurrentHistoryState();
		}
	}

	public HandlerManager getEventBus() {
		return eventBus;
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();

		if (token != null) {
			if (!uiFactory.equalsCurrentView(token)) {
				String[] vals = pages.get(token);
                if (vals == null) {
                    vals = pages.get(0);
                }
				showView(vals[0], vals[1]);
			}
		}
	}
	
	protected void showView(final String viewKey, final String presenterKey) {
        AppServices.SECURITY.getAdminUser(new AbstractCallback<AdminUser>() {
            @Override
            public void onSuccess(AdminUser result) {
                if (result == null) {
                    UrlBuilder builder = com.google.gwt.user.client.Window.Location.createUrlBuilder();
                    builder.setPath(BLCMain.webAppContext + "/admin.html");
                    builder.setParameter("time", String.valueOf(System.currentTimeMillis()));
                    com.google.gwt.user.client.Window.open(builder.buildString(), "_self", null);
                } else {
                    if (SecurityManager.getInstance().isUserAuthorizedToViewSection(viewKey)){
                        uiFactory.clearCurrentView();
                        uiFactory.getView(viewKey, false, false, new AsyncClient() {
                            @Override
                            public void onSuccess(Object instance) {
                                final Display view = (Display) instance;
                                uiFactory.getPresenter(presenterKey, new AsyncClient() {
                                    @Override
                                    public void onSuccess(Object instance) {
                                        EntityPresenter presenter = (EntityPresenter) instance;
                                        presenter.setDisplay(view);
                                        presenter.setEventBus(eventBus);
                                        BLCMain.currentViewKey = viewKey;
                                        if (presenter.getPresenterSequenceSetupManager() != null) {
                                            presenter.getPresenterSequenceSetupManager().setCanvas(container);
                                            presenter.setup();
                                            presenter.getPresenterSequenceSetupManager().launch();
                                        } else {
                                            presenter.setup();
                                        }
                                    }

                                    @Override
                                    public void onUnavailable() {
                                        throw new RuntimeException("Unable to show item: " + presenterKey);
                                    }
                                });
                            }

                            @Override
                            public void onUnavailable() {
                                throw new RuntimeException("Unable to show item: " + viewKey);
                            }
                        });
                    }
                }
            }
        });
	}
}
