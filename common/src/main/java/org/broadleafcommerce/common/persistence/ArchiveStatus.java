/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.common.persistence;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.sandbox.SandBoxNonProductionSkip;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Jeff Fischer
 */
@Embeddable
public class ArchiveStatus implements Serializable, SandBoxNonProductionSkip {

    @Column(name = "ARCHIVED")
    @AdminPresentation(friendlyName = "archived", visibility = VisibilityEnum.HIDDEN_ALL, group = "ArchiveStatus")
    protected Character archived = 'N';

    public Character getArchived() {
        return archived;
    }

    public void setArchived(Character archived) {
        this.archived = archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!getClass().isAssignableFrom(o.getClass())) return false;

        ArchiveStatus that = (ArchiveStatus) o;

        if (archived != null ? !archived.equals(that.archived) : that.archived != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return archived != null ? archived.hashCode() : 0;
    }
}
