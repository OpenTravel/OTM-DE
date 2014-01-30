/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.trees.documentation;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.DocumentationNode;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DocumentationTreeContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(final Object element) {
        return getChildren(element);
    }

    @Override
    public Object[] getChildren(final Object element) {
        Object[] toRet = null;
        if (element instanceof DocumentationNode) {
            DocumentationNode node = (DocumentationNode) element;
            List<DocumentationNode> navChildren = node.getChildren();
            toRet = navChildren != null ? navChildren.toArray() : null;
        }
        return toRet;
    }

    @Override
    public boolean hasChildren(final Object element) {
        if (element instanceof DocumentationNode) {
            DocumentationNode node = (DocumentationNode) element;
            return !node.getChildren().isEmpty();
        }
        return false;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof DocumentationNode) {
            DocumentationNode node = (DocumentationNode) element;
            return node.getParent();
        }
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
    }

}
