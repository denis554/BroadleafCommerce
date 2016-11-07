/*
 * #%L
 * BroadleafCommerce Framework
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
package org.broadleafcommerce.core.order.service.workflow.add;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.currency.domain.BroadleafCurrency;
import org.broadleafcommerce.common.extension.ExtensionResultHolder;
import org.broadleafcommerce.common.extension.ExtensionResultStatusType;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductOption;
import org.broadleafcommerce.core.catalog.domain.ProductOptionValue;
import org.broadleafcommerce.core.catalog.domain.ProductOptionXref;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.domain.SkuProductOptionValueXref;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.ProductOptionValidationService;
import org.broadleafcommerce.core.order.service.call.ConfigurableOrderItemRequest;
import org.broadleafcommerce.core.order.service.call.NonDiscreteOrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.exception.RequiredAttributeNotProvidedException;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.order.service.workflow.add.extension.ValidateAddRequestActivityExtensionManager;
import org.broadleafcommerce.core.workflow.ActivityMessages;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

public class ValidateAddRequestActivity extends BaseActivity<ProcessContext<CartOperationRequest>> {

    @Value("${solr.index.use.sku}")
    protected boolean useSku;
    
    @Resource(name = "blOrderService")
    protected OrderService orderService;
    
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Resource(name = "blProductOptionValidationService")
    protected ProductOptionValidationService productOptionValidationService;
    
    @Resource(name = "blOrderItemService")
    protected OrderItemService orderItemService;

    @Resource(name = "blValidateAddRequestActivityExtensionManager")
    protected ValidateAddRequestActivityExtensionManager extensionManager;

    @Override
    public ProcessContext<CartOperationRequest> execute(ProcessContext<CartOperationRequest> context) throws Exception {
        ExtensionResultHolder<Exception> resultHolder = new ExtensionResultHolder<>();
        resultHolder.setResult(null);
        ExtensionResultStatusType result = extensionManager.getProxy().validate(context, resultHolder);

        if (!ExtensionResultStatusType.NOT_HANDLED.equals(result)) {
            if (resultHolder.getResult() != null) {
                throw resultHolder.getResult();
            }
        }

        return validate(context);
    }

    protected ProcessContext<CartOperationRequest> validate(ProcessContext<CartOperationRequest> context) {
        CartOperationRequest request = context.getSeedData();
        OrderItemRequestDTO orderItemRequestDTO = request.getItemRequest();
        Integer orderItemQuantity = orderItemRequestDTO.getQuantity();

        if (!hasQuantity(orderItemQuantity)) {
            context.stopProcess();
        } else if (orderItemQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        } else if (request.getOrder() == null) {
            throw new IllegalArgumentException("Order is required when adding item to order");
        } else {
            Product product = determineProduct(orderItemRequestDTO);
            Sku sku;
            try {
                sku = determineSku(product, orderItemRequestDTO.getSkuId(), orderItemRequestDTO.getItemAttributes(),
                    (ActivityMessages) context);
            } catch (RequiredAttributeNotProvidedException e) {
                if (orderItemRequestDTO instanceof ConfigurableOrderItemRequest) {
                    // Mark the request as a configuration error and proceed with the add.
                    orderItemRequestDTO.setHasConfigurationError(Boolean.TRUE);
                    return context;
                }
                throw e;
            }

            addSkuToCart(sku, orderItemRequestDTO, product, request);

            if (!hasSameCurrency(orderItemRequestDTO, request, sku)) {
                throw new IllegalArgumentException("Cannot have items with differing currencies in one cart");
            }

            validateIfParentOrderItemExists(orderItemRequestDTO);
        }
        
        return context;
    }

    protected boolean hasQuantity(Integer orderItemQuantity) {
        return orderItemQuantity != null && orderItemQuantity != 0;
    }

    protected Product determineProduct(OrderItemRequestDTO orderItemRequestDTO) {
        Product product = null;
        // Validate that if the user specified a productId, it is a legitimate productId
        if (orderItemRequestDTO.getProductId() != null) {
            product = catalogService.findProductById(orderItemRequestDTO.getProductId());

            if (product == null) {
                throw new IllegalArgumentException("Product was specified but no matching product was found for productId "
                                                   + orderItemRequestDTO.getProductId());
            }
        }

        return product;
    }
    
    protected Sku determineSku(Product product, Long skuId, Map<String, String> attributeValues, ActivityMessages messages) throws RequiredAttributeNotProvidedException {
        Sku sku = null;
        
        //If sku browsing is enabled, product option data will not be available.
        if(!useSku) {
            // Check whether the sku is correct given the product options.
            sku = findMatchingSku(product, attributeValues, messages);
        }

        if (sku == null && skuId != null) {
            sku = catalogService.findSkuById(skuId);
        }

        if (sku == null && product != null) {
            // Set to the default sku
            if (cannotSellDefaultSku(product)) {
                throw new RequiredAttributeNotProvidedException("Unable to find non-default sku matching given options and cannot sell default sku", null);
            } else {
                sku = product.getDefaultSku();
            }
        }
        return sku;
    }

    protected boolean cannotSellDefaultSku(Product product) {
        return CollectionUtils.isNotEmpty(product.getAdditionalSkus()) && !product.getCanSellWithoutOptions();
    }
    
    protected Sku findMatchingSku(Product product, Map<String, String> attributeValues, ActivityMessages messages) throws RequiredAttributeNotProvidedException {
        Map<String, String> attributeValuesForSku = new HashMap<>();
        Sku matchingSku = null;

        // Verify that required product-option values were set.
        if (product != null) {
            for (ProductOptionXref productOptionXref : ListUtils.emptyIfNull(product.getProductOptionXrefs())) {
                ProductOption productOption = productOptionXref.getProductOption();
                String attributeName = productOption.getAttributeName();
                String attributeValue = attributeValues.get(attributeName);
                boolean isRequired = productOption.getRequired();
                boolean hasStrategy = productOptionValidationService.hasProductOptionValidationStrategy(productOption);
                boolean isAddOrNoneType = productOptionValidationService.isAddOrNoneType(productOption);

                if (isRequired && isAddOrNoneType) {
                    putAttributeValueForSku(attributeValuesForSku, productOption, attributeName, attributeValue, product.getId());
                }

                if (shouldValidateWithException(isRequired, isAddOrNoneType, attributeValue)) {
                    productOptionValidationService.validate(productOption, attributeValue);
                }

                if (hasStrategy && !isAddOrNoneType) {
                    // we need to validate; however, we will not error out
                    productOptionValidationService.validateWithoutException(productOption, attributeValue, messages);
                }
            }

            matchingSku = getMatchingSku(product, attributeValuesForSku);
        }

        return matchingSku;
    }

    protected void putAttributeValueForSku(Map<String, String> attributeValuesForSku, ProductOption productOption,
                                           String attributeName, String attributeValue, Long productId) {
        if (StringUtils.isEmpty(attributeValue)) {
            String message = "Unable to add to product (" + productId + ") cart. Required attribute was not provided: "
                             + attributeName;
            throw new RequiredAttributeNotProvidedException(message, attributeName, String.valueOf(productId));
        } else if (productOption.getUseInSkuGeneration()) {
            attributeValuesForSku.put(attributeName, attributeValue);
        }
    }

    protected boolean shouldValidateWithException(boolean isRequired, boolean isAddOrNoneType, String attributeValue) {
        return isAddOrNoneType && (isRequired || !StringUtils.isEmpty(attributeValue));
    }

    protected Sku getMatchingSku(Product product, Map<String, String> attributeValuesForSku) {
        Sku matchingSku = null;

        for (Sku sku : ListUtils.emptyIfNull(product.getAdditionalSkus())) {
            if (isMatchingSku(sku, attributeValuesForSku)) {
                matchingSku = sku;
                break;
            }
        }

        return matchingSku;
    }

    protected boolean isMatchingSku(Sku sku, Map<String,String> attributeValues) {
        boolean hasMatchingSku = true;

        if (MapUtils.isEmpty(attributeValues)) {
            hasMatchingSku = false;
        } else {
            for (String attributeName : attributeValues.keySet()) {
                boolean optionValueMatchFound = false;

                for (SkuProductOptionValueXref productOptionValueXref : sku.getProductOptionValueXrefs()) {
                    ProductOptionValue productOptionValue = productOptionValueXref.getProductOptionValue();
                    boolean isSameAttribute = productOptionValue.getProductOption().getAttributeName().equals(attributeName);

                    if (isSameAttribute) {
                        optionValueMatchFound = productOptionValue.getAttributeValue().equals(attributeValues.get(attributeName));

                        if (optionValueMatchFound) {
                            break;
                        } else {
                            hasMatchingSku = false;
                        }
                    }
                }

                if (!optionValueMatchFound) {
                    hasMatchingSku = false;
                }
            }
        }

        return hasMatchingSku;
    }

    protected void addSkuToCart(Sku sku, OrderItemRequestDTO orderItemRequestDTO, Product product, CartOperationRequest request) {
        // If we couldn't find a sku, then we're unable to add to cart.
        if (!hasSkuOrIsNonDiscreteOI(sku, orderItemRequestDTO)) {
            handleIfNoSku(orderItemRequestDTO, product);
        } else if (sku == null) {
            handleIfNonDiscreteOI(orderItemRequestDTO);
        } else if (!sku.isActive()) {
            throw new IllegalArgumentException("The requested skuId of " + sku.getId() + " is no longer active");
        } else {
            // We know which sku we're going to add, so we can add it
            request.getItemRequest().setSkuId(sku.getId());
        }
    }

    protected boolean hasSkuOrIsNonDiscreteOI(Sku sku, OrderItemRequestDTO orderItemRequestDTO) {
        return sku != null || orderItemRequestDTO instanceof NonDiscreteOrderItemRequestDTO;
    }

    protected void handleIfNoSku(OrderItemRequestDTO orderItemRequestDTO, Product product) {
        StringBuilder sb = new StringBuilder();

        for (Entry<String, String> entry : orderItemRequestDTO.getItemAttributes().entrySet()) {
            sb.append(entry.toString());
        }

        throw new IllegalArgumentException("Could not find SKU for :" +
                                           " productId: " + (product == null ? "null" : product.getId()) +
                                           " skuId: " + orderItemRequestDTO.getSkuId() +
                                           " attributes: " + sb.toString());
    }

    protected void handleIfNonDiscreteOI(OrderItemRequestDTO orderItemRequestDTO) {
        NonDiscreteOrderItemRequestDTO ndr = (NonDiscreteOrderItemRequestDTO) orderItemRequestDTO;

        if (StringUtils.isBlank(ndr.getItemName())) {
            throw new IllegalArgumentException("Item name is required for non discrete order item add requests");
        } else if (!hasPrice(ndr)) {
            throw new IllegalArgumentException("At least one override price is required for non discrete order item add requests");
        }
    }

    protected boolean hasPrice(NonDiscreteOrderItemRequestDTO ndr) {
        return ndr.getOverrideRetailPrice() != null || ndr.getOverrideSalePrice() != null;
    }

    protected boolean hasSameCurrency(OrderItemRequestDTO orderItemRequestDTO, CartOperationRequest request, Sku sku) {
        boolean isNDR = orderItemRequestDTO instanceof NonDiscreteOrderItemRequestDTO;
        boolean hasSameCurrency = false;

        BroadleafCurrency orderCurrency = request.getOrder().getCurrency();
        BroadleafCurrency skuCurrency = sku.getCurrency();
        boolean hasOrderCurrency = request.getOrder().getCurrency() != null;
        boolean hasSkuCurrency = sku.getCurrency() != null;

        if (hasOrderCurrency && hasSkuCurrency) {
            hasSameCurrency = orderCurrency.equals(skuCurrency);
        }

        return isNDR || !hasOrderCurrency || !hasSkuCurrency || hasSameCurrency;
    }

    protected void validateIfParentOrderItemExists(OrderItemRequestDTO orderItemRequestDTO) {
        // If the user has specified a parent order item to attach this to, it must exist in this cart
        if (orderItemRequestDTO.getParentOrderItemId() != null) {
            OrderItem parent = orderItemService.readOrderItemById(orderItemRequestDTO.getParentOrderItemId());
            if (parent == null) {
                throw new IllegalArgumentException("Could not find parent order item by the given id");
            }
        }
    }
}
