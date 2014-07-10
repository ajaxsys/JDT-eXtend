package com.github.ajaxsys.jdtx.utils;

import java.util.ArrayList;

public class IgnoreCaseArrayList extends ArrayList<String> {
	private static final long serialVersionUID = -4161635315287598869L;

	@Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

	@Override
    public int indexOf(Object o) {
		String paramStr = (String)o;

		Object[] elementData = this.toArray();
		int size = elementData.length;

        if (paramStr == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (paramStr.equalsIgnoreCase((String)elementData[i]))
                    return i;
        }
        return -1;
    }

	@Override
    public boolean add(String e) {
		if (!this.contains(e))
			super.add(e);
        return true;
    }

}