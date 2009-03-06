package org.broadleafcommerce.catalog.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Product {

    public Long getId();

    public void setId(Long id);

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public String getLongDescription();

    public void setLongDescription(String longDescription);

    public Date getActiveStartDate();

    public void setActiveStartDate(Date activeStartDate);

    public Date getActiveEndDate();

    public void setActiveEndDate(Date activeEndDate);

    public boolean isActive();

    public List<Sku> getSkus();

    public void setAllSkus(List<Sku> skus);

    public Map<String, String> getProductImages();

    public String getProductImage(String imageKey);

    public void setProductImages(Map<String, String> productImages);

    public Category getDefaultCategory();

    public void setDefaultCategory(Category defaultCategory);
}
