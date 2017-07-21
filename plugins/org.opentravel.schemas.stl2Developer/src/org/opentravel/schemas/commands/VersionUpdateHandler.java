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
package org.opentravel.schemas.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.ContextualFacetUserNode;
import org.opentravel.schemas.types.whereused.ExtensionUserNode;
import org.opentravel.schemas.types.whereused.LibraryProviderNode;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update type assignments to later versions.
 * 
 * @author Dave Hollander
 * 
 */

public class VersionUpdateHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionUpdateHandler.class);

	public static String COMMAND_ID = "org.opentravel.schemas.commands.VersionUpdate";

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		// ProjectNode project = null;
		Node node = mc.getSelectedNode_NavigatorView();
		if (node == null)
			return null;
		if (node instanceof LibraryProviderNode)
			updateLibrary((LibraryProviderNode) node);
		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	@Override
	public boolean isEnabled() {
		Node n = mc.getSelectedNode_NavigatorView();
		if (n instanceof LibraryProviderNode)
			return ((LibraryProviderNode) n).getParent().isEditable();
		return n != null && n.isEditable() ? n instanceof TypeUserNode : false;
	}

	/**
	 * Update all the type users (children) to the latest version of the parent library. Also update extension owners.
	 * <p>
	 * Library Provider node lists all of the parent's type users that are assigned types from the owner library.
	 * 
	 * @param providerLibNode
	 */
	private void updateLibrary(LibraryProviderNode providerLibNode) {
		DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		LibraryNode libToUpdate = (LibraryNode) providerLibNode.getParent();
		LibraryNode libProvidingTypes = (LibraryNode) providerLibNode.getOwner();

		// Get the type and extension users to update
		List<TypeUser> usersToUpdate = new ArrayList<TypeUser>();
		List<ExtensionOwner> extensionsToUpdate = new ArrayList<ExtensionOwner>();
		List<ContextualFacetNode> facetsToUpdate = new ArrayList<ContextualFacetNode>();
		for (Node user : providerLibNode.getChildren(false))
			if (user instanceof TypeUserNode) {
				if (!usersToUpdate.contains(((TypeUserNode) user).getOwner()))
					usersToUpdate.add(((TypeUserNode) user).getOwner());
			} else if (user instanceof ExtensionUserNode) {
				if (!extensionsToUpdate.contains(((ExtensionUserNode) user).getOwner()))
					extensionsToUpdate.add(((ExtensionUserNode) user).getOwner());
			} else if (user instanceof ContextualFacetUserNode) {
				if (!facetsToUpdate.contains(((ContextualFacetUserNode) user).getOwner()))
					facetsToUpdate.add(((ContextualFacetUserNode) user).getOwner());
			}
		if (usersToUpdate.isEmpty() && extensionsToUpdate.isEmpty() && facetsToUpdate.isEmpty()) {
			DialogUserNotifier.openWarning("Version Update Warning", "Could not find objects to update.");
			return;
		}

		// Ask the user if they want Draft versions?
		String question = "Update to latest Draft version? Answer Yes to include draft or No for only Review or Final versions.";
		String[] buttons = { "Yes", "No", "Cancel" };
		int result = DialogUserNotifier.openQuestionWithButtons("Update to Latest Version", question, buttons);
		boolean includeDrafts = false;
		if (result == 0)
			includeDrafts = true;
		else if (result == 2)
			return; // Exit on cancel

		// Get the latest version of parent library. It may not be open yet.
		LibraryNode replacement = null;
		try {
			replacement = rc.getLatestVersion(libProvidingTypes, includeDrafts);
		} catch (RepositoryException e1) {
			if (replacement == null)
				try {
					// retry
					replacement = rc.getLatestVersion(libProvidingTypes, includeDrafts);
				} catch (RepositoryException e) {
					DialogUserNotifier.openWarning("Version Update Warning", e.getLocalizedMessage());
					return;
				}
		}

		// Later version could not be found so just exit.
		// If the replacement is the same as the owner then inform the user and return
		if (replacement == null || replacement == libProvidingTypes) {
			DialogUserNotifier.openWarning("Version Update Warning", "Could not find a later version.");
			return;
		}

		// Confirm user wants to do the update
		question = "Do you want to update " + libProvidingTypes.getNameWithPrefix() + " with "
				+ replacement.getNameWithPrefix() + "?";
		if (DialogUserNotifier.openQuestion("Update to Latest Version", question)) {
			// replace type users using the replacement map
			replacement.replaceAllUsers(usersToUpdate);
			replacement.replaceAllExtensions(extensionsToUpdate);
			replacement.replaceAllContributors(facetsToUpdate);
		}

		// How to clear the TypeUserNode?
		libToUpdate.getWhereUsedHandler().refreshUsedByNode();
		mc.refresh(libToUpdate);
	}
	// private void updateLibraryOLD(LibraryProviderNode providerLibNode) {
	// DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
	// LibraryNode libToUpdate = (LibraryNode) providerLibNode.getParent();
	// List<TypeUser> usersToUpdate = new ArrayList<TypeUser>();
	// for (Node typeUserNode : providerLibNode.getChildren())
	// if (!usersToUpdate.contains(((TypeUserNode) typeUserNode).getOwner()))
	// usersToUpdate.add(((TypeUserNode) typeUserNode).getOwner());
	//
	// // Ask the user if they want Draft versions?
	// String question = "Update to latest Draft version? Answer Yes to include draft or No for only Final versions.";
	// String[] buttons = { "Yes", "No", "Cancel" };
	// int result = DialogUserNotifier.openQuestionWithButtons("Update to Latest Version", question, buttons);
	// boolean includeDrafts = false;
	// if (result == 0)
	// includeDrafts = true;
	// else if (result == 2)
	// return; // Exit on cancel
	//
	// // Get a list of extensionOwners and TypeUsers from libToUpdate
	// Set<LibraryNode> usedLibs = new HashSet<LibraryNode>();
	// for (Node n : providerLibNode.getChildren())
	// if (n instanceof TypeUserNode)
	// usedLibs.add(((TypeUserNode) n).getOwner().getAssignedType().getLibrary());
	// else if (n instanceof ExtensionUserNode)
	// usedLibs.add(((ExtensionUserNode) n).getOwner().getExtensionBase().getLibrary());
	//
	// // Create replacement map
	// HashMap<LibraryNode, LibraryNode> replacementMap;
	// try {
	// replacementMap = rc.getVersionUpdateMap(new ArrayList<LibraryNode>(usedLibs), includeDrafts);
	// } catch (RepositoryException e1) {
	// DialogUserNotifier.openWarning("Version Update Warning", e1.getLocalizedMessage());
	// return;
	// }
	//
	// LibraryNode targetLib = null;
	// LibraryNode oldLib = null;
	// for (Entry<LibraryNode, LibraryNode> e : replacementMap.entrySet()) {
	// targetLib = e.getValue();
	// oldLib = e.getKey();
	// }
	// if (targetLib == null) {
	// DialogUserNotifier.openWarning("Update to Latest Version", "Did not find a later version.");
	// return;
	// }
	//
	// question = "Do you want to update " + oldLib.getNameWithPrefix() + " with " + targetLib.getNameWithPrefix()
	// + "?";
	// if (DialogUserNotifier.openQuestion("Update to Latest Version", question))
	// // replace type users using the replacement map
	// libToUpdate.replaceAllUsers(replacementMap);
	//
	// // How to clear the TypeUserNode?
	// libToUpdate.getWhereUsedHandler().refreshUsedByNode();
	// mc.refresh(libToUpdate);
	// }
}
