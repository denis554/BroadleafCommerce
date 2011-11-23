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

package org.broadleafcommerce.core.marketing.service;

import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.core.marketing.dao.TargetContentDao;
import org.broadleafcommerce.core.marketing.domain.TargetContent;
import org.springframework.stereotype.Service;

@Service("blTargetContentService")
public class TargetContentServiceImpl implements TargetContentService {

    @Resource(name="blTargetContentDao")
    protected TargetContentDao targetContentDao;

    public TargetContent findTargetContentById(Long targetContentId) {
        return targetContentDao.readTargetContentById(targetContentId);
    }

    public List<TargetContent> findTargetContents() {
        return targetContentDao.readTargetContents();
    }

    public List<TargetContent> findTargetContentsByNameType(String name, String type) {
        return targetContentDao.readCurrentTargetContentByNameType(name, type);
    }

    public List<TargetContent> findTargetContentsByPriority(int priority) {
        return targetContentDao.readCurrentTargetContentsByPriority(priority);
    }

    public void removeTargetContent(Long targetContentId) {
        targetContentDao.delete(targetContentId);
    }

    public TargetContent updateTargetContent(TargetContent targetContent) {
        return targetContentDao.save(targetContent);
    }

}
