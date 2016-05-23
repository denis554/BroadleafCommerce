/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.offer.service.discount;

import org.broadleafcommerce.core.offer.service.discount.domain.PromotableCandidateItemOffer;

import java.util.Comparator;

/**
 * @author Chad Harchar (charchar)
 */
public class ItemOfferWeightedPercentComparator implements Comparator<PromotableCandidateItemOffer> {

    public static ItemOfferWeightedPercentComparator INSTANCE = new ItemOfferWeightedPercentComparator();

    public int compare(PromotableCandidateItemOffer p1, PromotableCandidateItemOffer p2) {

        Integer priority1 = p1.getPriority();
        Integer priority2 = p2.getPriority();

        int result = priority1.compareTo(priority2);

        if (result == 0) {
            // highest weighted percent wins
            return p2.getWeightedPercentSaved().compareTo(p1.getWeightedPercentSaved());
        }
        return result;
    }

}
