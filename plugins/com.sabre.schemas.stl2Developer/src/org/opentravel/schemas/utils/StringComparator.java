/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.utils;

import java.util.Comparator;

public abstract class StringComparator<T> implements Comparator<T> {

    @Override
    public final int compare(T o1, T o2) {
        String str1 = getNotNullString(o1);
        String str2 = getNotNullString(o2);
        return str1.compareToIgnoreCase(str2);
    }

    private String getNotNullString(T object) {
        String str = getString(object);
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    protected abstract String getString(T object);

}
