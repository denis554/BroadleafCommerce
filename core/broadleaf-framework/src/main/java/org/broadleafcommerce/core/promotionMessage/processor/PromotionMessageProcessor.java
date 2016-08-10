/*
 * #%L
 * BroadleafCommerce Framework Web
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
package org.broadleafcommerce.core.promotionMessage.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.promotionMessage.domain.PromotionMessage;
import org.broadleafcommerce.core.promotionMessage.service.PromotionMessageService;
import org.springframework.stereotype.Service;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.element.AbstractLocalVariableDefinitionElementProcessor;
import org.thymeleaf.standard.expression.Expression;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

/**
 * @author Chris Kittrell (ckittrell)
 */
@Service("blPromotionMessageProcessor")
public class PromotionMessageProcessor extends AbstractLocalVariableDefinitionElementProcessor {

    private static final Log LOG = LogFactory.getLog(PromotionMessageProcessor.class);

    public static final String PRODUCT = "product";

    @Resource(name="blPromotionMessageService")
    protected PromotionMessageService promotionMessageService;

    /**
     * Sets the name of this processor to be used in Thymeleaf template
     */
    public PromotionMessageProcessor() {
        super("offer_promotion_messages");
    }
    
    @Override
    public int getPrecedence() {
        return 1000;
    }

    @Override
    protected Map<String, Object> getNewLocalVariables(Arguments arguments, Element element) {
        Product product = getProductFromArguments(arguments, element);
        Set<PromotionMessage> promotionMessages = promotionMessageService.findActivePromotionMessagesForProduct(product);

        Map<String, Object> newVars = new HashMap<>();
        newVars.put("promotionMessages", promotionMessages);
        return newVars;
    }

    protected Product getProductFromArguments(Arguments arguments, Element element) {
        Expression expression = (Expression) StandardExpressions.getExpressionParser(arguments.getConfiguration())
                .parseExpression(arguments.getConfiguration(), arguments, element.getAttributeValue(PRODUCT));
        return (Product) expression.execute(arguments.getConfiguration(), arguments);
    }

    @Override
    protected boolean removeHostElement(Arguments arguments, Element element) {
        return false;
    }
}
