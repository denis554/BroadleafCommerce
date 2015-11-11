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
(function($, BLCAdmin) {
    
    // Add utility functions for list grids to the BLCAdmin object
    BLCAdmin.alert = {
        showAlert : function($container, message, options) {
            options = options || {};
            var alertType = options.alertType || '';
            
            var $alert = $('<span>').addClass('alert-box').addClass(alertType);
            var $closeLink = $('<a>').attr('href', '').addClass('close').html('&times;').css("display","none");
            
            $alert.append(message);
            $alert.append($closeLink);
            
            if (options.clearOtherAlerts) {
                $container.closest('.field-group').find('.alert-wrapper').find('.alert-box').remove();
            }

            $container.closest('.field-group').find('.alert-wrapper').append($alert);
            
            if (options.autoClose) {
                setTimeout(function() {
                    $closeLink.closest(".alert-box").remove();
                }, options.autoClose);
            }
        }
    };

})(jQuery, BLCAdmin);