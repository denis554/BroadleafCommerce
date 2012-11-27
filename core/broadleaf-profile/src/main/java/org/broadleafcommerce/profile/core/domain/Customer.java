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

package org.broadleafcommerce.profile.core.domain;

import org.broadleafcommerce.common.audit.Auditable;
import org.broadleafcommerce.common.locale.domain.Locale;

import java.io.Serializable;
import java.util.List;

public interface Customer extends Serializable {

    public Long getId();

    public void setId(Long id);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

    public boolean isPasswordChangeRequired();

    public void setPasswordChangeRequired(boolean passwordChangeRequired);

    public String getFirstName();

    public void setFirstName(String firstName);

    public String getLastName();

    public void setLastName(String lastName);

    public String getEmailAddress();

    public void setEmailAddress(String emailAddress);

    public ChallengeQuestion getChallengeQuestion();

	public void setChallengeQuestion(ChallengeQuestion challengeQuestion);

    public String getChallengeAnswer();

    public void setChallengeAnswer(String challengeAnswer);

    public String getUnencodedPassword();

    public void setUnencodedPassword(String unencodedPassword);

    public boolean isReceiveEmail();

    public void setReceiveEmail(boolean receiveEmail);

    public boolean isRegistered();

    public void setRegistered(boolean registered);

    public String getUnencodedChallengeAnswer();

    public void setUnencodedChallengeAnswer(String unencodedChallengeAnswer);

    public Auditable getAuditable();

    public void setAuditable(Auditable auditable);
    
	public void setCookied(boolean cookied);
    
    public boolean isCookied();

    public void setLoggedIn(boolean loggedIn);
    
    public boolean isLoggedIn();

    public void setAnonymous(boolean anonymous);
    
    public boolean isAnonymous(); 

    public Locale getCustomerLocale();

    public void setCustomerLocale(Locale customerLocale);
    
    public List<CustomerAttribute> getCustomerAttributes();
    
	/**
	 * Looks at all CustomerAttributes for this product and returns the attribute that matches the given name
	 * 
	 * @param name the name of the attribute
	 * @return the matching CustomerAttribute (null if no match)
	 */
	public CustomerAttribute getCustomerAttributeByName(String name);

	public void setCustomerAttributes(List<CustomerAttribute> customerAttributes);  
	
	/**
	 * Returns true if this user has been deactivated.
	 * Most implementations will not allow the user to login if they are deactivated.
	 * 
	 * @return
	 */
	public boolean isDeactivated();
	
	/**
	 * Sets the users deactivated status.
	 * 
	 * @param deactivated
	 */
	public void setDeactivated(boolean deactivated);

    /**
     * Returns a list of the Group Memberships that the customer is a member of.
     *
     * @return
     */
    public List<GroupMembership> getGroupMemberships();

    /**
     * Set the list of the Group Memberships that the customer is a member of.
     *
     * @param groupMemberships
     */
    public void setGroupMemberships(List<GroupMembership> groupMemberships);
	
}
