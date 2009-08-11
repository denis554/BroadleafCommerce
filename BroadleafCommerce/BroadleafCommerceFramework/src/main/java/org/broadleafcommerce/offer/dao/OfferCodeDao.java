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
package org.broadleafcommerce.offer.dao;

import org.broadleafcommerce.offer.domain.OfferCode;

public interface OfferCodeDao {

	public OfferCode readOfferCodeById(Long offerCode);

    public OfferCode readOfferCodeByCode(String code);

	public OfferCode save(OfferCode offerCode);

	public void delete(OfferCode offerCodeId);

	public OfferCode create();

}
