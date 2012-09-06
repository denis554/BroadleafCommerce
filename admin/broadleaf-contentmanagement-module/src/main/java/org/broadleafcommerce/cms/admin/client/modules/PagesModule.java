/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.cms.admin.client.modules;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import org.broadleafcommerce.cms.admin.client.ContentManagementMessages;
import org.broadleafcommerce.cms.admin.client.GeneratedMessagesEntityCMS;
import org.broadleafcommerce.openadmin.client.AbstractHtmlEditingModule;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.GeneratedMessagesEntityCommon;
import org.broadleafcommerce.openadmin.client.GeneratedMessagesEntityOpenAdmin;

import java.util.ArrayList;
import java.util.List;

public class PagesModule extends AbstractHtmlEditingModule {

    public void onModuleLoad() {
        addConstants(GWT.<ConstantsWithLookup>create(ContentManagementMessages.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityCMS.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityCommon.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityOpenAdmin.class));

        setModuleTitle(BLCMain.getMessageManager().getString("cmsModuleTitle"));
        setModuleKey("BLCPagesModule");

        List<String> pagePermissions = new ArrayList<String>();
        pagePermissions.add("PERMISSION_CREATE_PAGE");
        pagePermissions.add("PERMISSION_UPDATE_PAGE");
        pagePermissions.add("PERMISSION_DELETE_PAGE");
        pagePermissions.add("PERMISSION_READ_PAGE");
        setSection(
                BLCMain.getMessageManager().getString("pagesTitle"),
                "pages",
                "org.broadleafcommerce.cms.admin.client.view.pages.PagesView",
                "pagesPresenter",
                "org.broadleafcommerce.cms.admin.client.presenter.pages.PagesPresenter",
                pagePermissions
        );


        setOrder(100);

        registerModule();
    }
}
