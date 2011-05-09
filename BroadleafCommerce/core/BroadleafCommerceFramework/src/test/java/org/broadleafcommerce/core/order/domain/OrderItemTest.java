package org.broadleafcommerce.core.order.domain;

import junit.framework.TestCase;

import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferImpl;
import org.broadleafcommerce.core.offer.service.OfferDataItemProvider;
import org.broadleafcommerce.core.offer.service.discount.PromotionDiscount;
import org.broadleafcommerce.core.offer.service.discount.PromotionQualifier;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.core.offer.service.type.OfferItemRestrictionRuleType;
import org.broadleafcommerce.core.order.service.type.OrderItemType;
import org.broadleafcommerce.money.Money;

public class OrderItemTest extends TestCase {

	private DiscreteOrderItem orderItem1;
	private Offer offer;
	
	@Override
	protected void setUp() throws Exception {
		orderItem1 = new DiscreteOrderItemImpl();
		orderItem1.setName("test1");
		orderItem1.setOrderItemType(OrderItemType.DISCRETE);
		orderItem1.setQuantity(2);
		orderItem1.setRetailPrice(new Money(19.99D));
		orderItem1.setPrice(new Money(19.99D));
		
		OfferDataItemProvider dataProvider = new OfferDataItemProvider();
		
		offer = dataProvider.createItemBasedOfferWithItemCriteria(
			"order.subTotal.getAmount()>20", 
			OfferDiscountType.PERCENT_OFF, 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))", 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"
		).get(0);
	}

	public void testGetQuantityAvailableToBeUsedAsQualifier() throws Exception {
		int quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//no previous qualifiers, so all quantity is available
		assertTrue(quantity == 2);
		
		PromotionDiscount discount = new PromotionDiscount();
		discount.setPromotion(offer);
		discount.setQuantity(1);
		orderItem1.getPromotionDiscounts().add(discount);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//items that have already received this promotion cannot get it again
		assertTrue(quantity==1);
		
		Offer testOffer = new OfferImpl();
		testOffer.setOfferItemQualifierRuleType(OfferItemRestrictionRuleType.NONE);
		testOffer.setOfferItemTargetRuleType(OfferItemRestrictionRuleType.NONE);
		
		discount.setPromotion(testOffer);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//this item received a different promotion, but the restriction rule is NONE, so this item cannot be a qualifier for this promotion
		assertTrue(quantity==1);
		
		testOffer.setOfferItemTargetRuleType(OfferItemRestrictionRuleType.QUALIFIER);
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//this item received a different promotion, but the restriction rule is QUALIFIER, so this item can be a qualifier for this promotion
		assertTrue(quantity==2);
		
		orderItem1.getPromotionDiscounts().clear();
		
		PromotionQualifier qualifier = new PromotionQualifier();
		qualifier.setPromotion(offer);
		qualifier.setQuantity(1);
		orderItem1.getPromotionQualifiers().add(qualifier);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//items that have already qualified for this promotion cannot qualify again
		assertTrue(quantity==1);
		
		qualifier.setPromotion(testOffer);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//this item qualified for a different promotion, but the restriction rule is NONE, so this item cannot be a qualifier for this promotion
		assertTrue(quantity==1);
		
		testOffer.setOfferItemQualifierRuleType(OfferItemRestrictionRuleType.QUALIFIER);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsQualifier(offer);
		//this item qualified for a different promotion, but the restriction rule is QUALIFIER, so this item can be a qualifier for this promotion
		assertTrue(quantity==2);
	}
	
	public void testGetQuantityAvailableToBeUsedAsTarget() throws Exception {
		int quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//no previous qualifiers, so all quantity is available
		assertTrue(quantity == 2);
		
		PromotionDiscount discount = new PromotionDiscount();
		discount.setPromotion(offer);
		discount.setQuantity(1);
		orderItem1.getPromotionDiscounts().add(discount);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//items that have already received this promotion cannot get it again
		assertTrue(quantity==1);
		
		Offer tempOffer = new OfferImpl();
		tempOffer.setStackable(true);
		tempOffer.setOfferItemQualifierRuleType(OfferItemRestrictionRuleType.NONE);
		tempOffer.setOfferItemTargetRuleType(OfferItemRestrictionRuleType.NONE);
		
		discount.setPromotion(tempOffer);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//this item received a different promotion, but the restriction rule is NONE, so this item cannot be a qualifier for this promotion
		assertTrue(quantity==1);
		
		tempOffer.setOfferItemTargetRuleType(OfferItemRestrictionRuleType.TARGET);
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//this item received a different promotion, but the restriction rule is QUALIFIER, so this item can be a qualifier for this promotion
		assertTrue(quantity==2);
		
		orderItem1.getPromotionDiscounts().clear();
		
		PromotionQualifier qualifier = new PromotionQualifier();
		qualifier.setPromotion(offer);
		qualifier.setQuantity(1);
		orderItem1.getPromotionQualifiers().add(qualifier);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//items that have already qualified for this promotion cannot qualify again
		assertTrue(quantity==1);
		
		qualifier.setPromotion(tempOffer);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//this item qualified for a different promotion, but the restriction rule is NONE, so this item cannot be a qualifier for this promotion
		assertTrue(quantity==1);
		
		tempOffer.setOfferItemQualifierRuleType(OfferItemRestrictionRuleType.TARGET);
		
		quantity = orderItem1.getQuantityAvailableToBeUsedAsTarget(offer);
		//this item qualified for a different promotion, but the restriction rule is QUALIFIER, so this item can be a qualifier for this promotion
		assertTrue(quantity==2);
	}
}
