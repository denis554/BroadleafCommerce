/*
 * #%L
 * BroadleafCommerce Custom Field
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
(function($, BLCAdmin) {
    if (location.pathname.match("^" + BLC.servletContext + "/product")) {

        var className = 'org.broadleafcommerce.core.catalog.domain.Product';

        BLCAdmin.addDependentFieldHandler(
            className,
            '#field-pricingModel',
            '#field-defaultSku--retailPrice, #field-defaultSku--salePrice, #field-defaultSku--cost',
            function(value, $container) {
                return value !== "ITEM_SUM";
            }
        );
    }
})(jQuery, BLCAdmin);
