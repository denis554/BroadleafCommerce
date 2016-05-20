/*
 * #%L
 * BroadleafCommerce Open Admin Platform
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
package org.broadleafcommerce.openadmin.web.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.exception.SiteNotFoundException;
import org.broadleafcommerce.common.web.BroadleafWebRequestProcessor;
import org.broadleafcommerce.openadmin.security.ClassNameRequestParamValidationService;
import org.broadleafcommerce.openadmin.server.service.persistence.Persistable;
import org.broadleafcommerce.openadmin.server.service.persistence.PersistenceThreadManager;
import org.broadleafcommerce.openadmin.server.service.persistence.TargetModeType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Responsible for setting the necessary attributes on the BroadleafRequestContext
 * 
 * @author Andre Azzolini (apazzolini)
 */
@Component("blAdminRequestFilter")
public class BroadleafAdminRequestFilter extends AbstractBroadleafAdminRequestFilter {

    private final Log LOG = LogFactory.getLog(BroadleafAdminRequestFilter.class);

    @Resource(name = "blAdminRequestProcessor")
    protected BroadleafWebRequestProcessor requestProcessor;

    @Resource(name="blPersistenceThreadManager")
    protected PersistenceThreadManager persistenceThreadManager;

    @Resource(name="blClassNameRequestParamValidationService")
    protected ClassNameRequestParamValidationService validationService;

    @Override
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws IOException, ServletException {

        if (!validateClassNameParams(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!shouldProcessURL(request, request.getRequestURI())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Process URL not processing URL " + request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            persistenceThreadManager.operation(TargetModeType.SANDBOX, new Persistable <Void, RuntimeException>() {
                @Override
                public Void execute() {
                    try {
                        requestProcessor.process(new ServletWebRequest(request, response));
                        filterChain.doFilter(request, response);
                        return null;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (SiteNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            requestProcessor.postProcess(new ServletWebRequest(request, response));
        }
    }

    protected boolean validateClassNameParams(HttpServletRequest request) {
        String ceilingEntityClassname = request.getParameter("ceilingEntityClassname");
        String ceilingEntity = request.getParameter("ceilingEntity");
        String ceilingEntityFullyQualifiedClassname = request.getParameter("fields['ceilingEntityFullyQualifiedClassname'].value");
        String originalType = request.getParameter("fields['__originalType'].value");
        String entityType = request.getParameter("entityType");
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("ceilingEntityClassname", ceilingEntityClassname);
        params.put("entityType", entityType);
        params.put("ceilingEntity", ceilingEntity);
        params.put("ceilingEntityFullyQualifiedClassname", ceilingEntityFullyQualifiedClassname);
        params.put("__originalType", originalType);
        return validationService.validateClassNameParams(params, "blPU");
    }
}
