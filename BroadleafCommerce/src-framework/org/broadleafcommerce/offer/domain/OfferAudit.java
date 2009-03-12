package org.broadleafcommerce.offer.domain;

import java.math.BigDecimal;
import java.sql.Date;

public interface OfferAudit {
	public Long getId() ;

	public void setId(Long id) ;

	public Offer getOffer() ;

	public void setOffer(Offer offer) ;

	public Long getOfferCodeId() ;

	public void setOfferCodeId(Long offerCodeId) ;

	public Long getCustomerId() ;

	public void setCustomerId(Long customerId) ;

	public void setRelatedId(Long id);
	
	public BigDecimal getRelatedRetailPrice() ;

	public void setRelatedRetailPrice(BigDecimal relatedRetailPrice) ;

	public BigDecimal getRelatedSalePrice() ;

	public void setRelatedSalePrice(BigDecimal relatedSalePrice) ;

	public BigDecimal getRelatedPrice() ;
	
	public void setRelatedPrice(BigDecimal relatedPrice) ;

	public Date getRedeemedDate() ;

	public void setRedeemedDate(Date redeemedDate) ;

}
