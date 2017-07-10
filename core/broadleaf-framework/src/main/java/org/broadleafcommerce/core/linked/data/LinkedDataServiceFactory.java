/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.linked.data;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Jacob Mitash
 */
@Component("blLinkedDataServiceFactory")
public class LinkedDataServiceFactory {

    public LinkedDataService categoryLinkedDataService(String url, List<Product> products) {
        return new CategoryLinkedDataServiceImpl(url, products);
    }

    public LinkedDataService defaultLinkedDataService(String url) {
        return new DefaultLinkedDataServiceImpl(url);
    }

    public LinkedDataService homepageLinkedDataService(String url) {
        return new HomepageLinkedDataServiceImpl(url);
    }

    public LinkedDataService productLinkedDataService(String url, Product product) {
        return new ProductLinkedDataServiceImpl(url, product);
    }
}
