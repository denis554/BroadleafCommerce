package org.broadleafcommerce.admin.catalog.commands.product
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	
	import org.broadleafcommerce.admin.catalog.control.events.product.EditProductEvent;
	import org.broadleafcommerce.admin.catalog.control.events.product.ViewCurrentProductEvent;
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	import org.broadleafcommerce.admin.catalog.model.ProductModel;
	import org.broadleafcommerce.admin.catalog.model.SkuModel;
	import org.broadleafcommerce.admin.catalog.vo.category.Category;
	import org.broadleafcommerce.admin.catalog.vo.sku.Sku;
	import org.broadleafcommerce.admin.core.model.AppModelLocator;
	
	public class EditProductCommand implements Command
	{
		
		public function execute(event:CairngormEvent):void{
			trace("DEBUG: execute : ");
			var ecpc:EditProductEvent = EditProductEvent(event);
			var productModel:ProductModel = CatalogModelLocator.getInstance().productModel;
			var skuModel:SkuModel = CatalogModelLocator.getInstance().skuModel;
			
			
			if(CatalogModelLocator.getInstance().productModel.currentProductChanged){
				// replace this with a real pop-up
				Alert.show("Save current Changes to product?");
				CatalogModelLocator.getInstance().productModel.currentProductChanged = false;
			}
			
			productModel.currentProduct = ecpc.product;
			var test:ArrayCollection = new ArrayCollection();
			for each(var cat:Category in productModel.currentProduct.allParentCategories){
				for each(var cat2:Category in CatalogModelLocator.getInstance().categoryModel.categoryArray){
					if(cat.id == cat2.id)
						test.addItem(cat2);
				}
			}
			productModel.selectedCategories =  test.toArray();
			//productModel.selectedCategories = 
			skuModel.viewSkus = ecpc.product.allSkus;
			productModel.viewState = ProductModel.STATE_VIEW_EDIT;
	
			if(productModel.currentProduct.allSkus.length< 0){
				productModel.currentProduct.allSkus.addItem(new Sku());
			}
			
			skuModel.currentSku = Sku(productModel.currentProduct.allSkus.getItemAt(0));

			AppModelLocator.getInstance().configModel.currentCodeTypes = CatalogModelLocator.getInstance().productModel.productMediaCodes;

			if(ecpc.switchView){
				var vcpe:ViewCurrentProductEvent = new ViewCurrentProductEvent();
				vcpe.dispatch();
			}
		}

	}
}