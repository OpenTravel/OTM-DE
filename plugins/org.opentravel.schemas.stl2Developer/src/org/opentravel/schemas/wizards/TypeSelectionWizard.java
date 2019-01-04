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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTreeVersionSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard to allow user to select a type for the passed node objects. Selected type is assigned if wizard returns true.
 * 
 * Uses the passed node or first node of the list to set filters for simple/complex/vwa types.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeSelectionWizard extends Wizard implements IDoubleClickListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeSelectionWizard.class);

	private Node curNode = null;
	private TypeSelectionPage selectionPage;
	private WizardDialog dialog;

	// A simple data container for the strings in a page
	private class WindowStringsDTO {
		private static final String PREFIX = "wizard.typeSelection.";
		private static final String PN = "pageName.";
		private static final String T = "title.";
		private static final String D = "description.";

		// Default messages
		private static final String C = "component";
		String pageName = Messages.getString(PREFIX + PN + C);
		String title = Messages.getString(PREFIX + T + C);
		String description = Messages.getString(PREFIX + D + C);

		private void set(String suffix) {
			pageName = Messages.getString(PREFIX + PN + suffix);
			title = Messages.getString(PREFIX + T + suffix);
			description = Messages.getString(PREFIX + D + suffix);
		}
	}

	/**
	 * Type selection wizard to select a node to assign as a type.
	 * 
	 * @param n
	 *            the node to assign to. Type of node selects the filter to use on tree view.
	 */
	public TypeSelectionWizard(final Node n) {
		super();
		// Make sure node is non-null and editable
		if (n != null && n.isEditable())
			curNode = n;
	}

	// Create a type selection page. Called by jface methods
	@Override
	public void addPages() {
		// LOGGER.debug("Adding Selection Page.");

		// Default messages
		WindowStringsDTO strings = new WindowStringsDTO();

		// Except for special cases, delegate getting filter to nodes
		TypeSelectionFilter filter = null;

		// Special cases - set messages and filters for special cases
		if (curNode instanceof ResourceNode) {
			strings.set("resource");
		} else if (curNode.getLaterVersions() != null) {
			filter = new TypeTreeVersionSelectionFilter(curNode);
		} else if (curNode instanceof ServiceNode) {
			strings.set("service");
		} else if (curNode instanceof ChoiceFacetNode) {
			strings.set("contextualChoiceFacet");
		} else if (curNode instanceof ContextualFacetNode) {
			strings.set("contextualFacet"); // Order is important since choice is contextualFacet
		} else if (curNode instanceof LibraryNode) {
			strings.set("library");
		}

		// Get a type selection filter if not already set
		if (filter == null)
			filter = curNode.getTypeSelectionFilter();

		// Create the selection page
		selectionPage = new TypeSelectionPage(strings.pageName, strings.title, strings.description, null, curNode);
		selectionPage.setTypeSelectionFilter(filter);
		selectionPage.addDoubleClickListener(this);

		addPage(selectionPage);

		// Testing Guide
		// 1. Resource - messages and only business objects
		// 2. Older minor version - find an object that is in minor version that has properties that have later minor
		// versions.
		// 3. Service - business object only
		// 4. Contextual facets - filter with correct type variable set
		// 4.a. choice
		// 4.b. query
		// 4.c. custom
		// 4.d - copy CF into different library
		// 5. ID Reference
		// 6. VWA Property
		// 6.a VWA as type to VWA
		// 7. VWA ID Reference property
		// 8. Attribute
		// 9. Simple Object
		// 5. Library - library where used menu: change provider library

		// FIXME - errors testing 2) older minor versions
		// FIXME - navigator state after library change provider action
	}

	// According to the link, this belongs in the wizard not page
	// http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet047WizardWithLongRunningOperation.java?view=markup
	@Override
	public boolean canFinish() {
		return selectionPage.getSelectedNode() != null;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		if (canFinish()) {
			performFinish();
			dialog.close();
		}
	}

	/**
	 * @return the setNodeList which is the filtered copy of the source list
	 */
	public Node getSelection() {
		return selectionPage == null ? null : selectionPage.getSelectedNode();
	}

	// This code is in the AssignTypeAction.execute().
	@Override
	public boolean performFinish() {
		return getSelection() != null;
	}

	/**
	 * Run the wizard but DO NOT assign the types. Usage if (wizard.run(OtmRegistry.getActiveShell())) {
	 * AssignTypeAction.execute(wizard.getList(), wizard.getSelection()); }
	 * 
	 * @return true if selecting made, false if cancelled
	 */
	public boolean run(final Shell shell) {
		if (curNode == null) {
			LOGGER.warn("Early Exit - no node(s) to post.");
			return false; // DO Nothing
		}

		dialog = new WizardDialog(shell, this);
		dialog.setPageSize(700, 600);
		dialog.create();
		int result = dialog.open();
		return result == org.eclipse.jface.window.Window.OK;
	}
}
