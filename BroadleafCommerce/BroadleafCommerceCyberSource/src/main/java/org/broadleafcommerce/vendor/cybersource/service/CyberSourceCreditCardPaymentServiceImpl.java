/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.vendor.cybersource.service;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.util.money.Money;
import org.broadleafcommerce.vendor.cybersource.service.api.CCAuthReply;
import org.broadleafcommerce.vendor.cybersource.service.api.CCAuthService;
import org.broadleafcommerce.vendor.cybersource.service.api.Card;
import org.broadleafcommerce.vendor.cybersource.service.api.ReplyMessage;
import org.broadleafcommerce.vendor.cybersource.service.api.RequestMessage;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceAuthResponse;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceCardRequest;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceCardResponse;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourcePaymentRequest;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourcePaymentResponse;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceRequest;
import org.broadleafcommerce.vendor.cybersource.service.type.CyberSourceMethodType;
import org.broadleafcommerce.vendor.cybersource.service.type.CyberSourceServiceType;
import org.broadleafcommerce.vendor.cybersource.service.type.CyberSourceTransactionType;
import org.broadleafcommerce.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.service.exception.PaymentHostException;

public class CyberSourceCreditCardPaymentServiceImpl extends AbstractCyberSourcePaymentService implements CyberSourcePaymentService {
	
	private static final Log LOG = LogFactory.getLog(CyberSourceCreditCardPaymentServiceImpl.class);

	public CyberSourcePaymentResponse process(CyberSourcePaymentRequest paymentRequest) throws PaymentException {
		//TODO add validation for the request
		CyberSourceCardResponse cardResponse = new CyberSourceCardResponse();
		cardResponse.setServiceType(paymentRequest.getServiceType());
		cardResponse.setTransactionType(paymentRequest.getTransactionType());
		cardResponse.setMethodType(paymentRequest.getMethodType());
		RequestMessage request = buildRequestMessage(paymentRequest);
		ReplyMessage reply;
		try {
			reply = sendRequest(request);
        } catch (Exception e) {
            incrementFailure();
            throw new PaymentException(e);
        }
        clearStatus();
        buildResponse(cardResponse, reply);
        String[] invalidFields = reply.getInvalidField();
        String[] missingFields = reply.getMissingField();
        if ((invalidFields != null && invalidFields.length > 0) || (missingFields != null && missingFields.length > 0)) {
            PaymentHostException e = new PaymentHostException();
            cardResponse.setErrorDetected(true);
            StringBuffer sb = new StringBuffer();
            if (invalidFields != null && invalidFields.length > 0) {
	            sb.append("invalid fields :[ ");
	            for (String invalidField : invalidFields) {
	            	sb.append(invalidField);
	            }
	            sb.append(" ]\n");
            }
            if (missingFields != null && missingFields.length > 0) {
	            sb.append("missing fields: [ ");
	            for (String missingField : missingFields) {
	            	sb.append(missingField);
	            }
	            sb.append(" ]");
            }
            cardResponse.setErrorText(sb.toString());
            e.setPaymentResponse(cardResponse);
            throw e;
        }
        
        return cardResponse;
	}
	
	protected void logReply(ReplyMessage reply) {
		if (LOG.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Decision: ");
			sb.append(reply.getDecision());
			sb.append("\nMerchant Reference Code: ");
			sb.append(reply.getMerchantReferenceCode());
			sb.append("\nInvalid Fields[]: ");
			if (reply.getInvalidField() != null) {
				for (String invalidField: reply.getInvalidField()) {
					sb.append(invalidField);
					sb.append(";");
				}
			}
			sb.append("\nMissing Fields[]: ");
			if (reply.getMissingField() != null) {
				for (String missingField: reply.getMissingField()) {
					sb.append(missingField);
					sb.append(";");
				}
			}
			sb.append("\nReason Code: ");
			sb.append(reply.getReasonCode());
			sb.append("\nRequest ID: ");
			sb.append(reply.getRequestID());
			sb.append("\nRequest Token: ");
			sb.append(reply.getRequestToken());
			
			if (reply.getCcAuthReply() != null) {
				CCAuthReply authReply = reply.getCcAuthReply();
				sb.append("\nAccount Balance: ");
				sb.append(authReply.getAccountBalance());
				sb.append("\nAmount: ");
				sb.append(authReply.getAmount());
				sb.append("\nApproved Amount: ");
				sb.append(authReply.getApprovedAmount());
				sb.append("\nApproved Terms: ");
				sb.append(authReply.getApprovedTerms());
				sb.append("\nAuthentication XID: ");
				sb.append(authReply.getAuthenticationXID());
				sb.append("\nAuth Factor Code: ");
				sb.append(authReply.getAuthFactorCode());
				sb.append("\nAuthorization Code: ");
				sb.append(authReply.getAuthorizationCode());
				sb.append("\nAuthorization XID: ");
				sb.append(authReply.getAuthorizationXID());
				sb.append("\nAuthorized Date Time: ");
				sb.append(authReply.getAuthorizedDateTime());
				sb.append("\nAuth Record: ");
				sb.append(authReply.getAuthRecord());
				sb.append(("\nAvs Code: "));
				sb.append(authReply.getAvsCode());
				sb.append("\nAvs Code Raw: ");
				sb.append(authReply.getAvsCodeRaw());
				sb.append("\nBML Account Number: ");
				sb.append(authReply.getBmlAccountNumber());
				sb.append("\nCard Category: ");
				sb.append(authReply.getCardCategory());
				sb.append("\nCAVV Response Code: ");
				sb.append(authReply.getCavvResponseCode());
				sb.append("\nCAVV Response Code Raw: ");
				sb.append(authReply.getCavvResponseCodeRaw());
				sb.append("\nCredit Line: ");
				sb.append(authReply.getCreditLine());
				sb.append("\nCv Code: ");
				sb.append(authReply.getCvCode());
				sb.append("\nCv Code Raw: ");
				sb.append(authReply.getCvCodeRaw());
				sb.append("\nEnhanced Data Enabled: ");
				sb.append(authReply.getEnhancedDataEnabled());
				sb.append("\nForward Code: ");
				sb.append(authReply.getForwardCode());
				sb.append("\nMerchant Advice Code: ");
				sb.append(authReply.getMerchantAdviceCode());
				sb.append("\nMerchant Advice Code Raw: ");
				sb.append(authReply.getMerchantAdviceCodeRaw());
				sb.append("\nPayment Network Transaction ID: ");
				sb.append(authReply.getPaymentNetworkTransactionID());
				sb.append("\nPersonal ID Code: ");
				sb.append(authReply.getPersonalIDCode());
				sb.append("\nProcessor Card Type: ");
				sb.append(authReply.getProcessorCardType());
				sb.append("\nProcessor Response: ");
				sb.append(authReply.getProcessorResponse());
				sb.append("\nReason Code: ");
				sb.append(authReply.getReasonCode());
				sb.append("\nReconciliation ID: ");
				sb.append(authReply.getReconciliationID());
				sb.append("\nReferral Response Number: ");
				sb.append(authReply.getReferralResponseNumber());
				sb.append("\nSub Response Code: ");
				sb.append(authReply.getSubResponseCode());
			}
			LOG.debug("CyberSource Response:\n" + sb.toString());
		}
	}
	
	protected void buildResponse(CyberSourcePaymentResponse paymentResponse, ReplyMessage reply) {
		logReply(reply);
		paymentResponse.setDecision(reply.getDecision());
		paymentResponse.setInvalidField(reply.getInvalidField());
		paymentResponse.setMerchantReferenceCode(reply.getMerchantReferenceCode());
		paymentResponse.setMissingField(reply.getMissingField());
		if (reply.getReasonCode() != null) {
			paymentResponse.setReasonCode(reply.getReasonCode().intValue());
		}
		paymentResponse.setRequestID(reply.getRequestID());
		paymentResponse.setRequestToken(reply.getRequestToken());
		if (CyberSourceTransactionType.AUTHORIZE.equals(paymentResponse.getTransactionType())) {
			CCAuthReply authReply = reply.getCcAuthReply();
			CyberSourceAuthResponse authResponse = new CyberSourceAuthResponse();
			if (authReply.getAccountBalance() != null) {
				authResponse.setAccountBalance(new Money(authReply.getAccountBalance()));
			}
			if (authReply.getAmount() != null) {
				authResponse.setAmount(new Money(authReply.getAmount()));
			}
			if (authReply.getApprovedAmount() != null) {
				authResponse.setApprovedAmount(new Money(authReply.getApprovedAmount()));
			}
			authResponse.setApprovedTerms(authReply.getApprovedTerms());
			authResponse.setAuthenticationXID(authReply.getAuthenticationXID());
			authResponse.setAuthFactorCode(authReply.getAuthFactorCode());
			authResponse.setAuthorizationCode(authReply.getAuthorizationCode());
			authResponse.setAuthorizationXID(authReply.getAuthorizationXID());
			authResponse.setAuthorizedDateTime(authReply.getAuthorizedDateTime());
			authResponse.setAuthRecord(authReply.getAuthRecord());
			authResponse.setAvsCode(authReply.getAvsCode());
			authResponse.setAvsCodeRaw(authReply.getAvsCodeRaw());
			authResponse.setBmlAccountNumber(authReply.getBmlAccountNumber());
			authResponse.setCardCategory(authReply.getCardCategory());
			authResponse.setCavvResponseCode(authReply.getCavvResponseCode());
			authResponse.setCavvResponseCodeRaw(authReply.getCavvResponseCodeRaw());
			authResponse.setCreditLine(authReply.getCreditLine());
			authResponse.setCvCode(authReply.getCvCode());
			authResponse.setCvCodeRaw(authReply.getCvCodeRaw());
			authResponse.setEnhancedDataEnabled(authReply.getEnhancedDataEnabled());
			authResponse.setForwardCode(authReply.getForwardCode());
			authResponse.setMerchantAdviceCode(authReply.getMerchantAdviceCode());
			authResponse.setMerchantAdviceCodeRaw(authReply.getMerchantAdviceCodeRaw());
			authResponse.setPaymentNetworkTransactionID(authReply.getPaymentNetworkTransactionID());
			authResponse.setPersonalIDCode(authReply.getPersonalIDCode());
			authResponse.setProcessorCardType(authReply.getProcessorCardType());
			authResponse.setProcessorResponse(authReply.getProcessorResponse());
			authResponse.setReasonCode(authReply.getReasonCode());
			authResponse.setReconciliationID(authReply.getReconciliationID());
			authResponse.setReferralResponseNumber(authReply.getReferralResponseNumber());
			authResponse.setSubResponseCode(authReply.getSubResponseCode());
			
			((CyberSourceCardResponse) paymentResponse).setAuthResponse(authResponse);
		}
	}
	
	protected RequestMessage buildRequestMessage(CyberSourcePaymentRequest paymentRequest) {
		RequestMessage request = super.buildRequestMessage(paymentRequest);
		setCardInformation(paymentRequest, request);
		if (CyberSourceTransactionType.AUTHORIZE.equals(paymentRequest.getTransactionType())) {
			request.setCcAuthService(new CCAuthService());
	        request.getCcAuthService().setRun("true");
		}
		
		return request;
	}

	protected void setCardInformation(CyberSourcePaymentRequest paymentRequest, RequestMessage request) {
		CyberSourceCardRequest cardRequest = (CyberSourceCardRequest) paymentRequest;
		Card card = new Card();
		card.setAccountNumber(cardRequest.getAccountNumber());
		card.setBin(cardRequest.getBin());
		card.setCardType(cardRequest.getCardType());
		card.setCvIndicator(cardRequest.getCvIndicator());
		card.setCvNumber(cardRequest.getCvNumber());
		if (cardRequest.getExpirationMonth() != null) {
			card.setExpirationMonth(new BigInteger(String.valueOf(cardRequest.getExpirationMonth())));
		}
		if (cardRequest.getExpirationYear() != null) {
			card.setExpirationYear(new BigInteger(String.valueOf(cardRequest.getExpirationYear())));
		}
		card.setFullName(cardRequest.getFullName());
		card.setIssueNumber(cardRequest.getIssueNumber());
		card.setPin(cardRequest.getPin());
		if (cardRequest.getStartMonth() != null) {
			card.setStartMonth(new BigInteger(String.valueOf(cardRequest.getStartMonth())));
		}
		if (cardRequest.getStartYear() != null) {
			card.setStartYear(new BigInteger(String.valueOf(cardRequest.getStartYear())));
		}
		
        request.setCard(card);
	}

	public boolean isValidService(CyberSourceRequest request) {
		return CyberSourceServiceType.PAYMENT.equals(request.getServiceType()) && CyberSourceMethodType.CREDITCARD.equals(request.getMethodType());
	}
	
}
