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
package org.broadleafcommerce.vendor.usps.service.type;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.broadleafcommerce.util.TypeEnumeration;

/**
 * An extendible enumeration of V2 service types.
 * 
 * @author jfischer
 */
public class USPSServiceV2Type implements Serializable, TypeEnumeration {

    private static final long serialVersionUID = 1L;

    private static final Map<String, USPSServiceV2Type> types = new Hashtable<String, USPSServiceV2Type>();

    public static USPSServiceV2Type ALL  = new USPSServiceV2Type("ALL");
    public static USPSServiceV2Type FIRSTCLASS = new USPSServiceV2Type("FIRST CLASS");
    public static USPSServiceV2Type PRIORITY = new USPSServiceV2Type("PRIORITY");
    public static USPSServiceV2Type EXPRESS = new USPSServiceV2Type("EXPRESS");
    public static USPSServiceV2Type BPM = new USPSServiceV2Type("BPM");
    public static USPSServiceV2Type PARCEL = new USPSServiceV2Type("PARCEL");
    public static USPSServiceV2Type MEDIA = new USPSServiceV2Type("MEDIA");
    public static USPSServiceV2Type LIBRARY = new USPSServiceV2Type("LIBRARY");

    public static USPSServiceV2Type getInstance(String type) {
        return types.get(type);
    }

    private String type;

    public USPSServiceV2Type() {
        //do nothing
    }

    public USPSServiceV2Type(String type) {
        setType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        if (!types.containsKey(type)) {
            types.put(type, this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        USPSServiceV2Type other = (USPSServiceV2Type) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
