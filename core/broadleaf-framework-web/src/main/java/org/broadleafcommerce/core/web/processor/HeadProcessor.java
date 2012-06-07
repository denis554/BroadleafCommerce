/*
 * Copyright 2012 the original author or authors.
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

package org.broadleafcommerce.core.web.processor;

import java.util.Map;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractFragmentElementProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;

/**
 * A Thymeleaf processor that will include the standard head element. It will also set the
 * following variables for use by the head fragment.
 * 
 * <ul>
 * 	<li><b>pageTitle</b> - The title of the page</li>
 * 	<li><b>additionalCss</b> - An additional, page specific CSS file to include</li>
 * 
 * 
 * @author apazzolini
 */
public class HeadProcessor extends AbstractFragmentElementProcessor {

    public static final String FRAGMENT_ATTR_NAME = StandardFragmentAttrProcessor.ATTR_NAME;
    
	/**
	 * Sets the name of this processor to be used in Thymeleaf template
	 */
    public HeadProcessor() {
        super("head");
    }

    @Override
    public int getPrecedence() {
        return 10000;
    }

	@Override
	@SuppressWarnings("unchecked")
	protected AbstractFragmentSpec getFragmentSpec(Arguments arguments, Element element) {
		((Map<String, Object>) arguments.getExpressionEvaluationRoot()).put("pageTitle", element.getAttributeValue("pageTitle"));
		((Map<String, Object>) arguments.getExpressionEvaluationRoot()).put("additionalCss", element.getAttributeValue("additionalCss"));
        return new CompleteTemplateFragmentSpec("partials/head");
	}



	@Override
	protected boolean getSubstituteInclusionNode(Arguments arguments, Element element) {
		return true;
	}

}
