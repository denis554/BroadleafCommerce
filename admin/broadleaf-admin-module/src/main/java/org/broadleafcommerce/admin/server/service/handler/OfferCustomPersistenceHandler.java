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

package org.broadleafcommerce.admin.server.service.handler;

import com.anasoft.os.daofusion.criteria.AssociationPath;
import com.anasoft.os.daofusion.criteria.FilterCriterion;
import com.anasoft.os.daofusion.criteria.NestedPropertyCriteria;
import com.anasoft.os.daofusion.criteria.PersistentEntityCriteria;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider;
import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.anasoft.os.daofusion.cto.client.FilterAndSortCriteria;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.admin.client.datasource.EntityImplementations;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferCode;
import org.broadleafcommerce.core.offer.domain.OfferCodeImpl;
import org.broadleafcommerce.core.offer.domain.OfferItemCriteria;
import org.broadleafcommerce.core.offer.domain.OfferRule;
import org.broadleafcommerce.core.offer.service.type.OfferRuleType;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ForeignKey;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.server.cto.BaseCtoConverter;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.InspectHelper;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.broadleafcommerce.openadmin.web.rulebuilder.DataDTODeserializer;
import org.broadleafcommerce.openadmin.web.rulebuilder.DataDTOToMVELTranslator;
import org.broadleafcommerce.openadmin.web.rulebuilder.MVELToDataWrapperTranslator;
import org.broadleafcommerce.openadmin.web.rulebuilder.MVELTranslationException;
import org.broadleafcommerce.openadmin.web.rulebuilder.dto.DataDTO;
import org.broadleafcommerce.openadmin.web.rulebuilder.dto.DataWrapper;
import org.broadleafcommerce.openadmin.web.rulebuilder.service.RuleBuilderFieldServiceFactory;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.tool.hbm2x.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author jfischer
 *
 */
public class OfferCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    public static final String IDENTITYCRITERIA = "Offer";
    private static final Log LOG = LogFactory.getLog(OfferCustomPersistenceHandler.class);
    
    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Resource(name = "blRuleBuilderFieldServiceFactory")
    protected RuleBuilderFieldServiceFactory ruleBuilderFieldServiceFactory;

    @Override
    public Boolean canHandleInspect(PersistencePackage persistencePackage) {
        String[] customCriteria = persistencePackage.getCustomCriteria();
        boolean canHandle = false;
        if (customCriteria != null) {
            for (String criteria : customCriteria) {
                if (criteria != null && criteria.equals(IDENTITYCRITERIA)) {
                    canHandle = true;
                    break;
                }
            }
        }
        return canHandle;
    }

    @Override
    public Boolean canHandleFetch(PersistencePackage persistencePackage) {
        return canHandleInspect(persistencePackage);
    }

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        return canHandleInspect(persistencePackage);
    }

    @Override
    public Boolean canHandleRemove(PersistencePackage persistencePackage) {
        return canHandleInspect(persistencePackage);
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        return canHandleInspect(persistencePackage);
    }

    @Override
    public DynamicResultSet inspect(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, InspectHelper helper) throws ServiceException {
        try {
            Map<MergedPropertyType, Map<String, FieldMetadata>> allMergedProperties = new HashMap<MergedPropertyType, Map<String, FieldMetadata>>();
            Map<String, FieldMetadata> mergedProperties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePackage.getPersistencePerspective());
            allMergedProperties.put(MergedPropertyType.PRIMARY, mergedProperties);
            /*
             * Add a fake property to hold the fulfillment group rules. This property is the same type as appliesToOrderRules
             */
            mergedProperties.put("appliesToFulfillmentGroupRules", mergedProperties.get("appliesToOrderRules"));
            
            PersistencePerspective offerCodePersistencePerspective = new PersistencePerspective(null, new String[]{}, new ForeignKey[]{new ForeignKey("offer", EntityImplementations.OFFER, null)});
            Map<String, FieldMetadata> offerCodeMergedProperties = helper.getSimpleMergedProperties(OfferCode.class.getName(), offerCodePersistencePerspective);
            BasicFieldMetadata metadata = (BasicFieldMetadata) offerCodeMergedProperties.get("offerCode");
            metadata.setVisibility(VisibilityEnum.HIDDEN_ALL);
            mergedProperties.put("offerCode.offerCode", metadata);
            BasicFieldMetadata metadata2 = (BasicFieldMetadata) offerCodeMergedProperties.get("id");
            metadata2.setVisibility(VisibilityEnum.HIDDEN_ALL);
            mergedProperties.put("offerCode.id", metadata2);

            Class<?>[] entityClasses = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(Offer.class);
            ClassMetadata mergedMetadata = helper.getMergedClassMetadata(entityClasses, allMergedProperties);
            
            DynamicResultSet results = new DynamicResultSet(mergedMetadata, null, null);
            
            return results;
        } catch (Exception e) {
            ServiceException ex = new ServiceException("Unable to retrieve inspection results for " + persistencePackage.getCeilingEntityFullyQualifiedClassname(), e);
            LOG.error("Unable to retrieve inspection results for " + persistencePackage.getCeilingEntityFullyQualifiedClassname(), ex);
            throw ex;
        }
    }

    @Override
    public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<String, FieldMetadata> offerProperties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePerspective);
            BaseCtoConverter ctoConverter = helper.getCtoConverter(persistencePerspective, cto, Offer.class.getName(), offerProperties);
            PersistentEntityCriteria queryCriteria = ctoConverter.convert(cto, Offer.class.getName());
            
            //If necessary, filter out the archived Offers
            if (!persistencePackage.getPersistencePerspective().getShowArchivedFields()) {
                SimpleFilterCriterionProvider criterionProvider = new  SimpleFilterCriterionProvider(SimpleFilterCriterionProvider.FilterDataStrategy.NONE, 0) {
                    @Override
                    public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                        return Restrictions.or(Restrictions.eq(targetPropertyName, 'N'), Restrictions.isNull(targetPropertyName));
                    }
                };
                FilterCriterion filterCriterion = new FilterCriterion(AssociationPath.ROOT, "archiveStatus.archived", criterionProvider);
                ((NestedPropertyCriteria) queryCriteria).add(filterCriterion);
            }
            
            List<Serializable> records = dynamicEntityDao.query(queryCriteria, Offer.class);
            Entity[] entities = helper.getRecords(offerProperties, records, null, null);
            MVELToDataWrapperTranslator translator = new MVELToDataWrapperTranslator();
            ObjectMapper mapper = new ObjectMapper();

            //populate the rules from the new map associated with Offer
            for (int j=0;j<entities.length;j++) {
                Offer offer = (Offer) records.get(j);
                OfferRule orderRule = offer.getOfferMatchRules().get(OfferRuleType.ORDER.getType());
                if (orderRule != null) {
                    entities[j].findProperty("appliesToOrderRules").setValue(orderRule.getMatchRule());

                    //**** Admin 3.0 ****
                    //Convert the MVEL into JSON and place as a new property on the entity: "appliesToOrderRulesJson"
                    convertMatchRuleToJson(entities[j], translator, mapper, orderRule,
                            "appliesToOrderRulesJson","ORDER_FIELDS");
                }
                //**** Admin 3.0 ****
                //Add the RuleBuilderFieldService property on the entity: "appliesToOrderRulesFieldService"
                Property appliesToOrderRulesFieldService = new Property();
                appliesToOrderRulesFieldService.setName("appliesToOrderRulesFieldService");
                appliesToOrderRulesFieldService.setValue("ORDER_FIELDS");
                entities[j].addProperty(appliesToOrderRulesFieldService);

                OfferRule customerRule = offer.getOfferMatchRules().get(OfferRuleType.CUSTOMER.getType());
                if (customerRule != null) {
                    entities[j].findProperty("appliesToCustomerRules").setValue(customerRule.getMatchRule());

                    //**** Admin 3.0 ****
                    //Convert the MVEL into JSON and place as a new property on the entity: "appliesToCustomerRulesJson"
                    convertMatchRuleToJson(entities[j], translator, mapper, customerRule,
                            "appliesToCustomerRulesJson","CUSTOMER_FIELDS");
                }
                //**** Admin 3.0 ****
                //Add the RuleBuilderFieldService property on the entity: "appliesToCustomerRulesFieldService"
                Property appliesToCustomerRulesFieldService = new Property();
                appliesToCustomerRulesFieldService.setName("appliesToCustomerRulesFieldService");
                appliesToCustomerRulesFieldService.setValue("CUSTOMER_FIELDS");
                entities[j].addProperty(appliesToCustomerRulesFieldService);

                OfferRule fgRule = offer.getOfferMatchRules().get(OfferRuleType.FULFILLMENT_GROUP.getType());
                if (fgRule != null) {
                    Property prop = new Property();
                    prop.setName("appliesToFulfillmentGroupRules");
                    prop.setValue(fgRule.getMatchRule());
                    entities[j].addProperty(prop);

                    //**** Admin 3.0 ****
                    //Convert the MVEL into JSON and place as a new property on the entity:
                    // "appliesToFulfillmentGroupRulesJson"
                    convertMatchRuleToJson(entities[j], translator, mapper, fgRule,
                            "appliesToFulfillmentGroupRulesJson","FULFILLMENT_GROUP_FIELDS");
                }
                //**** Admin 3.0 ****
                //Add the RuleBuilderFieldService property on the entity: "appliesToFulfillmentGroupRulesFieldService"
                Property appliesToFulfillmentGroupRulesFieldService = new Property();
                appliesToFulfillmentGroupRulesFieldService.setName("appliesToFulfillmentGroupRulesFieldService");
                appliesToFulfillmentGroupRulesFieldService.setValue("FULFILLMENT_GROUP_FIELDS");
                entities[j].addProperty(appliesToFulfillmentGroupRulesFieldService);

                //**** Admin 3.0 ****
                //Convert the MVEL into JSON and place as a new property on the entity: "targetItemCriteriaJson"
                //Add the RuleBuilderFieldService property on the entity: "targetItemCriteriaFieldService"
                convertItemCriteriaToJson(entities[j], translator, mapper, offer.getTargetItemCriteria(),
                        "targetItemCriteriaJson", "targetItemCriteriaFieldService");

                //**** Admin 3.0 ****
                //Convert the MVEL into JSON and place as a new property on the entity: "targetItemCriteriaJson"
                //Add the RuleBuilderFieldService property on the entity: "targetItemCriteriaFieldService"
                convertItemCriteriaToJson(entities[j], translator, mapper, offer.getQualifyingItemCriteria(),
                        "qualifyingItemCriteriaJson", "qualifyingItemCriteriaFieldService");
            }
            
            PersistencePerspective offerCodePersistencePerspective = new PersistencePerspective(null, new String[]{}, new ForeignKey[]{new ForeignKey("offer", EntityImplementations.OFFER, null)});
            Map<String, FieldMetadata> offerCodeMergedProperties = helper.getSimpleMergedProperties(OfferCode.class.getName(), offerCodePersistencePerspective);
            for (Entity record : entities) {
                CriteriaTransferObject offerCodeCto = new CriteriaTransferObject();
                FilterAndSortCriteria filterCriteria = offerCodeCto.get("offer");
                filterCriteria.setFilterValue(record.findProperty("id").getValue());
                BaseCtoConverter offerCodeCtoConverter = helper.getCtoConverter(offerCodePersistencePerspective, offerCodeCto, OfferCode.class.getName(), offerCodeMergedProperties);
                
                PersistentEntityCriteria offerCodeQueryCriteria = offerCodeCtoConverter.convert(offerCodeCto, OfferCode.class.getName());
                List<Serializable> offerCodes = dynamicEntityDao.query(offerCodeQueryCriteria, OfferCode.class);
                Entity[] offerCodeEntities = helper.getRecords(offerCodeMergedProperties, offerCodes, null, null);
                
                if (offerCodeEntities.length > 0) {
                    Entity temp = new Entity();
                    temp.setType(offerCodeEntities[0].getType());
                    temp.setProperties(new Property[] {offerCodeEntities[0].findProperty("offerCode"), offerCodeEntities[0].findProperty("id")});
                    record.mergeProperties("offerCode", temp);
                }
            }
            
            int totalRecords = helper.getTotalRecords(persistencePackage, cto, ctoConverter);
            
            DynamicResultSet response = new DynamicResultSet(null, entities, totalRecords);
            
            return response;
        } catch (Exception e) {
            LOG.error("Unable to perform fetch for entity" + persistencePackage.getCeilingEntityFullyQualifiedClassname(), e);
            throw new ServiceException("Unable to perform fetch for entity: "+ceilingEntityFullyQualifiedClassname, e);
        }
    }

    protected void convertItemCriteriaToJson(Entity entity, MVELToDataWrapperTranslator translator, ObjectMapper mapper,
                    Set<OfferItemCriteria> offerItemCriteria, String jsonProp, String fieldServiceProp)
            throws MVELTranslationException, IOException {

        int k=0;
        Entity[] targetItemCriterias = new Entity[offerItemCriteria.size()];
        for (OfferItemCriteria oic : offerItemCriteria) {
            Property[] properties = new Property[3];
            Property mvelProperty = new Property();
            mvelProperty.setName("orderItemMatchRule");
            mvelProperty.setValue(oic.getMatchRule());
            Property quantityProperty = new Property();
            quantityProperty.setName("quantity");
            quantityProperty.setValue(oic.getQuantity().toString());
            Property idProperty = new Property();
            idProperty.setName("id");
            idProperty.setValue(oic.getId().toString());
            properties[0] = mvelProperty;
            properties[1] = quantityProperty;
            properties[2] = idProperty;
            Entity criteria = new Entity();
            criteria.setProperties(properties);
            targetItemCriterias[k] = criteria;
            k++;
        }

        DataWrapper oiWrapper = translator.createRuleData(targetItemCriterias, "orderItemMatchRule", "quantity", "id",
                ruleBuilderFieldServiceFactory.createInstance("ORDER_ITEM_FIELDS"));
        String json = mapper.writeValueAsString(oiWrapper);
        Property jsonP = new Property();
        jsonP.setName(jsonProp);
        jsonP.setValue(json);
        entity.addProperty(jsonP);
        Property fieldServiceP = new Property();
        fieldServiceP.setName(fieldServiceProp);
        fieldServiceP.setValue("ORDER_ITEM_FIELDS");
        entity.addProperty(fieldServiceP);
    }

    protected void convertMatchRuleToJson(Entity entity, MVELToDataWrapperTranslator translator, ObjectMapper mapper,
                    OfferRule offerRule, String jsonProp, String fieldService)
        throws MVELTranslationException, IOException {
        Entity[] matchCriteria = new Entity[1];
        Property[] properties = new Property[1];
        Property mvelProperty = new Property();
        mvelProperty.setName("matchRule");
        mvelProperty.setValue(offerRule.getMatchRule());
        properties[0] = mvelProperty;
        Entity criteria = new Entity();
        criteria.setProperties(properties);
        matchCriteria[0] = criteria;

        DataWrapper orderWrapper = translator.createRuleData(matchCriteria, "matchRule", null, null,
                ruleBuilderFieldServiceFactory.createInstance(fieldService));
        String json = mapper.writeValueAsString(orderWrapper);
        Property p = new Property();
        p.setName(jsonProp);
        p.setValue(json);
        entity.addProperty(p);
    }

    protected void removeHTMLEncoding(Entity entity) {
        Property prop = entity.findProperty("targetItemCriteria.orderItemMatchRule");
        if (prop != null && prop.getValue() != null) {
            //antisamy XSS protection encodes the values in the MVEL
            //reverse this behavior
            prop.setValue(prop.getRawValue());
        }
        prop = entity.findProperty("appliesToCustomerRules");
        if (prop != null && prop.getValue() != null) {
            //antisamy XSS protection encodes the values in the MVEL
            //reverse this behavior
            prop.setValue(prop.getRawValue());
        }
        prop = entity.findProperty("appliesToOrderRules");
        if (prop != null && prop.getValue() != null) {
            //antisamy XSS protection encodes the values in the MVEL
            //reverse this behavior
            prop.setValue(prop.getRawValue());
        }
        prop = entity.findProperty("appliesToFulfillmentGroupRules");
        if (prop != null && prop.getValue() != null) {
            //antisamy XSS protection encodes the values in the MVEL
            //reverse this behavior
            prop.setValue(prop.getRawValue());
        }
    }

    @Override
    public Entity add(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        removeHTMLEncoding(entity);
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Offer offerInstance = (Offer) Class.forName(entity.getType()[0]).newInstance();
            Map<String, FieldMetadata> offerProperties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePerspective);
            offerInstance = (Offer) helper.createPopulatedInstance(offerInstance, entity, offerProperties, false);

            addRule(entity, offerInstance, "appliesToOrderRules", OfferRuleType.ORDER);
            addRule(entity, offerInstance, "appliesToCustomerRules", OfferRuleType.CUSTOMER);
            addRule(entity, offerInstance, "appliesToFulfillmentGroupRules", OfferRuleType.FULFILLMENT_GROUP);
            
            dynamicEntityDao.persist(offerInstance);
            
            OfferCode offerCode = null;
            if (entity.findProperty("deliveryType").getValue().equals("CODE")) {
                offerCode = (OfferCode) entityConfiguration.createEntityInstance(OfferCode.class.getName());
                offerCode.setOfferCode(entity.findProperty("offerCode.offerCode").getValue());
                offerCode.setEndDate(offerInstance.getEndDate());
                offerCode.setMaxUses(offerInstance.getMaxUses());
                offerCode.setOffer(offerInstance);
                offerCode.setStartDate(offerInstance.getStartDate());
                offerCode = (OfferCode) dynamicEntityDao.merge(offerCode);
            }
            
            Entity offerEntity = helper.getRecord(offerProperties, offerInstance, null, null);
            if (offerCode != null) {
                PersistencePerspective offerCodePersistencePerspective = new PersistencePerspective(null, new String[]{}, new ForeignKey[]{new ForeignKey("offer", EntityImplementations.OFFER, null)});
                Map<String, FieldMetadata> offerCodeMergedProperties = helper.getSimpleMergedProperties(OfferCode.class.getName(), offerCodePersistencePerspective);
                Entity offerCodeEntity = helper.getRecord(offerCodeMergedProperties, offerCode, null, null);
                
                Entity temp = new Entity();
                temp.setType(offerCodeEntity.getType());
                temp.setProperties(new Property[] {offerCodeEntity.findProperty("offerCode"), offerCodeEntity.findProperty("id")});
                offerEntity.mergeProperties("offerCode", temp);
            }
            Property fgProperty = entity.findProperty("appliesToFulfillmentGroupRules");
            if (fgProperty != null) {
                offerEntity.addProperty(fgProperty);
            }
            
            return offerEntity;
        } catch (Exception e) {
            LOG.error("Unable to add entity for " + entity.getType()[0], e);
            throw new ServiceException("Unable to add entity for " + entity.getType()[0], e);
        }
    }

    @Override
    public void remove(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Property offerCodeId = entity.findProperty("offerCode.id");
            if (offerCodeId != null){
                OfferCode offerCode = (OfferCode) dynamicEntityDao.retrieve(OfferCodeImpl.class, Long.valueOf(entity.findProperty("offerCode.id").getValue()));
                if (offerCode != null) {
                    offerCode.setOffer(null);
                    dynamicEntityDao.remove(offerCode);
                }
            }
            Map<String, FieldMetadata> offerProperties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePerspective);
            Object primaryKey = helper.getPrimaryKey(entity, offerProperties);
            Offer offerInstance = (Offer) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);
            dynamicEntityDao.remove(offerInstance);
        } catch (Exception e) {
            LOG.error("Unable to remove entity for " + entity.getType()[0] + ". It is likely this offer is currently associated with one or more orders. Only unused offers may be deleted.", e);
            throw new ServiceException("Unable to remove entity for " + entity.getType()[0] + ". It is likely this offer is currently associated with one or more orders. Only unused offers may be deleted.", e);
        }
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        removeHTMLEncoding(entity);
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<String, FieldMetadata> offerProperties = helper.getSimpleMergedProperties(Offer.class.getName(), persistencePerspective);
            Object primaryKey = helper.getPrimaryKey(entity, offerProperties);
            Offer offerInstance = (Offer) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);
            offerInstance = (Offer) helper.createPopulatedInstance(offerInstance, entity, offerProperties, false);

            //**** Admin 3.0 ****
            //Convert the appliesTo* JSON fields into MVEL and update the appropriate properties on the Entity
            DataDTOToMVELTranslator translator = new DataDTOToMVELTranslator();
            convertMatchRuleJsonToMvel("appliesToOrderRulesJson", "appliesToOrderRules",
                    entity, translator, "order", "ORDER_FIELDS");
            convertMatchRuleJsonToMvel("appliesToCustomerRulesJson", "appliesToCustomerRules",
                    entity, translator, "customer", "CUSTOMER_FIELDS");
            convertMatchRuleJsonToMvel("appliesToFulfillmentGroupRulesJson", "appliesToFulfillmentGroupRules",
                    entity, translator, "fulfillmentGroup", "FULFILLMENT_GROUP_FIELDS");

            updateRule(entity, offerInstance, "appliesToOrderRules", OfferRuleType.ORDER);
            updateRule(entity, offerInstance, "appliesToCustomerRules", OfferRuleType.CUSTOMER);
            updateRule(entity, offerInstance, "appliesToFulfillmentGroupRules", OfferRuleType.FULFILLMENT_GROUP);

            //**** Admin 3.0 ****
            //Convert the item criteria JSON fields into MVEL and update the OfferInstance
            Set<OfferItemCriteria> netNewTargetCriteria = convertItemJsonToMvel(entity, offerInstance, translator,
                    "targetItemCriteriaJson", offerInstance.getTargetItemCriteria());
            for (OfferItemCriteria oic: netNewTargetCriteria) {
                dynamicEntityDao.persist(oic);
            }

            Set<OfferItemCriteria> netNewQualifyCriteria = convertItemJsonToMvel(entity, offerInstance, translator,
                    "qualifyingItemCriteriaJson", offerInstance.getQualifyingItemCriteria());
            for (OfferItemCriteria oic: netNewQualifyCriteria) {
                dynamicEntityDao.persist(oic);
            }

            dynamicEntityDao.merge(offerInstance);
            
            Property offerCodeId = entity.findProperty("offerCode.id");
            OfferCode offerCode = null;
            if (entity.findProperty("deliveryType") != null && entity.findProperty("deliveryType").getValue().equals("CODE")) {
                if (offerCodeId == null) {
                    offerCode = (OfferCode) entityConfiguration.createEntityInstance(OfferCode.class.getName());
                } else {
                    offerCode = (OfferCode) dynamicEntityDao.retrieve(OfferCodeImpl.class, Long.valueOf(entity.findProperty("offerCode.id").getValue()));
                }
                offerCode.setOfferCode(entity.findProperty("offerCode.offerCode").getValue());
                offerCode.setEndDate(offerInstance.getEndDate());
                offerCode.setMaxUses(offerInstance.getMaxUses());
                offerCode.setOffer(offerInstance);
                offerCode.setStartDate(offerInstance.getStartDate());
                offerCode = (OfferCode) dynamicEntityDao.merge(offerCode);
            } else if (offerCodeId != null){
                offerCode = (OfferCode) dynamicEntityDao.retrieve(OfferCodeImpl.class, Long.valueOf(entity.findProperty("offerCode.id").getValue()));
                offerCode.setOffer(null);
                dynamicEntityDao.remove(offerCode);
                offerCode = null;
            }
            
            Entity offerEntity = helper.getRecord(offerProperties, offerInstance, null, null);
            if (offerCode != null) {
                PersistencePerspective offerCodePersistencePerspective = new PersistencePerspective(null, new String[]{}, new ForeignKey[]{new ForeignKey("offer", EntityImplementations.OFFER, null)});
                Map<String, FieldMetadata> offerCodeMergedProperties = helper.getSimpleMergedProperties(OfferCode.class.getName(), offerCodePersistencePerspective);
                Entity offerCodeEntity = helper.getRecord(offerCodeMergedProperties, offerCode, null, null);
                
                Entity temp = new Entity();
                temp.setType(offerCodeEntity.getType());
                temp.setProperties(new Property[] {offerCodeEntity.findProperty("offerCode"), offerCodeEntity.findProperty("id")});
                offerEntity.mergeProperties("offerCode", temp);
            }
            Property fgProperty = entity.findProperty("appliesToFulfillmentGroupRules");
            if (fgProperty != null) {
                offerEntity.addProperty(fgProperty);
            }

            return offerEntity;
        } catch (Exception e) {
            LOG.error("Unable to update entity for " + entity.getType()[0], e);
            throw new ServiceException("Unable to update entity for " + entity.getType()[0], e);
        }
    }

    protected Set<OfferItemCriteria> convertItemJsonToMvel(Entity entity, Offer offerInstance,
                                         DataDTOToMVELTranslator translator,
                                         String itemCriteriaJson, Set<OfferItemCriteria> criteriaList)
            throws IOException, MVELTranslationException {

        Set<OfferItemCriteria> netNew = new HashSet<OfferItemCriteria>();
        Property jsonProp = entity.findProperty(itemCriteriaJson);
        if (jsonProp != null && !StringUtils.isEmpty(jsonProp.getValue())) {
            DataWrapper dw = convertJsonToDataWrapper(jsonProp.getValue());
            if (dw != null && !dw.getData().isEmpty()) {
                ArrayList<Long> updated = new ArrayList<Long>();
                for (DataDTO dto : dw.getData()) {
                    if (dto.getId() != null) {
                        //Update Existing Criteria
                        for (OfferItemCriteria oic : criteriaList) {
                            if (dto.getId().equals(oic.getId())){
                                oic.setQuantity(dto.getQuantity());
                                oic.setMatchRule(translator.createMVEL("discreteOrderItem", dto,
                                        ruleBuilderFieldServiceFactory.createInstance("ORDER_ITEM_FIELDS")));
                                updated.add(oic.getId());
                            }
                        }
                    } else {
                        //Create a new Criteria
                        OfferItemCriteria oic = (OfferItemCriteria) entityConfiguration.createEntityInstance(
                                OfferItemCriteria.class.getName());
                        oic.setQuantity(dto.getQuantity());
                        oic.setMatchRule(translator.createMVEL("discreteOrderItem", dto,
                                ruleBuilderFieldServiceFactory.createInstance("ORDER_ITEM_FIELDS")));
                        if ("targetItemCriteriaJson".equals(itemCriteriaJson)) {
                            oic.setTargetOffer(offerInstance);
                        } else if ("qualifyingItemCriteriaJson".equals(itemCriteriaJson)) {
                            oic.setQualifyingOffer(offerInstance);
                        }
                        netNew.add(oic);
                    }

                    Iterator<OfferItemCriteria> itr = criteriaList.iterator();
                    while (itr.hasNext()) {
                        OfferItemCriteria oic = itr.next();
                        if (oic.getId() != null && !updated.contains(oic.getId())) {
                            itr.remove();
                        }
                    }
                }
            }
        }
        return netNew;
    }

    protected void convertMatchRuleJsonToMvel(String jsonProperty, String ruleProperty,
                                              Entity entity, DataDTOToMVELTranslator translator, String entityKey,
                                              String fieldService) throws IOException, MVELTranslationException {
        Property appliesToJson = entity.findProperty(jsonProperty);
        Property appliesToRule = entity.findProperty(ruleProperty);
        if (appliesToJson != null && appliesToRule != null) {
            DataWrapper dw = convertJsonToDataWrapper(appliesToJson.getValue());
            //there can only be one DataDTO for an appliesTo* rule
            if (dw != null && dw.getData().size() == 1) {
                DataDTO dto = dw.getData().get(0);
                String mvel = translator.createMVEL(entityKey, dto,
                        ruleBuilderFieldServiceFactory.createInstance(fieldService));
                if (mvel != null) {
                    appliesToRule.setValue(mvel);
                }
            }
        }
    }

    protected void addRule(Entity entity, Offer offerInstance, String propertyName, OfferRuleType type) {
        Property ruleProperty = entity.findProperty(propertyName);
        if (ruleProperty != null && !StringUtils.isEmpty(ruleProperty.getValue())) {
            OfferRule rule = (OfferRule) entityConfiguration.createEntityInstance(OfferRule.class.getName());
            rule.setMatchRule(ruleProperty.getValue());
            offerInstance.getOfferMatchRules().put(type.getType(), rule);
        }
    }

    protected void updateRule(Entity entity, Offer offerInstance, String propertyName, OfferRuleType type) {
        Property ruleProperty = entity.findProperty(propertyName);
        if (ruleProperty != null && !StringUtils.isEmpty(ruleProperty.getValue())) {
            OfferRule rule = offerInstance.getOfferMatchRules().get(type.getType());
            if (rule == null) {
                rule = (OfferRule) entityConfiguration.createEntityInstance(OfferRule.class.getName());
            }
            rule.setMatchRule(ruleProperty.getValue());
            offerInstance.getOfferMatchRules().put(type.getType(), rule);
        } else {
            offerInstance.getOfferMatchRules().remove(type.getType());
        }
    }

    protected DataWrapper convertJsonToDataWrapper(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DataDTODeserializer dtoDeserializer = new DataDTODeserializer();
        SimpleModule module = new SimpleModule("DataDTODeserializerModule", new Version(1, 0, 0, null));
        module.addDeserializer(DataDTO.class, dtoDeserializer);
        mapper.registerModule(module);
        if (json == null || "[]".equals(json)){
            return null;
        }

        return mapper.readValue(json, DataWrapper.class);
    }

}
