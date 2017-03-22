/*
 * #%L
 * BroadleafCommerce Common Libraries
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
package org.broadleafcommerce.common.web.filter;

import org.broadleafcommerce.common.util.BLCRequestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sets a request attribute that informs all Broadleaf Filters that follow NOT to use the HTTP Session.
 * 
 * Intended for use by REST api requests.
 * 
 * @author bpolster
 */
@Component("blStatelessSessionFilter")
@ConditionalOnProperty(value = "use.stateless.request", matchIfMissing = true)
public class StatelessSessionFilter extends GenericFilterBean implements Ordered {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        BLCRequestUtils.setOKtoUseSession(new ServletWebRequest((HttpServletRequest) request, (HttpServletResponse) response), Boolean.FALSE);
        SessionlessHttpServletRequestWrapper wrapper = new SessionlessHttpServletRequestWrapper((HttpServletRequest) request);
        filterChain.doFilter(wrapper, response);
    }

    @Override
    public int getOrder() {
        return FilterOrdered.PRE_SECURITY_HIGH;
    }
}
