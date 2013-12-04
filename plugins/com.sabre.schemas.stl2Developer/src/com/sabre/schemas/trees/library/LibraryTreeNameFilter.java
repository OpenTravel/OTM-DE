/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.library;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.properties.PropertyNode;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTreeNameFilter extends ViewerFilter {

    private String txtFilter = "";
    private boolean exactFiltering = false;

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (txtFilter == null || txtFilter.isEmpty()) {
            return true;
        }
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        final INode n = (INode) element;
        if (shouldBeDisplayed(n)) {
            return true;
        }
        return childMatches(n);
    }

    private boolean childMatches(final INode cn) {
        for (final INode n : cn.getChildren()) {
            if (childMatches(n)) {
                return true;
            }
            if (shouldBeDisplayed(n)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldBeDisplayed(final INode cn) {
        return textMatches(cn) || assignmentMatches(cn);
    }

    private boolean textMatches(final INode cn) {
        if (cn.getName() == null) {
            return false;
        }
        if (isExactFiltering()) {
            return cn.getName().toLowerCase().startsWith(txtFilter);
        }
        return cn.getName().toLowerCase().contains(txtFilter);
    }

    private boolean assignmentMatches(final INode cn) {
        boolean matches = false;
        if (!isExactFiltering() && cn instanceof PropertyNode) {
            PropertyNode p = (PropertyNode) cn;
            String assignedType = p.getTypeNameWithPrefix();
            matches = assignedType != null && assignedType.toLowerCase().contains(txtFilter);
        }
        return matches;
    }

    public void setText(final String txt) {
        if (txt == null) {
            txtFilter = "";
        } else {
            txtFilter = txt.toLowerCase();
        }
    }

    /**
     * @return the exactFiltering
     */
    public boolean isExactFiltering() {
        return exactFiltering;
    }

    /**
     * @param exactFiltering
     *            if true filter will consider only nodes which names start with filter text;
     *            otherwise filter will also try to match any names that contain (not only start
     *            with) given text and also assigned types (whether their names match filter)
     */
    public void setExactFiltering(boolean exactFiltering) {
        this.exactFiltering = exactFiltering;
    }
}
