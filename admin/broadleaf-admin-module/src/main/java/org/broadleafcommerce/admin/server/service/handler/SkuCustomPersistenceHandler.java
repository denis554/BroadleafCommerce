/*
 * Copyright 2012 the original author or authors.
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

/**
 * 
 */

package org.broadleafcommerce.admin.server.service.handler;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.admin.client.datasource.catalog.product.module.SkuBasicClientEntityModule;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductOption;
import org.broadleafcommerce.core.catalog.domain.ProductOptionValue;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.domain.SkuImpl;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.openadmin.client.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.client.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.MergedPropertyType;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.server.cto.BaseCtoConverter;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.InspectHelper;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.anasoft.os.daofusion.criteria.AssociationPath;
import com.anasoft.os.daofusion.criteria.AssociationPathElement;
import com.anasoft.os.daofusion.criteria.FilterCriterion;
import com.anasoft.os.daofusion.criteria.NestedPropertyCriteria;
import com.anasoft.os.daofusion.criteria.PersistentEntityCriteria;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider.FilterDataStrategy;
import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.anasoft.os.daofusion.cto.client.FilterAndSortCriteria;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

/**
 * @author Phillip Verheyden
 *
 */
public class SkuCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(SkuCustomPersistenceHandler.class);

    public static String PRODUCT_OPTION_FIELD_PREFIX = "productOption";

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
    
    /**
     * This represents the field that all of the product option values will be stored in. This would be used in the case
     * where there are a bunch of product options and displaying each option as a grid header would have everything
     * squashed together. Filtering on this field is currently unsupported.
     */
    public static String CONSOLIDATED_PRODUCT_OPTIONS_FIELD_NAME = "consolidatedProductOptions";
    public static String CONSOLIDATED_PRODUCT_OPTIONS_DELIMETER = "; ";

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Override
    public Boolean canHandleInspect(PersistencePackage persistencePackage) {
        return canHandle(persistencePackage, persistencePackage.getPersistencePerspective().getOperationTypes()
                .getInspectType());
    }

    @Override
    public Boolean canHandleFetch(PersistencePackage persistencePackage) {
        OperationType fetchType = persistencePackage.getPersistencePerspective().getOperationTypes().getFetchType();
        return canHandle(persistencePackage, fetchType);
    }

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        OperationType addType = persistencePackage.getPersistencePerspective().getOperationTypes().getAddType();
        return canHandle(persistencePackage, addType);
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        OperationType updateType = persistencePackage.getPersistencePerspective().getOperationTypes().getUpdateType();
        return canHandle(persistencePackage, updateType);
    }

    /**
     * Since this is the default for all Skus, it's possible that we are providing custom criteria for this
     * Sku lookup. In that case, we probably want to delegate to a child class, so only use this particular
     * persistence handler if there is no custom criteria being used and the ceiling entity is an instance of Sku. The
     * exception to this rule is when we are pulling back Media, since the admin actually uses Sku for the ceiling entity
     * class name. That should be handled by the map structure module though, so only handle things in the Sku custom
     * persistence handler for OperationType.BASIC
     * 
     */
    protected Boolean canHandle(PersistencePackage persistencePackage, OperationType operationType) {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {

            Class testClass = Class.forName(ceilingEntityFullyQualifiedClassname);
            return Sku.class.isAssignableFrom(testClass) && ArrayUtils.isEmpty(persistencePackage.getCustomCriteria()) &&
                    OperationType.BASIC.equals(operationType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Build out the extra fields for the product options
     */
    @Override
    public DynamicResultSet inspect(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, InspectHelper helper) throws ServiceException {
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<MergedPropertyType, Map<String, FieldMetadata>> allMergedProperties = new HashMap<MergedPropertyType, Map<String, FieldMetadata>>();

            //Grab the default properties for the Sku
            Map<String, FieldMetadata> properties = helper.getSimpleMergedProperties(Sku.class.getName(), persistencePerspective);

            //look up all the ProductOptions and then create new fields for each of them. Although
            //all of the options might not be relevant for the current Product (and thus the Skus as well) we
            //can hide the irrelevant fields in the fetch via a custom ClientEntityModule
            List<ProductOption> options = catalogService.readAllProductOptions();
            int order = 0;
            for (ProductOption option : options) {
                //add this to the built Sku properties
                properties.put("productOption" + option.getId(), createIndividualOptionField(option, order));
            }

            //also build the consolidated field; if using the SkuBasicClientEntityModule then this field will be
            //permanently hidden
            properties.put(CONSOLIDATED_PRODUCT_OPTIONS_FIELD_NAME, createConsolidatedOptionField(SkuImpl.class));

            allMergedProperties.put(MergedPropertyType.PRIMARY, properties);
            Class<?>[] entityClasses = dynamicEntityDao.getAllPolymorphicEntitiesFromCeiling(Sku.class);
            ClassMetadata mergedMetadata = helper.getMergedClassMetadata(entityClasses, allMergedProperties);
            DynamicResultSet results = new DynamicResultSet(mergedMetadata, null, null);

            return results;
        } catch (Exception e) {
            ServiceException ex = new ServiceException("Unable to retrieve inspection results for " +
                    persistencePackage.getCeilingEntityFullyQualifiedClassname(), e);
            LOG.error("Unable to retrieve inspection results for " +
                    persistencePackage.getCeilingEntityFullyQualifiedClassname(), ex);
            throw ex;
        }
    }

    /**
     * Creates the metadata necessary for displaying all of the product option values in a single field. The display of this
     * field is a single string with every product option value appended to it separated by a semicolon. This method should
     * be invoked on an inspect for whatever is utilizing this so that the property will be ready to be populated on fetch.
     * 
     * The metadata that is returned will also be set to prominent by default so that it will be ready to display on whatever
     * grid is being inspected. If you do not want this behavior you will need to override this functionality in the metadata
     * that is returned.
     * 
     * @param inheritedFromType which type this should appear on. This would normally be SkuImpl.class, but if you want to
     * display this field with a different entity then this should be that entity
     * @return
     */
    public static FieldMetadata createConsolidatedOptionField(Class<?> inheritedFromType) {
        BasicFieldMetadata metadata = new BasicFieldMetadata();
        metadata.setFieldType(SupportedFieldType.STRING);
        metadata.setMutable(false);
        metadata.setInheritedFromType(SkuImpl.class.getName());
        metadata.setAvailableToTypes(new String[] { SkuImpl.class.getName() });
        metadata.setForeignKeyCollection(false);
        metadata.setMergedPropertyType(MergedPropertyType.PRIMARY);

        metadata.setName(CONSOLIDATED_PRODUCT_OPTIONS_FIELD_NAME);
        metadata.setFriendlyName("Options");
        metadata.setGroup("");
        metadata.setExplicitFieldType(SupportedFieldType.UNKNOWN);
        metadata.setProminent(true);
        metadata.setVisibility(VisibilityEnum.FORM_HIDDEN);
        metadata.setBroadleafEnumeration("");
        metadata.setReadOnly(true);
        metadata.setRequiredOverride(false);

        return metadata;
    }

    /**
     * Returns a {@link Property} filled out with a delimited list of the <b>values</b> that are passed in. This should be
     * invoked on a fetch and the returned property should be added to the fetched {@link Entity} dto.
     * 
     * @param values
     * @return
     * @see {@link #createConsolidatedOptionField(Class)};
     */
    public static Property getConsolidatedOptionProperty(List<ProductOptionValue> values) {
        Property optionValueProperty = new Property();
        optionValueProperty.setName(CONSOLIDATED_PRODUCT_OPTIONS_FIELD_NAME);

        //order the values by the display order of their correspond product option
        //        Collections.sort(values, new Comparator<ProductOptionValue>() {
        //
        //            @Override
        //            public int compare(ProductOptionValue value1, ProductOptionValue value2) {
        //                return new CompareToBuilder().append(value1.getProductOption().getDisplayOrder(),
        //                        value2.getProductOption().getDisplayOrder()).toComparison();
        //            }
        //        });

        ArrayList<String> stringValues = new ArrayList<String>();
        CollectionUtils.collect(values, new Transformer() {

            @Override
            public Object transform(Object input) {
                return ((ProductOptionValue) input).getAttributeValue();
            }
        }, stringValues);

        optionValueProperty.setValue(StringUtils.join(stringValues, CONSOLIDATED_PRODUCT_OPTIONS_DELIMETER));
        return optionValueProperty;
    }

    /**
     * <p>Creates an individual property for the specified product option. This should set up an enum field whose values will
     * be the option values for this option.  This is useful when you would like to display each product option in as its
     * own field in a grid so that you can further filter by product option values.</p>
     * <p>In order for these fields to be utilized property on the fetch, in the GWT frontend you must use the
     * {@link SkuBasicClientEntityModule} for your datasource.</p>
     * 
     * @param option
     * @param order
     * @return
     */
    public static FieldMetadata createIndividualOptionField(ProductOption option, int order) {
        BasicFieldMetadata metadata = new BasicFieldMetadata();
        metadata.setFieldType(SupportedFieldType.EXPLICIT_ENUMERATION);
        metadata.setMutable(true);
        metadata.setInheritedFromType(SkuImpl.class.getName());
        metadata.setAvailableToTypes(new String[] { SkuImpl.class.getName() });
        metadata.setForeignKeyCollection(false);
        metadata.setMergedPropertyType(MergedPropertyType.PRIMARY);

        //Set up the enumeration based on the product option values
        String[][] optionValues = new String[option.getAllowedValues().size()][2];
        for (int i = 0; i < option.getAllowedValues().size(); i++) {
            ProductOptionValue value = option.getAllowedValues().get(i);
            optionValues[i][0] = value.getId().toString();
            optionValues[i][1] = value.getAttributeValue();
        }
        metadata.setEnumerationValues(optionValues);

        metadata.setName(PRODUCT_OPTION_FIELD_PREFIX + option.getId());
        metadata.setFriendlyName(option.getLabel());
        metadata.setGroup("Options");
        metadata.setGroupOrder(-1);
        metadata.setOrder(order);
        metadata.setExplicitFieldType(SupportedFieldType.UNKNOWN);
        metadata.setProminent(false);
        metadata.setVisibility(VisibilityEnum.GRID_HIDDEN);
        metadata.setBroadleafEnumeration("");
        metadata.setReadOnly(false);
        metadata.setRequiredOverride(BooleanUtils.isFalse(option.getRequired()));

        return metadata;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DynamicResultSet fetch(PersistencePackage persistencePackage, CriteriaTransferObject cto, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        try {
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            //get the default properties from Sku and its subclasses
            Map<String, FieldMetadata> originalProps = helper.getSimpleMergedProperties(Sku.class.getName(), persistencePerspective);

            //Pull back the Skus based on the criteria from the client
            BaseCtoConverter ctoConverter = helper.getCtoConverter(persistencePerspective, cto,
                    ceilingEntityFullyQualifiedClassname, originalProps, new SkuPropertiesFilterCriterionProvider());
            PersistentEntityCriteria queryCriteria = ctoConverter.convert(cto, ceilingEntityFullyQualifiedClassname);

            //allow subclasses to provide additional criteria before executing the query
            applyProductOptionValueCriteria(queryCriteria, cto, persistencePackage, null);
            applyAdditionalFetchCriteria(queryCriteria, cto, persistencePackage);

            Criteria skuListCriteria = getSkuCriteria(queryCriteria, Class.forName(persistencePackage.getCeilingEntityFullyQualifiedClassname()), dynamicEntityDao, null);
            List<Serializable> records = skuListCriteria.list();
            //Convert Skus into the client-side Entity representation
            Entity[] payload = helper.getRecords(originalProps, records);

            //grab back the total results
            PersistentEntityCriteria countCriteria = helper.getCountCriteria(persistencePackage, cto, ctoConverter);
            //again, apply the additional criteria if any to the count criteria as well
            applyProductOptionValueCriteria(countCriteria, cto, persistencePackage, null);
            applyAdditionalFetchCriteria(countCriteria, cto, persistencePackage);

            Criteria skuCountCriteria = getSkuCriteria(countCriteria, Class.forName(persistencePackage.getCeilingEntityFullyQualifiedClassname()), dynamicEntityDao, null);
            int totalRecords = dynamicEntityDao.rowCount(skuCountCriteria);

            //Communicate to the front-end to allow form editing for all of the product options available for the current
            //Product to allow inserting Skus one at a time
            ClassMetadata metadata = new ClassMetadata();
            if (cto.get("product").getFilterValues().length > 0) {
                Long productId = Long.parseLong(cto.get("product").getFilterValues()[0]);
                Product product = catalogService.findProductById(productId);
                List<Property> properties = new ArrayList<Property>();
                for (ProductOption option : product.getProductOptions()) {
                    Property optionProperty = new Property();
                    optionProperty.setName(PRODUCT_OPTION_FIELD_PREFIX + option.getId());
                    properties.add(optionProperty);
                }
                metadata.setProperties(properties.toArray(new Property[0]));
            }

            //Now fill out the relevant properties for the product options for the Skus that were returned
            for (int i = 0; i < records.size(); i++) {
                Sku sku = (Sku) records.get(i);
                Entity entity = payload[i];

                //In the list of Skus from the database, it is possible that some important properties (like name,
                //description, etc) are actually null. This isn't a problem on the site because the getters for Sku
                //use the defaultSku on this Sku's product if they are null. Nothing like this happens for displaying the
                //list of Skus in the admin, however, because everything is done via property reflection. Let's attempt to
                //actually call the getters (using bean utils) if the properties are null from this Sku, so that values
                //actually come back from the defaultSku (just like on the site)
                for (Property property : entity.getProperties()) {
                    if (StringUtils.isEmpty(property.getValue())) {
                        String propertyName = property.getName();
                        String strValue = SkuCustomPersistenceHandler.getStringValueFromGetter(propertyName, sku, helper);
                        property.setValue(strValue);
                    }
                }

                List<ProductOptionValue> optionValues = sku.getProductOptionValues();
                for (ProductOptionValue value : optionValues) {
                    Property optionProperty = new Property();
                    optionProperty.setName(PRODUCT_OPTION_FIELD_PREFIX + value.getProductOption().getId());
                    optionProperty.setValue(value.getId().toString());
                    entity.addProperty(optionProperty);
                }

                if (CollectionUtils.isNotEmpty(optionValues)) {
                    entity.addProperty(getConsolidatedOptionProperty(optionValues));
                }
            }

            return new DynamicResultSet(metadata, payload, totalRecords);
        } catch (Exception e) {
            LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to perform fetch for entity: " + ceilingEntityFullyQualifiedClassname, e);
        }
    }

    /**
     * Returns the Hibernate criteria with the proper table aliases based on the PersistentEntityCriteria representation.
     * Should be used in a fetch for both the row count criteria and actual fetch criteria. This will also apply the given
     * CTO onto returned Hibernate criteria
     * 
     * This can also be used if you are attempting to filter on an object that could contain a Sku 'ToOne'
     * relationship that might need to be filtered on. For instance, InventoryImpl has a 'Sku' property called 'sku'. In
     * this scenario, the <b>skuPropertyPrefix</b> would be 'sku'.
     * 
     * @return
     */
    public static Criteria getSkuCriteria(PersistentEntityCriteria criteria,
                                      Class entityClass,
                                      DynamicEntityDao deDao,
                                      String skuPropertyPrefix) {
        Criteria hibernateCriteria = deDao.createCriteria(entityClass);
        //Join these with left joins so that I get default Skus (that do not have this relationship) back as well
        if (StringUtils.isNotEmpty(skuPropertyPrefix)) {
            hibernateCriteria.createAlias(skuPropertyPrefix, skuPropertyPrefix);
            skuPropertyPrefix += ".";
        }
        if (skuPropertyPrefix == null) {
            skuPropertyPrefix = "";
        }
        hibernateCriteria.createAlias(skuPropertyPrefix + "product", "product", CriteriaSpecification.LEFT_JOIN)
                         .createAlias("product.defaultSku", "defaultSku", CriteriaSpecification.LEFT_JOIN);
        criteria.apply(hibernateCriteria);
        return hibernateCriteria;
    }

    /**
     * Under the covers this uses PropertyUtils to call the getter of the property name for the given Sku, then undergoes
     * conversion according to the formatters from <b>helper</b>.  This also attempts to only get the first-level properties
     * so it does not try to get values for things like 'sku.weight.weight', but only 'sku.weight'.
     * 
     * @param propertyName - name of the property in relation to <b>sku</b>. Thus, if you are attempting to bring back the
     * sku name, the propertyName should just be 'name' rather than 'sku.name'. 
     * @param sku the Sku instance to get the value from
     * @param helper a RecordHelper to help convert decimals and dates to their string equivalents
     * @return the String value from <b>sku</b> for <b>propertyName</b>
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     */
    public static String getStringValueFromGetter(String propertyName, Sku sku, RecordHelper helper) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        //only attempt the getter on the first-level Sku properties
        if (propertyName.contains(".")) {
            StringTokenizer tokens = new StringTokenizer(propertyName, ".");
            propertyName = tokens.nextToken();
        }

        Object value = PropertyUtils.getProperty(sku, propertyName);

        String strVal;
        if (value == null) {
            strVal = null;
        } else {
            if (Date.class.isAssignableFrom(value.getClass())) {
                strVal = dateFormat.format((Date) value);
            } else if (Timestamp.class.isAssignableFrom(value.getClass())) {
                strVal = dateFormat.format(new Date(((Timestamp) value).getTime()));
            } else if (Calendar.class.isAssignableFrom(value.getClass())) {
                strVal = dateFormat.format(((Calendar) value).getTime());
            } else if (Double.class.isAssignableFrom(value.getClass())) {
                strVal = helper.getDecimalFormatter().format(value);
            } else if (BigDecimal.class.isAssignableFrom(value.getClass())) {
                strVal = helper.getDecimalFormatter().format(((BigDecimal) value).doubleValue());
            } else {
                strVal = value.toString();
            }
        }

        return strVal;
    }

    public static void applyProductOptionValueCriteria(PersistentEntityCriteria queryCriteria, CriteriaTransferObject cto, PersistencePackage persistencePackage, String skuPropertyPrefix) {

        //if the front
        final List<Long> productOptionValueFilterIDs = new ArrayList<Long>();
        for (String filterProperty : cto.getPropertyIdSet()) {
            if (filterProperty.startsWith(PRODUCT_OPTION_FIELD_PREFIX)) {
                FilterAndSortCriteria criteria = cto.get(filterProperty);
                productOptionValueFilterIDs.add(Long.parseLong(criteria.getFilterValues()[0]));
            }
        }

        //also determine if there is a consolidated POV query
        final List<String> productOptionValueFilterValues = new ArrayList<String>();
        FilterAndSortCriteria consolidatedCriteria = cto.get(CONSOLIDATED_PRODUCT_OPTIONS_FIELD_NAME);
        if (consolidatedCriteria.getFilterValues().length > 0) {
            //the criteria in this case would be a semi-colon delimeter value list
            productOptionValueFilterValues.addAll(Arrays.asList(StringUtils.split(consolidatedCriteria.getFilterValues()[0], CONSOLIDATED_PRODUCT_OPTIONS_DELIMETER)));
        }
        
        AssociationPath path;
        if (StringUtils.isNotEmpty(skuPropertyPrefix)) {
            path = new AssociationPath(new AssociationPathElement(skuPropertyPrefix), new AssociationPathElement("productOptionValues"));
        } else {
            path = new AssociationPath(new AssociationPathElement("productOptionValues"));
        }
        
        if (productOptionValueFilterIDs.size() > 0) {
            ((NestedPropertyCriteria) queryCriteria).add(
                    new FilterCriterion(path,
                            "id",
                            productOptionValueFilterIDs,
                            false,
                            new SimpleFilterCriterionProvider(FilterDataStrategy.DIRECT, 1) {

                                @Override
                                @SuppressWarnings("unchecked")
                                public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                                    return Restrictions.in(targetPropertyName, (List<Long>) directValues[0]);
                                }
                            }));
        }
        if (productOptionValueFilterValues.size() > 0) {
            ((NestedPropertyCriteria) queryCriteria).add(
                    new FilterCriterion(path,
                            "attributeValue",
                            productOptionValueFilterValues,
                            false,
                            new SimpleFilterCriterionProvider(FilterDataStrategy.DIRECT, 1) {

                                @Override
                                @SuppressWarnings("unchecked")
                                public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                                    return Restrictions.in(targetPropertyName, (List<String>) directValues[0]);
                                }
                            }));
        }
    }

    /**
     * <p>Available override point for subclasses if they would like to add additional criteria via the queryCritiera. At the
     * point that this method has been called, criteria from the frontend has already been applied, thus allowing you to
     * override from there as well.</p>
     * <p>Subclasses that choose to override this should also call this super method so that correct filter criteria
     * can be applied for product option values</p>
     * 
     */
    public void applyAdditionalFetchCriteria(PersistentEntityCriteria queryCriteria, CriteriaTransferObject cto, PersistencePackage persistencePackage) {
        //unimplemented
    }

    @Override
    public Entity add(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            //Fill out the Sku instance from the form
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Sku adminInstance = (Sku) Class.forName(entity.getType()[0]).newInstance();
            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(Sku.class.getName(), persistencePerspective);
            adminInstance = (Sku) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);

            //Verify that there isn't already a Sku for this particular product option value combo
            Entity errorEntity = validateUniqueProductOptionValueCombination(adminInstance.getProduct(),
                                                                             getProductOptionProperties(entity),
                                                                             null);
            if (errorEntity != null) {
                return errorEntity;
            }

            //persist the newly-created Sku
            adminInstance = (Sku) dynamicEntityDao.persist(adminInstance);

            //associate the product option values
            associateProductOptionValuesToSku(entity, adminInstance, dynamicEntityDao);

            //After associating the product option values, save off the Sku
            adminInstance = (Sku) dynamicEntityDao.merge(adminInstance);

            //Fill out the DTO and add in the product option value properties to it
            Entity result = helper.getRecord(adminProperties, adminInstance, null, null);
            for (Property property : getProductOptionProperties(entity)) {
                result.addProperty(property);
            }
            return result;
        } catch (Exception e) {
            LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to perform fetch for entity: " + Sku.class.getName(), e);
        }
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = persistencePackage.getEntity();
        try {
            //Fill out the Sku instance from the form
            PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
            Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(Sku.class.getName(), persistencePerspective);
            Object primaryKey = helper.getPrimaryKey(entity, adminProperties);
            Sku adminInstance = (Sku) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);
            adminInstance = (Sku) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);

            //Verify that there isn't already a Sku for this particular product option value combo
            Entity errorEntity = validateUniqueProductOptionValueCombination(adminInstance.getProduct(),
                                                                            getProductOptionProperties(entity),
                                                                            adminInstance);
            if (errorEntity != null) {
                return errorEntity;
            }

            associateProductOptionValuesToSku(entity, adminInstance, dynamicEntityDao);

            adminInstance = (Sku) dynamicEntityDao.merge(adminInstance);

            //Fill out the DTO and add in the product option value properties to it
            Entity result = helper.getRecord(adminProperties, adminInstance, null, null);
            for (Property property : getProductOptionProperties(entity)) {
                result.addProperty(property);
            }
            return result;
        } catch (Exception e) {
            LOG.error("Unable to execute persistence activity", e);
            throw new ServiceException("Unable to perform fetch for entity: " + Sku.class.getName(), e);
        }
    }

    /**
     * This initially removes all of the product option values that are currently related to the Sku and then re-associates
     * the {@link PrdouctOptionValue}s
     * @param entity
     * @param adminInstance
     */
    protected void associateProductOptionValuesToSku(Entity entity, Sku adminInstance, DynamicEntityDao dynamicEntityDao) {
        //Get the list of product option value ids that were selected from the form
        List<Long> productOptionValueIds = new ArrayList<Long>();
        for (Property property : getProductOptionProperties(entity)) {
            productOptionValueIds.add(Long.parseLong(property.getValue()));
        }

        //remove the current list of product option values from the Sku
        if (adminInstance.getProductOptionValues().size() > 0) {
            adminInstance.getProductOptionValues().clear();
            dynamicEntityDao.merge(adminInstance);
        }

        //Associate the product option values from the form with the Sku
        List<ProductOption> productOptions = adminInstance.getProduct().getProductOptions();
        for (ProductOption option : productOptions) {
            for (ProductOptionValue value : option.getAllowedValues()) {
                if (productOptionValueIds.contains(value.getId())) {
                    adminInstance.getProductOptionValues().add(value);
                }
            }
        }
    }

    protected List<Property> getProductOptionProperties(Entity entity) {
        List<Property> productOptionProperties = new ArrayList<Property>();
        for (Property property : entity.getProperties()) {
            if (property.getName().startsWith(PRODUCT_OPTION_FIELD_PREFIX)) {
                productOptionProperties.add(property);
            }
        }
        return productOptionProperties;
    }

    /**
     * Ensures that the given list of {@link ProductOptionValue} IDs is unique for the given {@link Product}
     * @param product
     * @param productOptionValueIds
     * @param currentSku - for update operations, this is the current Sku that is being updated; should be excluded from
     * attempting validation
     * @return <b>null</b> if successfully validation, the error entity otherwise
     */
    protected Entity validateUniqueProductOptionValueCombination(Product product, List<Property> productOptionProperties, Sku currentSku) {
        List<Long> productOptionValueIds = new ArrayList<Long>();
        for (Property property : productOptionProperties) {
            productOptionValueIds.add(Long.parseLong(property.getValue()));
        }

        boolean validated = true;
        for (Sku sku : product.getAdditionalSkus()) {
            if (currentSku == null || !sku.getId().equals(currentSku.getId())) {
                List<Long> testList = new ArrayList<Long>();
                for (ProductOptionValue optionValue : sku.getProductOptionValues()) {
                    testList.add(optionValue.getId());
                }

                if (productOptionValueIds.containsAll(testList) && productOptionValueIds.size() == testList.size()) {
                    validated = false;
                    break;
                }
            }
        }

        if (!validated) {
            Entity errorEntity = new Entity();
            errorEntity.setValidationFailure(true);
            for (Property productOptionProperty : productOptionProperties) {
                errorEntity.addValidationError(productOptionProperty.getName(), "uniqueSkuError");
            }
            return errorEntity;
        }
        return null;
    }

}
