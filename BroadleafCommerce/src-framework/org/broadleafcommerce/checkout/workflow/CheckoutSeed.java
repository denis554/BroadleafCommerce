package org.broadleafcommerce.checkout.workflow;

import java.util.Map;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.payment.secure.domain.Referenced;

public class CheckoutSeed {

    private Map<PaymentInfo, Referenced> infos;
    private Order order;

    public CheckoutSeed(Order order, Map<PaymentInfo, Referenced> infos) {
        this.order = order;
        this.infos = infos;
    }

    public Map<PaymentInfo, Referenced> getInfos() {
        return infos;
    }

    public Order getOrder() {
        return order;
    }

}
