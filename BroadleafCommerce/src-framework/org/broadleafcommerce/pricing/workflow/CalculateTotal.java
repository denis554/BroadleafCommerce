package org.broadleafcommerce.pricing.workflow;

import java.math.BigDecimal;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.util.money.Money;
import org.broadleafcommerce.workflow.BaseActivity;
import org.broadleafcommerce.workflow.ProcessContext;

public class CalculateTotal extends BaseActivity {

    @Override
    public ProcessContext execute(ProcessContext context) throws Exception {
        Order order = ((OfferContext) context).getSeedData();
        Money total = new Money(BigDecimal.ZERO);
        total = total.add(order.getSubTotal());
        total = total.add(order.getTotalTax());
        total = total.add(order.getTotalShipping());
        order.setTotal(total);
        context.setSeedData(order);
        return context;
    }

}
