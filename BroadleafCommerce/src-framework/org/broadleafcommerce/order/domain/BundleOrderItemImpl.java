package org.broadleafcommerce.order.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.broadleafcommerce.util.money.Money;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_BUNDLE_ORDER_ITEM")
public class BundleOrderItemImpl extends OrderItemImpl implements BundleOrderItem {

    private static final long serialVersionUID = 1L;

    @Column(name = "NAME")
    protected String name;

    @OneToMany(mappedBy = "bundleOrderItem", targetEntity = DiscreteOrderItemImpl.class, cascade = {CascadeType.ALL})
    protected List<DiscreteOrderItem> discreteOrderItems = new ArrayList<DiscreteOrderItem>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DiscreteOrderItem> getDiscreteOrderItems() {
        return discreteOrderItems;
    }

    public void setDiscreteOrderItems(List<DiscreteOrderItem> discreteOrderItems) {
        this.discreteOrderItems = discreteOrderItems;
    }

    @Override
    public void removeAllCandidateOffers() {
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            discreteOrderItem.removeAllCandidateOffers();
        }
    }

    @Override
    public void removeAllAdjustments() {
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            discreteOrderItem.removeAllAdjustments();
        }
    }

    @Override
    public void assignFinalPrice() {
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            discreteOrderItem.assignFinalPrice();
        }
        price = getCurrentPrice().getAmount();
    }

    @Override
    public Money getRetailPrice() {
        Money bundleRetailPrice = new Money();
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            Money itemRetailPrice = discreteOrderItem.getRetailPrice();
            bundleRetailPrice = bundleRetailPrice.add(new Money(itemRetailPrice.doubleValue() * discreteOrderItem.getQuantity()));
        }
        return bundleRetailPrice;
    }


    @Override
    public Money getSalePrice() {
        Money bundleSalePrice = null;
        if (hasSaleItems()) {
            bundleSalePrice = new Money();
            for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
                Money itemSalePrice = null;
                if (discreteOrderItem.getSalePrice() != null) {
                    itemSalePrice = discreteOrderItem.getSalePrice();
                } else {
                    itemSalePrice = discreteOrderItem.getRetailPrice();
                }
                bundleSalePrice = bundleSalePrice.add(new Money(itemSalePrice.doubleValue() * discreteOrderItem.getQuantity()));
            }
        }
        return bundleSalePrice;
    }

    private boolean hasSaleItems() {
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            if (discreteOrderItem.getSalePrice() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Money getAdjustmentPrice() {
        Money bundleAdjustmentPrice = null;
        if (hasAdjustmentItems()) {
            bundleAdjustmentPrice = new Money();
            for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
                Money itemAdjustmentPrice = null;
                if (discreteOrderItem.getAdjustmentPrice() != null) {
                    itemAdjustmentPrice = discreteOrderItem.getAdjustmentPrice();
                } else if (discreteOrderItem.getSalePrice() != null) {
                    itemAdjustmentPrice = discreteOrderItem.getSalePrice();
                } else {
                    itemAdjustmentPrice = discreteOrderItem.getRetailPrice();
                }
                bundleAdjustmentPrice = bundleAdjustmentPrice.add(new Money(itemAdjustmentPrice.doubleValue() * discreteOrderItem.getQuantity()));
            }
        }
        return bundleAdjustmentPrice;
    }

    private boolean hasAdjustmentItems() {
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            if (discreteOrderItem.getAdjustmentPrice() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Money getCurrentPrice() {
        Money currentBundlePrice = new Money();
        for (DiscreteOrderItem discreteOrderItem : discreteOrderItems) {
            Money currentItemPrice = discreteOrderItem.getCurrentPrice();
            currentBundlePrice = currentBundlePrice.add(new Money(currentItemPrice.doubleValue() * discreteOrderItem.getQuantity()));
        }
        return currentBundlePrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BundleOrderItemImpl other = (BundleOrderItemImpl) obj;
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
