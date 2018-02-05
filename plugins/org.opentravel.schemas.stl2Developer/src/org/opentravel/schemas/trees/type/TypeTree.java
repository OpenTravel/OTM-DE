/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;

public class TypeTree extends ViewPart implements ISelectionListener {

	private IContentProvider contentProvider = new TypeTreeContentProvider();
	private Node curNode;
	// set to 2 - 2/5/2018 - private static int ExpandLevels = 5;
	private static int ExpandLevels = 2;

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
			getSite().getPage().addSelectionListener("org.opentravel.schemas.stl2Developer.TypeTree", this);
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
