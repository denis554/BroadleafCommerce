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

package org.broadleafcommerce.core.catalog.domain;

import java.io.Serializable;


/**
 * 
 * @author Phillip Verheyden
 */
public interface CategoryAttribute extends Serializable {

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public Long getId();

    /**
     * Sets the id.
     * 
     * @param id the new id
     */
    public void setId(Long id);

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public String getValue();

    /**
     * Sets the value.
     * 
     * @param value the new value
     */
    public void setValue(String value);

    /**
     * Gets the searchable.
     * 
     * @deprecated in favor of Field configuration since 2.0
     * @return the searchable
     */
    public Boolean getSearchable();

    /**
     * Sets the searchable.
     * 
     * @deprecated in favor of Field configuration since 2.0
     * @param searchable the new searchable
     */
    public void setSearchable(Boolean searchable);

    /**
     * Gets the {@link Category}.
     * 
     * @return the {@link Category}
     */
    public Category getCategory();

    /**
     * Sets the {@link Category}.
     * 
     * @param category the new {@link Category}
     */
    public void setCategory(Category category);

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName();

    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name);

}