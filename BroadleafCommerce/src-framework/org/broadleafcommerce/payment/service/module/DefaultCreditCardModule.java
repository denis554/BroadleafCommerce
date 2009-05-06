package org.broadleafcommerce.payment.service.module;

import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.payment.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.exception.PaymentProcessorException;

public class DefaultCreditCardModule implements CreditCardModule {

    public static final String MODULENAME = "defaultCreditCardModule";

    protected String name = MODULENAME;

    @Override
    public PaymentResponse authorize(PaymentInfo paymentInfo, CreditCardPaymentInfo creditCardPaymentInfo) throws PaymentException, PaymentProcessorException {
        throw new PaymentException("authorize not implemented.");
    }

    @Override
    public PaymentResponse authorizeAndDebit(PaymentInfo paymentInfo, CreditCardPaymentInfo creditCardPaymentInfo) throws PaymentException, PaymentProcessorException {
        throw new PaymentException("authorizeAndDebit not implemented.");
    }

    @Override
    public PaymentResponse debit(PaymentInfo paymentInfo, CreditCardPaymentInfo creditCardPaymentInfo) throws PaymentException, PaymentProcessorException {
        throw new PaymentException("debit not implemented.");
    }

    @Override
    public PaymentResponse credit(PaymentInfo paymentInfo, CreditCardPaymentInfo creditCardPaymentInfo) throws PaymentException, PaymentProcessorException {
        throw new PaymentException("credit not implemented.");
    }

    @Override
    public PaymentResponse voidPayment(PaymentInfo paymentInfo, CreditCardPaymentInfo creditCardPaymentInfo) throws PaymentException, PaymentProcessorException {
        throw new PaymentException("voidPayment not implemented.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
