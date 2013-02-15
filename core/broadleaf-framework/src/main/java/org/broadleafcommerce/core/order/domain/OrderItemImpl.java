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

package org.broadleafcommerce.core.order.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.currency.util.BroadleafCurrencyUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverride;
import org.broadleafcommerce.common.presentation.override.AdminPresentationOverrides;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.offer.domain.CandidateItemOffer;
import org.broadleafcommerce.core.offer.domain.CandidateItemOfferImpl;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustment;
import org.broadleafcommerce.core.offer.domain.OrderItemAdjustmentImpl;
import org.broadleafcommerce.core.order.service.type.OrderItemType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_ORDER_ITEM")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
@AdminPresentationOverrides(
    {
        @AdminPresentationOverride(name="giftWrapOrderItem", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="skuBundleItem", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.defaultCategory", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.name", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.description", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.longDescription", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.activeStartDate", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.activeEndDate", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.sku", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="product.defaultSku", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="sku.name", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="sku.salePrice", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="sku.retailPrice", value=@AdminPresentation(excluded = true)),
        @AdminPresentationOverride(name="bundleOrderItem", value=@AdminPresentation(excluded = true))
    }
)
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "OrderItemImpl_baseOrderItem")
public class OrderItemImpl implements OrderItem, Cloneable {

    private static final Log LOG = LogFactory.getLog(OrderItemImpl.class);
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "OrderItemId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "OrderItemId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "OrderItemImpl", allocationSize = 150)
    @Column(name = "ORDER_ITEM_ID")
    @AdminPresentation(friendlyName = "OrderItemImpl_Order_Item_ID", group = "OrderItemImpl_Primary_Key", visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @ManyToOne(targetEntity = CategoryImpl.class)
    @JoinColumn(name = "CATEGORY_ID")
    @Index(name="ORDERITEM_CATEGORY_INDEX", columnNames={"CATEGORY_ID"})
    @NotFound(action = NotFoundAction.IGNORE)
    @AdminPresentation(excluded = true)
    protected Category category;

    @ManyToOne(targetEntity = OrderImpl.class)
    @JoinColumn(name = "ORDER_ID")
    @Index(name="ORDERITEM_ORDER_INDEX", columnNames={"ORDER_ID"})
    @AdminPresentation(excluded = true, visibility = VisibilityEnum.HIDDEN_ALL)
    protected Order order;

    @Column(name = "PRICE", precision = 19, scale = 5)
    @AdminPresentation(friendlyName = "OrderItemImpl_Item_Price", order = 1, group = "OrderItemImpl_Pricing", fieldType = SupportedFieldType.MONEY)
    protected BigDecimal price;

    @Column(name = "QUANTITY", nullable = false)
    @AdminPresentation(friendlyName = "OrderItemImpl_Item_Quantity", order = 2, group = "OrderItemImpl_Pricing")
    protected int quantity;

    @Column(name = "RETAIL_PRICE", precision=19, scale=5)
    @AdminPresentation(friendlyName = "OrderItemImpl_Item_Retail_Price", order = 3, group = "OrderItemImpl_Pricing", fieldType = SupportedFieldType.MONEY)
    protected BigDecimal retailPrice;

    @Column(name = "SALE_PRICE", precision=19, scale=5)
    @AdminPresentation(friendlyName = "OrderItemImpl_Item_Sale_Price", order = 4, group = "OrderItemImpl_Pricing", fieldType = SupportedFieldType.MONEY)
    protected BigDecimal salePrice;


    
    @Column(name = "NAME")
    @AdminPresentation(friendlyName = "OrderItemImpl_Item_Name", order=1, group = "OrderItemImpl_Description", prominent=true, groupOrder = 1)
    protected String name;

    @ManyToOne(targetEntity = PersonalMessageImpl.class, cascade = { CascadeType.ALL })
    @JoinColumn(name = "PERSONAL_MESSAGE_ID")
    @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    @Index(name="ORDERITEM_MESSAGE_INDEX", columnNames={"PERSONAL_MESSAGE_ID"})
    protected PersonalMessage personalMessage;

    @ManyToOne(targetEntity = GiftWrapOrderItemImpl.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "GIFT_WRAP_ITEM_ID", nullable = true)
    @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    @Index(name="ORDERITEM_GIFT_INDEX", columnNames={"GIFT_WRAP_ITEM_ID"})
    @AdminPresentation(excluded = true)
    protected GiftWrapOrderItem giftWrapOrderItem;

    @OneToMany(mappedBy = "orderItem", targetEntity = OrderItemAdjustmentImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "blOrderElements")
    protected List<OrderItemAdjustment> orderItemAdjustments = new ArrayList<OrderItemAdjustment>();

    @OneToMany(mappedBy = "orderItem", targetEntity = CandidateItemOfferImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "blOrderElements")
    protected List<CandidateItemOffer> candidateItemOffers = new ArrayList<CandidateItemOffer>();

    @OneToMany(mappedBy = "orderItem", targetEntity = OrderItemPriceDetailImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<OrderItemPriceDetail> orderItemPriceDetails = new ArrayList<OrderItemPriceDetail>();
    
    @Column(name = "ORDER_ITEM_TYPE")
    @Index(name="ORDERITEM_TYPE_INDEX", columnNames={"ORDER_ITEM_TYPE"})
    @AdminPresentation(excluded = true)
    protected String orderItemType;

    @Column(name = "ITEM_TAXABLE_FLAG")
    @AdminPresentation(excluded = true)
    protected Boolean itemTaxable;

    @Column(name = "DISCOUNTS_ALLOWED")
    @AdminPresentation(excluded = true)
    protected Boolean discountsAllowed;

    @OneToMany(mappedBy = "orderItem", targetEntity = OrderItemAttributeImpl.class, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    @MapKey(name="name")
    protected Map<String,OrderItemAttribute> orderItemAttributeMap = new HashMap<String, OrderItemAttribute>();

    @Column(name = "TOTAL_TAX")
    protected BigDecimal totalTax;

    @Column(name = "TAXABLE_PRORATED_ORDER_ADJ")
    protected BigDecimal taxableProratedOrderAdjustment;

    @Column(name = "PRORATED_ORDER_ADJ")
    protected BigDecimal proratedOrderAdjustment;

    @Column(name = "PRORATED_FULFILLMENT_CHARGE")
    protected BigDecimal proratedFulfillmentCharge;       
    
    @Override
    public Money getRetailPrice() {
        return convertToMoney(retailPrice);
    }

    @Override
    public void setRetailPrice(Money retailPrice) {
        this.retailPrice = Money.toAmount(retailPrice);
    }

    @Override
    public Money getSalePrice() {
        return convertToMoney(salePrice);
    }

    @Override
    public void setSalePrice(Money salePrice) {
        this.salePrice = Money.toAmount(salePrice);
    }

    @Override
    public Money getPrice() {
        return convertToMoney(price);
    }

    @Override
    public void setPrice(Money finalPrice) {
        this.price = Money.toAmount(finalPrice);
    }

    @Override
    public Money getTaxablePrice() {
        Money taxablePrice = BroadleafCurrencyUtils.getMoney(BigDecimal.ZERO, getOrder().getCurrency());
        if (isTaxable() == null || isTaxable()) {
            taxablePrice = getAveragePrice();
        }
        return taxablePrice;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public List<CandidateItemOffer> getCandidateItemOffers() {
        return candidateItemOffers;
    }

    @Override
    public void setCandidateItemOffers(List<CandidateItemOffer> candidateItemOffers) {
        this.candidateItemOffers = candidateItemOffers;
    }

    @Override
    public PersonalMessage getPersonalMessage() {
        return personalMessage;
    }

    @Override
    public void setPersonalMessage(PersonalMessage personalMessage) {
        this.personalMessage = personalMessage;
    }

    @Override
    public Order getOrder() {
        return order;
    }

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isInCategory(String categoryName) {
        Category currentCategory = category;
        if (currentCategory != null) {
            if (currentCategory.getName().equals(categoryName)) {
                return true;
            }
            while ((currentCategory = currentCategory.getDefaultParentCategory()) != null) {
                if (currentCategory.getName().equals(categoryName)) {
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public List<OrderItemAdjustment> getOrderItemAdjustments() {
        return this.orderItemAdjustments;
    }

    @Override
    public void setOrderItemAdjustments(List<OrderItemAdjustment> orderItemAdjustments) {       
        this.orderItemAdjustments = orderItemAdjustments;
    }

    @Override
    public Money getAdjustmentValue() {
        return getAverageAdjustmentValue();
    }

    @Override
    public GiftWrapOrderItem getGiftWrapOrderItem() {
        return giftWrapOrderItem;
    }

    @Override
    public void setGiftWrapOrderItem(GiftWrapOrderItem giftWrapOrderItem) {
        this.giftWrapOrderItem = giftWrapOrderItem;
    }

    @Override
    public OrderItemType getOrderItemType() {
        return convertOrderItemType(orderItemType);
    }

    @Override
    public void setOrderItemType(OrderItemType orderItemType) {
        this.orderItemType = orderItemType.getType();
    }

    @Override
    public boolean getIsOnSale() {
        if (getSalePrice() != null) {
            return !getSalePrice().equals(getRetailPrice());
        } else {
            return false;
        }
    }

    @Override
    public boolean getIsDiscounted() {
        if (getPrice() != null) {
            return !getPrice().equals(getRetailPrice());
        } else {
            return false;
        }
    }

    @Override
    public boolean updateSaleAndRetailBasePrices() {
        return false;
    }
    
    @Override
    public void assignFinalPrice() {
        setPrice(getCurrentPrice());
    }
    
    @Override
    public Money getCurrentPrice() {
        updateSaleAndRetailBasePrices();
        Money currentPrice = null;
        if (getPrice() != null) {
            currentPrice = getPrice();
        } else if (getSalePrice() != null) {
            currentPrice = getSalePrice();
        } else {
            currentPrice = getRetailPrice();
        }
        return currentPrice;
    }
    
    @Override
    public Money getPriceBeforeAdjustments(boolean allowSalesPrice) {
        updateSaleAndRetailBasePrices();

        if (getSalePrice() != null && allowSalesPrice) {
            return getSalePrice();
        } else {
            return getRetailPrice();
        }
    }
    
    @Override
    public void addCandidateItemOffer(CandidateItemOffer candidateItemOffer) {
        getCandidateItemOffers().add(candidateItemOffer);
    }
    
    @Override
    public void removeAllCandidateItemOffers() {
        if (getCandidateItemOffers() != null) {
            for (CandidateItemOffer candidate : getCandidateItemOffers()) {
                candidate.setOrderItem(null);
            }
            getCandidateItemOffers().clear();
        }
    }
    
    @Override
    public int removeAllAdjustments() {
        int removedAdjustmentCount = 0;
        if (getOrderItemAdjustments() != null) {
            for (OrderItemAdjustment adjustment : getOrderItemAdjustments()) {
                adjustment.setOrderItem(null);
            }
            removedAdjustmentCount = getOrderItemAdjustments().size();
            getOrderItemAdjustments().clear();
        }
        assignFinalPrice();
        return removedAdjustmentCount;
    }
    
    /**
     * A list of arbitrary attributes added to this item.
     */
    @Override
    public Map<String,OrderItemAttribute> getOrderItemAttributes() {
        return orderItemAttributeMap;
    }

    /**
     * Sets the map of order item attributes.
     *
     * @param orderItemAttributes
     */
    @Override
    public void setOrderItemAttributes(Map<String,OrderItemAttribute> orderItemAttributes) {
        this.orderItemAttributeMap = orderItemAttributes;
    }

    
    public void checkCloneable(OrderItem orderItem) throws CloneNotSupportedException, SecurityException, NoSuchMethodException {
        Method cloneMethod = orderItem.getClass().getMethod("clone", new Class[]{});
        if (cloneMethod.getDeclaringClass().getName().startsWith("org.broadleafcommerce") && !orderItem.getClass().getName().startsWith("org.broadleafcommerce")) {
            //subclass is not implementing the clone method
            throw new CloneNotSupportedException("Custom extensions and implementations should implement clone in order to guarantee split and merge operations are performed accurately");
        }
    }

    protected Money convertToMoney(BigDecimal amount) {
        return amount == null ? null : BroadleafCurrencyUtils.getMoney(amount, getOrder().getCurrency());
    }

    protected OrderItemType convertOrderItemType(String type) {
        return OrderItemType.getInstance(type);
    }
    
    @Override
    public OrderItem clone() {
        //this is likely an extended class - instantiate from the fully qualified name via reflection
        OrderItemImpl clonedOrderItem;
        try {
            clonedOrderItem = (OrderItemImpl) Class.forName(this.getClass().getName()).newInstance();
            try {
                checkCloneable(clonedOrderItem);
            } catch (CloneNotSupportedException e) {
                LOG.warn("Clone implementation missing in inheritance hierarchy outside of Broadleaf: " + clonedOrderItem.getClass().getName(), e);
            }
            if (candidateItemOffers != null) {
                for (CandidateItemOffer candidate : candidateItemOffers) {
                    CandidateItemOffer clone = candidate.clone();
                    clone.setOrderItem(clonedOrderItem);
                    clonedOrderItem.getCandidateItemOffers().add(clone);
                }
            }
            
            if (orderItemAttributeMap != null && !orderItemAttributeMap.isEmpty()) {
                for (OrderItemAttribute attribute : orderItemAttributeMap.values()) {
                    OrderItemAttribute clone = attribute.clone();
                    clone.setOrderItem(clonedOrderItem);
                    clonedOrderItem.getOrderItemAttributes().put(clone.getName(), clone);
                }
            }
            
            clonedOrderItem.setCategory(category);
            clonedOrderItem.setGiftWrapOrderItem(giftWrapOrderItem);
            clonedOrderItem.setName(name);
            clonedOrderItem.setOrder(order);
            clonedOrderItem.setOrderItemType(convertOrderItemType(orderItemType));
            clonedOrderItem.setPersonalMessage(personalMessage);
            clonedOrderItem.setQuantity(quantity);
            clonedOrderItem.retailPrice = retailPrice;
            clonedOrderItem.salePrice = salePrice;
            clonedOrderItem.discountsAllowed = discountsAllowed;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return clonedOrderItem;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((giftWrapOrderItem == null) ? 0 : giftWrapOrderItem.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        result = prime * result + ((orderItemType == null) ? 0 : orderItemType.hashCode());
        result = prime * result + ((personalMessage == null) ? 0 : personalMessage.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + quantity;
        result = prime * result + ((retailPrice == null) ? 0 : retailPrice.hashCode());
        result = prime * result + ((salePrice == null) ? 0 : salePrice.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrderItemImpl other = (OrderItemImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (category == null) {
            if (other.category != null) {
                return false;
            }
        } else if (!category.equals(other.category)) {
            return false;
        }
        if (giftWrapOrderItem == null) {
            if (other.giftWrapOrderItem != null) {
                return false;
            }
        } else if (!giftWrapOrderItem.equals(other.giftWrapOrderItem)) {
            return false;
        }
        if (order == null) {
            if (other.order != null) {
                return false;
            }
        } else if (!order.equals(other.order)) {
            return false;
        }
        if (orderItemType == null) {
            if (other.orderItemType != null) {
                return false;
            }
        } else if (!orderItemType.equals(other.orderItemType)) {
            return false;
        }
        if (personalMessage == null) {
            if (other.personalMessage != null) {
                return false;
            }
        } else if (!personalMessage.equals(other.personalMessage)) {
            return false;
        }
        if (price == null) {
            if (other.price != null) {
                return false;
            }
        } else if (!price.equals(other.price)) {
            return false;
        }
        if (quantity != other.quantity) {
            return false;
        }
        if (retailPrice == null) {
            if (other.retailPrice != null) {
                return false;
            }
        } else if (!retailPrice.equals(other.retailPrice)) {
            return false;
        }
        if (salePrice == null) {
            if (other.salePrice != null) {
                return false;
            }
        } else if (!salePrice.equals(other.salePrice)) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean isTaxable() {
        return itemTaxable == null ? true : itemTaxable;
    }

    @Override
    public void setTaxable(Boolean taxable) {
        this.itemTaxable = taxable;
    }

    @Override
    public List<OrderItemPriceDetail> getOrderItemPriceDetails() {
        return orderItemPriceDetails;
    }

    @Override
    public void setOrderItemPriceDetails(List<OrderItemPriceDetail> orderItemPriceDetails) {
        this.orderItemPriceDetails = orderItemPriceDetails;
    }

    @Override
    public boolean isDiscountingAllowed() {
        if (discountsAllowed == null) {
            return true;
        } else {
            return discountsAllowed.booleanValue();
        }
    }

    @Override
    public void setDiscountingAllowed(boolean discountsAllowed) {
        this.discountsAllowed = discountsAllowed;
    }

    @Override
    public Money getAveragePrice() {
        return getTotalPrice().divide(quantity);
    }

    @Override
    public Money getAverageAdjustmentValue() {
        return getTotalAdjustmentValue().divide(quantity);
    }

    @Override
    public Money getTotalAdjustmentValue() {
        Money totalAdjustmentValue = BroadleafCurrencyUtils.getMoney(order.getCurrency());
        List<OrderItemPriceDetail> priceDetails = getOrderItemPriceDetails();
        if (priceDetails != null) {
            for (OrderItemPriceDetail priceDetail : getOrderItemPriceDetails()) {
                totalAdjustmentValue = totalAdjustmentValue.add(priceDetail.getTotalAdjustmentValue());
            }
        }

        return totalAdjustmentValue;
    }
    
    @Override
    public Money getTotalPrice() {
        Money returnValue = convertToMoney(BigDecimal.ZERO);
        if (orderItemPriceDetails != null) {
            for (OrderItemPriceDetail oipd : orderItemPriceDetails) {
                returnValue = returnValue.add(oipd.getTotalAdjustedPrice());
            }
        } else {
            // Support for legacy orders
            returnValue = getPrice();
        }
        
        return returnValue;
    }    

    @Override
    public Money getTotalTaxableAmount() {
        Money returnValue = convertToMoney(BigDecimal.ZERO);
        if (isTaxable()) {
            return getTotalPrice();
        }
        return returnValue;
    }

    @Override
    public Money getTotalTax() {
        return convertToMoney(totalTax);
    }

    @Override
    public void setTotalTax(Money totalTax) {
        this.totalTax = Money.toAmount(totalTax);
    }

    @Override
    public Money getTaxableProratedOrderAdjustment() {
        return convertToMoney(taxableProratedOrderAdjustment);
    }

    @Override
    public void setTaxableProratedOrderAdjustment(Money taxableProratedOrderAdjustment) {
        this.taxableProratedOrderAdjustment = Money.toAmount(taxableProratedOrderAdjustment);
    }

    @Override
    public Money getProratedOrderAdjustment() {
        return convertToMoney(proratedOrderAdjustment);
    }

    @Override
    public void setProratedOrderAdjustment(Money proratedOrderAdjustment) {
        this.proratedOrderAdjustment = Money.toAmount(proratedOrderAdjustment);
    }

    @Override
    public Money getProratedFulfillmentCharges() {
        return convertToMoney(proratedFulfillmentCharge);
    }

    @Override
    public void setProratedFulfillmentCharges(Money proratedFulfillmentCharge) {
        this.proratedFulfillmentCharge = Money.toAmount(proratedFulfillmentCharge);
    }
}
