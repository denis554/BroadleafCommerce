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

import org.broadleafcommerce.common.currency.util.BroadleafCurrencyUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorColumn(name = "TYPE")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FULFILLMENT_GROUP_FEE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "blOrderElements")
public class FulfillmentGroupFeeImpl implements FulfillmentGroupFee {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FulfillmentGroupFeeId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "FulfillmentGroupFeeId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "FulfillmentGroupFeeImpl", allocationSize = 50)
    @Column(name = "FULFILLMENT_GROUP_FEE_ID")
    protected Long id;

    @ManyToOne(targetEntity = FulfillmentGroupImpl.class, optional = false)
    @JoinColumn(name = "FULFILLMENT_GROUP_ID")
    protected FulfillmentGroup fulfillmentGroup;

    @Column(name = "AMOUNT", precision=19, scale=5)
    protected BigDecimal amount;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "REPORTING_CODE")
    protected String reportingCode;
    
    @Column(name = "FEE_TAXABLE_FLAG")
    @AdminPresentation(excluded = true)
    protected Boolean feeTaxable; 

    @OneToMany(fetch = FetchType.LAZY, targetEntity = TaxDetailImpl.class, cascade = {CascadeType.ALL})
    @JoinTable(name = "BLC_FG_FEE_TAX_XREF", joinColumns = @JoinColumn(name = "FULFILLMENT_GROUP_FEE_ID"), inverseJoinColumns = @JoinColumn(name = "TAX_DETAIL_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<TaxDetail> taxes = new ArrayList<TaxDetail>();
    
    @Column(name = "TOTAL_FEE_TAX", precision=19, scale=5)
    @AdminPresentation(friendlyName = "FulfillmentGroupFeeImpl_Total_Fee_Tax", order=9, group = "FulfillmentGroupFeeImpl_Pricing", fieldType=SupportedFieldType.MONEY)
    protected BigDecimal totalTax;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public FulfillmentGroup getFulfillmentGroup() {
        return fulfillmentGroup;
    }

    @Override
    public void setFulfillmentGroup(FulfillmentGroup fulfillmentGroup) {
        this.fulfillmentGroup = fulfillmentGroup;
    }

    @Override
    public Money getAmount() {
        return amount == null ? null : BroadleafCurrencyUtils.getMoney(amount, getFulfillmentGroup().getOrder().getCurrency());
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
    public String getReportingCode() {
        return reportingCode;
    }

    @Override
    public void setReportingCode(String reportingCode) {
        this.reportingCode = reportingCode;
    }
    
    @Override
    public Boolean isTaxable() {
        return feeTaxable == null ? true : feeTaxable;
    }

    @Override
    public void setTaxable(Boolean taxable) {
        this.feeTaxable = taxable;
    }
    
    @Override
    public List<TaxDetail> getTaxes() {
        return taxes;
    }

    @Override
    public void setTaxes(List<TaxDetail> taxes) {
        this.taxes = taxes;
    }
    
    @Override
    public Money getTotalTax() {
        return totalTax == null ? null : BroadleafCurrencyUtils.getMoney(totalTax, getFulfillmentGroup().getOrder().getCurrency());
    }

    @Override
    public void setTotalTax(Money totalTax) { 
        this.totalTax = Money.toAmount(totalTax);
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((fulfillmentGroup == null) ? 0 : fulfillmentGroup.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((reportingCode == null) ? 0 : reportingCode.hashCode());
        result = prime * result + ((taxes == null) ? 0 : taxes.hashCode());
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
        FulfillmentGroupFeeImpl other = (FulfillmentGroupFeeImpl) obj;
        if (amount == null) {
            if (other.amount != null) {
                return false;
            }
        } else if (!amount.equals(other.amount)) {
            return false;
        }
        if (fulfillmentGroup == null) {
            if (other.fulfillmentGroup != null) {
                return false;
            }
        } else if (!fulfillmentGroup.equals(other.fulfillmentGroup)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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
