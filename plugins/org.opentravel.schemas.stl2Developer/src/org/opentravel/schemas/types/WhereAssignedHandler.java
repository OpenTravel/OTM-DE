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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
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
	protected WhereUsedNode<?> whereUsedNode = null;
	protected TypeProvider owner = null;

	/*********************************************************************************
	 * 
	 * Create a where assigned handler for this type provider <i>owner</i>.
	 */
	public WhereAssignedHandler(TypeProvider owner) {
		whereUsedNode = new TypeProviderWhereUsedNode(owner);
		this.owner = owner;
	}

	/**
	 * Add the passed user to the provider's where used list. Also add the user's library to the owner's library where
	 * used list.
	 * 
	 * @param user
	 *            type user to add if not already in list
	 */
	public void add(TypeUser user) {
		if (!users.contains(user)) {
			users.add((Node) user);
			// LOGGER.debug("Added " + user + " to " + owner + " where assigned list.");
		}

		// Also add to library where used
		if (owner.getLibrary() != null)
			owner.getLibrary().getWhereUsedHandler().add(user);
	}

	// TODO - make this the primary method and deprecate add()
	public void addUser(TypeUser user) {
		if (!users.contains(user)) {
			users.add((Node) user);
			setListener(user);

			// Also add to library where used
			if (owner.getLibrary() != null)
				owner.getLibrary().getWhereUsedHandler().add(user);

			// LOGGER.debug("Added " + user + " to " + owner + " where assigned list.");
		}
	}

	/**
	 * @return the owner
	 */
	public TypeProvider getOwner() {
		return owner;
	}

	/**
	 * This handler is associated with a type provider. It can have many listeners, one for each user of this type
	 * provider.
	 * <p>
	 * However, a user can only have one type assigned.
	 * 
	 * @param user
	 */
	public void setListener(TypeUser user) {
		if (user.getTLModelObject() == null)
			return;
		// Make sure there is at most one assignment listener
		for (ModelElementListener l : getAssignmentListeners(user))
			user.getTLModelObject().removeListener(l);
		// Make assignment
		user.getTLModelObject().addListener(new TypeUserAssignmentListener(this));

		// LOGGER.debug("Added listener for provider " + owner + " to user " + user);
	}

	public void setListener(ExtensionOwner owner) {
		// NO-OP
	}

	/**
	 * Get the assignment listener from passed user.
	 * <p>
	 * ONLY public to simplify JUnits
	 * 
	 * @param user
	 * @return
	 */
	public List<TypeUserAssignmentListener> getAssignmentListeners(TypeUser user) {
		List<TypeUserAssignmentListener> listeners = new ArrayList<TypeUserAssignmentListener>();
		Collection<ModelElementListener> userListeners = Collections.emptyList();
		if (user != null && user.getTLModelObject() != null)
			userListeners = user.getTLModelObject().getListeners();
		for (ModelElementListener l : userListeners)
			if (l instanceof TypeUserAssignmentListener)
				listeners.add((TypeUserAssignmentListener) l);
		assert listeners.size() < 2;
		// May not be set yet - assert listeners.get(0).getNode() == owner;
		return listeners;
	}

	public void removeListener(TypeUser user) {
		if (user == null)
			return; // No listeners to check

		for (TypeUserAssignmentListener l : getAssignmentListeners(user))
			// if (l.getNode() == owner || l.getNode().isDeleted())
			user.getTLModelObject().removeListener(l);

		assert getAssignmentListeners(user).isEmpty();

		// LOGGER.debug("Removed assignment listener to " + owner + " from user " + user);
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

		for (TypeProvider n : ((Node) ((Node) owner).getOwningComponent()).getDescendants_TypeProviders())
			if (!((Node) n).isDeleted())
				ul.addAll(n.getWhereAssigned());
		return ul;
	}

	public int getWhereAssignedIncludingDescendantsCount() {
		// same logic as getWhereAssignedIncludingDescendants but without the array
		// used in background decorators
		int count = 0;
		for (Node n : users)
			if (n instanceof TypeUser)
				count++;

		if (((Node) owner).getOwningComponent() == null)
			return count; // happens when building inheritance wizard tree

		for (TypeProvider n : ((Node) ((Node) owner).getOwningComponent()).getDescendants_TypeProviders())
			if (!((Node) n).isDeleted())
				count += n.getWhereAssignedCount();
		return count;
		// return getWhereAssignedIncludingDescendants().size();
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
				if (scopeLibrary == null || (n.getLibrary() != null && n.getLibrary().equals(scopeLibrary))) {
					n.setAssignedType(replacement);
					// if (n.setAssignedType(replacement))
					// LOGGER.debug("replace " + ((Node) n).getNameWithPrefix() + " with "
					// + ((Node) replacement).getNameWithPrefix());
					// else
					// LOGGER.debug("Failed to replace " + n + " with " + replacement);
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
	 * descendants of this owner node with matching replace descendant when possible. Also does the TL properties.
	 * <p>
	 * Note - user counts may change when business replace core objects because core is also a valid simple type.
	 * 
	 * @param scopeLibrary
	 *            - if not null, only change type users that are in the specified library.
	 */
	public void replaceAll(TypeProvider replacement, LibraryNode scopeLibrary) {

		// Create map of replacement candidates being all descendants of the replacement
		java.util.HashMap<String, TypeProvider> replacementTypes = new java.util.HashMap<String, TypeProvider>();
		for (TypeProvider r : ((Node) replacement).getDescendants_TypeProviders())
			replacementTypes.put(r.getName(), r);

		// Replace where each type-provider child of this owner is used with it equivalent from replacement
		for (TypeProvider child : owner.getDescendants_TypeProviders()) {
			Collection<TypeUser> kids = new ArrayList<TypeUser>(child.getWhereAssigned());
			// FIXME - Doing core_simpleFacetFacetNode causes errors
			for (TypeUser n : kids) {
				// Try to find a replacement equivalent from replacement object
				String name = n.getAssignedType().getName();
				TypeProvider r = replacementTypes.get(name);
				if (r == null) {
					r = replacement;
					LOGGER.debug("ReplaceAll equivalent not found for " + n + ", using " + r + " instead");
				}
				if (scopeLibrary == null || n.getLibrary().equals(scopeLibrary))
					if (n.isEditable()) {
						n.setAssignedType(r);
						LOGGER.debug("replace " + n + " with " + r);
					}
				// LOGGER.debug("ReplaceAll replaced " + n + " with " + r);
			}
		}

		// Replace all users of this owner.
		replace(replacement, scopeLibrary);
	}

	public WhereUsedNode<?> getWhereUsedNode() {
		return whereUsedNode;
	}

	/**
	 * Remove user from where used list.
	 * <p>
	 * Also removed from library's where used handler.
	 * <p>
	 * Also remove listeners.
	 * 
	 * @param typeUser
	 *            - type user to remove from list
	 * @return true if removed, false if not in list
	 */
	public boolean remove(TypeUser typeUser) {
		boolean result = removeUser(typeUser);
		removeListener(typeUser);
		return result;
	}

	/**
	 * Remove user from where used list. Also removed from library's where used handler. Listeners are not affected.
	 * <p>
	 * Safe to use in an event listener.
	 * 
	 * @param typeUser
	 *            - type user to remove from list
	 * @return true if removed, false if not in list
	 */
	public boolean removeUser(TypeUser typeUser) {
		if (!users.contains(typeUser))
			return false;
		users.remove(typeUser);

		// Also remove from library where used
		if (owner.getLibrary() != null)
			owner.getLibrary().getWhereUsedHandler().remove(typeUser);

		return true;
	}

	public void clear() {
		users.clear();
		whereUsedNode = null;
	}

}
