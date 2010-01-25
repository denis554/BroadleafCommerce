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
package org.broadleafcommerce.vendor.cybersource.service.payment.message;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.util.money.Money;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceItemRequest;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceRequest;
import org.broadleafcommerce.vendor.cybersource.service.payment.type.CyberSourceMethodType;
import org.broadleafcommerce.vendor.cybersource.service.payment.type.CyberSourceTransactionType;


public abstract class CyberSourcePaymentRequest extends CyberSourceRequest {
	
	private static final long serialVersionUID = 1L;
	
	private CyberSourceBillingRequest billingRequest;
	private String currency;
	private List<CyberSourceItemRequest> itemRequests = new ArrayList<CyberSourceItemRequest>();
	private CyberSourceTransactionType transactionType;
	private CyberSourceMethodType methodType;
	private Money grandTotal;
	private Boolean useGrandTotal;

	public CyberSourceTransactionType getTransactionType() {
		return transactionType;
	}
	
	public void setTransactionType(CyberSourceTransactionType transactionType) {
		this.transactionType = transactionType;
	}
	
	public CyberSourceMethodType getMethodType() {
		return methodType;
	}

	public void setMethodType(CyberSourceMethodType methodType) {
		this.methodType = methodType;
	}
	
	public CyberSourceBillingRequest getBillingRequest() {
		return billingRequest;
	}
	
	public void setBillingRequest(CyberSourceBillingRequest billingRequest) {
		this.billingRequest = billingRequest;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public List<CyberSourceItemRequest> getItemRequests() {
		return itemRequests;
	}
	
	public void setItemRequests(List<CyberSourceItemRequest> itemRequests) {
		this.itemRequests = itemRequests;
	}

	public Money getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(Money grandTotal) {
		this.grandTotal = grandTotal;
	}

	public Boolean getUseGrandTotal() {
		return useGrandTotal;
	}

	public void setUseGrandTotal(Boolean useGrandTotal) {
		this.useGrandTotal = useGrandTotal;
	}
	
}
