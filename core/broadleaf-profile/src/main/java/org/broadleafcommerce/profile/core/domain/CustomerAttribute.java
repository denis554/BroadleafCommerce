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

package org.broadleafcommerce.profile.core.domain;

import java.io.Serializable;


/**
 * Implementations of this interface are used to hold data about a Customers Attributes.
 * <br>
 * For high volume sites, you should consider extending the BLC Customer entity instead of
 * relying on custom attributes as the extension mechanism is more performant under load.
 *
 * @see {@link CustomerAttributeImpl}, {@link Customer}
 * @author bpolster
 *
 */
public interface CustomerAttribute extends Serializable {

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
     * Gets the name.  (e.g. shoeSize)
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

    /**
     * Gets the value.   (e.g. 9.5)
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
     * Gets the associated customer.
     *
     * @return the customer
     */
    public Customer getCustomer();

    /**
     * Sets the associated customer.
     *
     * @param customer
     */
    public void setCustomer(Customer customer);
}
