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

package org.broadleafcommerce.core.catalog.dao;

import org.broadleafcommerce.common.persistence.EntityConfiguration;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.hibernate.ejb.QueryHints;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Jeff Fischer
 */
@Repository("blCategoryDao")
public class CategoryDaoImpl implements CategoryDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    protected Long currentDateResolution = 10000L;
    private Date currentDate = SystemTime.asDate();

    @Override
    public Category save(Category category) {
        return em.merge(category);
    }

    @Override
    public Category readCategoryById(Long categoryId) {
        return em.find(CategoryImpl.class, categoryId);
    }

    @Override
    @Deprecated
    public Category readCategoryByName(String categoryName) {
        Query query = em.createNamedQuery("BC_READ_CATEGORY_BY_NAME");
        query.setParameter("categoryName", categoryName);
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.setHint(QueryHints.HINT_CACHE_REGION, "query.Catalog");
        return (Category) query.getSingleResult();
    }

    @Override
    public List<Category> readCategoriesByName(String categoryName) {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_CATEGORY_BY_NAME", Category.class);
        query.setParameter("categoryName", categoryName);
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.setHint(QueryHints.HINT_CACHE_REGION, "query.Catalog");
        return query.getResultList();
    }

    @Override
    public List<Category> readCategoriesByName(String categoryName, int limit, int offset) {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_CATEGORY_BY_NAME", Category.class);
        query.setParameter("categoryName", categoryName);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<Category> readAllCategories() {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ALL_CATEGORIES", Category.class);
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.setHint(QueryHints.HINT_CACHE_REGION, "query.Catalog");
        return query.getResultList();
    }

    @Override
    public List<Category> readAllCategories(int limit, int offset) {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ALL_CATEGORIES", Category.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<Product> readAllProducts() {
        TypedQuery<Product> query = em.createNamedQuery("BC_READ_ALL_PRODUCTS", Product.class);
        return query.getResultList();
    }

    @Override
    public List<Product> readAllProducts(int limit, int offset) {
        TypedQuery<Product> query = em.createNamedQuery("BC_READ_ALL_PRODUCTS", Product.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<Category> readAllSubCategories(Category category) {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ALL_SUBCATEGORIES", Category.class);
        query.setParameter("defaultParentCategory", category);
        return query.getResultList();
    }

    @Override
    public List<Category> readAllSubCategories(Category category, int limit, int offset) {
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ALL_SUBCATEGORIES", Category.class);
        query.setParameter("defaultParentCategory", category);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    public List<Category> readActiveSubCategoriesByCategory(Category category) {
        Date myDate;
        Long myCurrentDateResolution = currentDateResolution;
        synchronized (this) {
            Date now = SystemTime.asDate();
            if (now.getTime() - this.currentDate.getTime() > myCurrentDateResolution) {
                currentDate = new Date(now.getTime());
                myDate = currentDate;
            } else {
                myDate = currentDate;
            }
        }
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ACTIVE_SUBCATEGORIES_BY_CATEGORY", Category.class);
        query.setParameter("defaultParentCategory", category);
        query.setParameter("currentDate", myDate);
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.setHint(QueryHints.HINT_CACHE_REGION, "query.Catalog");

        return query.getResultList();
    }

    @Override
    public List<Category> readActiveSubCategoriesByCategory(Category category, int limit, int offset) {
        Date myDate;
        Long myCurrentDateResolution = currentDateResolution;
        synchronized (this) {
            Date now = SystemTime.asDate();
            if (now.getTime() - this.currentDate.getTime() > myCurrentDateResolution) {
                currentDate = new Date(now.getTime());
                myDate = currentDate;
            } else {
                myDate = currentDate;
            }
        }
        TypedQuery<Category> query = em.createNamedQuery("BC_READ_ACTIVE_SUBCATEGORIES_BY_CATEGORY", Category.class);
        query.setParameter("defaultParentCategory", category);
        query.setParameter("currentDate", myDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public void delete(Category category) {
        if (!em.contains(category)) {
            category = readCategoryById(category.getId());
        }
        em.remove(category);
    }

    @Override
    public Category create() {
        return (Category) entityConfiguration.createEntityInstance(Category.class.getName());
    }

    public Long getCurrentDateResolution() {
        return currentDateResolution;
    }

    public void setCurrentDateResolution(Long currentDateResolution) {
        this.currentDateResolution = currentDateResolution;
    }

    @Override
    public Category findCategoryByURI(String uri) {
        Query query;
        query = em.createNamedQuery("BC_READ_CATEGORY_OUTGOING_URL");
        query.setParameter("url", uri);

        @SuppressWarnings("unchecked")
        List<Category> results = (List<Category>) query.getResultList();
        if (results != null && !results.isEmpty()) {
            return results.get(0);

        } else {
            return null;
        }
    }

}
