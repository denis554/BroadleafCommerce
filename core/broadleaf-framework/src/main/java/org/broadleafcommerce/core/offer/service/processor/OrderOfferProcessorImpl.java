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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.offer.dao.OfferDao;
import org.broadleafcommerce.core.offer.domain.CandidateOrderOffer;
import org.broadleafcommerce.core.offer.domain.Offer;
import org.broadleafcommerce.core.offer.domain.OfferRule;
import org.broadleafcommerce.core.offer.domain.OrderAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustment;
import org.broadleafcommerce.core.offer.service.discount.CandidatePromotionItems;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableCandidateOrderOffer;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableFulfillmentGroup;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableItemFactory;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrder;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderAdjustment;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderItem;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderItemAdjustment;
import org.broadleafcommerce.core.offer.service.discount.domain.PromotableOrderItemImpl;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.core.offer.service.type.OfferRuleType;
import org.broadleafcommerce.core.order.dao.FulfillmentGroupItemDao;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupImpl;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.GiftWrapOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.order.domain.OrderMultishipOption;
import org.broadleafcommerce.core.order.service.FulfillmentGroupService;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderMultishipOptionService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.FulfillmentGroupItemRequest;
import org.broadleafcommerce.core.order.service.call.FulfillmentGroupRequest;
import org.broadleafcommerce.core.order.service.exception.RemoveFromCartException;
import org.broadleafcommerce.core.order.service.manipulation.BundleOrderItemSplitContainer;
import org.broadleafcommerce.core.order.service.manipulation.OrderItemSplitContainer;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author jfischer
 */
@Service("blOrderOfferProcessor")
public class OrderOfferProcessorImpl extends AbstractBaseProcessor implements OrderOfferProcessor {

    private static final Log LOG = LogFactory.getLog(OrderOfferProcessorImpl.class);

    @Resource(name = "blOfferDao")
    protected OfferDao offerDao;

    @Resource(name = "blOrderService")
    protected OrderService orderService;

    @Resource(name = "blOrderItemService")
    protected OrderItemService orderItemService;
    
    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

	@Resource(name = "blFulfillmentGroupItemDao")
    protected FulfillmentGroupItemDao fulfillmentGroupItemDao;

    @Resource(name = "blPromotableItemFactory")
    protected PromotableItemFactory promotableItemFactory;

    @Resource(name="blOrderMultishipOptionService")
    protected OrderMultishipOptionService orderMultishipOptionService;

    @Override
    public void filterOrderLevelOffer(PromotableOrder order, List<PromotableCandidateOrderOffer> qualifiedOrderOffers, Offer offer) {
        if (offer.getDiscountType().getType().equals(OfferDiscountType.FIX_PRICE.getType())) {
            LOG.warn("Offers of type ORDER may not have a discount type of FIX_PRICE. Ignoring order offer (name=" + offer.getName() + ")");
            return;
        }
        boolean orderLevelQualification = false;
        //Order Qualification
        orderQualification:
        {
            if (couldOfferApplyToOrder(offer, order)) {
                orderLevelQualification = true;
                break orderQualification;
            }
            for (PromotableOrderItem discreteOrderItem : order.getDiscountableDiscreteOrderItems(offer.getApplyDiscountToSalePrice())) {
                if (couldOfferApplyToOrder(offer, order, discreteOrderItem)) {
                    orderLevelQualification = true;
                    break orderQualification;
                }
            }
            for (PromotableFulfillmentGroup fulfillmentGroup : order.getFulfillmentGroups()) {
                if (couldOfferApplyToOrder(offer, order, fulfillmentGroup)) {
                    orderLevelQualification = true;
                    break orderQualification;
                }
            }
        }
        //Item Qualification - new for 1.5!
        if (orderLevelQualification) {
            CandidatePromotionItems candidates = couldOfferApplyToOrderItems(offer, order.getDiscountableDiscreteOrderItems(offer.getApplyDiscountToSalePrice()));
            if (candidates.isMatchedQualifier()) {
                PromotableCandidateOrderOffer candidateOffer = createCandidateOrderOffer(order, qualifiedOrderOffers, offer);
                candidateOffer.getCandidateQualifiersMap().putAll(candidates.getCandidateQualifiersMap());
            }
        }
    }

    @Override
    public boolean couldOfferApplyToOrder(Offer offer, PromotableOrder order) {
        return couldOfferApplyToOrder(offer, order, null, null);
    }

    /**
     * Private method which executes the appliesToOrderRules in the Offer to determine if this offer
     * can be applied to the Order, OrderItem, or FulfillmentGroup.
     *
     * @param offer
     * @param order
     * @param discreteOrderItem
     * @return true if offer can be applied, otherwise false
     */
    protected boolean couldOfferApplyToOrder(Offer offer, PromotableOrder order, PromotableOrderItem discreteOrderItem) {
        return couldOfferApplyToOrder(offer, order, discreteOrderItem, null);
    }

    /**
     * Private method which executes the appliesToOrderRules in the Offer to determine if this offer
     * can be applied to the Order, OrderItem, or FulfillmentGroup.
     *
     * @param offer
     * @param order
     * @param fulfillmentGroup
     * @return true if offer can be applied, otherwise false
     */
    protected boolean couldOfferApplyToOrder(Offer offer, PromotableOrder order, PromotableFulfillmentGroup fulfillmentGroup) {
        return couldOfferApplyToOrder(offer, order, null, fulfillmentGroup);
    }

    /**
     * Private method which executes the appliesToOrderRules in the Offer to determine if this offer
     * can be applied to the Order, OrderItem, or FulfillmentGroup.
     *
     * @param offer
     * @param order
     * @param discreteOrderItem
     * @param fulfillmentGroup
     * @return true if offer can be applied, otherwise false
     */
    protected boolean couldOfferApplyToOrder(Offer offer, PromotableOrder order, PromotableOrderItem discreteOrderItem, PromotableFulfillmentGroup fulfillmentGroup) {
        boolean appliesToItem = false;
        String rule = null;
        if (offer.getAppliesToOrderRules() != null && offer.getAppliesToOrderRules().trim().length() != 0) {
            rule = offer.getAppliesToOrderRules();
        } else {
            OfferRule orderRule = offer.getOfferMatchRules().get(OfferRuleType.ORDER.getType());
            if (orderRule != null) {
                rule = orderRule.getMatchRule();
            }
        }

        if (rule != null) {

            HashMap<String, Object> vars = new HashMap<String, Object>();
            vars.put("order", order.getDelegate());
            vars.put("offer", offer);
            if (fulfillmentGroup != null) {
                vars.put("fulfillmentGroup", fulfillmentGroup.getDelegate());
            }
            if (discreteOrderItem != null) {
                vars.put("discreteOrderItem", discreteOrderItem.getDelegate());
            }
            Boolean expressionOutcome = executeExpression(rule, vars);
            if (expressionOutcome != null && expressionOutcome) {
                appliesToItem = true;
            }
        } else {
            appliesToItem = true;
        }

        return appliesToItem;
    }

    protected PromotableCandidateOrderOffer createCandidateOrderOffer(PromotableOrder order, List<PromotableCandidateOrderOffer> qualifiedOrderOffers, Offer offer) {
        CandidateOrderOffer candidateOffer = offerDao.createCandidateOrderOffer();
        candidateOffer.setOrder(order.getDelegate());
        candidateOffer.setOffer(offer);
        // Why do we add offers here when we set the sorted list later
        //order.addCandidateOrderOffer(candidateOffer);
        PromotableCandidateOrderOffer promotableCandidateOrderOffer = promotableItemFactory.createPromotableCandidateOrderOffer(candidateOffer, order);
        qualifiedOrderOffers.add(promotableCandidateOrderOffer);

        return promotableCandidateOrderOffer;
    }

    @Override
    public List<PromotableCandidateOrderOffer> removeTrailingNotCombinableOrderOffers(List<PromotableCandidateOrderOffer> candidateOffers) {
        List<PromotableCandidateOrderOffer> remainingCandidateOffers = new ArrayList<PromotableCandidateOrderOffer>();
        int offerCount = 0;
        for (PromotableCandidateOrderOffer candidateOffer : candidateOffers) {
            if (offerCount == 0) {
                remainingCandidateOffers.add(candidateOffer);
            } else {
                boolean treatAsNewFormat = false;
                if (candidateOffer.getOffer().getTreatAsNewFormat() != null && candidateOffer.getOffer().getTreatAsNewFormat()) {
                    treatAsNewFormat = true;
                }
                if ((!treatAsNewFormat && candidateOffer.getOffer().isCombinableWithOtherOffers()) || (treatAsNewFormat && (candidateOffer.getOffer().isTotalitarianOffer() == null || !candidateOffer.getOffer().isTotalitarianOffer()))) {
                    remainingCandidateOffers.add(candidateOffer);
                }
            }
            offerCount++;
        }
        return remainingCandidateOffers;
    }

    @Override
    public boolean applyAllOrderOffers(List<PromotableCandidateOrderOffer> orderOffers, PromotableOrder order) {
        // If order offer is not combinable, first verify order adjustment is zero, if zero, compare item discount total vs this offer's total
        boolean orderOffersApplied = false;
        Iterator<PromotableCandidateOrderOffer> orderOfferIterator = orderOffers.iterator();
        while (orderOfferIterator.hasNext()) {
            PromotableCandidateOrderOffer orderOffer = orderOfferIterator.next();
            if (orderOffer.getOffer().getTreatAsNewFormat() == null || !orderOffer.getOffer().getTreatAsNewFormat()) {
                if ((orderOffer.getOffer().isStackable()) || !order.isHasOrderAdjustments()) {
                    boolean alreadyContainsNotCombinableOfferAtAnyLevel = order.isNotCombinableOfferAppliedAtAnyLevel();
                    applyOrderOffer(order, orderOffer);
                    orderOffersApplied = true;
                    if (!orderOffer.getOffer().isCombinableWithOtherOffers() || alreadyContainsNotCombinableOfferAtAnyLevel) {
                        orderOffersApplied = compareAndAdjustOrderAndItemOffers(order, orderOffersApplied);
                        if (orderOffersApplied) {
                            break;
                        } else {
                            orderOfferIterator.remove();
                        }
                    }
                }
            } else {
                if (!order.containsNotStackableOrderOffer() || !order.isHasOrderAdjustments()) {
                    boolean alreadyContainsTotalitarianOffer = order.isTotalitarianOfferApplied();

                    // TODO:  Add filter for item-subtotal
                    applyOrderOffer(order, orderOffer);
                    orderOffersApplied = true;
                    if (
                            (orderOffer.getOffer().isTotalitarianOffer() != null && orderOffer.getOffer().isTotalitarianOffer()) ||
                                    alreadyContainsTotalitarianOffer
                            ) {
                        orderOffersApplied = compareAndAdjustOrderAndItemOffers(order, orderOffersApplied);
                        if (orderOffersApplied) {
                            break;
                        } else {
                            orderOfferIterator.remove();
                        }
                    } else if (!orderOffer.getOffer().isCombinableWithOtherOffers()) {
                        break;
                    }
                }
            }
        }
        return orderOffersApplied;
    }

    @Override
    public void initializeBundleSplitItems(PromotableOrder order) {
        List<OrderItem> basicOrderItems = order.getDelegate().getOrderItems();
        for (OrderItem basicOrderItem : basicOrderItems) {
            if (basicOrderItem instanceof BundleOrderItem) {
                BundleOrderItem bundleOrderItem = (BundleOrderItem) basicOrderItem;
                List<BundleOrderItem> searchHit = order.searchBundleSplitItems(bundleOrderItem);
                if (searchHit == null) {
                    searchHit = new ArrayList<BundleOrderItem>();
                    BundleOrderItemSplitContainer container = new BundleOrderItemSplitContainer();
                    container.setKey(bundleOrderItem);
                    container.setSplitItems(searchHit);
                    order.getBundleSplitItems().add(container);
                }
                BundleOrderItem temp = (BundleOrderItem) bundleOrderItem.clone();
                for (int x=0;x<temp.getDiscreteOrderItems().size();x++) {
                    temp.getDiscreteOrderItems().get(x).setId(bundleOrderItem.getDiscreteOrderItems().get(x).getId());
                }
                temp.setId(-1L);
                searchHit.add(temp);
            }
        }
    }

    @Override
    public void initializeSplitItems(PromotableOrder order) {
        List<PromotableOrderItem> items = order.getDiscountableDiscreteOrderItems();
        for (PromotableOrderItem item : items) {
            List<PromotableOrderItem> temp = new ArrayList<PromotableOrderItem>();
            temp.add(item);
            OrderItemSplitContainer container = new OrderItemSplitContainer();
            container.setKey(item.getDelegate());
            container.setSplitItems(temp);
            order.getSplitItems().add(container);
        }
    }

    protected boolean compareAndAdjustOrderAndItemOffers(PromotableOrder order, boolean orderOffersApplied) {
        if (order.getAdjustmentPrice().greaterThanOrEqual(order.calculateOrderItemsCurrentPrice())) {
            // item offer is better; remove not combinable order offer and process other order offers
            order.removeAllOrderAdjustments();
            orderOffersApplied = false;
        } else {
            // totalitarian order offer is better; remove all item offers
            order.removeAllItemAdjustments();
            gatherCart(order);
            initializeSplitItems(order);
        }
        return orderOffersApplied;
    }

    @Override
    public void prepareCart(PromotableOrder promotableOrder) {
        try {
            Order order = promotableOrder.getDelegate();
            if (!CollectionUtils.isEmpty(order.getFulfillmentGroups())) {
                List<OrderMultishipOption> options = orderMultishipOptionService.findOrderMultishipOptions(order.getId());
                promotableOrder.setMultiShipOptions(options);
                promotableOrder.setHasMultiShipOptions(!CollectionUtils.isEmpty(options));
                //collapse to a single fg - we'll rebuild later
                orderMultishipOptionService.deleteAllOrderMultishipOptions(order);
                fulfillmentGroupService.collapseToOneFulfillmentGroup(order, false);
            }
        } catch (PricingException e) {
            throw new RuntimeException("Could not prepare the cart", e);
        }
    }

    @Override
    public void gatherCart(PromotableOrder promotableOrder) {
        Order order = promotableOrder.getDelegate();
        try {
            if (!CollectionUtils.isEmpty(order.getFulfillmentGroups())) {
                //stage 1 - gather possible split items - including those inside a bundle order item
                gatherFulfillmentGroupLinkedDiscreteOrderItems(order);
                //stage 2 - gather the bundles themselves
                gatherFulfillmentGroupLinkedBundleOrderItems(order);
            } else {
                //stage 1 - gather possible split items - including those inside a bundle order item
                gatherOrderLinkedDiscreteOrderItems(order);
                //stage 2 - gather the bundles themselves
                gatherOrderLinkedBundleOrderItems(order);
            }

        } catch (PricingException e) {
            throw new RuntimeException("Could not gather the cart", e);
        }
        promotableOrder.resetDiscreteOrderItems();
    }

    protected void gatherOrderLinkedBundleOrderItems(Order order) throws PricingException {
        Map<String, BundleOrderItem> gatherBundle = new HashMap<String, BundleOrderItem>();
        List<BundleOrderItem> bundlesToRemove = new ArrayList<BundleOrderItem>();
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem instanceof BundleOrderItem) {
                String identifier = buildIdentifier(orderItem, null);
                BundleOrderItem retrieved = gatherBundle.get(identifier);
                if (retrieved == null) {
                    gatherBundle.put(identifier, (BundleOrderItem) orderItem);
                    continue;
                }
                retrieved.setQuantity(retrieved.getQuantity() + orderItem.getQuantity());
                bundlesToRemove.add((BundleOrderItem) orderItem);
            }
        }
        for (BundleOrderItem bundleOrderItem : gatherBundle.values()) {
            orderItemService.saveOrderItem(bundleOrderItem);
        }
        for (BundleOrderItem orderItem : bundlesToRemove) {
        	try {
        		orderService.removeItem(order.getId(), orderItem.getId(), false);
        	} catch (RemoveFromCartException e) {
        		throw new PricingException("Item could not be removed", e);
        	}
        }
    }

    protected void gatherOrderLinkedDiscreteOrderItems(Order order) throws PricingException {
        List<DiscreteOrderItem> itemsToRemove = new ArrayList<DiscreteOrderItem>();
        Map<String, OrderItem> gatheredItem = new HashMap<String, OrderItem>();
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem instanceof BundleOrderItem) {
                for (DiscreteOrderItem discreteOrderItem : ((BundleOrderItem) orderItem).getDiscreteOrderItems()) {
                    gatherOrderLinkedDiscreteOrderItem(itemsToRemove, gatheredItem, discreteOrderItem, String.valueOf(orderItem.getId()));
                }
            } else {
                gatherOrderLinkedDiscreteOrderItem(itemsToRemove, gatheredItem, (DiscreteOrderItem) orderItem, null);
            }

        }
        for (OrderItem orderItem : gatheredItem.values()) {
            orderItemService.saveOrderItem(orderItem);
        }
        for (DiscreteOrderItem orderItem : itemsToRemove) {
            if (orderItem.getBundleOrderItem() == null) {
	        	try {
					orderService.removeItem(order.getId(), orderItem.getId(), false);
				} catch (RemoveFromCartException e) {
					throw new PricingException("Could not remove item", e);
				}
            } else {
                BundleOrderItem bundleOrderItem = orderItem.getBundleOrderItem();
                orderMultishipOptionService.deleteOrderItemOrderMultishipOptions(orderItem.getId());
                fulfillmentGroupService.removeOrderItemFromFullfillmentGroups(order, orderItem);
                bundleOrderItem.getDiscreteOrderItems().remove(orderItem);
                orderItem.setBundleOrderItem(null);
                orderItemService.saveOrderItem(bundleOrderItem);
            }
        }
    }

    protected void gatherFulfillmentGroupLinkedBundleOrderItems(Order order) throws PricingException {
        List<BundleOrderItem> bundlesToRemove = new ArrayList<BundleOrderItem>();
        Map<Long, Map<String, Object[]>> gatherBundle = new HashMap<Long, Map<String, Object[]>>();
        for (FulfillmentGroup group : order.getFulfillmentGroups()) {
            Map<String, Object[]> gatheredItem = gatherBundle.get(group.getId());
            if (gatheredItem == null) {
                gatheredItem = new HashMap<String, Object[]>();
                gatherBundle.put(group.getId(), gatheredItem);
            }
            for (FulfillmentGroupItem fgItem : group.getFulfillmentGroupItems()) {
                OrderItem orderItem = fgItem.getOrderItem();
                if (orderItem instanceof BundleOrderItem) {
                    String identifier = buildIdentifier(orderItem, null);
                    Object[] gatheredOrderItem = gatheredItem.get(identifier);
                    if (gatheredOrderItem == null) {
                        gatheredItem.put(identifier, new Object[]{orderItem, fgItem});
                        continue;
                    }
                    ((OrderItem) gatheredOrderItem[0]).setQuantity(((OrderItem) gatheredOrderItem[0]).getQuantity() + orderItem.getQuantity());
                    ((FulfillmentGroupItem) gatheredOrderItem[1]).setQuantity(((FulfillmentGroupItem) gatheredOrderItem[1]).getQuantity() + fgItem.getQuantity());
                    bundlesToRemove.add((BundleOrderItem) orderItem);
                }
            }
        }
        for (Map<String, Object[]> values : gatherBundle.values()) {
            for (Object[] item : values.values()) {
                orderItemService.saveOrderItem((OrderItem) item[0]);
                fulfillmentGroupItemDao.save((FulfillmentGroupItem) item[1]);
            }
        }
        for (BundleOrderItem orderItem : bundlesToRemove) {
        	try {
        		orderService.removeItem(order.getId(), orderItem.getId(), false);
        	} catch (RemoveFromCartException e) {
        		throw new PricingException("Item could not be removed", e);
        	}
        }
    }

    protected void gatherFulfillmentGroupLinkedDiscreteOrderItems(Order order) throws PricingException {
        List<DiscreteOrderItem> itemsToRemove = new ArrayList<DiscreteOrderItem>();
        Map<Long, Map<String, Object[]>> gatherMap = new HashMap<Long, Map<String, Object[]>>();
        for (FulfillmentGroup group : order.getFulfillmentGroups()) {
            Map<String, Object[]> gatheredItem = gatherMap.get(group.getId());
            if (gatheredItem == null) {
                gatheredItem = new HashMap<String, Object[]>();
                gatherMap.put(group.getId(), gatheredItem);
            }
            for (FulfillmentGroupItem fgItem : group.getFulfillmentGroupItems()) {
                OrderItem orderItem = fgItem.getOrderItem();
                if (orderItem instanceof BundleOrderItem) {
                    for (DiscreteOrderItem discreteOrderItem : ((BundleOrderItem) orderItem).getDiscreteOrderItems()) {
                        gatherFulfillmentGroupLinkedDiscreteOrderItem(itemsToRemove, gatheredItem, fgItem, discreteOrderItem, String.valueOf(orderItem.getId()));
                    }
                } else {
                    gatherFulfillmentGroupLinkedDiscreteOrderItem(itemsToRemove, gatheredItem, fgItem, (DiscreteOrderItem) orderItem, null);
                }
            }
        }
        for (Map<String, Object[]> values : gatherMap.values()) {
            for (Object[] item : values.values()) {
                orderItemService.saveOrderItem((OrderItem) item[0]);
                fulfillmentGroupItemDao.save((FulfillmentGroupItem) item[1]);
            }
        }
        for (DiscreteOrderItem orderItem : itemsToRemove) {
            if (orderItem.getBundleOrderItem() == null) {
                try {
                    orderService.removeItem(order.getId(), orderItem.getId(), false);
                } catch (RemoveFromCartException e) {
                    throw new PricingException("Could not remove item", e);
                }
            } else {
                BundleOrderItem bundleOrderItem = orderItem.getBundleOrderItem();
                orderMultishipOptionService.deleteOrderItemOrderMultishipOptions(orderItem.getId());
                fulfillmentGroupService.removeOrderItemFromFullfillmentGroups(order, orderItem);
                bundleOrderItem.getDiscreteOrderItems().remove(orderItem);
                orderItem.setBundleOrderItem(null);
                orderItemService.saveOrderItem(bundleOrderItem);
            }
        }
    }

    protected void gatherOrderLinkedDiscreteOrderItem(List<DiscreteOrderItem> itemsToRemove, Map<String, OrderItem> gatheredItem, DiscreteOrderItem orderItem, String extraIdentifier) {
        if (CollectionUtils.isEmpty(orderItem.getOrderItemAdjustments())) {
            String identifier = buildIdentifier(orderItem, extraIdentifier);

            OrderItem gatheredOrderItem = gatheredItem.get(identifier);
            if (gatheredOrderItem == null) {
                gatheredItem.put(identifier, orderItem);
                return;
            }
            gatheredOrderItem.setQuantity(gatheredOrderItem.getQuantity() + orderItem.getQuantity());
            itemsToRemove.add(orderItem);
        }
    }

    /**
     * Appends the item attributes so that items with different attibutes are not merged together
     * as part of the merge/split logic.
     *
     * @param identifier
     * @param orderItem
     */
    protected void addOptionAttributesToIdentifier(StringBuffer identifier, OrderItem orderItem) {
        if (orderItem.getOrderItemAttributes() != null && orderItem.getOrderItemAttributes().size() > 0) {
            List<String> valueList = new ArrayList<String>();
            for (OrderItemAttribute itemAttribute : orderItem.getOrderItemAttributes().values()) {
                valueList.add(itemAttribute.getName() + "_" + itemAttribute.getValue());
            }
            Collections.sort(valueList);
            identifier.append('_');
            for (String value : valueList) {
                identifier.append(value);
            }
        }
    }

    @Override
    public String buildIdentifier(OrderItem orderItem, String extraIdentifier) {
        StringBuffer identifier = new StringBuffer();
        if (orderItem.getSplitParentItemId() != null || orderService.getAutomaticallyMergeLikeItems()) {
            if (!orderService.getAutomaticallyMergeLikeItems()) {
                identifier.append(orderItem.getSplitParentItemId());
            } else {
                if (orderItem instanceof BundleOrderItem) {
                    BundleOrderItem bundleOrderItem = (BundleOrderItem) orderItem;
                    if (bundleOrderItem.getSku() != null) {
                        identifier.append(bundleOrderItem.getSku().getId());
                    } else {
                        if (orderItem.getSplitParentItemId() != null) {
                            identifier.append(orderItem.getSplitParentItemId());
                        } else {
                            identifier.append(orderItem.getId());
                        }
                    }
                } else if (orderItem instanceof DiscreteOrderItem) {
                    DiscreteOrderItem discreteOrderItem = (DiscreteOrderItem) orderItem;
                    identifier.append(discreteOrderItem.getSku().getId());
                } else {
                    if (orderItem.getSplitParentItemId() != null) {
                        identifier.append(orderItem.getSplitParentItemId());
                    } else {
                        identifier.append(orderItem.getId());
                    }
                }
            }

            identifier.append('_').append(orderItem.getPrice().stringValue());
            if (extraIdentifier != null) {
                identifier.append('_').append(extraIdentifier);
            }

            addOptionAttributesToIdentifier(identifier, orderItem);
        } else {
            identifier.append(orderItem.getId());
        }
        return identifier.toString();
    }

    protected void gatherFulfillmentGroupLinkedDiscreteOrderItem(List<DiscreteOrderItem> itemsToRemove, Map<String, Object[]> gatheredItem, FulfillmentGroupItem fgItem, DiscreteOrderItem orderItem, String extraIdentifier) {
        if (CollectionUtils.isEmpty(orderItem.getOrderItemAdjustments())) {
            String identifier = buildIdentifier(orderItem, extraIdentifier);


            Object[] gatheredOrderItem = gatheredItem.get(identifier);
            if (gatheredOrderItem == null) {
                gatheredItem.put(identifier, new Object[]{orderItem, fgItem});
                return;
            }
            ((OrderItem) gatheredOrderItem[0]).setQuantity(((OrderItem) gatheredOrderItem[0]).getQuantity() + orderItem.getQuantity());
            ((FulfillmentGroupItem) gatheredOrderItem[1]).setQuantity(((FulfillmentGroupItem) gatheredOrderItem[1]).getQuantity() + fgItem.getQuantity());
            itemsToRemove.add(orderItem);
        }
    }

    /**
     * Private method used by applyAllOrderOffers to create an OrderAdjustment from a CandidateOrderOffer
     * and associates the OrderAdjustment to the Order.
     *
     * @param orderOffer a CandidateOrderOffer to apply to an Order
     */
    protected void applyOrderOffer(PromotableOrder order, PromotableCandidateOrderOffer orderOffer) {
        OrderAdjustment orderAdjustment = offerDao.createOrderAdjustment();
        orderAdjustment.init(order.getDelegate(), orderOffer.getOffer(), orderOffer.getOffer().getName());
        PromotableOrderAdjustment promotableOrderAdjustment = promotableItemFactory.createPromotableOrderAdjustment(orderAdjustment, order);
        //add to adjustment
        order.addOrderAdjustments(promotableOrderAdjustment);
    }

    protected void mergeSplitItems(final PromotableOrder order) {
        try {
            mergeSplitDiscreteOrderItems(order);

            mergeSplitBundleOrderItems(order);

            if (order.isHasMultiShipOptions()) {
                List<OrderMultishipOption> multishipOptions =  orderMultishipOptionService.findOrderMultishipOptions(order.getDelegate().getId());
                List<FulfillmentGroupItem> itemsToRemove = new ArrayList<FulfillmentGroupItem>();
                for (OrderMultishipOption option : multishipOptions) {
                    for (FulfillmentGroupItem item : order.getDelegate().getFulfillmentGroups().get(0).getFulfillmentGroupItems()) {
                        if (option.getOrderItem().getId().equals(item.getOrderItem().getId())) {
                            FulfillmentGroupRequest fgr = new FulfillmentGroupRequest();
                            fgr.setOrder(order.getDelegate());
                            if (option.getAddress() != null) {
                                fgr.setAddress(option.getAddress());
                            }
                            if (option.getFulfillmentOption() != null) {
                                fgr.setOption(option.getFulfillmentOption());
                            }
                            FulfillmentGroup fg = fulfillmentGroupService.addFulfillmentGroupToOrder(fgr, false);
                            fg = fulfillmentGroupService.save(fg);
                            order.getDelegate().getFulfillmentGroups().add(fg);

                            FulfillmentGroupItem fulfillmentGroupItem = fulfillmentGroupItemDao.create();
                            fulfillmentGroupItem.setFulfillmentGroup(fg);
                            fulfillmentGroupItem.setOrderItem(option.getOrderItem());
                            fulfillmentGroupItem.setQuantity(1);
                            fulfillmentGroupItem = fulfillmentGroupItemDao.save(fulfillmentGroupItem);
                            fg.getFulfillmentGroupItems().add(fulfillmentGroupItem);

                            if (item.getQuantity() - 1 <= 0) {
                                itemsToRemove.add(item);
                            } else {
                                item.setQuantity(item.getQuantity()-1);
                            }
                        }
                    }
                }

                for (FulfillmentGroupItem item : itemsToRemove) {
                    FulfillmentGroup fg = item.getFulfillmentGroup();
                    fg.getFulfillmentGroupItems().remove(item);
                    item.setFulfillmentGroup(null);
                    if (fg.getFulfillmentGroupItems().size() == 0) {
                        order.getDelegate().getFulfillmentGroups().remove(fg);
                        fg.setOrder(null);
                        fulfillmentGroupService.delete(fg);
                        orderService.save(order.getDelegate(), false);
                    }
                }
            }

            order.resetDiscreteOrderItems();

            for (PromotableOrderItem myItem : order.getDiscountableDiscreteOrderItems()) {
                //reset adjustment retail and sale values, since their transient values are erased after the above persistence events
                if (myItem.isHasOrderItemAdjustments()) {
                    for (OrderItemAdjustment adjustment : myItem.getDelegate().getOrderItemAdjustments()) {
                        PromotableOrderItemAdjustment promotableOrderItemAdjustment = promotableItemFactory.createPromotableOrderItemAdjustment(adjustment, myItem);
                        myItem.resetAdjustmentPrice();
                        promotableOrderItemAdjustment.computeAdjustmentValues();
                        myItem.computeAdjustmentPrice();
                    }
                }
            }


        } catch (PricingException e) {
            throw new RuntimeException("Could not propagate the items split by the promotion engine into the order", e);
        }
    }

    /**
     * Returns null if the item is not part of a bundle.
     * @return
     */
    private Long getBundleId(OrderItem item) {
        if (item instanceof DiscreteOrderItem) {
            DiscreteOrderItem discreteItem =  (DiscreteOrderItem) item;
            if (discreteItem.getBundleOrderItem() != null) {
                return discreteItem.getBundleOrderItem().getId();
            }
        }
        return null;
    }

    protected void mergeSplitDiscreteOrderItems(PromotableOrder order) throws PricingException {
        //If adjustments are removed - merge split items back together before adding to the cart
        List<PromotableOrderItem> itemsToRemove = new ArrayList<PromotableOrderItem>();
        List<DiscreteOrderItem> delegatesToRemove = new ArrayList<DiscreteOrderItem>();
        Iterator<PromotableOrderItem> finalItems = order.getDiscountableDiscreteOrderItems().iterator();
        Map<String, PromotableOrderItem> allItems = new HashMap<String, PromotableOrderItem>();
        while (finalItems.hasNext()) {
            PromotableOrderItem nextItem = finalItems.next();
            List<PromotableOrderItem> mySplits = order.searchSplitItems(nextItem);
            if (!CollectionUtils.isEmpty(mySplits)) {
                PromotableOrderItem cloneItem = nextItem.clone();
                cloneItem.clearAllDiscount();
                cloneItem.clearAllQualifiers();
                cloneItem.removeAllAdjustments();
                cloneItem.setQuantity(0);
                Iterator<PromotableOrderItem> splitItemIterator = mySplits.iterator();
                while (splitItemIterator.hasNext()) {
                    PromotableOrderItem splitItem = splitItemIterator.next();
                    if (!splitItem.isHasOrderItemAdjustments()) {
                        cloneItem.setQuantity(cloneItem.getQuantity() + splitItem.getQuantity());
                        splitItemIterator.remove();
                    }
                }
                if (cloneItem.getQuantity() > 0) {
                    String identifier = String.valueOf(cloneItem.getSku().getId());
                    Long bundleItemId = getBundleId(cloneItem.getDelegate());
                    if (bundleItemId != null) {
                        identifier += bundleItemId;
                    }
                    if (allItems.containsKey(identifier)) {
                        PromotableOrderItem savedItem = allItems.get(identifier);
                        savedItem.setQuantity(savedItem.getQuantity() + cloneItem.getQuantity());
                    } else {
                        allItems.put(identifier, cloneItem);
                        mySplits.add(cloneItem);
                    }
                }

                if (nextItem.getDelegate().getBundleOrderItem() == null) {
                    if (mySplits.contains(nextItem)) {
                        mySplits.remove(nextItem);
                    } else {
                        itemsToRemove.add(nextItem);
                        delegatesToRemove.add(nextItem.getDelegate());
                    }
                } else {
                    itemsToRemove.add(nextItem);
                    delegatesToRemove.add(nextItem.getDelegate());
                }
            }
        }

        for (OrderItemSplitContainer key : order.getSplitItems()) {
            List<PromotableOrderItem> mySplits = key.getSplitItems();
            if (!CollectionUtils.isEmpty(mySplits)) {
                for (PromotableOrderItem myItem : mySplits) {
                    myItem.assignFinalPrice();
                    DiscreteOrderItem delegateItem = myItem.getDelegate();
                    Long delegateItemBundleItemId = getBundleId(delegateItem);
                    if (delegateItemBundleItemId == null) {
                    	delegateItem = (DiscreteOrderItem) addOrderItemToOrder(order.getDelegate(), delegateItem, false);
                        for (int j=0;j<delegateItem.getQuantity();j++){
                            Iterator<OrderMultishipOption> itr = order.getMultiShipOptions().iterator();
                            while(itr.hasNext()) {
                                OrderMultishipOption option = itr.next();
                                if ((option.getOrderItem() instanceof DiscreteOrderItem) && ((DiscreteOrderItem) option.getOrderItem()).getSku().equals(delegateItem.getSku())) {
                                    option.setOrderItem(delegateItem);
                                    orderMultishipOptionService.save(option);
                                    itr.remove();
                                    break;
                                }
                            }
                        }
                        FulfillmentGroupItem fgItem = fulfillmentGroupItemDao.create();
                        fgItem.setQuantity(delegateItem.getQuantity());
                        fgItem.setOrderItem(delegateItem);
                        fgItem.setFulfillmentGroup(order.getDelegate().getFulfillmentGroups().get(0));
                        order.getDelegate().getFulfillmentGroups().get(0).getFulfillmentGroupItems().add(fgItem);
                    }
                    myItem.setDelegate(delegateItem);
                }
            }
        }

        //compile a list of any gift wrap items that we're keeping
        List<GiftWrapOrderItem> giftWrapItems = new ArrayList<GiftWrapOrderItem>();
        for (DiscreteOrderItem discreteOrderItem : order.getDelegate().getDiscreteOrderItems()) {
            if (discreteOrderItem instanceof GiftWrapOrderItem) {
                if (!delegatesToRemove.contains(discreteOrderItem)) {
                    giftWrapItems.add((GiftWrapOrderItem) discreteOrderItem);
                } else {
                    Iterator<OrderItem> wrappedItems = ((GiftWrapOrderItem) discreteOrderItem).getWrappedItems().iterator();
                    while (wrappedItems.hasNext()) {
                        OrderItem wrappedItem = wrappedItems.next();
                        wrappedItem.setGiftWrapOrderItem(null);
                        wrappedItems.remove();
                    }
                }
            }
        }

        for (PromotableOrderItem itemToRemove : itemsToRemove) {
            DiscreteOrderItem delegateItem = itemToRemove.getDelegate();

            mergeSplitGiftWrapOrderItems(order, giftWrapItems, itemToRemove, delegateItem);

            if (delegateItem.getBundleOrderItem() == null) {
	        	try {
	        		orderService.removeItem(order.getDelegate().getId(), delegateItem.getId(), false);
				} catch (RemoveFromCartException e) {
					throw new PricingException("Could not remove item", e);
				}
            }
        }
    }
    
    protected OrderItem addOrderItemToOrder(Order order, OrderItem newOrderItem, Boolean priceOrder) throws PricingException {
        List<OrderItem> orderItems = order.getOrderItems();
        newOrderItem.setOrder(order);
        newOrderItem = orderItemService.saveOrderItem(newOrderItem);
        orderItems.add(newOrderItem);
        orderService.save(order, priceOrder);
        return newOrderItem;
    }

    protected void mergeSplitGiftWrapOrderItems(PromotableOrder order, List<GiftWrapOrderItem> giftWrapItems, PromotableOrderItem itemToRemove, DiscreteOrderItem delegateItem) {
        for (GiftWrapOrderItem giftWrapOrderItem : giftWrapItems) {
            List<OrderItem> newItems = new ArrayList<OrderItem>();
            Iterator<OrderItem> wrappedItems = giftWrapOrderItem.getWrappedItems().iterator();
            boolean foundItems = false;
            while (wrappedItems.hasNext()) {
                OrderItem wrappedItem = wrappedItems.next();
                if (wrappedItem.equals(delegateItem)) {
                    foundItems = true;
                    //add in the new wrapped items (split or not)
                    List<PromotableOrderItem> searchHits = order.searchSplitItems(itemToRemove);
                    if (!CollectionUtils.isEmpty(searchHits)) {
                        for (PromotableOrderItem searchHit : searchHits) {
                            newItems.add(searchHit.getDelegate());
                            searchHit.getDelegate().setGiftWrapOrderItem(giftWrapOrderItem);
                        }
                    }
                    //eradicate the old wrapped items
                    delegateItem.setGiftWrapOrderItem(null);
                    wrappedItems.remove();
                }
            }
            if (foundItems) {
                giftWrapOrderItem.getWrappedItems().addAll(newItems);
                orderItemService.saveOrderItem(giftWrapOrderItem);
            }
        }
    }

    protected void mergeSplitBundleOrderItems(PromotableOrder order) throws PricingException {
        List<BundleOrderItemSplitContainer> bundleContainers = order.getBundleSplitItems();
        for (BundleOrderItemSplitContainer bundleContainer : bundleContainers) {
            for (BundleOrderItemSplitContainer bundleOrderItemSplitContainer : bundleContainers) {
                BundleOrderItem val = bundleOrderItemSplitContainer.getSplitItems().get(0);
                val.setId(null);
                List<DiscreteOrderItem> itemsToAdd = new ArrayList<DiscreteOrderItem>();
                for (DiscreteOrderItem discreteOrderItem : bundleOrderItemSplitContainer.getKey().getDiscreteOrderItems()) {
                    PromotableOrderItem poi = new PromotableOrderItemImpl(discreteOrderItem, null, null);
                    List<PromotableOrderItem> items = order.searchSplitItems(poi);
                    for (PromotableOrderItem temp : items) {
                        DiscreteOrderItem delegate = temp.getDelegate();
                        delegate.setId(null);
                        delegate.setBundleOrderItem(val);
                        itemsToAdd.add(delegate);
                    }
                }
                val.getDiscreteOrderItems().clear();
                val.getDiscreteOrderItems().addAll(itemsToAdd);

                try {
                    orderService.removeItem(order.getDelegate().getId(), bundleContainer.getKey().getId(), false);
                } catch (RemoveFromCartException e) {
                    throw new PricingException("Could not remove item", e);
                }

                if (CollectionUtils.isEmpty(order.getDelegate().getFulfillmentGroups())) {
                    FulfillmentGroup fg = fulfillmentGroupService.createEmptyFulfillmentGroup();
                    fg.setOrder(order.getDelegate());
                    order.getDelegate().getFulfillmentGroups().add(fg);
                }

                val = (BundleOrderItem) addOrderItemToOrder(order.getDelegate(), val, false);

                for (DiscreteOrderItem discreteOrderItem : val.getDiscreteOrderItems()) {
                    for (int j=0;j<discreteOrderItem.getQuantity();j++){
                        Iterator<OrderMultishipOption> itr = order.getMultiShipOptions().iterator();
                        while(itr.hasNext()) {
                            OrderMultishipOption option = itr.next();
                            if ((option.getOrderItem() instanceof DiscreteOrderItem) && ((DiscreteOrderItem) option.getOrderItem()).getSku().equals(discreteOrderItem.getSku())) {
                                option.setOrderItem(discreteOrderItem);
                                orderMultishipOptionService.save(option);
                                itr.remove();
                                break;
                            }
                        }
                    }
                    FulfillmentGroupItem fgItem = fulfillmentGroupItemDao.create();
                    fgItem.setQuantity(discreteOrderItem.getQuantity());
                    fgItem.setOrderItem(discreteOrderItem);
                    fgItem.setFulfillmentGroup(order.getDelegate().getFulfillmentGroups().get(0));
                    order.getDelegate().getFulfillmentGroups().get(0).getFulfillmentGroupItems().add(fgItem);
                }

                for (int j=0;j<val.getQuantity();j++){
                    Iterator<OrderMultishipOption> itr = order.getMultiShipOptions().iterator();
                    while(itr.hasNext()) {
                        OrderMultishipOption option = itr.next();
                        if (buildIdentifier(option.getOrderItem(), null).equals(buildIdentifier(val, null))) {
                            option.setOrderItem(val);
                            orderMultishipOptionService.save(option);
                            itr.remove();
                            break;
                        }
                    }
                }
            }
        }

        orderService.save(order.getDelegate(), false);
    }

    @Override
    public void compileOrderTotal(PromotableOrder order) {
        order.assignOrderItemsFinalPrice();
        order.setSubTotal(order.calculateOrderItemsFinalPrice(true));
    }

    @Override
    public OfferDao getOfferDao() {
        return offerDao;
    }

    @Override
    public void setOfferDao(OfferDao offerDao) {
        this.offerDao = offerDao;
    }

    @Override
    public OrderService getOrderService() {
        return orderService;
    }

    @Override
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public OrderItemService getOrderItemService() {
        return orderItemService;
    }

    @Override
    public void setOrderItemService(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Override
    public FulfillmentGroupItemDao getFulfillmentGroupItemDao() {
        return fulfillmentGroupItemDao;
    }

    @Override
    public void setFulfillmentGroupItemDao(
            FulfillmentGroupItemDao fulfillmentGroupItemDao) {
        this.fulfillmentGroupItemDao = fulfillmentGroupItemDao;
    }

    @Override
    public PromotableItemFactory getPromotableItemFactory() {
        return promotableItemFactory;
    }

    @Override
    public void setPromotableItemFactory(PromotableItemFactory promotableItemFactory) {
        this.promotableItemFactory = promotableItemFactory;
    }

    @Override
    public FulfillmentGroupService getFulfillmentGroupService() {
		return fulfillmentGroupService;
	}

	@Override
    public void setFulfillmentGroupService(FulfillmentGroupService fulfillmentGroupService) {
		this.fulfillmentGroupService = fulfillmentGroupService;
	}

    @Override
    public OrderMultishipOptionService getOrderMultishipOptionService() {
        return orderMultishipOptionService;
    }

    @Override
    public void setOrderMultishipOptionService(OrderMultishipOptionService orderMultishipOptionService) {
        this.orderMultishipOptionService = orderMultishipOptionService;
    }
}
