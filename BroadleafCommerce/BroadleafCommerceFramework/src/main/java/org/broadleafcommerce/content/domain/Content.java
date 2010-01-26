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
package org.broadleafcommerce.content.domain;

import java.util.Date;


/**
 * DOCUMENT ME!
 * @author btaylor
 * @author dwtalk
 */
public interface Content {
    
    public Date getActiveEndDate();

    public Date getActiveStartDate();

    public String getApprovedBy();

    public Date getApprovedDate();
    
    public String getBrowserTitle();

    public String getContentType();

    public String getDisplayRule();
    
    public Boolean isDeployed();
    
    public String getDescription();

    public Integer getId();
    
    public String getKeywords();
    
    public String getLanguageCode();

    public Date getLastModifiedDate();

    public String getLastModifiedBy();

    public String getMetaDescription();

    public String getNote();
    
    public Boolean isOnline();
    
    public Integer getParentContentId();

    public Integer getPriority();

    public String getRejectedBy();

    public Date getRejectedDate();

    public String getRenderTemplate();

    public String getSandbox();

    public String getSubmittedBy();

    public Date getSubmittedDate();
    
    public String getTitle();

    public void setActiveEndDate(Date activeEndDate);

    public void setActiveStartDate(Date activeStartDate);

    public void setApprovedBy(String approvedBy);

    public void setApprovedDate(Date approvedDate);

    public void setBrowserTitle(String browserTitle);

    public void setContentType(String contentType);

    public void setDisplayRule(String displayRule);
    
    public void setDeployed(Boolean deployed);
    
    public void setDescription(String description);

    public void setId(Integer id);
    
    public void setKeywords(String keywords);
    
    public void setLanguageCode(String languageCode);

    public void setLastModifiedDate(Date lastModifiedDate);

    public void setLastModifiedBy(String lastModifiedBy);
    
    public void setMetaDescription(String metaDescription);
    
    public void setNote(String note);
    
    public void setOnline(Boolean online);
    
    public void setParentContentId(Integer parentContentId);

    public void setPriority(Integer priority);

    public void setRejectedBy(String rejectedBy);

    public void setRejectedDate(Date rejectedDate);
    
    public void setRenderTemplate(String renderTemplate);

    public void setSandbox(String sandbox);

    public void setSubmittedBy(String submimttedBy);

    public void setSubmittedDate(Date submittedDate);
    
    public void setTitle(String title);
}
