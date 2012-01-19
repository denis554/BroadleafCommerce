/*
 * Copyright 2008-2009 the original author or authors.
 *
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
 */

package org.broadleafcommerce.admin.client.presenter.order;

import com.smartgwt.client.data.Record;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.AbstractDynamicDataSource;
import org.broadleafcommerce.openadmin.client.presenter.entity.SubPresentable;
import org.broadleafcommerce.openadmin.client.presenter.entity.SubPresenter;
import org.broadleafcommerce.openadmin.client.view.dynamic.SubItemDisplay;

/**
 * @author Jeff Fischer
 */
public class PaymentInfoPresenter extends SubPresenter {

    protected SubPresentable paymentResponseItemPresenter;
    protected SubPresentable paymentLogPresenter;
    
    public PaymentInfoPresenter(SubItemDisplay display, String[] availableToTypes, Boolean showDisabledState, Boolean canEdit, Boolean showId, SubPresentable paymentResponseItemPresenter, SubPresentable paymentLogPresenter) {
        super(display, availableToTypes, showDisabledState, canEdit, showId);
        this.paymentLogPresenter = paymentLogPresenter;
        this.paymentResponseItemPresenter = paymentResponseItemPresenter;
    }
}
