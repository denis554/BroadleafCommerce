/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.offer.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferAdminPresentation;
import org.broadleafcommerce.core.offer.service.type.OfferItemRestrictionRuleType;
import org.broadleafcommerce.openadmin.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.dto.CriteriaTransferObject;
import org.broadleafcommerce.openadmin.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.dto.Property;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.InspectHelper;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Jon on 11/23/15.
 */
@Component("blOfferCustomPersistenceHandler")
public class OfferCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(OfferCustomPersistenceHandler.class);

    protected static final String SHOW_ADVANCED_VISIBILITY_OPTIONS = "showAdvancedVisibilityOptions";
    protected static final String QUALIFIERS_CAN_BE_QUALIFIERS = "qualifiersCanBeQualifiers";
    protected static final String QUALIFIERS_CAN_BE_TARGETS = "qualifiersCanBeTargets";
    protected static final String OFFER_ITEM_QUALIFIER_RULE_TYPE = "offerItemQualifierRuleType";

    private Boolean isAssignableFromOffer(PersistencePackage persistencePackage) {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        return Offer.class.getName().equals(ceilingEntityFullyQualifiedClassname);
    }

    @Override
    public Boolean canHandleInspect(PersistencePackage persistencePackage) {
       return isAssignableFromOffer(persistencePackage);
    }

    @Override
    public Boolean canHandleFetch(PersistencePackage persistencePackage) {
        return isAssignableFromOffer(persistencePackage);
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        return isAssignableFromOffer(persistencePackage);
    }

    @Override
    public DynamicResultSet inspect(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, InspectHelper helper) throws ServiceException {
        Map<String, FieldMetadata> md = getMetadata(persistencePackage, helper);

        PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
        Map<MergedPropertyType, Map<String, FieldMetadata>> allMergedProperties = new HashMap<MergedPropertyType, Map<String, FieldMetadata>>();

        //retrieve the default properties for WorkflowEvents
        Map<String, FieldMetadata> properties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePerspective);

        properties.put(SHOW_ADVANCED_VISIBILITY_OPTIONS, buildAdvancedVisibilityOptionsFieldMetaData());
        properties.put(QUALIFIERS_CAN_BE_QUALIFIERS, buildQualifiersCanBeQualifiersFieldMetaData());
        properties.put(QUALIFIERS_CAN_BE_TARGETS, buildQualifiersCanBeTargetsFieldMetaData());

        allMergedProperties.put(MergedPropertyType.PRIMARY, properties);
        Class<?>[] entityClasses = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(Offer.class);
        ClassMetadata mergedMetadata = helper.buildClassMetadata(entityClasses, persistencePackage, allMergedProperties);

        return new DynamicResultSet(mergedMetadata, null, null);
    }

    protected FieldMetadata buildAdvancedVisibilityOptionsFieldMetaData() {
        BasicFieldMetadata advancedLabelMetadata = new BasicFieldMetadata();
        advancedLabelMetadata.setFieldType(SupportedFieldType.BOOLEAN_LINK);
        advancedLabelMetadata.setForeignKeyCollection(false);
        advancedLabelMetadata.setMergedPropertyType(MergedPropertyType.PRIMARY);
        advancedLabelMetadata.setName("test");
        advancedLabelMetadata.setFriendlyName("OfferImpl_View_Visibility_Options");
        advancedLabelMetadata.setGroup(OfferAdminPresentation.GroupName.ActivityRange);
        advancedLabelMetadata.setOrder(5000);
        advancedLabelMetadata.setDefaultValue("true");
        return advancedLabelMetadata;
    }

    protected FieldMetadata buildQualifiersCanBeQualifiersFieldMetaData() {
        BasicFieldMetadata qualifiersCanBeQualifiers = new BasicFieldMetadata();
        qualifiersCanBeQualifiers.setFieldType(SupportedFieldType.BOOLEAN);
        qualifiersCanBeQualifiers.setName(QUALIFIERS_CAN_BE_QUALIFIERS);
        qualifiersCanBeQualifiers.setFriendlyName("OfferImpl_Qualifiers_Can_Be_Qualifiers");
        qualifiersCanBeQualifiers.setGroup(OfferAdminPresentation.GroupName.QualifierRuleRestriction);
        qualifiersCanBeQualifiers.setOrder(OfferAdminPresentation.FieldOrder.QualifiersCanBeQualifiers);
        qualifiersCanBeQualifiers.setDefaultValue("false");
        return qualifiersCanBeQualifiers;
    }

    protected FieldMetadata buildQualifiersCanBeTargetsFieldMetaData() {
        BasicFieldMetadata qualifiersCanBeTargets = new BasicFieldMetadata();
        qualifiersCanBeTargets.setFieldType(SupportedFieldType.BOOLEAN);
        qualifiersCanBeTargets.setName(QUALIFIERS_CAN_BE_TARGETS);
        qualifiersCanBeTargets.setFriendlyName("OfferImpl_Qualifiers_Can_Be_Targets");
        qualifiersCanBeTargets.setGroup(OfferAdminPresentation.GroupName.QualifierRuleRestriction);
        qualifiersCanBeTargets.setOrder(OfferAdminPresentation.FieldOrder.QualifiersCanBeTargets);
        qualifiersCanBeTargets.setDefaultValue("false");
        return qualifiersCanBeTargets;
    }

    @Override
    public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        DynamicResultSet resultSet = helper.getCompatibleModule(OperationType.BASIC).fetch(persistencePackage, cto);
        String customCriteria = "";
        if (persistencePackage.getCustomCriteria().length > 0) {
            customCriteria = persistencePackage.getCustomCriteria()[0];
        }

        for (Entity entity : resultSet.getRecords()) {
            Property discountType = entity.findProperty("discountType");
            Property discountValue = entity.findProperty("value");

            String value = discountValue.getValue();
            if (discountType.getValue().equals("PERCENT_OFF")) {
                value = value.indexOf(".") < 0 ? value : value.replaceAll("0*$", "").replaceAll("\\.$", "");
                discountValue.setValue(value + "%");
            } else if (discountType.getValue().equals("AMOUNT_OFF")) {
                NumberFormat nf = NumberFormat.getCurrencyInstance();
                discountValue.setValue(nf.format(new BigDecimal(value)));
            } else if (discountType.getValue().equals("")) {
                discountValue.setValue("");
            }

            Property timeRule = entity.findProperty("offerMatchRules---TIME");
            entity.addProperty(buildAdvancedVisibilityOptionsProperty(timeRule));

            Property offerItemQualifierRuleType = entity.findProperty("offerItemQualifierRuleType");
            entity.addProperty(buildQualifiersCanBeQualifiersProperty(offerItemQualifierRuleType));
            entity.addProperty(buildQualifiersCanBeTargetsProperty(offerItemQualifierRuleType));

            if (!"listGridView".equals(customCriteria)) {
                String setValue = discountValue.getValue();
                setValue = setValue.replaceAll("\\%", "").replaceAll("\\$", "");
                discountValue.setValue(setValue);
            }
        }
        return resultSet;
    }

    protected Property buildAdvancedVisibilityOptionsProperty(Property timeRule) {
        Property advancedLabel = new Property();
        advancedLabel.setName(SHOW_ADVANCED_VISIBILITY_OPTIONS);
        advancedLabel.setValue((timeRule.getValue() == null) ? "true" : "false");
        return advancedLabel;
    }

    protected Property buildQualifiersCanBeQualifiersProperty(Property offerItemQualifierRuleType) {
        boolean qualifiersCanBeQualifiers = isQualifierType(offerItemQualifierRuleType) || isQualifierTargetType(offerItemQualifierRuleType);

        Property property = new Property();
        property.setName(QUALIFIERS_CAN_BE_QUALIFIERS);
        property.setValue(String.valueOf(qualifiersCanBeQualifiers));
        return property;
    }

    protected Property buildQualifiersCanBeTargetsProperty(Property offerItemQualifierRuleType) {
        boolean qualifiersCanBeTargets = isTargetType(offerItemQualifierRuleType) || isQualifierTargetType(offerItemQualifierRuleType);

        Property property = new Property();
        property.setName(QUALIFIERS_CAN_BE_TARGETS);
        property.setValue(String.valueOf(qualifiersCanBeTargets));
        return property;
    }

    protected boolean isQualifierType(Property offerItemQualifierRuleType) {
        return offerItemQualifierRuleType != null && Objects.equals(offerItemQualifierRuleType.getValue(), OfferItemRestrictionRuleType.QUALIFIER.getType());
    }

    protected boolean isTargetType(Property offerItemQualifierRuleType) {
        return offerItemQualifierRuleType != null && Objects.equals(offerItemQualifierRuleType.getValue(), OfferItemRestrictionRuleType.TARGET.getType());
    }

    protected boolean isQualifierTargetType(Property offerItemQualifierRuleType) {
        return offerItemQualifierRuleType != null && Objects.equals(offerItemQualifierRuleType.getValue(), OfferItemRestrictionRuleType.QUALIFIER_TARGET.getType());
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();

        Property qualifiersCanBeQualifiers = entity.findProperty(QUALIFIERS_CAN_BE_QUALIFIERS);
        Property qualifiersCanBeTargets = entity.findProperty(QUALIFIERS_CAN_BE_TARGETS);
        Property offerItemQualifierRuleType = buildOfferItemQualifierRuleTypeProperty(qualifiersCanBeQualifiers, qualifiersCanBeTargets);

        entity.addProperty(offerItemQualifierRuleType);
        entity.removeProperty(QUALIFIERS_CAN_BE_QUALIFIERS);
        entity.removeProperty(QUALIFIERS_CAN_BE_TARGETS);

        OperationType updateType = persistencePackage.getPersistencePerspective().getOperationTypes().getUpdateType();
        return helper.getCompatibleModule(updateType).update(persistencePackage);
    }

    protected Property buildOfferItemQualifierRuleTypeProperty(Property qualifiersCanBeQualifiers, Property qualifiersCanBeTargets) {
        String offerItemQualifierRuleType;
        boolean canBeQualifiers = qualifiersCanBeQualifiers == null ? false : Boolean.parseBoolean(qualifiersCanBeQualifiers.getValue());
        boolean canBeTargets = qualifiersCanBeTargets == null ? false : Boolean.parseBoolean(qualifiersCanBeTargets.getValue());

        if (canBeTargets && canBeQualifiers) {
            offerItemQualifierRuleType = OfferItemRestrictionRuleType.QUALIFIER_TARGET.getType();
        } else if (canBeTargets) {
            offerItemQualifierRuleType = OfferItemRestrictionRuleType.TARGET.getType();
        } else if (canBeQualifiers){
            offerItemQualifierRuleType = OfferItemRestrictionRuleType.QUALIFIER.getType();
        } else {
            offerItemQualifierRuleType = OfferItemRestrictionRuleType.NONE.getType();
        }

        Property property = new Property();
        property.setName(OFFER_ITEM_QUALIFIER_RULE_TYPE);
        property.setValue(offerItemQualifierRuleType);
        return property;
    }
}
