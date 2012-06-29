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

/**
 * 
 */
package org.broadleafcommerce.core.order.domain;

import org.broadleafcommerce.core.order.service.type.FulfillmentType;
import org.broadleafcommerce.core.pricing.service.fulfillment.processor.FulfillmentPricingProcessor;

import java.io.Serializable;

/**
 * A FulfillmentOption is used to hold information about a particular type of Fulfillment implementation.
 * Third-party fulfillment implementations should extend this to provide their own configuration options
 * particular to that implementation. For instance, a UPS shipping calculator might want an admin user to be
 * able to specify which type of UPS shipping this FulfillmentOption represents.
 * <br />
 * <br />
 * This entity will be presented to the user to allow them to specify which shipping they want. A possible
 * scenario is that say a site can ship with both UPS and Fedex. They will import both the Fedex and UPS
 * third-party modules, each of which will have a unique definition of FulfillmentOption (for instance,
 * FedexFulfillmentOption and UPSFulfillmentOption). Let's say the site can do 2-day shipping with UPS,
 * and next-day shipping with Fedex. What they would do in the admin is create an instance of FedexFulfillmentOption
 * entity and give it the name "Overnight" (along with any needed Fedex configuration properties), then create an instance of
 * UPSFulfillmentOption and give it the name "2 Day". When the user goes to check out, they will then see a list
 * with "Overnight" and "2 day" in it. A FulfillmentPricingProcessor can then be used to estimate the fulfillment cost
 * (and calculate the fulfillment cost) for that particular option.
 * <br />
 * <br />
 * FulfillmentOptions are also inherently related to FulfillmentProcessors, in that specific types of FulfillmentOption
 * implementations should also have a FulfillmentPricingProcessor that can handle operations (estimation and calculation) for
 * pricing a FulfillmentGroup. Typical third-party implementations of this paradigm would have a 1 FulfillmentOption
 * entity implementation and 1 FulfillmentPricingProcessor implementation for that particular service.
 * 
 * @author Phillip Verheyden
 * @see {@link FulfillmentPricingProcessor}, {@link FulfillmentGroup}
 */
public interface FulfillmentOption extends Serializable {
    
    public Long getId();
    
    public void setId(Long id);
    
    /**
     * Gets the name displayed to the user when they selected the FulfillmentOption for
     * their order. This might be "2-day" or "Super-saver shipping"
     * 
     * @return the display name for this option
     */
    public String getName();

    /**
     * Set the display name for this option that will be shown to the user to select from
     * such as "2-day" or "Express" or "Super-saver shipping"
     * 
     * @param name - the display name for this option
     */
    public void setName(String name);

    /**
     * Gets the long description for this option which can be shown to the user
     * to provide more information about the option they are selecting. An example
     * might be that this is shipped the next business day or that it requires additional
     * processing time
     * 
     * @return the description to display to the user
     */
    public String getLongDescription();

    /**
     * Sets the long description for this option to show to the user when they select an option
     * for fulfilling their order 
     * 
     * @param longDescription - the description to show to the user
     */
    public void setLongDescription(String longDescription);

    public Boolean getUseFlatRates();

    public void setUseFlatRates(Boolean useFlatRates);
    
    public Boolean getAddFulfillmentFees();
    
    public void setAddFulfillmentFees(Boolean addFulfillmentFees);

    /**
     * Gets the type of fulfillment that this option supports
     * 
     * @return the type of this option
     */
    public FulfillmentType getFulfillmentType();

    /**
     * Sets the type of fulfillment that this option supports
     * 
     * @param fulfillmentType
     */
    public void setFulfillmentType(FulfillmentType fulfillmentType);
    
}
