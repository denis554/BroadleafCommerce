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
package org.broadleafcommerce.admin.core.commands
{
	import com.adobe.cairngorm.commands.Command;
	import com.adobe.cairngorm.control.CairngormEvent;
	import com.adobe.cairngorm.view.ViewLocator;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.modules.IModuleInfo;
	
	import org.broadleafcommerce.admin.core.control.events.LoadModulesEvent;
	import org.broadleafcommerce.admin.core.model.AppModelLocator;
	import org.broadleafcommerce.admin.core.view.helpers.AdminContentViewHelper;
	import org.broadleafcommerce.admin.core.vo.ModuleConfig;

	public class LoadModulesCommand implements Command
	{
		private var moduleInfo:IModuleInfo;
		private var modules:ArrayCollection;
		private var i:int =0;

		public function LoadModulesCommand()
		{
		}

		public function execute(event:CairngormEvent):void
		{
			var lme:LoadModulesEvent = LoadModulesEvent(event);
			var userRoles:Object = AppModelLocator.getInstance().authModel.userPrincipal.allRoles;
			var authModules:ArrayCollection = AppModelLocator.getInstance().authModel.authenticatedModules;
			modules = lme.modules;
			AppModelLocator.getInstance().authModel.authenticatedModules = new ArrayCollection();

			for each(var module:ModuleConfig in modules){
				for each(var role:Object in userRoles){
					if(role["name"] != null && role["name"] is String){
						if(module.authenticatedRoles.indexOf(String(role["name"])) > -1){
							try{
								AppModelLocator.getInstance().authModel.authenticatedModules.addItem(module);								
							} catch (error:Error){
								Alert.show("Error loading module: "+module.label+": "+error.message);
							}
							
						}
					}
				}
			}

//			for each(var module2:ModuleConfig in AppModelLocator.getInstance().authModel.authenticatedModules){
//				AdminContentViewHelper(ViewLocator.getInstance().getViewHelper("adminContent")).loadModule(module2);
//			}

			AdminContentViewHelper(ViewLocator.getInstance().getViewHelper("adminContent")).loadModules(lme.modules);
		}


	}
}