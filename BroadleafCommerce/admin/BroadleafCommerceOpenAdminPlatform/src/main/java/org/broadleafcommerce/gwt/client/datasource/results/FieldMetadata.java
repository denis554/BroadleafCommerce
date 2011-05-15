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
package org.broadleafcommerce.gwt.client.datasource.results;

import java.io.Serializable;
import java.util.Arrays;

import org.broadleafcommerce.gwt.client.presentation.SupportedFieldType;

/**
 * 
 * @author jfischer
 *
 */
public class FieldMetadata implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private SupportedFieldType fieldType;
	private SupportedFieldType secondaryType = SupportedFieldType.INTEGER;
	private Integer length;
	private Boolean required;
	private Boolean unique;
	private Integer scale;
	private Integer precision;
	private Boolean mutable;
	private String inheritedFromType;
	private String[] availableToTypes;
	private String foreignKeyProperty;
	private String foreignKeyClass;
	private String foreignKeyDisplayValueProperty;
	private Boolean collection;
	private MergedPropertyType mergedPropertyType;
	//private String prefix;
	private String[][] enumerationValues;
	private String enumerationClass;
	
	private FieldPresentationAttributes presentationAttributes = new FieldPresentationAttributes();
	
	public SupportedFieldType getFieldType() {
		return fieldType;
	}
	
	public void setFieldType(SupportedFieldType fieldType) {
		this.fieldType = fieldType;
	}
	
	public SupportedFieldType getSecondaryType() {
		return secondaryType;
	}

	public void setSecondaryType(SupportedFieldType secondaryType) {
		this.secondaryType = secondaryType;
	}

	public Integer getLength() {
		return length;
	}
	
	public void setLength(Integer length) {
		this.length = length;
	}
	
	public Boolean getRequired() {
		return required;
	}
	
	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public Boolean getMutable() {
		return mutable;
	}

	public void setMutable(Boolean mutable) {
		this.mutable = mutable;
	}

	/*public String getComplexType() {
		return complexType;
	}

	public void setComplexType(String complexType) {
		this.complexType = complexType;
	}*/

	public String[] getAvailableToTypes() {
		return availableToTypes;
	}

	public void setAvailableToTypes(String[] availableToTypes) {
		Arrays.sort(availableToTypes);
		this.availableToTypes = availableToTypes;
	}

	public String getForeignKeyProperty() {
		return foreignKeyProperty;
	}

	public void setForeignKeyProperty(String foreignKeyProperty) {
		this.foreignKeyProperty = foreignKeyProperty;
	}

	public String getInheritedFromType() {
		return inheritedFromType;
	}

	public void setInheritedFromType(String inheritedFromType) {
		this.inheritedFromType = inheritedFromType;
	}

	public String getForeignKeyClass() {
		return foreignKeyClass;
	}

	public void setForeignKeyClass(String foreignKeyClass) {
		this.foreignKeyClass = foreignKeyClass;
	}

	public FieldPresentationAttributes getPresentationAttributes() {
		return presentationAttributes;
	}

	public void setPresentationAttributes(FieldPresentationAttributes presentationAttributes) {
		this.presentationAttributes = presentationAttributes;
	}

	public Boolean getCollection() {
		return collection;
	}

	public void setCollection(Boolean collection) {
		this.collection = collection;
	}

	public MergedPropertyType getMergedPropertyType() {
		return mergedPropertyType;
	}

	public void setMergedPropertyType(MergedPropertyType mergedPropertyType) {
		this.mergedPropertyType = mergedPropertyType;
	}

//	public String getPrefix() {
//		return prefix;
//	}
//
//	public void setPrefix(String prefix) {
//		this.prefix = prefix;
//	}

	public String[][] getEnumerationValues() {
		return enumerationValues;
	}

	public void setEnumerationValues(String[][] enumerationValues) {
		this.enumerationValues = enumerationValues;
	}

	public String getForeignKeyDisplayValueProperty() {
		return foreignKeyDisplayValueProperty;
	}

	public void setForeignKeyDisplayValueProperty(String foreignKeyDisplayValueProperty) {
		this.foreignKeyDisplayValueProperty = foreignKeyDisplayValueProperty;
	}

	public String getEnumerationClass() {
		return enumerationClass;
	}

	public void setEnumerationClass(String enumerationClass) {
		this.enumerationClass = enumerationClass;
	}

}
