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
package org.broadleafcommerce.profile.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_PHONE")
public class PhoneImpl implements Phone {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "PhoneId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "PhoneId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "PhoneImpl", allocationSize = 1)
    @Column(name = "PHONE_ID")
    protected Long id;

    @Column(name = "PHONE_NUMBER")
    protected String phoneNumber;

    @Column(name = "IS_DEFAULT")
    protected boolean isDefault = false;

    @Column(name = "IS_ACTIVE")
    protected boolean isActive = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isActive ? 1231 : 1237);
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhoneImpl other = (PhoneImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (isActive != other.isActive)
            return false;
        if (isDefault != other.isDefault)
            return false;
        if (phoneNumber == null) {
            if (other.phoneNumber != null)
                return false;
        } else if (!phoneNumber.equals(other.phoneNumber))
            return false;
        return true;
    }
}
