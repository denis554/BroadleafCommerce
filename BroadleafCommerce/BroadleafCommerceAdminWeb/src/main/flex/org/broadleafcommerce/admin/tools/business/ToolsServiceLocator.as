package org.broadleafcommerce.admin.tools.business
{
	import com.adobe.cairngorm.CairngormError;
	import com.adobe.cairngorm.CairngormMessageCodes;
	import com.adobe.cairngorm.business.ServiceLocator;
	
	import mx.rpc.remoting.mxml.RemoteObject;
	
	import org.broadleafcommerce.admin.tools.model.ToolsModel;
	
	public class ToolsServiceLocator
	{
		private static var _instance:ToolsServiceLocator;

		private static var myService:RemoteObject;
		
		/**
		 * Return the ServiceLocator instance.
		 * @return the instance.
		*/
		public static function get instance():ToolsServiceLocator{
			if(!_instance){
	        	_instance = new ToolsServiceLocator();
	        }
	        return _instance;
        }

		/**
		 * Return the ServiceLocator instance.
		 * @return the instance.
		 */
		public static function getInstance():ToolsServiceLocator{
			return instance;
		}
		
		// Constructor should be private but current AS3.0 does not allow it
		public function ToolsServiceLocator(){
			if( _instance){
			   throw new CairngormError(CairngormMessageCodes.SINGLETON_EXCEPTION, "BroadleafCommerceAdminToolsServiceLocator" );
			}
			_instance = this;
		}
		
		public function getService():RemoteObject{
			if(myService == null){				
				myService = new mx.rpc.remoting.mxml.RemoteObject(); 
				var adminService:RemoteObject = mx.rpc.remoting.mxml.RemoteObject((ServiceLocator.getInstance().getRemoteObject("blcAdminService")));
				myService.concurrency = "multiple";
				myService.endpoint = adminService.endpoint;
				myService.showBusyCursor = true; 
				myService.destination = ToolsModel.SERVICE_ID;
			}
			return myService;
		}
	}
}