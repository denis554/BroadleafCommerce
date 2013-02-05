/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.web.processor;

import com.gwtincubator.security.exception.ApplicationSecurityException;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.web.dialect.AbstractModelVariableModifierProcessor;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.web.service.RuleBuilderService;
import org.broadleafcommerce.openadmin.web.translation.MVELTranslationException;
import org.broadleafcommerce.openadmin.web.translation.dto.OutputWrapper;
import org.broadleafcommerce.openadmin.web.translation.dto.RuleBuilderDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.spring3.context.SpringWebContext;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Component("blAdminPromoOrderItemProcessor")
public class AdminPromoOrderItemProcessor extends AbstractModelVariableModifierProcessor {

    private RuleBuilderService ruleBuilderService;

    /**
     * Sets the name of this processor to be used in Thymeleaf template
     */
    public AdminPromoOrderItemProcessor() {
        super("admin_promo_order_item");
    }

    @Override
    public int getPrecedence() {
        return 10000;
    }

    @Override
    protected void modifyModelAttributes(Arguments arguments, Element element) {
        String resultVar = element.getAttributeValue("resultVar");
        OutputWrapper wrapper = new OutputWrapper();
        initServices(arguments);

        try {
            //String ceilingOfferItem = "org.broadleafcommerce.core.offer.domain.OfferItemCriteria";
            //ForeignKey[] fksTargetOfferItem = new ForeignKey[]{new ForeignKey("targetOffer", "org.broadleafcommerce.core.offer.domain.OfferImpl", null)};
            //RuleBuilderDTO dtoOfferItem = new RuleBuilderDTO(ceilingOfferItem, null, fksTargetOfferItem);
            //ForeignKey[] fksQualifyOfferItem = new ForeignKey[]{new ForeignKey("qualifyingOffer", "org.broadleafcommerce.core.offer.domain.OfferImpl", null)};
            //RuleBuilderDTO dtoQualifyOfferItem = new RuleBuilderDTO(ceilingOfferItem, null, fksQualifyOfferItem);

            Entity[] entities = (Entity[]) StandardExpressionProcessor.processExpression(arguments, element.getAttributeValue("entities"));

            String mvelPropertyName = "orderItemMatchRule";
            String mvel = null;
            if (entities != null) {
                for (Entity e : entities) {
                    for (Property p : e.getProperties()) {
                        if (mvelPropertyName.equals(p.getName())){
                            mvel = p.getValue();
                        }
                    }
                }
            }

            if (mvel != null) {
                String ceilingOrderItem = "org.broadleafcommerce.core.order.domain.OrderItem";
                String configKeyOrderItem = "promotionOrderItem";
                RuleBuilderDTO dtoOrderItem = new RuleBuilderDTO(ceilingOrderItem, configKeyOrderItem, null);

                List<RuleBuilderDTO> dtoList = new ArrayList<RuleBuilderDTO>();
                dtoList.add(dtoOrderItem);

                wrapper.setConditions(ruleBuilderService.buildConditionsDTO(dtoList, mvel));
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (ApplicationSecurityException e) {
            e.printStackTrace();
        } catch (MVELTranslationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addToModel(arguments, resultVar, wrapper);
    }

    protected void initServices(Arguments arguments) {
        final ApplicationContext applicationContext = ((SpringWebContext) arguments.getContext()).getApplicationContext();

        if (ruleBuilderService == null) {
            ruleBuilderService = (RuleBuilderService) applicationContext.getBean("blRuleBuilderService");
        }

    }

}
