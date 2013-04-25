/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.cms.field.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Created by jfischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FLD_ENUM_ITEM")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
public class FieldEnumerationItemImpl implements FieldEnumerationItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FieldEnumerationItemId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "FieldEnumerationItemId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "FieldEnumerationItemImpl", allocationSize = 10)
    @Column(name = "FLD_ENUM_ITEM_ID")
    protected Long id;

    @Column (name = "NAME")
    protected String name;

    @Column (name = "FRIENDLY_NAME")
    protected String friendlyName;

    @Column(name="FLD_ORDER")
    protected int fieldOrder;

    @ManyToOne(targetEntity = FieldEnumerationImpl.class)
    @JoinColumn(name = "FLD_ENUM_ID")
    protected FieldEnumeration fieldEnumeration;

    @Override
    public FieldEnumeration getFieldEnumeration() {
        return fieldEnumeration;
    }

    @Override
    public void setFieldEnumeration(FieldEnumeration fieldEnumeration) {
        this.fieldEnumeration = fieldEnumeration;
    }

    @Override
    public int getFieldOrder() {
        return fieldOrder;
    }

    @Override
    public void setFieldOrder(int fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
