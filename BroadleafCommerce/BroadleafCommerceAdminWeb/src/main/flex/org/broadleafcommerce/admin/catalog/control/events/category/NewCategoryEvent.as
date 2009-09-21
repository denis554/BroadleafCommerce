package org.broadleafcommerce.admin.catalog.control.events.category
{
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import org.broadleafcommerce.admin.catalog.vo.category.Category;

	public class NewCategoryEvent extends CairngormEvent
	{
		public static const EVENT_NEW_CATALOG_CATEGORY:String = "event_new_catalog_category";
		
		
		public function NewCategoryEvent()
		{
			super(EVENT_NEW_CATALOG_CATEGORY);
		}
		
	}
}