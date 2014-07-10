package com.github.ajaxsys.jdtx.actions;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * No Recursive action. <br>
 * Check if contains sub call hierarchy only.
 */
public class FindMethodCallersNoRecursive extends FindMethodCallers implements IWorkbenchWindowActionDelegate {
	/**
     * The constructor.
     */
    public FindMethodCallersNoRecursive() {
    	super.isRecursive = false;
    }
}