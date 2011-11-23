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

package org.broadleafcommerce.core.pricing.service.exception;

public class PricingException extends Exception {

    private static final long serialVersionUID = 1L;

    public PricingException() {
        super();
    }

    public PricingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PricingException(String message) {
        super(message);
    }

    public PricingException(Throwable cause) {
        super(cause);
    }

}
