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
package org.broadleafcommerce.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.broadleafcommerce.util.money.Money;

@Entity
@DiscriminatorColumn(name = "TYPE")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FULFILLMENT_GROUP_ITEM")
public class FulfillmentGroupItemImpl implements FulfillmentGroupItem, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FulfillmentGroupItemId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "FulfillmentGroupItemId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "FulfillmentGroupItemImpl", allocationSize = 1)
    @Column(name = "FULFILLMENT_GROUP_ITEM_ID")
    private Long id;

    @ManyToOne(targetEntity = FulfillmentGroupImpl.class)
    @JoinColumn(name = "FULFILLMENT_GROUP_ID")
    private FulfillmentGroup fulfillmentGroup;

    @OneToOne(targetEntity = OrderItemImpl.class)
    @JoinColumn(name = "ORDER_ITEM_ID")
    private OrderItem orderItem;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "RETAIL_PRICE")
    private BigDecimal retailPrice;

    @Column(name = "SALE_PRICE")
    private BigDecimal salePrice;

    @Column(name = "PRICE")
    private BigDecimal price;

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

    public Money getRetailPrice() {
        return retailPrice == null ? null : new Money(retailPrice);
    }

    public void setRetailPrice(Money retailPrice) {
        this.retailPrice = Money.toAmount(retailPrice);
    }

    public Money getSalePrice() {
        return salePrice == null ? null : new Money(salePrice);
    }

    public void setSalePrice(Money salePrice) {
        this.salePrice = Money.toAmount(salePrice);
    }

    public Money getPrice() {
        return price == null ? null : new Money(price);
    }

    public void setPrice(Money price) {
        this.price = Money.toAmount(price);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FulfillmentGroupItemImpl other = (FulfillmentGroupItemImpl) obj;
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        if (orderItem == null) {
            if (other.orderItem != null)
                return false;
        } else if (!orderItem.equals(other.orderItem))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderItem == null) ? 0 : orderItem.hashCode());
        return result;
    }
}
