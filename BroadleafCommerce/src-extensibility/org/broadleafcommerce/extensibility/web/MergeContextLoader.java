package org.broadleafcommerce.extensibility.web;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by {@link MergeContextLoaderListener}.
 *
 * <p>Processes a {@link #CONFIG_LOCATION_PARAM "contextConfigLocation"}
 * context-param and passes its value to the context instance, parsing it into
 * potentially multiple file paths which can be separated by any number of
 * commas and spaces, e.g. "WEB-INF/applicationContext1.xml,
 * WEB-INF/applicationContext2.xml". Ant-style path patterns are supported as well,
 * e.g. "WEB-INF/*Context.xml,WEB-INF/spring*.xml" or "WEB-INF/&#42;&#42;/*Context.xml".
 * If not explicitly specified, the context implementation is supposed to use a
 * default location (with XmlWebApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in previously loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Above and beyond loading the root application context, this class
 * can optionally load or obtain and hook up a shared parent context to
 * the root application context. See the
 * {@link #loadParentContext(ServletContext)} method for more information.
 *
 * <p>Additionally, Processes a {@link #PATCH_LOCATION_PARAM "patchConfigLocation"}
 * context-param and passes its value to the context instance, parsing it into
 * potentially multiple file paths which can be separated by any number of
 * commas and spaces, e.g. "WEB-INF/patch1.xml,
 * WEB-INF/patch2.xml". Ant-style path patterns are supported as well,
 * e.g. "WEB-INF/*Patch.xml,WEB-INF/spring*.xml" or "WEB-INF/&#42;&#42;/*Patch.xml".
 * The patch configuration files are merged into the above config
 * {@link org.broadleafcommerce.extensibility.MergeXmlConfigResourceFactory}.
 *
 * @author Jeff Fischer
 */
public class MergeContextLoader extends ContextLoader {

	/**
	 * Name of servlet context parameter (i.e., "<code>patchConfigLocation</code>")
	 * that can specify the config location for the root context.
	 */
	public static final String PATCH_LOCATION_PARAM = "patchConfigLocation";

	/**
	 * Instantiate the root WebApplicationContext for this loader, either the
	 * default context class or a custom context class if specified.
	 * <p>This implementation expects custom contexts to implement the
	 * {@link ConfigurableWebApplicationContext} interface.
	 * Can be overridden in subclasses.
	 * <p>In addition, {@link #customizeContext} gets called prior to refreshing the
	 * context, allowing subclasses to perform custom modifications to the context.
	 * @param servletContext current servlet context
	 * @param parent the parent ApplicationContext to use, or <code>null</code> if none
	 * @return the root WebApplicationContext
	 * @throws BeansException if the context couldn't be initialized
	 * @see ConfigurableWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) throws BeansException {
		MergeXmlWebApplicationContext wac = new MergeXmlWebApplicationContext();
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
		wac.setPatchLocation(servletContext.getInitParameter(PATCH_LOCATION_PARAM));
		customizeContext(servletContext, wac);
		wac.refresh();

		return wac;
	}

}
