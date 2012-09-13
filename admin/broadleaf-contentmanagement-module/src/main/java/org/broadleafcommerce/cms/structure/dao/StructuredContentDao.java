/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.cms.structure.dao;

import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.cms.structure.domain.StructuredContent;
import org.broadleafcommerce.cms.structure.domain.StructuredContentField;
import org.broadleafcommerce.cms.structure.domain.StructuredContentType;
import org.broadleafcommerce.common.sandbox.domain.SandBox;

import java.util.List;
import java.util.Map;

/**
 * Responsible for querying and updating {@link StructuredContent} items
 * @author bpolster
 */
public interface StructuredContentDao {
    /**
     * Returns the <code>StructuredContent</code> item that matches
     * the passed in Id.
     * @param contentId
     * @return the found item or null if it does not exist
     */
    public StructuredContent findStructuredContentById(Long contentId);

    /**
     * Returns the <code>StructuredContentType</code> that matches
     * the passed in contentTypeId.
     * @param contentTypeId
     * @return the found item or null if it does not exist
     */
    public StructuredContentType findStructuredContentTypeById(Long contentTypeId);

    /**
     * Returns the list of all <code>StructuredContentType</code>s.
     *
     * @return the list of found items
     */
    public List<StructuredContentType> retrieveAllStructuredContentTypes();

    public Map<String,StructuredContentField> readFieldsForStructuredContentItem(StructuredContent sc);

    /**
     * Persists the changes or saves a new content item.
     *
     * @param content
     * @return the newly saved or persisted item
     */
    public StructuredContent addOrUpdateContentItem(StructuredContent content);

    /**
     * Removes the passed in item from the underlying storage.
     *
     * @param content
     */
    public void delete(StructuredContent content);

    /**
     * Called by the <code>DisplayContentTag</code> to locate content based
     * on the current SandBox, StructuredContentType, and Locale.
     *
     * @param sandBox to search for the content
     * @param type of content to search for
     * @param locale to restrict the search to
     * @return a list of all matching content
     * @see org.broadleafcommerce.cms.web.structure.DisplayContentTag
     */
    public List<StructuredContent> findActiveStructuredContentByType(SandBox sandBox, StructuredContentType type, Locale locale);

    /**
     * Called by the <code>DisplayContentTag</code> to locate content based
     * on the current SandBox, StructuredContentType, Name, and Locale.
     *
     * @param sandBox
     * @param type
     * @param name
     * @param locale
     * @return
     */
    public List<StructuredContent> findActiveStructuredContentByNameAndType(SandBox sandBox, StructuredContentType type, String name, Locale locale);

    /**
     * Called by the <code>DisplayContentTag</code> to locate content based
     * on the current SandBox, StructuredContentType, Name, and Locale.
     *
     * @param sandBox
     * @param name
     * @param locale
     * @return
     */
    public List<StructuredContent> findActiveStructuredContentByName(SandBox sandBox, String name, Locale locale);


    /**
     * Used to lookup the StructuredContentType by name.
     *
     * @param name
     * @return
     */
    public StructuredContentType findStructuredContentTypeByName(String name);

    /**
     * Detaches the item from the JPA session.   This is intended for internal
     * use by the CMS system.   It supports the need to clone an item as part
     * of the editing process.
     *
     * @param sc - the item to detach
     */
    public void detach(StructuredContent sc);
}
