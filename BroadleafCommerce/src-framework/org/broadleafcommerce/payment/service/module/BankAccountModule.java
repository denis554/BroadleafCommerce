package org.broadleafcommerce.payment.service.module;

import org.broadleafcommerce.order.domain.PaymentInfo;
import org.broadleafcommerce.payment.domain.BankAccountPaymentInfo;
import org.broadleafcommerce.payment.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.exception.PaymentProcessorException;

public interface BankAccountModule {

    public String getName();

    public void setName(String name);

    public PaymentResponse authorize(PaymentInfo paymentInfo, BankAccountPaymentInfo bankAccountPaymentInfo) throws PaymentException, PaymentProcessorException;

    public PaymentResponse debit(PaymentInfo paymentInfo, BankAccountPaymentInfo bankAccountPaymentInfo) throws PaymentException, PaymentProcessorException;

    public PaymentResponse authorizeAndDebit(PaymentInfo paymentInfo, BankAccountPaymentInfo bankAccountPaymentInfo) throws PaymentException, PaymentProcessorException;

    public PaymentResponse credit(PaymentInfo paymentInfo, BankAccountPaymentInfo bankAccountPaymentInfo) throws PaymentException, PaymentProcessorException;

    public PaymentResponse voidPayment(PaymentInfo paymentInfo, BankAccountPaymentInfo bankAccountPaymentInfo) throws PaymentException, PaymentProcessorException;
}
