package org.broadleafcommerce.email.domain;

/**
 * Basic implementation of EmailTarget
 * @author bpolster
 */
public class EmailTargetImpl implements EmailTarget {

    private static final long serialVersionUID = 1L;

    private String[] bccAddresses;
    private String[] ccAddresses;
    private String emailAddress;

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

}
