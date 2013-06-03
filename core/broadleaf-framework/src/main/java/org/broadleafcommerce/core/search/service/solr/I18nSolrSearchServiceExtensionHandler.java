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

package org.broadleafcommerce.core.search.service.solr;

import org.apache.commons.beanutils.PropertyUtils;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.extension.ExtensionResultStatusType;
import org.broadleafcommerce.core.search.domain.Field;
import org.broadleafcommerce.core.search.domain.solr.FieldType;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * If the field is translatable, then this method prefixes the field with supported locales.
 * 
 * @author bpolster
 */
@Service("blI18nSolrSearchServiceExtensionHandler")
public class I18nSolrSearchServiceExtensionHandler extends AbstractSolrSearchServiceExtensionHandler
        implements SolrSearchServiceExtensionHandler {

    @Resource(name = "blSolrHelperService")
    protected SolrHelperService shs;

    @Resource(name = "blSolrSearchServiceExtensionManager")
    protected SolrSearchServiceExtensionManager extensionManager;

    @PostConstruct
    public void init() {
        extensionManager.getHandlers().add(this);
    }

    @Override
    public ExtensionResultStatusType buildPrefixListForSearchableFacet(Field field, List<String> prefixList) {
        return getLocalePrefix(field, prefixList);
    }

    @Override
    public ExtensionResultStatusType buildPrefixListForSearchableField(Field field, FieldType searchableFieldType, List<String> prefixList) {
        return getLocalePrefix(field, prefixList);
    }

    @Override
    public ExtensionResultStatusType addPropertyValues(Product product, Field field, FieldType fieldType,
            Map<String, Object> values, String propertyName, List<Locale> locales)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        ExtensionResultStatusType result = ExtensionResultStatusType.NOT_HANDLED;
        if (field.getTranslatable()) {
            result = ExtensionResultStatusType.HANDLED;

            for (Locale locale : locales) {
                String localeCode = locale.getLocaleCode();

                // To fetch the appropriate translated property, we need to set the current request context's locale
                if (field.getTranslatable()) {
                    BroadleafRequestContext tempContext = new BroadleafRequestContext();
                    tempContext.setLocale(locale);
                    BroadleafRequestContext.setBroadleafRequestContext(tempContext);
                }

                Object propertyValue = PropertyUtils.getProperty(product, propertyName);
                values.put(localeCode, propertyValue);
            }
        }
        return result;
    }

    /**
     * If the field is translatable, take the current locale and add that as a prefix.
     * @param context
     * @param field
     * @return
     */
    protected ExtensionResultStatusType getLocalePrefix(Field field, List<String> prefixList) {
        if (field.getTranslatable()) {
            if (BroadleafRequestContext.getBroadleafRequestContext() != null) {
                Locale locale = BroadleafRequestContext.getBroadleafRequestContext().getLocale();
                if (locale != null) {
                    prefixList.add(locale.getLocaleCode());
                    return ExtensionResultStatusType.HANDLED_CONTINUE;
                }
            }
        }

        return ExtensionResultStatusType.NOT_HANDLED;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}
