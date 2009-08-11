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
package org.broadleafcommerce.offer;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.offer.domain.Offer;
import org.broadleafcommerce.offer.domain.OfferImpl;
import org.broadleafcommerce.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.offer.service.type.OfferType;
import org.broadleafcommerce.util.DateUtil;
import org.broadleafcommerce.util.money.Money;
import org.testng.annotations.DataProvider;


public class OfferDataProvider {

    @DataProvider(name = "offerDataProvider")
    public static Object[][] provideBasicOffer(){
        List<Offer> allOffers = new ArrayList<Offer>();
        OfferImpl o = new OfferImpl();
        o.setDiscountType(OfferDiscountType.AMOUNT_OFF);
        o.setValue(new Money(new BigDecimal("5.00")));
        o.setName("Some test offer");
        o.setPriority(100);
        o.setStackable(true);
        o.setStartDate(new Date(DateUtil.getNow().getTime()));
        o.setEndDate(new Date(DateUtil.getNow().getTime()+100000000));
        o.setApplyDiscountToMarkedItems(false);
        o.setTargetSystem("WEB");
        o.setType(OfferType.ORDER_ITEM);
        o.setAppliesToOrderRules(
                "package org.broadleafcommerce.offer.service;"+
                "import org.broadleafcommerce.offer.domain.Offer;"+
                "import org.broadleafcommerce.order.domain.Order;"+
                "import org.broadleafcommerce.order.domain.OrderItem;"+
                "import org.broadleafcommerce.type.OfferType;"+
                "import java.util.List;"+
                "global List orderItems;"+
                "global List offerPackages;"+
                "rule \"Offer 1 Rule\" "+
                "salience 100"+
                "when "+
                "  orderItem : OrderItem(sku == 1) "+
                "  "+
                " then"+
                "   System.err.println(\"applying offer 1\");"+
                "   orderItem.addRulesCandidateOffer"+
        "end");

        allOffers.add(o);
        o = new OfferImpl();
        o.setDiscountType(OfferDiscountType.AMOUNT_OFF);
        o.setValue(new Money(new BigDecimal("5.00")));
        o.setName("Second test offer");
        o.setPriority(100);
        o.setStackable(false);
        o.setStartDate(new Date(DateUtil.getNow().getTime()));
        o.setEndDate(new Date(DateUtil.getNow().getTime()+100000000));
        o.setApplyDiscountToMarkedItems(false);
        o.setTargetSystem("WEB");
        o.setType(OfferType.FULFILLMENT_GROUP);
        o.setAppliesToOrderRules(
                "package org.broadleafcommerce.offer.service;"+
                "import org.broadleafcommerce.offer.domain.Offer;"+
                "import org.broadleafcommerce.order.domain.Order;"+
                "import org.broadleafcommerce.order.domain.OrderItem;"+
                "import org.broadleafcommerce.type.OfferType;"+
                "import java.util.List;"+
                "global List orderItems;"+
                "global List offerPackages;"+
                "rule \"Offer 1 Rule\" "+
                "salience 100"+
                "when "+
                "  orderItem : OrderItem(retailPrice &gt= 100)"+
                " then"+
                " System.err.println(\"applying offer 2\");"+
                " insert(offer);"+
        "end");

        allOffers.add(o);
        return new Object[][] {{allOffers}};

    }

}
