/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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

package org.broadleafcommerce.core.web.processor;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.fragment.WholeFragmentSpec;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.element.AbstractElementProcessor;
import org.thymeleaf.standard.expression.Expression;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.fragment.StandardFragment;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.AbstractStandardFragmentHandlingAttrProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;

import java.util.List;

/**
 * 
 * @author Andre Azzolini (apazzolini)
 */
public class CachedSubstituteByProcessor extends AbstractElementProcessor {

    public static final int ATTR_PRECEDENCE = 100;
    public static final String ATTR_NAME = "substituteby";
    public static final String FRAGMENT_ATTR_NAME = StandardFragmentAttrProcessor.ATTR_NAME;

    protected Cache cache;
    
    public CachedSubstituteByProcessor() {
        super(ATTR_NAME);
    }

    public String buildCacheKey(String template, String cacheKey, String customerCacheMethod) {
        StringBuilder sb = new StringBuilder();        
        sb.append(template);
        if (cacheKey != null) {
            sb.append('-').append(cacheKey);
        }
        BroadleafRequestContext brc = BroadleafRequestContext.getBroadleafRequestContext();
        if (brc != null && brc.getLocale() != null) {
            sb.append(brc.getLocale().getLocaleCode());

            if (! StringUtils.isEmpty(customerCacheMethod)) {
                Customer c = CustomerState.getCustomer(brc.getWebRequest());
                if ("anonymousOnly".equals(customerCacheMethod)) {
                    if (c != null && !c.isAnonymous()) {
                        return null;
                    }
                } else if ("byId".equals(customerCacheMethod)) {
                    if (c != null && c.getId() != null) {
                        sb.append('-').append(c.getId());
                    }
                } else if ("common".equals(customerCacheMethod)) {
                    if (c != null && c.getId() != null && !c.isAnonymous()) {
                        sb.append('-').append(c.getId());
                    }
                }
            }
        }

        return sb.toString();
    }

    @Override
    public final ProcessorResult processElement(final Arguments arguments, final Element element) {
        String template = element.getAttributeValue("template");
        String cacheKeyAttrValue = element.getAttributeValueFromNormalizedName("cachekey");
        String customerCacheMethodAttrValue = element.getAttributeValueFromNormalizedName("customercachemethod");

        String cacheKey = "";
        if (cacheKeyAttrValue != null) {
            Expression expression = (Expression) StandardExpressions.getExpressionParser(arguments.getConfiguration())
                .parseExpression(arguments.getConfiguration(), arguments, cacheKeyAttrValue);
            cacheKey = (String) expression.execute(arguments.getConfiguration(), arguments);
        }

        String customerCacheMethod = "";
        if (customerCacheMethodAttrValue != null) {
            Expression expression = (Expression) StandardExpressions.getExpressionParser(arguments.getConfiguration())
                    .parseExpression(arguments.getConfiguration(), arguments, customerCacheMethodAttrValue);
            customerCacheMethod = (String) expression.execute(arguments.getConfiguration(), arguments);
        }

        String blcCacheKey = buildCacheKey(template, cacheKey, customerCacheMethod);

        if (blcCacheKey != null) {
            element.setAttribute("blcCacheKey", blcCacheKey);
        }

        net.sf.ehcache.Element cacheElement = getCache().get(blcCacheKey);

        if (cacheElement != null && cacheElement.getObjectValue() != null) {
            // This template has been cached.
            element.clearChildren();
        } else {
            final List<Node> fragmentNodes = computeFragment(arguments, element, "template", template);
            element.clearChildren();
            element.setChildren(fragmentNodes);
        }
        return ProcessorResult.OK;
    }


    /**
     * <b>NOTE</b> This method is copied from {@link AbstractStandardFragmentHandlingAttrProcessor#computeFragment}
     * 
     * @param arguments
     * @param element
     * @param attributeName
     * @param attributeValue
     * @return
     */
    protected final List<Node> computeFragment(final Arguments arguments, final Element element,
            final String attributeName, final String attributeValue) {
        final String dialectPrefix = Attribute.getPrefixFromAttributeName(attributeName);

        final String fragmentSignatureAttributeName =
                getFragmentSignatureUnprefixedAttributeName(arguments, element, attributeName, attributeValue);

        final StandardFragment fragment =
                StandardFragmentProcessor.computeStandardFragmentSpec(
                        arguments.getConfiguration(), arguments, attributeValue, dialectPrefix, fragmentSignatureAttributeName);

        final List<Node> extractedNodes =
                fragment.extractFragment(arguments.getConfiguration(), arguments, arguments.getTemplateRepository());

        final boolean removeHostNode = getRemoveHostNode(arguments, element);

        // If fragment is a whole document (no selection inside), we should never remove its parent node/s
        // Besides, we know that StandardFragmentProcessor.computeStandardFragmentSpec only creates two types of
        // IFragmentSpec objects: WholeFragmentSpec and DOMSelectorFragmentSpec.
        final boolean isWholeDocument = (fragment.getFragmentSpec() instanceof WholeFragmentSpec);

        if (extractedNodes == null || removeHostNode || isWholeDocument) {
            return extractedNodes;
        }

        // Host node is NOT to be removed, therefore what should be removed is the top-level elements of the returned
        // nodes.

        final Element containerElement = new Element("container");

        for (final Node extractedNode : extractedNodes) {
            // This is done in this indirect way in order to preserver internal structures like e.g. local variables.
            containerElement.addChild(extractedNode);
            containerElement.extractChild(extractedNode);
        }

        final List<Node> extractedChildren = containerElement.getChildren();
        containerElement.clearChildren();

        return extractedChildren;
    }

    protected String getFragmentSignatureUnprefixedAttributeName(final Arguments arguments, final Element element,
            final String attributeName, final String attributeValue) {
        return FRAGMENT_ATTR_NAME;
    }

    @Override
    public int getPrecedence() {
        return ATTR_PRECEDENCE;
    }

    protected boolean getRemoveHostNode(Arguments arguments, Element element) {
        return false;
    }
    
    public Cache getCache() {
        if (cache == null) {
            cache = CacheManager.getInstance().getCache("blTemplateElements");
        }
        return cache;
    }    

}
