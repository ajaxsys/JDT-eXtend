package com.github.ajaxsys.jdtx.actions;

/**
 * Recursive but Single Thread Action.
 */
public class FindMethodCallersSingleThread extends FindMethodCallers {
    public FindMethodCallersSingleThread() {
    	super.isMultiThread = false;
    }
}