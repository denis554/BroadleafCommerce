/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.openadmin.client.dto;

import org.broadleafcommerce.openadmin.client.dto.visitor.MetadataVisitor;

/**
 * @author Jeff Fischer
 */
public class MapMetadata extends CollectionMetadata {

    private String valueClassName;
    private String[][] keys;
    private boolean isSimpleValue;
    private String mediaField;
    private String mapKeyOptionEntityClass;
    private String mapKeyOptionEntityDisplayField;
    private String mapKeyOptionEntityValueField;

    public String getValueClassName() {
        return valueClassName;
    }

    public void setValueClassName(String valueClassName) {
        this.valueClassName = valueClassName;
    }

    public boolean isSimpleValue() {
        return isSimpleValue;
    }

    public void setSimpleValue(boolean simpleValue) {
        isSimpleValue = simpleValue;
    }

    public String getMediaField() {
        return mediaField;
    }

    public void setMediaField(String mediaField) {
        this.mediaField = mediaField;
    }

    public String[][] getKeys() {
        return keys;
    }

    public void setKeys(String[][] keys) {
        this.keys = keys;
    }

    public String getMapKeyOptionEntityClass() {
        return mapKeyOptionEntityClass;
    }

    public void setMapKeyOptionEntityClass(String mapKeyOptionEntityClass) {
        this.mapKeyOptionEntityClass = mapKeyOptionEntityClass;
    }

    public String getMapKeyOptionEntityDisplayField() {
        return mapKeyOptionEntityDisplayField;
    }

    public void setMapKeyOptionEntityDisplayField(String mapKeyOptionEntityDisplayField) {
        this.mapKeyOptionEntityDisplayField = mapKeyOptionEntityDisplayField;
    }

    public String getMapKeyOptionEntityValueField() {
        return mapKeyOptionEntityValueField;
    }

    public void setMapKeyOptionEntityValueField(String mapKeyOptionEntityValueField) {
        this.mapKeyOptionEntityValueField = mapKeyOptionEntityValueField;
    }

    @Override
    public void accept(MetadataVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected FieldMetadata populate(FieldMetadata metadata) {
        ((MapMetadata) metadata).valueClassName = valueClassName;
        ((MapMetadata) metadata).isSimpleValue = isSimpleValue;
        ((MapMetadata) metadata).mediaField = mediaField;
        ((MapMetadata) metadata).keys = keys;
        ((MapMetadata) metadata).mapKeyOptionEntityClass = mapKeyOptionEntityClass;
        ((MapMetadata) metadata).mapKeyOptionEntityDisplayField = mapKeyOptionEntityDisplayField;
        ((MapMetadata) metadata).mapKeyOptionEntityValueField = mapKeyOptionEntityValueField;

        return super.populate(metadata);
    }

    @Override
    public FieldMetadata cloneFieldMetadata() {
        MapMetadata metadata = new MapMetadata();
        return populate(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapMetadata)) return false;
        if (!super.equals(o)) return false;

        MapMetadata metadata = (MapMetadata) o;

        if (isSimpleValue != metadata.isSimpleValue) return false;
        if (mapKeyOptionEntityClass != null ? !mapKeyOptionEntityClass.equals(metadata.mapKeyOptionEntityClass) : metadata.mapKeyOptionEntityClass != null)
            return false;
        if (mapKeyOptionEntityDisplayField != null ? !mapKeyOptionEntityDisplayField.equals(metadata.mapKeyOptionEntityDisplayField) : metadata.mapKeyOptionEntityDisplayField != null)
            return false;
        if (mapKeyOptionEntityValueField != null ? !mapKeyOptionEntityValueField.equals(metadata.mapKeyOptionEntityValueField) : metadata.mapKeyOptionEntityValueField != null)
            return false;
        if (mediaField != null ? !mediaField.equals(metadata.mediaField) : metadata.mediaField != null) return false;
        if (valueClassName != null ? !valueClassName.equals(metadata.valueClassName) : metadata.valueClassName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (valueClassName != null ? valueClassName.hashCode() : 0);
        result = 31 * result + (isSimpleValue ? 1 : 0);
        result = 31 * result + (mediaField != null ? mediaField.hashCode() : 0);
        result = 31 * result + (mapKeyOptionEntityClass != null ? mapKeyOptionEntityClass.hashCode() : 0);
        result = 31 * result + (mapKeyOptionEntityDisplayField != null ? mapKeyOptionEntityDisplayField.hashCode() : 0);
        result = 31 * result + (mapKeyOptionEntityValueField != null ? mapKeyOptionEntityValueField.hashCode() : 0);
        return result;
    }
}
