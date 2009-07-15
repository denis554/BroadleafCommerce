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
package org.broadleafcommerce.email.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.email.dao.EmailReportingDao;
import org.broadleafcommerce.profile.domain.Customer;
import org.springframework.stereotype.Service;

/**
 * @author jfischer
 *
 */
@Service("blEmailTrackingManager")
public class EmailTrackingManagerImpl implements EmailTrackingManager {

    private static final Log LOG = LogFactory.getLog(EmailTrackingManagerImpl.class);

    @Resource(name="blEmailReportingDao")
    protected EmailReportingDao emailReportingDao;

    /* (non-Javadoc)
     * @see com.containerstore.web.task.service.EmailTrackingManager#createTrackedEmail(java.lang.String, java.lang.String, java.lang.String)
     */
    public Long createTrackedEmail(String emailAddress, String type, String extraValue) {
        return emailReportingDao.createTracking(emailAddress, type, extraValue);
    }

    @Override
    public void recordClick(Long emailId, Map<String, String> parameterMap, Customer customer, Map<String, String> extraValues) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("recordClick() => Click detected for Email["+emailId+"]");
        }

        Iterator<String> keys = parameterMap.keySet().iterator();
        // clean up and normalize the query string
        ArrayList<String> queryParms = new ArrayList<String>();
        while ( keys.hasNext() ) {
            String p = keys.next();
            // exclude email_id from the parms list
            if ( !p.equals("email_id") ) {
                queryParms.add( p );
            }
        }

        String newQuery = null;

        if ( queryParms.size() > 0 ) {

            String[] p = queryParms.toArray( new String[ queryParms.size() ] );
            Arrays.sort( p );

            StringBuffer newQueryParms = new StringBuffer();
            for ( int cnt = 0; cnt < p.length; cnt++ ) {
                newQueryParms.append( p[ cnt ] );
                newQueryParms.append( "=" );
                newQueryParms.append( parameterMap.get(p[ cnt ]) );
                if ( cnt != p.length - 1 ) {
                    newQueryParms.append("&");
                }
            }
            newQuery = newQueryParms.toString();
        }

        emailReportingDao.recordClick(emailId, customer, extraValues.get("requestUri"), newQuery);
    }

    /* (non-Javadoc)
     * @see com.containerstore.web.task.service.EmailTrackingManager#recordOpen(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public void recordOpen(Long emailId, Map<String, String> extraValues) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recording open for email id: " + emailId);
        }
        // extract necessary information from the request and record the open
        emailReportingDao.recordOpen(emailId, extraValues.get("userAgent"));
    }

}
