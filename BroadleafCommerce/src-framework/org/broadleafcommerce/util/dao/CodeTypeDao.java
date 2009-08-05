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
package org.broadleafcommerce.util.dao;

import java.util.List;

import org.broadleafcommerce.util.domain.CodeType;

public interface CodeTypeDao {

    public List<CodeType> readAllCodeTypes();

    public CodeType readCodeTypeById(Long codeTypeId);

    public CodeType readCodeTypeByKey(String key);

    public CodeType save(CodeType codeType);

    public void delete(CodeType codeType);

    public CodeType create();
}
