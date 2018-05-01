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
package org.opentravel.schemas.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryMenus;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryRootNsNode;
import org.opentravel.schemas.trees.repository.RepositoryTreeContentProvider.RepositoryTreeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Present a view of the repositories. Uses Repository Manager as its primary model. Uses Repository Controller as the
 * controller. Tree Content from RepositoryTreeContentProvider which works with RepositoryNodes
 * 
 * @author Dave Hollander
 * 
 */

public class RepositoryView extends OtmAbstractView implements ISelectionListener, ISelectionChangedListener,
		ITreeViewerListener {

	public static final String VIEW_ID = "org.opentravel.schemas.stl2Developer.RepositoryView";

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryView.class);

	private RepositoryMenus repositoryMenus = null;
	private TreeViewer viewer = null;

	private Node curNode;
	private Node prevNode;

	public RepositoryView() {
		// LOGGER.info("Constructor for " + this.getClass());
		OtmRegistry.registerRepositoryView(this);
	}

	@Override
	public boolean activate() {
		return false;
	}

	@Override
	public void clearFilter() {
	}

	@Override
	public void clearSelection() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		viewer.setSelection(null);
	}

	@Override
	public void collapse() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		viewer.collapseAll();
	}

	@Override
	public void expand() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		viewer.expandAll();
	}

	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());
		if (parent == null)
			throw new IllegalArgumentException("Can not create part without a parent composite.");

		final MainWindow mainWindow = getMainWindow();

		final GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);

		final GridData treeGD = new GridData();
		treeGD.horizontalAlignment = SWT.FILL;
		treeGD.grabExcessHorizontalSpace = true;
		treeGD.verticalAlignment = SWT.FILL;
		treeGD.grabExcessVerticalSpace = true;

		// contains the tree viewer
		repositoryMenus = new RepositoryMenus(parent, getSite());
		viewer = repositoryMenus.getViewer();

		// // Set up Filters
		// textFilter = new LibraryTreeNameFilter();
		// inheritedFilter = new LibraryTreeInheritedFilter();
		// propFilter = new LibraryPropertyOnlyFilter();
		// // start out with filters on
		// viewer.addFilter(textFilter);
		// viewer.addFilter(inheritedFilter);
		// viewer.addFilter(propFilter);

		// 8/1/2015 viewer.setComparator(new ViewerComparator());
		viewer.setComparator(new RepositoryTreeComparator());

		viewer.getTree().setLayoutData(treeGD);

		// mainMenus = new DeveloperMenus(mainWindow, parent);

		getSite().setSelectionProvider(viewer);
		// was -- getSite().setSelectionProvider(getTreeView());

		// Set up Drag-n-Drop.
		// TODO: DragSource created should be disposed with the MainWindow!
		mc.getHandlers().enableDragSource(viewer.getControl(), mainWindow);
		// Enable drop onto the tree
		// mc.getHandlers().enableDropTarget(repositoryMenus.getControl(),
		// mc.getActions(), OtmActions.importToTree(), mc.getWidgets());

		// set the input to the repository root node.
		viewer.setInput(mc.getRepositoryController().getRoot());

		viewer.addSelectionChangedListener(this);
		getSite().getPage().addSelectionListener(VIEW_ID, this);

		viewer.expandToLevel(2);
		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	@Override
	public Node getCurrentNode() {
		return curNode;
	}

	@Override
	public INode getPreviousNode() {
		return prevNode;
	}

	/**
	 * @return a new list of the currently selected nodes, possibly empty.
	 */
	@Override
	public List<Node> getSelectedNodes() {
		if (!getMainWindow().hasDisplay() && curNode != null) {
			// provide random content for testing
			return new ArrayList<Node>(curNode.getLibrary().getDescendants_LibraryMembersAsNodes());
		}
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Iterator<?> iter = selection.iterator();
		List<Node> ret = new ArrayList<Node>();
		while (iter.hasNext()) {
			Node node = (Node) iter.next();
			ret.add(node);
		}
		return ret;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	/** ********************** Refresh ************************* **/

	@Override
	public void refresh() {
		refresh(null);
	}

	/**
	 * Updates the given node's presentation when one or more of its properties changes. Only the given element is
	 * updated. This handles structural changes (e.g. addition or removal of elements), and updating any other related
	 * elements (e.g. child elements).
	 * 
	 * @param n
	 */
	@Override
	public void refresh(INode node) {
		//
		// TEST - make sure the forced refresh is used for repository operations.
		//
		// set the input to the repository root node.
		// if (viewer == null)
		// return; // happens when headless and during initial load
		// Object[] expanded = viewer.getExpandedElements();
		// if (node == null) {
		// viewer.refresh();
		// } else {
		// viewer.refresh(node);
		// }
		// viewer.setExpandedElements(expanded);
	}

	@Override
	public void refresh(final INode node, boolean force) {
		// set the input to the repository root node.
		if (!force || viewer == null)
			return; // happens when headless and during initial load
		Object[] expanded = viewer.getExpandedElements();
		viewer.getTree().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (node == null) {
					viewer.refresh();
				} else {
					viewer.refresh(node);
				}
			}
		});
		// viewer.setExpandedElements(expanded);

	}

	/**
	 * Select the navigator view node. Generates a selection event.
	 * 
	 * @param n
	 */
	public void select(final Node n) {
		if (viewer == null)
			return; // happens when headless and during initial load
		if (n != null) {
			viewer.setSelection(new StructuredSelection(n), true);
			viewer.expandToLevel(n, 0);
			viewer.update(n, null);
			// LOGGER.debug("Nav View Select() : " + n);
		}
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		// 8/1/2015 dmh - Expand paths and chains to show libraries on selection
		if (selection instanceof TreeSelection)
			if (((TreeSelection) selection).getFirstElement() instanceof RepositoryChainNode
					|| ((TreeSelection) selection).getFirstElement() instanceof RepositoryRootNsNode)
				if (viewer.getExpandedState(((TreeSelection) selection).getFirstElement()))
					viewer.collapseToLevel(((TreeSelection) selection).getFirstElement(), 1);
				else
					viewer.expandToLevel(((TreeSelection) selection).getFirstElement(), 1);
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final IStructuredSelection iss = (IStructuredSelection) event.getSelection();
		// LOGGER.debug("Selection Changed = " + iss.getFirstElement());
		if (iss.getFirstElement() == null)
			return;

		if (iss.getFirstElement() instanceof Node)
			curNode = (Node) iss.getFirstElement();

	}

	@Override
	public void setCurrentNode(final INode n) {
		prevNode = curNode;
		curNode = (Node) n;
	}

	@Override
	public void setFocus() {
		select(getCurrentNode());
		viewer.getTree().setFocus();
	}

	@Override
	public void setInput(final INode n) {
		if (!getMainWindow().hasDisplay()) {
			curNode = (Node) n;
			return;
		}
		viewer.setInput(n);
	}

	public RepositoryMenus getRepositoryMenus() {
		return repositoryMenus;
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
	}

}
