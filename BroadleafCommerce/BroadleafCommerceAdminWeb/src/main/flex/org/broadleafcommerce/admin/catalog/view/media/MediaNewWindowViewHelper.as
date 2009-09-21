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
 package org.broadleafcommerce.admin.catalog.view.media
{
	import com.adobe.cairngorm.view.ViewHelper;
	
	import flash.events.Event;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	
	import mx.controls.Alert;
	
	import org.broadleafcommerce.admin.catalog.model.CatalogModelLocator;
	import org.broadleafcommerce.admin.catalog.model.MediaModel;
	import org.broadleafcommerce.admin.catalog.vo.media.Media;

	public class MediaNewWindowViewHelper extends ViewHelper
	{
		private var fileRef:FileReference;
		private var directory:String;
		private var urlRequest:URLRequest;
		private var media:Media;
		private const FILE_UPLOAD_URL:String = "http://localhost:8080/broadleafadmin/spring/upload";		

		public function MediaNewWindowViewHelper()
		{
			super();
		}
		public function uploadImage(directory:String, media:Media):void{
			fileRef = new FileReference();
            fileRef.addEventListener(Event.SELECT, selectHandler);
            fileRef.addEventListener(Event.COMPLETE, completeHandler);
			this.urlRequest = new URLRequest(FILE_UPLOAD_URL);			
	    	var params:URLVariables = new URLVariables();
	    	this.directory = directory;
	    	params.directory = directory;
			urlRequest.data = params;
			this.media = media;
			var mediaModel:MediaModel = CatalogModelLocator.getInstance().mediaModel;
			var fileFilter:FileFilter = new FileFilter(mediaModel.fileFilter1, mediaModel.fileFilter2);
			try {
			    var success:Boolean = fileRef.browse(new Array(fileFilter));
			} catch (error:Error) {
				Alert.show("Unable to browse for files.");
			    trace("DEBUG: Unable to browse for files.");
			}
		}
	
		private function selectHandler(event:Event):void {
		    try {
				fileRef.upload(urlRequest, "file");
		    } catch (error:Error) {
		    	Alert.show("Unable to upload file.");
		        trace("DEBUG: Unable to upload file.");
		    }
		}


		private function completeHandler(event:Event):void {
				//this.media.url = directory+fileRef.name;
				// MediaNewWindow(view).urlLabelEdit.text = directory+fileRef.name;
			CatalogModelLocator.getInstance().mediaModel.currentMedia.url = directory+fileRef.name; 			
				
				// var mlr:MediaListRenderer = MediaListRenderer(MediaCanvas(CategoryCanvas(view).categoryMediaCanvas).mediaDataGrid.itemEditorInstance);
				// mlr.handleEdit();
				// mlr.urlLabelEdit.text = directory+fileRef.name;
//				MediaCanvas(CategoryCanvas(view).categoryMediaCanvas).mediaDataGrid.editedItemPosition = MediaCanvas(CategoryCanvas(view).categoryMediaCanvas).mediaDataGrid.editedItemPosition; 	    
//			Alert.show("Upload complete");
		    trace("DEBUG: uploaded");
		}						
		
	}
}