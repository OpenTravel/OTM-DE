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
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
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
	private PropertyOwnerInterface actOnNode; // note - extension facets are not sub-types of facet
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
		else if (!(actOnNode instanceof FacetNode) && !(actOnNode instanceof ExtensionPointNode))
			error = "Node to be acted upon is not a component that can own properties.";
		else if (!actOnNode.isEditable())
			error = "Node to be acted upon is not editable.";
		if (!error.isEmpty())
			throw new IllegalArgumentException(error);
	}

	private void getActOnNode(Node selectedNode) {
		if (selectedNode instanceof PropertyOwnerInterface) {
			if (selectedNode instanceof SimpleFacetNode)
				actOnNode = (PropertyOwnerInterface) selectedNode.getOwningComponent().getDefaultFacet();
			else
				actOnNode = (PropertyOwnerInterface) selectedNode;
		} else {
			if (selectedNode instanceof ComplexComponentInterface)
				actOnNode = (PropertyOwnerInterface) ((ComplexComponentInterface) selectedNode).getDefaultFacet();
			else if (selectedNode instanceof PropertyNode)
				actOnNode = (PropertyOwnerInterface) selectedNode.getParent();
		}
	}

	private void getScopeNode(Node selectedNode) {
		scopeNode = OtmRegistry.getMainController().getModelNode();
		if (selectedNode.isQueryFacet())
			scopeNode = selectedNode.getOwningComponent();
		else if (selectedNode.isCustomFacet())
			scopeNode = (Node) ((BusinessObjectNode) selectedNode.getOwningComponent()).getDetailFacet();

	}

	private void getEnabledPropertyTypes(PropertyOwnerInterface actOnNode2) {
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
		return true;
	}

	@Override
	public boolean performCancel() {
		canceled = true;
		for (PropertyNode p : page.getNewProperties()) {
			actOnNode.removeProperty(p);
		}
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
