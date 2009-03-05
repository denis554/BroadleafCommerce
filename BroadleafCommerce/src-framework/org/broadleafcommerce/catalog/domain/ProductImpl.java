package org.broadleafcommerce.catalog.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.broadleafcommerce.util.DateUtil;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.OrderBy;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_PRODUCT")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ProductImpl implements Product, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LONG_DESCRIPTION")
    private String longDescription;

    @Column(name = "ACTIVE_START_DATE")
    private Date activeStartDate;

    @Column(name = "ACTIVE_END_DATE")
    private Date activeEndDate;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = SkuImpl.class)
    @JoinTable(name = "BLC_PRODUCT_SKU_XREF", joinColumns = @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID"), inverseJoinColumns = @JoinColumn(name = "SKU_ID", referencedColumnName = "SKU_ID"))
    @OrderBy(clause = "DISPLAY_ORDER")
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private List<Sku> allSkus;

    @CollectionOfElements
    @JoinTable(name = "BLC_PRODUCT_IMAGE", joinColumns = @JoinColumn(name = "PRODUCT_ID"))
    @org.hibernate.annotations.MapKey(columns = { @Column(name = "NAME", length = 5) })
    @Column(name = "URL")
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private Map<String, String> productImages;

    // TODO fix jb
    // This is a One-To-Many which OWNS!!! the collection
    // Notice that I don't have a "mappedBy" member on the @OneToMany annotation
    //@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = ProductAuxillaryImageImpl.class)
    //@OrderBy(clause = "DISPLAY_ORDER")
    //@JoinTable(name = "BLC_PRODUCT_AUX_IMAGE", joinColumns = @JoinColumn(name = "PRODUCT_ID"), inverseJoinColumns = @JoinColumn(name = "ID"))
    //@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    //private List<ImageDescription> productAuxillaryImages;

    @OneToOne(targetEntity = CategoryImpl.class)
    @JoinColumn(name = "DEFAULT_CATEGORY_ID")
    private Category defaultCategory;

    @Transient
    private List<Sku> skus;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Date getActiveStartDate() {
        return activeStartDate;
    }

    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    public Date getActiveEndDate() {
        return activeEndDate;
    }

    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    public boolean isActive() {
        return DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false);
    }

    private List<Sku> getAllSkus() {
        return allSkus;
    }

    public List<Sku> getSkus() {
        if (skus == null) {
            skus = new ArrayList<Sku>();
            List<Sku> allSkus = getAllSkus();
            for (Sku sku : allSkus) {
                if (sku.isActive()) {
                    skus.add(sku);
                }
            }
        }
        return skus;
    }

    public void setAllSkus(List<Sku> skus) {
        this.allSkus = skus;
        this.skus = null;
    }

    public Map<String, String> getProductImages() {
        return productImages;
    }

    public String getProductImage(String imageKey) {
        return productImages.get(imageKey);
    }

    public void setProductImages(Map<String, String> productImages) {
        this.productImages = productImages;
    }

    //public List<ImageDescription> getProductAuxillaryImages() {
    //    return productAuxillaryImages;
    //}

    //public void setProductAuxillaryImages(List<ImageDescription> productAuxillaryImages) {
    //    this.productAuxillaryImages = productAuxillaryImages;
   // }

    public Category getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(Category defaultCategory) {
        this.defaultCategory = defaultCategory;
    }
}
