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

package org.broadleafcommerce.openadmin.server.domain;

import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.openadmin.audit.AdminAuditable;
import org.broadleafcommerce.openadmin.audit.AdminAuditableListener;
import org.broadleafcommerce.common.presentation.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="BLC_SANDBOX_ITEM")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blSandBoxElements")
@AdminPresentationOverrides(
        {
            @AdminPresentationOverride(name="auditable.createdBy.id", value=@AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
            @AdminPresentationOverride(name="auditable.updatedBy.id", value=@AdminPresentation(readOnly = true, visibility = VisibilityEnum.HIDDEN_ALL)),
            @AdminPresentationOverride(name="auditable.dateCreated", value=@AdminPresentation(friendlyName = "SandBoxItemImpl_Date_Created", group = "SandBoxItemImpl_Audit", readOnly = true)),
            @AdminPresentationOverride(name="auditable.dateUpdated", value=@AdminPresentation(friendlyName = "SandBoxItemImpl_Date_Updated", group = "SandBoxItemImpl_Audit", readOnly = true)),
            @AdminPresentationOverride(name="sandBox.name", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="sandBox.author", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="sandBox.site", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="sandBox.sandboxType", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="originalSandBox.name", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="originalSandBox.author", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="originalSandBox.site", value=@AdminPresentation(excluded = true)),
            @AdminPresentationOverride(name="originalSandBox.sandboxType", value=@AdminPresentation(excluded = true))
        }
)
@EntityListeners(value = { AdminAuditableListener.class })
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "SandBoxItemImpl_baseSandBoxItem")
public class SandBoxItemImpl implements SandBoxItem {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "SandBoxItemId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "SandBoxItemId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "SandBoxItemImpl", allocationSize = 50)
    @Column(name = "SANDBOX_ITEM_ID")
    @AdminPresentation(visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @Embedded
    @AdminPresentation(excluded = true)
    protected AdminAuditable auditable = new AdminAuditable();

    @Column(name = "SANDBOX_ID")
    @Index(name="SANDBOX_ID_INDEX", columnNames={"SANDBOX_ID"})
	protected Long sandBoxId;

    @Column(name = "ORIG_SANDBOX_ID")
    @Index(name="ORIG_SANDBOX_ID_INDEX", columnNames={"ORIG_SANDBOX_ID"})
	protected Long originalSandBoxId;

    @Column(name = "SANDBOX_ITEM_TYPE")
    @AdminPresentation(friendlyName = "SandBoxItemImpl_Item_Type", order=4, group = "SandBoxItemImpl_Details", fieldType= SupportedFieldType.BROADLEAF_ENUMERATION, broadleafEnumeration="org.broadleafcommerce.openadmin.server.domain.SandBoxItemType")
    @Index(name="SANDBOX_ITEM_TYPE_INDEX", columnNames={"SANDBOX_ITEM_TYPE"})
    protected String sandBoxItemType;

    @Column(name = "SANDBOX_OPERATION_TYPE")
    @AdminPresentation(friendlyName = "SandBoxItemImpl_Operation_Type", order=4, group = "SandBoxItemImpl_Details", fieldType= SupportedFieldType.BROADLEAF_ENUMERATION, broadleafEnumeration="org.broadleafcommerce.openadmin.server.domain.SandBoxOperationType")
    protected String sandboxOperationType;

    @Column(name = "DESCRIPTION")
    @AdminPresentation(friendlyName = "SandBoxItemImpl_Description", order=1, group = "SandBoxItemImpl_Details")
    protected String description;

    @Column(name = "GRP_DESCRIPTION")
    @AdminPresentation(friendlyName = "SandBoxItemImpl_Grp_Description", order=2, group = "SandBoxItemImpl_Details", visibility = VisibilityEnum.GRID_HIDDEN)
    protected String groupDescription;

    @Column(name = "TEMPORARY_ITEM_ID")
    @AdminPresentation(excluded = true)
    @Index(name="TEMP_ITEM_INDEX", columnNames={"TEMPORARY_ITEM_ID"})
    protected Long temporaryItemId;

    @Column(name = "ORIGINAL_ITEM_ID")
    @AdminPresentation(excluded = true)
    @Index(name="SB_ORIG_ITEM_ID_INDEX", columnNames={"ORIGINAL_ITEM_ID"})
    protected Long originalItemId;

    @Column(name = "ARCHIVED_FLAG")
    @AdminPresentation(visibility = VisibilityEnum.HIDDEN_ALL)
    @Index(name="ARCHIVED_FLAG_INDEX", columnNames={"ARCHIVED_FLAG"})
    protected Character archivedFlag = 'N';

    @ManyToMany(targetEntity = SandBoxActionImpl.class, cascade = CascadeType.ALL)
    @JoinTable(
        name = "SANDBOX_ITEM_ACTION",
        joinColumns = {@JoinColumn(name ="SANDBOX_ITEM_ID", referencedColumnName = "SANDBOX_ITEM_ID")},
        inverseJoinColumns = {@JoinColumn(name = "SANDBOX_ACTION_ID", referencedColumnName = "SANDBOX_ACTION_ID")}
    )
    protected List<SandBoxAction> sandBoxActions;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

    @Override
	public Long getSandBoxId() {
		return sandBoxId;
	}

    @Override
	public void setSandBoxId(Long sandBoxId) {
		this.sandBoxId = sandBoxId;
	}

    @Override
	public Long getOriginalSandBoxId() {
		return originalSandBoxId;
	}

    @Override
	public void setOriginalSandBoxId(Long originalSandBoxId) {
		this.originalSandBoxId = originalSandBoxId;
	}

    @Override
    public SandBoxItemType getSandBoxItemType() {
        return SandBoxItemType.getInstance(sandBoxItemType);
    }

   @Override
    public void setSandBoxItemType(SandBoxItemType sandBoxItemType) {
        this.sandBoxItemType = sandBoxItemType.getType();
    }

    @Override
    public SandBoxOperationType getSandBoxOperationType() {
        return SandBoxOperationType.getInstance(sandboxOperationType);
    }


    @Override
    public void setSandBoxOperationType(SandBoxOperationType sandboxOperationType) {
        this.sandboxOperationType = sandboxOperationType.getType();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Long getOriginalItemId() {
        return originalItemId;
    }

    @Override
    public void setOriginalItemId(Long itemIdentifer) {
        this.originalItemId = itemIdentifer;
    }

    @Override
    public Long getTemporaryItemId() {
        return temporaryItemId;
    }

    @Override
    public void setTemporaryItemId(Long temporaryItemId) {
        this.temporaryItemId = temporaryItemId;
    }

    @Override
    public List<SandBoxAction> getSandBoxActions() {
        return sandBoxActions;
    }

    @Override
    public void setSandBoxActions(List<SandBoxAction> actionList) {
        this.sandBoxActions = actionList;
    }

    @Override
    public Boolean getArchivedFlag() {
        if (archivedFlag == null) {
            return null;
        } else {
            return archivedFlag == 'Y' ? Boolean.TRUE : Boolean.FALSE;
        }
    }                             

    @Override
    public void setArchivedFlag(Boolean archivedFlag) {
        if (archivedFlag == null) {
            this.archivedFlag = null;
        } else {
            this.archivedFlag = archivedFlag ? 'Y' : 'N';
        }
    }

    @Override
    public void addSandBoxAction(SandBoxAction action) {
        if (sandBoxActions == null) {
            sandBoxActions = new ArrayList<SandBoxAction>();
        }
        sandBoxActions.add(action);
    }

    @Override
    public String getGroupDescription() {
        return groupDescription;
    }

    @Override
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    @Override
    public AdminAuditable getAuditable() {
        return auditable;
    }

    @Override
    public void setAuditable(AdminAuditable auditable) {
        this.auditable = auditable;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SandBoxItemImpl other = (SandBoxItemImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
