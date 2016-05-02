/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
/**
 * 
 */
package org.broadleafcommerce.common.i18n.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.i18n.domain.TranslatedEntity;
import org.broadleafcommerce.common.i18n.domain.Translation;

import java.util.Collection;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Thread-local cache structure that contains all of the {@link Translation}s for a batch of processing. This is mainly
 * used when executing a search re-index operation. Rather than go to the database for each item being indexed, it makes
 * more sense to go to the database once, cache all of the results here, and then let the {@link TranslationService}
 * use this instead.
 * 
 * @author Phillip Verheyden (phillipuniverse)
 */
public class TranslationBatchReadCache {
    
    public static final String CACHE_NAME = "blBatchTranslationCache";

    public static Cache getCache() {
        return CacheManager.getInstance().getCache(CACHE_NAME);
    }
    
    public static void clearCache() {
        getCache().removeAll();
    }
    
    public static void addToCache(List<Translation> translations) {
        Collection<Element> translationCacheElements = CollectionUtils.collect(translations, new Transformer<Translation, Element>() {

            @Override
            public Element transform(Translation input) {
                return new Element(buildCacheKey(input), input);
            }
            
        });
        getCache().putAll(translationCacheElements);
    }
    
    public static Translation getFromCache(TranslatedEntity entityType, String id, String propertyName, String localeCode) {
        Element cacheEntry = getCache().get(buildCacheKey(entityType, id, propertyName, localeCode));
        if (cacheEntry == null && StringUtils.contains(localeCode, '_')) {
            String languageWithoutCountryCode = localeCode.substring(localeCode.indexOf('_') + 1);
            cacheEntry = getCache().get(buildCacheKey(entityType, id, propertyName, languageWithoutCountryCode));
        }
        
        return (cacheEntry == null) ? null : (Translation) cacheEntry.getObjectValue();
    }
    
    public static String buildCacheKey(Translation translation) {
        return buildCacheKey(translation.getEntityType(),
            translation.getEntityId(),
            translation.getFieldName(),
            translation.getLocaleCode());
    }
    
    public static String buildCacheKey(TranslatedEntity entityType, String id, String propertyName, String localeCode) {
        return StringUtils.join(new String[]{entityType.getType(), id, propertyName, localeCode}, "-");
    }
}
