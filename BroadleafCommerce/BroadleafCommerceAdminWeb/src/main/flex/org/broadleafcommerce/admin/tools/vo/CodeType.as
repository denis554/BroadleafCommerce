package org.broadleafcommerce.admin.tools.vo
{
	[Bindable]
	[RemoteClass(alias="org.broadleafcommerce.util.domain.CodeTypeImpl")]
	public class CodeType
	{
		public var id:int;
		public var codeType:String;
		public var key:String;
		public var description:String;
		public var modifiable:String;
	}
}