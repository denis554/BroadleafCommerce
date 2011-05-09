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
package org.broadleafcommerce.offer.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderImpl;
import org.broadleafcommerce.presentation.AdminPresentation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "BLC_OFFER_CODE")
@Inheritance(strategy=InheritanceType.JOINED)
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
public class OfferCodeImpl implements OfferCode {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "OfferCodeId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "OfferCodeId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "OfferCodeImpl", allocationSize = 50)
    @Column(name = "OFFER_CODE_ID")
    protected Long id;

    @ManyToOne(targetEntity = OfferImpl.class, optional=false)
    @JoinColumn(name = "OFFER_ID")
    @Index(name="OFFERCODE_OFFER_INDEX", columnNames={"OFFER_ID"})
    @AdminPresentation(friendlyName="Offer", group="Description")
    protected Offer offer;

    @Column(name = "OFFER_CODE", nullable=false)
    @Index(name="OFFERCODE_CODE_INDEX", columnNames={"OFFER_CODE"})
    @AdminPresentation(friendlyName="Offer Code", order=1, group="Description", prominent=true)
    protected String offerCode;

    @Column(name = "START_DATE")
    @AdminPresentation(friendlyName="Code Start Date", order=1, group="Activity Range")
    protected Date startDate;

    @Column(name = "END_DATE")
    @AdminPresentation(friendlyName="Code End Date", order=2, group="Activity Range")
    protected Date endDate;

    @Column(name = "MAX_USES")
    @AdminPresentation(friendlyName="Code Max Uses", order=2, group="Description", prominent=true)
    protected int maxUses;

    @Column(name = "USES")
    @AdminPresentation(friendlyName="Code Uses", order=3, group="Description", prominent=true)
    protected int uses;
    
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = OrderImpl.class)
    @JoinTable(name = "BLC_ORDER_OFFER_CODE_XREF", joinColumns = @JoinColumn(name = "OFFER_CODE_ID", referencedColumnName = "OFFER_CODE_ID"), inverseJoinColumns = @JoinColumn(name = "ORDER_ID", referencedColumnName = "ORDER_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blOrderElements")
    protected List<Order> orders = new ArrayList<Order>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((offer == null) ? 0 : offer.hashCode());
        result = prime * result + ((offerCode == null) ? 0 : offerCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OfferCodeImpl other = (OfferCodeImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (offer == null) {
            if (other.offer != null)
                return false;
        } else if (!offer.equals(other.offer))
            return false;
        if (offerCode == null) {
            if (other.offerCode != null)
                return false;
        } else if (!offerCode.equals(other.offerCode))
            return false;
        return true;
    }


}
