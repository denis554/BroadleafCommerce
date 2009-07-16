package org.broadleafcommerce.admin.catalog.control.events.category
{
	import com.adobe.cairngorm.control.CairngormEvent;

	public class ViewCategoriesEvent extends CairngormEvent
	{
		public static const EVENT_VIEW_CATEGORIES:String = "view_categories_event";
		
		public function ViewCategoriesEvent()
		{
			super(EVENT_VIEW_CATEGORIES);
		}
		
	}
}