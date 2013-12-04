/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;

import com.sabre.schemas.node.Node;

public class TypeTreeVWASimpleTypeOnlyFilter extends TypeSelectionFilter {

    /**
     * @see com.sabre.schemas.typeTree.TypeSelectionFilter#isValidSelection(com.sabre.schemas.node.Node)
     */
    @Override
    public boolean isValidSelection(Node n) {
        return (n != null) && n.isAssignable() && n.isVWASimpleAssignable();
    }

    /**
     * Establish the filter to select only nodes that match the node.library.
     */
    public TypeTreeVWASimpleTypeOnlyFilter() {
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        final Node n = (Node) element;
        return (n.isNavigation()) ? true : n.isVWASimpleAssignable();
    }

}
