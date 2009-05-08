package org.broadleafcommerce.order.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.ProductImpl;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuImpl;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_DISCRETE_ORDER_ITEM")
public class DiscreteOrderItemImpl extends OrderItemImpl implements DiscreteOrderItem {

    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = SkuImpl.class)
    @JoinColumn(name = "SKU_ID", nullable = false)
    protected Sku sku;

    @ManyToOne(targetEntity = ProductImpl.class)
    @JoinColumn(name = "PRODUCT_ID")
    protected Product product;

    @ManyToOne(targetEntity = BundleOrderItemImpl.class)
    @JoinColumn(name = "BUNDLE_ORDER_ITEM_ID")
    protected BundleOrderItem bundleOrderItem;

    public Sku getSku() {
        return sku;
    }

    public void setSku(Sku sku) {
        this.sku = sku;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BundleOrderItem getBundleOrderItem() {
        return bundleOrderItem;
    }

    public void setBundleOrderItem(BundleOrderItem bundleOrderItem) {
        this.bundleOrderItem = bundleOrderItem;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscreteOrderItemImpl other = (DiscreteOrderItemImpl) obj;
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        if (bundleOrderItem == null) {
            if (other.bundleOrderItem != null)
                return false;
        } else if (!bundleOrderItem.equals(other.bundleOrderItem))
            return false;
        if (sku == null) {
            if (other.sku != null)
                return false;
        } else if (!sku.equals(other.sku))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bundleOrderItem == null) ? 0 : bundleOrderItem.hashCode());
        result = prime * result + ((sku == null) ? 0 : sku.hashCode());
        return result;
    }

}
