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
package org.broadleafcommerce.admin.catalog.commands.product
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	
	import org.broadleafcommerce.admin.catalog.business.CatalogServiceDelegate;
	import org.broadleafcommerce.admin.catalog.control.events.product.FindProductsByCategoryEvent;
	import org.broadleafcommerce.admin.catalog.control.events.product.RemoveProductEvent;
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	import org.broadleafcommerce.admin.catalog.model.ProductModel;
	import org.broadleafcommerce.admin.catalog.vo.product.Product;

	public class RemoveProductCommand implements Command, IResponder
	{
		public function RemoveProductCommand()
		{
		}

		public function execute(event:CairngormEvent):void
		{
			var rpe:RemoveProductEvent = RemoveProductEvent(event);
			var delegate:CatalogServiceDelegate = new CatalogServiceDelegate(this);
			delegate.removeProduct(rpe.product);			
		}


		public function result(data:Object):void{
			var productModel:ProductModel = CatalogModelLocator.getInstance().productModel; 
			productModel.currentProductChanged = false;
			productModel.currentProduct = new Product();
			var fpbce:FindProductsByCategoryEvent = new FindProductsByCategoryEvent(CatalogModelLocator.getInstance().categoryModel.currentCategory);
			fpbce.dispatch();			
		}
		
		public function fault(info:Object):void{
			var event:FaultEvent = FaultEvent(info);
			Alert.show("Error: " + event);
		}
		
	}
}