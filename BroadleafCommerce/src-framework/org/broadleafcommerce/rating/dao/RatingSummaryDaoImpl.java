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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.rating.domain.RatingDetail;
import org.broadleafcommerce.rating.domain.RatingSummary;
import org.broadleafcommerce.rating.domain.ReviewDetail;
import org.broadleafcommerce.rating.service.type.RatingType;
import org.springframework.stereotype.Repository;

@Repository("blRatingSummaryDao")
public class RatingSummaryDaoImpl implements RatingSummaryDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    public void deleteRatingSummary(RatingSummary summary) {
        em.remove(summary);
    }

    public RatingSummary saveRatingSummary(RatingSummary summary) {
        summary.resetAverageRating();

        if (summary.getId() == null) {
            em.persist(summary);
        } else {
            summary = em.merge(summary);
        }
        return summary;
    }

    @SuppressWarnings("unchecked")
    public List<RatingSummary> readRatingSummaries(List<String> itemIds, RatingType type) {
        Query query = em.createNamedQuery("BC_READ_RATING_SUMMARIES_BY_ITEM_ID_AND_TYPE");
        query.setParameter("itemIds", itemIds);
        query.setParameter("ratingType", type.getType());
        List<RatingSummary> ratings = query.getResultList();

        return ratings;
    }

    public RatingSummary readRatingSummary(String itemId, RatingType type) {
        Query query = em.createNamedQuery("BC_READ_RATING_SUMMARY_BY_ITEM_ID_AND_TYPE");
        query.setParameter("itemId", itemId);
        query.setParameter("ratingType", type.getType());
        RatingSummary ratingSummary = null;

        try {
            ratingSummary = (RatingSummary) query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }

        return ratingSummary;
    }

    public RatingDetail readRating(Long customerId, Long ratingSummaryId) {
        Query query = em.createNamedQuery("BC_READ_RATING_DETAIL_BY_CUSTOMER_ID_AND_RATING_SUMMARY_ID");
        query.setParameter("customerId", customerId);
        query.setParameter("ratingSummaryId", ratingSummaryId);

        RatingDetail ratingDetail = null;

        try {
            ratingDetail = (RatingDetail) query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }

        return ratingDetail;
    }

    public ReviewDetail readReview(Long customerId, Long ratingSummaryId) {
        Query query = em.createNamedQuery("BC_READ_REVIEW_DETAIL_BY_CUSTOMER_ID_AND_RATING_SUMMARY_ID");
        query.setParameter("customerId", customerId);
        query.setParameter("ratingSummaryId", ratingSummaryId);
        ReviewDetail reviewDetail = null;

        try {
            reviewDetail = (ReviewDetail) query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }

        return reviewDetail;
    }

}
