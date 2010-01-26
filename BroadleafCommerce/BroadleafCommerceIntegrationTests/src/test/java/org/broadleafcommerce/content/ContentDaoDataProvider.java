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
package org.broadleafcommerce.content;

import java.util.Date;

import org.broadleafcommerce.content.domain.Content;
import org.broadleafcommerce.content.domain.ContentDetails;
import org.broadleafcommerce.content.domain.ContentDetailsImpl;
import org.broadleafcommerce.content.domain.ContentImpl;
import org.broadleafcommerce.util.DateUtil;

import org.testng.annotations.DataProvider;

/**
* DOCUMENT ME!
*
* @author btaylor
 */
public class ContentDaoDataProvider {
    @DataProvider(name = "basicContent")
    public static Object[][] provideBasicContent() {
        Content content = new ContentImpl();
        content.setOnline(true);
        content.setActiveStartDate(new Date(DateUtil.getNow()));
        content.setActiveEndDate(new Date(DateUtil.getNow() + 100000000));
        content.setDisplayRule("customer.location=tx");
        content.setContentType("HomePageArticle");
        content.setSandbox("AwaitingApproval_TestUser_123");
        content.setTitle("/some/file/path");

        return new Object[][] {
                   { content }
               };
    }

    @DataProvider(name = "basicContentAndDetail")
    public static Object[][] provideBasicContentAndDetail() {
        Content content = new ContentImpl();
        content.setOnline(true);
        content.setActiveStartDate(new Date(DateUtil.getNow()));
        content.setActiveEndDate(new Date(DateUtil.getNow() + 100000000));
        content.setDisplayRule("customer.location=tx");
        content.setContentType("HomePageArticle");
        content.setTitle("/some/file/path");

        ContentDetails contentDetails = new ContentDetailsImpl();
        contentDetails.setContentHash("abc123");
        contentDetails.setXmlContent("abc123");

        return new Object[][] {
                   { content, contentDetails }
               };
    }
}
