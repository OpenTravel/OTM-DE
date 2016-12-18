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
package org.opentravel.schemas.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.GlobalLocalCancelDialog;
import org.opentravel.schemas.wizards.GlobalLocalCancelDialog.GlobalDialogResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska / Dave Hollander
 * 
 */
public class ImportObjectToLibraryAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportObjectToLibraryAction.class);

	private final LibraryNode library;

	/**
	 *
	 */
	public ImportObjectToLibraryAction(final MainWindow mainWindow, final StringProperties props,
			final LibraryNode library) {
		super(mainWindow, props);
		this.library = library;
	}

	public ImportObjectToLibraryAction(final MainWindow mainWindow, final LibraryNode library) {
		super(mainWindow);
		this.library = library;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		importSelectedToLibrary(library);
	}

	/**
	 * Import/Copy selected components into the destination library. User is asked about type assignment changes. Used
	 * by DND and right-click copy operations.
	 * 
	 * @param destination
	 */
	public void importSelectedToLibrary(final LibraryNode destination) {
		if (destination == null) {
			LOGGER.error("Tried to import to null destination library.");
			return;
		}
		if (!destination.isTLLibrary()) {
			LOGGER.error("Cannot import into built-in or XSD libraries");
			DialogUserNotifier.openInformation("WARNING", "You can not import into a built-in or XSD library.");
			return;
		}
		if (!destination.isEditable()) {
			LOGGER.error("Destination library is not editable.");
			DialogUserNotifier.openInformation("WARNING", "Destination library is not editable.");
			return;
		}
		final List<Node> sourceNodes = mc.getSelectedNodes_NavigatorView();
		if (sourceNodes == null || sourceNodes.size() <= 0) {
			LOGGER.error("No source nodes selected for import.");
			DialogUserNotifier.openInformation("WARNING", "No components selected for importing.");
			return;
		}
		LOGGER.info("Importing " + sourceNodes.size() + " selected nodes to library " + destination);

		// If a library is selected, get its named-type children and filter the other removing those
		// in the destination library.
		final List<Node> eligibleForImporting = new ArrayList<Node>();
		for (Node n : sourceNodes) {
			if (n instanceof LibraryNode)
				eligibleForImporting.addAll(n.getDescendants_LibraryMembers());
			else if (!(n.getLibrary()).equals(destination))
				eligibleForImporting.add(n);
		}
		// check to see if a service is being imported and is legal to do so
		if (destination.hasService()) {
			for (Node n : eligibleForImporting)
				if (n instanceof ServiceNode) {
					eligibleForImporting.remove(n);
					break;
				}
		}
		if (eligibleForImporting == null || eligibleForImporting.size() <= 0) {
			LOGGER.error("No eligible nodes selected for import.");
			DialogUserNotifier.openInformation("WARNING", "No eligible components selected for importing.");
			return;
		}
		// Ask if they want to also import the types assigned to these objects.
		int ans = DialogUserNotifier.openQuestionWithCancel(Messages.getString("action.import.copyDescendants.title"),
				Messages.getString("action.import.copyDescendants"));
		switch (ans) {
		case 0: // Yes
			ArrayList<Node> selected = new ArrayList<Node>(eligibleForImporting);
			for (Node n : selected) {
				for (Node nn : n.getDescendants_AssignedTypes(true))
					if (!eligibleForImporting.contains(nn))
						eligibleForImporting.add(nn);
			}
		case 1: // No
			break;
		case 2:
			return;
		}

		LOGGER.info("Importing " + eligibleForImporting.size() + " eligible nodes to library.");
		mc.postStatus("Ready to import " + eligibleForImporting.size() + " components.");

		// TODO - where are already existing objects removed from list?

		// ask the global/local/none question then do the import
		GlobalDialogResult result = askGlobalLocalNone(eligibleForImporting, destination);
		Collection<Node> importedNodesMap = null;
		switch (result) {
		case GLOBAL:
			importedNodesMap = destination.importNodes(eligibleForImporting, true);
			break;
		case LOCAL:
			importedNodesMap = destination.importNodes(eligibleForImporting, false);
			break;
		case NONE:
			importedNodesMap = destination.importNodes(eligibleForImporting).values();
			break;
		case CANCEL:
		default:
			break;
		}

		selectImportedNodesInNavigation(importedNodesMap);
		mc.postStatus("Imported " + eligibleForImporting.size() + " types to " + destination);
		mc.refresh(destination); // refresh everything.
	}

	private void selectImportedNodesInNavigation(Collection<Node> importedNodesMap) {
		if (importedNodesMap != null && !importedNodesMap.isEmpty()) {
			Node firstNode = importedNodesMap.iterator().next();
			mc.selectNavigatorNodeAndRefresh(firstNode);
		}
	}

	/**
	 * Change the existing sourceNode type assignments to use newNode. Only writable nodes are changed. The user is
	 * asked if they want the change to be global, local or none. Properties of the newNode, if they exist, are always
	 * done.
	 * 
	 * @param sourceNode
	 * @param newNode
	 */
	public GlobalDialogResult askGlobalLocalNone(final List<Node> sourceNodes, final LibraryNode destLib) {
		LOGGER.debug("Asking about global/local/none change of type assignments.");

		// Now, ask the question.
		//
		final StringBuilder message = new StringBuilder(
				"Imported objects are assigned to properties as types or base types.");
		message.append("The newly imported objects can be assigned replacing assignments to the existing objects.");
		message.append("\n\nPlease choose how to change the assignments:");
		message.append("\n\t Globally \t- everywhere in all of the open libraries.");
		message.append("\n\t Locally  \t- only in the " + destLib.getName() + " library.");
		message.append("\n\t None     \t- nowhere; keep all existing assignments.");

		// Post the dialog unless they already answered and said apply to all.
		final GlobalLocalCancelDialog nsChangeDialog = new GlobalLocalCancelDialog(OtmRegistry.getActiveShell(),
				message.toString());

		nsChangeDialog.open();
		GlobalDialogResult result = nsChangeDialog.getResult();
		return result;
	}

}
