package org.broadleafcommerce.openadmin.server.util;

import org.broadleafcommerce.openadmin.client.dto.override.FieldMetadataOverride;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
public class MetadataOverrideBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder overallMapBuilder = BeanDefinitionBuilder.rootBeanDefinition(MapFactoryBean.class);
        Map<String, BeanDefinition> overallMap = new ManagedMap<String, BeanDefinition>();
        List<Element> overrideItemElements = DomUtils.getChildElementsByTagName(element, "overrideItem");
        for (Element overrideItem : overrideItemElements) {
            String configKey = overrideItem.getAttribute("configurationKey");
            String ceilingEntity = overrideItem.getAttribute("ceilingEntity");
            String type = overrideItem.getAttribute("type");
            StringBuilder sb = new StringBuilder();
            sb.append(configKey);
            sb.append("_");
            sb.append(ceilingEntity);
            sb.append("_");
            sb.append(type);

            BeanDefinitionBuilder fieldMapBuilder = BeanDefinitionBuilder.rootBeanDefinition(MapFactoryBean.class);
            Map<String, BeanDefinition> fieldMap = new ManagedMap<String, BeanDefinition>();
            List<Element> fieldElements = DomUtils.getChildElementsByTagName(overrideItem, "field");
            for (Element fieldElement : fieldElements) {
                String fieldName = fieldElement.getAttribute("name");
                BeanDefinitionBuilder metadataBuilder = BeanDefinitionBuilder.rootBeanDefinition(FieldMetadataOverride.class);
                {
                    List<Element> propElements = DomUtils.getChildElementsByTagName(fieldElement, "property");
                    for (Element propElement : propElements) {
                        String propName = propElement.getAttribute("name");
                        String propValue = propElement.getAttribute("value");
                        metadataBuilder.addPropertyValue(propName, propValue);
                        fieldMap.put(fieldName, metadataBuilder.getBeanDefinition());
                    }
                }

                {
                    List<Element> validationElements = DomUtils.getChildElementsByTagName(fieldElement, "validation");
                    Map<String, Map<String, String>> validationConfigMap = new ManagedMap<String, Map<String, String>>();
                    for (Element validationElement : validationElements) {
                        Map<String, String> validationMap = new ManagedMap<String, String>();
                        List<Element> valPropElements = DomUtils.getChildElementsByTagName(validationElement, "property");
                        for (Element valPropElement : valPropElements) {
                            String valPropName = valPropElement.getAttribute("name");
                            String valPropValue = valPropElement.getAttribute("value");
                            validationMap.put(valPropName, valPropValue);
                        }
                        String className = validationElement.getAttribute("className");
                        validationConfigMap.put(className, validationMap);
                    }
                    metadataBuilder.addPropertyValue("validationConfigurations", validationConfigMap);
                }

                {
                    List<Element> optionElements = DomUtils.getChildElementsByTagName(fieldElement, "optionFilterValue");
                    String[][] optionFilterValues = new String[optionElements.size()][3];
                    int j = 0;
                    for (Element optionElement : optionElements) {
                        optionFilterValues[j][0] = optionElement.getAttribute("name");
                        optionFilterValues[j][1] = optionElement.getAttribute("value");
                        optionFilterValues[j][2] = optionElement.getAttribute("type");
                        j++;
                    }
                    metadataBuilder.addPropertyValue("optionFilterValues", optionFilterValues);
                }

                {
                    List<Element> keyElements = DomUtils.getChildElementsByTagName(fieldElement, "mapKey");
                    String[][] keyValues = new String[keyElements.size()][2];
                    int j = 0;
                    for (Element keyElement : keyElements) {
                        keyValues[j][0] = keyElement.getAttribute("value");
                        keyValues[j][1] = keyElement.getAttribute("displayValue");
                        j++;
                    }
                    metadataBuilder.addPropertyValue("keys", keyValues);
                }

                {
                    List<Element> children = DomUtils.getChildElementsByTagName(fieldElement, "customCriteria");
                    String[] values = new String[children.size()];
                    int j = 0;
                    for (Element childElem : children) {
                        values[j] = childElem.getAttribute("value");
                        j++;
                    }
                    metadataBuilder.addPropertyValue("customCriteria", values);
                }

                {
                    List<Element> children = DomUtils.getChildElementsByTagName(fieldElement, "maintainedAdornedTargetField");
                    String[] values = new String[children.size()];
                    int j = 0;
                    for (Element childElem : children) {
                        values[j] = childElem.getAttribute("value");
                        j++;
                    }
                    metadataBuilder.addPropertyValue("maintainedAdornedTargetFields", values);
                }

                {
                    List<Element> children = DomUtils.getChildElementsByTagName(fieldElement, "gridVisibleField");
                    String[] values = new String[children.size()];
                    int j = 0;
                    for (Element childElem : children) {
                        values[j] = childElem.getAttribute("value");
                        j++;
                    }
                    metadataBuilder.addPropertyValue("gridVisibleFields", values);
                }
            }
            fieldMapBuilder.addPropertyValue("sourceMap", fieldMap);

            overallMap.put(sb.toString(), fieldMapBuilder.getBeanDefinition());
        }

        overallMapBuilder.addPropertyValue("sourceMap", overallMap);

        return overallMapBuilder.getBeanDefinition();
    }
}
