/*
 * #%L
 * BroadleafCommerce CMS Module
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
package org.broadleafcommerce.cms.page.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.cms.file.service.StaticAssetService;
import org.broadleafcommerce.cms.page.dao.PageDao;
import org.broadleafcommerce.cms.page.domain.Page;
import org.broadleafcommerce.cms.page.domain.PageField;
import org.broadleafcommerce.cms.page.domain.PageTemplate;
import org.broadleafcommerce.common.cache.CacheStatType;
import org.broadleafcommerce.common.cache.StatisticsService;
import org.broadleafcommerce.common.extensibility.jpa.SiteDiscriminator;
import org.broadleafcommerce.common.extension.ExtensionResultHolder;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.locale.service.LocaleService;
import org.broadleafcommerce.common.locale.util.LocaleUtil;
import org.broadleafcommerce.common.page.dto.NullPageDTO;
import org.broadleafcommerce.common.page.dto.PageDTO;
import org.broadleafcommerce.common.rule.RuleProcessor;
import org.broadleafcommerce.common.sandbox.domain.SandBox;
import org.broadleafcommerce.common.template.TemplateOverrideExtensionManager;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by bpolster.
 */
@Service("blPageService")
public class PageServiceImpl implements PageService {

    protected static final Log LOG = LogFactory.getLog(PageServiceImpl.class);
    
    protected static String AND = " && ";
    protected static final String FOREIGN_LOOKUP = "BLC_FOREIGN_LOOKUP";

    @Resource(name="blPageDao")
    protected PageDao pageDao;
    
    @Resource(name="blPageRuleProcessors")
    protected List<RuleProcessor<PageDTO>> pageRuleProcessors;    

    @Resource(name="blLocaleService")
    protected LocaleService localeService;
    
    @Resource(name="blStaticAssetService")
    protected StaticAssetService staticAssetService;

    @Resource(name="blStatisticsService")
    protected StatisticsService statisticsService;

    @Resource(name = "blTemplateOverrideExtensionManager")
    protected TemplateOverrideExtensionManager templateOverrideManager;

    @Resource(name = "blPageServiceUtility")
    protected PageServiceUtility pageServiceUtility;

    @Resource(name = "blPageServiceExtensionManager")
    protected PageServiceExtensionManager extensionManager;

    protected Cache pageCache;
    protected Cache pageMapCache;
    protected final PageDTO NULL_PAGE = new NullPageDTO();

    /**
     * Returns the page with the passed in id.
     *
     * @param pageId - The id of the page.
     * @return The associated page.
     */
    @Override
    public Page findPageById(Long pageId) {
        return pageDao.readPageById(pageId);
    }

    /**
     * Returns the page with the passed in id.
     *
     * @param pageId - The id of the page.
     * @return The associated page.
     */
    @Override
    public Map<String, PageField> findPageFieldMapByPageId(Long pageId) {
        Map<String, PageField> returnMap = new HashMap<String, PageField>();
        List<PageField> pageFields = pageDao.readPageFieldsByPageId(pageId);
        for (PageField pf : pageFields) {
            returnMap.put(pf.getFieldKey(), pf);
        }
        return returnMap;
    }

    @Override
    public PageTemplate findPageTemplateById(Long id) {
        return pageDao.readPageTemplateById(id);
    }
    
    @Override
    @Transactional("blTransactionManager")
    public PageTemplate savePageTemplate(PageTemplate template) {
        return pageDao.savePageTemplate(template);
    }

    /**
     * Retrieve the page if one is available for the passed in uri.
     */
    @Override
    public PageDTO findPageByURI(Locale locale, String uri, Map<String,Object> ruleDTOs, boolean secure) {
        List<PageDTO> returnList = null;
        if (uri != null) {
            Locale languageOnlyLocale = findLanguageOnlyLocale(locale);
            BroadleafRequestContext context = BroadleafRequestContext.getBroadleafRequestContext();
            //store the language only locale for cache since we have to use the lowest common denominator (i.e. the cache
            //locale and the pageTemplate locale used for cache invalidation can be different countries)
            Long sandBox = context.getSandBox() == null?null:context.getSandBox().getId();
            Long site = context.getSite() == null?null:context.getSite().getId();
            String key = buildKey(sandBox, site, languageOnlyLocale, uri);
            key = key + "-" + secure;
            if (context.isProductionSandBox()) {
                returnList = getPageListFromCache(key);
            }
            if (returnList == null) {
                //TODO does this pull the right sandbox in multitenant?
                List<Page> pageList = pageDao.findPageByURI(uri);
                returnList = buildPageDTOList(pageList, secure);
                if (context.isProductionSandBox()) {
                    Collections.sort(returnList, new BeanComparator("priority"));
                    addPageListToCache(returnList, key, uri, sandBox, site);
                }
            }
        }
        
        PageDTO dto = evaluatePageRules(returnList, locale, ruleDTOs);
        
        if (dto.getId() != null) {
            Page page = findPageById(dto.getId());

            ExtensionResultHolder<PageDTO> newDTO = new ExtensionResultHolder<PageDTO>();

            // Allow an extension point to override the page to render.
            extensionManager.getProxy().overridePageDto(newDTO, dto, page);
            if (newDTO.getResult() != null) {
                dto = newDTO.getResult();
            }
        }
        
        if (dto != null) {
            dto = pageServiceUtility.hydrateForeignLookups(dto);
        }
        
        return dto;
    }

    @Override
    public List<Page> readAllPages() {
        return pageDao.readAllPages();
    }

    @Override
    public List<PageTemplate> readAllPageTemplates() {
        return pageDao.readAllPageTemplates();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removePageFromCache(String key) {
        Element e = getPageMapCache().get(key);
        if (e != null && e.getObjectValue() != null) {
            List<String> keys = (List<String>) e.getObjectValue();
            for (String k : keys) {
                getPageCache().remove(k);
            }
        }
    }

    /**
     * Converts a list of pages to a list of pageDTOs.<br>
     * Internally calls buildPageDTO(...).
     *
     * @param pageList
     * @param secure
     * @return
     */
    @Override
    public List<PageDTO> buildPageDTOList(List<Page> pageList, boolean secure) {
        List<PageDTO> dtoList = new ArrayList<PageDTO>();
        if (pageList != null) {
            for(Page page : pageList) {
                dtoList.add(pageServiceUtility.buildPageDTO(page, secure));
            }
        }
        return dtoList;
    }

    protected PageDTO evaluatePageRules(List<PageDTO> pageDTOList, Locale locale, Map<String, Object> ruleDTOs) {
        if (pageDTOList == null) {
            return NULL_PAGE;
        }

        // First check to see if we have a page that matches on the full locale.
        for (PageDTO page : pageDTOList) {
            if (locale != null && locale.getLocaleCode() != null) {
                if (locale.getLocaleCode().equals(page.getLocaleCode())) {
                    if (passesPageRules(page, ruleDTOs)) {
                        return page;
                    }
                }
            }
        }

        // Otherwise, we look for a match using just the language.
        for (PageDTO page : pageDTOList) {
            if (passesPageRules(page, ruleDTOs)) {
                return page;
            }
        }

        return NULL_PAGE;
    }
    

    protected boolean passesPageRules(PageDTO page, Map<String, Object> ruleDTOs) {
        if (pageRuleProcessors != null) {
            for (RuleProcessor<PageDTO> processor : pageRuleProcessors) {
                boolean matchFound = processor.checkForMatch(page, ruleDTOs);
                if (! matchFound) {
                    return false;
                }
            }
        }
        return true;
    }

    protected Locale findLanguageOnlyLocale(Locale locale) {
        if (locale != null ) {
            Locale languageOnlyLocale = localeService.findLocaleByCode(LocaleUtil.findLanguageCode(locale));
            if (languageOnlyLocale != null) {
                return languageOnlyLocale;
            }
        }
        return locale;
    }

    protected String buildKey(Long currentSandBox, Long site, Locale locale, String uri) {
        StringBuilder key = new StringBuilder(uri);
        if (locale != null) {
            key.append("-").append(locale.getLocaleCode());
        }

        if (currentSandBox != null) {
            key.append("-").append(currentSandBox);
        }

        if (site != null) {
            key.append("-").append(site);
        }

        return key.toString();
    }

    @Override
    public Cache getPageCache() {
        if (pageCache == null) {
            pageCache = CacheManager.getInstance().getCache("cmsPageCache");
        }
        return pageCache;
    }

    @Override
    public Cache getPageMapCache() {
        if (pageMapCache == null) {
            pageMapCache = CacheManager.getInstance().getCache("cmsPageMapCache");
        }
        return pageMapCache;
    }

    protected String buildKey(SandBox sandBox, Page page) {
        Locale locale = page.getPageTemplate() == null ? null : page.getPageTemplate().getLocale();
        Long sandBoxId = sandBox==null?null:sandBox.getId();
        Long siteId = (page instanceof SiteDiscriminator)?((SiteDiscriminator) page).getSiteDiscriminator():null;        
        return buildKey(sandBoxId, siteId, findLanguageOnlyLocale(locale), page.getFullUrl());
    }

    protected void addPageListToCache(List<PageDTO> pageList, String key, String uri, Long sandBox, Long site) {
        getPageCache().put(new Element(key, pageList));
        
        addPageMapCacheEntry(key, uri, sandBox, site);
        if (site != null) {
            addPageMapCacheEntry(key, uri, sandBox, null);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void addPageMapCacheEntry(String keyToStore, String uri, Long sandBox, Long site) {
        String key = getPageMapCacheKey(uri, sandBox, site);

        Element e = getPageMapCache().get(key);
        if (e == null || e.getObjectValue() == null) {
            List<String> keys = new ArrayList<String>();
            keys.add(keyToStore);
            getPageMapCache().put(new Element(key, keys));
        } else {
            ((List<String>) e.getObjectValue()).add(keyToStore);
        }
    }
    
    @Override
    public String getPageMapCacheKey(String uri, Long sandBox, Long site) {
        return uri + "-" + sandBox + "-" + (site == null ? "ALL" : site);
    }

    protected List<PageDTO> getPageListFromCache(String key) {
        Element cacheElement = getPageCache().get(key);
        if (cacheElement != null && cacheElement.getValue() != null) {
            statisticsService.addCacheStat(CacheStatType.PAGE_CACHE_HIT_RATE.toString(), true);
            return (List<PageDTO>) cacheElement.getValue();
        }
        statisticsService.addCacheStat(CacheStatType.PAGE_CACHE_HIT_RATE.toString(), false);
        return null;
    }

}
