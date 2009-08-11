/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.vendor.usps.service.type;

import org.broadleafcommerce.vendor.service.type.ContainerShapeType;

/**
 * An extendible enumeration of container shape types.
 * 
 * @author jfischer
 */
public class USPSContainerShapeType extends ContainerShapeType {

    private static final long serialVersionUID = 1L;

    public static USPSContainerShapeType RECTANGULAR  = new USPSContainerShapeType("RECTANGULAR");
    public static USPSContainerShapeType NONRECTANGULAR = new USPSContainerShapeType("NONRECTANGULAR");
    public static USPSContainerShapeType VARIABLE = new USPSContainerShapeType("VARIABLE");
    public static USPSContainerShapeType FLATRATEBOX = new USPSContainerShapeType("FLAT RATE BOX");
    public static USPSContainerShapeType FLATRATEENVELOPE= new USPSContainerShapeType("FLAT RATE ENVELOPE");
    public static USPSContainerShapeType LGFLATRATEBOX= new USPSContainerShapeType("LG FLAT RATE BOX");

    public USPSContainerShapeType() {
        //do nothing
    }

    public USPSContainerShapeType(String type) {
        super(type);
    }

}
