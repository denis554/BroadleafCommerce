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
package org.broadleafcommerce.rating.dao;

import java.util.List;

import org.broadleafcommerce.rating.domain.RatingDetail;
import org.broadleafcommerce.rating.domain.RatingSummary;
import org.broadleafcommerce.rating.domain.ReviewDetail;
import org.broadleafcommerce.rating.service.type.RatingType;

public interface RatingSummaryDao {

    public RatingSummary readRatingSummary(String itemId, RatingType type);
    public List<RatingSummary> readRatingSummaries(List<String> itemIds, RatingType type);
    public RatingSummary saveRatingSummary(RatingSummary summary);
    public void deleteRatingSummary(RatingSummary summary);

    public RatingDetail readRating(Long customerId, Long ratingSummaryId);
    public ReviewDetail readReview(Long customerId, Long ratingSummaryId);
}
