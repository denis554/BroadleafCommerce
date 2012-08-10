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

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.security.service.ExploitProtectionService;
import org.broadleafcommerce.common.util.UrlUtil;
import org.broadleafcommerce.core.search.domain.ProductSearchCriteria;
import org.broadleafcommerce.core.search.domain.ProductSearchResult;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.service.ProductSearchService;
import org.broadleafcommerce.core.searchRedirect.domain.SearchRedirect;
import org.broadleafcommerce.core.searchRedirect.service.SearchRedirectService;
import org.broadleafcommerce.core.web.service.SearchFacetDTOService;
import org.springframework.ui.Model;

/**
 * Handles searching the catalog for a given search term. Will apply product search criteria
 * such as filters, sorts, and pagination if applicable
 * 
 * @author Andre Azzolini (apazzolini)
 */
public class BroadleafSearchController extends AbstractCatalogController {

	@Resource(name = "blProductSearchService")
	protected ProductSearchService productSearchService;
	
	@Resource(name = "blExploitProtectionService")
	protected ExploitProtectionService exploitProtectionService;
	
	@Resource(name = "blSearchFacetDTOService")
	protected SearchFacetDTOService facetService;
	@Resource(name = "blSearchRedirectService")
	private SearchRedirectService searchRedirectService;
	protected static String searchView = "catalog/search";
	
    protected static String PRODUCTS_ATTRIBUTE_NAME = "products";  
    protected static String FACETS_ATTRIBUTE_NAME = "facets";  
    protected static String ACTIVE_FACETS_ATTRIBUTE_NAME = "activeFacets";  
    protected static String ORIGINAL_QUERY_ATTRIBUTE_NAME = "originalQuery";  

	public String search(Model model, HttpServletRequest request, HttpServletResponse response,String query) throws ServletException, IOException {
		try {
			if (StringUtils.isNotEmpty(query)) {
				query = exploitProtectionService.cleanString(query);
			}
		} catch (ServiceException e) {
			query = null;
		}
	        SearchRedirect handler = searchRedirectService
	                .findSearchRedirectBySearchTerm(query);
	       
	        if (handler != null) {
	            String contextPath = request.getContextPath();
	            String url = UrlUtil.fixRedirectUrl(contextPath, handler.getUrl());
	            response.sendRedirect(url);   
	            return null;
	        }; 
		if (StringUtils.isNotEmpty(query)) {
			List<SearchFacetDTO> availableFacets = productSearchService.getSearchFacets();
			ProductSearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
			ProductSearchResult result = productSearchService.findProductsByQuery(query, searchCriteria);
			
			facetService.setActiveFacetResults(result.getFacets(), request);
	    	
	    	model.addAttribute(PRODUCTS_ATTRIBUTE_NAME, result.getProducts());
	    	model.addAttribute(FACETS_ATTRIBUTE_NAME, result.getFacets());
	    	model.addAttribute(ORIGINAL_QUERY_ATTRIBUTE_NAME, query);
		}
		
	        return getSearchView();
	        

	        
	    }

	    public String getSearchView() {
	                return searchView;
	        }
	    
}

