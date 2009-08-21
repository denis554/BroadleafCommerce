package org.broadleafcommerce.admin.core.model
{
	import com.adobe.cairngorm.CairngormError;
	import com.adobe.cairngorm.CairngormMessageCodes;
	import com.adobe.cairngorm.model.IModelLocator;
	
	import org.broadleafcommerce.admin.core.model.ToolsModel;
	

	public class AppModelLocator implements IModelLocator
	{
		private static var modelLocator:AppModelLocator;


		public static function getInstance():AppModelLocator
		{
			if(modelLocator == null)
				modelLocator = new AppModelLocator();
			
			return modelLocator;
		}
		
		public function AppModelLocator()
		{
			if(modelLocator != null)
				throw new CairngormError(CairngormMessageCodes.SINGLETON_EXCEPTION, "BlcAdminModelLocator");				
		}
		
		public var authModel:AuthenticationModel = new AuthenticationModel();
		
		public var configModel:ConfigModel = new ConfigModel();
		
	}
}