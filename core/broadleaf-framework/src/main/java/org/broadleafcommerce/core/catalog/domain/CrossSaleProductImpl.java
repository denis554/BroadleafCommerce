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

package org.broadleafcommerce.core.catalog.domain;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="BLC_PRODUCT_CROSS_SALE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
@XmlRootElement(name = "crossSaleProduct")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CrossSaleProductImpl implements RelatedProduct {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(generator= "CrossSaleProductId")
    @GenericGenerator(
        name="CrossSaleProductId",
        strategy="org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="table_name", value="SEQUENCE_GENERATOR"),
            @Parameter(name="segment_column_name", value="ID_NAME"),
            @Parameter(name="value_column_name", value="ID_VAL"),
            @Parameter(name="segment_value", value="CrossSaleProductImpl"),
            @Parameter(name="increment_size", value="50"),
            @Parameter(name="entity_name", value="org.broadleafcommerce.core.catalog.domain.CrossSaleProductImpl")
        }
    )
    @Column(name = "CROSS_SALE_PRODUCT_ID")
    private Long id;
	
	@Column(name = "PROMOTION_MESSAGE")
    @AdminPresentation(friendlyName="Cross Sale Promotion Message", largeEntry=true)
    private String promotionMessage;

    @Column(name = "SEQUENCE")
    private Long sequence;
    
	@ManyToOne(targetEntity = ProductImpl.class, optional=false)
    @JoinColumn(name = "PRODUCT_ID")
    @Index(name="CROSSSALE_INDEX", columnNames={"PRODUCT_ID"})
    private Product product = new ProductImpl();

    @ManyToOne(targetEntity = ProductImpl.class, optional=false)
    @JoinColumn(name = "RELATED_SALE_PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    @Index(name="CROSSSALE_RELATED_INDEX", columnNames={"RELATED_SALE_PRODUCT_ID"})
    private Product relatedSaleProduct = new ProductImpl();

    @XmlElement
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement
    public String getPromotionMessage() {
        return promotionMessage;
    }
    
    public void setPromotionMessage(String promotionMessage) {
        this.promotionMessage = promotionMessage;
    }

    @XmlElement
    public Long getSequence() {
        return sequence;
    }
    
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    @XmlElement
    public Product getProduct() {
        return product;
    }

    @XmlElement
    public Product getRelatedProduct() {
        return relatedSaleProduct;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setRelatedProduct(Product relatedSaleProduct) {
        this.relatedSaleProduct = relatedSaleProduct;
    }
}
