/*
 * Copyright 2012 the original author or authors.
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

package org.broadleafcommerce.core.web.controller;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * An abstract controller that provides convenience methods and resource declarations for its  children
 * 
 * Operations that are shared between all controllers belong here.
 * 
 * @author apazzolini
 */
public abstract class BroadleafAbstractController {
	
    @Resource(name="blCartService")
    protected CartService cartService;
    
    @Resource(name="blCustomerState")
    protected CustomerState customerState;
    
    /**
     * This will ensure that the cart for the current customer (or a shared, empty cart if the customer has
     * not yet added anything) is on the model for every controller that extends this controller.
     * 
     * @param request
     * @return The cart (potentially a shared, "null" cart) for the current customer
     */
    @ModelAttribute(value = "cart")
    protected Order getCart(HttpServletRequest request) {
    	Customer customer = customerState.getCustomer(request);
    	Order cart = cartService.findCartForCustomer(customer);
    	
    	// FIXME: This should create a "NULL" cart instead
    	if (cart == null) { 
    		cart = cartService.getNullOrder();
    	}
    	
    	return cart;
    }
    
	/**
	 * A helper method that takes care of concatenating the current request context
	 * to the desired path to forward the user to
	 * 
	 * @param request
	 * @param response
	 * @param path the desired non-context-specific path to redirect the user to
	 * @throws IOException 
	 */
	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
		response.sendRedirect(request.getContextPath() + path);
	}
	
	/**
	 * A helper method that returns whether or not the given request was invoked via an AJAX call
	 * 
	 * @param request
	 * @return - whether or not it was an AJAX request
	 */
    protected boolean isAjaxRequest(HttpServletRequest request) {
    	return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
    
    /**
     * <p>A helper method that will correctly handle either returning the partial content requested (to be displayed
     * in a modal or only update a certain part of a page for example) or render it in its full view. It will 
     * determine which rendering method to use based on whether or not it was an AJAX request.</p>
     * 
     * <p>This method will assume that given the String identifier of a nonModalPath (such as "cart"), the partial
     * view that would be returned lives in "ajax/cart".</p>
     * 
     * 
     * @param nonModalPath - the path to the full content view
     * @param request
     * @param model
     * @return the String that should be returned by the method responsible for returning a view. Typically this
     * will be the method with the @RequestMapping annotation
     */
    protected String ajaxRender(String nonModalPath, HttpServletRequest request, Model model) {
    	return isAjaxRequest(request) ? "ajax/" + nonModalPath : nonModalPath;
    }

}
