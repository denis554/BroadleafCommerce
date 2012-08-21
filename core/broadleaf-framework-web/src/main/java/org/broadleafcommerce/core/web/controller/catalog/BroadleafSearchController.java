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

package org.broadleafcommerce.core.web.controller.catalog;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.security.service.ExploitProtectionService;
import org.broadleafcommerce.core.search.domain.ProductSearchCriteria;
import org.broadleafcommerce.core.search.domain.ProductSearchResult;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.service.SearchService;
import org.broadleafcommerce.core.web.service.SearchFacetDTOService;
import org.broadleafcommerce.core.web.util.ProcessorUtils;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handles searching the catalog for a given search term. Will apply product search criteria
 * such as filters, sorts, and pagination if applicable
 * 
 * @author Andre Azzolini (apazzolini)
 */
public class BroadleafSearchController extends AbstractCatalogController {

	@Resource(name = "blSearchService")
	protected SearchService searchService;
	
	@Resource(name = "blExploitProtectionService")
	protected ExploitProtectionService exploitProtectionService;
	
	@Resource(name = "blSearchFacetDTOService")
	protected SearchFacetDTOService facetService;
	
	protected static String searchView = "catalog/search";
	
    protected static String PRODUCTS_ATTRIBUTE_NAME = "products";  
    protected static String FACETS_ATTRIBUTE_NAME = "facets";  
    protected static String PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME = "result";  
    protected static String ACTIVE_FACETS_ATTRIBUTE_NAME = "activeFacets";  
    protected static String ORIGINAL_QUERY_ATTRIBUTE_NAME = "originalQuery";  

	@SuppressWarnings("unchecked")
	public String search(Model model, HttpServletRequest request, String query) throws ServiceException {
		try {
			if (StringUtils.isNotEmpty(query)) {
				query = StringUtils.trim(query);
				query = exploitProtectionService.cleanString(query);
			}
		} catch (ServiceException e) {
			query = null;
		}
		
		if (query.length() == 0) {
			return "redirect:/";
		}
		if (request.getMethod().equals("POST")) {
			// If we receive a POST to a search url, we need to convert the POSTed fields to the 
			// product search criteria expected format. This is used in multi-facet selection. We 
			// will send a redirect to the appropriate URL to maintain canonical URLs
			
			String fieldName = request.getParameter("facetField");
			List<String> activeFieldFilters = new ArrayList<String>();
			Map<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
			
			for (Iterator<Entry<String,String[]>> iter = parameters.entrySet().iterator(); iter.hasNext();){
				Map.Entry<String, String[]> entry = iter.next();
				String key = entry.getKey();
				if (key.startsWith(fieldName + "-")) {
					activeFieldFilters.add(key.substring(key.indexOf('-') + 1));
					iter.remove();
				}
			}
			
			parameters.put(fieldName, activeFieldFilters.toArray(new String[activeFieldFilters.size()]));
			parameters.remove("facetField");
			
			String newUrl = ProcessorUtils.getUrl(request.getRequestURL().toString(), parameters);
			return "redirect:" + newUrl;
		} else {
			// Else, if we received a GET to the category URL (either the user performed a search or we redirected
			// from the POST method, we can actually process the results
		
			if (StringUtils.isNotEmpty(query)) {
				List<SearchFacetDTO> availableFacets = searchService.getSearchFacets();
				ProductSearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
				ProductSearchResult result = searchService.findProductsByQuery(query, searchCriteria);
				
				facetService.setActiveFacetResults(result.getFacets(), request);
		    	
		    	model.addAttribute(PRODUCTS_ATTRIBUTE_NAME, result.getProducts());
		    	model.addAttribute(FACETS_ATTRIBUTE_NAME, result.getFacets());
		    	model.addAttribute(PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME, result);
		    	model.addAttribute(ORIGINAL_QUERY_ATTRIBUTE_NAME, query);
			}
			
		}

        return getSearchView();
    }

	public String getSearchView() {
		return searchView;
	}
    
}
