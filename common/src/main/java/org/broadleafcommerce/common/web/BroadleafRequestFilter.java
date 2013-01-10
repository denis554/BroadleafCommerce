/*
 * Copyright 2008-2012 the original author or authors.
 *
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
 */

package org.broadleafcommerce.common.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.RequestDTOImpl;
import org.broadleafcommerce.common.currency.domain.BroadleafCurrency;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.sandbox.domain.SandBox;
import org.broadleafcommerce.common.site.domain.Site;
import org.broadleafcommerce.common.site.domain.Theme;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for setting up the site and locale used by Broadleaf Commerce components.
 *
 * @author bpolster
 */
@Component("blRequestFilter")
public class BroadleafRequestFilter extends OncePerRequestFilter {
    private final Log LOG = LogFactory.getLog(BroadleafRequestFilter.class);

    @Resource(name = "blSiteResolver")
    private BroadleafSiteResolver siteResolver;
    
    @Resource(name = "blLocaleResolver")
    private BroadleafLocaleResolver localeResolver;

    @Resource(name = "blCurrencyResolver")
    private BroadleafCurrencyResolver currencyResolver;

    @Resource(name = "blSandBoxResolver")
    private BroadleafSandBoxResolver sandboxResolver;

    @Resource(name = "blThemeResolver")
    private BroadleafThemeResolver themeResolver;


    /**
     * Parameter/Attribute name for the current language
     */
    public static String REQUEST_DTO = "blRequestDTO";


    // Properties to manage URLs that will not be processed by this filter.
    private static final String BLC_ADMIN_GWT = "org.broadleafcommerce.admin";
    private static final String BLC_ADMIN_PREFIX = "blcadmin";
    private static final String BLC_ADMIN_SERVICE = ".service";
    

    private Set<String> ignoreSuffixes;

    /**
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (!shouldProcessURL(request, request.getRequestURI())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Process URL not processing URL " + request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        String requestURIWithoutContext;

        if (request.getContextPath() != null) {
            requestURIWithoutContext = request.getRequestURI().substring(request.getContextPath().length());
        } else {
            requestURIWithoutContext = request.getRequestURI();
        }
        
        // Remove JSESSION-ID or other modifiers
        int pos = requestURIWithoutContext.indexOf(";");
        if (pos >= 0) {
            requestURIWithoutContext = requestURIWithoutContext.substring(0,pos);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Process URL Filter Begin " + requestURIWithoutContext);
        }

        if (request.getAttribute(REQUEST_DTO) == null) {
            request.setAttribute(REQUEST_DTO, new RequestDTOImpl(request));
        }

        Site site = siteResolver.resolveSite(request);
        Locale locale = localeResolver.resolveLocale(request);
        BroadleafCurrency currency = currencyResolver.resolveCurrency(request);
        Theme theme = themeResolver.resolveTheme(request, site);
    
    SandBox currentSandbox = sandboxResolver.resolveSandBox(request, site);
    if (currentSandbox != null) {
            SandBoxContext previewSandBoxContext = new SandBoxContext();
            previewSandBoxContext.setSandBoxId(currentSandbox.getId());
            previewSandBoxContext.setPreviewMode(true);
            SandBoxContext.setSandBoxContext(previewSandBoxContext);
        }
        BroadleafRequestContext brc = new BroadleafRequestContext();        
        brc.setSite(site);
        brc.setLocale(locale);
        brc.setBroadleafCurrency(currency);
        brc.setRequest(request);
        brc.setSandbox(currentSandbox);
        brc.setResponse(response);
        brc.setTheme(theme);
        BroadleafRequestContext.setBroadleafRequestContext(brc);
        
        Map<String, Object> ruleMap = (Map<String, Object>) request.getAttribute("blRuleMap");
        if (ruleMap == null) {
            LOG.trace("Creating ruleMap and adding in Locale.");
            ruleMap = new HashMap<String, Object>();
            request.setAttribute("blRuleMap", ruleMap);
        } else {
            LOG.trace("Using pre-existing ruleMap - added by non standard BLC process.");
        }
        ruleMap.put("locale", locale);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SandBoxContext.setSandBoxContext(null);
        }
    }
   

    /**
     * Determines if the passed in URL should be processed by the content management system.
     * <p/>
     * By default, this method returns false for any BLC-Admin URLs and service calls and for all
     * common image/digital mime-types (as determined by an internal call to {@code getIgnoreSuffixes}.
     * <p/>
     * This check is called with the {@code doFilterInternal} method to short-circuit the content
     * processing which can be expensive for requests that do not require it.
     *
     * @param requestURI - the HttpServletRequest.getRequestURI
     * @return true if the {@code HttpServletRequest} should be processed
     */
    protected boolean shouldProcessURL(HttpServletRequest request, String requestURI) {
        if (requestURI.contains(BLC_ADMIN_GWT) || 
            requestURI.endsWith(BLC_ADMIN_SERVICE) ||
            requestURI.contains(BLC_ADMIN_PREFIX)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("BroadleafProcessURLFilter ignoring admin request URI " + requestURI);
            }
            return false;
        } else {
            int pos = requestURI.lastIndexOf(".");
            if (pos > 0) {
                String suffix = requestURI.substring(pos);
                if (getIgnoreSuffixes().contains(suffix.toLowerCase())) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("BroadleafProcessURLFilter ignoring request due to suffix " + requestURI);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a set of suffixes that can be ignored by content processing.   The following
     * are returned:
     * <p/>
     * <B>List of suffixes ignored:</B>
     *
     * ".aif", ".aiff", ".asf", ".avi", ".bin", ".bmp", ".doc", ".eps", ".gif", ".hqx", ".jpg", ".jpeg", ".mid", ".midi", ".mov", ".mp3", ".mpg", ".mpeg", ".p65", ".pdf", ".pic", ".pict", ".png", ".ppt", ".psd", ".qxd", ".ram", ".ra", ".rm", ".sea", ".sit", ".stk", ".swf", ".tif", ".tiff", ".txt", ".rtf", ".vob", ".wav", ".wmf", ".xls", ".zip";
     *
     * @return set of suffixes to ignore.
     */
    protected Set getIgnoreSuffixes() {
        if (ignoreSuffixes == null || ignoreSuffixes.isEmpty()) {
            String[] ignoreSuffixList = {".aif", ".aiff", ".asf", ".avi", ".bin", ".bmp", ".css", ".doc", ".eps", ".gif", ".hqx", ".js", ".jpg", ".jpeg", ".mid", ".midi", ".mov", ".mp3", ".mpg", ".mpeg", ".p65", ".pdf", ".pic", ".pict", ".png", ".ppt", ".psd", ".qxd", ".ram", ".ra", ".rm", ".sea", ".sit", ".stk", ".swf", ".tif", ".tiff", ".txt", ".rtf", ".vob", ".wav", ".wmf", ".xls", ".zip"};
            ignoreSuffixes = new HashSet<String>(Arrays.asList(ignoreSuffixList));
        }
        return ignoreSuffixes;
    }
}
