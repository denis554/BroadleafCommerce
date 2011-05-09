package org.broadleafcommerce.core.offer.service.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.broadleafcommerce.core.offer.dao.CustomerOfferDao;
import org.broadleafcommerce.core.offer.dao.OfferCodeDao;
import org.broadleafcommerce.core.offer.dao.OfferDao;
import org.broadleafcommerce.core.offer.domain.CandidateFulfillmentGroupOffer;
import org.broadleafcommerce.core.offer.domain.CandidateFulfillmentGroupOfferImpl;
import org.broadleafcommerce.core.offer.domain.CandidateItemOffer;
import org.broadleafcommerce.core.offer.domain.CandidateItemOfferImpl;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustment;
import org.broadleafcommerce.core.offer.domain.FulfillmentGroupAdjustmentImpl;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustmentImpl;
import org.broadleafcommerce.core.offer.service.OfferDataItemProvider;
import org.broadleafcommerce.core.offer.service.OfferServiceImpl;
import org.broadleafcommerce.core.offer.service.discount.CandidatePromotionItems;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;

public class FulfillmentGroupOfferProcessorTest extends TestCase {

	private OfferDao offerDaoMock;
	private CustomerOfferDao customerOfferDaoMock;
	private OfferCodeDao offerCodeDaoMock;
	private FulfillmentGroupOfferProcessorImpl fgProcessor;
	private OfferServiceImpl offerService;
	private OfferDataItemProvider dataProvider = new OfferDataItemProvider();
	
	@Override
	protected void setUp() throws Exception {
		offerService = new OfferServiceImpl();
		customerOfferDaoMock = EasyMock.createMock(CustomerOfferDao.class);
		offerCodeDaoMock = EasyMock.createMock(OfferCodeDao.class);
		offerDaoMock = EasyMock.createMock(OfferDao.class);
		offerService.setCustomerOfferDao(customerOfferDaoMock);
		offerService.setOfferCodeDao(offerCodeDaoMock);
		offerService.setOfferDao(offerDaoMock);
		
		fgProcessor = new FulfillmentGroupOfferProcessorImpl();
		fgProcessor.setOfferDao(offerDaoMock);
		
		OrderOfferProcessor orderProcessor = new OrderOfferProcessorImpl();
		orderProcessor.setOfferDao(offerDaoMock);
		offerService.setOrderOfferProcessor(orderProcessor);
		
		ItemOfferProcessor itemProcessor = new ItemOfferProcessorImpl();
		itemProcessor.setOfferDao(offerDaoMock);
		offerService.setItemOfferProcessor(itemProcessor);
		
		offerService.setFulfillmentGroupOfferProcessor(fgProcessor);
	}
	
	public void replay() {
		EasyMock.replay(offerDaoMock);
	}
	
	public void verify() {
		EasyMock.verify(offerDaoMock);
	}
	
	public void testApplyAllFulfillmentGroupOffersWithOrderItemOffers() throws Exception {
		CandidateFulfillmentGroupOfferAnswer candidateFGOfferAnswer = new CandidateFulfillmentGroupOfferAnswer();
		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(candidateFGOfferAnswer).times(6);
		
		FulfillmentGroupAdjustmentAnswer fgAdjustmentAnswer = new FulfillmentGroupAdjustmentAnswer();
		EasyMock.expect(offerDaoMock.createFulfillmentGroupAdjustment()).andAnswer(fgAdjustmentAnswer).times(5);
		
		CandidateItemOfferAnswer candidateItemOfferAnswer = new CandidateItemOfferAnswer();
		OrderItemAdjustmentAnswer orderItemAdjustmentAnswer = new OrderItemAdjustmentAnswer();
		EasyMock.expect(offerDaoMock.createCandidateItemOffer()).andAnswer(candidateItemOfferAnswer).times(2);
		EasyMock.expect(offerDaoMock.createOrderItemAdjustment()).andAnswer(orderItemAdjustmentAnswer).times(4);
		
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<CandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
		offers.addAll(dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"));
		offers.get(1).setName("secondOffer");
		offers.get(0).setTotalitarianOffer(true);
		offers.addAll(dataProvider.createItemBasedOfferWithItemCriteria(
			"order.subTotal.getAmount()>20", 
			OfferDiscountType.PERCENT_OFF, 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))", 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"
		));
		offerService.applyOffersToOrder(offers, order);
		
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(1));
		boolean offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
		
		//confirm that at least one of the fg offers was applied
		assertTrue(offerApplied);
		
		int fgAdjustmentCount = 0;
		for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
			fgAdjustmentCount += fg.getFulfillmentGroupAdjustments().size();
		}
		//The totalitarian offer that applies to both fg's is not combinable and is a worse offer than the order item offers - it is therefore ignored
		//However, the second combinable fg offer is allowed to be applied.
		assertTrue(fgAdjustmentCount == 1);
		
		order = dataProvider.createBasicOrder();
		offers.get(2).setValue(new BigDecimal("1"));
		
		offerService.applyOffersToOrder(offers, order);
		
		qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(1));
		offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
		
		//confirm that at least one of the fg offers was applied
		assertTrue(offerApplied);
		
		fgAdjustmentCount = 0;
		for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
			fgAdjustmentCount += fg.getFulfillmentGroupAdjustments().size();
		}
		//The totalitarian fg offer is now a better deal than the order item offers, therefore the totalitarian fg offer is applied
		//and the order item offers are removed
		assertTrue(fgAdjustmentCount == 2);
		
		int itemAdjustmentCount = 0;
		for (OrderItem item : order.getOrderItems()) {
			itemAdjustmentCount += item.getOrderItemAdjustments().size();
		}
		
		//Confirm that the order item offers are removed
		assertTrue(itemAdjustmentCount == 0);
		verify();
	}
	
	public void testApplyAllFulfillmentGroupOffers() {
		CandidateFulfillmentGroupOfferAnswer answer = new CandidateFulfillmentGroupOfferAnswer();
		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(answer).times(5);
		
		FulfillmentGroupAdjustmentAnswer answer2 = new FulfillmentGroupAdjustmentAnswer();
		EasyMock.expect(offerDaoMock.createFulfillmentGroupAdjustment()).andAnswer(answer2).times(5);
		
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<CandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		
		boolean offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
		
		assertTrue(offerApplied);
		
		order = dataProvider.createBasicOrder();
		qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
		offers.addAll(dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"));
		offers.get(1).setName("secondOffer");
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(1));
		
		offerApplied = fgProcessor.applyAllFulfillmentGroupOffers(qualifiedOffers, order);
		
		//the first offer applies to both fulfillment groups, but the second offer only applies to one of the fulfillment groups
		assertTrue(offerApplied);
		int fgAdjustmentCount = 0;
		for (FulfillmentGroup fg : order.getFulfillmentGroups()) {
			fgAdjustmentCount += fg.getFulfillmentGroupAdjustments().size();
		}
		assertTrue(fgAdjustmentCount == 3);
		
		verify();
	}

	public void testFilterFulfillmentGroupLevelOffer() {
		CandidateFulfillmentGroupOfferAnswer answer = new CandidateFulfillmentGroupOfferAnswer();
		EasyMock.expect(offerDaoMock.createCandidateFulfillmentGroupOffer()).andAnswer(answer).times(3);
		
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<CandidateFulfillmentGroupOffer> qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		
		//test that the valid fg offer is included
		//No item criteria, so each fulfillment group applies
		assertTrue(qualifiedOffers.size() == 2 && qualifiedOffers.get(0).getOffer().equals(offers.get(0)));
		
		qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		
		//test that the valid fg offer is included
		//only 1 fulfillment group has qualifying items
		assertTrue(qualifiedOffers.size() == 1 && qualifiedOffers.get(0).getOffer().equals(offers.get(0))) ;
		 
		qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75240", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"),MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		
		//test that the invalid fg offer is excluded - zipcode is wrong
		assertTrue(qualifiedOffers.size() == 0) ;
		
		qualifiedOffers = new ArrayList<CandidateFulfillmentGroupOffer>();
		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"),MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		fgProcessor.filterFulfillmentGroupLevelOffer(order, qualifiedOffers, order.getDiscountableDiscreteOrderItems(), offers.get(0));
		
		//test that the invalid fg offer is excluded - no qualifying items
		assertTrue(qualifiedOffers.size() == 0) ;
		
		verify();
	}
	
	public void testCouldOfferApplyToFulfillmentGroup() {
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<Offer> offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF);
		boolean couldApply = fgProcessor.couldOfferApplyToFulfillmentGroup(offers.get(0), order.getFulfillmentGroups().get(0));
		//test that the valid fg offer is included
		assertTrue(couldApply);
		
		offers = dataProvider.createFGBasedOffer("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75240", OfferDiscountType.PERCENT_OFF);
		couldApply = fgProcessor.couldOfferApplyToFulfillmentGroup(offers.get(0), order.getFulfillmentGroups().get(0));
		//test that the invalid fg offer is excluded
		assertFalse(couldApply);
		
		verify();
	}
	
	public void testCouldOrderItemMeetOfferRequirement() {
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<Offer> offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		boolean couldApply = fgProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().get(0), order.getOrderItems().get(0));
		//test that the valid fg offer is included
		assertTrue(couldApply);
		
		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		couldApply = fgProcessor.couldOrderItemMeetOfferRequirement(offers.get(0).getQualifyingItemCriteria().get(0), order.getOrderItems().get(0));
		//test that the invalid fg offer is excluded
		assertFalse(couldApply);
		
		verify();
	}
	
	public void testCouldOfferApplyToOrderItems() {
		replay();
		
		Order order = dataProvider.createBasicOrder();
		List<Offer> offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		CandidatePromotionItems candidates = fgProcessor.couldOfferApplyToOrderItems(offers.get(0), order.getDiscountableDiscreteOrderItems());
		//test that the valid fg offer is included
		assertTrue(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
		
		offers = dataProvider.createFGBasedOfferWithItemCriteria("order.subTotal.getAmount()>20", "fulfillmentGroup.address.postalCode==75244", OfferDiscountType.PERCENT_OFF, "([MVEL.eval(\"toUpperCase()\",\"test5\"), MVEL.eval(\"toUpperCase()\",\"test6\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))");
		candidates = fgProcessor.couldOfferApplyToOrderItems(offers.get(0), order.getDiscountableDiscreteOrderItems());
		//test that the invalid fg offer is excluded because there are no qualifying items
		assertFalse(candidates.isMatchedQualifier() && candidates.getCandidateQualifiersMap().size() == 1);
		
		verify();
	}
	
	public class CandidateFulfillmentGroupOfferAnswer implements IAnswer<CandidateFulfillmentGroupOffer> {

		public CandidateFulfillmentGroupOffer answer() throws Throwable {
			return new CandidateFulfillmentGroupOfferImpl();
		}
		
	}
	
	public class FulfillmentGroupAdjustmentAnswer implements IAnswer<FulfillmentGroupAdjustment> {

		public FulfillmentGroupAdjustment answer() throws Throwable {
			return new FulfillmentGroupAdjustmentImpl();
		}
		
	}
	
	public class CandidateItemOfferAnswer implements IAnswer<CandidateItemOffer> {

		public CandidateItemOffer answer() throws Throwable {
			return new CandidateItemOfferImpl();
		}
		
	}
	
	public class OrderItemAdjustmentAnswer implements IAnswer<OrderItemAdjustment> {

		public OrderItemAdjustment answer() throws Throwable {
			return new OrderItemAdjustmentImpl();
		}
		
	}
}
