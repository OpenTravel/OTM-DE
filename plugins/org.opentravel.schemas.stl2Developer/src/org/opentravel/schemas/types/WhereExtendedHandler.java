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

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages lists of where extension owners are assigned to this object.
 * <p>
 * The WhereExtended handler is on the extension base. WhereExtendedListeners are placed on TL objects that extend this
 * owner.
 * 
 * Extension handler <b>must</b> add and remove listeners to the extensions via set and remove listener methods.
 * 
 * @author Dave Hollander
 * 
 */
public class WhereExtendedHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(WhereExtendedHandler.class);

	// nodes that use this node as a base/extension type.
	protected ArrayList<ExtensionOwner> users = new ArrayList<>();
	protected Node owner = null;

	protected WhereUsedNode whereUsedNode = null;

	/*********************************************************************************
	 *
	 * Listener class to add to the TL Model Element where the extension owner is assigned.
	 *
	 */
	public class WhereExtendedListener extends BaseNodeListener {
		private WhereExtendedHandler handler = null;

		public WhereExtendedListener(Node owner, WhereExtendedHandler handler) {
			super(owner);
			this.handler = handler;
		}

		@Override
		public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
			Node source = getSource(event);
			// LOGGER.debug("WhereExtendedListener: " + event.getType() + " handler = " + handler.owner + " source = "
			// + source);

			switch (event.getType()) {
			case EXTENDS_ADDED:
				if (source instanceof ExtensionOwner)
					handler.addUser((ExtensionOwner) source);
				break;
			case EXTENDS_REMOVED:
				if (source instanceof ExtensionOwner) {
					handler.removeUser(source); // remove source from owner's where extended list
					// ((ExtensionOwner) source).getExtensionHandler().removeListener();
				}
				break;
			case EXTENDS_ENTITY_MODIFIED:
				LOGGER.debug("Unhandled event: " + event.getType());
				break;
			default:
				// LOGGER.debug("Unhandled event: " + event.getType());

			}
		}

		// when existing extends entity overwritten the affectedItem -> extends entity is null
		@Override
		public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
			super.processValueChangeEvent(event); // logger.debug statements
			Node source = getSource(event);

			// this will be the case when parent is used for extension.
			// Events are thrown for both base and extension, only process one.
			switch (event.getType()) {
			case TYPE_ASSIGNMENT_MODIFIED:
				if (getNewValue(event) == null)
					handler.removeUser(source);
				else if (source != getNode() && source instanceof ExtensionOwner)
					handler.addUser((ExtensionOwner) source);
				break;
			default:
				// LOGGER.debug(event.getType() + " - " + getSource(event) + " on " + getNode() + " changed to: "
				// + getNewValue(event) + " from " + getOldValue(event));
				break;
			}
		}
	}

	/*********************************************************************************
	 * 
	 * Create a where assigned listener for this type provider <i>owner</i>.
	 */
	public WhereExtendedHandler(Node owner) {
		// whereUsedNode = new TypeNode(owner);
		this.owner = owner;
	}

	protected void add(ExtensionOwner user) {
		addUser(user);
		setListener(user);
	}

	protected void addUser(ExtensionOwner user) {
		if (!users.contains(user)) {
			users.add(user);
			// LOGGER.debug("Added " + user + " to " + owner + " where extended list.");
		}
	}

	/**
	 * Set a listener on the extension to associate it with this base object associated with a extension.
	 * 
	 * Note: the extension may have more than one listener during the assignment process, one for the old base type and
	 * one for the new.
	 * 
	 * @param extension
	 *            an extension setting this owner as its base type.
	 */
	public void setListener(ExtensionOwner extension) {
		for (ModelElementListener l : extension.getTLModelObject().getListeners())
			if (l instanceof WhereExtendedListener && ((BaseNodeListener) l).getNode() == owner) {
				// LOGGER.debug("Trying to add a duplicate listener to " + extension + " for " + owner);
				return;
			}
		WhereExtendedListener listener = new WhereExtendedListener(owner, this);
		extension.getTLModelObject().addListener(listener);
		// LOGGER.debug("Added listener for provider " + owner + " to extension " + extension);
	}

	/**
	 * Remove WhereExtendedListener from user if it exists.
	 * 
	 * @param user
	 */
	public void removeListener(ExtensionOwner user) {
		ModelElementListener listener = null;
		for (ModelElementListener l : ((Node) user).getTLModelObject().getListeners())
			if (l instanceof WhereExtendedListener)
				if (((WhereExtendedListener) l).getNode() == owner)
					listener = l;
		if (listener != null) {
			((Node) user).getTLModelObject().removeListener(listener);
		} else if (user != null) {
			// LOGGER.warn("Listener for " + user + " not found to be removed.");
			return;
		}
		// LOGGER.debug("Removed listener from " + owner + " for user " + user);
	}

	/**
	 * @return the unmodifiable collection of users of this type.
	 */
	public Collection<ExtensionOwner> getWhereExtended() {
		return Collections.unmodifiableCollection(users);
	}

	// Only library members can be extended so getWhereDescendantsAreExtended is not needed.

	public int getWhereExtendedCount() {
		return users.contains(owner) ? users.size() - 1 : users.size();
	}

	/**
	 * Remove user from where used list.
	 * 
	 * @param user
	 *            - type user to remove from list
	 * @return true if removed, false if not in list
	 */
	protected boolean removeUser(Node user) {
		if (!users.contains(user))
			return false;
		users.remove(user);
		return true;
	}

	/**
	 * Remove user from where used list and remove WhereExtended listener
	 * 
	 * @param user
	 * @return
	 */
	public boolean remove(ExtensionOwner user) {
		boolean result = removeUser((Node) user);
		if (result && user instanceof ExtensionOwner)
			removeListener(user);
		return result;
	}

	/**
	 * Replace this owner with the replacement on all extension owners that extend this owner.
	 */
	public void replace(Node replacement, LibraryNode libScope) {
		Collection<ExtensionOwner> targets = new ArrayList<>(users);
		for (ExtensionOwner extension : targets)
			if (libScope == null || ((Node) extension).getLibrary() == libScope) {
				extension.setExtension(replacement);
				// LOGGER.debug("replaced extension base with " + replacement);
			}
	}

}
