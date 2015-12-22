package com.github.ajaxsys.jdtx.actions;

/**
 * No Recursive action. <br>
 * Check if contains sub call hierarchy only.
 */
public class FindMethodCallersNoRecursiveFromTextInput extends FindMethodCallers {
    public FindMethodCallersNoRecursiveFromTextInput() {
        super.isRecursive = false;
    }

    @Override
    String getText() {
        return super.getTextFromDialog();
    }
}