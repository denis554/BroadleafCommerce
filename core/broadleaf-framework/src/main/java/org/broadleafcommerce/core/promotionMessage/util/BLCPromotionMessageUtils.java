/*
 * #%L
 * BroadleafCommerce Common Libraries
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
package org.broadleafcommerce.core.promotionMessage.util;

import org.broadleafcommerce.common.util.BLCCollectionUtils;
import org.broadleafcommerce.common.util.TypedTransformer;
import org.broadleafcommerce.core.promotionMessage.domain.PromotionMessage;
import org.broadleafcommerce.core.promotionMessage.domain.type.PromotionMessagePlacementType;
import org.broadleafcommerce.core.promotionMessage.dto.PromotionMessageDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convenience methods for interacting with {@link PromotionMessage}s
 * 
 * @author Chris Kittrell (ckittrell)
 */
public class BLCPromotionMessageUtils {
    
    /**
     * Given a list of {@link PromotionMessage}s, gather their message properties.
     * 
     * @param promotionMessages
     * @return messages
     */
    public static List<String> gatherMessages(List<PromotionMessage> promotionMessages) {
        return BLCCollectionUtils.collectList(promotionMessages, new TypedTransformer<String>() {
            @Override
            public String transform(Object input) {
                PromotionMessage promotionMessage = (PromotionMessage) input;
                return promotionMessage.getMessage();
            }
        });
    }

    /**
     * Given a map of {@link PromotionMessagePlacementType}s to {@link PromotionMessageDTO}s, gather the message properties.
     *
     * @param promotionMessageMap
     * @param type
     * @return messages
     */
    public static List<String> gatherMessagesByPlacementType(Map<String, List<PromotionMessageDTO>> promotionMessageMap,
            PromotionMessagePlacementType type) {
        List<PromotionMessageDTO> promotionMessageDTOs = promotionMessageMap.get(type.getType());

        if (promotionMessageDTOs == null) {
            return new ArrayList<>();
        }

        return BLCCollectionUtils.collectList(promotionMessageDTOs, new TypedTransformer<String>() {
            @Override
            public String transform(Object input) {
                PromotionMessageDTO promotionMessageDTO = (PromotionMessageDTO) input;
                return promotionMessageDTO.getMessage();
            }
        });
    }
}
