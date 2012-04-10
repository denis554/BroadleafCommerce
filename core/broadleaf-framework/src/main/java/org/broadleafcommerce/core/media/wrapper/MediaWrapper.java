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


package org.broadleafcommerce.core.media.wrapper;

import org.broadleafcommerce.common.api.APIWrapper;
import org.broadleafcommerce.common.api.BaseWrapper;
import org.broadleafcommerce.core.media.domain.Media;

import javax.xml.bind.annotation.*;

/**
 *  JAXB wrapper class for Media.
 */
@XmlRootElement(name = "media")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class MediaWrapper extends BaseWrapper implements APIWrapper<Media> {

    /**
     * This allows us to control whether the URL should / can be overwritten, for example by the static asset service.
     */
    @XmlTransient
    protected boolean allowOverrideUrl = true;

    @XmlElement
    protected Long id;

    @XmlElement
    protected String name;

    @XmlElement
    protected String url;

    @XmlElement
    protected String label;
    
    public void wrap(Media media) {
        this.id = media.getId();
        this.name = media.getName();
        this.label = media.getLabel();
        this.url = media.getUrl();
    }

    public boolean isAllowOverrideUrl() {
        return allowOverrideUrl;
    }

    public void setAllowOverrideUrl(boolean allow) {
        this.allowOverrideUrl = allow;
    }

    /**
     * Call this only if allowOverrideUrl is true, and only AFTER you call wrap.
     * @param url
     */
    public void setUrl(String url) {
        if (allowOverrideUrl){
            this.url = url;
        }
    }
}
