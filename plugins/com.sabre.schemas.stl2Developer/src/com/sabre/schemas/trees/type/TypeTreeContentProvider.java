/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.type;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.INode;

/**
 * Provides tree view content of only type providers.
 * 
 * NOTE - for library version chains, each version is included. This means that the leaf nodes will
 * not be actual type providers but version node.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeTreeContentProvider implements ITreeContentProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeTreeContentProvider.class);

    /**
	 *
	 */
    public TypeTreeContentProvider() {
    }

    @Override
    public Object[] getElements(final Object element) {
        return getChildren(element);
    }

    @Override
    public Object[] getChildren(final Object element) {
        return (((INode) element).getChildren_TypeProviders().toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang. Object)
     */
    @Override
    public boolean hasChildren(final Object element) {
        return ((INode) element).hasChildren_TypeProviders();
    }

    @Override
    public Object getParent(final Object element) {
        return ((INode) element).getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }

}
