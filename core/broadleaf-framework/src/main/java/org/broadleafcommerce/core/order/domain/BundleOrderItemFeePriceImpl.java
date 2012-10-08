/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.order.domain;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@DiscriminatorColumn(name = "TYPE")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_BUND_ITEM_FEE_PRICE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "blOrderElements")
public class BundleOrderItemFeePriceImpl implements BundleOrderItemFeePrice  {

    public static final Log LOG = LogFactory.getLog(BundleOrderItemFeePriceImpl.class);
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "BundleOrderItemFeePriceId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "BundleOrderItemFeePriceId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "BundleOrderItemFeePriceImpl", allocationSize = 50)
    @Column(name = "BUND_ITEM_FEE_PRICE_ID")
    protected Long id;

    @ManyToOne(targetEntity = BundleOrderItemImpl.class, optional = false)
    @JoinColumn(name = "BUND_ORDER_ITEM_ID")
    protected BundleOrderItem bundleOrderItem;

    @Column(name = "AMOUNT", precision=19, scale=5)
    protected BigDecimal amount;

    @Column(name = "NAME")
    private String name;

    @Column(name = "REPORTING_CODE")
    private String reportingCode;

    @Column(name = "IS_TAXABLE")
    private Boolean isTaxable = Boolean.FALSE;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public BundleOrderItem getBundleOrderItem() {
		return bundleOrderItem;
	}

	@Override
    public void setBundleOrderItem(BundleOrderItem bundleOrderItem) {
		this.bundleOrderItem = bundleOrderItem;
	}

	@Override
    public Money getAmount() {
        return amount == null ? null : org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl.getMoney(amount,bundleOrderItem.getOrder().getCurrency());
    }

    @Override
    public void setAmount(Money amount) {
        this.amount = Money.toAmount(amount);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Boolean isTaxable() {
        return isTaxable;
    }

    @Override
    public void setTaxable(Boolean isTaxable) {
        this.isTaxable = isTaxable;
    }

    @Override
    public String getReportingCode() {
        return reportingCode;
    }

    @Override
    public void setReportingCode(String reportingCode) {
        this.reportingCode = reportingCode;
    }

    public void checkCloneable(BundleOrderItemFeePrice bundleFeePrice) throws CloneNotSupportedException, SecurityException, NoSuchMethodException {
        Method cloneMethod = bundleFeePrice.getClass().getMethod("clone", new Class[]{});
        if (cloneMethod.getDeclaringClass().getName().startsWith("org.broadleafcommerce") && !bundleFeePrice.getClass().getName().startsWith("org.broadleafcommerce")) {
            //subclass is not implementing the clone method
            throw new CloneNotSupportedException("Custom extensions and implementations should implement clone in order to guarantee split and merge operations are performed accurately");
        }
    }

    @Override
    public BundleOrderItemFeePrice clone() {
        //instantiate from the fully qualified name via reflection
        BundleOrderItemFeePrice clone;
        try {
            clone = (BundleOrderItemFeePrice) Class.forName(this.getClass().getName()).newInstance();
            try {
                checkCloneable(clone);
            } catch (CloneNotSupportedException e) {
                LOG.warn("Clone implementation missing in inheritance hierarchy outside of Broadleaf: " + clone.getClass().getName(), e);
            }
            clone.setAmount(getAmount());
            clone.setName(getName());
            clone.setReportingCode(getReportingCode());
            clone.setBundleOrderItem(getBundleOrderItem());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((bundleOrderItem == null) ? 0 : bundleOrderItem.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isTaxable ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((reportingCode == null) ? 0 : reportingCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BundleOrderItemFeePriceImpl other = (BundleOrderItemFeePriceImpl) obj;
        if (amount == null) {
            if (other.amount != null) {
                return false;
            }
        } else if (!amount.equals(other.amount)) {
            return false;
        }
        if (bundleOrderItem == null) {
            if (other.bundleOrderItem != null) {
                return false;
            }
        } else if (!bundleOrderItem.equals(other.bundleOrderItem)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (isTaxable != other.isTaxable) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (reportingCode == null) {
            if (other.reportingCode != null) {
                return false;
            }
        } else if (!reportingCode.equals(other.reportingCode)) {
            return false;
        }
        return true;
    }
}
