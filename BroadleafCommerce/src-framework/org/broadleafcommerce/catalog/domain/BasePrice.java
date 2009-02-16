package org.broadleafcommerce.catalog.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.broadleafcommerce.common.domain.Auditable;

public interface BasePrice {

    public Long getId();

    public void setId(Long id);

    public BigDecimal getAmount();

    public void setAmount(BigDecimal amount);

    public Date getStartDate();

    public void setStartDate(Date startDate);

    public Date getEndDate();

    public void setEndDate(Date endDate);

    public Sku getSku();

    public void setSku(Sku sku);

    public Auditable getAuditable();

    public void setAuditable(Auditable auditable);
}
