package org.springcommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springcommerce.catalog.domain.SellableItem;
import org.springcommerce.search.domain.SearchQuery;
import org.springcommerce.search.service.SearchService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class SearchController extends SimpleFormController {
	private SearchService searchService;

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {

		SearchQuery input = (SearchQuery) command;
		System.out.println("------------------------ Searching Index;");
		List<SellableItem> sellableItems = searchService.performSearch(input.getQueryString());
		System.out.println("------------------------ Finished Searching Index;");

        Map<Object, Object> model = new HashMap<Object, Object>();
        model.put("sellableItems", sellableItems);

		ModelAndView mav = new ModelAndView(getSuccessView(), model);

		return mav;
	}
}
