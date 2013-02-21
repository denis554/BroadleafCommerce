/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.openadmin.web.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetList;
import org.broadleafcommerce.openadmin.client.dto.BasicCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.MapMetadata;
import org.broadleafcommerce.openadmin.client.dto.MapStructure;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.server.domain.PersistencePackageRequest;
import org.broadleafcommerce.openadmin.server.service.AdminEntityService;
import org.broadleafcommerce.openadmin.web.form.component.ListGrid;
import org.broadleafcommerce.openadmin.web.form.component.ListGridRecord;
import org.broadleafcommerce.openadmin.web.form.component.RuleBuilder;
import org.broadleafcommerce.openadmin.web.form.entity.ComboField;
import org.broadleafcommerce.openadmin.web.form.entity.EntityForm;
import org.broadleafcommerce.openadmin.web.form.entity.Field;
import org.springframework.stereotype.Service;

import com.gwtincubator.security.exception.ApplicationSecurityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

/**
 * @author Andre Azzolini (apazzolini)
 */
@Service("blFormBuilderService")
public class FormBuilderServiceImpl implements FormBuilderService {

    @Resource(name = "blAdminEntityService")
    protected AdminEntityService adminEntityService;

    @Override
    public ListGrid buildMainListGrid(Entity[] entities, ClassMetadata cmd)
            throws ServiceException, ApplicationSecurityException {

        List<Field> headerFields = new ArrayList<Field>();
        ListGrid.Type type = ListGrid.Type.MAIN;

        for (Property p : cmd.getProperties()) {
            if (p.getMetadata() instanceof BasicFieldMetadata) {
                BasicFieldMetadata fmd = (BasicFieldMetadata) p.getMetadata();
                if (fmd.isProminent() != null && fmd.isProminent()) {
                    Field hf = new Field()
                            .withName(p.getName())
                            .withFriendlyName(fmd.getFriendlyName());
                    headerFields.add(hf);
                }
            }
        }

        return createListGrid(cmd.getCeilingType(), headerFields, type, entities);
    }

    @Override
    public ListGrid buildCollectionListGrid(String containingEntityId, Entity[] entities, Property field)
            throws ServiceException, ApplicationSecurityException {
        FieldMetadata fmd = field.getMetadata();
        // Get the class metadata for this particular field
        PersistencePackageRequest ppr = PersistencePackageRequest.fromMetadata(fmd);
        ClassMetadata cmd = adminEntityService.getClassMetadata(ppr);

        List<Field> headerFields = new ArrayList<Field>();
        ListGrid.Type type = null;

        // Get the header fields for this list grid. Note that the header fields are different depending on the
        // kind of field this is.
        if (fmd instanceof BasicFieldMetadata) {
            for (Property p : cmd.getProperties()) {
                if (p.getMetadata() instanceof BasicFieldMetadata) {
                    BasicFieldMetadata md = (BasicFieldMetadata) p.getMetadata();
                    if (md.isProminent() != null && md.isProminent()) {
                        Field hf = new Field()
                                .withName(p.getName())
                                .withFriendlyName(fmd.getFriendlyName());
                        headerFields.add(hf);
                    }
                }
            }

            type = ListGrid.Type.TO_ONE;
        } else if (fmd instanceof BasicCollectionMetadata) {
            for (Property p : cmd.getProperties()) {
                if (p.getMetadata() instanceof BasicFieldMetadata) {
                    BasicFieldMetadata md = (BasicFieldMetadata) p.getMetadata();
                    if (md.isProminent() != null && md.isProminent()) {
                        Field hf = new Field()
                                .withName(p.getName())
                                .withFriendlyName(md.getFriendlyName());
                        headerFields.add(hf);
                    }
                }
            }

            type = ListGrid.Type.BASIC;
        } else if (fmd instanceof AdornedTargetCollectionMetadata) {
            AdornedTargetCollectionMetadata atcmd = (AdornedTargetCollectionMetadata) fmd;

            for (String fieldName : atcmd.getGridVisibleFields()) {
                Property p = cmd.getPMap().get(fieldName);
                Field hf = new Field()
                        .withName(p.getName())
                        .withFriendlyName(p.getMetadata().getFriendlyName());
                headerFields.add(hf);
            }

            type = ListGrid.Type.ADORNED;
        } else if (fmd instanceof MapMetadata) {
            MapMetadata mmd = (MapMetadata) fmd;

            Property p2 = cmd.getPMap().get("key");
            Field hf = new Field()
                    .withName(p2.getName())
                    .withFriendlyName(p2.getMetadata().getFriendlyName());
            headerFields.add(hf);

            for (Property p : cmd.getProperties()) {
                if (p.getMetadata() instanceof BasicFieldMetadata) {
                    BasicFieldMetadata md = (BasicFieldMetadata) p.getMetadata();
                    if (md.getTargetClass().equals(mmd.getValueClassName())) {
                        if (md.isProminent() != null && md.isProminent()) {
                            hf = new Field()
                                    .withName(p.getName())
                                    .withFriendlyName(md.getFriendlyName());
                            headerFields.add(hf);
                        }
                    }
                }
            }

            type = ListGrid.Type.MAP;
        }

        ListGrid listGrid = createListGrid(cmd.getCeilingType(), headerFields, type, entities);
        listGrid.setSubCollectionFieldName(field.getName());
        listGrid.setContainingEntityId(containingEntityId);

        return listGrid;
    }

    protected ListGrid createListGrid(String className, List<Field> headerFields, ListGrid.Type type, Entity[] entities) {
        // Create the list grid and set some basic attributes
        ListGrid listGrid = new ListGrid();
        listGrid.setClassName(className);
        listGrid.setHeaderFields(headerFields);
        listGrid.setListGridType(type);

        // For each of the entities (rows) in the list grid, we need to build the associated
        // ListGridRecord and set the required fields on the record. These fields are the same ones
        // that are used for the header fields.
        for (Entity e : entities) {
            ListGridRecord record = new ListGridRecord();
            record.setId(e.findProperty("id").getValue());

            for (Field headerField : headerFields) {
                Property p = e.findProperty(headerField.getName());
                Field recordField = new Field()
                        .withName(headerField.getName())
                        .withValue(p.getValue());
                record.getFields().add(recordField);
            }

            listGrid.getRecords().add(record);
        }

        return listGrid;
    }

    @Override
    public RuleBuilder buildRuleBuilder(String fieldName, String friendlyName, Entity[] entities) {
        RuleBuilder rb = new RuleBuilder();
        rb.setFieldName(fieldName);
        rb.setFriendlyName(friendlyName);
        rb.setEntities(entities);

        return rb;
    }

    protected void setEntityFormFields(EntityForm ef, List<Property> properties) {
        for (Property property : properties) {
            if (property.getMetadata() instanceof BasicFieldMetadata) {
                BasicFieldMetadata fmd = (BasicFieldMetadata) property.getMetadata();

                String fieldType = fmd.getFieldType() == null ? null : fmd.getFieldType().toString();

                // Create the field and set some basic attributes
                Field f = new Field()
                        .withName(property.getName())
                        .withFieldType(fieldType)
                        .withFriendlyName(property.getMetadata().getFriendlyName())
                        .withForeignKeyDisplayValueProperty(fmd.getForeignKeyDisplayValueProperty());

                if (StringUtils.isBlank(f.getFriendlyName())) {
                    f.setFriendlyName(f.getName());
                }

                // Add the field to the appropriate FieldGroup
                ef.addField(f, fmd.getGroup());
            }
        }
    }

    @Override
    public EntityForm buildEntityForm(ClassMetadata cmd) {
        EntityForm ef = new EntityForm();
        ef.setEntityType(cmd.getCeilingType());

        setEntityFormFields(ef, Arrays.asList(cmd.getProperties()));

        return ef;
    }

    @Override
    public EntityForm buildEntityForm(ClassMetadata cmd, Entity entity) {
        // Get the empty form with appropriate fields
        EntityForm ef = buildEntityForm(cmd);
        ef.setId(entity.findProperty("id").getValue());
        ef.setEntityType(entity.getType()[0]);

        // Set the appropriate property values
        for (Property p : cmd.getProperties()) {
            if (p.getMetadata() instanceof BasicFieldMetadata) {
                Property entityProp = entity.findProperty(p.getName());

                if (entityProp == null) {
                    ef.removeField(p.getName());
                } else {
                    Field field = ef.findField(p.getName());
                    field.setValue(entityProp.getValue());
                    field.setDisplayValue(entityProp.getDisplayValue());
                }
            }
        }

        return ef;
    }

    @Override
    public EntityForm buildEntityForm(ClassMetadata cmd, Entity entity, Map<String, Entity[]> collectionRecords)
            throws ServiceException, ApplicationSecurityException {
        // Get the form with values for this entity
        EntityForm ef = buildEntityForm(cmd, entity);

        // Attach the sub-collection list grids
        for (Property p : cmd.getProperties()) {

            if (p.getMetadata() instanceof BasicFieldMetadata) {
                continue;
            }

            Entity[] subCollectionEntities = collectionRecords.get(p.getName());

            if (p.getMetadata() instanceof BasicCollectionMetadata) {
                BasicCollectionMetadata fmd = (BasicCollectionMetadata) p.getMetadata();

                if (fmd.isRuleBuilder()) {
                    RuleBuilder subCollectionRuleBuilder = buildRuleBuilder(p.getName(), fmd.getFriendlyName(),
                            subCollectionEntities);
                    ef.getCollectionRuleBuilders().add(subCollectionRuleBuilder);

                    continue;
                }

            }

            String containingEntityId = entity.getPMap().get("id").getValue();
            ListGrid listGrid = buildCollectionListGrid(containingEntityId, subCollectionEntities, p);
            listGrid.setListGridType(ListGrid.Type.INLINE);

            ef.getCollectionListGrids().add(listGrid);
        }

        return ef;
    }

    @Override
    public void setEntityFormValues(EntityForm destinationForm, EntityForm sourceForm) {
        for (Entry<String, Field> entry : sourceForm.getFields().entrySet()) {
            Field destinationField = destinationForm.getFields().get(entry.getKey());
            destinationField.setValue(entry.getValue().getValue());
            destinationField.setDisplayValue(entry.getValue().getDisplayValue());
        }
    }

    @Override
    public EntityForm buildAdornedListForm(AdornedTargetCollectionMetadata adornedMd, AdornedTargetList adornedList,
            String parentId)
            throws ServiceException, ApplicationSecurityException {
        EntityForm ef = new EntityForm();
        ef.setEntityType(adornedList.getAdornedTargetEntityClassname());

        // Get the metadata for this adorned field
        PersistencePackageRequest request = PersistencePackageRequest.adorned()
                .withClassName(adornedMd.getCollectionCeilingEntity())
                .withAdornedList(adornedList);
        ClassMetadata collectionMetadata = adminEntityService.getClassMetadata(request);

        // We want our entity form to only render the maintained adorned target fields
        List<Property> entityFormProperties = new ArrayList<Property>();
        for (String targetFieldName : adornedMd.getMaintainedAdornedTargetFields()) {
            Property p = collectionMetadata.getPMap().get(targetFieldName);
            entityFormProperties.add(p);
        }

        // Set the maintained fields on the form
        setEntityFormFields(ef, entityFormProperties);

        // Add these two additional hidden fields that are required for persistence
        Field f = new Field()
                .withName(adornedList.getLinkedObjectPath() + "." + adornedList.getLinkedIdProperty())
                .withFieldType(SupportedFieldType.HIDDEN.toString())
                .withValue(parentId);
        ef.addField(f, EntityForm.HIDDEN_GROUP);

        f = new Field()
                .withName(adornedList.getTargetObjectPath() + "." + adornedList.getTargetIdProperty())
                .withFieldType(SupportedFieldType.HIDDEN.toString())
                .withIdOverride("adornedTargetIdProperty");
        ef.addField(f, EntityForm.HIDDEN_GROUP);

        return ef;
    }

    @Override
    public EntityForm buildMapForm(MapMetadata mapMd, final MapStructure mapStructure, ClassMetadata cmd, String parentId)
            throws ServiceException, ApplicationSecurityException {
        EntityForm ef = new EntityForm();
        ef.setEntityType(mapMd.getTargetClass());

        // We will use a combo field to render the key choices
        ComboField keyField = new ComboField();
        keyField.withName("key")
                .withFieldType("combo_field")
                .withFriendlyName("Key");

        if (mapMd.getKeys() != null) {
            // The keys can be explicitly set in the annotation...
            for (String[] key : mapMd.getKeys()) {
                keyField.putOption(key[0], key[1]);
            }
        } else {
            // Or they could be based on a different entity
            PersistencePackageRequest ppr = PersistencePackageRequest.standard()
                    .withClassName(mapMd.getMapKeyOptionEntityClass());

            Entity[] rows = adminEntityService.getRecords(ppr);

            for (Entity entity : rows) {
                String keyValue = entity.getPMap().get(mapMd.getMapKeyOptionEntityValueField()).getValue();
                String keyDisplayValue = entity.getPMap().get(mapMd.getMapKeyOptionEntityDisplayField()).getValue();
                keyField.putOption(keyValue, keyDisplayValue);
            }
        }
        ef.addField(keyField, EntityForm.MAP_KEY_GROUP);

        // Set the fields for this form
        List<Property> mapFormProperties = new ArrayList<Property>(Arrays.asList(cmd.getProperties()));
        CollectionUtils.filter(mapFormProperties, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Property p = (Property) object;
                return ArrayUtils.contains(p.getMetadata().getAvailableToTypes(), mapStructure.getValueClassName());
            }
        });

        setEntityFormFields(ef, mapFormProperties);

        // Add the symbolicId field required for persistence
        Field f = new Field()
                .withName("symbolicId")
                .withFieldType(SupportedFieldType.HIDDEN.toString())
                .withValue(parentId);
        ef.addField(f, EntityForm.HIDDEN_GROUP);

        return ef;
    }

}