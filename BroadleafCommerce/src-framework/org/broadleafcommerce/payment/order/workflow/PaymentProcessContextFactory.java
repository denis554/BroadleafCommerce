package org.broadleafcommerce.payment.order.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.broadleafcommerce.checkout.workflow.CheckoutSeed;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.order.service.OrderService;
import org.broadleafcommerce.payment.secure.dao.SecurePaymentInfoDao;
import org.broadleafcommerce.payment.secure.domain.BankAccountPaymentInfo;
import org.broadleafcommerce.payment.secure.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.payment.secure.domain.Referenced;
import org.broadleafcommerce.type.PaymentInfoType;
import org.broadleafcommerce.workflow.ProcessContext;
import org.broadleafcommerce.workflow.ProcessContextFactory;
import org.broadleafcommerce.workflow.WorkflowException;

public class PaymentProcessContextFactory implements ProcessContextFactory {

    @Resource
    private SecurePaymentInfoDao securePaymentInfoDao;

    @Resource
    private OrderService orderService;

    private PaymentActionType paymentActionType;

    @Override
    public ProcessContext createContext(Object seedData) throws WorkflowException {
        if(!(seedData instanceof CheckoutSeed)){
            throw new WorkflowException("Seed data instance is incorrect. " +
                    "Required class is "+CheckoutSeed.class.getName()+" " +
                    "but found class: "+seedData.getClass().getName());
        }
        CheckoutSeed checkoutSeed = (CheckoutSeed) seedData;
        Map<PaymentInfo, Referenced> secureMap = checkoutSeed.getInfos();
        if (secureMap == null) {
            secureMap = new HashMap<PaymentInfo, Referenced>();
            List<PaymentInfo> paymentInfoList = orderService.readPaymentInfosForOrder(checkoutSeed.getOrder());
            if (paymentInfoList == null || paymentInfoList.size() == 0) {
                throw new WorkflowException("No payment info instances associated with the order -- id: " + checkoutSeed.getOrder().getId());
            }
            Iterator<PaymentInfo> infos = paymentInfoList.iterator();
            while(infos.hasNext()) {
                PaymentInfo info = infos.next();
                if (info.getType() == PaymentInfoType.CREDIT_CARD) {
                    CreditCardPaymentInfo ccinfo = securePaymentInfoDao.findCreditCardInfo(info.getReferenceNumber());
                    if (ccinfo == null) {
                        throw new WorkflowException("No credit card info associated with credit card payment type with reference number: " + info.getReferenceNumber());
                    }
                    secureMap.put(info, ccinfo);
                } else if (info.getType() == PaymentInfoType.BANK_ACCOUNT) {
                    BankAccountPaymentInfo bankinfo = securePaymentInfoDao.findBankAccountInfo(info.getReferenceNumber());
                    if (bankinfo == null) {
                        throw new WorkflowException("No bank account info associated with bank account payment type with reference number: " + info.getReferenceNumber());
                    }
                    secureMap.put(info, bankinfo);
                } else if (info.getType() == PaymentInfoType.GIFT_CARD) {
                    secureMap.put(info, null);
                } else {
                    throw new WorkflowException("Payment info type ['" + info.getType() +  "'] not recognized with reference number: " + info.getReferenceNumber());
                }
            }
        }
        CombinedPaymentContextSeed combinedSeed = new CombinedPaymentContextSeed(secureMap, paymentActionType);
        PaymentContext response = new PaymentContext();
        response.setSeedData(combinedSeed);

        return response;
    }

    public PaymentActionType getPaymentActionType() {
        return paymentActionType;
    }

    public void setPaymentActionType(PaymentActionType paymentActionType) {
        this.paymentActionType = paymentActionType;
    }

}
