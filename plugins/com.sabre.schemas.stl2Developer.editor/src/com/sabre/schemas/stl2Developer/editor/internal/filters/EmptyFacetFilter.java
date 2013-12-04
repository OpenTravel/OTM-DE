/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.internal.filters;

import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class EmptyFacetFilter extends NodeFilter {

    @Override
    public boolean select(UINode toTest) {
        if (toTest.getNode() instanceof FacetNode) {
            return toTest.getNode().hasChildren();
        }
        return true;
    }

}
