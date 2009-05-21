package org.broadleafcommerce.catalog.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementations of this interface are used to hold data for a Product.  A product is a general description
 * of an item that can be sold (for example: a hat).  Products are not sold or added to a cart.  {@link Sku}s
 * which are specific items (for example: a XL Blue Hat) are sold or added to a cart.
 * <br>
 * <br>
 * You should implement this class if you want to make significant changes to how the
 * Product is persisted.  If you just want to add additional fields then you should extend {@link ProductImpl}.
 * 
 * @author btaylor
 * @see {@link ProductImpl},{@link Sku}, {@link Category}
 */
public interface Product {

    /**
     * The id of the Product.
     * 
     * @return the id of the Product
     */
    public Long getId();

    /**
     * Sets the id of the Product.
     * 
     * @param id - the id of the product
     */
    public void setId(Long id);

    /**
     * Returns the name of the product that is used for display purposes.
     * 
     * @return the name of the product
     */
    public String getName();

    /**
     * Sets the name of the product that is used for display purposes.
     * 
     * @param name - the name of the Product
     */
    public void setName(String name);

    /**
     * Returns a brief description of the product that is used for display.
     * 
     * @return a brief description of the product
     */
    public String getDescription();

    /**
     * Sets a brief description of the product that is used for display.
     * 
     * @param description - a brief description of the product
     */
    public void setDescription(String description);

    /**
     * Returns a long description of the product that is used for display.
     * 
     * @return a long description of the product
     */
    public String getLongDescription();

    /**
     * Sets a long description of the product that is used for display.
     * 
     * @param longDescription the long description
     */
    public void setLongDescription(String longDescription);

    /**
     * Returns the first date a product will be available that is used to determine whether
     * to display the product.
     * 
     * @return the first date the product will be available
     */
    public Date getActiveStartDate();

    /**
     * Sets the first date a product will be available that is used to determine whether
     * to display the product.
     * 
     * @param activeStartDate - the first day the product is available
     */
    public void setActiveStartDate(Date activeStartDate);

    /**
     * Returns the last date a product will be available that is used to determine whether
     * to display the product.
     * 
     * @return the last day the product is available
     */
    public Date getActiveEndDate();

    /**
     * Sets the last date a product will be available that is used to determine whether
     * to display the product.
     * 
     * @param activeEndDate - the last day the product is available
     */
    public void setActiveEndDate(Date activeEndDate);

    /**
     * Returns a boolean that indicates if the product is currently active.
     * 
     * @return a boolean indicates if the product is active.
     */
    public boolean isActive();

    /**
     * Returns a list of {@link Sku}s that are part of this product.
     * 
     * @return a list of {@link Sku}s associated with this product
     */
    public List<Sku> getSkus();

    /**
     * Sets the {@link Sku}s that are to be associated with this product.
     * 
     * @param skus - a List of {@link Sku}s to associate with this product.
     */
    public void setAllSkus(List<Sku> skus);

    /**
     * Returns a map of key/value pairs that associate the image name (key) with the URL to the image (value)
     * for display purposes.
     * 
     * @return a map of product images
     */
    public Map<String, String> getProductImages();

    /**
     * Returns a string URL to an image given the string key passed in for this product.
     * 
     * @param imageKey - a string key to lookup the image for the product
     * 
     * @return a URL to the image associated witht he key passed in.
     */
    public String getProductImage(String imageKey);

    /**
     * Sets the product images map.
     * 
     * @param productImages - a map of product images
     */
    public void setProductImages(Map<String, String> productImages);

    /**
     * Returns the default {@link Category} this product is associated with.
     * 
     * @return the default category for this product
     */
    public Category getDefaultCategory();

    /**
     * Sets the default {@link Category} to associate this product with.
     * 
     * @param defaultCategory - the default {@link Category} to associate this product with
     */
    public void setDefaultCategory(Category defaultCategory);
}
