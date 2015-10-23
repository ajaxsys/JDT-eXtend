package jdtx.actions;

/**
 * Recursive but Single Thread Action.
 */
public class FindMethodCallersSingleThreadNoRecursiveFromTextInput extends FindMethodBase {
    public FindMethodCallersSingleThreadNoRecursiveFromTextInput() {
    	super.isMultiThread = false;
    	super.isRecursive = false;
    }

    @Override
    String getText() {
        return super.getTextFromDialog();
    }
}