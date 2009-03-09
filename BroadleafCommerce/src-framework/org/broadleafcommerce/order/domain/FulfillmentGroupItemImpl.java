package org.broadleafcommerce.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.broadleafcommerce.promotion.domain.Offer;
import org.broadleafcommerce.promotion.domain.OfferAudit;
import org.broadleafcommerce.promotion.domain.OfferAuditImpl;
import org.broadleafcommerce.promotion.domain.OfferImpl;

@Entity
@DiscriminatorColumn(name="TYPE")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FULFILLMENT_GROUP_ITEM")
public class FulfillmentGroupItemImpl implements FulfillmentGroupItem, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "FULFILLMENT_GROUP_ID")
    private Long fulfillmentGroupId;

    @OneToOne(targetEntity=OrderItemImpl.class)
    @JoinColumn(name = "ORDER_ID")
    private OrderItem orderItem;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "RETAIL_PRICE")
    private BigDecimal retailPrice;
    
    @Column(name = "SALE_PRICE")
    private BigDecimal salePrice;
    
    @Column(name = "PRICE")
    private BigDecimal price;
    
    @OneToMany(mappedBy = "id", targetEntity = OfferImpl.class)
    private List<Offer> candidateOffers;
    
    @OneToMany(mappedBy = "id", targetEntity = OfferAuditImpl.class)
    private List<OfferAudit> appliedOffers;
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFulfillmentGroupId() {
        return fulfillmentGroupId;
    }

    public void setFulfillmentGroupId(Long fulfillmentGroupId) {
        this.fulfillmentGroupId = fulfillmentGroupId;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public void addAppliedOffer(OfferAudit offer) {
		appliedOffers.add(offer);
	}

	@Override
	public void addCandidateOffer(Offer offer) {
		candidateOffers.add(offer);
	}

	@Override
	public List<OfferAudit> getAppliedOffers() {
		return appliedOffers;
	}

	@Override
	public List<Offer> getCandidateOffers() {
		return candidateOffers;
	}

	@Override
	public void removeAppliedOffer(OfferAudit offer) {
		appliedOffers.remove(offer);
		
	}

	@Override
	public void removeCandidateOffer(Offer offer) {
		candidateOffers.remove(offer);
	}

	@Override
	public void setAppliedOffers(List<OfferAudit> offers) {
		this.appliedOffers = offers;
	}

	@Override
	public void setCandidateOffers(List<Offer> offers) {
		this.candidateOffers = offers;
	}
    
    
}
