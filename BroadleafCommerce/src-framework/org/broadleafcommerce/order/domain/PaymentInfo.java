package org.broadleafcommerce.order.domain;

import java.util.List;

import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.Phone;
import org.broadleafcommerce.util.money.Money;

public interface PaymentInfo {

    public Long getId();

    public void setId(Long id);

    public Order getOrder();

    public void setOrder(Order order);

    public Address getAddress();

    public void setAddress(Address address);

    public Phone getPhone();

    public void setPhone(Phone phone);

    public Money getAmount();

    public void setAmount(Money amount);

    public String getReferenceNumber();

    public void setReferenceNumber(String referenceNumber);

    public String getType();

    public void setType(String type);

    public List<PaymentResponseItem> getPaymentResponseItems();

    public void setPaymentResponseItems(List<PaymentResponseItem> paymentResponseItems);

}
