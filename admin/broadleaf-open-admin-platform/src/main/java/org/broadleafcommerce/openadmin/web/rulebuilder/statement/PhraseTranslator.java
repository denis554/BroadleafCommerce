/*
 * #%L
 * BroadleafCommerce Open Admin Platform
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
package org.broadleafcommerce.openadmin.web.rulebuilder.statement;

import org.broadleafcommerce.openadmin.server.service.persistence.module.FieldManager;
import org.broadleafcommerce.openadmin.web.rulebuilder.BLCOperator;
import org.broadleafcommerce.openadmin.web.rulebuilder.MVELTranslationException;
import org.broadleafcommerce.openadmin.web.rulebuilder.RuleBuilderFormatUtil;

import java.text.ParseException;

/**
 * @author jfischer
 * @author Elbert Bautista (elbertbautista)
 */
public class PhraseTranslator {

    private static final String COLLECTIONCASE = "CollectionUtils.intersection";

    private static final String[] SPECIALCASES = {
            "org.apache.commons.lang3.StringUtils.startsWith",
            "org.apache.commons.lang3.StringUtils.endsWith",
            "org.apache.commons.lang3.StringUtils.contains"
    };

    private static final String[] STANDARDOPERATORS = {
            ".size()>",
            ".size()>=",
            ".size()<",
            ".size()<=",
            ".size()==",
            "==",
            "!=",
            "<=",
            "<",
            ">=",
            ">"
    };

    public Expression createExpression(String phrase) throws MVELTranslationException {
        String[] components = extractComponents(phrase);
        String field = components[0];
        String operator = components[1];
        String value = components[2];

        boolean isNegation = false;
        if (field.startsWith("!")) {
            isNegation = true;
        }

        boolean isIgnoreCase = false;
        boolean isCollectionCase = false;
        if (phrase.startsWith(COLLECTIONCASE)) {
            isCollectionCase = true;
        }

        //remove null check syntax
        field = field.replaceAll("\\.\\?", ".");

        //keep for backwards compatibility with legacy generated MVEL
        String legacyCaseInsensitivityKey = "MVEL.eval(\"toUpperCase()\",";

        String newCaseInsensitivityKey = "MvelHelper.toUpperCase(";
        String caseInsensitivityKey;
        if (field.contains(legacyCaseInsensitivityKey) || value.contains(legacyCaseInsensitivityKey)) {
            caseInsensitivityKey = legacyCaseInsensitivityKey;
        } else {
            caseInsensitivityKey = newCaseInsensitivityKey;
        }
        if (field.startsWith(caseInsensitivityKey)) {
            isIgnoreCase = true;
            field = field.substring(caseInsensitivityKey.length(), field.length()-1);
        }
        //check for NOT operator
        if (field.startsWith("!" + caseInsensitivityKey)) {
            isIgnoreCase = true;
            field = field.substring(("!" + caseInsensitivityKey).length(), field.length()-1);
        }
        while(value.contains(caseInsensitivityKey)) {
            value = value.substring(0, value.indexOf(caseInsensitivityKey)) +
                    value.substring(value.indexOf(caseInsensitivityKey) + caseInsensitivityKey.length(), value.length());
            value = value.substring(0, value.indexOf(")")) + value.substring(value.indexOf(")")+1, value.length());
        }
        if (value.startsWith("[") && value.endsWith("]") && !isCollectionCase) {
            value = value.substring(1, value.length() - 1);
            String[] temps = value.split(",");
            for (int j = 0;j<temps.length;j++) {
                if (temps[j].startsWith("\"") && temps[j].endsWith("\"")) {
                    temps[j] = temps[j].substring(1, temps[j].length()-1);
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int j = 0;j<temps.length;j++) {
                sb.append(temps[j]);
                if (j < temps.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            value = sb.toString();
        }
        //keep for backwards compatibility with legacy generated MVEL
        String legacyDateFormatKey = "java.text.DateFormat.getDateTimeInstance(3,3).parse(";

        String newDateFormatKey = "MvelHelper.convertField(\"DATE\",\"";
        String dateFormatKey;
        if (value.contains(legacyDateFormatKey)) {
            dateFormatKey = legacyDateFormatKey;
        } else {
            dateFormatKey = newDateFormatKey;
        }
        if (value.startsWith(dateFormatKey)) {
            value = value.substring(dateFormatKey.length(), value.length()-1);
            //convert the date into admin display format
            try {
                if (value.startsWith("\"")) {
                    value = value.substring(1, value.length());
                }
                if (value.endsWith("\"")) {
                    value = value.substring(0, value.length()-1);
                }
                value = RuleBuilderFormatUtil.formatDate(RuleBuilderFormatUtil.parseDate(value));
            } catch (ParseException e) {
                throw new MVELTranslationException(MVELTranslationException.INCOMPATIBLE_DATE_VALUE, "Unable to convert " +
                        "the persisted date value(" + value + ") to the admin display format.");
            }
        }
        int entityKeyIndex = field.indexOf(".");
        if (entityKeyIndex < 0) {
            throw new MVELTranslationException(MVELTranslationException.NO_FIELD_FOUND_IN_RULE, "Could not identify a " +
                    "valid property field value in the expression: ("+phrase+")");
        }
        if (value.startsWith(caseInsensitivityKey)) {
            value = value.substring(caseInsensitivityKey.length(), value.length()-1);
        }
        String entityKey = field.substring(0, entityKeyIndex);
        boolean isFieldComparison = false;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length()-1);
        } else if (value.startsWith(entityKey + ".")){
            isFieldComparison = true;
            value = value.substring(entityKey.length() + 1, value.length());
        }
        field = field.substring(entityKeyIndex + 1, field.length());
        // If this is a Money field, then DataDTOToMVELTranslator.formatValue() will append .getAmount() onto the end
        // of the field name. We need to remove that as it should not be considered part of the field name, but we still
        // want to support other method invocations in MVEL expressions (like for getProductAttributes())
        String moneyAmountMethod = ".getAmount()";
        int amountMethodPos = field.lastIndexOf(moneyAmountMethod);
        if (amountMethodPos >= 0) {
            field = field.substring(0, amountMethodPos);
        }

        // Same as above, but for Enumeration types
        String typeMethod = ".getType()";
        int typeMethodPos = field.lastIndexOf(typeMethod);
        if (typeMethodPos >= 0) {
           field = field.substring(0, typeMethodPos);
        }

        Expression expression = new Expression();
        expression.setField(field);
        BLCOperator operatorId = getOperator(field, operator, value, isNegation, isFieldComparison, isIgnoreCase);
        expression.setOperator(operatorId);
        expression.setValue(value);

        return expression;
    }

    protected String[] extractComponents(String phrase) throws MVELTranslationException {
        String[] components = new String[]{};

        //If the phrase is a CollectionUtils case - this will need to be evaluated first
        //e.g. CollectionUtils.intersection(groupIds,["100","300"]).size()>0
        if (phrase.startsWith(COLLECTIONCASE)) {
            components = new String[3];
            //field
            components[0] = phrase.substring((COLLECTIONCASE+"(").length(), phrase.indexOf(","));
            //value
            components[2] = phrase.substring(((COLLECTIONCASE+"(")+components[0]+",").length(), phrase.indexOf(").size"));
            //operator
            components[1] = phrase.substring(phrase.indexOf(".size"));

            components[0] = convertMapAccessSyntax(components[0]);

            return components;
        }

        for (String operator : STANDARDOPERATORS) {
            String[] temp;
            if (phrase.contains(operator)) {
                temp = new String[2];
                temp[0] = phrase.substring(0, phrase.indexOf(operator));
                temp[1] = phrase.substring(phrase.indexOf(operator) + operator.length(), phrase.length());
            } else {
                temp = new String[1];
                temp[0] = phrase;
            }
            if (temp.length == 2) {
                components = new String[3];
                components[0] = temp[0];
                components[1] = operator;
                components[2] = temp[1];
                break;
            }
            components = temp;
        }
        if (components.length != 3) {
            //may be a special expression
            try {
                for (String key : SPECIALCASES) {
                    if (components[0].indexOf(key) >= 0) {
                        String[] temp = extractSpecialComponents(components, key);
                        components = temp;
                        break;
                    }
                }
            } catch (Exception e) {
                //do nothing
            }
            if (components.length != 3) {
                //may be a projection
                try {
                    String[] temp = extractProjection(components);
                    components = temp;
                } catch (Exception e1) {
                    //do nothing
                }

                if (components.length != 3) {
                    throw new MVELTranslationException(MVELTranslationException.UNRECOGNIZABLE_RULE, "Could not parse the MVEL expression to a " +
                            "compatible form for the rules builder (" + phrase + ")");
                }
            }
        }

        components[0] = convertMapAccessSyntax(components[0]);

        return components;
    }

    protected String convertMapAccessSyntax(String field) {
        if (field.matches(".*\\[\".*?\"\\].*")) {
            //this is using map access syntax - must be a map field
            field = field.substring(0, field.lastIndexOf("[")) + FieldManager.MAPFIELDSEPARATOR +
                    field.substring(field.lastIndexOf("[") + 2, field.lastIndexOf("]") - 1) +
                    field.substring(field.lastIndexOf("]") + 1, field.length());
            //strip any convertField usage
            if (field.startsWith("MvelHelper.convertField(")) {
                field = field.substring(field.indexOf("(") + 1, field.length() - 1);
            }
        } else if (field.matches(".*\\?get\\(\".*?\"\\)\\.\\?getValue\\(\\).*")) {
            //this is using null-safe map access syntax - must be a map field
            field = field.substring(0, field.lastIndexOf(".?get(")) + FieldManager.MAPFIELDSEPARATOR +
                    field.substring(field.lastIndexOf(".?get(") + 7, field.lastIndexOf(").?getValue()") - 1) +
                    field.substring(field.lastIndexOf(").?getValue()") + 13, field.length());
        } else if (field.matches(".*\\?get\\(\".*?\"\\)\\.\\?value.*")) {
            //this is using MVELHelper null-safe map access syntax - must be a map field
            field = field.substring(0, field.lastIndexOf(".?get(")) + FieldManager.MAPFIELDSEPARATOR +
                    field.substring(field.lastIndexOf(".?get(") + 7, field.lastIndexOf(").?value") - 1) +
                    field.substring(field.lastIndexOf(").?value") + 8, field.length());
        }

        return field;
    }

    protected String[] extractProjection(String[] components) {
        String[] temp = new String[3];
        int startsWithIndex = components[0].indexOf("contains");
        temp[0] = components[0].substring(startsWithIndex+"contains".length()+1, components[0].length()).trim();
        if (temp[0].endsWith(".intValue()")) {
            temp[0] = temp[0].substring(0, temp[0].indexOf(".intValue()"));
        }
        temp[1] = "==";
        temp[2] = components[0].substring(components[0].indexOf("["), components[0].indexOf("]") + 1);
        return temp;
    }

    protected String[] extractSpecialComponents(String[] components, String key) {
        String[] temp = new String[3];
        temp[0] = components[0].substring(key.length() + 1, components[0].indexOf(","));
        temp[1] = key.substring(37, key.length());
        temp[2] = components[0].substring(components[0].indexOf(",") + 1, components[0].lastIndexOf(")"));
        return temp;
    }

    protected BLCOperator getOperator(String field, String operator, String value, boolean isNegation,
                                     boolean isFieldComparison, boolean isIgnoreCase) throws MVELTranslationException {
        if (operator.equals("==") && value.equals("null")) {
            return BLCOperator.IS_NULL;
        } else if (operator.equals("==") && isFieldComparison) {
            return BLCOperator.EQUALS_FIELD;
        } else if (
                isIgnoreCase &&
                        operator.equals("==")
                ) {
            return BLCOperator.IEQUALS;
        } else if (operator.equals("==")) {
            return BLCOperator.EQUALS;
        } else if (operator.equals("!=") && value.equals("null")) {
            return BLCOperator.NOT_NULL;
        } else if (operator.equals("!=") && isFieldComparison) {
            return BLCOperator.NOT_EQUAL_FIELD;
        } else if (
                isIgnoreCase &&
                        operator.equals("!=")
                ) {
            return BLCOperator.INOT_EQUAL;
        } else if (operator.equals("!=")) {
            return BLCOperator.NOT_EQUAL;
        } else if (operator.equals(">") && isFieldComparison) {
            return BLCOperator.GREATER_THAN_FIELD;
        } else if (operator.equals(">")) {
            return BLCOperator.GREATER_THAN;
        } else if (operator.equals("<") && isFieldComparison) {
            return BLCOperator.LESS_THAN_FIELD;
        } else if (operator.equals("<")) {
            return BLCOperator.LESS_THAN;
        } else if (operator.equals(">=") && isFieldComparison) {
            return BLCOperator.GREATER_OR_EQUAL_FIELD;
        } else if (operator.equals(">=")) {
            return BLCOperator.GREATER_OR_EQUAL;
        } else if (operator.equals("<=") && isFieldComparison) {
            return BLCOperator.LESS_OR_EQUAL_FIELD;
        } else if (operator.equals("<=")) {
            return BLCOperator.LESS_OR_EQUAL;
        } else if (
                isIgnoreCase &&
                        operator.equals("contains") &&
                        isNegation
                ) {
            return BLCOperator.INOT_CONTAINS;
        } else if (operator.equals("contains") && isNegation) {
            return BLCOperator.NOT_CONTAINS;
        } else if (
                isIgnoreCase &&
                        operator.equals("contains")
                ) {
            return BLCOperator.ICONTAINS;
        } else if (operator.equals("contains") && isFieldComparison) {
            return BLCOperator.CONTAINS_FIELD;
        } else if (operator.equals("contains")) {
            return BLCOperator.CONTAINS;
        } else if (
                isIgnoreCase &&
                        operator.equals("startsWith") &&
                        isNegation
                ) {
            return BLCOperator.INOT_STARTS_WITH;
        } else if (operator.equals("startsWith") && isNegation) {
            return BLCOperator.NOT_STARTS_WITH;
        } else if (
                isIgnoreCase &&
                        operator.equals("startsWith")
                ) {
            return BLCOperator.ISTARTS_WITH;
        } else if (operator.equals("startsWith") && isFieldComparison) {
            return BLCOperator.STARTS_WITH_FIELD;
        } else if (operator.equals("startsWith")) {
            return BLCOperator.STARTS_WITH;
        } else if (
                isIgnoreCase &&
                        operator.equals("endsWith") &&
                        isNegation
                ) {
            return BLCOperator.INOT_ENDS_WITH;
        } else if (operator.equals("endsWith") && isNegation) {
            return BLCOperator.NOT_ENDS_WITH;
        } else if (
                isIgnoreCase &&
                        operator.equals("endsWith")
                ) {
            return BLCOperator.IENDS_WITH;
        } else if (operator.equals("endsWith") && isFieldComparison) {
            return BLCOperator.ENDS_WITH_FIELD;
        } else if (operator.equals("endsWith")) {
            return BLCOperator.ENDS_WITH;
        } else if (operator.equals(".size()>")) {
            return BLCOperator.COUNT_GREATER_THAN;
        } else if (operator.equals(".size()>=")) {
            return BLCOperator.COUNT_GREATER_OR_EQUAL;
        } else if (operator.equals(".size()<")) {
            return BLCOperator.COUNT_LESS_THAN;
        } else if (operator.equals(".size()<=")) {
            return BLCOperator.COUNT_LESS_OR_EQUAL;
        } else if (operator.equals(".size()==")) {
            return BLCOperator.COUNT_EQUALS;
        } else if (operator.equals(".size()>0")){
            return BLCOperator.COLLECTION_IN;
        } else if (operator.equals(".size()==0")){
            return BLCOperator.COLLECTION_NOT_IN;
        }
        throw new MVELTranslationException(MVELTranslationException.OPERATOR_NOT_FOUND, "Unable to identify an operator compatible with the " +
                "rules builder: ("+(isNegation?"!":""+field+operator+value)+")");
    }

}
