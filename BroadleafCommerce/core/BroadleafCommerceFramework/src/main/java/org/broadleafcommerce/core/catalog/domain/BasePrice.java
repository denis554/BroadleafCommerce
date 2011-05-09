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
package org.broadleafcommerce.core.catalog.domain;

import java.io.Serializable;
import java.util.Date;

import org.broadleafcommerce.money.Money;
import org.broadleafcommerce.profile.common.domain.Auditable;

/**
 * The Interface BasePrice.
 */
public interface BasePrice extends Serializable {

    /**
     * Gets the id.
     * 
     * @return the id
     */
    Long getId();

    /**
     * Sets the id.
     * 
     * @param id the new id
     */
    void setId(Long id);

    /**
     * Gets the amount.
     * 
     * @return the amount
     */
    Money getAmount();

    /**
     * Sets the amount.
     * 
     * @param amount the new amount
     */
    void setAmount(Money amount);

    /**
     * Gets the start date.
     * 
     * @return the start date
     */
    Date getStartDate();

    /**
     * Sets the start date.
     * 
     * @param startDate the new start date
     */
    void setStartDate(Date startDate);

    /**
     * Gets the end date.
     * 
     * @return the end date
     */
    Date getEndDate();

    /**
     * Sets the end date.
     * 
     * @param endDate the new end date
     */
    void setEndDate(Date endDate);

    /**
     * Gets the sku.
     * 
     * @return the sku
     */
    Sku getSku();

    /**
     * Sets the sku.
     * 
     * @param sku the new sku
     */
    void setSku(Sku sku);

    /**
     * Gets the auditable.
     * 
     * @return the auditable
     */
    Auditable getAuditable();

    /**
     * Sets the auditable.
     * 
     * @param auditable the new auditable
     */
    void setAuditable(Auditable auditable);
}
