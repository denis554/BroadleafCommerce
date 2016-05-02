/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License” located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License” located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.openadmin.web.controller;

import org.broadleafcommerce.common.extension.ExtensionHandler;
import org.broadleafcommerce.openadmin.dto.ClassMetadata;
import org.broadleafcommerce.openadmin.web.form.entity.DefaultMainActions;
import org.broadleafcommerce.openadmin.web.form.entity.EntityFormAction;

import java.util.List;


/**
 * Deprecated - Use {@link org.broadleafcommerce.openadmin.web.controller.AdminAbstractControllerExtensionHandler}
 *
 * @author Phillip Verheyden (phillipuniverse)
 */
@Deprecated
public interface MainEntityActionsExtensionHandler extends ExtensionHandler {

    /**
     * Extension point to override the actions that are added by default when viewing a ceiling entity for a particular
     * section (for instance, a list of Products in the 'Product' section). Assuming that the user has proper permissions,
     * the mainActions list would have {@link DefaultMainActions#ADD}
     * 
     * @param cmd the metadata for the ceiling entity that is being displayed
     * @param mainActions the actions that are added to the main form by default. Use this list to add more actions
     */
    public void modifyMainActions(ClassMetadata cmd, List<EntityFormAction> mainActions);
    
}
