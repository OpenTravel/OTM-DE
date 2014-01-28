/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;

public class TypeTreeNameFilter extends ViewerFilter {
    private String txtFilter = "";

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (txtFilter == null || txtFilter.isEmpty()) {
            return true;
        }
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        final Node n = (Node) element;
        if (shouldBeDisplayed(n)) {
            return true;
        }
        return childMatches(n);
    }

    private boolean childMatches(final INode cn) {
        for (final Node n : cn.getChildren()) {
            if (childMatches(n)) {
                return true;
            }
            if (shouldBeDisplayed(n)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldBeDisplayed(final Node cn) {
        return (textMatches(cn) && cn.isAssignable())
                || (cn.getParent() != null && textMatches(cn.getParent()));
    }

    private boolean textMatches(final INode cn) {
        // return cn.getName().matches(txtFilter);
        return cn.getName().toLowerCase().contains(txtFilter);
    }

    public void setText(final String txt) {
        // txtFilter = "(?i)*" + txt + "*";
        if (txt == null) {
            txtFilter = "";
        } else {
            txtFilter = txt.toLowerCase();
        }
    }
}
