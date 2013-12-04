/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.type;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.trees.library.LibraryTreeLabelProvider;

public class TypeTree extends ViewPart implements ISelectionListener {

    private IContentProvider contentProvider = new TypeTreeContentProvider();
    private Node curNode;
    private static int ExpandLevels = 4;

    public TypeTree(final Node n, IContentProvider contentProvider) {
        super();
        curNode = n;

        if (contentProvider != null) {
            this.contentProvider = contentProvider;
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        final TreeViewer typeTree = setUpViewer(parent);
        if (getSite() != null) {
            getSite().setSelectionProvider(typeTree);
            getSite().getPage().addSelectionListener("com.sabre.schemas.stl2Developer.TypeTree",
                    this);
        }
    }

    /**
     * Set up the viewer for use when parentNode is not the window shell.
     * 
     * @param parentNode
     * @param n
     */
    public TreeViewer setUpViewer(final Composite parent) {
        TreeViewer typeTree;

        typeTree = new TreeViewer(parent);
        typeTree.setContentProvider(contentProvider);
        typeTree.setLabelProvider(new LibraryTreeLabelProvider());
        typeTree.setSorter(new TypeTreeSorter());
        // typeTree.addSelectionChangedListener(new TypeTreeSelectionChanged()); // Debugging output
        // from selection-change listener

        if (curNode != null) {
            typeTree.setInput(curNode);
        }
        typeTree.expandToLevel(ExpandLevels);
        return typeTree;
    }

    public void setInput(final Node n) {
        curNode = n;
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
    }

    @Override
    public void setFocus() {
    }
}
