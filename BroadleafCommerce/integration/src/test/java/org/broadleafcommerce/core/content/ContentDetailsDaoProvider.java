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

package org.broadleafcommerce.core.content;

import org.broadleafcommerce.core.content.domain.ContentDetails;
import org.broadleafcommerce.core.content.domain.ContentDetailsImpl;
import org.testng.annotations.DataProvider;

/**
 * @author btaylor
 *
 */
public class ContentDetailsDaoProvider {

	@DataProvider(name = "basicContentDetails")
	public static Object[][] provideBasicContentDetails() {
		ContentDetails cd = new ContentDetailsImpl();
		cd.setId(Integer.getInteger("1919"));
		cd.setXmlContent("<xml>some xml content</xml>");
		return new Object[][] {{cd}};
	}
}
