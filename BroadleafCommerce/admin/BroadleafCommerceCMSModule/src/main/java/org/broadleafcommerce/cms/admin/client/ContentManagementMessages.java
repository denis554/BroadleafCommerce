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

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;

/**
 * 
 * @author jfischer
 *
 */
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
@DefaultLocale("en_US")
public interface ContentManagementMessages extends ConstantsWithLookup {

    public String cmsModuleTitle();
    public String assetListTitle();
    public String newAssetTitle();
    public String deleteAssetTitle();

    public String pageListTitle();
    public String newPageTitle();
    public String deletePageTitle();



    public String structuredContentListTitle();
    public String newStructuredContentTitle();
    public String deleteStructuredContentTitle();



    public String pagesTitle();
    public String detailsTitle();
    public String allChildItemsTitle();
    public String defaultPageName();
    public String newItemTitle();

    public String staticAssetsTitle();
    public String staticAssetFoldersTitle();
    public String assetDescriptionTitle();
    public String newAssetDescriptionTitle();

    public String structuredContentTitle();

    public String userSandBoxTitle();
    public String approverSandBoxTitle();
    public String pendingApprovalTitle();

    public String contentTypeFilterTitle();
}
