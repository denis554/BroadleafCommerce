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
package org.broadleafcommerce.gwt.admin.client.view.catalog.product;

import org.broadleafcommerce.gwt.admin.client.AdminModule;
import org.broadleafcommerce.gwt.client.reflection.Instantiable;
import org.broadleafcommerce.gwt.client.view.TabSet;
import org.broadleafcommerce.gwt.client.view.dynamic.DynamicEntityListDisplay;
import org.broadleafcommerce.gwt.client.view.dynamic.DynamicEntityListView;
import org.broadleafcommerce.gwt.client.view.dynamic.form.DynamicFormDisplay;
import org.broadleafcommerce.gwt.client.view.dynamic.form.DynamicFormView;
import org.broadleafcommerce.gwt.client.view.dynamic.form.FormOnlyView;
import org.broadleafcommerce.gwt.client.view.dynamic.grid.GridStructureDisplay;
import org.broadleafcommerce.gwt.client.view.dynamic.grid.GridStructureView;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

/**
 * 
 * @author jfischer
 *
 */
public class OneToOneProductSkuView extends HLayout implements Instantiable, OneToOneProductSkuDisplay {
	
	protected DynamicFormView dynamicFormDisplay;
	protected GridStructureView crossSaleDisplay;
	protected GridStructureView upSaleDisplay;
	protected GridStructureView mediaDisplay;
	protected DynamicEntityListView listDisplay;
	protected GridStructureView attributesDisplay;
	protected GridStructureView allCategoriesDisplay;
    
	public OneToOneProductSkuView() {
		setHeight100();
		setWidth100();
	}
	
	public void build(DataSource entityDataSource, DataSource... additionalDataSources) {
		VLayout leftVerticalLayout = new VLayout();
		leftVerticalLayout.setID("productSkuLeftVerticalLayout");
		leftVerticalLayout.setHeight100();
		leftVerticalLayout.setWidth("50%");
		leftVerticalLayout.setShowResizeBar(true);
        
		listDisplay = new DynamicEntityListView(AdminModule.ADMINMESSAGES.productsListTitle(), entityDataSource);
        leftVerticalLayout.addMember(listDisplay);
        
        TabSet topTabSet = new TabSet(); 
        topTabSet.setID("productSkuTopTabSet");
        topTabSet.setTabBarPosition(Side.TOP);  
        topTabSet.setPaneContainerOverflow(Overflow.HIDDEN);
        topTabSet.setWidth("50%");  
        topTabSet.setHeight100();
        topTabSet.setPaneMargin(0);
        
        Tab detailsTab = new Tab("Details");
        detailsTab.setID("productSkuDetailsTab");
        
        dynamicFormDisplay = new DynamicFormView(AdminModule.ADMINMESSAGES.productDetailsTitle(), entityDataSource);
        attributesDisplay = new GridStructureView(AdminModule.ADMINMESSAGES.productAttributesTitle(), false, true);
        ((FormOnlyView) dynamicFormDisplay.getFormOnlyDisplay()).addMember(attributesDisplay);
        detailsTab.setPane(dynamicFormDisplay);
        
        Tab crossSaleTab = new Tab("Featured"); 
        crossSaleTab.setID("productSkuCrossSaleTab");
        
        VLayout crossLayout = new VLayout();
        crossLayout.setID("productSkuCrossLayout");
        crossLayout.setHeight100();
        crossLayout.setWidth100();
        crossLayout.setBackgroundColor("#eaeaea");
        crossLayout.setOverflow(Overflow.AUTO);
        
        crossSaleDisplay = new GridStructureView(AdminModule.ADMINMESSAGES.crossSaleProductsTitle(), true, true);
        crossLayout.addMember(crossSaleDisplay);
        
        upSaleDisplay = new GridStructureView(AdminModule.ADMINMESSAGES.upsaleProductsTitle(), true, true);
        crossLayout.addMember(upSaleDisplay);
        
        crossSaleTab.setPane(crossLayout);
        
        Tab mediaTab = new Tab(AdminModule.ADMINMESSAGES.mediaTabTitle()); 
        mediaTab.setID("productSkuMediaTab");
        
        VLayout mediaLayout = new VLayout();
        mediaLayout.setID("productSkuMediaLayout");
        mediaLayout.setHeight100();
        mediaLayout.setWidth100();
        mediaLayout.setBackgroundColor("#eaeaea");
        mediaLayout.setOverflow(Overflow.AUTO);
        
        mediaDisplay = new GridStructureView(AdminModule.ADMINMESSAGES.mediaListTitle(), false, true);
        mediaLayout.addMember(mediaDisplay);
        
        mediaTab.setPane(mediaLayout);
        
        Tab categoriesTab = new Tab("Categories");
        categoriesTab.setID("productSkuCategoriesTab");
        
        VLayout categoriesLayout = new VLayout();
        categoriesLayout.setID("productSkuCategoriesLayout");
        categoriesLayout.setHeight100();
        categoriesLayout.setWidth100();
        categoriesLayout.setBackgroundColor("#eaeaea");
        categoriesLayout.setOverflow(Overflow.AUTO);
        
        allCategoriesDisplay = new GridStructureView(AdminModule.ADMINMESSAGES.allParentCategoriesListTitle(), false, false);
        categoriesLayout.addMember(allCategoriesDisplay);
        
        categoriesTab.setPane(categoriesLayout);
        
        topTabSet.addTab(detailsTab);
        topTabSet.addTab(crossSaleTab);
        topTabSet.addTab(mediaTab);
        topTabSet.addTab(categoriesTab);
        
        addMember(leftVerticalLayout);
        addMember(topTabSet);
	}

	public Canvas asCanvas() {
		return this;
	}

	public GridStructureDisplay getCrossSaleDisplay() {
		return crossSaleDisplay;
	}

	public GridStructureDisplay getUpSaleDisplay() {
		return upSaleDisplay;
	}

	public GridStructureDisplay getMediaDisplay() {
		return mediaDisplay;
	}

	public DynamicFormDisplay getDynamicFormDisplay() {
		return dynamicFormDisplay;
	}
	
	public DynamicEntityListDisplay getListDisplay() {
		return listDisplay;
	}
	
	public GridStructureDisplay getAttributesDisplay() {
		return attributesDisplay;
	}

	public GridStructureDisplay getAllCategoriesDisplay() {
		return allCategoriesDisplay;
	}

}
