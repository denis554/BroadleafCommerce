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
package org.broadleafcommerce.openadmin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.util.KeyCallback;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.SC;
import org.broadleafcommerce.openadmin.client.security.AdminUser;
import org.broadleafcommerce.openadmin.client.security.SecurityManager;
import org.broadleafcommerce.openadmin.client.service.AbstractCallback;
import org.broadleafcommerce.openadmin.client.service.AppServices;
import org.broadleafcommerce.openadmin.client.setup.AppController;
import org.broadleafcommerce.openadmin.client.view.MasterView;
import org.broadleafcommerce.openadmin.client.view.ProgressWindow;
import org.broadleafcommerce.openadmin.client.view.SimpleProgress;
import org.broadleafcommerce.openadmin.client.view.SplashView;
import org.broadleafcommerce.openadmin.client.view.SplashWindow;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.EntityEditDialog;
import org.broadleafcommerce.openadmin.client.view.dynamic.dialog.PolymorphicTypeSelectionDialog;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class BLCMain implements EntryPoint {
	
	public static final MessageManager MESSAGE_MANAGER = new MessageManager();
    static {
        MESSAGE_MANAGER.addConstants(GWT.<ConstantsWithLookup>create(OpenAdminMessages.class));
    }
	private static LinkedHashMap<String, Module> modules = new LinkedHashMap<String, Module>(10);

    public static String webAppContext;
    public static String storeFrontWebAppContext;
    public static String adminContext;
	public static ProgressWindow MODAL_PROGRESS = new ProgressWindow();
	public static SplashView SPLASH_PROGRESS = new SplashWindow(GWT.getModuleBaseURL()+"admin/images/splash_screen.jpg", "");
	public static SimpleProgress NON_MODAL_PROGRESS = new SimpleProgress(16, 150);
	public static EntityEditDialog ENTITY_ADD = new EntityEditDialog();
    public static PolymorphicTypeSelectionDialog POLYMORPHIC_ADD = new PolymorphicTypeSelectionDialog();
	public static MasterView MASTERVIEW;
	public static boolean ISNEW = true;
	public static String currentModuleKey;
    public static String currentPageKey;
    public static String currentViewKey;
	
	public static final boolean DEBUG = true;
	
	public static void addModule(Module module) {
		modules.put(module.getModuleKey(), module);
	}
	
	public static Module getModule(String moduleKey) {
		return modules.get(moduleKey);
	}
	
	public static void setSplashWindow(SplashView splashWindow) {
		SPLASH_PROGRESS = splashWindow;
	}
	
	public static void removeSplashWindow() {
		SPLASH_PROGRESS = null;
	}

    private static void setCurrentModuleKey(String requestedModuleKey) {
        if (requestedModuleKey != null && modules.get(requestedModuleKey) != null) {
            if (SecurityManager.getInstance().isUserAuthorizedToViewModule(requestedModuleKey)) {
                currentModuleKey = requestedModuleKey;
                return;
            }
        }

        currentModuleKey = null;

        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            Module currentModule = entry.getValue();
            if (SecurityManager.getInstance().isUserAuthorizedToViewModule(currentModule.getModuleKey())) {
                currentModuleKey = currentModule.getModuleKey();
                return;
            }
        }
    }

    private static void setCurrentPageKey(String requestedPageKey) {
        Map<String,String[]> pagesMap = modules.get(currentModuleKey).getPages();

        if (pagesMap.get(requestedPageKey) != null) {
            String pageView = pagesMap.get(requestedPageKey)[0];
            if (SecurityManager.getInstance().isUserAuthorizedToViewSection(pageView)) {
                currentPageKey = requestedPageKey;
                return;
            }
        }

        currentPageKey = null;
        for(String pageKey : pagesMap.keySet()) {
            String view = pagesMap.get(pageKey)[0];
            if (SecurityManager.getInstance().isUserAuthorizedToViewSection(view)) {
                currentPageKey = pageKey;
                return;
            }
        }
    }
	
	public static void drawCurrentState(final String requestedModuleKey, final String requestedPageKey) {
        SC.logWarn("Retrieving web app context...");
        AppServices.UTILITY.getWebAppContext(new AbstractCallback<String>() {
            @Override
            public void onSuccess(String result) {
                webAppContext = result;
                SC.logWarn("Web app context retrieved (" + result + ")");
                SC.logWarn("Retrieving storefront web app context...");
                AppServices.UTILITY.getStoreFrontWebAppContext(new AbstractCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (result != null) {
                            storeFrontWebAppContext = result;
                        } else {
                            storeFrontWebAppContext = webAppContext;
                        }
                        SC.logWarn("Web app context retrieved (" + result + ")");
                        SC.logWarn("Retrieving admin user...");
                        AppServices.SECURITY.getAdminUser(new AbstractCallback<AdminUser>() {
                            @Override
                            public void onSuccess(AdminUser result) {
                                SecurityManager.USER  = result;
                                if (result == null) {
                                    SC.logWarn("Admin user not found. Logging out...");
                                    UrlBuilder builder = Window.Location.createUrlBuilder();
                                    builder.setPath(BLCMain.webAppContext + "/adminLogout.htm");
                                    builder.setParameter("time", String.valueOf(System.currentTimeMillis()));
                                    Window.open(builder.buildString(), "_self", null);
                                } else {
                                    SC.logWarn("Admin user found. Loading interface...");
                                    setCurrentModuleKey(requestedModuleKey);

                                    if (currentModuleKey == null) {
                                        SC.say("Your login does not have authorization to view any modules.");
                                        return;
                                    }

                                    setCurrentPageKey(requestedPageKey);

                                    if (currentPageKey == null) {
                                        SC.say("Your login does not have authorization to view any pages for the passed in module.");
                                        return;
                                    }

                                    modules.get(currentModuleKey).preDraw();
                                    MASTERVIEW = new MasterView(currentModuleKey, currentPageKey, modules);
                                    MASTERVIEW.draw();
                                    AppController.getInstance().go(MASTERVIEW.getContainer(), modules.get(currentModuleKey).getPages(), currentPageKey, true);
                                    modules.get(currentModuleKey).postDraw();
                                }
                            }
                        });
                    }
                });
            }
        });
        adminContext = GWT.getModuleBaseURL();
	}

	public void onModuleLoad() {
		if (!GWT.isScript()) { 
		    KeyIdentifier debugKey = new KeyIdentifier(); 
		    debugKey.setCtrlKey(true); 
		    debugKey.setKeyName("D"); 
		    Page.registerKey(debugKey, new KeyCallback() { 
		        public void execute(String keyName) { 
		            SC.showConsole(); 
		        }
		    });
		}
	}

    public static MessageManager getMessageManager() {
        return MESSAGE_MANAGER;
    }

	/**
     * Log a debug.
     *
     * @param message  the message to log
     * @param category category to log in, defaults to "Log"
     */
    public static native void logDebug(String message, String category) /*-{
    	if ($wnd.isc.Log.logIsDebugEnabled(category)) {
    		$wnd.isc.Log.logDebug(message, category);
    	}
	}-*/;
    
    public static native boolean isLogDebugEnabled(String category) /*-{
		return $wnd.isc.Log.logIsDebugEnabled(category)
	}-*/;

}
