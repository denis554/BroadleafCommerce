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

package org.broadleafcommerce.cms.structure.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.cms.common.AbstractContentService;
import org.broadleafcommerce.cms.file.service.StaticAssetService;
import org.broadleafcommerce.cms.structure.dao.StructuredContentDao;
import org.broadleafcommerce.cms.structure.domain.StructuredContent;
import org.broadleafcommerce.cms.structure.domain.StructuredContentField;
import org.broadleafcommerce.cms.structure.domain.StructuredContentImpl;
import org.broadleafcommerce.cms.structure.domain.StructuredContentItemCriteria;
import org.broadleafcommerce.cms.structure.domain.StructuredContentRule;
import org.broadleafcommerce.cms.structure.domain.StructuredContentType;
import org.broadleafcommerce.cms.structure.dto.ItemCriteriaDTO;
import org.broadleafcommerce.cms.structure.dto.StructuredContentDTO;
import org.broadleafcommerce.cms.structure.message.ArchivedStructuredContentPublisher;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.sandbox.dao.SandBoxDao;
import org.broadleafcommerce.common.sandbox.domain.SandBox;
import org.broadleafcommerce.common.sandbox.domain.SandBoxType;
import org.broadleafcommerce.openadmin.server.dao.SandBoxItemDao;
import org.broadleafcommerce.openadmin.server.domain.SandBoxItem;
import org.broadleafcommerce.openadmin.server.domain.SandBoxItemType;
import org.broadleafcommerce.openadmin.server.domain.SandBoxOperationType;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bpolster
 */
@Service("blStructuredContentService")
public class StructuredContentServiceImpl extends AbstractContentService implements StructuredContentService {
    private static final Log LOG = LogFactory.getLog(StructuredContentServiceImpl.class);

    private static String AND = " && ";

    @Resource(name="blStructuredContentDao")
    protected StructuredContentDao structuredContentDao;

    @Resource(name="blSandBoxItemDao")
    protected SandBoxItemDao sandBoxItemDao;

    @Resource(name="blSandBoxDao")
    protected SandBoxDao sandBoxDao;
    
    @Resource(name="blStaticAssetService")
    protected StaticAssetService staticAssetService;

    @Resource(name="blContentRuleProcessors")
    protected List<StructuredContentRuleProcessor> contentRuleProcessors;

    @Value("${automatically.approve.structured.content}")
    protected boolean automaticallyApproveAndPromoteStructuredContent=true;

    protected Cache structuredContentCache;

    protected List<ArchivedStructuredContentPublisher> archivedStructuredContentListeners;

    @Override
    public StructuredContent findStructuredContentById(Long contentId) {
        return structuredContentDao.findStructuredContentById(contentId);
    }

    @Override
    public StructuredContentType findStructuredContentTypeById(Long id) {
        return structuredContentDao.findStructuredContentTypeById(id);
    }

    @Override
    public StructuredContentType findStructuredContentTypeByName(String name) {
        return structuredContentDao.findStructuredContentTypeByName(name);
    }

    @Override
    public List<StructuredContentType> retrieveAllStructuredContentTypes() {
        return structuredContentDao.retrieveAllStructuredContentTypes();
    }

    @Override
    public Map<String, StructuredContentField> findFieldsByContentId(Long contentId) {
        StructuredContent sc = findStructuredContentById(contentId);
        return structuredContentDao.readFieldsForStructuredContentItem(sc);
    }

    @Override
    public List<StructuredContent> findContentItems(SandBox sandbox, Criteria c) {
        return (List<StructuredContent>) findItems(sandbox, c, StructuredContent.class, StructuredContentImpl.class, "originalItemId");
    }

    @Override
    public Long countContentItems(SandBox sandbox, Criteria c) {
       return countItems(sandbox, c, StructuredContentImpl.class, "originalItemId");
    }

    @Override
    public StructuredContent addStructuredContent(StructuredContent content, SandBox destinationSandbox) {
        if (automaticallyApproveAndPromoteStructuredContent) {
            if (destinationSandbox != null && destinationSandbox.getSite() != null) {
                destinationSandbox = destinationSandbox.getSite().getProductionSandbox();
            } else {
                // Null means production for single-site installations.
                destinationSandbox = null;
            }
        }

        content.setSandbox(destinationSandbox);
        content.setArchivedFlag(false);
        content.setDeletedFlag(false);
        StructuredContent sc = structuredContentDao.addOrUpdateContentItem(content);
        if (! isProductionSandBox(destinationSandbox)) {
            sandBoxItemDao.addSandBoxItem(destinationSandbox, SandBoxOperationType.ADD, SandBoxItemType.STRUCTURED_CONTENT, sc.getContentName(), sc.getId(), null);
        }
        return sc;
    }

    @Override
    public StructuredContent updateStructuredContent(StructuredContent content, SandBox destSandbox) {
        if (content.getLockedFlag()) {
            throw new IllegalArgumentException("Unable to update a locked record");
        }

        if (automaticallyApproveAndPromoteStructuredContent) {
            if (destSandbox != null && destSandbox.getSite() != null) {
                destSandbox = destSandbox.getSite().getProductionSandbox();
            } else {
                // Null means production for single-site installations.
                destSandbox = null;
            }
        }

        if (checkForSandboxMatch(content.getSandbox(), destSandbox)) {
            if (content.getDeletedFlag()) {
                SandBoxItem item = sandBoxItemDao.retrieveBySandboxAndTemporaryItemId(content.getSandbox(), SandBoxItemType.STRUCTURED_CONTENT, content.getId());
                if (content.getOriginalItemId() == null && item != null) {
                    // This page was added in this sandbox and now needs to be deleted.
                    content.setArchivedFlag(true);
                    item.setArchivedFlag(true);
                } else if (item != null) {
                    // This page was being updated but now is being deleted - so change the
                    // sandbox operation type to deleted
                    item.setSandBoxOperationType(SandBoxOperationType.DELETE);
                    sandBoxItemDao.updateSandBoxItem(item);
                } else if (automaticallyApproveAndPromoteStructuredContent) {
                    content.setArchivedFlag(true);
                }

            }
            return structuredContentDao.addOrUpdateContentItem(content);
        } else if (checkForProductionSandbox(content.getSandbox())) {
            // The passed in content is an existing content item whose values were updated
            // Instead, we want to create a clone of this item for the destSandbox

            // Create the clone
            StructuredContent clonedContent = content.cloneEntity();
            clonedContent.setOriginalItemId(content.getId());
            clonedContent.setSandbox(destSandbox);

            // Detach the old item so it doesn't get updated
            structuredContentDao.detach(content);

            // Update the new item
            StructuredContent returnContent = structuredContentDao.addOrUpdateContentItem(clonedContent);

            // Lookup the previous item so that we can update its locked status
            StructuredContent prod = findStructuredContentById(content.getId());
            prod.setLockedFlag(true);
            prod = structuredContentDao.addOrUpdateContentItem(prod);

            SandBoxOperationType type = SandBoxOperationType.UPDATE;
            if (clonedContent.getDeletedFlag()) {
                type = SandBoxOperationType.DELETE;
            }

            sandBoxItemDao.addSandBoxItem(destSandbox, type, SandBoxItemType.STRUCTURED_CONTENT, returnContent.getContentName(), returnContent.getId(), returnContent.getOriginalItemId());
            return returnContent;
        } else {
            // This should happen via a promote, revert, or reject in the sandbox service
            throw new IllegalArgumentException("Update called when promote or reject was expected.");
        }
    }

    private boolean checkForSandboxMatch(SandBox src, SandBox dest) {
        if (src != null) {
            if (dest != null) {
                return src.getId().equals(dest.getId());
            }
        }
        return (src == null && dest == null);
    }

    private boolean checkForProductionSandbox(SandBox dest) {
        boolean productionSandbox = false;

        if (dest == null) {
            productionSandbox = true;
        } else {
            if (dest.getSite() != null && dest.getSite().getProductionSandbox() != null && dest.getSite().getProductionSandbox().getId() != null) {
                productionSandbox = dest.getSite().getProductionSandbox().getId().equals(dest.getId());
            }
        }

        return productionSandbox;
    }

    @Override
    public void deleteStructuredContent(StructuredContent content, SandBox destinationSandbox) {
        content.setDeletedFlag(true);
        updateStructuredContent(content, destinationSandbox);
    }
    
    private String buildRuleExpression(StructuredContent sc) {
       StringBuffer ruleExpression = null;
       Map<String, StructuredContentRule> ruleMap = sc.getStructuredContentMatchRules();
       if (ruleMap != null) {
           for (String ruleKey : ruleMap.keySet()) {
               if (ruleExpression == null) {
                   ruleExpression = new StringBuffer(ruleMap.get(ruleKey).getMatchRule());
               } else {
                   ruleExpression.append(AND);
                   ruleExpression.append(ruleMap.get(ruleKey).getMatchRule());
               }
           }
       }
       if (ruleExpression != null) {
           return ruleExpression.toString();
       } else {
           return null;
       }
    }
    
    private List<ItemCriteriaDTO> buildItemCriteriaDTOList(StructuredContent sc) {
        List<ItemCriteriaDTO> itemCriteriaDTOList = new ArrayList<ItemCriteriaDTO>();
        for(StructuredContentItemCriteria criteria : sc.getQualifyingItemCriteria()) {
            ItemCriteriaDTO criteriaDTO = new ItemCriteriaDTO();
            criteriaDTO.setMatchRule(criteria.getOrderItemMatchRule());
            criteriaDTO.setQty(criteria.getQuantity());
            itemCriteriaDTOList.add(criteriaDTO);
        }
        return itemCriteriaDTOList;
    }            
    
    private void buildFieldValues(StructuredContent sc, StructuredContentDTO scDTO, boolean secure) {
        String envPrefix = staticAssetService.getStaticAssetEnvironmentUrlPrefix();
        if (envPrefix != null && secure) {
            envPrefix = staticAssetService.getStaticAssetEnvironmentSecureUrlPrefix();
        }
        String cmsPrefix = staticAssetService.getStaticAssetUrlPrefix();

        for (String fieldKey : sc.getStructuredContentFields().keySet()) {
            StructuredContentField scf = sc.getStructuredContentFields().get(fieldKey);
            String originalValue = scf.getValue();
            if (StringUtils.isNotBlank(envPrefix) && StringUtils.isNotBlank(originalValue) && StringUtils.isNotBlank(cmsPrefix) && originalValue.contains(cmsPrefix)) {
                if (originalValue.startsWith("/")) {
                    originalValue = originalValue.substring(1);
                }
                String fldValue = originalValue.replaceAll(cmsPrefix, envPrefix+cmsPrefix);
                scDTO.getValues().put(fieldKey, fldValue);
            } else {
                scDTO.getValues().put(fieldKey, originalValue);
            }
        }
    }

    /**
     * Converts a list of structured content items to a list of structured content DTOs.<br>
     * Internally calls buildStructuredContentDTO(...).
     *
     * @param structuredContentList
     * @param secure
     * @return
     */
    protected List<StructuredContentDTO> buildStructuredContentDTOList(List<StructuredContent> structuredContentList, boolean secure) {
        List<StructuredContentDTO> dtoList = new ArrayList<StructuredContentDTO>();
        if (structuredContentList != null) {
            for(StructuredContent sc : structuredContentList) {
                dtoList.add(buildStructuredContentDTO(sc, secure));
            }
        }
        return dtoList;
    }


    /**
     * Converts a StructuredContent into a StructuredContentDTO.   If the item contains fields with
     * broadleaf cms urls, the urls are converted to utilize the domain
     * @param sc
     * @param secure
     * @return
     */
    protected StructuredContentDTO buildStructuredContentDTO(StructuredContent sc, boolean secure) {
        StructuredContentDTO scDTO = new StructuredContentDTO();
        scDTO.setContentName(sc.getContentName());
        scDTO.setContentType(sc.getStructuredContentType().getName());
        scDTO.setId(sc.getId());
        scDTO.setPriority(sc.getPriority());
        
        if (sc.getLocale() != null) {
            scDTO.setLocaleCode(sc.getLocale().getLocaleCode());
        }
        
        if (sc.getSandbox() != null) {
            scDTO.setSandboxId(sc.getSandbox().getId());
        }

        scDTO.setRuleExpression(buildRuleExpression(sc));
        buildFieldValues(sc, scDTO, secure);
        
        if (sc.getQualifyingItemCriteria() != null && sc.getQualifyingItemCriteria().size() > 0) {
            scDTO.setItemCriteriaDTOList(buildItemCriteriaDTOList(sc));
        }
        return scDTO;
        
    }


    private List<StructuredContentDTO> mergeContent(List<StructuredContentDTO> productionList, List<StructuredContent> sandboxList, boolean secure) {
        if (sandboxList == null || sandboxList.size() == 0) {
            return productionList;
        }

        Map<Long,StructuredContentDTO> scMap = new LinkedHashMap<Long,StructuredContentDTO>();
        if (productionList != null) {
            for(StructuredContentDTO sc : productionList) {
                scMap.put(sc.getId(), sc);
            }
        }

        for(StructuredContent sc : sandboxList) {
            if (sc.getOriginalItemId() != null) {
                scMap.remove(sc.getOriginalItemId());
            }

            if (! sc.getDeletedFlag() && ! sc.getOfflineFlag()) {
                StructuredContentDTO convertedItem = buildStructuredContentDTO(sc, secure);
                scMap.put(sc.getId(), convertedItem);
            }
        }

        ArrayList<StructuredContentDTO> returnList = new ArrayList<StructuredContentDTO>(scMap.values());

        if (returnList.size()  > 1) {
            Collections.sort(returnList, new BeanComparator("priority"));
        }

        return returnList;
    }

    private List<StructuredContentDTO> evaluateAndPriortizeContent(List<StructuredContentDTO> structuredContentList, int count, Map<String, Object> ruleDTOs) {
        // some optimization for single item lists which don't require prioritization
        if (structuredContentList.size() == 1) {
            if (processContentRules(structuredContentList.get(0), ruleDTOs)) {
                return structuredContentList;
            } else {
                return new ArrayList<StructuredContentDTO>();
            }
        }

        Iterator<StructuredContentDTO> structuredContentIterator = structuredContentList.iterator();
        List<StructuredContentDTO> returnList = new ArrayList<StructuredContentDTO>();
        List<StructuredContentDTO> tmpList = new ArrayList<StructuredContentDTO>();
        Integer lastPriority = Integer.MIN_VALUE;
        while (structuredContentIterator.hasNext()) {
            StructuredContentDTO sc = structuredContentIterator.next();
            if (! lastPriority.equals(sc.getPriority())) {
                // If we've moved to another priority, then shuffle all of the items
                // with the previous priority and add them to the return list.
                if (tmpList.size() > 1) {
                    Collections.shuffle(tmpList);
                }
                returnList.addAll(tmpList);

                tmpList.clear();

                // If we've added enough items to satisfy the count, then return the
                // list.
                if (returnList.size() == count) {
                    return returnList;
                } else if (returnList.size() > count) {
                    return returnList.subList(0, count);
                } else {
                    if (processContentRules(sc, ruleDTOs)) {
                        tmpList.add(sc);
                    }
                }
            } else {
                if (processContentRules(sc, ruleDTOs)) {
                    tmpList.add(sc);
                }
            }
            lastPriority = sc.getPriority();
        }

        if (tmpList.size() > 1) {
            Collections.shuffle(tmpList);
        }

        returnList.addAll(tmpList);


        if (returnList.size() > count) {
            return returnList.subList(0, count);
        }
        return returnList;
    }

    private boolean processContentRules(StructuredContentDTO sc, Map<String, Object> ruleDTOs) {
        if (contentRuleProcessors != null) {
            for (StructuredContentRuleProcessor processor : contentRuleProcessors) {
                boolean matchFound = processor.checkForMatch(sc, ruleDTOs);
                if (! matchFound) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<StructuredContentDTO> lookupStructuredContentItemsByType(SandBox sandBox, StructuredContentType contentType, Locale locale, Integer count, Map<String, Object> ruleDTOs, boolean secure) {
        
        List<StructuredContent> sandBoxContentList = null;
        
        String cacheKey = buildTypeKey(getProductionSandBox(sandBox), locale, contentType.getName()); 
        cacheKey = cacheKey+"-"+secure;
        List<StructuredContentDTO> productionContentDTOList = getStructuredContentListFromCache(cacheKey);
        if (productionContentDTOList == null) {
            List<StructuredContent> productionContentList = structuredContentDao.findActiveStructuredContentByType(getProductionSandBox(sandBox), contentType, locale);
            productionContentDTOList = buildStructuredContentDTOList(productionContentList, secure);
            if (productionContentDTOList != null) {
                addStructuredContentListToCache(cacheKey, productionContentDTOList);
            }
        }
        
        final List<StructuredContentDTO> contentList;
        if (! isProductionSandBox(sandBox)) {
            sandBoxContentList = structuredContentDao.findActiveStructuredContentByType(sandBox, contentType, locale);
            contentList = mergeContent(productionContentDTOList, sandBoxContentList, secure);
        } else {
            contentList = productionContentDTOList;
        }

        return evaluateAndPriortizeContent(contentList, count, ruleDTOs);
    }

    @Override
    public List<StructuredContentDTO> lookupStructuredContentItemsByName(SandBox sandBox, StructuredContentType contentType, String contentName, org.broadleafcommerce.common.locale.domain.Locale locale, Integer count, Map<String, Object> ruleDTOs, boolean secure) {
        List<StructuredContent> sandBoxContentList = null;
        
        String cacheKey = buildNameKey(getProductionSandBox(sandBox), locale, contentType.getName(), contentName); 
        cacheKey = cacheKey+"-"+secure;
        List<StructuredContentDTO> productionContentDTOList = getStructuredContentListFromCache(cacheKey);
        if (productionContentDTOList == null) {                
            List<StructuredContent> productionContentList = structuredContentDao.findActiveStructuredContentByNameAndType(getProductionSandBox(sandBox), contentType, contentName, locale);
            productionContentDTOList = buildStructuredContentDTOList(productionContentList, secure);
            if (productionContentDTOList != null) {
                addStructuredContentListToCache(cacheKey, productionContentDTOList);
            }
        }

        final List<StructuredContentDTO> contentList;
        if (! isProductionSandBox(sandBox)) {
            sandBoxContentList = structuredContentDao.findActiveStructuredContentByNameAndType(sandBox, contentType, contentName, locale);
            contentList = mergeContent(productionContentDTOList, sandBoxContentList, secure);
        } else {
            contentList = productionContentDTOList;
        }

        return evaluateAndPriortizeContent(contentList, count, ruleDTOs);
    }

    @Override
    public List<StructuredContentDTO> lookupStructuredContentItemsByName(SandBox sandBox, String contentName, org.broadleafcommerce.common.locale.domain.Locale locale, Integer count, Map<String, Object> ruleDTOs, boolean secure) {
        List<StructuredContent> sandBoxContentList = null;

        String cacheKey = buildNameKey(getProductionSandBox(sandBox), locale, "any", contentName);
        cacheKey = cacheKey+"-"+secure;
        List<StructuredContentDTO> productionContentDTOList = getStructuredContentListFromCache(cacheKey);
        if (productionContentDTOList == null) {
            List<StructuredContent> productionContentList = structuredContentDao.findActiveStructuredContentByName(getProductionSandBox(sandBox), contentName, locale);
            productionContentDTOList = buildStructuredContentDTOList(productionContentList, secure);
            if (productionContentDTOList != null) {
                addStructuredContentListToCache(cacheKey, productionContentDTOList);
            }
        }

        final List<StructuredContentDTO> contentList;
        if (! isProductionSandBox(sandBox)) {
            sandBoxContentList = structuredContentDao.findActiveStructuredContentByName(sandBox, contentName, locale);
            contentList = mergeContent(productionContentDTOList, sandBoxContentList, secure);
        } else {
            contentList = productionContentDTOList;
        }

        return evaluateAndPriortizeContent(contentList, count, ruleDTOs);
    }

    private SandBox getProductionSandBox(SandBox currentSandBox) {
        SandBox productionSandBox = null;
        if (currentSandBox == null || SandBoxType.PRODUCTION.equals(currentSandBox.getSandBoxType())) {
            productionSandBox = currentSandBox;
        } else if (currentSandBox.getSite() != null) {
            productionSandBox = currentSandBox.getSite().getProductionSandbox();
        }
        return productionSandBox;
    }

    private boolean isProductionSandBox(SandBox dest) {
        if (dest == null) {
            return true;
        } else {
            return SandBoxType.PRODUCTION.equals(dest.getSandBoxType());
        }
    }

    protected void productionItemArchived(StructuredContent sc) {
        // Immediately remove the content from this VM.
        removeStructuredContentFromCache(sc);

        if (archivedStructuredContentListeners != null) {
            for (ArchivedStructuredContentPublisher listener : archivedStructuredContentListeners) {                
                listener.processStructuredContentArchive(sc, buildTypeKey(sc), buildNameKey(sc));
            }
        }
    }

    @Override
    public void itemPromoted(SandBoxItem sandBoxItem, SandBox destinationSandBox) {
        if (! SandBoxItemType.STRUCTURED_CONTENT.equals(sandBoxItem.getSandBoxItemType())) {
            return;
        }

        StructuredContent sc = structuredContentDao.findStructuredContentById(sandBoxItem.getTemporaryItemId());
        if (sc == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Structured Content Item not found " + sandBoxItem.getTemporaryItemId());
            }
        } else {
            boolean productionSandBox = isProductionSandBox(destinationSandBox);
            if (productionSandBox) {
                sc.setLockedFlag(false);
            } else {
                sc.setLockedFlag(true);
            }
            if (productionSandBox && sc.getOriginalItemId() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Structured content promoted to production.  " + sc.getId() + ".  Archiving original item " + sc.getOriginalItemId());
                }
                StructuredContent originalSC = structuredContentDao.findStructuredContentById(sc.getOriginalItemId());
                originalSC.setArchivedFlag(Boolean.TRUE);
                structuredContentDao.addOrUpdateContentItem(originalSC);
                productionItemArchived(originalSC);

                if (sc.getDeletedFlag()) {
                    // if this deleted content is being pushed to production, set it as archived.
                    sc.setArchivedFlag(true);
                }

                // We are archiving the old content and making this the new "production page content", so
                // null out the original page id before saving.
                sc.setOriginalItemId(null);
            }
        }
        if (sc.getOriginalSandBox() == null) {
            sc.setOriginalSandBox(sc.getSandbox());
        }
        sc.setSandbox(destinationSandBox);
        structuredContentDao.addOrUpdateContentItem(sc);
    }

    @Override
    public void itemRejected(SandBoxItem sandBoxItem, SandBox destinationSandBox) {
        if (! SandBoxItemType.STRUCTURED_CONTENT.equals(sandBoxItem.getSandBoxItemType())) {
            return;
        }
        StructuredContent sc = structuredContentDao.findStructuredContentById(sandBoxItem.getTemporaryItemId());

        if (sc != null) {
            sc.setSandbox(destinationSandBox);
            sc.setOriginalSandBox(null);
            sc.setLockedFlag(false);
            structuredContentDao.addOrUpdateContentItem(sc);
        }
    }

    @Override
    public void itemReverted(SandBoxItem sandBoxItem) {
        if (! SandBoxItemType.STRUCTURED_CONTENT.equals(sandBoxItem.getSandBoxItemType())) {
            return;
        }
        StructuredContent sc = structuredContentDao.findStructuredContentById(sandBoxItem.getTemporaryItemId());

        if (sc != null) {
            if (sandBoxItem.getOriginalItemId() != null) {
                sc.setArchivedFlag(Boolean.TRUE);
                sc.setLockedFlag(Boolean.FALSE);
                structuredContentDao.addOrUpdateContentItem(sc);

                StructuredContent originalSc = structuredContentDao.findStructuredContentById(sandBoxItem.getOriginalItemId());
                originalSc.setLockedFlag(false);
                structuredContentDao.addOrUpdateContentItem(originalSc);
            }
        }
    }

    public List<StructuredContentRuleProcessor> getContentRuleProcessors() {
        return contentRuleProcessors;
    }

    public void setContentRuleProcessors(List<StructuredContentRuleProcessor> contentRuleProcessors) {
        this.contentRuleProcessors = contentRuleProcessors;
    }

    private Cache getStructuredContentCache() {
        if (structuredContentCache == null) {
            structuredContentCache = CacheManager.getInstance().getCache("cmsStructuredContentCache");
        }
        return structuredContentCache;
    }
    
    private String buildNameKey(StructuredContent sc) {
        return buildNameKey(sc.getSandbox(), sc.getLocale(), sc.getStructuredContentType().getName(), sc.getContentName());    
    }

    private String buildTypeKey(StructuredContent sc) {
        return buildTypeKey(sc.getSandbox(), sc.getLocale(), sc.getStructuredContentType().getName());
    }


    private String buildNameKey(SandBox currentSandbox, Locale locale, String contentType, String contentName) {
        StringBuffer key = new StringBuffer(contentType).append("-").append(contentName);
        if (locale != null) {
            key.append("-").append(locale.getLocaleCode());
        }

        if (currentSandbox != null) {
            key.append("-").append(currentSandbox.getId());
        }

        return key.toString();
    }
    
    private String buildTypeKey(SandBox currentSandbox, Locale locale, String contentType) {
        StringBuffer key = new StringBuffer(contentType);
        if (locale != null) {
            key.append("-").append(locale.getLocaleCode());
        }

        if (currentSandbox != null) {
            key.append("-").append(currentSandbox.getId());
        }

        return key.toString();
    }


    private void addStructuredContentListToCache(String key, List<StructuredContentDTO> scDTOList) {
        getStructuredContentCache().put(new Element(key, scDTOList));
    }

    private List<StructuredContentDTO> getStructuredContentListFromCache(String key) {
        Element scElement =  getStructuredContentCache().get(key);
        if (scElement != null) {
            return (List<StructuredContentDTO>) scElement.getValue();
        }
        return null;
    }

    /**
     * Call to evict an item from the cache.
     * @param sc
     */
    public void removeStructuredContentFromCache(StructuredContent sc) {
        // Remove secure and non-secure instances of the page.
        // Typically the page will be in one or the other if at all.
        removeItemFromCache(buildNameKey(sc), buildTypeKey(sc));
    }

    /**
     * Call to evict both secure and non-secure SC items matching
     * the passed in key.
     *
     * @param nameKey
     */
    public void removeItemFromCache(String nameKey, String typeKey) {
        // Remove secure and non-secure instances of the structured content.
        // Typically the structured content will be in one or the other if at all.
        getStructuredContentCache().remove(nameKey+"-"+true);
        getStructuredContentCache().remove(nameKey+"-"+false);

        getStructuredContentCache().remove(typeKey+"-"+true);
        getStructuredContentCache().remove(typeKey+"-"+false);
    }

    public List<ArchivedStructuredContentPublisher> getArchivedStructuredContentListeners() {
        return archivedStructuredContentListeners;
    }

    public void setArchivedStructuredContentListeners(List<ArchivedStructuredContentPublisher> archivedStructuredContentListeners) {
        this.archivedStructuredContentListeners = archivedStructuredContentListeners;
    }

    public boolean isAutomaticallyApproveAndPromoteStructuredContent() {
        return automaticallyApproveAndPromoteStructuredContent;
    }

    public void setAutomaticallyApproveAndPromoteStructuredContent(boolean automaticallyApproveAndPromoteStructuredContent) {
        this.automaticallyApproveAndPromoteStructuredContent = automaticallyApproveAndPromoteStructuredContent;
    }
}
