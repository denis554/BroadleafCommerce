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

package org.broadleafcommerce.openadmin.audit;

import org.broadleafcommerce.openadmin.server.security.domain.AdminUser;
import org.broadleafcommerce.openadmin.server.security.domain.AdminUserImpl;
import org.broadleafcommerce.common.presentation.AdminPresentation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class AdminAuditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "DATE_CREATED", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @AdminPresentation(friendlyName="Date Created", group="Audit", readOnly = true)
    protected Date dateCreated;

    @ManyToOne(targetEntity = AdminUserImpl.class)
    @JoinColumn(name = "CREATED_BY", updatable = false)
    protected AdminUser createdBy;

    @Column(name = "DATE_UPDATED")
    @Temporal(TemporalType.TIMESTAMP)
    @AdminPresentation(friendlyName="Date Updated", group="Audit", readOnly = true)
    protected Date dateUpdated;

    @ManyToOne(targetEntity = AdminUserImpl.class)
    @JoinColumn(name = "UPDATED_BY")
    protected AdminUser updatedBy;

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public AdminUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AdminUser createdBy) {
        this.createdBy = createdBy;
    }

    public AdminUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(AdminUser updatedBy) {
        this.updatedBy = updatedBy;
    }
}
