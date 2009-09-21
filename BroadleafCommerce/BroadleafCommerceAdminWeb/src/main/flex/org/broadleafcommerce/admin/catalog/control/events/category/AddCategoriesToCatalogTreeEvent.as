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
package org.broadleafcommerce.admin.catalog.control.events.category
{
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.collections.ArrayCollection;

	public class AddCategoriesToCatalogTreeEvent extends CairngormEvent
	{
		public static const EVENT_ADD_CATEGORIES_TO_CATALOG_TREE:String = "add_categories_to_catalog_tree_event";
		
		public var catalogTree:ArrayCollection;
		public var categoryArray:ArrayCollection;

		public function AddCategoriesToCatalogTreeEvent(catalogTree:ArrayCollection, categoryArray:ArrayCollection)
		{
			super(EVENT_ADD_CATEGORIES_TO_CATALOG_TREE);
			this.catalogTree = catalogTree;
			this.categoryArray = categoryArray;
		}
		
	}
}