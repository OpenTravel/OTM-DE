/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.internal.filters;

import org.eclipse.jface.viewers.IFilter;

import com.sabre.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class NodeFilter implements IFilter {

    @Override
    public final boolean select(Object toTest) {
        return select((UINode) toTest);
    }

    public abstract boolean select(UINode toTest);

}
