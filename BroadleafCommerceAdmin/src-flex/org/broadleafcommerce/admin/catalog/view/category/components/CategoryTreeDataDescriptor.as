package org.broadleafcommerce.admin.catalog.view.category.components
{
	import mx.collections.ICollectionView;
	import mx.controls.treeClasses.ITreeDataDescriptor;
	
	import org.broadleafcommerce.admin.catalog.control.events.category.SaveCategoryEvent;
	import org.broadleafcommerce.admin.catalog.vo.category.Category;

	public class CategoryTreeDataDescriptor implements ITreeDataDescriptor
	{
		public function CategoryTreeDataDescriptor()
		{
		}

		public function getChildren(node:Object, model:Object=null):ICollectionView
		{
			return Category(node).allChildCategories;
		}
		
		public function hasChildren(node:Object, model:Object=null):Boolean
		{
			//return (Category(node).allChildCategories.length > 0);
			for each(var object:Object in Category(node).allChildCategories){
				if(object is Category){
					return true;
				}
			}
			return false;
		}
		
		public function isBranch(node:Object, model:Object=null):Boolean
		{
			return (node is Category);
		}
		
		public function getData(node:Object, model:Object=null):Object
		{
			return Category(node);
		}
		
		public function addChildAt(parent:Object, newChild:Object, index:int, model:Object=null):Boolean
		{
			var childCat:Category = Category(newChild);
			var parentCat:Category = Category(parent);
			if(parentCat == null && model != null && model[0] != null){
				parentCat = 	Category(model[0])	
			}
			childCat.allParentCategories.addItem(parentCat);
			parentCat.allChildCategories.addItemAt(childCat,index);				
		
			var scce:SaveCategoryEvent = new SaveCategoryEvent(childCat);
			scce.dispatch();
		
			return true;
		}
		
		public function removeChildAt(parent:Object, child:Object, index:int, model:Object=null):Boolean
		{
			var parentCat:Category = Category(parent);
			parentCat.allChildCategories.removeItemAt(index);;			
			var childCat:Category = Category(child);
			for (var i:String  in childCat.allParentCategories){
				if(parentCat.id == Category(childCat.allParentCategories[i]).id){
					childCat.allParentCategories.removeItemAt(int(i));
				}
			}
			return true;
		}
		
	}
}