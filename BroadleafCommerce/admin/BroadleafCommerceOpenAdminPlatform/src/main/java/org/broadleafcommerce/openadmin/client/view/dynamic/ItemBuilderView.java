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

package org.broadleafcommerce.openadmin.client.view.dynamic;

import org.broadleafcommerce.openadmin.client.datasource.dynamic.FieldDataSourceWrapper;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FilterBuilder;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.IntegerItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * 
 * @author jfischer
 *
 */
public class ItemBuilderView extends HLayout implements ItemBuilderDisplay {
	
	protected FormItem itemQuantity;
	protected FilterBuilder itemFilterBuilder;
	protected ImgButton removeButton;
	protected Label label;
	protected DynamicForm itemForm;
	protected DynamicForm rawItemForm;
	protected TextAreaItem rawItemTextArea;
	protected Record record;
	protected Boolean incompatibleMVEL = false;
	protected Boolean dirty = false;
	

	public ItemBuilderView(DataSource itemDataSource, Boolean allowDelete) {
		super(10);
        setHeight(46);
		if (allowDelete) {
			VLayout removeLayout = new VLayout();
			removeLayout.setAlign(VerticalAlignment.TOP);
			removeLayout.setHeight(46);
			removeLayout.setWidth(16);

            VLayout innerRemoveLayout = new VLayout();
            innerRemoveLayout.setAlign(VerticalAlignment.CENTER);
			innerRemoveLayout.setHeight(38);
			innerRemoveLayout.setWidth(16);

			removeButton = new ImgButton();
			removeButton.setSrc(GWT.getModuleBaseURL()+"sc/skins/Enterprise/images/actions/remove.png");
			removeButton.setShowRollOver(false);
			removeButton.setShowDownIcon(false);
			removeButton.setShowDown(false);
			removeButton.setWidth(16);
			removeButton.setHeight(16);

            innerRemoveLayout.addMember(removeButton);
			removeLayout.addMember(innerRemoveLayout);
			addMember(removeLayout);
		}
        
		VLayout formLayout = new VLayout();
		formLayout.setAlign(VerticalAlignment.TOP);
		formLayout.setWidth(30);
		formLayout.setHeight(46);

        VLayout innerFormLayout = new VLayout();
		innerFormLayout.setAlign(VerticalAlignment.CENTER);
		innerFormLayout.setWidth(30);
		innerFormLayout.setHeight(38);

		itemForm = new DynamicForm();
        itemQuantity = new IntegerItem();
        itemQuantity.setShowTitle(false);
        itemQuantity.setValue(1);
        itemQuantity.setWidth(40);

		itemForm.setItems(itemQuantity);
		innerFormLayout.addMember(itemForm);
        formLayout.addMember(innerFormLayout);
		addMember(formLayout);
		
		VLayout labelLayout = new VLayout();
		labelLayout.setAlign(VerticalAlignment.TOP);
		labelLayout.setWidth(20);
		labelLayout.setHeight(46);

        VLayout innerLabelLayout = new VLayout();
		innerLabelLayout.setAlign(VerticalAlignment.CENTER);
		innerLabelLayout.setWidth(20);
		innerLabelLayout.setHeight(38);

		label = new Label("Of");
		label.setWidth(20);
		label.setHeight(20);
		innerLabelLayout.addMember(label);
        labelLayout.addMember(innerLabelLayout);
		addMember(labelLayout);
		
		VLayout builderLayout = new VLayout();
		builderLayout.setHeight(46);
		builderLayout.setAlign(VerticalAlignment.CENTER);

        VLayout innerBuilderLayout = new VLayout();
		innerBuilderLayout.setHeight(38);
		innerBuilderLayout.setAlign(VerticalAlignment.CENTER);

		itemFilterBuilder = new FilterBuilder();  
		itemFilterBuilder.setDataSource(itemDataSource);
		itemFilterBuilder.setFieldDataSource(new FieldDataSourceWrapper(itemDataSource));
		itemFilterBuilder.setLayoutBottomMargin(10);
		itemFilterBuilder.setAllowEmpty(true);
		itemFilterBuilder.setValidateOnChange(false);
		innerBuilderLayout.addMember(itemFilterBuilder);
        builderLayout.addMember(innerBuilderLayout);
		
		rawItemForm = new DynamicForm();
		rawItemForm.setVisible(false);
        rawItemTextArea = new TextAreaItem();
        rawItemTextArea.setHeight(70);
        rawItemTextArea.setWidth("600");
        rawItemTextArea.setShowTitle(false);
        rawItemForm.setFields(rawItemTextArea);
        builderLayout.addMember(rawItemForm);
        
		addMember(builderLayout);
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.admin.client.view.promotion.offer.ItemBuilderDisplay#getItemQuantity()
	 */
	public FormItem getItemQuantity() {
		return itemQuantity;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.admin.client.view.promotion.offer.ItemBuilderDisplay#getItemFilterBuilder()
	 */
	public FilterBuilder getItemFilterBuilder() {
		return itemFilterBuilder;
	}

	public ImgButton getRemoveButton() {
		return removeButton;
	}
	
	public DynamicForm getRawItemForm() {
		return rawItemForm;
	}

	public TextAreaItem getRawItemTextArea() {
		return rawItemTextArea;
	}
	
	public DynamicForm getItemForm() {
		return itemForm;
	}

	public Boolean getIncompatibleMVEL() {
		return incompatibleMVEL;
	}

	public void setIncompatibleMVEL(Boolean incompatibleMVEL) {
		this.incompatibleMVEL = incompatibleMVEL;
	}

	public Boolean getDirty() {
		return dirty;
	}

	public void setDirty(Boolean dirty) {
		this.dirty = dirty;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public void enable() {
		removeButton.enable();
		itemQuantity.enable();
		itemFilterBuilder.enable();
	}
	
	public void disable() {
		removeButton.disable();
		itemQuantity.disable();
		itemFilterBuilder.disable();
	}
	
	public void hide() {
		removeButton.setVisible(false);
		itemForm.setVisible(false);
		itemFilterBuilder.setVisible(false);
		label.setVisible(false);
	}
	
	public void show() {
		removeButton.setVisible(true);
		itemForm.setVisible(true);
		itemFilterBuilder.setVisible(true);
		label.setVisible(true);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemForm == null) ? 0 : itemForm.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemBuilderView other = (ItemBuilderView) obj;
		if (itemForm == null) {
			if (other.itemForm != null)
				return false;
		} else if (!itemForm.equals(other.itemForm))
			return false;
		return true;
	}
	
}
