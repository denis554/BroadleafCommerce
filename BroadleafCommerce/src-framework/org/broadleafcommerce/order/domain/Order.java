package org.broadleafcommerce.order.domain;

import java.util.Date;
import java.util.List;

import org.broadleafcommerce.common.domain.Auditable;
import org.broadleafcommerce.offer.domain.Offer;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.type.OrderStatus;
import org.broadleafcommerce.util.money.Money;

public interface Order {

    public Long getId();

    public void setId(Long id);

    public String getName();
    
    public void setName(String name);
    
    public Auditable getAuditable();

    public void setAuditable(Auditable auditable);

    public Money getSubTotal();

    public void setSubTotal(Money subTotal);

    public Money getTotal();

    public void setTotal(Money orderTotal);

    public Customer getCustomer();

    public void setCustomer(Customer customer);

    public OrderStatus getStatus();

	public void setStatus(OrderStatus status);

    public List<OrderItem> getOrderItems();

    public void setOrderItems(List<OrderItem> orderItems);

    public void addOrderItem(OrderItem orderItem) ;
    
    public void removeOrderItem(OrderItem orderItem);
    
    public List<FulfillmentGroup> getFulfillmentGroups();

    public void setFulfillmentGroups(List<FulfillmentGroup> fulfillmentGroups);

    public void addFulfillmentGroup(FulfillmentGroup fulfillmentGroup) ;
    
    public void removeFulfillmentGroup(FulfillmentGroup fulfillmentGroup) ;

        public List<Offer> getCandidateOffers();

    public void setCandidateOffers(List<Offer> offers);

    public void addCandidateOffer(Offer offer);

    public void removeAllOffers();

    public boolean isMarkedForOffer();

    public void setMarkedForOffer(boolean markForOffer);

	public Date getSubmitDate();

	public void setSubmitDate(Date submitDate);
}
