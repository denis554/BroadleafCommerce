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

package org.broadleafcommerce.core.offer.service.processor;

import junit.framework.TestCase;

/**
 * 
 * @author jfischer
 *
 */
public class FulfillmentGroupOfferProcessorTest extends TestCase {

//	private OfferDao offerDaoMock;
//    private OfferServiceImpl offerService;
//	private OfferDataItemProvider dataProvider = new OfferDataItemProvider();
//	private OrderService orderServiceMock;
//	private OrderItemService orderItemServiceMock;
//	private FulfillmentGroupItemDao fgItemDaoMock;
//	private FulfillmentGroupService fgServiceMock;
//    private OrderMultishipOptionService multishipOptionServiceMock;
//
//	private FulfillmentGroupOfferProcessorImpl fgProcessor;
//
//	@Override
//	protected void setUp() throws Exception {
//		offerService = new OfferServiceImpl();
//        CustomerOfferDao customerOfferDaoMock = EasyMock.createMock(CustomerOfferDao.class);
//        OfferCodeDao offerCodeDaoMock = EasyMock.createMock(OfferCodeDao.class);
//		orderServiceMock = EasyMock.createMock(OrderService.class);
//		orderItemServiceMock = EasyMock.createMock(OrderItemService.class);
//		fgItemDaoMock = EasyMock.createMock(FulfillmentGroupItemDao.class);
//		offerDaoMock = EasyMock.createMock(OfferDao.class);
//		fgServiceMock = EasyMock.createMock(FulfillmentGroupService.class);
//        multishipOptionServiceMock = EasyMock.createMock(OrderMultishipOptionService.class);
//
//
//		fgProcessor = new FulfillmentGroupOfferProcessorImpl();
//		fgProcessor.setOfferDao(offerDaoMock);
//		fgProcessor.setOrderService(orderServiceMock);
//		fgProcessor.setFulfillmentGroupItemDao(fgItemDaoMock);
//		fgProcessor.setOrderItemService(orderItemServiceMock);
//		fgProcessor.setFulfillmentGroupService(fgServiceMock);
//		fgProcessor.setPromotableItemFactory(new PromotableItemFactoryImpl());
//        fgProcessor.setOrderMultishipOptionService(multishipOptionServiceMock);
//
//		OrderOfferProcessor orderProcessor = new OrderOfferProcessorImpl();
//		orderProcessor.setOfferDao(offerDaoMock);
//		orderProcessor.setOrderService(orderServiceMock);
//		orderProcessor.setFulfillmentGroupItemDao(fgItemDaoMock);
//		orderProcessor.setOrderItemService(orderItemServiceMock);
//		orderProcessor.setPromotableItemFactory(new PromotableItemFactoryImpl());
//		orderProcessor.setFulfillmentGroupService(fgServiceMock);
//        orderProcessor.setOrderMultishipOptionService(multishipOptionServiceMock);
//
//		ItemOfferProcessor itemProcessor = new ItemOfferProcessorImpl();
//		itemProcessor.setOfferDao(offerDaoMock);
//		itemProcessor.setOrderService(orderServiceMock);
//		itemProcessor.setFulfillmentGroupItemDao(fgItemDaoMock);
//		itemProcessor.setOrderItemService(orderItemServiceMock);
//		itemProcessor.setFulfillmentGroupService(fgServiceMock);
//		itemProcessor.setPromotableItemFactory(new PromotableItemFactoryImpl());
//        itemProcessor.setOrderMultishipOptionService(multishipOptionServiceMock);
//
//		offerService.setCustomerOfferDao(customerOfferDaoMock);
//		offerService.setOfferCodeDao(offerCodeDaoMock);
//		offerService.setOfferDao(offerDaoMock);
//		offerService.setOrderOfferProcessor(orderProcessor);
//		offerService.setItemOfferProcessor(itemProcessor);
//		offerService.setFulfillmentGroupOfferProcessor(fgProcessor);
//		offerService.setPromotableItemFactory(new PromotableItemFactoryImpl());
//	}
//
//	public void replay() {
//		EasyMock.replay(offerDaoMock);
//		EasyMock.replay(orderServiceMock);
//		EasyMock.replay(orderItemServiceMock);
//		EasyMock.replay(fgItemDaoMock);
//		EasyMock.replay(fgServiceMock);
//        EasyMock.replay(multishipOptionServiceMock);
//	}
//
//	public void verify() {
//		EasyMock.verify(offerDaoMock);
//		EasyMock.verify(orderServiceMock);
//		EasyMock.verify(orderItemServiceMock);
//		EasyMock.verify(fgItemDaoMock);
//		EasyMock.verify(fgServiceMock);
//        EasyMock.verify(multishipOptionServiceMock);
//	}
//
//	public void testApplyAllFulfillmentGroupOffersWithOrderItemOffers() throws Exception {
//		CandidateFulfillmentGroupOfferAnswer candidateFGOfferAnswer = new CandidateFulfillmentGroupOfferAnswer();
//		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(candidateFGOfferAnswer).times(6);
//
//		FulfillmentGroupAdjustmentAnswer fgAdjustmentAnswer = new FulfillmentGroupAdjustmentAnswer();
//		EasyMock.expect(offerDaoMock.createFulfillmentGroupAdjustment()).andAnswer(fgAdjustmentAnswer).times(5);
//
//		CandidateItemOfferAnswer candidateItemOfferAnswer = new CandidateItemOfferAnswer();
//		OrderItemAdjustmentAnswer orderItemAdjustmentAnswer = new OrderItemAdjustmentAnswer();
//		EasyMock.expect(offerDaoMock.createCandidateItemOffer()).andAnswer(candidateItemOfferAnswer).times(2);
//		EasyMock.expect(offerDaoMock.createOrderItemAdjustment()).andAnswer(orderItemAdjustmentAnswer).times(4);
//
//		EasyMock.expect(fgServiceMock.addItemToFulfillmentGroup(EasyMock.isA(FulfillmentGroupItemRequest.class), EasyMock.eq(false))).andAnswer(OfferDataItemProvider.getAddItemToFulfillmentGroupAnswer()).anyTimes();
//		EasyMock.expect(orderServiceMock.removeItem(EasyMock.isA(Long.class), EasyMock.isA(Long.class), EasyMock.eq(false))).andAnswer(OfferDataItemProvider.getRemoveItemFromOrderAnswer()).anyTimes();
//        EasyMock.expect(orderServiceMock.save(EasyMock.isA(Order.class),EasyMock.isA(Boolean.class))).andAnswer(OfferDataItemProvider.getSaveOrderAnswer()).anyTimes();
//
//        EasyMock.expect(orderServiceMock.getAutomaticallyMergeLikeItems()).andReturn(true).anyTimes();
//		EasyMock.expect(orderItemServiceMock.saveOrderItem(EasyMock.isA(OrderItem.class))).andAnswer(OfferDataItemProvider.getSaveOrderItemAnswer()).anyTimes();
//		EasyMock.expect(fgItemDaoMock.save(EasyMock.isA(FulfillmentGroupItem.class))).andAnswer(OfferDataItemProvider.getSaveFulfillmentGroupItemAnswer()).anyTimes();
//
//        EasyMock.expect(multishipOptionServiceMock.findOrderMultishipOptions(EasyMock.isA(Long.class))).andAnswer(new IAnswer<List<OrderMultishipOption>>() {
//            @Override
//            public List<OrderMultishipOption> answer() throws Throwable {
//                return new ArrayList<OrderMultishipOption>();
//            }
//        }).anyTimes();
//
//        multishipOptionServiceMock.deleteAllOrderMultishipOptions(EasyMock.isA(Order.class));
//        EasyMock.expectLastCall().anyTimes();
//        EasyMock.expect(fgServiceMock.collapseToOneFulfillmentGroup(EasyMock.isA(Order.class), EasyMock.eq(false))).andAnswer(OfferDataItemProvider.getSameOrderAnswer()).anyTimes();
//        EasyMock.expect(fgItemDaoMock.create()).andAnswer(OfferDataItemProvider.getCreateFulfillmentGroupItemAnswer()).anyTimes();
//
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//		List<PromotableCandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
//		offers.addAll(dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"));
//		offers.get(1).setName("secondOffer");
//		offers.get(0).setTotalitarianOffer(true);
//		offers.addAll(dataProvider.createItemBasedOfferWithItemCriteria(
//			"order.subTotal.getAmount()>20",
//			OfferDiscountType.PERCENT_OFF,
//			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))",
//			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"
//		));
//		offerService.applyOffersToOrder(offers, order.getDelegate());
//
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(1));
//		boolean offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
//
//		//confirm that at least one of the fg offers was applied
//		assertTrue(offerApplied);
//
//		int fgAdjustmentCount = 0;
//		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
//			fgAdjustmentCount += fg.getDelegate().getFulfillmentGroupAdjustments().size();
//		}
//		//The totalitarian offer that applies to both fg's is not combinable and is a worse offer than the order item offers - it is therefore ignored
//		//However, the second combinable fg offer is allowed to be applied.
//		assertTrue(fgAdjustmentCount == 1);
//
//		order = dataProvider.createBasicOrder();
//		offers.get(2).setValue(new BigDecimal("1"));
//
//		offerService.applyOffersToOrder(offers, order.getDelegate());
//
//		qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(1));
//		offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
//
//		//confirm that at least one of the fg offers was applied
//		assertTrue(offerApplied);
//
//		fgAdjustmentCount = 0;
//		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
//			fgAdjustmentCount += fg.getDelegate().getFulfillmentGroupAdjustments().size();
//		}
//		//The totalitarian fg offer is now a better deal than the order item offers, therefore the totalitarian fg offer is applied
//		//and the order item offers are removed
//		assertTrue(fgAdjustmentCount == 2);
//
//		int itemAdjustmentCount = 0;
//		for (PromotableOrderItem item : order.getDiscreteOrderItems()) {
//			itemAdjustmentCount += item.getDelegate().getOrderItemAdjustments().size();
//		}
//
//		//Confirm that the order item offers are removed
//		assertTrue(itemAdjustmentCount == 0);
//		verify();
//	}
//
//	public void testApplyAllFulfillmentGroupOffers() {
//		CandidateFulfillmentGroupOfferAnswer answer = new CandidateFulfillmentGroupOfferAnswer();
//		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(answer).times(5);
//
//		FulfillmentGroupAdjustmentAnswer answer2 = new FulfillmentGroupAdjustmentAnswer();
//		EasyMock.expect(offerDaoMock.createFulfillmentGroupAdjustment()).andAnswer(answer2).times(5);
//
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//
//		List<PromotableCandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//
//		boolean offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
//
//		assertTrue(offerApplied);
//
//		order = dataProvider.createBasicOrder();
//
//		qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
//		offers.addAll(dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"));
//		offers.get(1).setName("secondOffer");
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(1));
//
//		offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
//
//		//the first offer applies to both fulfillment groups, but the second offer only applies to one of the fulfillment groups
//		assertTrue(offerApplied);
//		int fgAdjustmentCount = 0;
//		for (PromotableFulfillmentGroup fg : order.getFulfillmentGroups()) {
//			fgAdjustmentCount += fg.getDelegate().getFulfillmentGroupAdjustments().size();
//		}
//		assertTrue(fgAdjustmentCount == 3);
//
//		verify();
//	}
//
//	public void testFilterFulfillmentGroupLevelOffer() {
//		CandidateFulfillmentGroupOfferAnswer answer = new CandidateFulfillmentGroupOfferAnswer();
//		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(answer).times(3);
//
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//
//		List<PromotableCandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//
//		//test that the valid fg offer is included
//		//No item criteria, so each fulfillment group applies
//		assertTrue(qualifiedOffers.size() == 2 && qualifiedOffers.get(0).getOffer().equals(offers.get(0)));
//
//		qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//
//		//test that the valid fg offer is included
//		//only 1 fulfillment group has qualifying items
//		assertTrue(qualifiedOffers.size() == 1 && qualifiedOffers.get(0).getOffer().equals(offers.get(0))) ;
//
//		qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75240", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"),MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//
//		//test that the invalid fg offer is excluded - zipcode is wrong
//		assertTrue(qualifiedOffers.size() == 0) ;
//
//		qualifiedOffers = new ArrayList<PromotableCandidateFulfillmentGroupOffer>();
//		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"),MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, offers.get(0));
//
//		//test that the invalid fg offer is excluded - no qualifying items
//		assertTrue(qualifiedOffers.size() == 0) ;
//
//		verify();
//	}
//
//	public void testCouldOfferApplyToFulfillmentGroup() {
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
//		boolean couldApply = fgProcessor.couldOfferApplyToFulfillmentGroup(offers.get(0), (PromotableFulfillmentGroup) order.getFulfillmentGroups().get(0));
//		//test that the valid fg offer is included
//		assertTrue(couldApply);
//
//		offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75240", OfferDiscountType.PERCENT_OFF);
//		couldApply = fgProcessor.couldOfferApplyToFulfillmentGroup(offers.get(0), (PromotableFulfillmentGroup) order.getFulfillmentGroups().get(0));
//		//test that the invalid fg offer is excluded
//		assertFalse(couldApply);
//
//		verify();
//	}
//
//	public void testCouldOrderItemMeetOfferRequirement() {
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//		List<Offer> offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		boolean couldApply = fgProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().iterator().next(), order.getDiscreteOrderItems().get(0));
//		//test that the valid fg offer is included
//		assertTrue(couldApply);
//
//		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		couldApply = fgProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().iterator().next(), order.getDiscreteOrderItems().get(0));
//		//test that the invalid fg offer is excluded
//		assertFalse(couldApply);
//
//		verify();
//	}
//
//	public void testCouldOfferApplyToOrderItems() {
//		replay();
//
//		PromotableOrder order = dataProvider.createBasicOrder();
//
//		List<PromotableOrderItem> orderItems = new ArrayList<PromotableOrderItem>();
//		for (PromotableOrderItem orderItem : order.getDiscountableDiscreteOrderItems()) {
//			orderItems.add(orderItem);
//		}
//
//		List<Offer> offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		CandidatePromotionItems candidates = fgProcessor.couldOfferApplyToOrderItems(offers.get(0), orderItems);
//		//test that the valid fg offer is included
//		assertTrue(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
//
//		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
//		candidates = fgProcessor.couldOfferApplyToOrderItems(offers.get(0), orderItems);
//		//test that the invalid fg offer is excluded because there are no qualifying items
//		assertFalse(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
//
//		verify();
//	}
//
//	public class CandidateFulfillmentGroupOfferAnswer implements IAnswer<CandidateFulfillmentGroupOffer> {
//
//		public CandidateFulfillmentGroupOffer answer() throws Throwable {
//			return new CandidateFulfillmentGroupOfferImpl();
//		}
//
//	}
//
//	public class FulfillmentGroupAdjustmentAnswer implements IAnswer<FulfillmentGroupAdjustment> {
//
//		public FulfillmentGroupAdjustment answer() throws Throwable {
//			return new FulfillmentGroupAdjustmentImpl();
//		}
//
//	}
//
//	public class CandidateItemOfferAnswer implements IAnswer<CandidateItemOffer> {
//
//		public CandidateItemOffer answer() throws Throwable {
//			return new CandidateItemOfferImpl();
//		}
//
//	}
//
//	public class OrderItemAdjustmentAnswer implements IAnswer<OrderItemAdjustment> {
//
//		public OrderItemAdjustment answer() throws Throwable {
//			return new OrderItemAdjustmentImpl();
//		}
//
//	}
}
