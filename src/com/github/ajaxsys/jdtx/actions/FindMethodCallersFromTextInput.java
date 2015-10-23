package com.github.ajaxsys.jdtx.actions;


public class FindMethodCallersFromTextInput extends FindMethodCallers {

    @Override
    String getText() {
        return super.getTextFromDialog();
    }
}