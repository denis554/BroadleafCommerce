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

package org.broadleafcommerce.core.catalog.wrapper;

import org.broadleafcommerce.common.api.APIWrapper;
import org.broadleafcommerce.common.api.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.RelatedProduct;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a JAXB wrapper for RelatedProducts
 *
 * User: Kelly Tisdell
 * Date: 4/10/12
 */
@XmlRootElement(name = "relatedProduct")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class RelatedProductWrapper extends BaseWrapper implements APIWrapper<RelatedProduct> {

    @XmlElement
    protected Long id;
    
    @XmlElement
    protected Long sequence;
    
    @XmlElement
    protected String promotionalMessage;

    @XmlElement
    protected ProductWrapper product;
    
    @Override
    public void wrap(RelatedProduct model) {
        this.id = model.getId();
        this.sequence = model.getSequence();
        this.promotionalMessage = model.getPromotionMessage();

        product = (ProductWrapper)getEntityConfiguration().createEntityInstance(ProductWrapper.class.getName());
        product.wrap(model.getRelatedProduct());
    }
}
