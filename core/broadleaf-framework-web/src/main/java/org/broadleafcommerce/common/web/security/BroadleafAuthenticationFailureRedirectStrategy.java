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
package org.broadleafcommerce.common.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.util.UrlUtil;
import org.broadleafcommerce.common.web.controller.BroadleafControllerUtility;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extends the Spring DefaultRedirectStrategy with support for ajax redirects.
 * 
 * Designed for use with SpringSecurity when errors are present.
 * 
 * Tacks on the BLC_AJAX_PARAMETER=true to the redirect request if the request is an ajax request.   This will cause the
 * resulting controller (e.g. LoginController) to treat the request as if it is coming from Ajax and 
 * return the related page fragment rather than returning the full view of the page.
 * 
 * @author bpolster
 *
 */
@Component("blAuthenticationFailureRedirectStrategy")
public class BroadleafAuthenticationFailureRedirectStrategy implements RedirectStrategy {

    private static final Log LOG = LogFactory.getLog(BroadleafAuthenticationFailureRedirectStrategy.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        if (BroadleafControllerUtility.isAjaxRequest(request)) {
             url = updateUrlForAjax(url);
        }

        try {
            UrlUtil.validateUrl(url, request);
        }  catch (IOException e) {
            LOG.error("SECURITY FAILURE Bad redirect location: " + url, e);
            response.sendError(403);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, url);
    }

    public String updateUrlForAjax(String url) {
        String blcAjax = BroadleafControllerUtility.BLC_AJAX_PARAMETER;
        if (url != null && url.indexOf("?") > 0) {
            url = url + "&" + blcAjax + "=true";
        } else {
            url = url + "?" + blcAjax + "=true";
        }
        return url;
    }
    
    public RedirectStrategy getRedirectStrategy() {
        return redirectStrategy;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }
}
