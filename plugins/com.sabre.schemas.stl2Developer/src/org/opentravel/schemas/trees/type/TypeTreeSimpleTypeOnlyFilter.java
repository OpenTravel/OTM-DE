/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;

public class TypeTreeSimpleTypeOnlyFilter extends TypeSelectionFilter {

    /**
     * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
     */
    @Override
    public boolean isValidSelection(Node n) {
        return (n != null) && n.isAssignable() && n.isSimpleAssignable();
    }

    /**
     * Establish the filter to select only nodes that match the node.library.
     */
    public TypeTreeSimpleTypeOnlyFilter() {
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        final Node n = (Node) element;
        // System.out.println("TTSimpleTypeOnlyFilter:select() - is "+n.getName()+" Simple? "+n.isSimpleAssignable());
        return (n.isNavigation()) ? true : n.isSimpleAssignable();
    }
}
