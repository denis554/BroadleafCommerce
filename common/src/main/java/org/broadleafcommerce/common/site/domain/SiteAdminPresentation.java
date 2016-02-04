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
package org.broadleafcommerce.common.site.domain;

import org.broadleafcommerce.common.presentation.AdminGroupPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminTabPresentation;

/**
 * Control tab layout and grouping for the SiteImpl entity
 *
 * @author Jeff Fischer
 */
@AdminPresentationClass(friendlyName = "baseSite",
    tabs = {
        @AdminTabPresentation(name = SiteAdminPresentation.TabName.General,
            order = SiteAdminPresentation.TabOrder.General,
            groups = {
                @AdminGroupPresentation(name = SiteAdminPresentation.GroupName.General,
                    order = SiteAdminPresentation.GroupOrder.General,
                    untitled = true),
                @AdminGroupPresentation(name = SiteAdminPresentation.GroupName.Security,
                    order = SiteAdminPresentation.GroupOrder.Security)
            }
        )
    }
)
public interface SiteAdminPresentation {

    public static class TabName {
        public static final String General = "SiteImpl_General_Tab";
    }

    public static class TabOrder {
        public static final int General = 1000;
    }

    public static class GroupName {
        public static final String General = "SiteImpl_General_Description";
        public static final String Security = "SiteImpl_Security_Description";
    }

    public static class GroupOrder {
        public static final int General = 1000;
        public static final int Security = 4000;
    }

}
