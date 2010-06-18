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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The Class CategoryProductImpl is the default implmentation of {@link Category}.
 * This entity is only used for executing a named query.
 * 
 * If you want to add fields specific to your implementation of BroadLeafCommerce you should extend
 * this class and add your fields.  If you need to make significant changes to the class then you
 * should implement your own version of {@link Category}.
 * <br>
 * <br>
 * This implementation uses a Hibernate implementation of JPA configured through annotations.
 * The Entity references the following tables:
 * BLC_CATEGORY_PRODUCT_XREF,
 * 
 * @see {@link Category}, {@link ProductImpl}
 * @author btaylor
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_CATEGORY_PRODUCT_XREF")
public class CategoryProductImpl implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

      @EmbeddedId
      CategoryProductXrefPk categoryProductXref;

    public CategoryProductXrefPk getCategoryProductXref() {
		return categoryProductXref;
	}
    
    public void setCategoryProductXref(CategoryProductXrefPk categoryProductXref) {
		this.categoryProductXref = categoryProductXref;
	}

	/** The display order. */
    @Column(name = "DISPLAY_ORDER")
    protected Integer displayOrder;

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.CategoryProduct#getDisplayOrder()
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.CategoryProduct#setDisplayOrder(java.lang.Integer)
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public static class CategoryProductXrefPk implements Serializable{
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
        
        @ManyToOne(targetEntity = CategoryImpl.class, optional=false)
        @JoinColumn(name = "CATEGORY_ID")
        protected Category category;
        
        /** The product. */
        @ManyToOne(targetEntity = ProductImpl.class, optional=false)
        @JoinColumn(name = "PRODUCT_ID")
        protected Product product;

		public Category getCategory() {
			return category;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		public Product getProduct() {
			return product;
		}

		public void setProduct(Product product) {
			this.product = product;
		}

		@Override
		public int hashCode() {
			return category.hashCode() + product.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
	        if (obj == null) return false;
	        else if (!(obj instanceof CategoryProductXrefPk)) return false;

	        return category.getId().equals(((CategoryProductXrefPk) obj).category.getId())
	        && product.getId().equals(((CategoryProductXrefPk) obj).product.getId());
		}
		
		
        
    }


    
    
}
