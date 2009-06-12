////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2006-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package mx.automation.delegates.containers 
{
import flash.display.DisplayObject;
import flash.events.Event;

import mx.automation.Automation;
import mx.automation.IAutomationObject;
import mx.automation.delegates.core.ContainerAutomationImpl;
import mx.containers.ViewStack;
import mx.core.mx_internal;
import mx.events.ChildExistenceChangedEvent;

use namespace mx_internal;

[Mixin]
/**
 * 
 *  Defines the methods and properties required to perform instrumentation for the 
 *  ViewStack class. 
 * 
 *  @see mx.containers.ViewStack
 *  
 */
public class ViewStackAutomationImpl extends ContainerAutomationImpl 
{
    include "../../../core/Version.as";
    
    //--------------------------------------------------------------------------
    //
    //  Class methods
    //
    //--------------------------------------------------------------------------


    /**
     *  Registers the delegate class for a component class with automation manager.
     *  
     *  @param root The SystemManger of the application.
     */
    public static function init(root:DisplayObject):void
    {
        Automation.registerDelegateClass(ViewStack, ViewStackAutomationImpl);
    }   

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Constructor.
     * @param obj ViewStack object to be automated.     
     */
    public function ViewStackAutomationImpl(obj:ViewStack)
    {
        super(obj);
    }

    /**
     *  @private
     *  storage for the owner component
     */
    protected function get viewStack():ViewStack
    {
        return uiComponent as ViewStack;
    }

}
}