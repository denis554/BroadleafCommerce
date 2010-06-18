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
package org.broadleafcommerce.admin.search.control.events
{
	import com.adobe.cairngorm.control.CairngormEvent;

	public class FindAllSearchInterceptsEvent extends CairngormEvent
	{
		
		public static const EVENT_FIND_ALL_SEARCH_INTERCEPTS:String = "event_find_all_search_intercepts";
		
		public function FindAllSearchInterceptsEvent()
		{
			super(EVENT_FIND_ALL_SEARCH_INTERCEPTS);
		}
		
	}
}