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
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.LibraryUserNode;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assign type to navigator selected nodes. Wizard is run to allow the user to select the type to assign. If a selected
 * node is a TypeNode, the users of that type will be replaced.
 * 
 * TODO - move execute to a handler
 * 
 * TEST - TODO - use AbstractAction's main window
 * 
 * TODO - move the performFinish code from the wizard to here and make public.
 * 
 * TODO - make mainWindow=null safe
 * 
 * @author Dave Hollander
 * 
 */
public class AssignTypeAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssignTypeAction.class);
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.replaceUsers");

	/**
	 *
	 */
	public AssignTypeAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
	}

	@Override
	public void run() {
		LOGGER.debug("Assign Type action starting.");
		Node n = getNavigatorSelection();
		if (n instanceof TypeProviderWhereUsedNode)
			replaceTypeSelection((TypeProviderWhereUsedNode) n);
		return;
	}

	@Override
	public boolean isEnabled() {
		Node n = getNavigatorSelection();
		if (!(n instanceof TypeProviderWhereUsedNode))
			return false;

		// Allow replacement if any user is editable
		return (!((TypeProviderWhereUsedNode) n).getAllUsers(true).isEmpty());
	}

	// From OTM Actions (46) - typeSelector - type selection buttons in facet view
	public static void execute(List<Node> toChange, Node newType) {
		if (newType == null || !newType.isNamedEntity()) {
			LOGGER.warn("No type to assign. Early Exit.");
			// return false;
		}
		if (toChange == null || toChange.isEmpty()) {
			LOGGER.warn("Nothing to assign to. Early Exit.");
			// return false;
		}
		Node last = null;
		TypeProvider ret = null;
		for (Node cn : toChange) {
			// 10/2016 dmh - clean up logic and skip fixName since done in assign type
			ret = ((TypeUser) cn).setAssignedType((TypeProvider) newType);
			if (ret == null)
				DialogUserNotifier.openWarning("Warning", "Invalid type assignment");
			else if (last != null && cn.getParent() != last.getParent())
				OtmRegistry.getNavigatorView().refresh(last.getParent());
			last = cn;
		}
		OtmRegistry.getMainController().refresh();

		// LOGGER.debug("Assigned " + newType.getName() + " to " + toChange.size() + " nodes.");
		// return ret != null;
	}

	/**
	 * Replace the provider library with the latest version.
	 * 
	 * DOES NOT WORK...disabled (for now)
	 */
	private void replaceLibrary(LibraryUserNode user) {
		// LibraryNode providerLibrary = (LibraryNode) user.getParent();
		// LibraryNode userLibrary = user.getOwner();
		//
		// // Determine if the provider library has a later version
		// if (providerLibrary.getChain() == null) {
		// DialogUserNotifier.openWarning("Not Versioned", "The provider library is not versioned.");
		// return;
		// }
		// // If the user library is not at head version ???
		// if (!userLibrary.isEditable()) {
		// LOGGER.debug("User is not editable.");
		// return;
		// }
		// List<Node> kids = user.getChildren();
		// for (Node n : kids) {
		// // if the kid uses an older version of the provider library, update it.
		// if (n instanceof TypeUserNode) {
		// TypeUser tu = ((TypeUserNode) n).getOwner();
		// Node at = (Node) ((TypeUser) tu).getAssignedType();
		// if (at.getVersionNode() != null)
		// if (at.getVersionNode().getNewestVersion() != at)
		// LOGGER.debug("Update type assigned to " + n + " assigned " + at.getNameWithPrefix() + " to "
		// + at.getVersionNode().getNewestVersion());
		// List<Node> laterATs = at.getLaterVersions();
		// if (laterATs != null && !laterATs.isEmpty())
		// LOGGER.debug("Update type assigned to " + n + " assigned " + at.getNameWithPrefix() + "?");
		// }
		// }
		//
		// MinorVersionHelper vh = new MinorVersionHelper();
		// List<TLLibrary> tlCandiates = null;
		// try {
		// tlCandiates = vh.getLaterMinorVersions(providerLibrary.getTLLibrary());
		// } catch (VersionSchemeException e) {
		// e.printStackTrace();
		// }
		// List<Node> candiates = providerLibrary.getChain().getLaterVersions();
		// return;
	}

	/**
	 * Execute "Replace Where Used" action on a specific type provider
	 * 
	 * (Not used for TYPE SELECTION BUTTONS in the facet table.)
	 * 
	 * @param providerWhereUsed
	 */
	private void replaceTypeSelection(TypeProviderWhereUsedNode providerWhereUsed) {
		// TypeProvider provider = providerWhereUsed.getOwner();

		List<TypeUser> users = providerWhereUsed.getAllUsers(true);
		List<ExtensionOwner> owners = providerWhereUsed.getAllExtensions(true);
		if (users == null || users.isEmpty())
			return;

		// If the type assigned to the node passed to the wizard has later versions the selection will be limited to its
		// later versions.
		final TypeSelectionWizard wizard = new TypeSelectionWizard((Node) users.get(0));
		if (wizard.run(OtmRegistry.getActiveShell())) {
			Node newType = wizard.getSelection();
			if (!(newType instanceof TypeProvider))
				return;
			if (DialogUserNotifier.openConfirm("Confirm replace where used.", "Replace assigned type to: " + newType)) {
				for (TypeUser user : users)
					user.setAssignedType((TypeProvider) newType);
				for (ExtensionOwner owner : owners)
					owner.setExtension(newType);
			}
		}
		mc.refresh();
	}

	// private void addTypeUsers(LibraryUserNode tn, List<Node> users) {
	// users.add(tn.getParent()); // This is a type node for a specific type user.
	// }
	//
	// private void addTypeUsers(TypeUserNode tn, List<Node> users) {
	// // This is a type node for the type provider.
	// for (TypeUser n : ((TypeProvider) tn.getParent()).getWhereAssigned())
	// users.add((Node) n);
	// }

	private static TypeUser createVersionExtension(TypeUser user) {
		OtmAbstractHandler handler = new OtmAbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				return null;
			}
		};

		// If the owning component is not in the head the make a minor version of the owner.
		Node owner = (Node) ((Node) user).getOwningComponent();
		if (owner != null && owner.getChain() != null && !owner.isInHead2()) {
			owner = handler.createVersionExtension(owner);
			// Owner == null if they canceled out of creating version
			if (owner != null) {
				FacetInterface owningFacet = (FacetInterface) owner.findChildByName(user.getParent().getName());
				// Inherited children fails until children are retrieved (lazy evaluation). Force cloning now.
				// FacetInterface owningFacet = findFacet(owner, ((Node) user).getParent().getName());
				user = (TypeUser) ((Node) user).clone((Node) owningFacet, "");
			}
		}
		return owner == null ? null : user;
	}

	/**
	 * Run type selection wizard and assign selected type of the pass node list. Will create minor versions of the nodes
	 * in the list if necessary.
	 * 
	 * @param list
	 *            - nodes selected to be assigned the type selected.
	 */
	public static void execute(TypeUser user) {
		// Runs when the facet table type selection button activated

		// OtmAbstractHandler handler = new OtmAbstractHandler() {
		// @Override
		// public Object execute(ExecutionEvent event) throws ExecutionException {
		// return null;
		// }
		// };

		// If the owning component is not in the head the make a minor version of the owner.
		user = createVersionExtension(user);
		// if (owner != null && owner.getChain() != null && !owner.isInHead2()) {
		// owner = handler.createVersionExtension(owner);
		// // Inherited children fails until children are retrieved (lazy evaluation). Force cloning now.
		// FacetNode owningFacet = findFacet(owner, ((Node) user).getParent().getName());
		// user = (TypeUser) ((Node) user).clone(owningFacet, "");
		// }
		if (user == null) {
			LOGGER.debug("Failed to create version.");
			return;
		}
		Node owner = (Node) ((Node) user).getOwningComponent();

		// Determine if the property is in the same version as the owner. Older versions will be inherited.
		if (((Node) user).isInherited()) {
			FacetInterface owningFacet = findFacet(owner, ((Node) user).getParent().getName());
			user = (TypeUser) ((Node) user).clone((Node) owningFacet, "");
		}

		// Now run the wizard
		ArrayList<Node> list = new ArrayList<>();
		list.add((Node) user);
		final TypeSelectionWizard wizard = new TypeSelectionWizard((Node) user);
		if (wizard.run(OtmRegistry.getActiveShell()))
			AssignTypeAction.execute(list, wizard.getSelection());
		// TODO - eliminate need for list to be passed
		// AssignTypeAction.execute(wizard.getList(), wizard.getSelection());
		// else
		// DialogUserNotifier.openInformation("No Selection", Messages.getString("OtmW.101")); //$NON-NLS-1$
		// TODO - should the new owner be removed?

		OtmRegistry.getMainController().refresh(owner);
	}

	private static FacetInterface findFacet(Node owner, String name) {
		if (owner != null)
			for (Node n : owner.getChildren())
				if (n instanceof FacetInterface)
					if (n.getName().equals(name))
						return (FacetInterface) n;
		return null;
	}
}
