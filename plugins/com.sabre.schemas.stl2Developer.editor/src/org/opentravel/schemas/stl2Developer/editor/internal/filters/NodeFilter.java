/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.filters;

import org.eclipse.jface.viewers.IFilter;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

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
