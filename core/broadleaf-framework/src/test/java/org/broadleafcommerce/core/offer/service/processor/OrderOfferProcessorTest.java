/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.offer.service.processor;

import org.broadleafcommerce.core.offer.dao.OfferDao;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.service.OfferDataItemProvider;
import org.broadleafcommerce.core.offer.service.discount.CandidatePromotionItems;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableCandidateOrderOffer;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableItemFactoryImpl;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrder;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderItem;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.easymock.classextension.EasyMock;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 * @author jfischer
 *
 */
public class OrderOfferProcessorTest extends TestCase {

    private OfferDao offerDaoMock;
    private OrderOfferProcessorImpl orderProcessor;
    private OfferDataItemProvider dataProvider = new OfferDataItemProvider();
    
    @Override
    protected void setUp() throws Exception {
        offerDaoMock = EasyMock.createMock(OfferDao.class);
        orderProcessor = new OrderOfferProcessorImpl();
        orderProcessor.setOfferDao(offerDaoMock);
        orderProcessor.setPromotableItemFactory(new PromotableItemFactoryImpl());
    }
    
    public void replay() {
        EasyMock.replay(offerDaoMock);
    }
    
    public void verify() {
        EasyMock.verify(offerDaoMock);
    }
    
    public void testFilterOffers() throws Exception {
        replay();
        
        PromotableOrder order = dataProvider.createBasicPromotableOrder();
        List<Offer> offers = dataProvider.createCustomerBasedOffer("customer.registered==true", dataProvider.yesterday(), dataProvider.yesterday(), OfferDiscountType.PERCENT_OFF);
        orderProcessor.filterOffers(offers, order.getOrder().getCustomer());
        //confirm out-of-date orders are filtered out
        assertTrue(offers.size() == 0);
        
        offers = dataProvider.createCustomerBasedOffer("customer.registered==true", dataProvider.yesterday(), dataProvider.tomorrow(), OfferDiscountType.PERCENT_OFF);
        orderProcessor.filterOffers(offers, order.getOrder().getCustomer());
        //confirm valid customer offer is retained
        assertTrue(offers.size() == 1);
        
        offers = dataProvider.createCustomerBasedOffer("customer.registered==false", dataProvider.yesterday(), dataProvider.tomorrow(), OfferDiscountType.PERCENT_OFF);
        orderProcessor.filterOffers(offers, order.getOrder().getCustomer());
        //confirm invalid customer offer is culled
        assertTrue(offers.size() == 0);
        
        verify();
    }
    
    public void testFilterOrderLevelOffer() throws Exception {
        replay();
        
        PromotableOrder order = dataProvider.createBasicPromotableOrder();
        List<PromotableCandidateOrderOffer> qualifiedOffers = new ArrayList<PromotableCandidateOrderOffer>();
        List<Offer> offers = dataProvider.createOrderBasedOffer("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF);
        
        orderProcessor.filterOrderLevelOffer(order, qualifiedOffers, offers.get(0));
        
        //test that the valid order offer is included
        assertTrue(qualifiedOffers.size() == 1 && qualifiedOffers.get(0).getOffer().equals(offers.get(0)));
        
        qualifiedOffers = new ArrayList<PromotableCandidateOrderOffer>();
        offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
        orderProcessor.filterOrderLevelOffer(order, qualifiedOffers, offers.get(0));
        
        //test that the valid order offer is included
        assertTrue(qualifiedOffers.size() == 1 && qualifiedOffers.get(0).getOffer().equals(offers.get(0))) ;
         
        qualifiedOffers = new ArrayList<PromotableCandidateOrderOffer>();
        offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([5,6] contains discreteOrderItem.category.id.intValue())");
        orderProcessor.filterOrderLevelOffer(order, qualifiedOffers, offers.get(0));
        
        //test that the invalid order offer is excluded
        assertTrue(qualifiedOffers.size() == 0) ;
        
        verify();
    }
    
    public void testCouldOfferApplyToOrder() throws Exception {
        replay();
        
        PromotableOrder order = dataProvider.createBasicPromotableOrder();
        List<Offer> offers = dataProvider.createOrderBasedOffer("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF);
        boolean couldApply = orderProcessor.couldOfferApplyToOrder(offers.get(0), order, order.getDiscountableOrderItems().get(0), order.getFulfillmentGroups().get(0));
        //test that the valid order offer is included
        assertTrue(couldApply);
        
        offers = dataProvider.createOrderBasedOffer("order.subTotal.getAmount()==0", OfferDiscountType.PERCENT_OFF);
        couldApply = orderProcessor.couldOfferApplyToOrder(offers.get(0), order, order.getDiscountableOrderItems().get(0), order.getFulfillmentGroups().get(0));
        //test that the invalid order offer is excluded
        assertFalse(couldApply);
        
        verify();
    }
    
    public void testCouldOrderItemMeetOfferRequirement() throws Exception {
        replay();
        
        PromotableOrder order = dataProvider.createBasicPromotableOrder();
        List<Offer> offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
        boolean couldApply = orderProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().iterator().next(), order.getDiscountableOrderItems().get(0));
        //test that the valid order offer is included
        assertTrue(couldApply);
        
        offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
        couldApply = orderProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().iterator().next(), order.getDiscountableOrderItems().get(0));
        //test that the invalid order offer is excluded
        assertFalse(couldApply);
        
        verify();
    }
    
    public void testCouldOfferApplyToOrderItems() throws Exception {
        replay();
        
        PromotableOrder order = dataProvider.createBasicPromotableOrder();
        List<Offer> offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
        List<PromotableOrderItem> orderItems = new ArrayList<PromotableOrderItem>();
        for (PromotableOrderItem orderItem : order.getDiscountableOrderItems()) {
            orderItems.add(orderItem);
        }
        CandidatePromotionItems candidates = orderProcessor.couldOfferApplyToOrderItems(offers.get(0), orderItems);
        //test that the valid order offer is included
        assertTrue(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
        
        offers = dataProvider.createOrderBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
        candidates = orderProcessor.couldOfferApplyToOrderItems(offers.get(0), orderItems);
        //test that the invalid order offer is excluded because there are no qualifying items
        assertFalse(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
        
        verify();
    }
}
