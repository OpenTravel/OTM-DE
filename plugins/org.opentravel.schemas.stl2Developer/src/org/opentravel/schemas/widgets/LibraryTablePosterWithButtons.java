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
package org.opentravel.schemas.widgets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LibraryTablePoster} decorated with 'Go To' buttons on types
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTablePosterWithButtons extends LibraryTablePoster {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTablePosterWithButtons.class);

	private final TableEditorButtonSet buttonSet;

	public LibraryTablePosterWithButtons(final Table table, final MainWindow mainWindow) {
		super(table, mainWindow.getColorProvider());
		MainController mc = OtmRegistry.getMainController();
		// Create a table editor button set to manage buttons added to the table.
		buttonSet = new TableEditorButtonSet(mc.getWidgets(), table, OtmActions.typeSelector(),
				OtmWidgets.typeSelector, mc.getHandlers().new ButtonSelectionHandler());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.stl2developer.LibraryTablePoster#postTableRow(org.opentravel.schemas.node.Node)
	 */
	@Override
	protected TableItem postTableRow(final Node n) {
		final TableItem item = super.postTableRow(n);

		// Version Control - If the type has a newer version then allow that to be selected
		Button button = null;

		// List<Versioned> versions = null;
		// MinorVersionHelper helper = new MinorVersionHelper();
		// TLModelElement tlObj = n.getAssignedType().getTLModelObject();
		// Node assignedType = n.getAssignedType();
		// if (tlObj instanceof Versioned && !(assignedType instanceof ImpliedNode)) {
		// try {
		// versions = helper.getLaterMinorVersions((Versioned) n.getAssignedType().getTLModelObject());
		// } catch (VersionSchemeException e) {
		// LOGGER.debug("Error: " + e.getLocalizedMessage());
		// }
		// }

		// if (NodeUtils.checker(n.getAssignedType()).isInMinorOrPatch().existInPreviousVersions().get())
		// button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the row.
		// else
		// boolean enabled = false;
		// if (n.isEditable() && n instanceof TypeUser && !n.isInheritedProperty())
		// if (n.getChain() == null || n.getChain().isMajor())
		// enabled = true; // Unmanaged or major - allow editing.
		// else if (n.getChain().isPatch())
		// enabled = false; // no changes in a patch
		// else if (n instanceof SimpleAttributeNode)
		// enabled = n.isNewToChain(); // only allow editing if owner is new to the minor
		// else if (n.isInHead2())
		// enabled = true;
		// else if (n.getLaterVersions() != null)
		// enabled = true; // If the assigned type has a newer version then allow them to select that.

		if (n.isEnabled_AssignType())
			button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the row.

		// if (n.isEditable() && n instanceof TypeUser && !n.isInheritedProperty())
		// if ((n.getChain() == null || n.getChain().isMajor())
		// || !NodeUtils.checker(n).isInMinorOrPatch().existInPreviousVersions().get())
		// //button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the row.
		//
		// if (!NodeUtils.checker(n).isInMinorOrPatch().existInPreviousVersions().get()
		// && (!n.isInheritedProperty() || n.getLaterVersions() != null) && n.isEditable()
		// && n instanceof TypeUser) {
		// && (n instanceof AttributeNode || n instanceof ElementNode || n instanceof SimpleAttributeNode || n
		// instanceof ElementReferenceNode)) {
		// button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the row.
		// LOGGER.debug("Would have put a button in for " + n);
		// Version Control
		// if (NodeUtils.checker(n).isInMinorOrPatch().existInPreviousVersions().get()) {
		// button.setEnabled(false);
		// }
		// if (NodeUtils.checker(n).isPatch().existInPreviousVersions().get()) {
		// button.setEnabled(false);
		// }
		// }
		return item;
	}

	@Override
	public void clearTable() {
		super.clearTable();
		buttonSet.clearButtons();
	}

}
