package org.broadleafcommerce.admin.security.commands.permissions
{
	import com.adobe.cairngorm.commands.ICommand;
	import com.adobe.cairngorm.control.CairngormEvent;

	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;

	import org.broadleafcommerce.admin.security.business.SecurityServiceDelegate;
	import org.broadleafcommerce.admin.security.model.SecurityModelLocator;

	public class FindAllPermissionsCommand implements ICommand, IResponder
	{
		public function execute(event:CairngormEvent):void
		{
			var delegate:SecurityServiceDelegate = new SecurityServiceDelegate(this);
			delegate.findAllAdminPermissions();
		}

		public function result(data:Object):void
		{
			var event:ResultEvent = ResultEvent(data);
			var permissions:ArrayCollection = event.result as ArrayCollection;
			SecurityModelLocator.getInstance().adminPermissions = permissions;
		}

		public function fault(info:Object):void
		{
			var event:FaultEvent = FaultEvent(info);
			Alert.show("Error: "+ event);
		}

	}
}