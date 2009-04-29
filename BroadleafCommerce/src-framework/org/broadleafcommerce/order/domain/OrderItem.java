package org.broadleafcommerce.order.domain;

import java.util.List;

import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.offer.domain.CandidateItemOffer;
import org.broadleafcommerce.offer.domain.Offer;
import org.broadleafcommerce.util.money.Money;

public interface OrderItem {

    public Long getId();

    public void setId(Long id);

    public Sku getSku();

    public void setSku(Sku sku);

    public Long getOrderId();

    public void setOrderId(Long orderId);

    public Money getRetailPrice();

    public void setRetailPrice(Money retailPrice);

    public Money getSalePrice();

    public void setSalePrice(Money salePrice);

    public Money getPrice();

    public void setPrice(Money price);

    public int getQuantity();

    public void setQuantity(int quantity);

    public Product getProduct();

    public void setProduct(Product product);

    public Category getCategory();

    public void setCategory(Category category);

    public List<CandidateItemOffer> getCandidateItemOffers();

    public void setCandidateItemOffers(List<CandidateItemOffer> candidateOffers);

    public List<CandidateItemOffer> addCandidateItemOffer(CandidateItemOffer candidateOffer);

    public void setAppliedItemOffers(List<Offer> appliedOffers);

    public List<Offer> getAppliedItemOffers();

    public List<Offer> addAppliedItemOffer(Offer appliedOffer);

    public void removeAllOffers();

    public boolean markForOffer();

    public int getMarkedForOffer();

    public boolean unmarkForOffer();

    public boolean isAllQuantityMarkedForOffer();

    public PersonalMessage getPersonalMessage();

    public void setPersonalMessage(PersonalMessage personalMessage);
}
