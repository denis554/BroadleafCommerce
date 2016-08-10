/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.promotionMessage.domain;

import org.broadleafcommerce.common.copy.MultiTenantCloneable;
import org.broadleafcommerce.common.persistence.Status;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author Chris Kittrell (ckittrell)
 */
public interface PromotionMessage extends Status, Serializable,MultiTenantCloneable<PromotionMessage> {

    public void setId(Long id);

    public Long getId();

    public String getName();

    public void setName(String name);

    public String getType();

    public void setType(String type);

    public PromotionMessage getOverriddenPromotionMessage();

    public void setOverriddenPromotionMessage(PromotionMessage overriddenPromotionMessage);

    public String getMessage();

    public void setMessage(String message);

    public Map<String, PromotionMessageMediaXref> getPromotionMessageMedia();

    public void setPromotionMessageMedia(Map<String, PromotionMessageMediaXref> promotionMessageMedia);

    public int getPriority();

    public void setPriority(Integer priority);

    public Boolean getExcludeFromDisplay();

    public void setExcludeFromDisplay(Boolean excludeFromDisplay);

    public Date getStartDate();

    public void setStartDate(Date startDate);

    public Date getEndDate();

    public void setEndDate(Date endDate);

}
