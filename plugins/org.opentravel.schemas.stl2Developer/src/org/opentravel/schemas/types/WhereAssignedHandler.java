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

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages lists of where type providers are assigned to type users. The handler is on the type providers. Assignment
 * handlers <b>must</b> add and removes listeners to the users via set and remove listener methods.
 * 
 * @author Dave Hollander
 * 
 */
public class WhereAssignedHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(WhereAssignedHandler.class);

	// nodes that use this node as a type definition.
	protected ArrayList<Node> users = new ArrayList<Node>();
	protected TypeNode whereUsedNode = null;
	protected TypeProvider owner = null;

	/*********************************************************************************
	 *
	 * Listener class to add to the TL Model Element where the provider is assigned.
	 *
	 */
	public class WhereAssignedListener extends BaseNodeListener {
		private WhereAssignedHandler handler = null;

		public WhereAssignedListener(Node node, WhereAssignedHandler handler) {
			super(node);
			this.handler = handler;
		}

		@Override
		public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
			super.processOwnershipEvent(event);
		}

		@Override
		public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
			super.processValueChangeEvent(event); // logger.debug statements

			switch (event.getType()) {
			case TYPE_ASSIGNMENT_MODIFIED:
				// Listeners can't be set/removed here, doing so makes the event stream not have the assignment event.
				// LOGGER.debug("Type Assignment Modified event - " + getSource(event) + " on " + getNode()
				// + " changed to: " + getNewValue(event) + "  from " + getOldValue(event));

				// VWA and Core have event listeners but are not type users.
				Node source = getSource(event);
				if (source instanceof SimpleAttributeOwner)
					source = ((SimpleAttributeOwner) source).getSimpleAttribute();

				if (getNewValue(event) == getNode())
					handler.add((TypeUser) source);

				if (getOldValue(event) == getNode() && event.getOldValue() != event.getNewValue())
					handler.remove((TypeUser) source);

				break;
			case DOCUMENTATION_MODIFIED:
			case NAME_MODIFIED:
				break;
			default:
				// LOGGER.debug(event.getType() + " - " + getSource(event) + " on " + getNode() + " changed to: "
				// + getNewValue(event) + "  from " + getOldValue(event));

				break;
			}
		}
	}

	/*********************************************************************************
	 * 
	 * Create a where assigned listener for this type provider <i>owner</i>.
	 */
	public WhereAssignedHandler(TypeProvider owner) {
		whereUsedNode = new TypeNode(owner);
		this.owner = owner;
	}

	public void add(TypeUser user) {
		if (!users.contains(user)) {
			users.add((Node) user);
			// LOGGER.debug("Added " + user + " to " + owner + " where assigned list.");
		}
	}

	/**
	 * This handler is associated with a type provider. It can have many listeners, one for each user of this type
	 * provider.
	 * 
	 * @param user
	 */
	public void setListener(TypeUser user) {
		for (ModelElementListener l : user.getTLModelObject().getListeners())
			if (l instanceof WhereAssignedListener && ((BaseNodeListener) l).getNode() == owner) {
				// FIXME - study startup and see why duplicates are trying to be created.
				// LOGGER.debug("Trying to add a duplicate listener to " + user + " for " + owner);
				return;
			}
		WhereAssignedListener listener = new WhereAssignedListener((Node) owner, this);
		((Node) user).getTLModelObject().addListener(listener);
		// listeners.add(listener); // Not sure why we are keeping array - debugging?

		// LOGGER.debug("Added listener for provider " + owner + " to user " + user);
	}

	public void setListener(ExtensionOwner owner) {

	}

	public void removeListener(TypeUser user) {
		ModelElementListener listener = null;
		for (ModelElementListener l : ((Node) user).getTLModelObject().getListeners())
			if (l instanceof WhereAssignedListener)
				if (((WhereAssignedListener) l).getNode() == owner)
					listener = l;
		if (listener != null) {
			((Node) user).getTLModelObject().removeListener(listener);
			// listeners.remove(listener);
		} else if (user != null) {
			// LOGGER.warn("Listener for " + user + " not found to be removed.");
			return;
		}
		// LOGGER.debug("Removed listener from " + owner + " for user " + user);
	}

	/**
	 * @return the unmodifiable collection of users of this type.
	 */
	@Deprecated
	public Collection<Node> getWhereUsed() {
		// Safety check - remove any deleted users
		ArrayList<Node> whereused = new ArrayList<Node>();
		for (Node n : whereused)
			if (n.isDeleted()) {
				users.remove(n);
				// LOGGER.debug("Removed deleted user: " + n);
			}
		return Collections.unmodifiableCollection(users);
	}

	/**
	 * @return the unmodifiable collection of users of this type.
	 */
	public Collection<TypeUser> getWhereAssigned() {
		ArrayList<TypeUser> whereused = new ArrayList<TypeUser>();
		for (Node n : users)
			if (n instanceof TypeUser)
				whereused.add((TypeUser) n);
		// Safety check - remove any deleted users
		for (TypeUser n : whereused)
			if (((INode) n).isDeleted()) {
				users.remove(n);
				// LOGGER.debug("Removed deleted user: " + n);
			}
		return Collections.unmodifiableCollection(whereused);
	}

	/**
	 * @return the number of users of this type.
	 */
	public int getWhereAssignedCount() {
		return users.size();
	}

	public Collection<TypeUser> getWhereAssignedIncludingDescendants() {
		List<TypeUser> ul = new ArrayList<TypeUser>();
		for (Node n : users)
			if (n instanceof TypeUser)
				ul.add((TypeUser) n);

		if (((Node) owner).getOwningComponent() == null)
			return ul; // happens when building inheritance wizard tree

		for (TypeProvider n : ((Node) owner).getOwningComponent().getDescendants_TypeProviders())
			if (!((Node) n).isDeleted())
				ul.addAll(n.getWhereAssigned());
		return ul;
	}

	public int getWhereAssignedIncludingDescendantsCount() {
		return getWhereAssignedIncludingDescendants().size();
	}

	/**
	 * Replace type assignment everywhere this node is used as a type.
	 * 
	 * @param replacement
	 */
	public void replace(TypeProvider replacement) {
		List<TypeUser> users = new ArrayList<TypeUser>(getWhereAssigned());
		for (TypeUser n : users)
			n.setAssignedType(replacement);
	}

	/**
	 * Replace type assignment everywhere this node is assigned as a type. TypeUsers must be editable and in the scope
	 * library if specified.
	 * 
	 * @param replacement
	 * @param scopeLibrary
	 *            if not null, only change type users that are in the specified library.
	 */
	public void replace(TypeProvider replacement, LibraryNode scopeLibrary) {
		if (replacement == null)
			return;
		List<TypeUser> users = new ArrayList<TypeUser>(getWhereAssigned());
		for (TypeUser n : users)
			if (n.isEditable())
				if (scopeLibrary == null || ((Node) n).getLibrary().equals(scopeLibrary)) {
					((TypeUser) n).setAssignedType(replacement);
					// LOGGER.debug("replace " + n + " with " + replacement);
				}
	}

	/**
	 * Replace type assignment everywhere this node or any of its descendants are used as a type. An attempt is made to
	 * match which child of the replacement is used based on name but if not found then the owning component is used.
	 * 
	 * @param replacement
	 */
	public void replaceAll(TypeProvider replacement) {
		replaceAll(replacement, null);
	}

	/**
	 * Replace this provider with replacement for all users of this provider as a type. Also replaces type usage of
	 * descendants of this owner node with matching replace descendant when possible. Also does the TL properties. Note
	 * - user counts may change when business replace core objects because core is also a valid simple type.
	 * 
	 * @param scopeLibrary
	 *            - if not null, only change type users that are in the specified library.
	 */
	public void replaceAll(TypeProvider replacement, LibraryNode scopeLibrary) {
		// Create map of replacement candidates
		java.util.HashMap<String, TypeProvider> replacementTypes = new java.util.HashMap<String, TypeProvider>();
		for (TypeProvider r : ((Node) replacement).getDescendants_TypeProviders())
			replacementTypes.put(r.getName(), r);

		// Replace where each type-provider child of this owner is used with it equivalent from replacement
		for (TypeProvider child : ((Node) owner).getDescendants_TypeProviders()) {
			Collection<TypeUser> kids = new ArrayList<TypeUser>(child.getWhereAssigned());
			for (TypeUser n : kids) {
				// Try to find a replacement equivalent from replacement object
				String name = n.getAssignedTLNamedEntity().getLocalName();
				TypeProvider r = replacementTypes.get(name);
				if (r == null) {
					r = replacement;
					// LOGGER.debug("ReplaceAll equivalent not found for " + n + ", using " + r + " instead");
				}
				if (scopeLibrary == null || ((Node) n).getLibrary().equals(scopeLibrary))
					if (n.isEditable()) {
						((TypeUser) n).setAssignedType(r);
						LOGGER.debug("replace " + n + " with " + r);
					}
				// LOGGER.debug("ReplaceAll replaced " + n + " with " + r);
			}
		}

		// Replace all users of this owner.
		replace(replacement, scopeLibrary);
	}

	public TypeNode getWhereUsedNode() {
		return whereUsedNode;
	}

	/**
	 * Remove user from where used list.
	 * 
	 * @param typeUser
	 *            - type user to remove from list
	 * @return true if removed, false if not in list
	 */
	public boolean remove(TypeUser typeUser) {
		if (!users.contains(typeUser))
			return false;
		users.remove(typeUser);
		return true;
	}

}
