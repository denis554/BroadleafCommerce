/*
 * #%L
 * BroadleafCommerce Framework
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
package org.broadleafcommerce.core.search.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.broadleafcommerce.common.admin.domain.AdminMainEntity;
import org.broadleafcommerce.common.copy.CreateResponse;
import org.broadleafcommerce.common.copy.MultiTenantCopyContext;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransform;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformMember;
import org.broadleafcommerce.common.extensibility.jpa.copy.DirectCopyTransformTypes;
import org.broadleafcommerce.common.i18n.service.DynamicTranslationProvider;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationAdornedTargetCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationToOneLookup;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.RequiredOverride;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SEARCH_FACET")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blStandardElements")
@DirectCopyTransform({
        @DirectCopyTransformMember(templateTokens = DirectCopyTransformTypes.MULTITENANT_CATALOG)
})
public class SearchFacetImpl implements SearchFacet, Serializable, AdminMainEntity, SearchFacetAdminPresentation {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "SearchFacetId")
    @GenericGenerator(
        name="SearchFacetId",
        strategy="org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="segment_value", value="SearchFacetImpl"),
            @Parameter(name="entity_name", value="org.broadleafcommerce.core.search.domain.SearchFacetImpl")
        }
    )
    @Column(name = "SEARCH_FACET_ID")
    @AdminPresentation(friendlyName = "SearchFacetImpl_ID", order = 1, group = GroupName.General, visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @Column(name = "NAME")
    @AdminPresentation(friendlyName = "SearchFacetImpl_name", group = GroupName.General,
            groupOrder = 2, order = 2, prominent = true, translatable = true, gridOrder = 500, requiredOverride = RequiredOverride.REQUIRED)
    protected String name;
    
    @Column(name = "LABEL")
    @AdminPresentation(friendlyName = "SearchFacetImpl_label", order = 3, group = GroupName.General,
            prominent = true, translatable = true, gridOrder = 1000)
    protected String label;

    @ManyToOne(optional=false, targetEntity = IndexFieldTypeImpl.class)
    @JoinColumn(name = "INDEX_FIELD_TYPE_ID")
    @AdminPresentation(friendlyName = "SearchFacetImpl_field", order = 2000, group = GroupName.General,
            prominent = true, gridOrder = 2000, requiredOverride = RequiredOverride.REQUIRED)
    @AdminPresentationToOneLookup(lookupDisplayProperty = "indexField.field.friendlyName")
    protected IndexFieldType fieldType;
    
    @Column(name =  "SHOW_ON_SEARCH")
    @AdminPresentation(friendlyName = "SearchFacetImpl_showOnSearch", order = 2000,
            group = GroupName.Options, prominent = false,
            tooltip = "SearchFacetImpl_showOnSearchTooltip")
    protected Boolean showOnSearch = false;

    @Column(name = "USE_FACET_RANGES")
    @AdminPresentation(friendlyName = "SearchFacetImpl_useFacetRanges", order = 5000,
            group = GroupName.General,
            groupOrder = 1,
            tooltip = "SearchFacetImpl_useFacetRangesTooltip",
            defaultValue = "false")
    protected Boolean useFacetRanges = false;
    
    @Column(name = "SEARCH_DISPLAY_PRIORITY")
    @AdminPresentation(friendlyName = "SearchFacetImpl_searchPriority",
            order = 5000,
            group = GroupName.Options,
            groupOrder = 1,
            tooltip = "SearchFacetImpl_searchPriorityTooltip",
            tab = "SearchFacetImpl_Advanced_tab",
            tabOrder = 2000,
            visibility = VisibilityEnum.GRID_HIDDEN)
    protected Integer searchDisplayPriority = 1;
    
    @Column(name = "MULTISELECT")
    @AdminPresentation(friendlyName = "SearchFacetImpl_multiselect", order = 6000,
            group = GroupName.Options,
            groupOrder = 1,
            tooltip = "SearchFacetImpl_multiselectTooltip",
            tabOrder = 2000,
         defaultValue = "false")
    protected Boolean canMultiselect = true;
    
    @OneToMany(mappedBy = "searchFacet", targetEntity = SearchFacetRangeImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentationCollection(addType = AddMethodType.PERSIST,
            friendlyName = "newRangeTitle",
            group = GroupName.General)
    protected List<SearchFacetRange> searchFacetRanges  = new ArrayList<SearchFacetRange>();
    
    @OneToMany(mappedBy = "searchFacet", targetEntity = RequiredFacetImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentationAdornedTargetCollection(targetObjectProperty = "requiredFacet", friendlyName = "requiredFacetTitle",
            gridVisibleFields = { "name", "label", "fieldType.indexField.field.friendlyName" },
            group = GroupName.Dependent,
            tabOrder = 4000)
    protected List<RequiredFacet> requiredFacets = new ArrayList<RequiredFacet>();
    
    @Column(name = "REQUIRES_ALL_DEPENDENT")
    @AdminPresentation(friendlyName = "SearchFacetImpl_requiresAllDependentFacets",
            order = 7000,
            groupOrder = 1,
            tooltip = "SearchFacetImpl_requiresAllDependentFacetsTooltip",
            group = GroupName.Dependent,
            tabOrder = 3000)
    protected Boolean requiresAllDependentFacets = false;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public IndexFieldType getFieldType() {
        return fieldType;
    }
    
    @Override
    public void setFieldType(IndexFieldType fieldType) {
        this.fieldType = fieldType;
    }
    
    @Override
    public Field getField() {
        return getFieldType().getIndexField().getField();
    }
    
    @Override
    public String getFacetFieldType() {
        return getFieldType().getFieldType().getType();
    }

    @Override
    public String getName() {
        return DynamicTranslationProvider.getValue(this, "name", name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        if (Strings.isNullOrEmpty(this.label)) {
            return getName();
        }

        return DynamicTranslationProvider.getValue(this, "label", label);
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Boolean getShowOnSearch() {
        return showOnSearch;
    }

    @Override
    public void setShowOnSearch(Boolean showOnSearch) {
        this.showOnSearch = showOnSearch;
    }

    @Override
    public Integer getSearchDisplayPriority() {
        return searchDisplayPriority;
    }

    @Override
    public void setSearchDisplayPriority(Integer searchDisplayPriority) {
        this.searchDisplayPriority = searchDisplayPriority;
    }
    
    @Override
    public Boolean getCanMultiselect() {
        return canMultiselect;
    }

    @Override
    public void setCanMultiselect(Boolean canMultiselect) {
        this.canMultiselect = canMultiselect;
    }

    @Override
    public Boolean getUseFacetRanges() {
        return useFacetRanges;
    }

    @Override
    public void setUseFacetRanges(Boolean useFacetRanges) {
        this.useFacetRanges = useFacetRanges;
    }

    @Override
    public List<RequiredFacet> getRequiredFacets() {
        return requiredFacets;
    }

    @Override
    public void setRequiredFacets(List<RequiredFacet> requiredFacets) {
        this.requiredFacets = requiredFacets;
    }

    @Override
    public Boolean getRequiresAllDependentFacets() {
        return requiresAllDependentFacets == null ? false : requiresAllDependentFacets;
    }

    @Override
    public void setRequiresAllDependentFacets(Boolean requiresAllDependentFacets) {
        this.requiresAllDependentFacets = requiresAllDependentFacets;
    }

    @Override
    public List<SearchFacetRange> getSearchFacetRanges() {
        return searchFacetRanges;
    }

    @Override
    public void setSearchFacetRanges(List<SearchFacetRange> searchFacetRanges) {
        this.searchFacetRanges = searchFacetRanges;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass().isAssignableFrom(obj.getClass())) {
            SearchFacetImpl other = (SearchFacetImpl) obj;
            return new EqualsBuilder()
                .append(id, other.id)
                .append(fieldType, other.fieldType)
                .build();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31)
            .append(id)
            .append(fieldType)
            .toHashCode();
    }

    @Override
    public <G extends SearchFacet> CreateResponse<G> createOrRetrieveCopyInstance(MultiTenantCopyContext context) throws CloneNotSupportedException {
        CreateResponse<G> createResponse = context.createOrRetrieveCopyInstance(this);
        if (createResponse.isAlreadyPopulated()) {
            return createResponse;
        }
        SearchFacet cloned = createResponse.getClone();
        cloned.setCanMultiselect(canMultiselect);
        cloned.setLabel(label);
        cloned.setRequiresAllDependentFacets(requiresAllDependentFacets);
        cloned.setShowOnSearch(showOnSearch);
        cloned.setFieldType(fieldType.createOrRetrieveCopyInstance(context).getClone());
        for(RequiredFacet entry : requiredFacets){
            RequiredFacet clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getRequiredFacets().add(clonedEntry);
        }
        cloned.setSearchDisplayPriority(searchDisplayPriority);
        for(SearchFacetRange entry : searchFacetRanges){
            SearchFacetRange clonedEntry = entry.createOrRetrieveCopyInstance(context).getClone();
            cloned.getSearchFacetRanges().add(clonedEntry);
        }

        return createResponse;
    }

    @Override
    public String getMainEntityName() {
        return getLabel();
    }
}
