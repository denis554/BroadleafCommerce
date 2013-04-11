/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.payment.domain;

import java.io.Serializable;

import org.broadleafcommerce.common.encryption.EncryptionModule;

public interface Referenced extends Serializable {

    /**
     * @return the referenceNumber
     */
    public String getReferenceNumber();

    /**
     * @param referenceNumber the referenceNumber to set
     */
    public void setReferenceNumber(String referenceNumber);

    public EncryptionModule getEncryptionModule();

    public void setEncryptionModule(EncryptionModule encryptionModule);
    
    public Long getId();
    
    public void setId(Long id);

}
