package org.broadleafcommerce.admin.control
{
	import com.adobe.cairngorm.control.FrontController;
	
	import org.broadleafcommerce.admin.control.commands.offers.AddUpdateOfferCommand;
	import org.broadleafcommerce.admin.control.commands.FindAllCatalogCategoriesCommand;
	import org.broadleafcommerce.admin.control.commands.offers.FindAllOffersCommand;
	import org.broadleafcommerce.admin.control.commands.InitializeApplicationCommand;
	import org.broadleafcommerce.admin.control.commands.offers.ShowOfferWindowCommand;
	import org.broadleafcommerce.admin.control.events.AddUpdateOfferEvent;
	import org.broadleafcommerce.admin.control.events.FindAllCatalogCategoriesEvent;
	import org.broadleafcommerce.admin.control.events.FindAllOffersEvent;
	import org.broadleafcommerce.admin.control.events.InitializeApplicationEvent;
	import org.broadleafcommerce.admin.control.events.ShowOfferWindowEvent;
	
	public class BlcAdminController extends FrontController
	{
		public function BlcAdminController()
		{
			super();
			addCommand(ShowOfferWindowEvent.EVENT_SHOW_OFFER_WINDOW,ShowOfferWindowCommand);
			addCommand(AddUpdateOfferEvent.EVENT_ADD_UPDATE_OFFER,AddUpdateOfferCommand);
			addCommand(FindAllCatalogCategoriesEvent.EVENT_FIND_ALL_CATALOG_CATEGORIES,FindAllCatalogCategoriesCommand);
			addCommand(FindAllOffersEvent.EVENT_FIND_ALL_OFFERS, FindAllOffersCommand);
			addCommand(InitializeApplicationEvent.EVENT_INITIALIZE_APPLICATION, InitializeApplicationCommand);
		}
		
	}
}