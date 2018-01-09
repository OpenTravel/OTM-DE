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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.opentravel.schemas.navigation.DoubleClickSelection;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * Master view containing {@link FacetView} and {@link PropertiesView}
 * 
 * @author Dave Hollander
 * 
 */
public class TypeView extends OtmAbstractView implements ISelectionListener {
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.TypeView";
	// private static final Logger LOGGER = LoggerFactory.getLogger(TypeView.class);

	private FacetView facetView;
	private PropertiesView propertiesView;
	private INode currentNode;
	private boolean listening = true; // flag that implements linked behavior

	public TypeView() {
		OtmRegistry.registerTypeView(this);
		facetView = new FacetView();
		propertiesView = new PropertiesView();

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		facetView.init(site);
		propertiesView.init(site);
	}

	@Override
	public void createPartControl(final Composite parent) {

		// LOGGER.info("Initializing part control of " + this.getClass());
		if (parent == null)
			throw new IllegalArgumentException("Can not create part without a parent composite.");

		final SashForm mainSashForm = new SashForm(parent, SWT.HORIZONTAL);
		mainSashForm.setSashWidth(2);

		facetView.initialize(mainSashForm);
		propertiesView.initialize(mainSashForm);
		getSite().setSelectionProvider(facetView.getSelectionProvider());
		getSite().getPage().addSelectionListener(VIEW_ID, propertiesView);
		getSite().getPage().addSelectionListener(NavigatorView.VIEW_ID, this);
		select(mc.getCurrentNode_NavigatorView());

		mainSashForm.setWeights(new int[] { 1, 1 });

		Node node = mc.getCurrentNode_NavigatorView();
		setCurrentNode(node, true);

		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (NavigatorView.VIEW_ID.equals(part.getSite().getId()) && !selection.isEmpty()) {
			INode node = extractFirstNode(selection);
			if (node instanceof VersionNode)
				node = ((VersionNode) node).get();

			if (selection instanceof DoubleClickSelection) {
				// force view refresh event if not listen
				setCurrentNode(node, true);
			} else {
				setCurrentNode(node);
			}
		}
	}

	/**
	 * @return the current node in the facet table
	 */
	@Override
	public Node getCurrentNode() {
		return facetView.getCurrentNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.views.OtmAbstractView#getPreviousNode()
	 */
	@Override
	public INode getPreviousNode() {
		return propertiesView.getPreviousNode();
	}

	@Override
	public List<Node> getSelectedNodes() {
		return facetView.getSelectedNodes();
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public boolean isListening() {
		return listening;
	}

	public void setFacetViewFocus(int i) {
		facetView.setFocus(i);
	}

	@Override
	public void refresh() {
		facetView.refresh();
		propertiesView.refresh();
	}

	@Override
	public void refresh(INode node) {
		refresh(node, false);
	}

	@Override
	public void refresh(INode node, boolean force) {
		if (isListening()) {
			facetView.refresh(node, force);
			propertiesView.refresh(node, force);
		}
	}

	@Override
	public void refresh(boolean regenerate) {
		if (regenerate) {
			facetView.setCurrentNode(currentNode);
			propertiesView.setCurrentNode(currentNode);
		}
		this.refresh();
	}

	@Override
	public void restorePreviousNode() {
		if (listening) {
			propertiesView.postPrevProperties();
			facetView.restorePreviousNode();
		}
	}

	private void setCurrentNode(final INode node, final boolean force) {
		if (listening || force) {
			// if (listening || force || ((Node) currentNode).getOwningComponent() == ((Node)
			// node).getOwningComponent()) {
			currentNode = node;
			facetView.setCurrentNode(node); // Force??
			propertiesView.setCurrentNode(node);
		}
		// LOGGER.debug("Type view cur node set to: " + currentNode + " and prev = " + facetView.getPreviousNode());

		// TODO - is current node used? if so, propertyView may need to set it.
	}

	/**
	 * Set the node to be displayed in the facet table and properties pane. NOTE - Obeys the listening flag
	 */
	@Override
	public void setCurrentNode(INode node) {
		setCurrentNode(node, false);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void setListening(final boolean listening) {
		this.listening = listening;
		if (listening)
			setCurrentNode(currentNode, false);
	}

	@Override
	public void select(INode node) {
		setCurrentNode(node);
	}

}
