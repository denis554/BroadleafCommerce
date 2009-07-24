package org.broadleafcommerce.admin.security.business
{
	import com.adobe.cairngorm.CairngormError;
	import com.adobe.cairngorm.CairngormMessageCodes;
	import com.adobe.cairngorm.business.ServiceLocator;

	import mx.rpc.remoting.mxml.RemoteObject;

	import org.broadleafcommerce.admin.security.model.SecurityModel;

	public class AdminSecurityServiceLocator
	{
		private static var _instance:AdminSecurityServiceLocator;

      /**
       * Return the ServiceLocator instance.
       * @return the instance.
       */
      public static function get instance() : AdminSecurityServiceLocator
      {
         if ( ! _instance )
         {
            _instance = new AdminSecurityServiceLocator();
         }

         return _instance;
      }

      /**
       * Return the ServiceLocator instance.
       * @return the instance.
       */
      public static function getInstance() : AdminSecurityServiceLocator
      {
         return instance;
      }

      // Constructor should be private but current AS3.0 does not allow it
      public function AdminSecurityServiceLocator()
      {
         if ( _instance )
         {
            throw new CairngormError( CairngormMessageCodes.SINGLETON_EXCEPTION, "AdminSecurityServiceLocator" );
         }

         _instance = this;
      }

		public function getService():RemoteObject{
			var myService:RemoteObject = mx.rpc.remoting.mxml.RemoteObject(ServiceLocator.getInstance().getRemoteObject("blcAdminService"));
			myService.destination = SecurityModel.SERVICE_ID;
			return myService;
		}

	}
}