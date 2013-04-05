package org.broadleafcommerce.openadmin.server.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * @author Jeff Fischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_CUSTOM_FIELD")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
@AdminPresentationClass(friendlyName = "CustomFieldImpl_baseCustomField")
public class CustomFieldImpl implements CustomField {

    private static final Log LOG = LogFactory.getLog(CustomFieldImpl.class);
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "CustomFieldId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "CustomFieldId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL",
            pkColumnValue = "CustomFieldImpl", allocationSize = 50)
    @Column(name = "CUSTOM_FIELD_ID")
    protected Long id;

    @Column(name = "CUSTOM_FIELD_TARGET", nullable=false)
    @AdminPresentation(friendlyName = "CustomFieldImpl_Custom_Field_Target", order = 1, prominent = true, gridOrder = 2,
            fieldType = SupportedFieldType.BROADLEAF_ENUMERATION,
            broadleafEnumeration = "org.broadleafcommerce.openadmin.server.service.type.CustomFieldTargetType")
    protected String customFieldTarget;

    @Column(name = "LABEL", nullable=false)
    @AdminPresentation(friendlyName = "CustomFieldImpl_Label", order = 2, prominent = true, gridOrder = 1)
    protected String label;

    @Column(name = "CUSTOM_FIELD_TYPE", nullable=false)
    @AdminPresentation(friendlyName = "CustomFieldImpl_Custom_Field_Type", order = 3, prominent = true, gridOrder = 3,
            fieldType = SupportedFieldType.BROADLEAF_ENUMERATION,
            broadleafEnumeration = "org.broadleafcommerce.openadmin.server.service.type.CustomFieldType")
    protected String customFieldType;

    @Column(name = "SHOW_FIELD_ON_FORM")
    @AdminPresentation(friendlyName = "CustomFieldImpl_Show_Field_On_Form", order = 4)
    protected Boolean showFieldOnForm = true;

    @Column(name = "SHOW_FIELD_IN_RULE")
    @AdminPresentation(friendlyName = "CustomFieldImpl_Show_Field_In_Rule", order = 5)
    protected Boolean showFieldInRuleBuilder = true;

    @Column(name = "GROUP_NAME")
    @AdminPresentation(friendlyName = "CustomFieldImpl_Group_Name", order = 6)
    protected String groupName;

    @Column(name = "GRID_ORDER")
    @AdminPresentation(friendlyName = "CustomFieldImpl_Grid_Order", order = 7)
    protected Integer gridOrder;

    @Override
    public String getCustomFieldTarget() {
        return customFieldTarget;
    }

    @Override
    public void setCustomFieldTarget(String customFieldTarget) {
        this.customFieldTarget = customFieldTarget;
    }

    @Override
    public String getCustomFieldType() {
        return customFieldType;
    }

    @Override
    public void setCustomFieldType(String customFieldType) {
        this.customFieldType = customFieldType;
    }

    @Override
    public Integer getGridOrder() {
        return gridOrder;
    }

    @Override
    public void setGridOrder(Integer gridOrder) {
        this.gridOrder = gridOrder;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Boolean getShowFieldInRuleBuilder() {
        return showFieldInRuleBuilder;
    }

    @Override
    public void setShowFieldInRuleBuilder(Boolean showFieldInRuleBuilder) {
        this.showFieldInRuleBuilder = showFieldInRuleBuilder;
    }

    @Override
    public Boolean getShowFieldOnForm() {
        return showFieldOnForm;
    }

    @Override
    public void setShowFieldOnForm(Boolean showFieldOnForm) {
        this.showFieldOnForm = showFieldOnForm;
    }
}
