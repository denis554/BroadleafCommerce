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
package org.broadleafcommerce.core.web.api.jaxrs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.core.web.api.BroadleafWebServicesException;
import org.broadleafcommerce.core.web.api.wrapper.ErrorMessageWrapper;
import org.broadleafcommerce.core.web.api.wrapper.ErrorWrapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAXRS-compatible exception mapper
 *
 * @author Phillip Verheyden (phillipuniverse)
 * @deprecated along with the other JAXRS components, this is deprecated in favor of using Spring MVC for REST services
 */
@Provider
@Component("blJaxrsRestExceptionMapper")
@Conditional(IsJaxrsAvailableCondition.class)
@Deprecated
public class JaxrsRestExceptionMapper implements ExceptionMapper<Throwable>, MessageSourceAware, ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(JaxrsRestExceptionMapper.class);

    protected String messageKeyPrefix = BroadleafWebServicesException.class.getName() + '.';

    @Context
    protected HttpHeaders headers;

    protected MessageSource messageSource;

    protected ApplicationContext context;

    @Override
    public Response toResponse(Throwable t) {
        MediaType mediaType = resolveResponseMediaType(t);
        ErrorWrapper errorWrapper = (ErrorWrapper) context.getBean(ErrorWrapper.class.getName());
        Locale locale = null;
        BroadleafRequestContext requestContext = BroadleafRequestContext.getBroadleafRequestContext();
        if (requestContext != null) {
            locale = requestContext.getJavaLocale();
        }

        if (t instanceof BroadleafWebServicesException) {
            //If this is a BroadleafWebServicesException, then we will build the components of the response from that.
            BroadleafWebServicesException blcException = (BroadleafWebServicesException) t;
            if (t.getCause() != null) {
                LOG.error("An error occured invoking a REST service.", t.getCause());
            }
            errorWrapper.setHttpStatusCode(blcException.getHttpStatusCode());
            if (blcException.getLocale() != null) {
                locale = blcException.getLocale();
            }
            if (locale == null) {
                locale = Locale.getDefault();
            }

            if (blcException.getMessages() != null && !blcException.getMessages().isEmpty()) {
                Set<String> keys = blcException.getMessages().keySet();
                for (String key : keys) {
                    ErrorMessageWrapper errorMessageWrapper = (ErrorMessageWrapper) context.getBean(ErrorMessageWrapper.class.getName());
                    errorMessageWrapper.setMessageKey(resolveClientMessageKey(key));
                    errorMessageWrapper.setMessage(messageSource.getMessage(key, blcException.getMessages().get(key), key, locale));
                    errorWrapper.getMessages().add(errorMessageWrapper);
                }
            } else {
                ErrorMessageWrapper errorMessageWrapper = (ErrorMessageWrapper) context.getBean(ErrorMessageWrapper.class.getName());
                errorMessageWrapper.setMessageKey(resolveClientMessageKey(BroadleafWebServicesException.UNKNOWN_ERROR));
                errorMessageWrapper.setMessage(messageSource.getMessage(BroadleafWebServicesException.UNKNOWN_ERROR, null,
                        BroadleafWebServicesException.UNKNOWN_ERROR, locale));
                errorWrapper.getMessages().add(errorMessageWrapper);
            }

        } else if (t instanceof WebApplicationException) {
            //We will trust that if someone through a WebApplicationException, then they already created the 
            //response properly.
            if (t.getCause() != null) {
                LOG.error("An error occured invoking a REST service.", t.getCause());
            }
            WebApplicationException webAppException = (WebApplicationException) t;
            return webAppException.getResponse();
        } else {
            LOG.error("An error occured invoking a REST service", t);
            if (locale == null) {
                locale = Locale.getDefault();
            }
            errorWrapper.setHttpStatusCode(500);
            ErrorMessageWrapper errorMessageWrapper = (ErrorMessageWrapper) context.getBean(ErrorMessageWrapper.class.getName());
            errorMessageWrapper.setMessageKey(resolveClientMessageKey(BroadleafWebServicesException.UNKNOWN_ERROR));
            errorMessageWrapper.setMessage(messageSource.getMessage(BroadleafWebServicesException.UNKNOWN_ERROR, null,
                    BroadleafWebServicesException.UNKNOWN_ERROR, locale));
            errorWrapper.getMessages().add(errorMessageWrapper);
        }

        return Response.status(resolveResponseStatusCode(t, errorWrapper)).type(mediaType).entity(errorWrapper).build();
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * This key is the prefix that will be stripped off of all message keys that are returned to a client.
     * The default is "org.broadleafcommerce.core.web.api.BroadleafWebServicesException.". So, if a message key contained 
     * in a BroadleafWebServicesException is org.broadleafcommerce.core.web.api.BroadleafWebServicesException.unknownError, 
     * just "unknownError" will be returned to the client. This behavior can be changed by overriding the 
     * <code>resolveClientMessageKey</code> method. 
     * @param prefix
     */
    public void setMessageKeyPrefix(String prefix) {
        this.messageKeyPrefix = prefix;
    }

    /*
     * This allows you to return a different HTTP response code in the HTTP response than what is in the response wrapper.
     * For example, some clients may wish to always return a 200 (SUCCESS), even when there is an error.
     * The default behavior is to return the same status code associated with the error wrapper, or 500 if it is not known.
     */
    protected int resolveResponseStatusCode(Throwable t, ErrorWrapper error) {
        if (error.getHttpStatusCode() == null) {
            return 500;
        }
        return error.getHttpStatusCode();
    }

    protected MediaType resolveResponseMediaType(Throwable t) {
        if (headers.getAcceptableMediaTypes() != null && !headers.getAcceptableMediaTypes().isEmpty()) {
            List<MediaType> types = headers.getAcceptableMediaTypes();
            for (MediaType type : types) {
                if (MediaType.APPLICATION_XML_TYPE.equals(type) || MediaType.APPLICATION_JSON_TYPE.equals(type)) {
                    return type;
                }
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(headers.getMediaType())) {
            return MediaType.APPLICATION_XML_TYPE;
        }

        return MediaType.APPLICATION_JSON_TYPE;
    }

    protected String resolveClientMessageKey(String key) {
        if (messageKeyPrefix != null) {
            return StringUtils.remove(key, messageKeyPrefix);
        }
        return key;
    }

}
