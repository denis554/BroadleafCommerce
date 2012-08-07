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

package org.broadleafcommerce.cms.admin.client.presenter.structure;


import org.broadleafcommerce.cms.admin.client.view.structure.StructuredContentDisplay;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;

import com.smartgwt.client.data.Record;

/**
 * 
 * @author jfischer
 *
 */
public class StructuredContentPresenterInitializer extends PresenterInitializer<StructuredContentPresenter, StructuredContentDisplay> {

	public StructuredContentPresenterInitializer(StructuredContentPresenter presenter, DynamicEntityDataSource offerItemCriteriaDataSource, DynamicEntityDataSource orderItemDataSource) {
		this.presenter = presenter;
		this.offerItemCriteriaDataSource = offerItemCriteriaDataSource;
		this.orderItemDataSource = orderItemDataSource;
	}
	public void initSection(Record selectedRecord, boolean disabled) {
	    initFilterBuilder(((StructuredContentDisplay)getDisplay()).getCustomerFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.CUSTOMER)));
	    initFilterBuilder(((StructuredContentDisplay)getDisplay()).getProductFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.PRODUCT)));
	    initFilterBuilder(((StructuredContentDisplay)getDisplay()).getRequestFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.REQUEST)));
	    initFilterBuilder(((StructuredContentDisplay)getDisplay()).getTimeFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.TIME)));
		initItemQualifiers(selectedRecord, disabled);
	}
	protected void bindItemBuilderEvents(org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay display) {
		((StructuredContentPresenter) presenter).bindItemBuilderEvents(display);
	};

}
