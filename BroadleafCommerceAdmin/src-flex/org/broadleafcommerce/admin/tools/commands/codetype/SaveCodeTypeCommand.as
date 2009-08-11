package org.broadleafcommerce.admin.tools.commands.codetype
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	
	import org.broadleafcommerce.admin.tools.business.BroadleafCommerceAdminToolsServiceDelegate;
	import org.broadleafcommerce.admin.tools.control.events.codetype.FindAllCodeTypesEvent;
	import org.broadleafcommerce.admin.tools.control.events.codetype.SaveCodeTypeEvent;
	import org.broadleafcommerce.admin.tools.vo.CodeType;
	
	public class SaveCodeTypeCommand implements Command, IResponder
	{
		public function execute(event:CairngormEvent):void{
			var scte:SaveCodeTypeEvent = SaveCodeTypeEvent(event);
			var codeType:CodeType = scte.codeType;
			var delegate:BroadleafCommerceAdminToolsServiceDelegate = new BroadleafCommerceAdminToolsServiceDelegate(this);
			delegate.saveCodeType(codeType);
		}
		
		public function result(data:Object):void{
			var facte:FindAllCodeTypesEvent = new FindAllCodeTypesEvent();
			facte.dispatch();
		}		
		
		public function fault(info:Object):void{
			var event:FaultEvent = FaultEvent(info);
			Alert.show("Error: " + event);			
		}
	}
}