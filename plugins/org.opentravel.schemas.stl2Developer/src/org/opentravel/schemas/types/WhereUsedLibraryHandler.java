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
/**
 * 
 */
package org.opentravel.schemas.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.whereused.LibraryUsesNode;
import org.opentravel.schemas.types.whereused.LibraryWhereUsedNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages lists of libraries used by this library.
 * 
 * @author Dave Hollander
 * 
 */
public class WhereUsedLibraryHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(WhereUsedLibraryHandler.class);

	// nodes that use this node as a type definition. Includes base type users.
	protected ArrayList<LibraryNode> users = new ArrayList<LibraryNode>();
	protected LibraryWhereUsedNode whereUsedNode = null;
	protected LibraryUsesNode usedByNode = null;
	protected LibraryNode owner = null;

	// protected ArrayList<WhereUsedListener> listeners = new ArrayList<WhereUsedListener>();

	public class WhereUsedLibListener extends BaseNodeListener {
		private WhereUsedLibraryHandler handler = null;

		public WhereUsedLibListener(Node node, WhereUsedLibraryHandler handler) {
			super(node);
			this.handler = handler;
		}

		@Override
		public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
			super.processValueChangeEvent(event); // logger.debug statements

			switch (event.getType()) {
			// case NAME_MODIFIED:
			// LOGGER.debug("Name Modified - from: " + event.getOldValue() + " to: " + event.getNewValue());
			// break;
			case TYPE_ASSIGNMENT_MODIFIED:
				// As much as i would like to set/remove listeners here, doing so makes the event stream not have the
				// assignment event.
				// LOGGER.debug(getSource(event) + " on " + getNode() + " changed to: " + getNewValue(event) + "  from "
				// + getOldValue(event));
				break;
			default:
				break;
			}
		}
	}

	public WhereUsedLibraryHandler(LibraryNode libraryNode) {
		whereUsedNode = new LibraryWhereUsedNode(libraryNode);
		usedByNode = new LibraryUsesNode(libraryNode);
		this.owner = libraryNode;
		// LOGGER.debug("Constructed where used handler for " + libraryNode);
	}

	public void add(TypeUser user) {
		LibraryNode userLib = ((Node) user).getLibrary();
		if (userLib != owner && !users.contains(userLib))
			users.add(userLib);
	}

	public void add(ExtensionOwner user) {
		LibraryNode userLib = ((Node) user).getLibrary();
		if (userLib != owner && !users.contains(userLib))
			users.add(userLib);
	}

	/**
	 * Remove user from where used list.
	 * 
	 */
	public void remove(TypeUser user) {
		LibraryNode lib = ((Node) user).getLibrary();
		if (users.contains(lib))
			if (getUsersOfTypesFromOwnerLibrary(lib).isEmpty())
				users.remove(lib);
	}

	public void remove(ExtensionOwner user) {
		LibraryNode lib = ((Node) user).getLibrary();
		if (users.contains(lib))
			if (getUsersOfTypesFromOwnerLibrary(lib).isEmpty())
				users.remove(lib);
	}

	/**
	 * This handler is associated with a type provider. It can have many listeners, one for each user of this type
	 * provider.
	 * 
	 * @param user
	 */
	public void setListener(TypeUser user) {
		// for (ModelElementListener l : user.getTLModelObject().getListeners())
		// if (l instanceof WhereUsedListener && ((BaseNodeListener) l).getNode() == owner) {
		// LOGGER.debug("Trying to add a duplicate listener to " + user + " for " + owner);
		// return;
		// }
		// WhereUsedListener listener = new WhereUsedListener((Node) owner, this);
		// ((Node) user).getTLModelObject().addListener(listener);
		// // listeners.add(listener); // Not sure why we are keeping array - debugging?
		//
		// LOGGER.debug("Added listener " + owner + " for user " + user);
	}

	public void removeListener(TypeUser user) {
		// ModelElementListener listener = null;
		// for (ModelElementListener l : ((Node) user).getTLModelObject().getListeners())
		// if (l instanceof WhereUsedListener)
		// if (((WhereUsedListener) l).getNode() == owner)
		// listener = l;
		// if (listener != null) {
		// ((Node) user).getTLModelObject().removeListener(listener);
		// // listeners.remove(listener);
		// } else if (user != null)
		// LOGGER.warn("Listener for " + user + " not found to be removed.");
		// LOGGER.debug("Removed listener from " + owner + " for user " + user);
	}

	/**
	 * Get all libraries that have types that are assigned types from this library or chain.
	 * 
	 * @param deep
	 *            if true collect users from the chain that contains the library.
	 * @return unmodifiable collection of libraries that contain users of this library
	 */
	public Collection<LibraryNode> getWhereUsed(boolean deep) {
		// LOGGER.debug("Getting where used for: " + owner.getNameWithPrefix());
		Set<LibraryNode> chainUsers = new HashSet<>();
		// List<LibraryNode> chainUsers = new ArrayList<LibraryNode>();
		if (deep && owner.getChain() != null)
			for (LibraryNode ln : owner.getChain().getLibraries())
				chainUsers.addAll(ln.getWhereUsedHandler().getWhereUsed());
		else
			chainUsers.addAll(users);
		chainUsers.remove(owner);
		if (deep && owner.getChain() != null)
			chainUsers.removeAll(owner.getChain().getLibraries());
		return Collections.unmodifiableCollection(chainUsers);
		// return chainUsers;
	}

	/**
	 * @return the unmodifiable collection of users of this type.
	 */
	public Collection<LibraryNode> getWhereUsed() {
		return Collections.unmodifiableCollection(users);
	}

	/**
	 * Find all type providers in a library that have types assigned to owner library.
	 * 
	 * @return unmodifiable list of type users and extension owners
	 */
	public Collection<Node> getUsersOfTypesFromOwnerLibrary(LibraryNode ownerLib) {
		return getUsersOfTypesFromOwnerLibrary(ownerLib, false);
	}

	/**
	 * Find all type providers in a library that have types assigned to owner library.
	 * 
	 * @param deep
	 *            If deep, examine all owner libraries in the chain and all type users in the owners chain.
	 * @return unmodifiable list of type users and extension owners
	 */
	public Collection<Node> getUsersOfTypesFromOwnerLibrary(LibraryNode ownerLib, boolean deep) {
		// user set is the collection to return
		Set<Node> userSet = new HashSet<Node>();
		if (ownerLib == null)
			return Collections.unmodifiableCollection(userSet);

		// TODO - should the deep function should be done by chainNode

		// Get the libraries to examine. If deep, get all libraries in the chain.
		List<LibraryNode> ownerLibs = new ArrayList<LibraryNode>();
		if (deep && ownerLib.getChain() != null)
			ownerLibs.addAll(ownerLib.getChain().getLibraries());
		else
			ownerLibs.add(ownerLib);

		// Get all the libraries in this handler's owner chain
		List<LibraryNode> theseOwners = new ArrayList<LibraryNode>();
		if (deep && owner.getChain() != null)
			theseOwners.addAll(owner.getChain().getLibraries());
		else
			theseOwners.add(owner);

		// For each library in the ownerLib chain
		for (LibraryNode ol : ownerLibs) {
			// Get type users
			for (TypeUser user : ol.getDescendants_TypeUsers()) {
				TypeProvider typeProvider = user.getAssignedType();
				// if (ut != null && ((Node) ut).getLibrary() == owner) {
				if (typeProvider != null && theseOwners.contains(typeProvider.getLibrary())) {
					userSet.add((Node) user);
				}
			}
			// Get extended objects
			for (ExtensionOwner eo : ol.getDescendants_ExtensionOwners()) {
				Node extensionBase = eo.getExtensionBase();
				if (extensionBase != null && theseOwners.contains(extensionBase.getLibrary())) {
					userSet.add((Node) eo);
				}
			}
			// Get contextual facets that contribute these owners
			for (ContextualFacetNode cf : ol.getDescendants_ContextualFacets()) {
				ContextualFacetOwnerInterface cfOwner = null;
				if (cf == null || cf.getWhereContributed() == null)
					continue;
				if (cf.getWhereContributed().getOwningComponent() instanceof ContextualFacetOwnerInterface)
					cfOwner = (ContextualFacetOwnerInterface) cf.getWhereContributed().getOwningComponent();
				if (cfOwner != null && theseOwners.contains(cfOwner.getLibrary()))
					userSet.add((Node) cf);
			}
		}
		return Collections.unmodifiableCollection(userSet);
	}

	public int getTypeUsersAndDescendantsCount() {
		// return getUsersOfTypesFromOwnerLibrary(lib)().size();
		return (0);
	}

	public WhereUsedNode getWhereUsedNode() {
		return whereUsedNode;
	}

	public Node getUsedByNode() {
		return usedByNode;
	}

	public void refreshUsedByNode() {
		usedByNode = new LibraryUsesNode(owner);
	}

}
