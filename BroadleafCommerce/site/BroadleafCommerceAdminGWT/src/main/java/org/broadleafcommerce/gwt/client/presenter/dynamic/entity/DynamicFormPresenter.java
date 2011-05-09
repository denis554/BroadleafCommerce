package org.broadleafcommerce.gwt.client.presenter.dynamic.entity;

import org.broadleafcommerce.gwt.client.view.dynamic.form.DynamicFormDisplay;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;

public class DynamicFormPresenter {

	protected DynamicFormDisplay display;
	
	public DynamicFormPresenter(DynamicFormDisplay display) {
		this.display = display;
	}
	
	public void setStartState() {
		display.getSaveButton().disable();
		display.getFormOnlyDisplay().getForm().enable();
		display.getRefreshButton().enable();
	}
	
	public void enable() {
		display.getSaveButton().enable();
		display.getFormOnlyDisplay().getForm().enable();
		display.getRefreshButton().enable();
	}
	
	public void disable() {
		display.getSaveButton().disable();
		display.getFormOnlyDisplay().getForm().disable();
		display.getRefreshButton().disable();
		display.getFormOnlyDisplay().getForm().reset();
	}
	
	public void bind() {
		display.getSaveButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
					display.getFormOnlyDisplay().getForm().saveData();
					display.getSaveButton().disable();
				}
			}
        });
		display.getRefreshButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.isLeftButtonDown()) {
					display.getFormOnlyDisplay().getForm().reset();
					display.getSaveButton().disable();
				}
			}
        });
		display.getFormOnlyDisplay().getForm().addItemChangedHandler(new ItemChangedHandler() {
			public void onItemChanged(ItemChangedEvent event) {
				display.getSaveButton().enable();
			}
		});
	}
}
