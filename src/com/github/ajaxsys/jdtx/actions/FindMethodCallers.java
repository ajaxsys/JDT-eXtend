package com.github.ajaxsys.jdtx.actions;

import java.io.FileNotFoundException;

public class FindMethodCallers extends FindMethodBase {

    @Override
    String getText() throws FileNotFoundException {
        return super.getTextFromFile();
    }

}