package org.broadleafcommerce.core.web.controller.catalog;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.search.domain.ProductSearchCriteria;
import org.broadleafcommerce.core.search.domain.ProductSearchResult;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.service.SearchService;
import org.broadleafcommerce.core.web.catalog.CategoryHandlerMapping;
import org.broadleafcommerce.core.web.service.SearchFacetDTOService;
import org.broadleafcommerce.core.web.util.ProcessorUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class works in combination with the CategoryHandlerMapping which finds a category based upon
 * the passed in URL.
 *
 * @author bpolster
 */
public class BroadleafCategoryController extends BroadleafAbstractController implements Controller {
	
    protected static String defaultCategoryView = "catalog/category";
    protected static String CATEGORY_ATTRIBUTE_NAME = "category";  
    protected static String PRODUCTS_ATTRIBUTE_NAME = "products";  
    protected static String FACETS_ATTRIBUTE_NAME = "facets";  
    protected static String PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME = "result";  
    protected static String ACTIVE_FACETS_ATTRIBUTE_NAME = "activeFacets";  
    
	@Resource(name = "blSearchService")
	protected SearchService searchService;
	
	@Resource(name = "blSearchFacetDTOService")
	protected SearchFacetDTOService facetService;

	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView model = new ModelAndView();
		
		if (request.getMethod().equals("POST")) {
			// If we receive a POST to a category url, we need to convert the POSTed fields to the 
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
			
			parameters.put(ProductSearchCriteria.PAGE_NUMBER, new String[] {"1"});
			parameters.put(fieldName, activeFieldFilters.toArray(new String[activeFieldFilters.size()]));
			parameters.remove("facetField");
			
			String newUrl = ProcessorUtils.getUrl(request.getRequestURL().toString(), parameters);
			model.setViewName("redirect:" + newUrl);
		} else {
			// Else, if we received a GET to the category URL (either the user clicked this link or we redirected
			// from the POST method, we can actually process the results
			
			Category category = (Category) request.getAttribute(CategoryHandlerMapping.CURRENT_CATEGORY_ATTRIBUTE_NAME);
			assert(category != null);
			
			List<SearchFacetDTO> availableFacets = searchService.getCategoryFacets(category);
			ProductSearchCriteria searchCriteria = facetService.buildSearchCriteria(request, availableFacets);
			ProductSearchResult result = searchService.findProductsByCategory(category, searchCriteria);
			
			facetService.setActiveFacetResults(result.getFacets(), request);
	    	
			model.addObject(CATEGORY_ATTRIBUTE_NAME, category);
	    	model.addObject(PRODUCTS_ATTRIBUTE_NAME, result.getProducts());
	    	model.addObject(FACETS_ATTRIBUTE_NAME, result.getFacets());
		    model.addObject(PRODUCT_SEARCH_RESULT_ATTRIBUTE_NAME, result);
	
			if (StringUtils.isNotEmpty(category.getDisplayTemplate())) {
				model.setViewName(category.getDisplayTemplate());	
			} else {
				model.setViewName(getDefaultCategoryView());
			}
		}
		return model;
	}

	public static String getDefaultCategoryView() {
		return defaultCategoryView;
	}

	public static void setDefaultCategoryView(String defaultCategoryView) {
		BroadleafCategoryController.defaultCategoryView = defaultCategoryView;
	}

}
