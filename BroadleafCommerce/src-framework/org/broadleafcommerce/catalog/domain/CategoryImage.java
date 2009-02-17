package org.broadleafcommerce.catalog.domain;

public interface CategoryImage {

    public Long getId();

    public void setId(Long id);

    public String getName();

    public void setName(String name);

    public String getUrl();

    public void setUrl(String url);

    public Category getCategory();

    public void setCategory(Category category);
}
