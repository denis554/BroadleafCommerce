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

package org.broadleafcommerce.core.offer.domain;

import java.io.Serializable;
import java.sql.Date;

import org.broadleafcommerce.common.money.Money;

public interface OfferAudit extends Serializable {

    public Long getId();

    public void setId(Long id);

    public Offer getOffer();

    public void setOffer(Offer offer);

    public Long getOfferCodeId();

    public void setOfferCodeId(Long offerCodeId);

    public Long getCustomerId();

    public void setCustomerId(Long customerId);

    public void setRelatedId(Long id);

    public Money getRelatedRetailPrice();

    public void setRelatedRetailPrice(Money relatedRetailPrice);

    public Money getRelatedSalePrice();

    public void setRelatedSalePrice(Money relatedSalePrice);

    public Money getRelatedPrice();

    public void setRelatedPrice(Money relatedPrice);

    public Date getRedeemedDate();

    public void setRedeemedDate(Date redeemedDate);
}
