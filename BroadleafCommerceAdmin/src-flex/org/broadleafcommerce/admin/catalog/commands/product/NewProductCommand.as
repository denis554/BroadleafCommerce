package org.broadleafcommerce.admin.catalog.commands.product
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	import org.broadleafcommerce.admin.catalog.model.ProductModel;
	import org.broadleafcommerce.admin.catalog.vo.product.Product;
	import org.broadleafcommerce.admin.core.model.AppModelLocator;

	public class NewProductCommand implements Command
	{
		
		public function execute(event:CairngormEvent):void
		{
			CatalogModelLocator.getInstance().productModel.currentProduct = new Product();
			CatalogModelLocator.getInstance().productModel.viewState = ProductModel.STATE_VIEW_EDIT;
		}
		
	}
}