/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */

package org.broadleafcommerce.common.web.payment.controller;

import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayCheckoutService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfigurationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * <p>Abstract controller that provides convenience methods and resource declarations for Payment Gateway
 * Operations that are shared between all gateway controllers belong here.
 *
 * The Core Framework should have an implementation of a "blPaymentGatewayCheckoutService" bean defined.
 * If you are using the common jars without the framework dependency, you will either have to
 * implement the blPaymentGatewayCheckoutService yourself, or override the applyPaymentToOrder and
 * the markPaymentAsInvalid methods accordingly.</p>
 *
 * @author Elbert Bautista (elbertbautista)
 */
public abstract class PaymentGatewayAbstractController extends BroadleafAbstractController {

    protected static String baseConfirmationView = "ajaxredirect:/confirmation";

    @Autowired(required=false)
    @Qualifier("blPaymentGatewayCheckoutService")
    protected PaymentGatewayCheckoutService paymentGatewayCheckoutService;

    public Long applyPaymentToOrder(PaymentResponseDTO responseDTO) {
        if (paymentGatewayCheckoutService != null) {
            return paymentGatewayCheckoutService.applyPaymentToOrder(responseDTO);
        }
        return null;
    }

    public void markPaymentAsInvalid(Long orderPaymentId) {
        if (paymentGatewayCheckoutService != null) {
            paymentGatewayCheckoutService.markPaymentAsInvalid(orderPaymentId);
        }
    }

    public String initiateCheckout(Long orderId) {
        if (paymentGatewayCheckoutService != null && orderId != null) {
            return paymentGatewayCheckoutService.initiateCheckout(orderId);
        }
        return null;
    }

    public String lookupOrderNumberFromOrderId(PaymentResponseDTO responseDTO) {
        if (paymentGatewayCheckoutService != null) {
            return paymentGatewayCheckoutService.lookupOrderNumberFromOrderId(responseDTO);
        }
        return null;
    }

    // ***********************************************
    // Common Result Processing
    // ***********************************************
    /**
     *
     * translate http request to DTO
     * apply payment to order
     * check success and validity of response
     *
     * try {
     *   if (complete checkout on callback == true)
     *     initiateCheckout(order id);
     *   else
     *     show review page;
     * } catch (Exception e) {
     *     notify admin user of failure
     *     mark payment as invalid
     *     handle processing exception
     * }
     *
     */
    public String process(Model model, HttpServletRequest request) throws Exception {
        PaymentResponseDTO responseDTO = getWebResponseService().translateWebResponse(request);
        Long orderPaymentId = applyPaymentToOrder(responseDTO);

        if (!responseDTO.getSuccessful()) {
            handleUnsuccessfulTransaction(model, responseDTO);
        }

        if (!responseDTO.getValid()) {
            handleProcessingException(new PaymentException("The validity of the response cannot be confirmed." +
                    "Check the Tamper Proof Seal for more details."));
        }

        try {
            String orderId = responseDTO.getOrderId();
            if (orderId == null) {
                throw new RuntimeException("Order ID must be set on the Payment Response DTO");
            }

            if (getConfigurationService().completeCheckoutOnCallback()) {
                String orderNumber = initiateCheckout(Long.parseLong(orderId));
                return getConfirmationView(orderNumber);
            } else {
                //TODO show review page
            }

        } catch (Exception e) {
            markPaymentAsInvalid(orderPaymentId);
            handleProcessingException(e);
        }

        return null;
    }

    /**
     * If the order has been finalized. i.e. all the payments have been applied to the order,
     * then you can go ahead and call checkout using the passed in order id.
     * This is usually called from a Review Page, the security check is pushed to the framework
     * to see if the current request has the permission to do this.
     *
     * @param orderId
     * @return
     * @throws Exception
     */
    public String processCheckoutOrderFinalized(Long orderId) throws Exception {
        try {
            String orderNumber = initiateCheckout(orderId);
            return getConfirmationView(orderNumber);
        } catch (Exception e) {
            handleProcessingException(e);
        }

        return null;
    }

    public abstract void handleProcessingException(Exception e) throws Exception;

    public abstract void handleUnsuccessfulTransaction(Model model, PaymentResponseDTO responseDTO) throws Exception;

    public abstract PaymentGatewayWebResponseService getWebResponseService();

    public abstract PaymentGatewayConfigurationService getConfigurationService();

    public String getBaseConfirmationView() {
        return baseConfirmationView;
    }

    protected String getConfirmationView(String orderNumber) {
        return getBaseConfirmationView() + "/" + orderNumber;
    }

}
