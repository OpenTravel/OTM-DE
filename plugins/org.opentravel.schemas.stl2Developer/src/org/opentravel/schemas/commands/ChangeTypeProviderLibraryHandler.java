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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.LibraryProviderNode;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change assigned types from library to user selected library.
 * 
 * All type assignments to current library are scanned and if the local name matches a provider from the user selected
 * library then the new type is assigned.
 * 
 * @author Dave Hollander
 * 
 */

public class ChangeTypeProviderLibraryHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTypeProviderLibraryHandler.class);

	public static String COMMAND_ID = "org.opentravel.schemas.commands.ChangeProviderLibrary";

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
		// DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		LibraryNode libToUpdate = (LibraryNode) providerLibNode.getParent();
		LibraryNode oldLibProvidingTypes = providerLibNode.getOwner();
		LibraryNode newLibProvidingTypes = null;

		// Run the wizard
		// ArrayList<Node> list = new ArrayList<Node>();
		// list.add(libToUpdate);
		final TypeSelectionWizard wizard = new TypeSelectionWizard(libToUpdate);
		if (wizard.run(OtmRegistry.getActiveShell()) && (wizard.getSelection() instanceof LibraryNode))
			newLibProvidingTypes = (LibraryNode) wizard.getSelection();

		// Create list of impacted users and use library method to replace their assigned types
		if (newLibProvidingTypes != null) {
			List<TypeUser> impactedUsers = new ArrayList<>();
			for (TypeUser user : libToUpdate.getDescendants_TypeUsers())
				if (oldLibProvidingTypes.contains((Node) user.getAssignedType()))
					impactedUsers.add(user);

			newLibProvidingTypes.replaceAllUsers(impactedUsers);
		}

		mc.refresh(libToUpdate);
	}

}
