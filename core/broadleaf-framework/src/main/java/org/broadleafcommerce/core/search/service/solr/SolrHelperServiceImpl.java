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

package org.broadleafcommerce.core.search.service.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.common.locale.service.LocaleService;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.search.domain.Field;
import org.broadleafcommerce.core.search.domain.solr.FieldType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Provides utility methods that are used by other Solr service classes
 * 
 * @author Andre Azzolini (apazzolini)
 */
@Service("blSolrHelperService")
public class SolrHelperServiceImpl implements SolrHelperService {

    private static final Log LOG = LogFactory.getLog(SolrHelperServiceImpl.class);

    // The value of these two fields has no special significance, but they must be non-blank
    private static final String GLOBAL_FACET_TAG_FIELD = "a";
    private static final String DEFAULT_NAMESPACE = "d";

    protected static Locale defaultLocale;
    protected static SolrServer server;

    @Resource(name = "blLocaleService")
    protected LocaleService localeService;

    @Override
    public void swapActiveCores() throws ServiceException {
        if (SolrContext.isSingleCoreMode()) {
            LOG.debug("In single core mode. There are no cores to swap.");
        } else {
            LOG.debug("Swapping active cores");

            CoreAdminRequest car = new CoreAdminRequest();
            car.setCoreName(SolrContext.PRIMARY);
            car.setOtherCoreName(SolrContext.REINDEX);
            car.setAction(CoreAdminAction.SWAP);

            try {
                SolrContext.getServer().request(car);
                SolrContext.getServer().commit();
            } catch (Exception e) {
                LOG.error(e);
                throw new ServiceException("Unable to swap cores", e);
            }
        }
    }

    @Override
    public String getCurrentNamespace() {
        return DEFAULT_NAMESPACE;
    }

    @Override
    public String getGlobalFacetTagField() {
        return GLOBAL_FACET_TAG_FIELD;
    }

    @Override
    public String getPropertyNameForFieldSearchable(Field field, FieldType searchableFieldType, String prefix) {
        return new StringBuilder()
                .append(prefix)
                .append(field.getPropertyName()).append("_").append(searchableFieldType.getType())
                .toString();
    }

    @Override
    public String getPropertyNameForFieldFacet(Field field, String prefix) {
        if (field.getFacetFieldType() == null) {
            return null;
        }

        return new StringBuilder()
                .append(prefix)
                .append(field.getPropertyName()).append("_").append(field.getFacetFieldType().getType())
                .toString();
    }

    @Override
    public String getPropertyNameForFieldSearchable(Field field, FieldType searchableFieldType) {
        String prefix = "";
        if (!searchableFieldType.equals(FieldType.PRICE)) {
            prefix = getLocalePrefix();
        }

        return getPropertyNameForFieldSearchable(field, searchableFieldType, prefix);
    }

    @Override
    public String getPropertyNameForFieldFacet(Field field) {
        FieldType fieldType = field.getFacetFieldType();
        if (fieldType == null) {
            return null;
        }

        String prefix = "";
        if (!fieldType.equals(FieldType.PRICE)) {
            prefix = getLocalePrefix();
        }

        return getPropertyNameForFieldFacet(field, prefix);
    }

    @Override
    public String getNamespaceFieldName() {
        return "namespace";
    }

    @Override
    public String getIdFieldName() {
        return "id";
    }

    @Override
    public String getCategoryFieldName() {
        return "category";
    }

    @Override
    public String getExplicitCategoryFieldName() {
        return "explicitCategory";
    }

    @Override
    public String getSearchableFieldName(String prefix) {
        return new StringBuilder()
                .append(prefix)
                .append("searchable")
                .toString();
    }

    @Override
    public String getSearchableFieldName() {
        return getSearchableFieldName(getLocalePrefix());
    }

    @Override
    public String getCategorySortFieldName(Category category) {
        return new StringBuilder()
                .append(getCategoryFieldName())
                .append("_").append(category.getId()).append("_").append("sort_i")
                .toString();
    }

    @Override
    public String getLocalePrefix() {
        if (BroadleafRequestContext.getBroadleafRequestContext() != null) {
            Locale locale = BroadleafRequestContext.getBroadleafRequestContext().getLocale();
            if (locale != null) {
                return locale.getLocaleCode() + "_";
            }
        }
        return getDefaultLocalePrefix();
    }

    @Override
    public String getDefaultLocalePrefix() {
        return getDefaultLocale().getLocaleCode() + "_";
    }

    @Override
    public Locale getDefaultLocale() {
        if (defaultLocale == null) {
            defaultLocale = localeService.findDefaultLocale();
        }

        return defaultLocale;
    }

}