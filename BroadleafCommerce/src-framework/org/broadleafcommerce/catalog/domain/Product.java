package org.broadleafcommerce.catalog.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Map<String, ItemAttribute> itemAttributes;

    private Set<ProductListEntry> productListEntries;

    private String description;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, ItemAttribute> getItemAttributes() {
        return itemAttributes;
    }

    public void setItemAttributes(Map<String, ItemAttribute> itemAttributes) {
        this.itemAttributes = itemAttributes;
    }

    public Set<ProductListEntry> getProductListAssociations() {
        return productListEntries;
    }

    public void setProductListAssociations(Set<ProductListEntry> productListEntries) {
        this.productListEntries = productListEntries;
    }
}
