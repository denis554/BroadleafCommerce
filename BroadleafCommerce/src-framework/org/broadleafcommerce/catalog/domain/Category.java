package org.broadleafcommerce.catalog.domain;

import java.io.Serializable;
import java.util.List;

import org.broadleafcommerce.common.domain.Auditable;

//@Entity
//@Table(name = "BLC_CATEGORY")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

//    @Id
//    @GeneratedValue
//    @Column(name = "CATEGORY_ID")
    private Long id;

//    @Embedded
    private Auditable auditable;

//    @Column(name = "NAME")
    private String name;

//    @Column(name = "URL")
    private String url;

//    @Column(name = "URL_KEY")
    private String urlKey;

//    @ManyToOne
//    @JoinColumn(name = "PARENT_CATEGORY_ID")
    private Category parentCategory;
    
//    @ManyToMany
//    @JoinTable(name = "CATEGORY_ITEM_ASSOCIATIONS",
//        joinColumns=
//            @JoinColumn(name="CATEGORY_ID", referencedColumnName="CATEGORY_ID"),
//        inverseJoinColumns=
//            @JoinColumn(name="PRODUCT_ID", referencedColumnName="PRODUCT_ID")
//    
//    )
    private List<Product> products;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Auditable getAuditable() {
        return auditable;
    }

    public void setAuditable(Auditable auditable) {
        this.auditable = auditable;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlKey() {
		return urlKey;
	}

	public void setUrlKey(String urlKey) {
		this.urlKey = urlKey;
	}

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
