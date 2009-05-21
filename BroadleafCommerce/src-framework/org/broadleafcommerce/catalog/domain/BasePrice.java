package org.broadleafcommerce.catalog.domain;

import java.util.Date;

import org.broadleafcommerce.common.domain.Auditable;
import org.broadleafcommerce.util.money.Money;

/**
 * The Interface BasePrice.
 */
public interface BasePrice {

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public Long getId();

    /**
     * Sets the id.
     * 
     * @param id the new id
     */
    public void setId(Long id);

    /**
     * Gets the amount.
     * 
     * @return the amount
     */
    public Money getAmount();

    /**
     * Sets the amount.
     * 
     * @param amount the new amount
     */
    public void setAmount(Money amount);

    /**
     * Gets the start date.
     * 
     * @return the start date
     */
    public Date getStartDate();

    /**
     * Sets the start date.
     * 
     * @param startDate the new start date
     */
    public void setStartDate(Date startDate);

    /**
     * Gets the end date.
     * 
     * @return the end date
     */
    public Date getEndDate();

    /**
     * Sets the end date.
     * 
     * @param endDate the new end date
     */
    public void setEndDate(Date endDate);

    /**
     * Gets the sku.
     * 
     * @return the sku
     */
    public Sku getSku();

    /**
     * Sets the sku.
     * 
     * @param sku the new sku
     */
    public void setSku(Sku sku);

    /**
     * Gets the auditable.
     * 
     * @return the auditable
     */
    public Auditable getAuditable();

    /**
     * Sets the auditable.
     * 
     * @param auditable the new auditable
     */
    public void setAuditable(Auditable auditable);
}
