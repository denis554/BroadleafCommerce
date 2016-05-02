/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.search.service.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.broadleafcommerce.common.extension.ExtensionHandler;
import org.broadleafcommerce.common.extension.ExtensionResultHolder;
import org.broadleafcommerce.common.extension.ExtensionResultStatusType;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.search.domain.FieldEntity;
import org.broadleafcommerce.core.search.domain.IndexField;
import org.broadleafcommerce.core.search.domain.IndexFieldType;
import org.broadleafcommerce.core.search.domain.SearchCriteria;
import org.broadleafcommerce.core.search.domain.SearchFacet;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.domain.SearchFacetRange;
import org.broadleafcommerce.core.search.domain.solr.FieldType;

import java.util.List;
import java.util.Map;

/**
 * @author Andre Azzolini (apazzolini), bpolster
 */                                      
public interface SolrSearchServiceExtensionHandler extends ExtensionHandler {

    /**
     * Returns a prefix if required for the passed in searchable field. 
     */
    public ExtensionResultStatusType buildPrefixListForIndexField(IndexField field, FieldType fieldType,
            List<String> prefixList);

    /**
     * Builds the search facet ranges for the provided dto.
     * 
     * @param context
     * @param dto
     * @param ranges
     */
    public ExtensionResultStatusType filterSearchFacetRanges(SearchFacetDTO dto, List<SearchFacetRange> ranges);

    /**
     * Provides an extension point to modify the SolrQuery.
     * @param context
     * @param query
     * @param qualifiedSolrQuery
     * @param facets
     * @param searchCriteria
     * @param defaultSort
     */
    public ExtensionResultStatusType modifySolrQuery(SolrQuery query, String qualifiedSolrQuery,
            List<SearchFacetDTO> facets, SearchCriteria searchCriteria, String defaultSort);

    /**
     * In certain scenarios, the requested category id might not be the one that should be used in Solr.
     * If this method returns {@link ExtensionResultStatusType#HANDLED}, the value placed in the 0th element
     * in the returnContainer should be used.
     * 
     * @param category
     * @param returnContainer
     * @return the extension result status type
     */
    public ExtensionResultStatusType getCategoryId(Category category, Long[] returnContainer);

    /**
     * <p>
     * Finds and adds the query fields for the given search field and searchable field type. This method should only ADD
     * to the list within the <b>queryFieldsResult</b> parameters.
     *
     * <p>
     * Most implementations of this will need to invoke {@link SolrHelperService#getPropertyNameForIndexField(IndexField, FieldType)}
     * in order to return the right value to populate in the <b>queryFieldsResult</b>. If the returned result is
     * {@link ExtensionResultStatusType#NOT_HANDLED} then the default behavior is to only do that.
     *
     *
     * @param query
     * @param searchCriteria
     *@param indexFieldType the field type of the field
     * @param queryFieldsResult the binding result that contains the list of query fields, only add to this   @return the result of the handler, if NOT_HANDLED, then no query fields were added
     * @see {@link SolrHelperService#getPropertyNameForIndexField(IndexField, FieldType)}
     */
    public ExtensionResultStatusType getQueryField(SolrQuery query, SearchCriteria searchCriteria, IndexFieldType indexFieldType, ExtensionResultHolder<List<String>> queryFieldsResult);

    /**
     * <p>Modifies the product search results from a Solr query</p>
     *
     * <p>The parameters passed into this method should be assumed to be sorted identically and match one to one.</p>
     *
     * @param responseDocuments the response documents from Solr
     * @param products the products that tie to the response documents
     * @return the result of the handler, if NOT_HANDLED, then no changes where made
     */
    public ExtensionResultStatusType modifySearchResults(List<SolrDocument> responseDocuments, List<Product> products);

    /**
     * Populates the List of SearchFacet's, or else returns NOT_HANDLED
     *
     * @param searchFacets the List to populate
     * @return the result of the handler
     */
    public ExtensionResultStatusType getSearchFacets(List<SearchFacet> searchFacets);

    /**
     * Attaches the given dto to the given query, if possible
     *
     * @param query the SolrQuery to attach the facet to
     * @param key the key
     * @param dto
     * @return the result of the handler
     */
    public ExtensionResultStatusType attachFacet(SolrQuery query, String indexField, SearchFacetDTO dto);

    /**
     * Attaches any additional facet results to the namedFacetMap if they exist. This should only attach facets if they do
     * not already have result DTOs
     *
     * @param namedFacetMap
     * @param response
     * @return the result of the handler
     */
    public ExtensionResultStatusType setFacetResults(Map<String, SearchFacetDTO> namedFacetMap, QueryResponse response);

    /**
     * Builds the active facet filter query string for the given entity type and values. Typically this is only used when you
     * are doing faceting on nested child documents.
     *
     * @param entityType
     * @param solrKey
     * @param selectedValues
     * @param valueStrings
     * @return
     */
    public ExtensionResultStatusType buildActiveFacetFilter(FieldEntity entityType, String solrKey, String[] selectedValues, List<String> valueStrings);

    /**
     * Adds any additional category ids to filter by when category browsing or searching.
     *
     * @param category the current Category we are browsing or searching on
     * @param searchCriteria the criteria for the current query
     * @param categoryIds the category IDs we are going to filter on (this already includes the current category's ID)
     * @return NOT_HANDLED if no IDs were added, and HANDLED_CONTINUE if there were
     */
    public ExtensionResultStatusType addAdditionalCategoryIds(Category category, SearchCriteria searchCriteria, List<Long> categoryIds);

    /**
     * Populates the List of SearchFacet's for the given Category, or else returns NOT_HANDLED
     *
     * @param category
     * @param searchFacets
     * @return
     */
    public ExtensionResultStatusType getCategorySearchFacets(Category category, List<SearchFacet> searchFacets);

    /**
     * Populated the List of searchable IndexField's that will be used in building the query fields (qf) for a Solr query.
     * It is assumed that if the result of this call is NOT_HANDLED, then SolrSearchService will follow it's default behavior
     * for populating IndexFields.
     *
     * @param fields the List to be populated.
     * @return HANDLED_CONTINUE if it added field, NOT_HANDLED otherwise
     */
    public ExtensionResultStatusType getSearchableIndexFields(List<IndexField> fields);
}
