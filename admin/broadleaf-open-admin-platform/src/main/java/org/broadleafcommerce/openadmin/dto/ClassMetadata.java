/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.openadmin.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.util.BLCMapUtils;
import org.broadleafcommerce.common.util.BLCMessageUtils;
import org.broadleafcommerce.common.util.TypedClosure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 
 * @author jfischer
 *
 */
@JsonAutoDetect
public class ClassMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String ceilingType;

    @JsonProperty
    private String securityCeilingType;

    @JsonIgnore
    private ClassTree polymorphicEntities;

    @JsonProperty(value = "classProperties")
    private Property[] properties;

    @JsonIgnore
    private TreeMap<String, TabMetadata> tabAndGroupMetadata;

    @JsonProperty    
    private String currencyCode = "USD";
    
    private Map<String, Property> pMap = null;

    public Map<String, Property> getPMap() {
        if (pMap == null) {
            pMap = BLCMapUtils.keyedMap(properties, new TypedClosure<String, Property>() {

                @Override
                public String getKey(Property value) {
                    return value.getName();
                }
            });
        }
        return pMap;
    }

    public String getCeilingType() {
        return ceilingType;
    }
    
    public void setCeilingType(String type) {
        this.ceilingType = type;
    }

    /**
     * For dynamic forms, the type to check security permissions will be different than the type used to generate the 
     * forms.   For example, a user with "ADD" or "UPDATE" permissions on STRUCTURE_CONTENT does not need 
     * to have the same level of access to StructuredContentTemplate.   
     * 
     * @param type
     */
    public String getSecurityCeilingType() {
        return securityCeilingType;
    }

    public void setSecurityCeilingType(String securityCeilingType) {
        this.securityCeilingType = securityCeilingType;
    }

    public ClassTree getPolymorphicEntities() {
        return polymorphicEntities;
    }

    public void setPolymorphicEntities(ClassTree polymorphicEntities) {
        this.polymorphicEntities = polymorphicEntities;
    }

    public Property[] getProperties() {
        return properties;
    }
    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public Map<String, TabMetadata> getTabAndGroupMetadata() {
        return tabAndGroupMetadata;
    }

    public void setTabAndGroupMetadata(Map<String, TabMetadata> tabAndGroupMetadata) {
        TreeMap<String, TabMetadata> orderedMap = new TreeMap<>(new TabOrderComparator(tabAndGroupMetadata));
        orderedMap.putAll(tabAndGroupMetadata);
        this.tabAndGroupMetadata = orderedMap;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public TabMetadata getTabMetadataUsingTabKey(String tabKey) {
        return tabAndGroupMetadata.get(tabKey);
    }

    public TabMetadata getTabMetadataUsingGroupKey(String groupKey) {
        for (TabMetadata tab : tabAndGroupMetadata.values()) {
            if (tab.getGroupMetadata() != null) {
                for (GroupMetadata group : tab.getGroupMetadata().values()) {
                    if (group.getGroupName() != null && group.getGroupName().equals(groupKey)) {
                        return tab;
                    }
                }
            }
        }
        return null;
    }

    public TabMetadata getFirstTab() {
        return tabAndGroupMetadata.firstEntry() == null ? null : tabAndGroupMetadata.firstEntry().getValue();
    }

    public String[][] getGroupOptionsFromTabAndGroupMetadata() {
        List<String[]> result = new ArrayList<>();

        for (TabMetadata tab : tabAndGroupMetadata.values()) {
            for (GroupMetadata group : tab.getGroupMetadata().values()) {
                String key = group.getGroupName();
                String displayValue = BLCMessageUtils.getMessage(tab.getTabName()) + " : " + BLCMessageUtils.getMessage(group.getGroupName());
                result.add(new String[]{key, displayValue});
            }
        }

        return result.toArray(new String[result.size()][2]);
    }

    class TabOrderComparator implements Comparator<String> {
        Map<String, TabMetadata> base;

        public TabOrderComparator(Map<String, TabMetadata> base) {
            this.base = base;
        }

        @Override
        public int compare(String key1, String key2) {
            if (base.get(key1) == null) {
                return 1;
            } else if (base.get(key2) == null) {
                return -1;
            } else {
                int comparison = base.get(key1).getTabOrder().compareTo(base.get(key2).getTabOrder());
                return comparison == 0 ? key1.compareTo(key2) : comparison;
            }
        }
    }
}
