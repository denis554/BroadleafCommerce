/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.offer.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.broadleafcommerce.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.order.domain.FulfillmentGroup;
import org.broadleafcommerce.util.money.Money;

@Entity
@Table(name = "BLC_FG_ADJUSTMENT")
@Inheritance(strategy=InheritanceType.JOINED)
public class FulfillmentGroupAdjustmentImpl implements FulfillmentGroupAdjustment {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "FG_ADJUSTMENT_ID")
    protected Long id;

    @ManyToOne(targetEntity = FulfillmentGroupAdjustmentImpl.class)
    @JoinColumn(name = "FULFILLMENT_GROUP_ID")
    protected FulfillmentGroup fulfillmentGroup;

    @ManyToOne(targetEntity = FulfillmentGroupAdjustmentImpl.class)
    @JoinColumn(name = "OFFER_ID")
    protected Offer offer;

    @Column(name = "ADJUSTMENT_REASON")
    protected String reason;

    @Column(name = "ADJUSTMENT_VALUE")
    protected BigDecimal value;

    public FulfillmentGroupAdjustmentImpl() {
        this(null, null, null);
    }

    public FulfillmentGroupAdjustmentImpl(FulfillmentGroup fulfillmentGroup, Offer offer, String reason){
        this.fulfillmentGroup = fulfillmentGroup;
        this.offer = offer;
        this.reason = reason;
        computeAdjustmentValue();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FulfillmentGroup getFulfillmentGroup() {
        return fulfillmentGroup;
    }

    public void setFulfillmentGroup(FulfillmentGroup fulfillmentGroup) {
        this.fulfillmentGroup = fulfillmentGroup;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Money getValue() {
        return value == null ? null : new Money(value);
    }

    /*
     * Calculates the value of the adjustment
     */
    public void computeAdjustmentValue() {
        if (offer != null && fulfillmentGroup != null) {
            Money adjustmentPrice = fulfillmentGroup.getAdjustmentPrice(); // get the current price of the item with all adjustments
            if (adjustmentPrice == null) {
                adjustmentPrice = fulfillmentGroup.getRetailShippingPrice();
            }
            if (offer.getDiscountType() == OfferDiscountType.AMOUNT_OFF ) {
                value = offer.getValue().getAmount();
            }
            if (offer.getDiscountType() == OfferDiscountType.FIX_PRICE) {
                value = adjustmentPrice.subtract(offer.getValue()).getAmount();
            }
            if (offer.getDiscountType() == OfferDiscountType.PERCENT_OFF) {
                value = adjustmentPrice.multiply(offer.getValue().divide(new BigDecimal("100")).getAmount()).getAmount();
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fulfillmentGroup == null) ? 0 : fulfillmentGroup.hashCode());
        result = prime * result + ((offer == null) ? 0 : offer.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FulfillmentGroupAdjustmentImpl other = (FulfillmentGroupAdjustmentImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (fulfillmentGroup == null) {
            if (other.fulfillmentGroup != null)
                return false;
        } else if (!fulfillmentGroup.equals(other.fulfillmentGroup))
            return false;
        if (offer == null) {
            if (other.offer != null)
                return false;
        } else if (!offer.equals(other.offer))
            return false;
        if (reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
