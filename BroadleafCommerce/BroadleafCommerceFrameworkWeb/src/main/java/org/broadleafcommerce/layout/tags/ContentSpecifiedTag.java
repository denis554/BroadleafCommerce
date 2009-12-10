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
package org.broadleafcommerce.layout.tags;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.broadleafcommerce.content.domain.ContentDetails;
import org.broadleafcommerce.content.service.ContentService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ContentSpecifiedTag extends SimpleTagSupport {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> parameterMap;
    private String contentType;
    private String contentDetailsProperty;

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
	 * @return the parameterMap
	 */
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}
	/**
	 * @param parameterMap the parameterMap to set
	 */
	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	/**
	 * @return the contentDetailsProperty
	 */
	public String getContentDetailsProperty() {
		return contentDetailsProperty;
	}
	/**
	 * @param contentDetailsProperty the contentDetailsProperty to set
	 */
	public void setContentDetailsProperty(String contentDetailsProperty) {
		this.contentDetailsProperty = contentDetailsProperty;
	}
	
	@Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext)getJspContext();    	
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        ContentService contentService = (ContentService) applicationContext.getBean("blContentService");
        List<ContentDetails> contentDetailObjs;
        Date displayDate = null;
        String sandbox = "PROD";
        List<String> contentXmls = new ArrayList<String>();

        HttpSession session = pageContext.getSession();
        if(session != null){
        	
	        String newSandbox = (String)session.getAttribute("BLC_CONTENT_SANDBOX");
	        String displayDateString = (String)session.getAttribute("BLC_CONTENT_DATE_TIME");
	        
	        if(newSandbox != null && newSandbox != ""){
	        	sandbox = newSandbox;
	        }
	        
	        if(displayDateString != null && displayDateString != ""){
	        	try{
	        		
	        		displayDate = new SimpleDateFormat("MMddyyyy").parse(displayDateString);
	        	}catch (ParseException exp){
	        		throw new JspException();
	        	}
	        }
        }
        
        if(displayDate == null){
        	contentDetailObjs = contentService.findContentDetails(sandbox, contentType, parameterMap);
        }else{
        	contentDetailObjs = contentService.findContentDetails(sandbox, contentType, parameterMap, displayDate);
        }

        for(ContentDetails cd : contentDetailObjs){
        	contentXmls.add(cd.getXmlContent());
        }
        pageContext.setAttribute(contentDetailsProperty, contentXmls);
    }
}
