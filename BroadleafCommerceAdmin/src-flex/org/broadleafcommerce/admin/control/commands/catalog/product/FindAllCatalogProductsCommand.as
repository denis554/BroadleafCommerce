package org.broadleafcommerce.admin.control.commands.catalog.product
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.broadleafcommerce.admin.model.AppModelLocator;
	import org.broadleafcommerce.admin.model.business.BroadleafCommerceAdminServiceDelegate;
	
	public class FindAllCatalogProductsCommand implements Command, IResponder
	{
		public function execute(event:CairngormEvent):void
		{
			var delegate:BroadleafCommerceAdminServiceDelegate = new BroadleafCommerceAdminServiceDelegate(this);
			delegate.findAllProducts();
		}
		
		public function result(data:Object):void
		{
			var event:ResultEvent = ResultEvent(data);
			AppModelLocator.getInstance().productModel.catalogProducts = ArrayCollection(event.result);
		}
		
		public function fault(info:Object):void
		{
			var event:FaultEvent = FaultEvent(info);
			Alert.show("Error: "+ event);
		}

	}
}