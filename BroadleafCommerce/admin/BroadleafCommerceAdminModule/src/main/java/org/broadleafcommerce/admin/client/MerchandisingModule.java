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

package org.broadleafcommerce.admin.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import org.broadleafcommerce.openadmin.client.AbstractModule;
import org.broadleafcommerce.openadmin.client.BLCMain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jfischer
 *
 */
public class MerchandisingModule extends AbstractModule {
	
	public void onModuleLoad() {
        addConstants(GWT.<ConstantsWithLookup>create(MerchandisingMessages.class));
        addConstants(GWT.<ConstantsWithLookup>create(PromotionMessages.class));
		
		setModuleTitle(BLCMain.getMessageManager().getString("merchandisingModuleTitle"));
		setModuleKey("BLCMerchandising");
		
		List<String> categoryPermissions = new ArrayList<String>();
		categoryPermissions.add("PERMISSION_CREATE_CATEGORY");
		categoryPermissions.add("PERMISSION_UPDATE_CATEGORY");
        categoryPermissions.add("PERMISSION_DELETE_CATEGORY");
        categoryPermissions.add("PERMISSION_READ_CATEGORY");
		setSection(
            BLCMain.getMessageManager().getString("categoryMainTitle"),
			"category",
			"org.broadleafcommerce.admin.client.view.catalog.category.CategoryView",
			"categoryPresenter",
			"org.broadleafcommerce.admin.client.presenter.catalog.category.CategoryPresenter",
			categoryPermissions
		);
		List<String> productPermissions = new ArrayList<String>();
		productPermissions.add("PERMISSION_CREATE_PRODUCT");
		productPermissions.add("PERMISSION_UPDATE_PRODUCT");
        productPermissions.add("PERMISSION_DELETE_PRODUCT");
        productPermissions.add("PERMISSION_READ_PRODUCT");
		setSection(
            BLCMain.getMessageManager().getString("productMainTitle"),
			"product",
			"org.broadleafcommerce.admin.client.view.catalog.product.OneToOneProductSkuView",
			"productPresenter",
			"org.broadleafcommerce.admin.client.presenter.catalog.product.OneToOneProductSkuPresenter",
			productPermissions
		);

		List<String> offerPermissions = new ArrayList<String>();
		offerPermissions.add("PERMISSION_CREATE_PROMOTION");
		offerPermissions.add("PERMISSION_UPDATE_PROMOTION");
        offerPermissions.add("PERMISSION_DELETE_PROMOTION");
        offerPermissions.add("PERMISSION_READ_PROMOTION");
		setSection(
            BLCMain.getMessageManager().getString("promotionMainTitle"),
			"offer",
			"org.broadleafcommerce.admin.client.view.promotion.OfferView",
			"offerPresenter",
			"org.broadleafcommerce.admin.client.presenter.promotion.OfferPresenter",
			offerPermissions
		);

		registerModule();
	}

	@Override
	public void postDraw() {
		ImgButton sgwtHomeButton = new ImgButton();
        sgwtHomeButton.setSrc(GWT.getModuleBaseURL() + "admin/images/blc_logo.png");
        sgwtHomeButton.setWidth(98);
        sgwtHomeButton.setHeight(50);
        sgwtHomeButton.setPrompt(BLCMain.getMessageManager().getString("blcProjectPage"));
        sgwtHomeButton.setHoverStyle("interactImageHover");
        sgwtHomeButton.setShowRollOver(false);
        sgwtHomeButton.setShowDownIcon(false);
        sgwtHomeButton.setShowDown(false);
        sgwtHomeButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
            public void onClick(ClickEvent event) {
                com.google.gwt.user.client.Window.open("http://www.broadleafcommerce.org", "sgwt", null);
            }
        });
        BLCMain.MASTERVIEW.getTopBar().addMember(sgwtHomeButton, 1);  
	}

}
