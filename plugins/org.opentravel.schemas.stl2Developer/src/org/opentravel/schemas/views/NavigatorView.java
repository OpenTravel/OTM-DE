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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.NavigatorMenus;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.library.LibraryPropertyFilter;
import org.opentravel.schemas.trees.library.LibraryTreeContentProvider;
import org.opentravel.schemas.trees.library.LibraryTreeInheritedFilter;
import org.opentravel.schemas.trees.library.LibraryTreeNameFilter;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Agnieszka Janowska
 * 
 *         TODO - clean up current/previous node
 * 
 */
public class NavigatorView extends OtmAbstractView implements ISelectionChangedListener, IDoubleClickListener {

	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.NavigatorView";

	private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorView.class);

	private NavigatorMenus navigatorMenus = null;
	private Text filterText;
	private LibraryTreeNameFilter textFilter;
	private LibraryPropertyFilter propFilter;
	private LibraryTreeInheritedFilter inheritedFilter;

	private Node curNode;
	private Node prevNode;
	private final List<Node> selectedNodes = new LinkedList<Node>();

	private boolean propertiesDisplayed = false;
	private boolean inheritedPropertiesDisplayed = false;

	public NavigatorView() {
		// LOGGER.info("Constructor for " + this.getClass());
		OtmRegistry.registerNavigatorView(this);
	}

	@Override
	public boolean activate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearFilter() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		filterText.setText("");
		textFilter.setText("");
	}

	@Override
	public void clearSelection() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		selectedNodes.clear();
		navigatorMenus.setSelection(null);
	}

	@Override
	public void collapse() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		navigatorMenus.collapseAll();
	}

	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());
		if (parent == null)
			throw new IllegalArgumentException("Can not create part without a parent composite.");

		final MainWindow mainWindow = getMainWindow();

		final GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);

		final GridData textGD = new GridData();
		textGD.horizontalAlignment = SWT.FILL;
		textGD.grabExcessHorizontalSpace = true;

		filterText = WidgetFactory.createText(parent, SWT.BORDER);
		filterText.setLayoutData(textGD);
		filterText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				textFilter.setText(filterText.getText());
				// refreshAllViews();
				refresh();
			}
		});

		final GridData treeGD = new GridData();
		treeGD.horizontalAlignment = SWT.FILL;
		treeGD.grabExcessHorizontalSpace = true;
		treeGD.verticalAlignment = SWT.FILL;
		treeGD.grabExcessVerticalSpace = true;

		navigatorMenus = new NavigatorMenus(parent, getSite());

		// Content provider set in NavigatorMenus
		// Set up Filters
		textFilter = new LibraryTreeNameFilter();
		inheritedFilter = new LibraryTreeInheritedFilter();
		propFilter = new LibraryPropertyFilter();
		// start out with filters on
		navigatorMenus.addFilter(textFilter);
		navigatorMenus.addFilter(inheritedFilter);
		navigatorMenus.addFilter(propFilter);

		navigatorMenus.getTree().setLayoutData(treeGD);

		getSite().setSelectionProvider(navigatorMenus);
		// was -- getSite().setSelectionProvider(getTreeView());

		// Set up Drag-n-Drop.
		// TODO: DragSource created should be disposed with the MainWindow!
		mc.getHandlers().enableDragSource(navigatorMenus.getControl(), mainWindow);
		// Enable drop onto the tree
		mc.getHandlers().enableDropTarget(navigatorMenus.getControl(), mc.getActions(), OtmActions.importToTree(),
				mc.getWidgets());

		// posts dialog to get file, opens it and creates a catalog
		final ModelNode node = mc.getModelController().getModel();
		if (node != null) {
			mc.setModelNode(node);
		}

		attachSelectionListener();

		navigatorMenus.addDoubleClickListener(this);

		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	private void attachSelectionListener() {
		navigatorMenus.addSelectionChangedListener(this);
		navigatorMenus.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (OtmRegistry.getExampleView() != null)
					OtmRegistry.getExampleView().setCurrentNode(extractFirstNode(event.getSelection()));
			}
		});
	}

	@Override
	public void doubleClick(DoubleClickEvent dcEvent) {
		// if double click on where used go to type
		Node node = null;
		Node n = null;
		if (dcEvent.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) dcEvent.getSelection();
			if (ss.getFirstElement() instanceof Node)
				node = (Node) ss.getFirstElement();
		}
		if (node instanceof TypeUserNode)
			n = (Node) ((TypeUserNode) node).getOwner();
		else if (node instanceof WhereUsedNode)
			n = node.getParent();
		else if (node instanceof VersionNode)
			n = (((VersionNode) node).getNewestVersion());
		else
			n = node;
		if (n instanceof ContextualFacetNode)
			n = (((ContextualFacetNode) n).getWhereContributed().getOwningComponent());

		if (n != null) {
			setCurrentNode(n);
			select(n);
			navigatorMenus.doubleClickNotification();
			mc.selectNavigatorNodeAndRefresh(n);
		} else {
			remove(node);
		}
	}

	@Override
	public void expand() {
		if (!getMainWindow().hasDisplay())
			return;
		navigatorMenus.expandToLevel(6);
	}

	public void expand(Node node) {
		navigatorMenus.expandToLevel(node, 6);
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
			return new ArrayList<Node>(curNode.getDescendants_LibraryMembers());
		}
		return new ArrayList<Node>(selectedNodes);
	}

	@Override
	public boolean isShowInheritedProperties() {
		return inheritedPropertiesDisplayed;
	}

	/** ********************** Refresh ************************* **/

	@Override
	public void refresh() {
		if (navigatorMenus != null) {
			navigatorMenus.preservingSelection(new Runnable() {

				@Override
				public void run() {
					navigatorMenus.refresh();
				}
			});
		}
	}

	/**
	 * Updates the given node's presentation when one or more of its properties changes. Only the given element is
	 * updated. This handles structural changes (e.g. addition or removal of elements), and updating any other related
	 * elements (e.g. child elements).
	 * 
	 * @param n
	 */
	@Override
	public void refresh(INode n) {
		if (navigatorMenus == null)
			return; // happens when headless and during initial load
		if (n == null)
			return;
		navigatorMenus.refresh(n.getType()); // update the where used count
		navigatorMenus.refresh(n); // update structure
	}

	/**
	 * Refresh tree display to the current object.
	 */
	public void refreshNode(Node n, final boolean expand) {
		if (n == null)
			return; // happens when click is on facet separator bar
		if (navigatorMenus == null)
			return; // happens when headless and during initial load
		if (shouldParentBeDisplayed(n)) {
			n = n.getParent();
		}
		navigatorMenus.refresh(n.getType()); // update the where used count
		navigatorMenus.refreshNode(n, expand);
	}

	/**
	 * Select the navigator view node. Generates a selection event.
	 * 
	 * @param n
	 */
	@Override
	public void select(INode n) {
		if (navigatorMenus == null)
			return; // happens when headless and during initial load
		if (n == null || n.isDeleted())
			return;

		// // Show the node under the aggregate in the chain instead of in library.
		// if (n.getParentAggregate() != null)
		// n = n.getParentAggregate();
		navigatorMenus.refreshNode((Node) n, false);
		// 1/19/17 - navigatorMenus.refreshNode((Node) n, true);
	}

	/**
	 * Select the navigator view node. Generates a selection event.
	 * 
	 * @param n
	 */
	public void select(List<Node> nodes) {
		if (navigatorMenus == null)
			return; // happens when headless and during initial load

		setSelection(new StructuredSelection(nodes));
	}

	private void setSelection(IStructuredSelection selection) {
		navigatorMenus.setSelection(selection, true);
		applySelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers
	 * .SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final IStructuredSelection iss = (IStructuredSelection) event.getSelection();
		applySelection(iss);
	}

	private void applySelection(final IStructuredSelection iss) {
		// LOGGER.debug("nav view - apply selection event - run navigation to "+(Node)
		// iss.getFirstElement());
		if (iss.getFirstElement() == null)
			return;

		if (iss.getFirstElement() instanceof Node)
			curNode = (Node) iss.getFirstElement();
		if (curNode instanceof VersionNode)
			curNode = ((VersionNode) curNode).get();

		selectedNodes.clear();
		for (final Object o : iss.toList()) {
			if (o instanceof Node) {
				if (o instanceof VersionNode)
					selectedNodes.add(((VersionNode) o).get());
				else
					selectedNodes.add((Node) o);
				if (((Node) o).getLibrary() != null)
					mc.postStatus(((Node) o).getEditStatusMsg());
			}
		}

		// It this node has warnings or errors, post the findings
		if (!ValidationManager.isValidNoWarnings(curNode)) {
			ValidationResultsView view = mc.getView_Validation();
			if (view != null)
				view.setFindings(ValidationManager.validate(curNode), curNode);
		}

	}

	@Override
	public void setCurrentNode(final INode n) {
		prevNode = curNode;
		curNode = (Node) n;
		// LOGGER.debug("Navigator view cur node set to: " + curNode + " and prev = " + prevNode);

	}

	/**
	 * Set the property type filter.
	 * 
	 * @param on
	 */
	@Override
	public void setDeepPropertyView(final boolean on) {
		if (navigatorMenus == null)
			return; // happens during initial load
		this.propertiesDisplayed = on;
		((LibraryTreeContentProvider) navigatorMenus.getContentProvider()).setDeepMode(propertiesDisplayed);
		if (propertiesDisplayed) {
			navigatorMenus.removeFilter(propFilter);
			// navigatorMenus
			// .setContentProvider(new LibraryTreeWithPropertiesContentProvider(inheritedPropertiesDisplayed));
		} else {
			navigatorMenus.addFilter(propFilter);
			// navigatorMenus.setContentProvider(new LibraryTreeContentProvider());
		}
		navigatorMenus.refresh(OtmRegistry.getTypeView().getCurrentNode());
		OtmRegistry.getTypeView().refreshAllViews();
		// LOGGER.debug("SetDeepProperty  - properties? "+propertiesDisplayed+ " inherited? " +
		// inheritedPropertiesDisplayed);
	}

	@Override
	public void setExactMatchFiltering(boolean exactMatchFiltering) {
		if (navigatorMenus == null)
			return; // happens during initial load
		textFilter.setExactFiltering(exactMatchFiltering);
		refreshAllViews();
	}

	@Override
	public void setFocus() {
		navigatorMenus.getTree().setFocus();
	}

	/**
	 * Set the inherited properties filter.
	 * 
	 * @param on
	 */
	@Override
	public void setInheritedPropertiesDisplayed(final boolean on) {
		if (navigatorMenus == null)
			return; // happens during initial load
		this.inheritedPropertiesDisplayed = on;
		if (inheritedPropertiesDisplayed)
			navigatorMenus.removeFilter(inheritedFilter);
		else
			navigatorMenus.addFilter(inheritedFilter);
		// LOGGER.debug("SetInheritedProp - properties? "+propertiesDisplayed+ " inherited? " +
		// inheritedPropertiesDisplayed);
	}

	@Override
	public void setInput(final INode n) {
		if (!getMainWindow().hasDisplay()) {
			curNode = (Node) n;
			return;
		}
		navigatorMenus.setInput(n);
	}

	@Override
	public void remove(INode node) {
		navigatorMenus.remove(node);
	}

	/**
	 * @param n
	 * @return
	 */
	public boolean shouldParentBeDisplayed(Node n) {
		return n != null && (n instanceof PropertyNode && !(n instanceof RoleNode)) && !propertiesDisplayed;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	/**
	 * Check if given node exist in current model under {@link NavigatorView}. As a side effect this method will expand
	 * whole tree from node to root.
	 * 
	 * @param node
	 * @return true if node can be selected
	 */
	public boolean isReachable(Node node) {
		// force recreate if never expanded before
		navigatorMenus.expandToLevel(node, 0);
		return navigatorMenus.testFindItem(node) != null; // NOTE - this uses a testing hook!
	}

	/**
	 * @return true for non-empty filter text.
	 */
	public boolean isFilterActive() {
		return !filterText.getText().isEmpty();
	}

}
