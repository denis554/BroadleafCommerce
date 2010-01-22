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
package org.broadleafcommerce.vendor;

import javax.annotation.Resource;

import org.broadleafcommerce.encryption.EncryptionModule;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderImpl;
import org.broadleafcommerce.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.payment.domain.PaymentInfo;
import org.broadleafcommerce.payment.domain.PaymentInfoImpl;
import org.broadleafcommerce.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.payment.domain.Referenced;
import org.broadleafcommerce.payment.service.PaymentContext;
import org.broadleafcommerce.payment.service.PaymentContextImpl;
import org.broadleafcommerce.payment.service.module.CyberSourceCreditCardModule;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;
import org.broadleafcommerce.profile.domain.CountryImpl;
import org.broadleafcommerce.profile.domain.StateImpl;
import org.broadleafcommerce.test.BaseTest;
import org.broadleafcommerce.util.money.Money;
import org.broadleafcommerce.vendor.cybersource.service.CyberSourceServiceManager;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

/**
 * @author jfischer
 *
 */
public class CyberSourceCreditCardModuleTest extends BaseTest {

	@Resource
    private CyberSourceServiceManager serviceManager;
	
	@Test(groups = { "testSuccessfulCyberSourceCCModulePayment" })
    @Rollback(false)
    public void testSuccessfulCyberSourceCCModulePayment() throws Exception {
		if (serviceManager.getMerchantId().equals("?")) {
            return;
        }
		
		/*
		 * authorize
		 */
		CyberSourceCreditCardModule module = new CyberSourceCreditCardModule();
		module.setServiceManager(serviceManager);
		
		Money amount = new Money(81.46);
		PaymentInfo paymentInfo = new PaymentInfoImpl();
		paymentInfo.setAmount(amount);
		Address address = new AddressImpl();
		address.setAddressLine1("1295 Charleston Road");
		address.setCity("Mountain View");
		address.setCountry(new CountryImpl() {
			@Override
			public String getAbbreviation() {
				return "US";
			}
		}
		);
		address.setFirstName("John");
		address.setLastName("Doe");
		address.setPostalCode("94043");
		address.setState(new StateImpl() {
			@Override
			public String getAbbreviation() {
				return "CA";
			}
		});
		paymentInfo.setAddress(address);
		paymentInfo.setCustomerIpAddress("10.7.111.111");
		Order order = new OrderImpl();
		order.setEmailAddress("null@cybersource.com");
		paymentInfo.setOrder(order);
		Referenced referenced = createCreditCardPaymentInfo("4111111111111111", 12, 2020, null);
		PaymentContext context = new PaymentContextImpl(amount, amount, paymentInfo, referenced, "test");
		
		PaymentResponseItem responseItem = module.authorize(context);
		assert(responseItem.getAmountPaid().equals(amount));
		
		/*
		 * debit
		 */
		paymentInfo.getAdditionalFields().put("requestId", responseItem.getAdditionalFields().get("requestId"));
		paymentInfo.getAdditionalFields().put("requestToken", responseItem.getAdditionalFields().get("requestToken"));
		
		PaymentResponseItem responseItem2 = module.debit(context);
		assert(responseItem2.getAmountPaid().equals(amount));
		
		/*
		 * authorize and debit
		 */
		PaymentResponseItem responseItem3 = module.authorizeAndDebit(context);
		assert(responseItem3.getAmountPaid().equals(amount));
	}
	
	private Referenced createCreditCardPaymentInfo(final String pan, final Integer month, final Integer year, final String cvv) {
		CreditCardPaymentInfo ccInfo = new CreditCardPaymentInfo() {

			public String getCvvCode() {
				return cvv;
			}

			public Integer getExpirationMonth() {
				return month;
			}

			public Integer getExpirationYear() {
				return year;
			}

			public Long getId() {
				return null;
			}

			public String getPan() {
				return pan;
			}

			public void setCvvCode(String cvvCode) {
				//do nothing
			}

			public void setExpirationMonth(Integer expirationMonth) {
				//do nothing
			}

			public void setExpirationYear(Integer expirationYear) {
				//do nothing
			}

			public void setId(Long id) {
				//do nothing
			}

			public void setPan(String pan) {
				//do nothing
			}

			public EncryptionModule getEncryptionModule() {
				return null;
			}

			public String getReferenceNumber() {
				return null;
			}

			public void setEncryptionModule(EncryptionModule encryptionModule) {
				//do nothing
			}

			public void setReferenceNumber(String referenceNumber) {
				//do nothing
			}
			
		};
		
		return ccInfo;
	}
}
