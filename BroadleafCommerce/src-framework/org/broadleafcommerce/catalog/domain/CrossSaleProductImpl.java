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
package org.broadleafcommerce.catalog.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_PRODUCT_CROSS_SALE")
public class CrossSaleProductImpl implements RelatedProduct {

    @Id
    @GeneratedValue
    @Column(name = "CROSS_SALE_PRODUCT_ID")
    private Long id;

    @ManyToOne(targetEntity = ProductImpl.class, optional=false)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @ManyToOne(targetEntity = ProductImpl.class, optional=false)
    @JoinColumn(name = "RELATED_SALE_PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    private Product relatedSaleProduct;

    @Column(name = "PROMOTION_MESSAGE")
    private String promotionMessage;

    @Column(name = "SEQUENCE")
    private Long sequence;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Product getProduct() {
        return product;
    }

    @Override
    public String getPromotionMessage() {
        return promotionMessage;
    }

    @Override
    public Product getRelatedSaleProduct() {
        return relatedSaleProduct;
    }

    @Override
    public Long getSequence() {
        return sequence;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public void setPromotionMessage(String promotionMessage) {
        this.promotionMessage = promotionMessage;
    }

    @Override
    public void setRelatedProduct(Product relatedSaleProduct) {
        this.relatedSaleProduct = relatedSaleProduct;
    }

    @Override
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
}
