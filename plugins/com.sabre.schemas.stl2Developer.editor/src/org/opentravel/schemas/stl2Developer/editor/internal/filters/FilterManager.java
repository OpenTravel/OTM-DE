/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IFilter;

/**
 * @author Pawel Jedruch
 * 
 */
public class FilterManager implements IFilter {

    private List<IFilter> filters = new ArrayList<IFilter>();

    public void addFilter(IFilter filter) {
        filters.add(filter);
    }

    public void removeFilter(IFilter filter) {
        filters.remove(filter);
    }

    @Override
    public boolean select(Object toTest) {
        if (filters.isEmpty())
            return true;

        for (IFilter f : filters) {
            if (f.select(toTest))
                return true;
        }
        return false;
    }

}
