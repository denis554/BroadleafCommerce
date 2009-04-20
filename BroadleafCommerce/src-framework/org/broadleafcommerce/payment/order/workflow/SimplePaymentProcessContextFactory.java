package org.broadleafcommerce.payment.order.workflow;

import org.broadleafcommerce.workflow.ProcessContext;
import org.broadleafcommerce.workflow.ProcessContextFactory;
import org.broadleafcommerce.workflow.WorkflowException;

public class SimplePaymentProcessContextFactory implements ProcessContextFactory {

    @Override
    public ProcessContext createContext(Object seedData) throws WorkflowException {
        if(!(seedData instanceof PaymentSeed)){
            throw new WorkflowException("Seed data instance is incorrect. " +
                    "Required class is "+PaymentSeed.class.getName()+" " +
                    "but found class: "+seedData.getClass().getName());
        }

        PaymentContext response = new PaymentContext();
        response.setSeedData(seedData);

        return response;
    }

}
