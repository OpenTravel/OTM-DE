/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;

public class TypeTreeNamespaceFilter extends ViewerFilter {
    private Class<? extends AbstractLibrary> library = null;

    public TypeTreeNamespaceFilter() {
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (library == null) {
            return true;
        }
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        if (element instanceof ProjectNode)
            return true; // they contain libraries to be checked.
        final Node n = (Node) element;
        final AbstractLibrary libClass = n.getLibrary().getTLaLib();
        final boolean isInstance = library.isInstance(libClass);
        return isInstance;
    }

    public void setLibrary(final Class<? extends AbstractLibrary> lib) {
        library = lib;
    }
}
