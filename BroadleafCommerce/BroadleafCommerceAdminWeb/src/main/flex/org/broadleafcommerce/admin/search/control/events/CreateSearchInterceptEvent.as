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
	import org.broadleafcommerce.admin.search.vo.SearchIntercept;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	
	public class CreateSearchInterceptEvent extends CairngormEvent
	{
		
		public var searchIntercept:SearchIntercept;
		public static const EVENT_CREATE_SEARCH_INTERCEPT:String = "event_create_search_intercept";
		
		public function CreateSearchInterceptEvent(searchIntercept:SearchIntercept)
		{
			super(EVENT_CREATE_SEARCH_INTERCEPT);
			this.searchIntercept = searchIntercept;
		}
		
	}
}