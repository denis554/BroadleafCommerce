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

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.util.List;

/**
 * Created by jfischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FLD_ENUM")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
public class FieldEnumerationImpl implements FieldEnumeration {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FieldEnumerationId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "FieldEnumerationId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "FieldEnumerationImpl", allocationSize = 10)
    @Column(name = "FLD_ENUM_ID")
    protected Long id;

    @Column (name = "NAME")
    protected String name;

    @OneToMany(mappedBy = "fieldEnumeration", targetEntity = FieldEnumerationItemImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCMSElements")
    @OrderColumn(name = "FLD_ORDER")
    @BatchSize(size = 20)
    protected List<FieldEnumerationItem> enumerationItems;

    @Override
    public List<FieldEnumerationItem> getEnumerationItems() {
        return enumerationItems;
    }

    @Override
    public void setEnumerationItems(List<FieldEnumerationItem> enumerationItems) {
        this.enumerationItems = enumerationItems;
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
