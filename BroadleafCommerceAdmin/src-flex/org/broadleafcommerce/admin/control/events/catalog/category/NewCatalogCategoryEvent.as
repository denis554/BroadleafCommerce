package org.broadleafcommerce.admin.control.events.catalog.category
{
	import com.adobe.cairngorm.control.CairngormEvent;

	public class NewCatalogCategoryEvent extends CairngormEvent
	{
		public static const EVENT_NEW_CATALOG_CATEGORY:String = "event_new_catalog_category";
		
		public function NewCatalogCategoryEvent()
		{
			super(EVENT_NEW_CATALOG_CATEGORY);
		}
		
	}
}