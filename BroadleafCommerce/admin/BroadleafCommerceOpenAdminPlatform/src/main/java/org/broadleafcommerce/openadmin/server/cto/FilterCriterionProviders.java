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
package org.broadleafcommerce.openadmin.server.cto;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.anasoft.os.daofusion.criteria.FilterCriterionProvider;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider;
import com.anasoft.os.daofusion.criteria.SimpleFilterCriterionProvider.FilterDataStrategy;

/**
 * Utility class providing common {@link FilterCriterionProvider}
 * implementations.
 * 
 * @author jfischer
 */
public final class FilterCriterionProviders {
    
    private FilterCriterionProviders() {
    }
    
    public static final FilterCriterionProvider LIKE = new SimpleFilterCriterionProvider(FilterDataStrategy.DIRECT, 1) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
            return Restrictions.ilike(targetPropertyName, (String) directValues[0], MatchMode.ANYWHERE);
        }
    };
    
    public static final FilterCriterionProvider EQ = new SimpleFilterCriterionProvider(FilterDataStrategy.NONE, 1) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
            return Restrictions.eq(targetPropertyName, directValues[0]);
        }
    };
    
    public static final FilterCriterionProvider ISNULL = new SimpleFilterCriterionProvider(FilterDataStrategy.NONE, 1) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
            return Restrictions.isNull(targetPropertyName);
        }
    };
    
    public static final FilterCriterionProvider LE = new SimpleFilterCriterionProvider(FilterDataStrategy.DIRECT, 1) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
            return Restrictions.le(targetPropertyName, directValues[0]);
        }
    };
    
    public static final FilterCriterionProvider BETWEEN = new SimpleFilterCriterionProvider(FilterDataStrategy.NONE, 2) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
        	if (directValues.length > 1) {
				return Restrictions.between(targetPropertyName, directValues[0], directValues[1]);
        	} else {
        		return Restrictions.eq(targetPropertyName, directValues[0]);
        	}
        }
    };
    
    public static final FilterCriterionProvider COLLECTION_SIZE_EQ = new SimpleFilterCriterionProvider(FilterDataStrategy.DIRECT, 1) {
        public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
            return Restrictions.sizeEq(targetPropertyName, (Integer) directValues[0]);
        }
    };
    
}
