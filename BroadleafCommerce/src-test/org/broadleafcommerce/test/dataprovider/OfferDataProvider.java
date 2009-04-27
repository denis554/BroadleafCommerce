package org.broadleafcommerce.test.dataprovider;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.offer.domain.Offer;
import org.broadleafcommerce.offer.domain.OfferImpl;
import org.broadleafcommerce.type.OfferDiscountType;
import org.broadleafcommerce.type.OfferScopeType;
import org.broadleafcommerce.type.OfferType;
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
        o.setScopeType(OfferScopeType.ALL);
        o.setStackable(true);
        o.setStartDate(new Date(DateUtil.getNow().getTime()));
        o.setEndDate(new Date(DateUtil.getNow().getTime()+100000000));
        o.setApplyDiscountToSalePrice(false);
        o.setTargetSystem("WEB");
        o.setType(OfferType.ORDER_ITEM);
        o.setAppliesToRules(
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
        o.setScopeType(OfferScopeType.ALL);
        o.setStackable(false);
        o.setStartDate(new Date(DateUtil.getNow().getTime()));
        o.setEndDate(new Date(DateUtil.getNow().getTime()+100000000));
        o.setApplyDiscountToSalePrice(false);
        o.setTargetSystem("WEB");
        o.setType(OfferType.FULFILLMENT_GROUP);
        o.setAppliesToRules(
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
