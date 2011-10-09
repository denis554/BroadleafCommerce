package org.broadleafcommerce.openadmin.server.domain;


import org.broadleafcommerce.presentation.BroadleafEnumerationType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bpolster.
 */
public class SandBoxOperationType implements Serializable, BroadleafEnumerationType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, SandBoxOperationType> TYPES = new HashMap<String, SandBoxOperationType>();

    public static final SandBoxOperationType ADD     = new SandBoxOperationType("ADD", "Add");
    public static final SandBoxOperationType UPDATE  = new SandBoxOperationType("UPDATE", "Update");
    public static final SandBoxOperationType DELETE  = new SandBoxOperationType("DELETE", "Delete");

    public static SandBoxOperationType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public SandBoxOperationType() {
        //do nothing
    }

    public SandBoxOperationType(final String type, final String friendlyType) {
        this.friendlyType = friendlyType;
        setType(type);
    }

    public String getType() {
        return type;
    }

    public String getFriendlyType() {
        return friendlyType;
    }

    private void setType(final String type) {
        this.type = type;
        if (!TYPES.containsKey(type)) {
            TYPES.put(type, this);
        } else {
            throw new RuntimeException("Cannot add the type: (" + type + "). It already exists as a type via " + getInstance(type).getClass().getName());
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
        SandBoxOperationType other = (SandBoxOperationType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
