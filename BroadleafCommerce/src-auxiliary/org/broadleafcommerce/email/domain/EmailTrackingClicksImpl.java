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
package org.broadleafcommerce.email.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.domain.CustomerImpl;

/**
 * @author jfischer
 *
 */
@Entity
@Table(name = "BLC_EMAIL_TRACKING_CLICKS")
public class EmailTrackingClicksImpl implements EmailTrackingClicks {
	
	/** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue
	@Column(name = "CLICK_ID")
	private Long id;
	
	@ManyToOne(targetEntity = EmailTrackingImpl.class)
    @JoinColumn(name = "EMAIL_TRACKING_ID")
	private EmailTracking emailTracking;
	
	@Column(name = "DATE_CLICKED")
	private Date dateClicked;
	
	@ManyToOne(targetEntity = CustomerImpl.class)
    @JoinColumn(name = "CUSTOMER_ID")
	private Customer customer;
	
	@Column(name = "DESTINATION_URI")
	private String destinationUri;
	
	@Column(name = "QUERY_STRING")
	private String queryString;

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#getId()
	 */
	public Long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#getDateClicked()
	 */
	public Date getDateClicked() {
		return dateClicked;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#setDateClicked(java.util.Date)
	 */
	public void setDateClicked(Date dateClicked) {
		this.dateClicked = dateClicked;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#getDestinationUri()
	 */
	public String getDestinationUri() {
		return destinationUri;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#setDestinationUri(java.lang.String)
	 */
	public void setDestinationUri(String destinationUri) {
		this.destinationUri = destinationUri;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#getQueryString()
	 */
	public String getQueryString() {
		return queryString;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#setQueryString(java.lang.String)
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#getEmailTracking()
	 */
	public EmailTracking getEmailTracking() {
		return emailTracking;
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.email.domain.EmailTrackingClicks#setEmailTracking(org.broadleafcommerce.email.domain.EmailTrackingImpl)
	 */
	public void setEmailTracking(EmailTracking emailTracking) {
		this.emailTracking = emailTracking;
	}

	/**
	 * @return the customer
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * @param customer the customer to set
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
