package org.broadleafcommerce.admin.catalog.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.CategoryImpl;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.service.CatalogService;
import org.broadleafcommerce.media.domain.Media;
import org.broadleafcommerce.media.domain.MediaImpl;
import org.springframework.stereotype.Service;

import flex.messaging.io.amf.ASObject;

@Service("blAdminCatalogService")
public class AdminCatalogService {
    
    @Resource(name = "blCatalogService")
    CatalogService catalogService;
    
    public Product findProductById(Long productId) {
        return catalogService.findProductById(productId);
    }

    public List<Product> findProductsByName(String searchName) {
        return catalogService.findProductsByName(searchName);
    }

    public List<Product> findProductsByCategory(Category category) {
    	List<Product> p = catalogService.findProductsForCategory(category);    	
        return p;
    }

    public Product saveProduct(Product product) {
//    	Map<String, String> images = product.getProductImages();
//    	Map<String, Media> media = product.getProductMedia();
    	Product newProduct = catalogService.findProductById(product.getId());
    	newProduct.getAllParentCategories().clear();
    	newProduct.getAllParentCategories().add(newProduct.getDefaultCategory());
        return catalogService.saveProduct(product);
    }
    
    public Category findCategoryById(Long categoryId) {
        return catalogService.findCategoryById(categoryId);
    }

    public Category findCategoryByName(String categoryName) {
        return catalogService.findCategoryByName(categoryName);
    }

    public Category saveCategory(Category category) {
        if(category.getCategoryImages() != null && category.getCategoryImages() instanceof ASObject) {
            category.setCategoryImages(getImagesMapFromAsObject((ASObject)category.getCategoryImages()));
        }

        if(category.getCategoryMedia() != null && category.getCategoryMedia() instanceof ASObject) {            
            category.setCategoryMedia(getMediaMapFromAsObject((ASObject)category.getCategoryMedia()));
        }
        return catalogService.saveCategory(category);
    }


    public List<Category> findAllCategories() {
    	List<Category> c = catalogService.findAllCategories(); 
        return c;
    }

    public List<Product> findAllProducts() {
        return catalogService.findAllProducts();
    }

    public List<Sku> findAllSkus() {
        return catalogService.findAllSkus();
    }

    public Sku findSkuById(Long skuId) {
        return catalogService.findSkuById(skuId);
    }

    public Sku saveSku(Sku sku) {
    	if(sku.getSkuImages() != null && sku.getSkuImages() instanceof ASObject){
    		sku.setSkuImages(getImagesMapFromAsObject((ASObject)sku.getSkuImages()));
    	}
    	if(sku.getSkuMedia() != null && sku.getSkuMedia() instanceof ASObject){
    		sku.setSkuMedia(getMediaMapFromAsObject((ASObject)sku.getSkuMedia()));
    	}
        return catalogService.saveSku(sku);
    }

    public List<Sku> findSkusByIds(List<Long> ids) {
        return catalogService.findSkusByIds(ids);
    }

    private Map<String, String> getImagesMapFromAsObject(ASObject oldImages){
        Map<String, String> newImages = new HashMap<String, String>();        
        for(Object key : oldImages.keySet()) {
            if(String.class.equals(key.getClass())) {                
                Object test = oldImages.get(key);
                if(String.class.equals(test.getClass())) {
                    newImages.put((String)key, (String)oldImages.get(key));                
                }
            }
        }
        return newImages;
    	
    }
    
    private Map<String,Media> getMediaMapFromAsObject(ASObject oldMedia){
        Map<String, Media> newMedia = new HashMap<String, Media>();
        for(Object key:oldMedia.keySet()) {
            if(String.class.equals(key.getClass())) {
                Object test = oldMedia.get(key);
                if(test instanceof MediaImpl) {
                	String keyString = (String)key;
                    newMedia.put(keyString, (MediaImpl)oldMedia.get(key));
                }
            }
        }
        return newMedia;
    	
    }
    
    private List<Category> normalizeCategories(List<Category> asObjectCategories){
    	List<Category> normalizedCategories = new ArrayList<Category>();
    	for (Category category : asObjectCategories){
    		category.setCategoryImages(getImagesMapFromAsObject((ASObject)category.getCategoryImages()));
    		category.setCategoryMedia(getMediaMapFromAsObject((ASObject)category.getCategoryMedia()));
    		normalizedCategories.add(category);
    	}
    	return normalizedCategories;
    }

    
}
