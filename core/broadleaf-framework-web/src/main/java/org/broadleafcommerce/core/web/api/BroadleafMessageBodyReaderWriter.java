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
package org.broadleafcommerce.core.web.api;

import com.sun.jersey.core.impl.provider.entity.XMLListElementProvider;
import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;
import com.sun.jersey.json.impl.provider.entity.JSONListElementProvider;
import com.sun.jersey.json.impl.provider.entity.JSONRootElementProvider;
import com.sun.jersey.spi.inject.Injectable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This custom MessageBodyReaderWriter was written in order to correctly handle any custom extended entities that Broadleaf is aware of.
 * The intent is to replace any paramerterized types with the correct implentations that are defined in the Application Context.
 * Once the correct generic types are replaced, the class then delegates to the default XML or JSON List providers.
 * </p>
 *
 *
 * @author elbertbautista
 * @see com.sun.jersey.json.impl.provider.entity.JSONListElementProvider
 * @see com.sun.jersey.core.impl.provider.entity.XMLListElementProvider
 *
 */
@Component
@Provider
@Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
@Consumes(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class BroadleafMessageBodyReaderWriter implements MessageBodyReader<Object>, MessageBodyWriter<Object>, ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(BroadleafMessageBodyReaderWriter.class);

    protected ApplicationContext applicationContext;

    @Context
	protected Providers ps;

	@Context
	protected Injectable<XMLInputFactory> xif;

    @Context
    protected Injectable<SAXParserFactory> spf;

    protected static XMLListElementProvider.App xmlListProvider;
	protected static JSONListElementProvider.App jsonListProvider;
    protected static XMLRootElementProvider.App xmlRootElementProvider;
    protected static JSONRootElementProvider.App jsonRootElementProvider;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Type lookupType = getLookupType(type, genericType);

        if (getApiWrapper(type, lookupType) != null) {
            return true;
        }

        return false;
    }

    @Override
	public Object readFrom(Class<Object> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

        initializeProviders();

        Type lookupType =  getLookupType(type, genericType);

        if (getApiWrapper(type, lookupType) != null) {
            genericType = getApiWrapper(type, lookupType);
        }

        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            if (Collection.class.isAssignableFrom(type)){
                return jsonListProvider.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
            } else {
                // Since we've replaced  the genericType param with the correct implementation, we have to pass that as the first argument as well because
                // the default root element providers don't actually use genericType in their implementations.
                return jsonRootElementProvider.readFrom((Class)genericType, genericType, annotations, mediaType, httpHeaders, entityStream);
            }
        } else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) || mediaType.isCompatible(MediaType.TEXT_XML_TYPE)) {
            if (Collection.class.isAssignableFrom(type)){
                return xmlListProvider.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
            } else {
                // Since we've replaced  the genericType param with the correct implementation, we have to pass that as the first argument as well because
                // the default root element providers don't actually use genericType in their implementations.
                return xmlRootElementProvider.readFrom((Class)genericType, genericType, annotations, mediaType, httpHeaders, entityStream);
            }
        }

	    return null;
	}

    @Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

    @Override
    public void writeTo(
            Object t,
            Class<?> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        initializeProviders();

        Type lookupType = getLookupType(type, genericType);

        if (getApiWrapper(type, lookupType) != null) {
            genericType = getApiWrapper(type, lookupType);
        }

        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            if (Collection.class.isAssignableFrom(type)) {
    		    jsonListProvider.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
            } else {
                jsonRootElementProvider.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
            }
    	} else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) || mediaType.isCompatible(MediaType.TEXT_XML_TYPE)) {
            if (Collection.class.isAssignableFrom(type)) {
	    	    xmlListProvider.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
            } else {
                xmlRootElementProvider.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
            }
    	}
    }

    private void initializeProviders() {
        if (jsonListProvider == null) {
	    	jsonListProvider = new JSONListElementProvider.App(ps);
	    }

	    if (xmlListProvider == null) {
	    	xmlListProvider = new XMLListElementProvider.App(xif, ps);
	    }

        if (xmlRootElementProvider == null) {
            xmlRootElementProvider = new XMLRootElementProvider.App(spf, ps);
        }

        if (jsonRootElementProvider == null) {
            jsonRootElementProvider = new JSONRootElementProvider.App(ps);
        }
    }

    protected Type getLookupType(Class<?> type, Type genericType) {
        Type lookupType = genericType;
        if (Collection.class.isAssignableFrom(type)) {
            final ParameterizedType pt = (ParameterizedType) genericType;
            lookupType = pt.getActualTypeArguments()[0];
        }  else if (type.isArray()) {
            lookupType = type.getComponentType();
        }

        return lookupType;
    }

    /**
        *
        * Via the ApplicationContext we can look up exactly what implementation corresponds to the
        * parameterized domain interface and determine if that is annotated with proper JAXB annotations for serialization.
        * We then return the correct implementing Class in case of a root element or a new ParameterizedType in case of a Collection
        * The default providers can then handle the actual serialization of the List or Root element safely.
        *
        */
    protected Type getApiWrapper(Class<?> type, Type lookupType){
        Map<String, Object> apiWrappers = applicationContext.getBeansWithAnnotation(XmlRootElement.class);
        Set<String> keySet = apiWrappers.keySet();

        for (String key : keySet) {
            if (key.equals(((Class)lookupType).getName())) {
                if (Collection.class.isAssignableFrom(type)) {
                    Type[] paramType = {apiWrappers.get(key).getClass()};
                    return ParameterizedTypeImpl.make(type, paramType, null);
                } else {
                    return apiWrappers.get(key).getClass();
                }
            }
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
