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

package org.broadleafcommerce.core.web.api.endpoint.order;

import org.broadleafcommerce.core.offer.domain.OfferCode;
import org.broadleafcommerce.core.offer.service.OfferService;
import org.broadleafcommerce.core.offer.service.exception.OfferMaxUseExceededException;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.OrderItemRequestDTO;
import org.broadleafcommerce.core.order.service.exception.AddToCartException;
import org.broadleafcommerce.core.order.service.exception.ItemNotFoundException;
import org.broadleafcommerce.core.order.service.exception.RemoveFromCartException;
import org.broadleafcommerce.core.order.service.exception.UpdateCartException;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.api.endpoint.BaseEndpoint;
import org.broadleafcommerce.core.web.api.endpoint.catalog.CatalogEndpoint;
import org.broadleafcommerce.core.web.api.wrapper.OrderWrapper;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.web.core.CustomerState;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * This endpoint depends on JAX-RS to provide cart services.  It should be extended by components that actually wish 
 * to provide an endpoint.  The annotations such as @Path, @Scope, @Context, @PathParam, @QueryParam, 
 * @GET, @POST, @PUT, and @DELETE are purposely not provided here to allow implementors finer control over 
 * the details of the endpoint.
 *
 * <p/>
 * User: Kelly Tisdell
 * Date: 4/10/12
 */
public abstract class CartEndpoint extends BaseEndpoint {

    @Resource(name="blOrderService")
    protected OrderService orderService;

    @Resource(name="blOfferService")
    protected OfferService offerService;

    @Resource(name="blCustomerService")
    protected CustomerService customerService;

   /**
     * Search for {@code Order} by {@code Customer}
     *
     * @return the cart for the customer
     */
    public OrderWrapper findCartForCustomer(HttpServletRequest request) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);

            if (cart != null) {
                OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                wrapper.wrap(cart, request);

                return wrapper;
            }

            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

   /**
     * Create a new {@code Order} for {@code Customer}
     *
     * @return the cart for the customer
     */
    public OrderWrapper createNewCartForCustomer(HttpServletRequest request) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer == null) {
            customer = customerService.createCustomerFromId(null);
        }

        Order cart = orderService.findCartForCustomer(customer);
        if (cart == null) {
            cart = orderService.createNewCartForCustomer(customer);
        }

        OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
        wrapper.wrap(cart, request);

        return wrapper;
    }

    /**
     * This method takes in a categoryId and productId as path parameters.  In addition, query parameters can be supplied including:
     * 
     * <li>skuId</li>
     * <li>quantity</li>
     * <li>priceOrder</li>
     * 
     * You must provide a skuId OR product options. Product options can be posted as form or querystring parameters. 
     * You must pass in the ProductOption attributeName as the key and the 
     * ProductOptionValue attributeValue as the value.  See {@link CatalogEndpoint}.
     * 
     * @param request
     * @param uriInfo
     * @param categoryId
     * @param productId
     * @param skuId
     * @param quantity
     * @param priceOrder
     * @return OrderWrapper
     */
    public OrderWrapper addSkuToOrder(HttpServletRequest request,
            UriInfo uriInfo,
            Long categoryId,
            Long productId,
            Long skuId,
            int quantity,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);
        
        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                    //We allow product options to be submitted via form post or via query params.  We need to take 
                    //the product options and build a map with them...
                    MultivaluedMap<String, String> multiValuedMap = uriInfo.getQueryParameters();
                    HashMap<String, String> productOptions = new HashMap<String, String>();
                    
                    //Fill up a map of key values that will represent product options
                    Set<String> keySet = multiValuedMap.keySet();
                    for (String key : keySet) {
                        if (multiValuedMap.getFirst(key) != null) {
                            productOptions.put(key, multiValuedMap.getFirst(key));
                        }
                    }
                    
                    //Remove the items from the map that represent the other query params of the request
                    //Essentially these can't be used as product option names...
                    productOptions.remove("categoryId");
                    productOptions.remove("productId");
                    productOptions.remove("skuId");
                    productOptions.remove("quantity");
                    productOptions.remove("priceOrder");
                    
                    //If a Sku ID was not supplied and no product options were supplied, then we can't process this request
                    if (skuId == null && productOptions.size() == 0) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .type(MediaType.TEXT_PLAIN).entity("You must submit a Sku Id, or a map of product option / values.").build());
                    }
                    
                    OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
                    orderItemRequestDTO.setCategoryId(categoryId);
                    orderItemRequestDTO.setProductId(productId);
                    orderItemRequestDTO.setSkuId(skuId);
                    orderItemRequestDTO.setCategoryId(categoryId);
                    orderItemRequestDTO.setQuantity(quantity);
                    
                    //If we have product options set them on the DTO
                    if (productOptions.size() > 0) {
                        orderItemRequestDTO.setItemAttributes(productOptions);
                    }
                    
                    Order order = orderService.addItem(cart.getId(), orderItemRequestDTO, priceOrder);
                    order = orderService.save(order, priceOrder);
                    
                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(order, request);

                    return wrapper;
                } catch (PricingException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the order.").build());
                } catch (AddToCartException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured adding the item to the cart.").build());
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

    public OrderWrapper removeItemFromOrder(HttpServletRequest request,
            Long itemId,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                    Order order = orderService.removeItem(cart.getId(), itemId, priceOrder);
                    order = orderService.save(order, priceOrder);
                    
                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(order, request);

                    return wrapper;
                } catch (PricingException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the cart.").build());
                } catch (RemoveFromCartException e) {
                    if (e.getCause() instanceof ItemNotFoundException) {
                        throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND)
                                .type(MediaType.TEXT_PLAIN).entity("Could not find order item id " + itemId).build());
                    } else { 
                        throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .type(MediaType.TEXT_PLAIN).entity("An error occured removing the item to the cart.").build());
                    }
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

    public OrderWrapper updateItemQuantity(HttpServletRequest request,
            Long itemId,
            Integer quantity,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                    OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
                    orderItemRequestDTO.setOrderItemId(itemId);
                    orderItemRequestDTO.setQuantity(quantity);
                    Order order = orderService.updateItemQuantity(cart.getId(), orderItemRequestDTO, priceOrder);
                    order = orderService.save(order, priceOrder);

                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(order, request);

                    return wrapper;
                } catch (UpdateCartException e) {
                    if (e.getCause() instanceof ItemNotFoundException) {
                        throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND)
                                .type(MediaType.TEXT_PLAIN).entity("Could not find order item id " + itemId).build());
                    } else { 
                        throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .type(MediaType.TEXT_PLAIN).entity("An error occured removing the item to the cart.").build());
                    }
                } catch (RemoveFromCartException e) {
                    if (e.getCause() instanceof ItemNotFoundException) {
                        throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND)
                                .type(MediaType.TEXT_PLAIN).entity("Could not find order item id " + itemId).build());
                    } else { 
                        throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .type(MediaType.TEXT_PLAIN).entity("An error occured removing the item to the cart.").build());
                    }
                } catch (PricingException pe) {
                    throw new WebApplicationException(pe, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the cart.").build());
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

    public OrderWrapper addOfferCode(HttpServletRequest request,
            String promoCode,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            OfferCode offerCode = offerService.lookupOfferCodeByCode(promoCode);
            if (cart != null && offerCode != null) {
                try {
                    cart = orderService.addOfferCode(cart, offerCode, priceOrder);
                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(cart, request);

                    return wrapper;
                } catch (PricingException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the cart.").build());
                } catch (OfferMaxUseExceededException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.BAD_REQUEST)
                            .type(MediaType.TEXT_PLAIN).entity("The offer (promo) code provided has exceeded its max usages: " + promoCode).build());
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

    public OrderWrapper removeOfferCode(HttpServletRequest request,
            String promoCode,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            OfferCode offerCode = offerService.lookupOfferCodeByCode(promoCode);
            if (offerCode == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.TEXT_PLAIN).entity("Offer code was invalid or could not be found.").build());
            }

            if (cart != null) {
                try {
                    cart = orderService.removeOfferCode(cart, offerCode, priceOrder);
                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(cart, request);

                    return wrapper;
                } catch (PricingException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the cart.").build());
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

    public OrderWrapper removeAllOfferCodes(HttpServletRequest request,
            boolean priceOrder) {
        Customer customer = CustomerState.getCustomer(request);

        if (customer != null) {
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null) {
                try {
                    cart = orderService.removeAllOfferCodes(cart, priceOrder);
                    OrderWrapper wrapper = (OrderWrapper) context.getBean(OrderWrapper.class.getName());
                    wrapper.wrap(cart, request);

                    return wrapper;
                } catch (PricingException e) {
                    throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN).entity("An error occured pricing the cart.").build());
                }
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Cart could not be found").build());
        }

        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN).entity("Could not find customer associated with request. " +
                        "Ensure that customer ID is passed in the request as header or request parameter : customerId").build());
    }

}
