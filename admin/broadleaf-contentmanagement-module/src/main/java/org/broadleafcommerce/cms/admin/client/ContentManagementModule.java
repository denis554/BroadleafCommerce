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

package org.broadleafcommerce.cms.admin.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import org.broadleafcommerce.openadmin.client.AbstractHtmlEditingModule;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.GeneratedMessagesEntityCommon;
import org.broadleafcommerce.openadmin.client.GeneratedMessagesEntityOpenAdmin;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jfischer
 *
 */
public class ContentManagementModule extends AbstractHtmlEditingModule {

	public void onModuleLoad() {
        addConstants(GWT.<ConstantsWithLookup>create(ContentManagementMessages.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityCMS.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityCommon.class));
        addConstants(GWT.<ConstantsWithLookup>create(GeneratedMessagesEntityOpenAdmin.class));
        
		setModuleTitle(BLCMain.getMessageManager().getString("cmsModuleTitle"));
		setModuleKey("BLCContentManagement");

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

        List<String> assetPermissions = new ArrayList<String>();
		assetPermissions.add("PERMISSION_CREATE_ASSET");
		assetPermissions.add("PERMISSION_UPDATE_ASSET");
        assetPermissions.add("PERMISSION_DELETE_ASSET");
        assetPermissions.add("PERMISSION_READ_ASSET");
        setSection(
            BLCMain.getMessageManager().getString("staticAssetsTitle"),
			"staticAssets",
			"org.broadleafcommerce.cms.admin.client.view.file.StaticAssetsView",
			"staticAssetsPresenter",
			"org.broadleafcommerce.cms.admin.client.presenter.file.StaticAssetsPresenter",
			assetPermissions
		);

        List<String> structuredContentPermissions = new ArrayList<String>();
		structuredContentPermissions.add("PERMISSION_CREATE_STRUCTURED_CONTENT");
		structuredContentPermissions.add("PERMISSION_UPDATE_STRUCTURED_CONTENT");
        structuredContentPermissions.add("PERMISSION_DELETE_STRUCTURED_CONTENT");
        structuredContentPermissions.add("PERMISSION_READ_STRUCTURED_CONTENT");
        setSection(
            BLCMain.getMessageManager().getString("structuredContentTitle"),
			"structuredContent",
			"org.broadleafcommerce.cms.admin.client.view.structure.StructuredContentView",
			"structuredContentPresenter",
			"org.broadleafcommerce.cms.admin.client.presenter.structure.StructuredContentPresenter",
			structuredContentPermissions
		);

        List<String> userSandBoxPermissions = new ArrayList<String>();
		userSandBoxPermissions.add("PERMISSION_ALL_USER_SANDBOX");
        setSection(
            BLCMain.getMessageManager().getString("userSandBoxTitle"),
			"userSandBox",
			"org.broadleafcommerce.cms.admin.client.view.sandbox.MySandBoxView",
			"userSandBoxPresenter",
			"org.broadleafcommerce.cms.admin.client.presenter.sandbox.MySandBoxPresenter",
			userSandBoxPermissions
		);

        List<String> approverSandBoxPermissions = new ArrayList<String>();
		approverSandBoxPermissions.add("PERMISSION_ALL_APPROVER_SANDBOX");
        setSection(
            BLCMain.getMessageManager().getString("approverSandBoxTitle"),
			"approverSandBox",
			"org.broadleafcommerce.cms.admin.client.view.sandbox.SandBoxView",
			"approverSandBoxPresenter",
			"org.broadleafcommerce.cms.admin.client.presenter.sandbox.SandBoxPresenter",
			approverSandBoxPermissions
		);

        setHtmlEditorIFramePath(GWT.getModuleBaseURL()+"admin/richTextFullFeatured.html");
        setBasicHtmlEditorIFramePath(GWT.getModuleBaseURL()+"admin/richTextBasic.html");
        registerModule();
    }
}
