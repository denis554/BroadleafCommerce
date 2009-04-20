package org.broadleafcommerce.checkout.service;

import java.util.Map;

import javax.annotation.Resource;

import org.broadleafcommerce.checkout.exception.CheckoutException;
import org.broadleafcommerce.checkout.workflow.CheckoutSeed;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.payment.secure.domain.Referenced;
import org.broadleafcommerce.workflow.SequenceProcessor;
import org.broadleafcommerce.workflow.WorkflowException;
import org.springframework.stereotype.Service;

@Service("checkoutService")
public class CheckoutServiceImpl implements CheckoutService {

    @Resource(name="checkoutWorkflow")
    private SequenceProcessor checkoutWorkflow;

    /* (non-Javadoc)
     * @see org.broadleafcommerce.checkout.service.CheckoutService#performCheckout(org.broadleafcommerce.order.domain.Order, java.util.Map)
     */
    @Override
    public void performCheckout(Order order, Map<PaymentInfo, Referenced> payments) throws CheckoutException {
        /*
         * TODO add validation that checks the order and payment information for validity.
         */
        try {
            CheckoutSeed seed = new CheckoutSeed(order, payments);
            checkoutWorkflow.doActivities(seed);
        } catch (WorkflowException e) {
            throw new CheckoutException("Unable to checkout order -- id: " + order.getId(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.checkout.service.CheckoutService#performCheckout(org.broadleafcommerce.order.domain.Order)
     */
    @Override
    public void performCheckout(Order order) throws CheckoutException {
        performCheckout(order, null);
    }

}
