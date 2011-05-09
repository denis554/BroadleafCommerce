package org.broadleafcommerce.gwt.client;

import java.util.HashMap;

import org.broadleafcommerce.gwt.client.presenter.dynamic.entity.EntityPresenter;
import org.broadleafcommerce.gwt.client.view.Display;
import org.broadleafcommerce.gwt.client.view.UIFactory;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.smartgwt.client.widgets.Canvas;

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
		this.pages = pages;
		this.container = container;

		if ("".equals(History.getToken())) {
			History.newItem(pages.keySet().iterator().next());
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
				showView(vals[0], vals[1]);
			}
		}
	}
	
	protected void showView(String viewKey, String presenterKey) {
		uiFactory.clearCurrentView();
		Display view = uiFactory.getView(viewKey, false, false);
		EntityPresenter presenter = uiFactory.getPresenter(presenterKey);
		presenter.setDisplay(view);
		presenter.setEventBus(eventBus);
		presenter.go(container);
	}
}
