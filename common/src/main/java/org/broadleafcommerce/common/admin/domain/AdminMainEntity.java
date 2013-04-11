/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.common.admin.domain;

/**
 * When viewing entities that implement this interface in the admin, the {@link #getMainEntityName()} method will be
 * invoked to determine the title of the entity to be rendered.
 * 
 * @author Andre Azzolini (apazzolini)
 */
public interface AdminMainEntity {
    
    /**
     * @return the display name of this entity for the admin screen
     */
    public String getMainEntityName();

}
