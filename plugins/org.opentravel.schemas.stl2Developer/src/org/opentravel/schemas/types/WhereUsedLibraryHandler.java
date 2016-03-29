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
import java.util.List;

import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
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
	protected TypeNode whereUsedNode = null;
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
				// if (getNewValue(event) == getNode()) {
				// handler.add((TypeUser) getSource(event));
				// // handler.setListener((TypeUser) getSource(event));
				// }
				// if (getOldValue(event) == getNode()) {
				// handler.remove((TypeUser) getSource(event));
				// // handler.removeListener((TypeUser) getSource(event));
				// }
				break;
			default:
				break;
			}
		}
	}

	public WhereUsedLibraryHandler(LibraryNode libraryNode) {
		whereUsedNode = new TypeNode(libraryNode);
		this.owner = libraryNode;
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
	 * @return the unmodifiable collection of users of this type.
	 */
	public Collection<LibraryNode> getWhereUsed() {
		return Collections.unmodifiableCollection(users);
	}

	/**
	 * Find all type providers in a library that have types assigned to owner library.
	 * 
	 * @return
	 */
	public List<Node> getUsersOfTypesFromOwnerLibrary(LibraryNode lib) {
		List<Node> ul = new ArrayList<Node>();
		// Get type users
		for (TypeUser user : lib.getDescendants_TypeUsers()) {
			TypeProvider ut = user.getAssignedType();
			// Node userOwner = null;
			if (ut != null && ((Node) ut).getLibrary() == owner) {
				// userOwner = ((Node) user).getOwningComponent();
				if (!ul.contains(user))
					ul.add((Node) user);
			}
		}
		// Get extended objects
		for (ExtensionOwner o : lib.getDescendants_ExtensionOwners()) {
			Node extensionBase = o.getExtensionBase();
			if (extensionBase != null && extensionBase.getLibrary() == owner) {
				if (!ul.contains(extensionBase))
					ul.add((Node) o);
			}
		}
		return ul;
	}

	public int getTypeUsersAndDescendantsCount() {
		// return getUsersOfTypesFromOwnerLibrary(lib)().size();
		return (0);
	}

	public TypeNode getWhereUsedNode() {
		return whereUsedNode;
	}

}
