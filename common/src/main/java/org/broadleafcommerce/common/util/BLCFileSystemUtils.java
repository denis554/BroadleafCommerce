/*
 * #%L
 * BroadleafCommerce Common Libraries
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
package org.broadleafcommerce.common.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jon Fleschler (jfleschler)
 */
public class BLCFileSystemUtils {

    public static String getClasspathFileContents(String filePath) {
        String contents = null;

        try {
            org.springframework.core.io.Resource resource = new ClassPathResource(filePath);
            if (resource.exists()) {
                InputStream stream = resource.getInputStream();
                contents = IOUtils.toString(stream);
            }

            return contents;
        } catch (IOException e) {
            return null;
        }
    }

    public static InputStream getClasspathFileInputStream(String filePath) {
        try {
            org.springframework.core.io.Resource resource = new ClassPathResource(filePath);
            return resource.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }
}
