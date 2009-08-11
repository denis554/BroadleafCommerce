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

import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.util.money.Money;

public interface DiscreteOrderItem extends OrderItem {

    public Sku getSku();

    public void setSku(Sku sku);

    public Product getProduct();

    public void setProduct(Product product);

    public BundleOrderItem getBundleOrderItem();

    public void setBundleOrderItem(BundleOrderItem bundleOrderItem);

    public String getName();

    public Money getTaxablePrice();

}
