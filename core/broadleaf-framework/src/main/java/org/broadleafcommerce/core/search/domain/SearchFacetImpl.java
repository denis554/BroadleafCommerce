/*
 * Copyright 2012 the original author or authors.
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

package org.broadleafcommerce.core.search.domain;

import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.locale.domain.LocaleImpl;
import org.broadleafcommerce.common.locale.util.LocaleUtil;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationAdornedTargetCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationCollection;
import org.broadleafcommerce.common.presentation.AdminPresentationMap;
import org.broadleafcommerce.common.presentation.AdminPresentationToOneLookup;
import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MapKey;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SEARCH_FACET")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blStandardElements")
public class SearchFacetImpl implements SearchFacet,java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "SearchFacetId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "SearchFacetId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "SearchFacetImpl", allocationSize = 50)
    @Column(name = "SEARCH_FACET_ID")
    @AdminPresentation(friendlyName = "SearchFacetImpl_ID", order = 1, group = "SearchFacetImpl_description", groupOrder = 1, visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;
    
    @ManyToOne(optional=false, targetEntity = FieldImpl.class)
    @JoinColumn(name = "FIELD_ID")
    @AdminPresentation(friendlyName = "SearchFacetImpl_field",  order = 2, group = "SearchFacetImpl_description")
    @AdminPresentationToOneLookup(lookupDisplayProperty = "propertyName")
    protected Field field;
    
    @Column(name = "LABEL")
    @AdminPresentation(friendlyName = "SearchFacetImpl_label", order = 3, group = "SearchFacetImpl_description", groupOrder = 1, prominent = true)
    protected String label;
    
    @Column(name =  "SHOW_ON_SEARCH")
    @AdminPresentation(friendlyName = "SearchFacetImpl_showOnSearch", order = 4, group = "SearchFacetImpl_description", groupOrder = 1,prominent=false)
    protected Boolean showOnSearch = false;
    
    @Column(name = "SEARCH_DISPLAY_PRIORITY")
    @AdminPresentation(friendlyName = "SearchFacetImpl_searchPriority", order = 5, group = "SearchFacetImpl_description", groupOrder = 1, prominent=true)
    protected Integer searchDisplayPriority = 1;
    
    @Column(name = "MULTISELECT")
    @AdminPresentation(friendlyName = "SearchFacetImpl_multiselect", order = 6, group = "SearchFacetImpl_description", groupOrder = 1, prominent=true)
    protected Boolean canMultiselect = true;
    
    @OneToMany(mappedBy = "searchFacet", targetEntity = SearchFacetRangeImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentationCollection(addType = AddMethodType.PERSIST, friendlyName = "newRangeTitle", dataSourceName = "searchFacetRangeDS")
    protected List<SearchFacetRange> searchFacetRanges  = new ArrayList<SearchFacetRange>();
    
    @OneToMany(mappedBy = "searchFacet", targetEntity = RequiredFacetImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentationAdornedTargetCollection(targetObjectProperty = "requiredFacet", friendlyName = "requiredFacetTitle", dataSourceName = "requiredFacetDS", gridVisibleFields = {"label", "searchDisplayPriority", "canMultiselect", "requiresAllDependentFacets"})
    protected List<RequiredFacet> requiredFacets = new ArrayList<RequiredFacet>();
    
    @Column(name = "REQUIRES_ALL_DEPENDENT")
    @AdminPresentation(friendlyName = "SearchFacetImpl_requiresAllDependentFacets", order = 6, group = "SearchFacetImpl_description", groupOrder = 1, prominent=true)
    protected Boolean requiresAllDependentFacets = false;

    @ManyToMany(targetEntity = SearchFacetTranslationImpl.class)
    @JoinTable(name = "BLC_SEARCH_FACET_TRANSLATION_XREF",
            joinColumns = @JoinColumn(name = "SEARCH_FACET_ID", referencedColumnName = "SEARCH_FACET_ID"),
            inverseJoinColumns = @JoinColumn(name = "TRANSLATION_ID", referencedColumnName = "TRANSLATION_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @MapKey(columns = { @Column(name = "MAP_KEY", nullable = false) })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @BatchSize(size = 10)
    @AdminPresentationMap(
            friendlyName = "searchFacetImpl_Translations",
            dataSourceName = "searchFacetTranslationDS",
            keyPropertyFriendlyName = "TranslationsImpl_Key",
            deleteEntityUponRemove = true,
            mapKeyOptionEntityClass = LocaleImpl.class,
            mapKeyOptionEntityDisplayField = "friendlyName",
            mapKeyOptionEntityValueField = "localeCode"

    )
    protected Map<String, SearchFacetTranslation> translations = new HashMap<String,SearchFacetTranslation>();
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public Field getField() {
		return field;
	}

	@Override
	public void setField(Field field) {
		this.field = field;
	}

	@Override
	public String getLabel() {
        if (translations != null && BroadleafRequestContext.hasLocale()) {
            Locale locale = BroadleafRequestContext.getBroadleafRequestContext().getLocale();

            // Search for translation based on locale
            String localeCode = locale.getLocaleCode();
            if (localeCode != null) {
                SearchFacetTranslation translation = translations.get(localeCode);
                if (translation != null && translation.getLabel() != null) {
                    return translation.getLabel();
                }
            }

            // try just the language
            String languageCode = LocaleUtil.findLanguageCode(locale);
            if (languageCode != null && ! localeCode.equals(languageCode)) {
                SearchFacetTranslation translation = translations.get(languageCode);
                if (translation != null && translation.getLabel() != null) {
                    return translation.getLabel();
                }
            }
        }
        return label;
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

    public List<RequiredFacet> getRequiredFacets() {
        return requiredFacets;
    }

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
    public Map<String, SearchFacetTranslation> getTranslations() {
        return translations;
    }

    @Override
    public void setTranslations(Map<String, SearchFacetTranslation> translations) {
        this.translations = translations;
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
	   	if (this == obj) {
            return true;
        }
	    if (obj == null) {
            return false;
        }
	    if (getClass() != obj.getClass()) {
            return false;
        }
        SearchFacet other = (SearchFacet) obj;
        
        return getField().equals(other.getField());
    }

}
