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
package org.broadleafcommerce.rules.service;

import java.io.File;

import org.broadleafcommerce.rules.domain.ShoppingCartPromotion;
import org.drools.rule.Package;

public interface RuleService {

	public ShoppingCartPromotion saveShoppingCartPromotion(ShoppingCartPromotion shoppingCartPromotion);

	public Package addRuleToNewPackage(File drlFile);

	public void mergePackageWithRuleBase(Package pkg);

	public void writeRuleFile(ShoppingCartPromotion shoppingCartPromotion, String logicalOperator);

}
