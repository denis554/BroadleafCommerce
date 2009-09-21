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
package org.broadleafcommerce.admin.catalog.control.events.sku
{
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import org.broadleafcommerce.admin.catalog.vo.sku.Sku;

	public class EditSkuEvent extends CairngormEvent
	{
		public static const EVENT_EDIT_SKU:String = "edit_sku_event";
		
		public var sku:Sku;
		public var showSkusView:Boolean;
		
		public function EditSkuEvent(sku:Sku, showSkusView:Boolean)
		{
			super(EVENT_EDIT_SKU);
			this.sku = sku;
			this.showSkusView = showSkusView;
		}
		
	}
}