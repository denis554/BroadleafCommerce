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

package org.broadleafcommerce.cms.admin.server.handler;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.cms.field.domain.FieldDefinition;
import org.broadleafcommerce.cms.field.domain.FieldEnumerationItem;
import org.broadleafcommerce.cms.field.domain.FieldGroup;
import org.broadleafcommerce.cms.page.domain.Page;
import org.broadleafcommerce.cms.page.domain.PageField;
import org.broadleafcommerce.cms.page.domain.PageFieldImpl;
import org.broadleafcommerce.cms.page.domain.PageTemplate;
import org.broadleafcommerce.cms.page.domain.PageTemplateImpl;
import org.broadleafcommerce.cms.page.service.PageService;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.sandbox.domain.SandBox;
import org.broadleafcommerce.common.web.SandBoxContext;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassTree;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.SandBoxService;
import org.broadleafcommerce.openadmin.server.service.persistence.module.InspectHelper;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfischer
 */
public class PageTemplateCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private Log LOG = LogFactory.getLog(PageTemplateCustomPersistenceHandler.class);

    @Resource(name="blPageService")
    protected PageService pageService;

    @Resource(name="blSandBoxService")
    protected SandBoxService sandBoxService;

    @Override
    public Boolean canHandleFetch(PersistencePackage persistencePackage) {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        return
            PageTemplate.class.getName().equals(ceilingEntityFullyQualifiedClassname) &&
            persistencePackage.getCustomCriteria() != null &&
            persistencePackage.getCustomCriteria().length > 0 &&
            persistencePackage.getCustomCriteria()[0].equals("constructForm");
    }

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        return false;
    }

    @Override
    public Boolean canHandleInspect(PersistencePackage persistencePackage) {
        return canHandleFetch(persistencePackage);
    }

    @Override
    public Boolean canHandleRemove(PersistencePackage persistencePackage) {
        return false;
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        return canHandleFetch(persistencePackage);
    }

    protected SandBox getSandBox() {
        return sandBoxService.retrieveSandboxById(SandBoxContext.getSandBoxContext().getSandBoxId());
    }

    @Override
    public DynamicResultSet inspect(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, InspectHelper helper) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            String pageTemplateId = persistencePackage.getCustomCriteria()[1];
            PageTemplate template = pageService.findPageTemplateById(Long.valueOf(pageTemplateId));
            ClassMetadata metadata = new ClassMetadata();
            metadata.setCeilingType(PageTemplate.class.getName());
            ClassTree entities = new ClassTree(PageTemplateImpl.class.getName());
            metadata.setPolymorphicEntities(entities);
            int groupCount = 1;
            int fieldCount = 0;
            List<Property> propertiesList = new ArrayList<Property>();
            List<FieldGroup> groups = template.getFieldGroups();
            for (FieldGroup group : groups) {
                List<FieldDefinition> definitions = group.getFieldDefinitions();
                for (FieldDefinition definition : definitions) {
                    Property property = new Property();
                    property.setName(definition.getName());
                    BasicFieldMetadata fieldMetadata = new BasicFieldMetadata();
                    property.setMetadata(fieldMetadata);
                    fieldMetadata.setFieldType(definition.getFieldType());
                    fieldMetadata.setMutable(true);
                    fieldMetadata.setInheritedFromType(PageTemplateImpl.class.getName());
                    fieldMetadata.setAvailableToTypes(new String[] {PageTemplateImpl.class.getName()});
                    fieldMetadata.setForeignKeyCollection(false);
                    fieldMetadata.setMergedPropertyType(MergedPropertyType.PRIMARY);
                    fieldMetadata.setLength(definition.getMaxLength());
                    if (definition.getFieldEnumeration() != null && !CollectionUtils.isEmpty(definition.getFieldEnumeration().getEnumerationItems())) {
                        int count = definition.getFieldEnumeration().getEnumerationItems().size();
                        String[][] enumItems = new String[count][2];
                        for (int j=0;j<count;j++) {
                            FieldEnumerationItem item = definition.getFieldEnumeration().getEnumerationItems().get(j);
                            enumItems[j][0] = item.getName();
                            enumItems[j][1] = item.getFriendlyName();
                        }
                        fieldMetadata.setEnumerationValues(enumItems);
                    }
                    fieldMetadata.setName(definition.getName());
                    fieldMetadata.setFriendlyName(definition.getFriendlyName());
                    fieldMetadata.setSecurityLevel(definition.getSecurityLevel()==null?"":definition.getSecurityLevel());
                    fieldMetadata.setOrder(fieldCount++);
                    fieldMetadata.setVisibility(definition.getHiddenFlag()?VisibilityEnum.HIDDEN_ALL:VisibilityEnum.VISIBLE_ALL);
                    fieldMetadata.setGroup(group.getName());
                    fieldMetadata.setGroupOrder(groupCount);
                    fieldMetadata.setGroupCollapsed(group.getInitCollapsedFlag());
                    fieldMetadata.setExplicitFieldType(SupportedFieldType.UNKNOWN);
                    fieldMetadata.setLargeEntry(definition.getTextAreaFlag());
                    fieldMetadata.setProminent(false);
                    fieldMetadata.setColumnWidth(String.valueOf(definition.getColumnWidth()));
                    fieldMetadata.setBroadleafEnumeration("");
                    fieldMetadata.setReadOnly(false);
                    if (definition.getValidationRegEx() != null) {
                        Map<String, String> itemMap = new HashMap<String, String>();
                        itemMap.put("regularExpression", definition.getValidationRegEx());
                        itemMap.put("errorMessageKey", definition.getValidationErrorMesageKey());
                        fieldMetadata.getValidationConfigurations().put("com.smartgwt.client.widgets.form.validator.RegExpValidator", itemMap);
                    }
                    propertiesList.add(property);
                }
                groupCount++;
                fieldCount = 0;
            }
            Property property = new Property();
            property.setName("id");
            BasicFieldMetadata fieldMetadata = new BasicFieldMetadata();
            property.setMetadata(fieldMetadata);
            fieldMetadata.setFieldType(SupportedFieldType.ID);
            fieldMetadata.setSecondaryType(SupportedFieldType.INTEGER);
            fieldMetadata.setMutable(true);
            fieldMetadata.setInheritedFromType(PageTemplateImpl.class.getName());
            fieldMetadata.setAvailableToTypes(new String[] {PageTemplateImpl.class.getName()});
            fieldMetadata.setForeignKeyCollection(false);
            fieldMetadata.setMergedPropertyType(MergedPropertyType.PRIMARY);
            fieldMetadata.setName("id");
            fieldMetadata.setFriendlyName("PagesCustomPersistenceHandler_ID");
            fieldMetadata.setSecurityLevel("");
            fieldMetadata.setVisibility(VisibilityEnum.VISIBLE_ALL);
            fieldMetadata.setExplicitFieldType(SupportedFieldType.UNKNOWN);
            fieldMetadata.setLargeEntry(false);
            fieldMetadata.setProminent(false);
            fieldMetadata.setColumnWidth("*");
            fieldMetadata.setBroadleafEnumeration("");
            fieldMetadata.setReadOnly(true);
            propertiesList.add(property);

            Property[] properties = new Property[propertiesList.size()];
            properties = propertiesList.toArray(properties);
            Arrays.sort(properties, new Comparator<Property>() {
                public int compare(Property o1, Property o2) {
                    /*
                         * First, compare properties based on order fields
                         */
                    if (o1.getMetadata().getOrder() != null && o2.getMetadata().getOrder() != null) {
                        return o1.getMetadata().getOrder().compareTo(o2.getMetadata().getOrder());
                    } else if (o1.getMetadata().getOrder() != null && o2.getMetadata().getOrder() == null) {
                        /*
                              * Always favor fields that have an order identified
                              */
                        return -1;
                    } else if (o1.getMetadata().getOrder() == null && o2.getMetadata().getOrder() != null) {
                        /*
                              * Always favor fields that have an order identified
                              */
                        return 1;
                    } else if (o1.getMetadata().getFriendlyName() != null && o2.getMetadata().getFriendlyName() != null) {
                        return o1.getMetadata().getFriendlyName().compareTo(o2.getMetadata().getFriendlyName());
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                }
            });
            metadata.setProperties(properties);
            DynamicResultSet results = new DynamicResultSet(metadata);

            return results;
        } catch (Exception e) {
            LOG.error("Unable to perform inspect for entity: "+ceilingEntityFullyQualifiedClassname, e);
            throw new ServiceException("Unable to perform inspect for entity: "+ceilingEntityFullyQualifiedClassname, e);
        }
    }

    @Override
    public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            String pageId = persistencePackage.getCustomCriteria()[1];
            Entity entity = fetchEntityBasedOnId(pageId);
            DynamicResultSet results = new DynamicResultSet(new Entity[]{entity}, 1);

            return results;
        } catch (Exception e) {
            LOG.error("Unable to perform fetch for entity: "+ceilingEntityFullyQualifiedClassname, e);
            throw new ServiceException("Unable to perform fetch for entity: "+ceilingEntityFullyQualifiedClassname, e);
        }
    }

    protected Entity fetchEntityBasedOnId(String pageId) throws Exception {
        Page page = (Page) pageService.findPageById(Long.valueOf(pageId));
        Map<String, PageField> pageFieldMap = page.getPageFields();
        Entity entity = new Entity();
        entity.setType(new String[]{PageTemplateImpl.class.getName()});
        List<Property> propertiesList = new ArrayList<Property>();
        for (FieldGroup fieldGroup : page.getPageTemplate().getFieldGroups()) {
            for (FieldDefinition definition : fieldGroup.getFieldDefinitions()) {
                Property property = new Property();
                propertiesList.add(property);
                property.setName(definition.getName());
                String value = null;
                if (!MapUtils.isEmpty(pageFieldMap)) {
                    PageField pageField = pageFieldMap.get(definition.getName());
                    if(pageField==null)
                        value="";
                     else
                        value = pageField.getValue();
                }
                property.setValue(value);
            }
        }
        Property property = new Property();
        propertiesList.add(property);
        property.setName("id");
        property.setValue(pageId);

        entity.setProperties(propertiesList.toArray(new Property[]{}));

        return entity;
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            String pageId = persistencePackage.getCustomCriteria()[1];
            Page page = pageService.findPageById(Long.valueOf(pageId));
            List<String> templateFieldNames = new ArrayList<String>(20);
            for (FieldGroup group : page.getPageTemplate().getFieldGroups()) {
                for (FieldDefinition definition: group.getFieldDefinitions()) {
                    templateFieldNames.add(definition.getName());
                }
            }
            Map<String, PageField> pageFieldMap = page.getPageFields();
            for (Property property : persistencePackage.getEntity().getProperties()) {
                if (templateFieldNames.contains(property.getName())) {
                    PageField pageField = pageFieldMap.get(property.getName());
                    if (pageField != null) {
                        pageField.setValue(property.getValue());
                    } else {
                        pageField = new PageFieldImpl();
                        pageFieldMap.put(property.getName(), pageField);
                        pageField.setFieldKey(property.getName());
                        pageField.setPage(page);
                        pageField.setValue(property.getValue());
                    }
                }
            }
            List<String> removeItems = new ArrayList<String>();
            for (String key : pageFieldMap.keySet()) {
                if (persistencePackage.getEntity().findProperty(key)==null) {
                    removeItems.add(key);
                }
            }
            if (removeItems.size() > 0) {
                for (String removeKey : removeItems) {
                    PageField pageField = pageFieldMap.remove(removeKey);
                    pageField.setPage(null);
                }
            }
            pageService.updatePage(page, getSandBox());

            return fetchEntityBasedOnId(pageId);
        } catch (Exception e) {
            LOG.error("Unable to perform update for entity: "+ceilingEntityFullyQualifiedClassname, e);
            throw new ServiceException("Unable to perform update for entity: "+ceilingEntityFullyQualifiedClassname, e);
        }
    }
}
