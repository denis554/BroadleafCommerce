package org.broadleafcommerce.catalog.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
/**
 * Instances of this interface are used to hold data about a SKU.  A SKU is
 * a specific item that can be sold including any specific attributes of the item such as
 * color or size.
 * 
 * You should implement this class if you want to make significant changes to how the
 * SKU is persisted.  If you just want to add additional fields then you should extend {@link SkuImpl}. 
 * 
 * @see Product, SkuImpl
 * @author btaylor
 * 
 */
public interface Sku {

	/**
	 * Returns the id of this sku 
	 */
    public Long getId();

	/**
	 * Sets the id of this sku 
	 */
    public void setId(Long id);

	/**
	 * Returns the Sale Price of the Sku.  The Sale Price is the standard price the vendor sells
	 * this item for.
	 */
    public BigDecimal getSalePrice();

	/**
	 * Sets the the Sale Price of the Sku.  The Sale Price is the standard price the vendor sells
	 * this item for.
	 */
    public void setSalePrice(BigDecimal salePrice);

    /**
	 * Returns the Retail Price of the Sku.  The Retail Price is the MSRP of the sku.
	 */
    public BigDecimal getRetailPrice();
    
    public void setRetailPrice(BigDecimal retailPrice);
    
    /**
	 * Returns the List Price of the Sku.  The List Price is the MSRP of the sku.
	 * @deprecated
	 */
    public BigDecimal getListPrice();

	/**
	 * Sets the the List Price of the Sku.  The List Price is the MSRP of the sku.
	 * @deprecated
	 */
    public void setListPrice(BigDecimal listPrice);

	/**
	 * Returns the name of the Sku.  The name is a label used to show when displaying the sku. 
	 */
    public String getName();

	/**
	 * Sets the the name of the Sku.  The name is a label used to show when displaying the sku.  
	 */
    public void setName(String name);

	/**
	 * Returns the brief description of the Sku.
	 */
    public String getDescription();

	/**
	 * Sets the brief description of the Sku.
	 */
    public void setDescription(String description);

	/**
	 * Returns the long description of the sku.
	 */
    public String getLongDescription();

	/**
	 * Sets the long description of the sku. 
	 */
    public void setLongDescription(String longDescription);

	/**
	 * Returns whether the Sku qualifies for taxes or not.  This field is used by the pricing engine
	 * to calculate taxes.
	 */
    public Boolean isTaxable();

	/**
	 * Sets the whether the Sku qualifies for taxes or not.  This field is used by the pricing engine
	 * to calculate taxes.
	 */
    public void setTaxable(Boolean taxable);

	/**
	 * Returns the first date that the Sku should be available for sale.  This field is used to determine
	 * whether a user can add the sku to their cart. 
	 */
    public Date getActiveStartDate();

	/**
	 * Sets the the first date that the Sku should be available for sale.  This field is used to determine
	 * whether a user can add the sku to their cart.
	 */
    public void setActiveStartDate(Date activeStartDate);

	/**
	 * Returns the the last date that the Sku should be available for sale.  This field is used to determine
	 * whether a user can add the sku to their cart.
	 */
    public Date getActiveEndDate();

	/**
	 * Sets the the last date that the Sku should be available for sale.  This field is used to determine
	 * whether a user can add the sku to their cart.
	 */
    public void setActiveEndDate(Date activeEndDate);

	/**
	 * Returns a boolean indicating whether this sku is active.  This is used to determine whether a user
	 * the sku can add the sku to their cart.
	 */
    public boolean isActive();

	/**
	 * Returns a map of key/value pairs where the key is a string for the name of an image and the value
	 * is a string to the URL of the image.  This is used to display images while browsing the sku.
	 */
    public Map<String, String> getSkuImages();

	/**
	 * Returns the default image used for the Sku.
	 */
    public String getSkuImage(String imageKey);

	/**
	 * Sets a map of key/value pairs where the key is a string for the name of an image and the value
	 * is a string to the URL of the image.  This is used to display images while browsing the sku.
	 */
    public void setSkuImages(Map<String, String> skuImages);
}
