/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.core.search.domain;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.search.service.solr.SolrHelperService;

import java.util.Map;


/**
 * Container that holds additional criteria to consider when performing searches for Products
 * 
 * @author Andre Azzolini (apazzolini)
 */
public class SearchCriteria {
    
    public static String PAGE_SIZE_STRING = "pageSize";
    public static String PAGE_NUMBER = "page";
    public static String SORT_STRING = "sort";
    public static String QUERY_STRING = "q";
    
    protected Integer page = 1;
    protected Integer pageSize;
    protected Integer startIndex;
    protected String sortQuery;
    protected Map<String, String[]> filterCriteria;
    /**
     * The category that the user searched on
     */
    protected Category category;

    /**
     * The query that the user actually typed into the search box, fully sanitized
     */
    protected String query;
    
    /**
     * Whether or not to do category filtering based on {@link SolrHelperService#getExplicitCategoryFieldName()} or
     * {@link SolrHelperService#getCategoryFieldName()}
     */
    protected boolean searchExplicitCategory = false;
    
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public String getSortQuery() {
        return sortQuery;
    }
    
    public void setSortQuery(String sortQuery) {
        this.sortQuery = sortQuery;
    }

    public Map<String, String[]> getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(Map<String, String[]> filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean getSearchExplicitCategory() {
        return searchExplicitCategory;
    }

    public void setSearchExplicitCategory(boolean searchExplicitCategory) {
        this.searchExplicitCategory = searchExplicitCategory;
    }
}
