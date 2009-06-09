package org.broadleafcommerce.email.domain;

import java.util.Arrays;

/**
 * Basic implementation of EmailTarget
 * @author bpolster
 */
public class EmailTargetImpl implements EmailTarget {

    private static final long serialVersionUID = 1L;

    protected String[] bccAddresses;
    protected String[] ccAddresses;
    protected String emailAddress;

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#getBCCAddresses()
     */
    @Override
    public String[] getBCCAddresses() {
        return bccAddresses;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#getCCAddresses()
     */
    @Override
    public String[] getCCAddresses() {
        return ccAddresses;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#getEmailAddress()
     */
    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#setBCCAddresses(java.lang.String[])
     */
    @Override
    public void setBCCAddresses(String[] bccAddresses) {
        this.bccAddresses = bccAddresses;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#setCCAddresses(java.lang.String[])
     */
    @Override
    public void setCCAddresses(String[] ccAddresses) {
        this.ccAddresses = ccAddresses;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.email.domain.EmailTarget#setEmailAddress(java.lang.String)
     */
    @Override
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bccAddresses);
        result = prime * result + Arrays.hashCode(ccAddresses);
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
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
        EmailTargetImpl other = (EmailTargetImpl) obj;
        if (!Arrays.equals(bccAddresses, other.bccAddresses))
            return false;
        if (!Arrays.equals(ccAddresses, other.ccAddresses))
            return false;
        if (emailAddress == null) {
            if (other.emailAddress != null)
                return false;
        } else if (!emailAddress.equals(other.emailAddress))
            return false;
        return true;
    }

}
