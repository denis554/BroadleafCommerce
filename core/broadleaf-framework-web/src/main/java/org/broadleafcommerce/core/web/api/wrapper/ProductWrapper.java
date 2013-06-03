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

import org.broadleafcommerce.cms.file.service.StaticAssetService;
import org.broadleafcommerce.common.media.domain.Media;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductAttribute;
import org.broadleafcommerce.core.catalog.domain.RelatedProduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a JAXB wrapper around Product.
 *
 * User: Kelly Tisdell
 * Date: 4/10/12
 */
@XmlRootElement(name = "product")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ProductWrapper extends ProductSummaryWrapper implements APIWrapper<Product> {

    @XmlElement
    protected String longDescription;

    @XmlElement
    protected Date activeStartDate;

    @XmlElement
    protected Date activeEndDate;

    @XmlElement
    protected String manufacturer;

    @XmlElement
    protected String model;

    @XmlElement
    protected String promoMessage;
    
    @XmlElement
    protected Long defaultCategoryId;

    @XmlElement
    protected Money defaultRetailPrice;

    @XmlElement
    protected Money defaultSalePrice;

    @XmlElement(name = "upsaleProduct")
    @XmlElementWrapper(name = "upsaleProducts")
    protected List<RelatedProductWrapper> upsaleProducts;

    @XmlElement(name = "crossSaleProduct")
    @XmlElementWrapper(name = "crossSaleProducts")
    protected List<RelatedProductWrapper> crossSaleProducts;

    @XmlElement(name = "productAttribute")
    @XmlElementWrapper(name = "productAttributes")
    protected List<ProductAttributeWrapper> productAttributes;

    @XmlElement(name = "media")
    @XmlElementWrapper(name = "mediaItems")
    protected List<MediaWrapper> media;

    @Override
    public void wrap(Product model, HttpServletRequest request) {
        super.wrap(model, request);
        this.activeStartDate = model.getActiveStartDate();
        this.activeEndDate = model.getActiveEndDate();
        this.manufacturer = model.getManufacturer();
        this.model = model.getModel();
        this.promoMessage = model.getPromoMessage();
        this.longDescription = model.getLongDescription();
        
        if (model.getDefaultSku() != null) {
            this.defaultRetailPrice = model.getDefaultSku().getRetailPrice();
            this.defaultSalePrice = model.getDefaultSku().getSalePrice();
        }

        if (model.getDefaultCategory() != null) {
            this.defaultCategoryId = model.getDefaultCategory().getId();
        }

        if (model.getUpSaleProducts() != null && !model.getUpSaleProducts().isEmpty()) {
            upsaleProducts = new ArrayList<RelatedProductWrapper>();
            for (RelatedProduct p : model.getUpSaleProducts()) {
                RelatedProductWrapper upsaleProductWrapper =
                        (RelatedProductWrapper) context.getBean(RelatedProductWrapper.class.getName());
                upsaleProductWrapper.wrap(p, request);
                upsaleProducts.add(upsaleProductWrapper);
            }
        }

        if (model.getCrossSaleProducts() != null && !model.getCrossSaleProducts().isEmpty()) {
            crossSaleProducts = new ArrayList<RelatedProductWrapper>();
            for (RelatedProduct p : model.getCrossSaleProducts()) {
                RelatedProductWrapper crossSaleProductWrapper =
                        (RelatedProductWrapper) context.getBean(RelatedProductWrapper.class.getName());
                crossSaleProductWrapper.wrap(p, request);
                crossSaleProducts.add(crossSaleProductWrapper);
            }
        }

        if (model.getProductAttributes() != null && !model.getProductAttributes().isEmpty()) {
            productAttributes = new ArrayList<ProductAttributeWrapper>();
            if (model.getProductAttributes() != null) {
                for (Map.Entry<String, ProductAttribute> entry : model.getProductAttributes().entrySet()) {
                    ProductAttributeWrapper wrapper = (ProductAttributeWrapper) context.getBean(ProductAttributeWrapper.class.getName());
                    wrapper.wrap(entry.getValue(), request);
                    productAttributes.add(wrapper);
                }
            }
        }

        if (model.getMedia() != null && !model.getMedia().isEmpty()) {
            Map<String, Media> mediaMap = model.getMedia();
            media = new ArrayList<MediaWrapper>();
            StaticAssetService staticAssetService = (StaticAssetService) this.context.getBean("blStaticAssetService");
            for (Media med : mediaMap.values()) {
                MediaWrapper wrapper = (MediaWrapper) context.getBean(MediaWrapper.class.getName());
                wrapper.wrap(med, request);
                if (wrapper.isAllowOverrideUrl()) {
                    wrapper.setUrl(staticAssetService.convertAssetPath(med.getUrl(), request.getContextPath(), request.isSecure()));
                }
                media.add(wrapper);
            }
        }
    }

}
