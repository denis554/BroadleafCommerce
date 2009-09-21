/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.admin.security.control.events
{
	import com.adobe.cairngorm.control.CairngormEvent;

	import org.broadleafcommerce.admin.core.vo.security.AdminRole;

	public class SaveRoleEvent extends CairngormEvent
	{
		public static const EVENT_SAVE_ROLE:String = "save_role_event";
		public var role:AdminRole

		public function SaveRoleEvent(role:AdminRole)
		{
			super(EVENT_SAVE_ROLE);
			this.role = role;
		}

	}
}