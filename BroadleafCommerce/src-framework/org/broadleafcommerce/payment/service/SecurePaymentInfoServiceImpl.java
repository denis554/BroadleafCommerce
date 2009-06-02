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
package org.broadleafcommerce.payment.service;

import javax.annotation.Resource;

import org.broadleafcommerce.payment.dao.SecurePaymentInfoDao;
import org.broadleafcommerce.payment.domain.BankAccountPaymentInfo;
import org.broadleafcommerce.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.payment.domain.GiftCardPaymentInfo;
import org.broadleafcommerce.payment.domain.Referenced;
import org.broadleafcommerce.payment.service.type.BLCPaymentInfoType;
import org.broadleafcommerce.workflow.WorkflowException;
import org.springframework.stereotype.Service;

/**
 * Acquisition of Primary Account Number (PAN) and other sensitive information
 * is retrieved through a service separate from the order. This conceptual separation facilitates
 * the physical separation of this sensitive data from the order. As a result, implementors
 * may host sensitive user account information in a datastore separate from the datastore
 * housing the order. This measure goes towards achieving a PCI compliant architecture.
 *
 * @author jfischer
 *
 */
@Service("blSecurePaymentInfoService")
public class SecurePaymentInfoServiceImpl implements SecurePaymentInfoService {

    @Resource
    protected SecurePaymentInfoDao securePaymentInfoDao;

    protected BankAccountPaymentInfo findBankAccountInfo(String referenceNumber) {
        return securePaymentInfoDao.findBankAccountInfo(referenceNumber);
    }

    protected CreditCardPaymentInfo findCreditCardInfo(String referenceNumber) {
        return securePaymentInfoDao.findCreditCardInfo(referenceNumber);
    }

    protected GiftCardPaymentInfo findGiftCardInfo(String referenceNumber) {
        return securePaymentInfoDao.findGiftCardInfo(referenceNumber);
    }

    public Referenced save(Referenced securePaymentInfo) {
        return securePaymentInfoDao.save(securePaymentInfo);
    }

    public Referenced create(String paymentInfoType) {
        if (paymentInfoType.equals(BLCPaymentInfoType.CREDIT_CARD.toString())) {
            CreditCardPaymentInfo ccinfo = securePaymentInfoDao.createCreditCardPaymentInfo();
            return ccinfo;
        } else if (paymentInfoType.equals(BLCPaymentInfoType.BANK_ACCOUNT.toString())) {
            BankAccountPaymentInfo bankinfo = securePaymentInfoDao.createBankAccountPaymentInfo();
            return bankinfo;
        } else if (paymentInfoType.equals(BLCPaymentInfoType.GIFT_CARD.toString())) {
            GiftCardPaymentInfo gcinfo = securePaymentInfoDao.createGiftCardPaymentInfo();
            return gcinfo;
        }

        return null;
    }

    @Override
    public Referenced findSecurePaymentInfo(String referenceNumber, String paymentInfoType) throws WorkflowException {
        if (paymentInfoType.equals(BLCPaymentInfoType.CREDIT_CARD.toString())) {
            CreditCardPaymentInfo ccinfo = findCreditCardInfo(referenceNumber);
            if (ccinfo == null) {
                throw new WorkflowException("No credit card info associated with credit card payment type with reference number: " + referenceNumber);
            }
            return ccinfo;
        } else if (paymentInfoType.equals(BLCPaymentInfoType.BANK_ACCOUNT.toString())) {
            BankAccountPaymentInfo bankinfo = findBankAccountInfo(referenceNumber);
            if (bankinfo == null) {
                throw new WorkflowException("No bank account info associated with bank account payment type with reference number: " + referenceNumber);
            }
            return bankinfo;
        } else if (paymentInfoType.equals(BLCPaymentInfoType.GIFT_CARD.toString())) {
            GiftCardPaymentInfo gcinfo = findGiftCardInfo(referenceNumber);
            if (gcinfo == null) {
                throw new WorkflowException("No bank account info associated with gift card payment type with reference number: " + referenceNumber);
            }
            return gcinfo;
        }

        return null;
    }

    @Override
    public void findAndRemoveSecurePaymentInfo(String referenceNumber, String paymentInfoType) throws WorkflowException {
        Referenced referenced = findSecurePaymentInfo(referenceNumber, paymentInfoType);
        remove(referenced);

    }

    @Override
    public void remove(Referenced securePaymentInfo) {
        securePaymentInfoDao.delete(securePaymentInfo);
    }

}
