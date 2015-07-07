/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.admin.server.service.persistence.module.provider.extension;

import org.broadleafcommerce.common.extension.ExtensionHandler;
import org.broadleafcommerce.common.extension.ExtensionResultStatusType;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.openadmin.dto.Property;

/**
 * Extension handler for {@link org.broadleafcommerce.admin.server.service.persistence.module.provider.ProductParentCategoryFieldPersistenceProvider}
 *
 * @author Jeff Fischer
 */
public interface ProductParentCategoryFieldPersistenceProviderExtensionHandler extends ExtensionHandler {

    /**
     * Perform any special handling for the parent category of a product
     *
     * @param property
     * @param product
     * @return
     */
    ExtensionResultStatusType manageParentCategory(Property property, Product product);

}
