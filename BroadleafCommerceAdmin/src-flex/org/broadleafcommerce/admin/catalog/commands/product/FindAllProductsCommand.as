package org.broadleafcommerce.admin.catalog.commands.product
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.broadleafcommerce.admin.catalog.business.CatalogServiceDelegate;
	import org.broadleafcommerce.admin.catalog.control.events.BuildCatalogEvent;
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	
	public class FindAllProductsCommand implements Command, IResponder
	{
		public function execute(event:CairngormEvent):void
		{
			trace("execute : ");
			var delegate:CatalogServiceDelegate = new CatalogServiceDelegate(this);
			delegate.findAllProducts();
		}
		
		public function result(data:Object):void
		{
			var event:ResultEvent = ResultEvent(data);
			CatalogModelLocator.getInstance().productModel.catalogProducts = ArrayCollection(event.result);
			CatalogModelLocator.getInstance().productModel.filteredCatalogProducts = ArrayCollection(event.result);
			var bcte:BuildCatalogEvent = new BuildCatalogEvent();
			bcte.dispatch();
		}
		
		public function fault(info:Object):void
		{
			var event:FaultEvent = FaultEvent(info);
			Alert.show("Error: "+ event);
		}

	}
}