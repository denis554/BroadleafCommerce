/*
 * #%L
 * BroadleafCommerce Advanced CMS
 * %%
 * Copyright (C) 2009 - 2015 Broadleaf Commerce
 * %%
 * NOTICE:  All information contained herein is, and remains
 * the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained
 * herein are proprietary to Broadleaf Commerce, LLC
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Broadleaf Commerce, LLC.
 * #L%
 */
package org.broadleafcommerce.admin.persistence.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.sandbox.SandBoxHelper;
import org.broadleafcommerce.core.catalog.domain.ProductBundle;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.openadmin.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.PropertyValidationResult;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.UriPropertyValidator;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.ValidationConfigurationBasedPropertyValidator;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Validates that a ProductBundle cannot have its own Default Sku selected as a Sku Bundle Item
 * 
 * 
 * @author Chris Kittrell (ckittrell)
 */
@Component("blProductBundleSkuBundleItemValidator")
public class ProductBundleSkuBundleItemValidator extends ValidationConfigurationBasedPropertyValidator {

    protected static final Log LOG = LogFactory.getLog(UriPropertyValidator.class);
    private static final String ERROR_MESSAGE = "A Product Bundle's Sku Bundle Items are not allowed to include the Product Bundle's Default Sku.";

    @Resource(name = "blCatalogService")
    public CatalogService catalogService;

    @Resource(name="blSandBoxHelper")
    protected SandBoxHelper sandBoxHelper;

    @Override
    public PropertyValidationResult validate(Entity entity, Serializable instance, Map<String, FieldMetadata> entityFieldMetadata,
            Map<String, String> validationConfiguration, BasicFieldMetadata propertyMetadata, String propertyName,
            String value) {
        String skuId = entity.findProperty("sku") == null ? null : entity.findProperty("sku").getValue();
        String bundleId = entity.findProperty("bundle") == null ? null : entity.findProperty("bundle").getValue();

        if (skuId != null && bundleId != null) {
            ProductBundle productBundle = (ProductBundle) catalogService.findProductById(Long.valueOf(bundleId));
            Long defaultSkuOrigId = sandBoxHelper.getOriginalId(Sku.class, productBundle.getDefaultSku().getId()).getOriginalId();

            if (Long.valueOf(skuId) == defaultSkuOrigId) {
                return new PropertyValidationResult(false, ERROR_MESSAGE);
            }
        }

        return new PropertyValidationResult(true);
    }
}
