package org.broadleafcommerce.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.service.CatalogService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SimpleCatalogController extends AbstractController {
    protected final Log logger = LogFactory.getLog(getClass());
    private CatalogService catalogService;
    private String defaultCategoryView;
    private String defaultProductView;
    private Long rootCategoryId;
    private String categoryTemplatePrefix;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) {

        HashMap<String,Object> model = new HashMap<String,Object>();

        addCategoryToModel(request, model);
        boolean productFound = addProductsToModel(request, model);

        String view = defaultCategoryView;
        if (productFound) {
            // TODO: Nice to have: product logic similar to category below
            view = defaultProductView;
        } else {
            Category currentCategory = (Category) model.get("currentCategory");
            if (currentCategory == null) {
                view = defaultCategoryView;
            }
            else if (currentCategory.getUrl() != null) {
                return new ModelAndView("redirect:"+currentCategory.getUrl().replace("index.jhtml", ""));
            } else if (currentCategory.getDisplayTemplate() != null) {
                view = categoryTemplatePrefix + currentCategory.getDisplayTemplate();
            } else {
                view = defaultCategoryView;
            }
        }
        return new ModelAndView(view, model);
    }

    protected void addCategoryToModel(HttpServletRequest request, Map<String, Object> model) {
        Category rootCategory = null;
        if (getRootCategoryId() != null) {
            rootCategory = catalogService.findCategoryById(getRootCategoryId());
        }
        if (rootCategory == null) {
            throw new IllegalStateException("Catalog Controller configured incorrectly - root category not found: " + rootCategoryId);
        }

        String categoryId = request.getParameter("categoryId");

        Category category = null;
        if (categoryId != null) {
            category = catalogService.findCategoryById(new Long(categoryId));
        }

        if (category == null) {
            model.put("categoryError", true);
        }

        model.put("rootCategory", rootCategory);
        if (category != null) {
            model.put("currentCategory", category);
        } else {
            model.put("currentCategory", rootCategory);
        }
    }

    protected int findProductPositionInList(Product product, List<Product> products) {
        for (int i=0; i < products.size(); i++) {
            Product currentProduct = products.get(i);
            if (product.getId().equals(currentProduct.getId())) {
                return i+1;
            }
        }
        return 0;
    }

    protected List<Category> buildCategoryList(Category rootCategory, Category currentCategory, String url) {
        List<Category> categoryList = rootCategory.getChildCategoryURLMap().get(url);

        if (categoryList == null) {
            categoryList = new ArrayList<Category>();
            while (currentCategory != null) {
                categoryList.add(currentCategory);
                currentCategory = currentCategory.getDefaultParentCategory();
            }
            Collections.reverse(categoryList);
        }
        return categoryList;
    }

    protected boolean validateProductAndAddToModel(Product product, Map<String,Object> model) {
        Category currentCategory = (Category) model.get("currentCategory");
        Category rootCategory = (Category) model.get("rootCategory");
        int productPosition=0;

        List<Product> productList = catalogService.findActiveProductsByCategory(currentCategory);
        if (productList != null) {
            model.put("currentProducts", productList);
        }
        productPosition = findProductPositionInList(product, productList);
        if (productPosition == 0) {
            // look for product in its default category and override category from request URL
            currentCategory = product.getDefaultCategory();
            model.put("currentCategory", currentCategory);
            productList = catalogService.findActiveProductsByCategory(currentCategory);
            if (productList != null) {
                model.put("currentProducts", productList);
            }
            String url = currentCategory.getGeneratedUrl();

            // override category list settings using this products default
            List<Category> categoryList = buildCategoryList(rootCategory, currentCategory, url);
            if (categoryList != null) {
                productPosition = findProductPositionInList(product, productList);
            }
        }

        if (productPosition != 0) {
            model.put("productError", false);
            model.put("currentProduct", product);
            model.put("productPosition", productPosition);
            if (productPosition != 1) {
                model.put("previousProduct", productList.get(productPosition-2));
            }
            if (productPosition < productList.size()) {
                model.put("nextProduct", productList.get(productPosition));
            }
            model.put("totalProducts", productList.size());
        } else {
            model.put("productError", true);
        }

        return (productPosition !=0);
    }

    protected boolean addProductsToModel(HttpServletRequest request, Map<String,Object> model ) {
        boolean productFound = false;

        String productId = request.getParameter("productId");
        if (productId != null) {
            Product product = catalogService.findProductById(new Long(productId));
            if (product != null) {
                productFound = validateProductAndAddToModel(product, model);
            }
        } else {
            Category currentCategory = (Category) model.get("currentCategory");
            List<Product> productList = catalogService.findActiveProductsByCategory(currentCategory);
            model.put("currentProducts", productList);
        }

        return productFound;
    }

    public Long getRootCategoryId() {
        return rootCategoryId;
    }

    public void setRootCategoryId(Long rootCategoryId) {
        this.rootCategoryId = rootCategoryId;
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String getDefaultCategoryView() {
        return defaultCategoryView;
    }

    public void setDefaultCategoryView(String defaultCategoryView) {
        this.defaultCategoryView = defaultCategoryView;
    }

    public String getDefaultProductView() {
        return defaultProductView;
    }

    public void setDefaultProductView(String defaultProductView) {
        this.defaultProductView = defaultProductView;
    }

    public String getCategoryTemplatePrefix() {
        return categoryTemplatePrefix;
    }

    public void setCategoryTemplatePrefix(String categoryTemplatePrefix) {
        this.categoryTemplatePrefix = categoryTemplatePrefix;
    }
}
