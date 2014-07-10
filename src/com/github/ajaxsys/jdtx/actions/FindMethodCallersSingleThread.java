package com.github.ajaxsys.jdtx.actions;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Recursive Single Thread Action.
 */
public class FindMethodCallersSingleThread extends FindMethodCallers implements IWorkbenchWindowActionDelegate {
	/**
     * The constructor.
     */
    public FindMethodCallersSingleThread() {
    	super.isMultiThread = false;
    }
}