package org.broadleafcommerce.gwt.client.view;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.widgets.Window;

public class ProgressWindow extends Window {
	
	private SimpleProgress simpleProgress;
	
	public ProgressWindow() {
    	setWidth(360);  
    	setHeight(52);  
    	setShowMinimizeButton(false);  
    	setIsModal(true);   
    	centerInPage();
    	setTitle("Contacting Server");
    	setShowCloseButton(false);
    	simpleProgress = new SimpleProgress(24);    
    	addItem(simpleProgress);
	}

	public void startProgress(Timer timer) {
		show();
		simpleProgress.startProgress();
		timer.schedule(300);
	}
	
	public void stopProgress() {
		simpleProgress.stopProgress();
		hide();
	}
	
	public Boolean isActive() {
		return simpleProgress.isActive();
	}
}
