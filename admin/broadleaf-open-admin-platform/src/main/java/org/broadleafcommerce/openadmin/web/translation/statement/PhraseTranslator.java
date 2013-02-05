/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.openadmin.web.translation.statement;

import org.broadleafcommerce.openadmin.web.translation.BLCOperator;
import org.broadleafcommerce.openadmin.web.translation.MVELTranslationException;

/**
 * @author jfischer
 * @author Elbert Bautista (elbertbautista)
 */
public class PhraseTranslator {

    private static final String[] SPECIALCASES = {
            ".startsWith",
            ".endsWith",
            ".contains"
    };

    private static final String[] STANDARDOPERATORS = {
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
        String caseInsensitivityKey = "MVEL.eval(\"toUpperCase()\",";
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
        if (value.startsWith("[") && value.endsWith("]")) {
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
        String dateFormatKey = "java.text.DateFormat.getDateTimeInstance(3,3).parse(";
        if (value.startsWith(dateFormatKey)) {
            value = value.substring(dateFormatKey.length(), value.length()-1);
        }
        int entityKeyIndex = field.indexOf(".");
        if (entityKeyIndex < 0) {
            throw new MVELTranslationException("Could not identify a valid property field value " +
                    "in the expression: ("+phrase+")");
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
        //check to see if there's a method call on this field
        int methodPos = field.lastIndexOf(".");
        int parenthesisPos = -1;
        if (methodPos >= 0) {
            parenthesisPos = field.indexOf("()", methodPos);
        }
        if (methodPos >= 0 && parenthesisPos >= 0) {
            field = field.substring(0, methodPos);
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
        for (String operator : STANDARDOPERATORS) {
            String[] temp = phrase.split(operator);
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
                    throw new MVELTranslationException("Could not parse the MVEL expression to a " +
                            "compatible form for the rules builder (" + phrase + ")");
                }
            }
        }
        return components;
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
        int startsWithIndex = components[0].indexOf(key);
        temp[0] = components[0].substring(0, startsWithIndex);
        temp[1] = key.substring(1, key.length());
        temp[2] = components[0].substring(startsWithIndex + key.length() + 1, components[0].lastIndexOf(")"));
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
        }
        throw new MVELTranslationException("Unable to identify an operator compatible with the " +
                "rules builder: ("+(isNegation?"!":""+field+operator+value)+")");
    }

}
