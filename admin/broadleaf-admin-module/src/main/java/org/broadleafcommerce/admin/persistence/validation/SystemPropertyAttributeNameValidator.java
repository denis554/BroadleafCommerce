/*
 * #%L
 * BroadleafCommerce Advanced CMS
 * %%
 * Copyright (C) 2009 - 2015 Broadleaf Commerce
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
package org.broadleafcommerce.admin.persistence.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.util.BLCMessageUtils;
import org.broadleafcommerce.openadmin.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.PropertyValidationResult;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.ValidationConfigurationBasedPropertyValidator;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates that a SystemProperty's AttributeName field does not contain a reserved key word surrounded by ".".
 *  AttributeNames such as "should.not.fail" will be converted to "should__not__fail" by JSCompatibilityHelper.
 *  This will later lead to a Thymeleaf exception when it attempts to process #fields.hasErrors('fields[should__not__fail].value')
 *  in entityForm.html.
 * 
 * 
 * @author Chris Kittrell (ckittrell)
 */
@Component("blSystemPropertyAttributeNameValidator")
public class SystemPropertyAttributeNameValidator extends ValidationConfigurationBasedPropertyValidator {

    protected static final Log LOG = LogFactory.getLog(SystemPropertyAttributeNameValidator.class);

    private static final String[] reservedKeyWords = { "not", "and", "or", "gt", "lt", "ge", "le", "eq", "ne" };
    private static final String RESERVED_WORD_ERROR_MESSAGE = "SystemPropertyImpl_name_reservedWordError";
    private static final String DISALLOWED_CHARACTERS_ERROR_MESSAGE = "SystemPropertyImpl_name_disallowedCharactersError";

    @Override
    public PropertyValidationResult validate(Entity entity, Serializable instance, Map<String, FieldMetadata> entityFieldMetadata,
            Map<String, String> validationConfiguration, BasicFieldMetadata propertyMetadata, String propertyName,
            String value) {
        String attributeName = entity.findProperty("name") == null ? null : entity.findProperty("name").getValue();

        if (attributeName != null) {
            Matcher matcher = Pattern.compile("\\s").matcher(attributeName);
            boolean containsWhiteSpace = matcher.find();

            if (containsWhiteSpace || !attributeName.replaceAll("\\.", "").matches("([a-zA-Z0-9])\\w+")) {
                return new PropertyValidationResult(false, getDisallowedCharactersErrorMesssage());
            }

            for (String reservedKeyWord: reservedKeyWords) {
                if (attributeName.contains("." + reservedKeyWord + ".")) {
                    return new PropertyValidationResult(false, getReservedWordErrorMessage() + " '" + reservedKeyWord + "'.");
                }
            }
        }

        return new PropertyValidationResult(true);
    }

    private String getReservedWordErrorMessage() {
        return BLCMessageUtils.getMessage(RESERVED_WORD_ERROR_MESSAGE);
    }

    private String getDisallowedCharactersErrorMesssage() {
        return BLCMessageUtils.getMessage(DISALLOWED_CHARACTERS_ERROR_MESSAGE);
    }
}
