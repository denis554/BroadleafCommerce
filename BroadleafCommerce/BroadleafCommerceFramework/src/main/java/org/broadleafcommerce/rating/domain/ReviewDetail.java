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
package org.broadleafcommerce.rating.domain;

import java.util.Date;
import java.util.List;

import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.rating.service.type.ReviewStatusType;

public interface ReviewDetail {

    public Long getId();
    public Customer getCustomer();
    public String getReviewText();
    public void setReviewText(String reviewText);
    public Date getReviewSubmittedDate();
    public Integer helpfulCount();
    public Integer notHelpfulCount();
    public ReviewStatusType getStatus();
    public RatingSummary getRatingSummary();
    public RatingDetail getRatingDetail();
    public List<ReviewFeedback> getReviewFeedback();

}
