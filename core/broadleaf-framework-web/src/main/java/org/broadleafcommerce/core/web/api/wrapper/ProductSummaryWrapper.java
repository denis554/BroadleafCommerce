/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.core.web.api.wrapper;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductOption;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "productSummary")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ProductSummaryWrapper extends BaseWrapper implements APIWrapper<Product> {

    @XmlElement
    protected Long id;

    @XmlElement
    protected String name;

    @XmlElement
    protected String description;

    @XmlElement(name = "productOption")
    @XmlElementWrapper(name = "productOptions")
    protected List<ProductOptionWrapper> productOptions;

    @Override
    public void wrap(Product model, HttpServletRequest request) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();

        if (model.getProductOptions() != null && !model.getProductOptions().isEmpty()) {
            this.productOptions = new ArrayList<ProductOptionWrapper>();
            List<ProductOption> options = model.getProductOptions();
            for (ProductOption option : options) {
                ProductOptionWrapper optionWrapper = (ProductOptionWrapper) context.getBean(ProductOptionWrapper.class.getName());
                optionWrapper.wrap(option, request);
                this.productOptions.add(optionWrapper);
            }
        }
    }
}
