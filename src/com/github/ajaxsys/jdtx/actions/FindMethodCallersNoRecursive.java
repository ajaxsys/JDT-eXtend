package com.github.ajaxsys.jdtx.actions;

/**
 * No Recursive action. <br>
 * Check if contains sub call hierarchy only.
 */
public class FindMethodCallersNoRecursive extends FindMethodCallers {
    public FindMethodCallersNoRecursive() {
    	super.isRecursive = false;
    }
}