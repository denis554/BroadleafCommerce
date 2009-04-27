package org.broadleafcommerce.pricing.service.workflow;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.pricing.service.module.TaxModule;
import org.broadleafcommerce.workflow.BaseActivity;
import org.broadleafcommerce.workflow.ProcessContext;

public class TaxActivity extends BaseActivity {

    private TaxModule taxModule;

    @Override
    public ProcessContext execute(ProcessContext context) throws Exception {
        Order order = ((PricingContext)context).getSeedData();
        order = taxModule.calculateTaxForOrder(order);

        context.setSeedData(order);
        return context;
    }

    public TaxModule getTaxModule() {
        return taxModule;
    }

    public void setTaxModule(TaxModule taxModule) {
        this.taxModule = taxModule;
    }

}
