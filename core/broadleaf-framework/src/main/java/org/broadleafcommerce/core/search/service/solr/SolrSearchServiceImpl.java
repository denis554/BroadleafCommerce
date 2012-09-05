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

package org.broadleafcommerce.core.search.service.solr;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.catalog.dao.ProductDao;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.search.dao.FieldDao;
import org.broadleafcommerce.core.search.dao.SearchFacetDao;
import org.broadleafcommerce.core.search.domain.CategorySearchFacet;
import org.broadleafcommerce.core.search.domain.Field;
import org.broadleafcommerce.core.search.domain.ProductSearchCriteria;
import org.broadleafcommerce.core.search.domain.ProductSearchResult;
import org.broadleafcommerce.core.search.domain.SearchFacet;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.domain.SearchFacetRange;
import org.broadleafcommerce.core.search.domain.SearchFacetResultDTO;
import org.broadleafcommerce.core.search.domain.solr.FieldType;
import org.broadleafcommerce.core.search.service.SearchService;
import org.broadleafcommerce.core.util.StopWatch;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An implementation of SearchService that uses solr
 * 
 * @author Andre Azzolini (apazzolini)
 */
public class SolrSearchServiceImpl implements SearchService {
    private static final Log LOG = LogFactory.getLog(SolrSearchServiceImpl.class);
    protected static final String GLOBAL_FACET_TAG_FIELD = "a";
    
	@Resource(name = "blProductDao")
	protected ProductDao productDao;
	
	@Resource(name = "blFieldDao")
	protected FieldDao fieldDao;
	
	@Resource(name = "blSearchFacetDao")
	protected SearchFacetDao searchFacetDao;
	
	protected SolrServer server;

	public SolrSearchServiceImpl(String solrHome) throws IOException, ParserConfigurationException, SAXException {
		System.setProperty("solr.solr.home", solrHome);
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer = initializer.initialize();
		EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
		this.server = server;
	}
	
	public SolrSearchServiceImpl(SolrServer server) {
		this.server = server;
	}
	
	@Override
    @Transactional("blTransactionManager")
	public void rebuildIndex() throws ServiceException, IOException {
		LOG.info("Rebuilding the solr index...");
		StopWatch s = new StopWatch();
		
		List<Product> products = productDao.readAllActiveProducts(SystemTime.asDate());
		List<Field> fields = fieldDao.readAllProductFields();
		
	    Collection<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		for (Product product : products) {
			SolrInputDocument document = new SolrInputDocument();
			
			// Add fields that are present on all products
			document.addField("id", product.getId());
			for (Category category : product.getAllParentCategories()) {
				document.addField("category", category.getId());
				
				String categorySortField = getCategorySortField(category);
				int listIndex = category.getAllProducts().indexOf(product);
				document.addField(categorySortField, listIndex);
			}
			
			// Add data-driven user specified searchable fields
			List<String> addedProperties = new ArrayList<String>();
			List<String> copyFieldValue = new ArrayList<String>();
			for (Field field : fields) {
				try {
					String propertyName = field.getPropertyName();
					if (propertyName.contains("productAttributes.")) {
						propertyName = convertToMappedProperty(propertyName, "productAttributes", 
								"mappedProductAttributes");
					}
					Object propertyValue = PropertyUtils.getProperty(product, propertyName);
					
					// Index the searchable fields
					for (FieldType searchableFieldType : field.getSearchableFieldTypes()) {
						String solrPropertyName = field.getPropertyName() + "_" + searchableFieldType.getType();
						document.addField(solrPropertyName, propertyValue);
						addedProperties.add(solrPropertyName);
						copyFieldValue.add(propertyValue.toString());
					}
					
					// Index the faceted field type as well
					FieldType facetFieldType = field.getFacetFieldType();
					if (facetFieldType != null) {
						String solrFacetPropertyName = field.getPropertyName() + "_" + facetFieldType.getType();
						if (!addedProperties.contains(solrFacetPropertyName)) {
							document.addField(solrFacetPropertyName, propertyValue);
						}
					}
				} catch (Exception e) {
					LOG.debug("Could not get value for property[" + field.getQualifiedFieldName() + "] for product id["
							+ product.getId() + "]");
				}
			}
			document.addField("searchable", StringUtils.join(copyFieldValue, " "));
			documents.add(document);
		}
		
	    try {
	    	server.deleteByQuery("*:*");
	    	server.commit();
	    	
		    server.add(documents);
		    server.commit();
	    } catch (SolrServerException e) {
	    	throw new ServiceException("Could not rebuild index", e);
	    }
	    
	    LOG.info("Finished rebuilding the solr index in " + s.toLapString());
	}
	
	@Override
	public ProductSearchResult findProductsByCategory(Category category, ProductSearchCriteria searchCriteria) 
			throws ServiceException {
		List<SearchFacetDTO> facets = getCategoryFacets(category);
		String query = "category:" + category.getId();
		return findProducts(query, facets, searchCriteria, getCategorySortField(category) + " asc");
	}
	
	@Override
	public ProductSearchResult findProductsByQuery(String query, ProductSearchCriteria searchCriteria) 
			throws ServiceException {
		List<SearchFacetDTO> facets = getSearchFacets();
		query = "searchable:*" + query + "*"; // Surrounding with * allows partial word matches
		return findProducts(query, facets, searchCriteria, null);
	}
	
	@Override
	public List<SearchFacetDTO> getSearchFacets() {
		return buildSearchFacetDTOs(searchFacetDao.readAllSearchFacets());
	}

	@Override
	public List<SearchFacetDTO> getCategoryFacets(Category category) {
		List<CategorySearchFacet> categorySearchFacets = category.getCumulativeSearchFacets();
		
		List<SearchFacet> searchFacets = new ArrayList<SearchFacet>();
		for (CategorySearchFacet categorySearchFacet : categorySearchFacets) {
			searchFacets.add(categorySearchFacet.getSearchFacet());
		}
		
		return buildSearchFacetDTOs(searchFacets);
	}
	
	/**
	 * Given a qualified solr query string (such as "category:2002"), actually performs a solr search. It will
	 * take into considering the search criteria to build out facets / pagination / sorting.
	 * 
	 * @param qualifiedSolrQuery
	 * @param facets
	 * @param searchCriteria
	 * @return the ProductSearchResult of the search
	 * @throws ServiceException
	 */
	protected ProductSearchResult findProducts(String qualifiedSolrQuery, List<SearchFacetDTO> facets, 
			ProductSearchCriteria searchCriteria, String defaultSort) throws ServiceException {
		Map<String, SearchFacetDTO> namedFacetMap = getNamedFacetMap(facets, searchCriteria);
		
		// Build the basic query
	    SolrQuery solrQuery = new SolrQuery()
	    	.setQuery(qualifiedSolrQuery)
    		.setRows(searchCriteria.getPageSize())
    		.setStart((searchCriteria.getPage() - 1) * searchCriteria.getPageSize());
	    
	    // Attach additional restrictions
	    attachSortClause(solrQuery, searchCriteria, defaultSort);
	    attachActiveFacetFilters(solrQuery, namedFacetMap, searchCriteria);
	    attachFacets(solrQuery, namedFacetMap);

	    // Query solr
	    QueryResponse response;
	    try {
	    	response = server.query(solrQuery);
	    } catch (SolrServerException e) {
	    	throw new ServiceException("Could not perform search", e);
	    }
	    
	    // Get the facets
	    setFacetResults(namedFacetMap, response);
	    sortFacetResults(namedFacetMap);
	    	
	    // Get the products
	    List<Product> products = getProducts(response);
	    
	    ProductSearchResult result = new ProductSearchResult();
	    result.setFacets(facets);
	    result.setProducts(products);
	    setPagingAttributes(result, response, searchCriteria);
	    return result;
	}
	
	
	/**
	 * Sets up the sorting criteria. This will support sorting by multiple fields at a time
	 * 
	 * @param query
	 * @param searchCriteria
	 */
	protected void attachSortClause(SolrQuery query, ProductSearchCriteria searchCriteria, String defaultSort) {
		Map<String, String> solrFieldKeyMap = getSolrFieldKeyMap(searchCriteria);
		
		String sortQuery = searchCriteria.getSortQuery();
		if (StringUtils.isBlank(sortQuery)) {
			sortQuery = defaultSort;
		}
		
		if (StringUtils.isNotBlank(sortQuery)) {
			String[] sortFields = sortQuery.split(",");
			for (String sortField : sortFields) {
				String field = sortField.split(" ")[0];
				if (solrFieldKeyMap.containsKey(field)) {
					field = solrFieldKeyMap.get(field);
				}
				ORDER order = "desc".equals(sortField.split(" ")[1]) ? ORDER.desc : ORDER.asc;
				
				if (field != null) {
					query.addSortField(field, order);
				}
			}
		}
	}
	
	/**
	 * Restricts the query by adding active facet filters.
	 * 
	 * @param query
	 * @param namedFacetMap
	 * @param searchCriteria
	 */
	protected void attachActiveFacetFilters(SolrQuery query, Map<String, SearchFacetDTO> namedFacetMap, 
			ProductSearchCriteria searchCriteria) {
		for (Entry<String, String[]> entry : searchCriteria.getFilterCriteria().entrySet()) {
			String solrKey = null;
			for (Entry<String, SearchFacetDTO> dtoEntry : namedFacetMap.entrySet()) {
				if (dtoEntry.getValue().getFacet().getField().getAbbreviation().equals(entry.getKey())) {
					solrKey = dtoEntry.getKey();
					dtoEntry.getValue().setActive(true);
				}
			}
			
			if (solrKey != null) {
				String solrTag = getSolrFieldTag(GLOBAL_FACET_TAG_FIELD, "tag");
				
				String[] selectedValues = entry.getValue().clone();
				for (int i = 0; i < selectedValues.length; i++) {
					if (selectedValues[i].contains("range[")) {
						String rangeValue = selectedValues[i].substring(selectedValues[i].indexOf('[') + 1, 
								selectedValues[i].indexOf(']'));
						String[] rangeValues = StringUtils.split(rangeValue, ':');
						if (rangeValues[1].equals("null")) {
							rangeValues[1] = "*";
						}
						selectedValues[i] = solrKey + ":[" + rangeValues[0] + " TO " + rangeValues[1] + "]";
					} else {
						selectedValues[i] = solrKey + ":\"" + selectedValues[i] + "\"";
					}
				}
				String valueString = StringUtils.join(selectedValues, " OR ");
				
				StringBuilder sb = new StringBuilder();
				sb.append(solrTag).append("(").append(valueString).append(")");
				
				query.addFilterQuery(sb.toString());
			}
		}
	}
	
	/**
	 * Notifies solr about which facets you want it to determine results and counts for
	 * 
	 * @param query
	 * @param namedFacetMap
	 */
	protected void attachFacets(SolrQuery query, Map<String, SearchFacetDTO> namedFacetMap) {
		query.setFacet(true);
		for (Entry<String, SearchFacetDTO> entry : namedFacetMap.entrySet()) {
			SearchFacetDTO dto = entry.getValue();
			String facetTagField = entry.getValue().isActive() ? GLOBAL_FACET_TAG_FIELD : entry.getKey();
			if (dto.getFacet().getSearchFacetRanges().size() > 0) {
				for (SearchFacetRange range : dto.getFacet().getSearchFacetRanges()) {
					query.addFacetQuery(getSolrTaggedFieldString(entry.getKey(), facetTagField, "ex", range));
				}
			} else {
				query.addFacetField(getSolrTaggedFieldString(entry.getKey(), facetTagField, "ex", null));
			}
		}
	}
	
	/**
	 * Builds out the DTOs for facet results from the search. This will then be used by the view layer to
	 * display which values are avaialble given the current constraints as well as the count of the values.
	 * 
	 * @param namedFacetMap
	 * @param response
	 */
	protected void setFacetResults(Map<String, SearchFacetDTO> namedFacetMap, QueryResponse response) {
	    if (response.getFacetFields() != null) {
		    for (FacetField facet : response.getFacetFields()) {
		    	String facetFieldName = facet.getName();
		    	SearchFacetDTO facetDTO = namedFacetMap.get(facetFieldName);
		    	
		    	for (Count value : facet.getValues()) {
	    			SearchFacetResultDTO resultDTO = new SearchFacetResultDTO();
	    			resultDTO.setFacet(facetDTO.getFacet());
	    			resultDTO.setQuantity(new Long(value.getCount()).intValue());
	    			resultDTO.setValue(value.getName());
	    			facetDTO.getFacetValues().add(resultDTO);
		    	}
		    }
	    }
	    
	    if (response.getFacetQuery() != null) {
		    for (Entry<String, Integer> entry : response.getFacetQuery().entrySet()) {
		    	String key = entry.getKey();
	    		String facetFieldName = key.substring(key.indexOf("}") + 1, key.indexOf(':'));
	    		SearchFacetDTO facetDTO = namedFacetMap.get(facetFieldName);
	    		
	    		String minValue = key.substring(key.indexOf("[") + 1, key.indexOf(" TO"));
	    		String maxValue = key.substring(key.indexOf(" TO ") + 4, key.indexOf("]"));
	    		if (maxValue.equals("*")) {
	    			maxValue = null;
	    		}
	    		
	    		SearchFacetResultDTO resultDTO = new SearchFacetResultDTO();
	    		resultDTO.setFacet(facetDTO.getFacet());
	    		resultDTO.setQuantity(entry.getValue());
	    		resultDTO.setMinValue(new BigDecimal(minValue));
	    		resultDTO.setMaxValue(maxValue == null ? null : new BigDecimal(maxValue));
	    		
	    		facetDTO.getFacetValues().add(resultDTO);
		    }	
	    }
	}
	
	/**
	 * Invoked to sort the facet results. This method will use the natural sorting of the value attribute of the
	 * facet (or, if value is null, the minValue of the facet result). Override this method to customize facet
	 * sorting for your given needs.
	 * 
	 * @param namedFacetMap
	 */
	protected void sortFacetResults(Map<String, SearchFacetDTO> namedFacetMap) {
	    for (Entry<String, SearchFacetDTO> entry : namedFacetMap.entrySet()) {
    		Collections.sort(entry.getValue().getFacetValues(), new Comparator<SearchFacetResultDTO>() {
				public int compare(SearchFacetResultDTO o1, SearchFacetResultDTO o2) {
					if (o1.getValue() != null && o2.getValue() != null) {
						return o1.getValue().compareTo(o2.getValue());
					} else if (o1.getMinValue() != null && o2.getMinValue() != null) {
						return o1.getMinValue().compareTo(o2.getMinValue());
					}
					return 0; // Don't know how to compare
				}
    		});
	    }
	}
	
	/**
	 * Sets the total results, the current page, and the page size on the ProductSearchResult. Total results comes
	 * from solr, while page and page size are duplicates of the searchCriteria conditions for ease of use.
	 * 
	 * @param result
	 * @param response
	 * @param searchCriteria
	 */
	public void setPagingAttributes(ProductSearchResult result, QueryResponse response, 
			ProductSearchCriteria searchCriteria) {
	    result.setTotalResults(new Long(response.getResults().getNumFound()).intValue());
	    result.setPage(searchCriteria.getPage());
	    result.setPageSize(searchCriteria.getPageSize());
	}

	/**
	 * Given a list of product IDs from solr, this method will look up the IDs via the productDao and build out
	 * actual Product instances. It will return a Products that is sorted by the order of the IDs in the passed
	 * in list.
	 * 
	 * @param response
	 * @return the actual Product instances as a result of the search
	 */
	protected List<Product> getProducts(QueryResponse response) {
	    final List<Long> productIds = new ArrayList<Long>();
		SolrDocumentList docs = response.getResults();
    	for (SolrDocument doc : docs) {
    		productIds.add((Long) doc.getFieldValue("id"));
    	}
    	
	    List<Product> products = productDao.readProductsByIds(productIds); 
	    
	    // We have to sort the products list by the order of the productIds list to maintain sortability in the UI
	    if (products != null) {
		    Collections.sort(products, new Comparator<Product>() {
				public int compare(Product o1, Product o2) {
					return new Integer(productIds.indexOf(o1.getId())).compareTo(productIds.indexOf(o2.getId()));
				}
		    });
	    }
	    
		return products;
	}
	
	/**
	 * Returns a fully composed solr field string. Given indexField = a, tag = ex, and a non-null range,
	 * would produce the following String: {!ex=a}a:[minVal TO maxVal]
	 */
	protected String getSolrTaggedFieldString(String indexField, String tagField, String tag, SearchFacetRange range) {
		return getSolrFieldTag(tagField, tag) + getSolrFieldString(indexField, range);
	}
	
	/**
	 * Returns a solr field tag. Given indexField = a, tag = ex, would produce the following String:
	 * {!ex=a}
	 */
	protected String getSolrFieldTag(String tagField, String tag) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(tag)) {
			sb.append("{!").append(tag).append("=").append(tagField).append("}");
		}
		return sb.toString();
	}
	
	/**
	 * Returns a field string. Given indexField = a and a non-null range, would produce the following String:
	 * a:[minVal TO maxVal]
	 */
	protected String getSolrFieldString(String indexField, SearchFacetRange range) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(indexField);
		
		if (range != null) {
			String minValue = range.getMinValue().toPlainString();
			String maxValue = range.getMaxValue() == null ? "*" : range.getMaxValue().toPlainString();
			sb.append(":[").append(minValue).append(" TO ").append(maxValue).append("]");
		}
		
		return sb.toString();
	}
	
	protected String getCategorySortField(Category category) {
		return "category_" + category.getId() + "_sort_i";
	}
	
	/**
	 * Create the wrapper DTO around the SearchFacet
	 * 
	 * @param searchFacets
	 * @return the wrapper DTO
	 */
	protected List<SearchFacetDTO> buildSearchFacetDTOs(List<SearchFacet> searchFacets) {
		List<SearchFacetDTO> facets = new ArrayList<SearchFacetDTO>();
		
		for (SearchFacet facet : searchFacets) {
			SearchFacetDTO dto = new SearchFacetDTO();
			dto.setFacet(facet);
			dto.setShowQuantity(true);
			facets.add(dto);
		}
		
		return facets;
	}
	
	/**
	 * Converts a propertyName to one that is able to reference inside a map. For example, consider the property
	 * in Product that references a List<ProductAttribute>, "productAttributes". Also consider the utility method
	 * in Product called "mappedProductAttributes", which returns a map of the ProductAttributes keyed by the name
	 * property in the ProductAttribute. Given the parameters "productAttributes.heatRange", "productAttributes", 
	 * "mappedProductAttributes" (which would represent a property called "productAttributes.heatRange" that 
	 * references a specific ProductAttribute inside of a product whose "name" property is equal to "heatRange", 
	 * this method will convert this property to mappedProductAttributes(heatRange).value, which is then usable 
	 * by the standard beanutils PropertyUtils class to get the value.
	 * 
	 * @param propertyName
	 * @param listPropertyName
	 * @param mapPropertyName
	 * @return the converted property name
	 */
	protected String convertToMappedProperty(String propertyName, String listPropertyName, String mapPropertyName) {
		String[] splitName = StringUtils.split(propertyName, ".");
		StringBuilder convertedProperty = new StringBuilder();
		for (int i = 0; i < splitName.length; i++) {
			if (convertedProperty.length() > 0) {
				convertedProperty.append(".");
			}
			
			if (splitName[i].equals(listPropertyName)) {
				convertedProperty.append(mapPropertyName).append("(");
				convertedProperty.append(splitName[i+1]).append(").value");
				i++;
			} else {
				convertedProperty.append(splitName[i]);
			}
		}
		return convertedProperty.toString();
	}
	
	/**
	 * This method will be used to map a field abbreviation to the appropriate solr index field to use. Typically,
	 * this default implementation that maps to the facet field type will be sufficient. However, there may be 
	 * cases where you would want to use a different solr index depending on other currently active facets. In that
	 * case, you would associate that mapping here. For example, for the "price" abbreviation, we would generally
	 * want to use "defaultSku.retailPrice_td". However, if a secondary facet on item condition is selected (such
	 * as "refurbished", we may want to index "price" to "refurbishedSku.retailPrice_td". That mapping occurs here.
	 * 
	 * @param fields
	 * @return the solr field index key to use
	 */
	protected String getSolrFieldKey(Field field, ProductSearchCriteria searchCriteria) {
		if (field.getFacetFieldType() != null) {
			return field.getPropertyName() + "_" + field.getFacetFieldType().getType();
		}
		return null;
	}
	
	/**
	 * @param searchCriteria
	 * @return a map of abbreviated key to fully qualified solr index field key for all product fields
	 */
	protected Map<String, String> getSolrFieldKeyMap(ProductSearchCriteria searchCriteria) {
		List<Field> fields = fieldDao.readAllProductFields();
		Map<String, String> solrFieldKeyMap = new HashMap<String, String>();
		for (Field field : fields) {
			solrFieldKeyMap.put(field.getAbbreviation(), getSolrFieldKey(field, searchCriteria));
		}
		return solrFieldKeyMap;
	}
	
	/**
	 * @param facets
	 * @param searchCriteria
	 * @return a map of fully qualified solr index field key to the searchFacetDTO object
	 */
	protected Map<String, SearchFacetDTO> getNamedFacetMap(List<SearchFacetDTO> facets, 
			ProductSearchCriteria searchCriteria) {
		Map<String, SearchFacetDTO> namedFacetMap = new HashMap<String, SearchFacetDTO>();
		for (SearchFacetDTO facet : facets) {
			Field facetField = facet.getFacet().getField();
			namedFacetMap.put(getSolrFieldKey(facetField, searchCriteria), facet);
		}
		return namedFacetMap;
	}

}
