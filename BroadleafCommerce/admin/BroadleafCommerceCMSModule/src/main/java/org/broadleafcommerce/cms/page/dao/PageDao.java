/*
 * Copyright 2008-20011 the original author or authors.
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
package org.broadleafcommerce.cms.page.dao;

import org.broadleafcommerce.cms.locale.domain.Locale;
import org.broadleafcommerce.cms.page.domain.Page;
import org.broadleafcommerce.cms.page.domain.PageField;
import org.broadleafcommerce.cms.page.domain.PageFolder;
import org.broadleafcommerce.cms.page.domain.PageTemplate;
import org.broadleafcommerce.openadmin.server.domain.SandBox;

import java.util.List;
import java.util.Map;

/**
 * Created by bpolster.
 */
public interface PageDao {

    public PageFolder readPageById(Long id);

    public PageTemplate readPageTemplateById(Long id);

    public Map<String, PageField> readPageFieldsByPage(Page page);

    public List<PageFolder> readPageFolderChildren(PageFolder parentFolder, String localeCode, SandBox userSandbox, SandBox productionSandBox);

    public Page updatePage(Page page, boolean clearLevel1Cache);

    public void delete(Page page);

    public PageFolder updatePageFolder(PageFolder pageFolder);

    public Page addPage(Page clonedPage);

    public PageFolder addPageFolder(PageFolder pageFolder);

    public Page findPageByURI(SandBox sandBox, Locale locale, String uri);
}
