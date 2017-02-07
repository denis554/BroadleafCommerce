/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
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
package org.broadleafcommerce.core.web.expression;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.util.BLCMessageUtils;
import org.broadleafcommerce.common.util.BLCSystemProperty;
import org.broadleafcommerce.common.web.expression.BroadleafVariableExpression;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;

import java.util.Arrays;
import java.util.List;

public class DefaultSEOVariableExpression implements BroadleafVariableExpression {

    private static final Log LOG = LogFactory.getLog(DefaultSEOVariableExpression.class);

    @Override
    public String getName() {
        return "seo";
    }

    public String getSiteSimpleURL() {
        return BLCMessageUtils.getMessage("seo.site.simple.url");
    }

    public String getCategoryTitlePattern() {
        return BLCSystemProperty.resolveSystemProperty("seo.category.title.pattern");
    }

    public String getCategoryTitle(Category category) {
        try {
            String title = category.getMetaTitle();
            if (StringUtils.isEmpty(title)) {
                title = category.getName();
            }
            return title;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String getCategoryDescriptionPattern() {
        return BLCSystemProperty.resolveSystemProperty("seo.category.description.pattern");
    }

    public String getCategoryDescription(Category category) {
        try {
            String description = category.getMetaDescription();
            return StringUtils.isEmpty(description) ? "" : ". " + description;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String getProductTitlePattern(Category category) {
        try {
            String pattern = category.getProductTitlePatternOverride();
            if (StringUtils.isEmpty(pattern)) {
                pattern = BLCSystemProperty.resolveSystemProperty("seo.product.title.pattern");
            }
            return pattern;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String getProductTitle(Product product) {
        try {
            String title = product.getMetaTitle();
            if (StringUtils.isEmpty(title)) {
                title = product.getName();
            }
            return title;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String getProductDescriptionPattern(Category category) {
        try {
            String pattern = category.getProductDescriptionPatternOverride();
            if (StringUtils.isEmpty(pattern)) {
                pattern = BLCSystemProperty.resolveSystemProperty("seo.product.description.pattern");
            }
            return pattern;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String getProductDescription(Product product) {
        try {
            String description = product.getMetaDescription();
            return StringUtils.isEmpty(description) ? "" : ". " + description;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public String buildTitleString(List<String> titleElements, String elementDelimiter) {
        titleElements.removeAll(Arrays.asList(null, ""));
        String result = StringUtils.join(titleElements, elementDelimiter);
        return StringUtils.isNotEmpty(result) ? result : getSiteSimpleURL();
    }
}
