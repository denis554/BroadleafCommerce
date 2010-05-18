package org.broadleafcommerce.payment.service.module;

import java.util.Currency;

import org.broadleafcommerce.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.payment.service.PaymentContext;
import org.broadleafcommerce.payment.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.time.SystemTime;
import org.broadleafcommerce.util.money.Money;
import org.broadleafcommerce.vendor.cybersource.service.CyberSourceServiceManager;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceBillingRequest;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceItemRequest;
import org.broadleafcommerce.vendor.cybersource.service.payment.CyberSourcePaymentService;
import org.broadleafcommerce.vendor.cybersource.service.payment.message.CyberSourceCardRequest;
import org.broadleafcommerce.vendor.cybersource.service.payment.message.CyberSourceCardResponse;
import org.broadleafcommerce.vendor.cybersource.service.payment.type.CyberSourceTransactionType;

public class CyberSourceCreditCardModule extends CyberSourceModule implements PaymentModule {
	
	private CyberSourceServiceManager serviceManager;

	public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
		return authTypeTransaction(paymentContext, CyberSourceTransactionType.AUTHORIZE);
	}
	
	public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
		return authTypeTransaction(paymentContext, CyberSourceTransactionType.AUTHORIZEANDCAPTURE);
	}
	
	private PaymentResponseItem authTypeTransaction(PaymentContext paymentContext, CyberSourceTransactionType transactionType) throws PaymentException {
		CyberSourceCardRequest cardRequest = new CyberSourceCardRequest();
		setCardInfo(paymentContext, cardRequest);
        cardRequest.setTransactionType(transactionType);
        setCurrency(paymentContext, cardRequest);
        
        CyberSourceBillingRequest billingRequest = createBillingRequest(paymentContext.getPaymentInfo());
        cardRequest.setBillingRequest(billingRequest);
        
        CyberSourceItemRequest itemRequest = createItemRequest(paymentContext);
        cardRequest.getItemRequests().add(itemRequest);

        CyberSourceCardResponse response = callService(cardRequest);
		
        PaymentResponseItem responseItem = buildBasicResponse(response);
		responseItem.setAvsCode(response.getAuthResponse().getAvsCode());
		responseItem.setAuthorizationCode(response.getAuthResponse().getAuthorizationCode());
		responseItem.setAmountPaid(response.getAuthResponse().getAmount());
		responseItem.setProcessorResponseCode(response.getAuthResponse().getProcessorResponse());
		responseItem.setProcessorResponseText(response.getAuthResponse().getProcessorResponse());
        
        return responseItem;
	}

	public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
		throw new PaymentException("balance not supported");
	}

	public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
		CyberSourceCardRequest cardRequest = new CyberSourceCardRequest();
		cardRequest.setTransactionType(CyberSourceTransactionType.CREDIT);
		setCurrency(paymentContext, cardRequest);
		
		CyberSourceItemRequest itemRequest = createItemRequest(paymentContext);
        cardRequest.getItemRequests().add(itemRequest);
        
        cardRequest.setRequestID(paymentContext.getPaymentInfo().getAdditionalFields().get("requestId"));
        cardRequest.setRequestToken(paymentContext.getPaymentInfo().getAdditionalFields().get("requestToken"));
        
        CyberSourceCardResponse response = callService(cardRequest);
        
        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(response.getCreditResponse().getAmount());
        
        return responseItem;
	}

	public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
		CyberSourceCardRequest cardRequest = new CyberSourceCardRequest();
		cardRequest.setTransactionType(CyberSourceTransactionType.CAPTURE);
		setCurrency(paymentContext, cardRequest);
		
		CyberSourceItemRequest itemRequest = createItemRequest(paymentContext);
        cardRequest.getItemRequests().add(itemRequest);
        
        cardRequest.setRequestID(paymentContext.getPaymentInfo().getAdditionalFields().get("requestId"));
        cardRequest.setRequestToken(paymentContext.getPaymentInfo().getAdditionalFields().get("requestToken"));
        
        CyberSourceCardResponse response = callService(cardRequest);
        
        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(response.getCaptureResponse().getAmount());
        
        return responseItem;
	}
	
	public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
		CyberSourceCardRequest cardRequest = new CyberSourceCardRequest();
		cardRequest.setTransactionType(CyberSourceTransactionType.REVERSEAUTHORIZE);
		setCurrency(paymentContext, cardRequest);
		
		CyberSourceItemRequest itemRequest = createItemRequest(paymentContext);
        cardRequest.getItemRequests().add(itemRequest);
        
        cardRequest.setRequestID(paymentContext.getPaymentInfo().getAdditionalFields().get("requestId"));
        cardRequest.setRequestToken(paymentContext.getPaymentInfo().getAdditionalFields().get("requestToken"));
        
        CyberSourceCardResponse response = callService(cardRequest);
        
        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(response.getAuthReverseResponse().getAmount());
        responseItem.setAuthorizationCode(response.getAuthReverseResponse().getAuthorizationCode());
        responseItem.setProcessorResponseCode(response.getAuthReverseResponse().getProcessorResponse());
        responseItem.setProcessorResponseText(response.getAuthReverseResponse().getProcessorResponse());
        
        return responseItem;
	}

	public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
		CyberSourceCardRequest cardRequest = new CyberSourceCardRequest();
		cardRequest.setTransactionType(CyberSourceTransactionType.VOIDTRANSACTION);
		setCurrency(paymentContext, cardRequest);
		
		CyberSourceItemRequest itemRequest = createItemRequest(paymentContext);
        cardRequest.getItemRequests().add(itemRequest);
        
        cardRequest.setRequestID(paymentContext.getPaymentInfo().getAdditionalFields().get("requestId"));
        cardRequest.setRequestToken(paymentContext.getPaymentInfo().getAdditionalFields().get("requestToken"));
        
        CyberSourceCardResponse response = callService(cardRequest);
        
        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(response.getVoidResponse().getAmount());
        
        return responseItem;
	}

	public Boolean isValidCandidate(PaymentInfoType paymentType) {
		return PaymentInfoType.CREDIT_CARD.equals(paymentType);
	}
	
	private CyberSourceCardResponse callService(CyberSourceCardRequest cardRequest) throws PaymentException {
		CyberSourcePaymentService service = (CyberSourcePaymentService) serviceManager.getValidService(cardRequest);
        CyberSourceCardResponse response;
		try {
			response = (CyberSourceCardResponse) service.process(cardRequest);
		} catch (org.broadleafcommerce.vendor.service.exception.PaymentException e) {
			throw new PaymentException(e);
		}
		
		return response;
	}
	
	private PaymentResponseItem buildBasicResponse(CyberSourceCardResponse response) {
		PaymentResponseItem responseItem = new PaymentResponseItemImpl();
		responseItem.setTransactionTimestamp(SystemTime.asDate());
		responseItem.setMiddlewareResponseCode(response.getReasonCode().toString());
		responseItem.setMiddlewareResponseText(response.getDecision());
		responseItem.setReferenceNumber(response.getMerchantReferenceCode());
		responseItem.setTransactionId(response.getRequestToken());
		responseItem.setTransactionSuccess(response.getReasonCode().intValue() == 100);
		responseItem.getAdditionalFields().put("requestId", response.getRequestID());
		responseItem.getAdditionalFields().put("requestToken", response.getRequestToken());
		
		return responseItem;
	}
	
	private void setCardInfo(PaymentContext paymentContext, CyberSourceCardRequest cardRequest) {
		CreditCardPaymentInfo ccInfo = (CreditCardPaymentInfo) paymentContext.getReferencedPaymentInfo();
        cardRequest.setAccountNumber(ccInfo.getPan());
        cardRequest.setExpirationMonth(ccInfo.getExpirationMonth());
        cardRequest.setExpirationYear(ccInfo.getExpirationYear());
        cardRequest.setCvNumber(ccInfo.getCvvCode());
	}
	
	private void setCurrency(PaymentContext paymentContext, CyberSourceCardRequest cardRequest) {
		Currency currency = paymentContext.getPaymentInfo().getAmount().getCurrency();
        if (currency == null) {
        	currency = Money.defaultCurrency();
        }
        cardRequest.setCurrency(currency.getCurrencyCode());
	}
	
	private CyberSourceItemRequest createItemRequest(PaymentContext paymentContext) {
		CyberSourceItemRequest itemRequest = new CyberSourceItemRequest();
        itemRequest.setDescription("Order Charge");
        itemRequest.setId(1L);
        itemRequest.setQuantity(1L);
        itemRequest.setShortDescription("Order Charge");
        itemRequest.setUnitPrice(paymentContext.getPaymentInfo().getAmount());
        
        return itemRequest;
	}

	public CyberSourceServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(CyberSourceServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

}
