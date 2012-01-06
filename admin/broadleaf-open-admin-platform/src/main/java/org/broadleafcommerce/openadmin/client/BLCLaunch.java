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
import com.google.gwt.user.client.History;

/**
 * 
 * @author jfischer
 *
 */
public class BLCLaunch implements EntryPoint {
    private static String PAGE_FRAGMENT = "pageKey=";
    private static String MODULE_FRAGMENT = "moduleKey=";
    private static String ITEM_FRAGMENT = "itemId=";


	public void onModuleLoad() {
		if (BLCMain.SPLASH_PROGRESS != null) {
			BLCMain.SPLASH_PROGRESS.startProgress();
		}

        String currentModulePage = History.getToken();
        BLCMain.drawCurrentState(getSelectedModule(currentModulePage), getSelectedPage(currentModulePage));
	}
    
    private static String getSelectedString(String currentModulePage, String fragment) {
        String returnParam = null;
        if (currentModulePage != null) {
            int start = currentModulePage.indexOf(fragment);
            int ampLocation = currentModulePage.indexOf("&", start);

            if (start >= 0) {
                start = start + fragment.length();
                int end = currentModulePage.length();
                if (ampLocation > 0 && ampLocation > start) {
                    end = ampLocation;
                }

                returnParam = currentModulePage.substring(start,end);
            }
        }
        return returnParam;
    }

    public static String getSelectedModule(String currentModulePage) {
        return getSelectedString(currentModulePage, MODULE_FRAGMENT);
    }

    public static String getSelectedPage(String currentModulePage) {
        return getSelectedString(currentModulePage, PAGE_FRAGMENT);
    }

    public static String getDefaultItem(String url) {
        return getSelectedString(url, ITEM_FRAGMENT);
    }

}
