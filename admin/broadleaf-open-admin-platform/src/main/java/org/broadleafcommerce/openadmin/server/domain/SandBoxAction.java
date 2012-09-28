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

package org.broadleafcommerce.openadmin.server.domain;

import org.broadleafcommerce.openadmin.audit.AdminAuditable;

import java.util.List;

/**
 * Created by bpolster.
 */
public interface SandBoxAction {
    public Long getId();

    public void setId(Long id);

    public SandBoxActionType getActionType();

    public void setActionType(SandBoxActionType type);

    public String getComment();

    public void setComment(String comment);

    public List<SandBoxItem> getSandBoxItems();

    public void setSandBoxItems(List<SandBoxItem> itemList);

    public void addSandBoxItem(SandBoxItem item);

    public AdminAuditable getAuditable();

    public void setAuditable(AdminAuditable auditable);
}
