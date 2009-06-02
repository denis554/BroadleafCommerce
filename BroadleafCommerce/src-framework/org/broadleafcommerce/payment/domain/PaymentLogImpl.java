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
package org.broadleafcommerce.payment.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.broadleafcommerce.payment.service.type.BLCPaymentLogEventType;
import org.broadleafcommerce.payment.service.type.BLCTransactionType;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.domain.CustomerImpl;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_PAYMENT_LOG")
public class PaymentLogImpl implements PaymentLog {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "PaymentLogId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "PaymentLogId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "PaymentLogImpl", allocationSize = 1)
    @Column(name = "PAYMENT_LOG_ID")
    protected Long id;

    @Column(name = "USER_NAME")
    protected String userName;

    @Column(name = "TRANSACTION_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date transactionTimestamp;

    @ManyToOne(targetEntity = PaymentInfoImpl.class)
    @JoinColumn(name = "ORDER_PAYMENT_ID")
    protected PaymentInfo paymentInfo;

    @ManyToOne(targetEntity = CustomerImpl.class)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    protected Customer customer;

    @Column(name = "PAYMENT_INFO_REFERENCE_NUMBER")
    protected String paymentInfoReferenceNumber;

    @Column(name = "TRANSACTION_TYPE")
    protected String transactionType;

    @Column(name = "TRANSACTION_SUCCESS")
    protected Boolean transactionSuccess;

    @Column(name = "EXCEPTION_MESSAGE")
    protected String exceptionMessage;

    @Column(name = "LOG_TYPE")
    protected String logType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(Date transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getPaymentInfoReferenceNumber() {
        return paymentInfoReferenceNumber;
    }

    public void setPaymentInfoReferenceNumber(String paymentInfoReferenceNumber) {
        this.paymentInfoReferenceNumber = paymentInfoReferenceNumber;
    }

    public BLCTransactionType getTransactionType() {
        return BLCTransactionType.getInstance(transactionType);
    }

    public void setTransactionType(BLCTransactionType transactionType) {
        this.transactionType = transactionType.getType();
    }

    public BLCPaymentLogEventType getLogType() {
        return BLCPaymentLogEventType.getInstance(logType);
    }

    public void setLogType(BLCPaymentLogEventType logType) {
        this.logType = logType.getType();
    }

    public Boolean getTransactionSuccess() {
        return transactionSuccess;
    }

    public void setTransactionSuccess(Boolean transactionSuccess) {
        this.transactionSuccess = transactionSuccess;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
