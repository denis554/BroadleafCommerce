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

package org.broadleafcommerce.openadmin.server.service;

import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.openadmin.client.dto.AdornedTargetCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.BasicCollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.CollectionMetadata;
import org.broadleafcommerce.openadmin.client.dto.CriteriaTransferObject;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.FilterAndSortCriteria;
import org.broadleafcommerce.openadmin.client.dto.MapMetadata;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.client.service.DynamicEntityService;
import org.broadleafcommerce.openadmin.server.domain.PersistencePackageRequest;
import org.broadleafcommerce.openadmin.server.factory.PersistencePackageFactory;
import org.broadleafcommerce.openadmin.web.form.entity.EntityForm;
import org.broadleafcommerce.openadmin.web.form.entity.Field;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.gwtincubator.security.exception.ApplicationSecurityException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

/**
 * @author Andre Azzolini (apazzolini)
 */
@Service("blAdminEntityService")
public class AdminEntityServiceImpl implements AdminEntityService {

    @Resource(name = "blDynamicEntityRemoteService")
    protected DynamicEntityService service;

    @Resource(name = "blPersistencePackageFactory")
    protected PersistencePackageFactory persistencePackageFactory;

    @Override
    public ClassMetadata getClassMetadata(String className) throws ServiceException, ApplicationSecurityException {
        PersistencePackageRequest request = PersistencePackageRequest.standard()
                .withClassName(className);
        return getClassMetadata(request);
    }

    @Override
    public ClassMetadata getClassMetadata(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        ClassMetadata cmd = inspect(request).getClassMetaData();
        cmd.setCeilingType(request.getClassName());
        return cmd;
    }

    @Override
    public Entity[] getRecords(PersistencePackageRequest request) throws ServiceException, ApplicationSecurityException {
        return fetch(request).getRecords();
    }

    @Override
    public Entity getRecord(String className, String id) throws ServiceException, ApplicationSecurityException {
        FilterAndSortCriteria fasc = new FilterAndSortCriteria("id");
        fasc.setFilterValue(id);

        PersistencePackageRequest request = PersistencePackageRequest.standard()
                .withClassName(className)
                .addFilterAndSortCriteria(fasc);

        Entity[] entities = fetch(request).getRecords();

        Assert.isTrue(entities != null && entities.length == 1);

        Entity entity = entities[0];
        return entity;
    }

    @Override
    public Entity updateEntity(EntityForm entityForm, String className)
            throws ServiceException, ApplicationSecurityException {
        // Build the property array from the field map
        Property[] properties = new Property[entityForm.getFields().size()];
        int i = 0;
        for (Entry<String, Field> entry : entityForm.getFields().entrySet()) {
            Property p = new Property();
            p.setName(entry.getKey());
            p.setValue(entry.getValue().getValue());
            properties[i++] = p;
        }

        Entity entity = new Entity();
        entity.setProperties(properties);
        entity.setType(new String[] { entityForm.getEntityType() });

        PersistencePackageRequest request = PersistencePackageRequest.standard()
                .withEntity(entity)
                .withClassName(className);
        return update(request);
    }

    @Override
    public Entity[] getRecordsForCollection(ClassMetadata containingClassMetadata, String containingEntityId,
            Property collectionProperty)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackageRequest ppr = PersistencePackageRequest.fromMetadata(collectionProperty.getMetadata());
        FilterAndSortCriteria fasc;

        FieldMetadata md = collectionProperty.getMetadata();

        if (md instanceof BasicCollectionMetadata) {
            fasc = new FilterAndSortCriteria(ppr.getForeignKeys()[0].getManyToField());
        } else if (md instanceof AdornedTargetCollectionMetadata) {
            fasc = new FilterAndSortCriteria(ppr.getAdornedList().getCollectionFieldName());
        } else if (md instanceof MapMetadata) {
            fasc = new FilterAndSortCriteria(ppr.getForeignKeys()[0].getManyToField());
        } else {
            throw new IllegalArgumentException(String.format("The specified field [%s] for class [%s] was not a " +
                    "collection field.", collectionProperty.getName(), containingClassMetadata.getCeilingType()));
        }

        fasc.setFilterValue(containingEntityId);
        ppr.addFilterAndSortCriteria(fasc);

        return fetch(ppr).getRecords();
    }

    @Override
    public Map<String, Entity[]> getRecordsForAllSubCollections(String containingClassName, String containingEntityId)
            throws ServiceException, ApplicationSecurityException {
        Map<String, Entity[]> map = new HashMap<String, Entity[]>();

        ClassMetadata cmd = getClassMetadata(containingClassName);
        for (Property p : cmd.getProperties()) {
            if (p.getMetadata() instanceof CollectionMetadata) {
                Entity[] rows = getRecordsForCollection(cmd, containingEntityId, p);
                map.put(p.getName(), rows);
            }
        }

        return map;
    }

    @Override
    public Entity addSubCollectionEntity(EntityForm entityForm, ClassMetadata mainMetadata, Property field, String parentId)
            throws ServiceException, ApplicationSecurityException, ClassNotFoundException {
        // Assemble the properties from the entity form
        List<Property> properties = new ArrayList<Property>();
        for (Entry<String, Field> entry : entityForm.getFields().entrySet()) {
            Property p = new Property();
            p.setName(entry.getKey());
            p.setValue(entry.getValue().getValue());
            properties.add(p);
        }

        FieldMetadata md = field.getMetadata();

        PersistencePackageRequest ppr = PersistencePackageRequest.fromMetadata(md)
                .withEntity(new Entity());

        if (md instanceof BasicCollectionMetadata) {
            BasicCollectionMetadata fmd = (BasicCollectionMetadata) md;
            ppr.getEntity().setType(new String[] { fmd.getCollectionCeilingEntity() });

            Property fp = new Property();
            fp.setName(ppr.getForeignKeys()[0].getManyToField());
            fp.setValue(parentId);
            properties.add(fp);
        } else if (md instanceof AdornedTargetCollectionMetadata) {
            ppr.getEntity().setType(new String[] { ppr.getAdornedList().getAdornedTargetEntityClassname() });
        } else if (md instanceof MapMetadata) {
            ppr.getEntity().setType(new String[] { entityForm.getEntityType() });
        } else {
            throw new IllegalArgumentException(String.format("The specified field [%s] for class [%s] was" +
                    " not a collection field.", field.getName(), mainMetadata.getCeilingType()));
        }

        ppr.setClassName(ppr.getEntity().getType()[0]);

        Property[] propArr = new Property[properties.size()];
        properties.toArray(propArr);
        ppr.getEntity().setProperties(propArr);

        return add(ppr);
    }

    @Override
    public void removeSubCollectionEntity(ClassMetadata mainMetadata, Property field, String parentId, String itemId)
            throws ServiceException, ApplicationSecurityException {
        Property p = new Property();
        p.setName("id");
        p.setValue(itemId);

        Entity entity = new Entity();
        entity.setProperties(new Property[] { p });

        PersistencePackageRequest ppr = PersistencePackageRequest.fromMetadata(field.getMetadata())
                .withEntity(entity);

        if (field.getMetadata() instanceof BasicCollectionMetadata) {
            BasicCollectionMetadata fmd = (BasicCollectionMetadata) field.getMetadata();
            entity.setType(new String[] { fmd.getCollectionCeilingEntity() });
        } else {
            throw new RuntimeException("not yet");
        }

        remove(ppr);
    }

    protected Entity add(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackage pkg = persistencePackageFactory.create(request);
        return service.add(pkg);
    }

    protected Entity update(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackage pkg = persistencePackageFactory.create(request);
        return service.update(pkg);
    }

    protected DynamicResultSet inspect(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackage pkg = persistencePackageFactory.create(request);
        return service.inspect(pkg);
    }

    protected void remove(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackage pkg = persistencePackageFactory.create(request);
        service.remove(pkg);
    }

    protected DynamicResultSet fetch(PersistencePackageRequest request)
            throws ServiceException, ApplicationSecurityException {
        PersistencePackage pkg = persistencePackageFactory.create(request);

        CriteriaTransferObject cto = getDefaultCto();
        if (request.getFilterAndSortCriteria() != null) {
            cto.addAll(request.getFilterAndSortCriteria());
        }

        return service.fetch(pkg, cto);
    }

    protected CriteriaTransferObject getDefaultCto() {
        CriteriaTransferObject cto = new CriteriaTransferObject();
        cto.setFirstResult(0);
        cto.setMaxResults(75);
        return cto;
    }

}