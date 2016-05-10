/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.search.domain.SearchFacetDTO;
import org.broadleafcommerce.core.search.domain.SearchResult;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "searchResults")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SearchResultsWrapper extends BaseWrapper implements APIWrapper<SearchResult> {

    @XmlElement
    protected Integer page;

    /*
     * Indicates the requested or default page size.
     */
    @XmlElement
    protected Integer pageSize;

    /*
     * Indicates the actual results
     */
    @XmlElement
    protected Integer totalResults;

    /*
     * Indicates the number of pages
     */
    @XmlElement
    protected Integer totalPages;

    /*
     * List of products associated with a search
     */
    @XmlElementWrapper(name = "products")
    @XmlElement(name = "product")
    protected List<ProductWrapper> products;
    
    /*
     * List of products associated with a search
     */
    @XmlElementWrapper(name = "skus")
    @XmlElement(name = "sku")
    protected List<SkuWrapper> skus;

    /*
     * List of available facets to be used for searching
     */
    @XmlElementWrapper(name = "searchFacets")
    @XmlElement(name = "searchFacet")
    protected List<SearchFacetWrapper> searchFacets;

    @Override
    public void wrapDetails(SearchResult model, HttpServletRequest request) {

        page = model.getPage();
        pageSize = model.getPageSize();
        totalResults = model.getTotalResults();
        totalPages = model.getTotalPages();

        if (model.getProducts() != null) {
            products = new ArrayList<ProductWrapper>();
            for (Product product : model.getProducts()) {
                ProductWrapper productSummary = (ProductWrapper) context.getBean(ProductWrapper.class.getName());
                productSummary.wrapSummary(product, request);
                this.products.add(productSummary);
            }
        }
        
        if (model.getSkus() != null) {
            skus = new ArrayList<SkuWrapper>();
            for (Sku sku : model.getSkus()) {
                SkuWrapper skuSummary = (SkuWrapper) context.getBean(SkuWrapper.class.getName());
                skuSummary.wrapSummary(sku, request);
                this.skus.add(skuSummary);
            }
        }

        if (model.getFacets() != null) {
            this.searchFacets = new ArrayList<SearchFacetWrapper>();
            for (SearchFacetDTO facet : model.getFacets()) {
                SearchFacetWrapper facetWrapper = (SearchFacetWrapper) context.getBean(SearchFacetWrapper.class.getName());
                facetWrapper.wrapSummary(facet, request);
                this.searchFacets.add(facetWrapper);
            }
        }
    }

    @Override
    public void wrapSummary(SearchResult model, HttpServletRequest request) {
        wrapDetails(model, request);
    }

    
    /**
     * @return the page
     */
    public Integer getPage() {
        return page;
    }

    
    /**
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    
    /**
     * @return the pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }

    
    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    
    /**
     * @return the totalResults
     */
    public Integer getTotalResults() {
        return totalResults;
    }

    
    /**
     * @param totalResults the totalResults to set
     */
    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    
    /**
     * @return the totalPages
     */
    public Integer getTotalPages() {
        return totalPages;
    }

    
    /**
     * @param totalPages the totalPages to set
     */
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    
    /**
     * @return the products
     */
    public List<ProductWrapper> getProducts() {
        return products;
    }

    
    /**
     * @param products the products to set
     */
    public void setProducts(List<ProductWrapper> products) {
        this.products = products;
    }

    
    /**
     * @return the skus
     */
    public List<SkuWrapper> getSkus() {
        return skus;
    }

    
    /**
     * @param skus the skus to set
     */
    public void setSkus(List<SkuWrapper> skus) {
        this.skus = skus;
    }

    
    /**
     * @return the searchFacets
     */
    public List<SearchFacetWrapper> getSearchFacets() {
        return searchFacets;
    }

    
    /**
     * @param searchFacets the searchFacets to set
     */
    public void setSearchFacets(List<SearchFacetWrapper> searchFacets) {
        this.searchFacets = searchFacets;
    }
}
