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

package org.broadleafcommerce.cms.structure.domain;

import org.broadleafcommerce.openadmin.audit.AdminAuditable;
import org.broadleafcommerce.openadmin.audit.AdminAuditableListener;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

/**
 * Created by bpolster.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SC_FLD")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
@EntityListeners(value = { AdminAuditableListener.class })
public class StructuredContentFieldImpl implements StructuredContentField {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "StructuredContentFieldId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "StructuredContentFieldId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "StructuredContentFieldImpl", allocationSize = 10)
    @Column(name = "SC_FLD_ID")
    protected Long id;

    @Embedded
    @AdminPresentation(excluded = true)
    protected AdminAuditable auditable = new AdminAuditable();

    @Column (name = "FLD_KEY")
    protected String fieldKey;

    @ManyToOne(targetEntity = StructuredContentImpl.class)
    @JoinColumn(name="SC_ID")
    protected StructuredContent structuredContent;

    @Column (name = "VALUE")
    protected String stringValue;

    @Column (name = "LOB_VALUE")
    @Lob
    protected String lobValue;
    
    @Transient
    protected String processedValue;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getFieldKey() {
        return fieldKey;
    }

    @Override
    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    @Override
    public StructuredContent getStructuredContent() {
        return structuredContent;
    }

    @Override
    public void setStructuredContent(StructuredContent structuredContent) {
        this.structuredContent = structuredContent;
    }

    @Override
    public String getValue() {
        if (lobValue != null && lobValue.length() > 0) {
            return lobValue;
        } else {
            return stringValue;
        }
    }

    @Override
    public void setValue(String value) {
        if (value != null) {
            if (value.length() <= 256) {
                stringValue = value;
                lobValue = null;
            } else {
                stringValue = null;
                lobValue = value;
            }
        } else {
            lobValue = null;
            stringValue = null;
        }
    }

    @Override
    public StructuredContentField cloneEntity() {
        StructuredContentFieldImpl newContentField = new StructuredContentFieldImpl();
        newContentField.fieldKey = fieldKey;
        newContentField.structuredContent = structuredContent;
        newContentField.lobValue = lobValue;
        newContentField.stringValue = stringValue;

        return newContentField;

    }

    public AdminAuditable getAuditable() {
        return auditable;
    }

    public void setAuditable(AdminAuditable auditable) {
        this.auditable = auditable;
    }

    /**
     * The value within an SCField may require processing.   To allow for efficient caching within the client,
     * the post-processed value can be stored on the item itself.
     *
     * @return the processed value or null if no processed value exists for this item
     */
    @Override
    public String getProcessedValue() {
        return processedValue;
    }

    /**
     * The value within an SCField may require processing.   To allow for efficient caching within the client,
     * the post-processed value can be stored on the item itself.
     *
     * @param value              The post-processed value to be stored on the item.
     * @param clearExistingValue If set to true will null out the underlying value.   This makes since for most
     *                           implementations of this class because this data is read-only in most contexts and
     *                           clearing the existing value will allow it to be garbage collected.    Since SC items
     *                           can hold CLOB values this is an important memory concern.
     */
    @Override
    public void setProcessedValue(@Nonnull String value, boolean clearExistingValue) {
        this.processedValue = value;
        if (clearExistingValue) {
            setValue(null);
        }
    }


}

