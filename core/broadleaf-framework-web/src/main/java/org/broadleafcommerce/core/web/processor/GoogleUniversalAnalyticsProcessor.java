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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.order.domain.SkuAccessor;
import org.broadleafcommerce.core.order.service.OrderService;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.element.AbstractElementProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

/**
 * <p>
 * Takes advantage of the new-stype analytics.js from Google Analytics rather than the deprected ga.js. This also
 * supports a pre-processed <b>orderNumber</b> attribute that can be null, suitable for things like the order confirmation
 * page to send e-commerce transactions. Example usage:
 * 
 * <pre>
 * &lt;google_universal_analytics ordernumber="${order?.orderNumber" /&gt;
 * </pre>
 * 
 * <p>
 * This processor also supports:
 * <ul>
 *  <li>Multiple trackers (extensible via {@link #getTrackers()} or by setting the {@code googleAnalytics.masterWebPropertyId}
 *      and {@code googleAnalytics.webPropertyId})</li>
 *  <li>Affiliates for e-commerce ({@ googleAnalytics.affiliation property})</li>
 *  <li><a href="https://support.google.com/analytics/answer/2558867?hl=en&utm_id=ad">Link attribution</a>
 *      ({@code googleAnalytics.enableLinkAttribution} system property, default {@code true})</li>
 *  <li><a href="https://support.google.com/analytics/answer/3450482">Display Advertising</a>
 *      ({@code googleAnalytics.enableDisplayAdvertising} system property, default {@code false})</li>
 * </ul>
 * 
 * @param ordernumber the order number to look up for ecommerce tracking, such as on the confirmation page
 * 
 * @author Phillip Verheyden (phillipuniverse)
 */
public class GoogleUniversalAnalyticsProcessor extends AbstractElementProcessor {

    /**
     * Global value
     */
    @Value("${googleAnalytics.masterWebPropertyId:}")
    protected String masterWebPropertyId;
    
    /**
     * Site-specific value
     */
    @Value("${googleAnalytics.webPropertyId:}")
    protected String webPropertyId;
    
    @Value("${googleAnalytics.affiliation:}")
    protected String affiliation;
    
    @Value("${googleAnalytics.enableLinkAttribution:true}")
    protected boolean includeLinkAttribution;
    
    @Value("${googleAnalytics.enableDisplayAdvertising:false}")
    protected boolean includeDisplayAdvertising;
    
    @Resource(name = "blOrderService")
    protected OrderService orderService;
    
    /**
     * This will force the domain to 127.0.0.1 which is useful to determine if the Google Analytics tag is sending
     * a request to Google
     */
    protected boolean testLocal = false;
    
    public GoogleUniversalAnalyticsProcessor() {
        super("google_universal_analytics");
    }
    
    public GoogleUniversalAnalyticsProcessor(String elementName) {
        super(elementName);
    }
    
    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    protected ProcessorResult processElement(Arguments arguments, Element element) {
        StringBuffer sb = new StringBuffer();
        Map<String, String> trackers = getTrackers();
        if (MapUtils.isNotEmpty(trackers)) {
            sb.append("<script>\n");
            sb.append("(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){");
            sb.append("(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),");
            sb.append("m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)");
            sb.append("})(window,document,'script','//www.google-analytics.com/analytics.js','ga');");
            
            String orderNumberExpression = element.getAttributeValue("ordernumber");
            String orderNumber = (String) StandardExpressionProcessor.processExpression(arguments, orderNumberExpression);
            Order order = null;
            if (orderNumber != null) {
                order = orderService.findOrderByOrderNumber(orderNumber);
            }
            
            for (Entry<String, String> tracker : trackers.entrySet()) {
                String trackerName = tracker.getKey();
                String id = tracker.getValue();
                sb.append("ga('create', '" + id + "', 'auto', {'name': '" + trackerName + "'");
                if (testLocal) {
                    sb.append(",'cookieDomain': 'none'");
                }
                sb.append("});");
                sb.append("ga('" + trackerName + ".send', 'pageview');");
                
                if (isIncludeLinkAttribution()) {
                    sb.append(getLinkAttributionJs(trackerName));
                }
                if (isIncludeDisplayAdvertising()) {
                    sb.append(getDisplayAdvertisingJs(trackerName));
                }
                
                if (order != null) {
                    sb.append(getTransactionJs(order, trackerName));
                }
            }
            
            sb.append("</script>");
            
            // Add contentNode to the document
            Node contentNode = new Macro(sb.toString());
            element.clearChildren();
            element.getParent().insertAfter(element, contentNode);
            element.getParent().removeChild(element);
        } else {
            Log.warn("No trackers were found, not outputting Google Analytics script. Set the googleAnalytics.webPropertyId"
                    + " and/or the googleAnalytics.masterWebPropertyId system properties to output Google Analytics");
        }

        // Return OK
        return ProcessorResult.OK;
    }
    
    /**
     * Grabs a map of trackers keyed by the tracker name with the analytics ID as the value
     */
    protected Map<String, String> getTrackers() {
        Map<String, String> trackers = new HashMap<String, String>();
        if (StringUtils.isNotBlank(getMasterWebPropertyId())) {
            trackers.put("master", getMasterWebPropertyId());
        }
        if (StringUtils.isNotBlank(getWebPropertyId())) {
            trackers.put("webProperty", getWebPropertyId());
        }
        
        return trackers;
    }
    
    /**
     * Builds the linke attribution Javascript
     * @param tracker the name of the tracker that is using the link attribution
     * @return
     */
    protected String getLinkAttributionJs(String tracker) {
        return "ga('" + tracker + ".require', 'linkid', 'linkid.js');";
    }
    
    /**
     * Builds the display advertising Javascript for the given tracker
     * @param tracker
     * @return
     */
    protected String getDisplayAdvertisingJs(String tracker) {
        return "ga('" + tracker + ".require', 'displayfeatures');";
    }
    
    /**
     * Builds the transaction analytics for the given tracker name. Invokes {@link #getItemJs(Order, String) for each item
     * in the given <b>order</b>.
     */
    protected String getTransactionJs(Order order, String tracker) {
        StringBuffer sb = new StringBuffer();
        sb.append("ga('" + tracker + ".require', 'ecommerce', 'ecommerce.js');");
        
        sb.append("ga('" + tracker + ".ecommerce:addTransaction', {");
        sb.append("'id': '" + order.getOrderNumber() + "'");
        if (StringUtils.isNotBlank(affiliation)) {
            sb.append(",'affiliation': '" + affiliation + "'");
        }
        sb.append(",'revenue': '" + order.getTotal() + "'");
        sb.append(",'shipping':'" + order.getTotalShipping() + "'");
        sb.append(",'tax': '" + order.getTotalTax() + "'");

        if (order.getCurrency() != null) {
            sb.append(",'currency': '" + order.getCurrency().getCurrencyCode() + "'");
        }
        sb.append("});");
        
        getItemJs(order, tracker);
        
        sb.append("ga('" + tracker + ".ecommerce:send');");
        return sb.toString();
    }
    
    protected String getItemJs(Order order, String tracker) {
        StringBuffer sb = new StringBuffer();
        for (FulfillmentGroup fulfillmentGroup : order.getFulfillmentGroups()) {
            for (FulfillmentGroupItem fulfillmentGroupItem : fulfillmentGroup.getFulfillmentGroupItems()) {
                OrderItem orderItem = fulfillmentGroupItem.getOrderItem();
    
                Sku sku = ((SkuAccessor) orderItem).getSku();
                
                sb.append("ga('" + tracker + ".ecommerce:addItem', {");
                sb.append("'id': '" + order.getOrderNumber() + "'");
                sb.append(",'name': '" + sku.getName() + "'");
                sb.append(",'sku': '" + sku.getId() + "'");
                sb.append(",'category': '" + getVariation(orderItem) + "'");
                sb.append(",'price': '" + orderItem.getAveragePrice() + "'");
                sb.append(",'quantity': '" + orderItem.getQuantity() + "'");
                sb.append("});");
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns the product option values separated by a space if they are
     * relevant for the item, or the product category if no options are available
     * 
     * @return
     */
    protected String getVariation(OrderItem item) {
        if (MapUtils.isEmpty(item.getOrderItemAttributes())) {
            return item.getCategory() == null ? "" : item.getCategory().getName();
        }
        
        //use product options instead
        String result = "";
        for (Map.Entry<String, OrderItemAttribute> entry : item.getOrderItemAttributes().entrySet()) {
            result += entry.getValue().getValue() + " ";
        }

        //the result has a space at the end, ensure that is stripped out
        return result.substring(0, result.length() - 1);
    }

    
    public OrderService getOrderService() {
        return orderService;
    }

    
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    
    public String getMasterWebPropertyId() {
        return masterWebPropertyId;
    }

    
    public void setMasterWebPropertyId(String masterWebPropertyId) {
        this.masterWebPropertyId = masterWebPropertyId;
    }

    
    public String getAffiliation() {
        return affiliation;
    }

    
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    
    public String getWebPropertyId() {
        return webPropertyId;
    }

    
    public void setWebPropertyId(String webPropertyId) {
        this.webPropertyId = webPropertyId;
    }

    
    public boolean isIncludeLinkAttribution() {
        return includeLinkAttribution;
    }

    
    public void setIncludeLinkAttribution(boolean includeLinkAttribution) {
        this.includeLinkAttribution = includeLinkAttribution;
    }

    
    public boolean isIncludeDisplayAdvertising() {
        return includeDisplayAdvertising;
    }

    
    public void setIncludeDisplayAdvertising(boolean includeDisplayAdvertising) {
        this.includeDisplayAdvertising = includeDisplayAdvertising;
    }
        
}
