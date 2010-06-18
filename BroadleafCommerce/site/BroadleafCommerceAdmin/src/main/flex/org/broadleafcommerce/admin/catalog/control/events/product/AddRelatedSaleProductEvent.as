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
package org.broadleafcommerce.admin.catalog.control.events.product
{
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import org.broadleafcommerce.admin.catalog.vo.product.Product;
	import org.broadleafcommerce.admin.catalog.vo.product.RelatedProduct;

	public class AddRelatedSaleProductEvent extends CairngormEvent
	{
		public static const EVENT_ADD_RELATED_PRODUCT:String = "add_related_product_event";
		
		public var relatedProduct:RelatedProduct;
		public var relatedCollectionName:String;
		public var index:int;
		
		public function AddRelatedSaleProductEvent(relatedProduct:RelatedProduct, relatedCollectionName:String, index:int = -1)
		{
			super(EVENT_ADD_RELATED_PRODUCT);
			this.relatedProduct = relatedProduct;
			this.relatedCollectionName = relatedCollectionName;
			this.index = index;
		}
		
	}
}