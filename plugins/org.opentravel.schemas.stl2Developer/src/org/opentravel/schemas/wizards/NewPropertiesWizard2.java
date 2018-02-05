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
package org.opentravel.schemas.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard to allow the user to add element, attribute or indicator properties. Redesign of NewPropertiesWizard to make
 * more self contained and act directly upon the selected Object or Facet which assures that type assignment has the
 * full content for its validation rules.
 * 
 * Handles cancel by removing newly added properties. Returns via getter array list of new properties.
 * 
 * @author Dave Hollander
 * 
 */
public class NewPropertiesWizard2 extends ValidatingWizard implements Cancelable {
	private final static Logger LOGGER = LoggerFactory.getLogger(NewPropertiesWizard2.class);

	private NewPropertiesWizardPage2 page;
	private boolean canceled;
	private final List<PropertyNodeType> enabledTypes = new ArrayList<PropertyNodeType>();
	private ViewerFilter propertyFilter;
	private FacetInterface actOnNode; // note - extension facets are not sub-types of facet
	private Node scopeNode;

	final static String pageName = "Add property";
	final static String pageTitle = "Copy or create new properties.";

	/**
	 */
	public NewPropertiesWizard2(final Node selectedNode) throws IllegalArgumentException {
		getActOnNode(selectedNode);
		getScopeNode(selectedNode);
		getEnabledPropertyTypes(actOnNode);
		checkPreConditions();
	}

	private void checkPreConditions() throws IllegalArgumentException {
		String error = "";
		if (actOnNode == null)
			error = "Node to be acted upon is null.";
		else if (actOnNode instanceof VWA_SimpleFacetFacadeNode)
			error = "Node to be acted upon is not a component that can own properties.";
		else if (actOnNode instanceof CoreSimpleFacetNode)
			error = "Node to be acted upon is not a component that can own properties.";
		else if (!actOnNode.getOwningComponent().isEditable())
			error = "Node to be acted upon is not editable.";
		if (!error.isEmpty())
			throw new IllegalArgumentException(error);
	}

	private void getActOnNode(Node selectedNode) {
		if (selectedNode instanceof VWA_SimpleFacetFacadeNode)
			actOnNode = ((ComponentNode) selectedNode.getOwningComponent()).getFacet_Default();
		else if (selectedNode instanceof CoreSimpleFacetNode)
			actOnNode = ((ComponentNode) selectedNode.getOwningComponent()).getFacet_Default();
		else if (selectedNode instanceof FacetOwner)
			actOnNode = ((FacetOwner) selectedNode).getFacet_Default();
		else if (selectedNode instanceof FacetInterface)
			actOnNode = (FacetInterface) selectedNode;
		else if (selectedNode instanceof PropertyNode)
			getActOnNode(selectedNode.getParent());
		// actOnNode = ((Node) selectedNode.getOwningComponent()).getFacet_Default();
		else
			actOnNode = null;
	}

	// if (selectedNode instanceof PropertyOwnerInterface) {
	// if (selectedNode instanceof VWA_SimpleFacetFacadeNode)
	// actOnNode = ((ComponentNode) selectedNode.getOwningComponent()).getFacet_Default();
	// else if (selectedNode instanceof CoreSimpleFacetNode)
	// actOnNode = ((ComponentNode) selectedNode.getOwningComponent()).getFacet_Default();
	// else
	// actOnNode = (PropertyOwnerInterface) selectedNode;
	// } else {
	// if (selectedNode instanceof FacetOwner)
	// actOnNode = ((FacetOwner) selectedNode).getFacet_Default();
	// else if (selectedNode instanceof PropertyNode)
	// actOnNode = (PropertyOwnerInterface) selectedNode.getParent();
	// }
	// }

	private void getScopeNode(Node selectedNode) {
		scopeNode = OtmRegistry.getMainController().getModelNode();
		// if (selectedNode instanceof QueryFacetNode)
		// scopeNode = selectedNode.getOwningComponent();
		// else if (selectedNode instanceof CustomFacetNode)
		// scopeNode = selectedNode.getOwningComponent();
		// scopeNode = (Node) ((BusinessObjectNode) selectedNode.getOwningComponent()).getFacet_Detail();

	}

	private void getEnabledPropertyTypes(FacetInterface actOnNode2) {
		enabledTypes.clear();
		if (actOnNode2 == null)
			return;
		if (actOnNode2.getOwningComponent() instanceof VWA_Node)
			enabledTypes.addAll(PropertyNodeType.getVWA_PropertyTypes());
		else
			enabledTypes.addAll(PropertyNodeType.getAllTypedPropertyTypes());
	}

	@Override
	public void addPages() {
		page = new NewPropertiesWizardPage2(pageName, pageTitle, getValidator(), enabledTypes, actOnNode, scopeNode);
		page.setPropertyFilter(propertyFilter);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
		cleanFacet();
		return true;
	}

	/**
	 * The content provider could leave inherited or where used in facet, remove them.
	 */
	private void cleanFacet() {
		ArrayList<Node> kids = new ArrayList<Node>(actOnNode.getChildren());
		for (Node kid : kids) {
			if (kid instanceof AliasNode)
				continue;
			if (!(kid instanceof PropertyNode) || kid.isInherited())
				actOnNode.getChildren().remove(kid);
		}
	}

	@Override
	public boolean performCancel() {
		canceled = true;
		for (PropertyNode p : page.getNewProperties()) {
			actOnNode.removeProperty(p);
		}
		cleanFacet();
		return true;
	}

	public void run(final Shell shell) {
		final WizardDialog dialog = new WizardDialog(shell, this);
		dialog.setPageSize(SWT.DEFAULT, 350);
		dialog.create();
		dialog.open();
	}

	@Override
	public boolean wasCanceled() {
		return canceled;
	}

	/**
	 * @return the last node selected from the property tree.
	 */
	public PropertyNode getSelectedNode() {
		return page.getSelectedNode();
	}

	/**
	 * @return the list of newly created properties. NOTE: these are not attached to any specific object or library.
	 */
	public List<PropertyNode> getNewProperties() {
		return page.getNewProperties();
	}

	public void setPropertyFilter(final ViewerFilter filter) {
		propertyFilter = filter;

	}

}
