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

package org.broadleafcommerce.openadmin.client.view.dynamic.form;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * 
 * @author jfischer
 *
 */
public class FormOnlyView extends VLayout implements FormOnlyDisplay {
	
	protected DynamicForm form;
	
	public FormOnlyView() {
		this(null);
	}

    public FormOnlyView(DataSource dataSource) {
        this(dataSource, null, null, null);
    }
	
	public FormOnlyView(DataSource dataSource, Boolean showDisabedState, Boolean canEdit, Boolean showId) {
		super();
		
        setWidth100();
        setBackgroundColor("#eaeaea");
        form = new DynamicForm(); 
        form.setHeight(175);
        form.setWidth100();
        form.setPadding(10);
        form.disable();
        form.setBackgroundColor("#eaeaea");
        if (dataSource != null) {
        	buildFields(dataSource, showDisabedState==null?true:showDisabedState, canEdit==null?false:canEdit, showId==null?false:showId, null);
        }
        addMember(form);
        setOverflow(Overflow.AUTO);
	}
	
	public void buildFields(final DataSource dataSource, Boolean showDisabedState, Boolean canEdit, Boolean showId, Record currentRecord) {
		FormBuilder.buildForm(dataSource, form, showDisabedState, canEdit, showId, currentRecord);
	}

	public DynamicForm getForm() {
		return form;
	}
	
}
