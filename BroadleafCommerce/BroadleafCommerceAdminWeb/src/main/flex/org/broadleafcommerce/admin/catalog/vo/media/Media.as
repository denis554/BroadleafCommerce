package org.broadleafcommerce.admin.catalog.vo.media
{
	[Bindable]
	[RemoteClass(alias="org.broadleafcommerce.media.domain.MediaImpl")]	
	public class Media
	{
		public function Media():void{
			id = -1;
		}
		
		public var id:Number;
		public var name:String;
		public var url:String;
		private var _label:String;
		
		
		public var key:String;
		
		public function set label(newLabel:String):void{
			_label = newLabel;
		} 
		
		public function get label():String{
			return _label;
		}

	}
}