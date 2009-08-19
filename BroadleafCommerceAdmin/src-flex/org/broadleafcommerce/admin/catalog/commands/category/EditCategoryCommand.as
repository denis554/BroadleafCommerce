package org.broadleafcommerce.admin.catalog.commands.category
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	import com.adobe.cairngorm.view.ViewLocator;
	
	import mx.collections.ArrayCollection;
	
	import org.broadleafcommerce.admin.catalog.control.events.category.EditCategoryEvent;
	import org.broadleafcommerce.admin.catalog.control.events.product.FindProductsByCategoryEvent;
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	import org.broadleafcommerce.admin.catalog.model.CategoryModel;
	import org.broadleafcommerce.admin.catalog.view.components.MediaCanvasViewHelper;
	import org.broadleafcommerce.admin.catalog.vo.Media;
	
	public class EditCategoryCommand implements Command
	{
		public function execute(event:CairngormEvent):void{
			trace("EditCategoryCommand.execute()");
			var ecce:EditCategoryEvent = EditCategoryEvent(event);
			var categoryModel:CategoryModel = CatalogModelLocator.getInstance().categoryModel; 			
			categoryModel.currentCategory = ecce.category;
			categoryModel.categoryMedia = new ArrayCollection();
			for (var x:String in ecce.category.categoryMedia){
				if(x is String && ecce.category.categoryMedia[x] is Media){
					var m:Media = new Media(); 
					m.id = Media(ecce.category.categoryMedia[x]).id;
					m.key = x;
					m.name = Media(ecce.category.categoryMedia[x]).name;
					m.label = Media(ecce.category.categoryMedia[x]).label;
					m.url = Media(ecce.category.categoryMedia[x]).url;
					categoryModel.categoryMedia.addItem(m);
				}
			}

			categoryModel.viewState = CategoryModel.STATE_EDIT;
			var fpbce:FindProductsByCategoryEvent = new FindProductsByCategoryEvent(ecce.category);
			fpbce.dispatch();

		}
	}
}