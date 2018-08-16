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
package org.opentravel.schemas.node.typeProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.handlers.XsdObjectHandler;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeProviderListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> type providers.
 * <p>
 * Role is to manage assignments including where used.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class TypeProviders extends ComponentNode implements TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeProviders.class);

	protected WhereAssignedHandler whereAssignedHandler = null;
	protected XsdObjectHandler xsdObjectHandler;

	public TypeProviders() {
		// For types that have no TL
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public TypeProviders(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public void addTypeUser(TypeUser user) {
		if (!((Node) user).isDeleted())
			whereAssignedHandler.addUser(user);
		else
			whereAssignedHandler.remove(user); // Safety check also does library where used
	}

	/**
	 * {@link #addTypeUser(TypeUser)}
	 */
	@Override
	public void addWhereUsed(TypeUser user) {
		whereAssignedHandler.add(user);
	}

	public LibraryMember cloneTL() {
		LibraryElement clone = super.cloneTLObj();
		assert clone instanceof LibraryMember;
		return (LibraryMember) clone;
	}

	// @Override
	public LibraryMemberInterface copy(LibraryNode destLib) throws IllegalArgumentException {
		// Not all type providers are library members
		if (!(this instanceof LibraryMemberInterface))
			return null;

		if (destLib == null)
			destLib = getLibrary();

		// Clone the TL object
		LibraryMember tlCopy = cloneTL();

		// Create contextual facet from the copy
		LibraryMemberInterface copy = NodeFactory.newLibraryMember(tlCopy);
		if (!(copy instanceof LibraryMemberInterface))
			throw new IllegalArgumentException("Unable to copy " + this);
		LibraryMemberInterface lm = copy;

		// Fix any contexts
		((Node) lm).fixContexts();

		destLib.addMember(lm);
		return lm;
	}

	@Override
	public void deleteTL() {
		if (getTLModelObject() instanceof LibraryMember)
			((LibraryMember) getTLModelObject()).getOwningLibrary()
					.removeNamedMember((LibraryMember) getTLModelObject());
		else
			LOGGER.debug("Unable to delete tl model object for: " + this);
	}

	@Deprecated
	// TODO - add the inherited flag
	public List<AbstractContextualFacet> getContextualFacets() {
		return getContextualFacets(false);
	}

	/**
	 * @return list of contextual facets or empty list. If contributed facets are found, their contributor is returned.
	 */
	public List<AbstractContextualFacet> getContextualFacets(boolean inherited) {
		if (!(this instanceof ContextualFacetOwnerInterface))
			return Collections.emptyList();

		ArrayList<AbstractContextualFacet> facets = new ArrayList<>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof ContributedFacetNode)
				facets.add(((ContributedFacetNode) n).getContributor());
			else if (n instanceof AbstractContextualFacet)
				facets.add((AbstractContextualFacet) n);

		if (inherited)
			for (Node n : getChildrenHandler().getInheritedChildren())
				if (n instanceof ContributedFacetNode)
					facets.add(((ContributedFacetNode) n).getContributor());
				else if (n instanceof AbstractContextualFacet)
					facets.add((AbstractContextualFacet) n);

		return facets;
	}

	@Deprecated
	// Add inherited flag
	public List<ContributedFacetNode> getContributedFacets() {
		return getContributedFacets(false);
	}

	/**
	 * @return list of contributed facet children of this object
	 */
	public List<ContributedFacetNode> getContributedFacets(boolean inherited) {
		if (!(this instanceof ContextualFacetOwnerInterface))
			return Collections.emptyList();

		ArrayList<ContributedFacetNode> facets = new ArrayList<>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof ContributedFacetNode)
				facets.add((ContributedFacetNode) n);

		if (inherited)
			for (Node n : getChildrenHandler().getInheritedChildren())
				if (n instanceof ContributedFacetNode)
					facets.add((ContributedFacetNode) n);

		return facets;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeProviderListener(this);
	}

	@Override
	public Collection<TypeUser> getWhereAssigned() {
		return whereAssignedHandler.getWhereAssigned();
	}

	/**
	 * Use the assignment handler to get the where assigned count which is the size of the users collection.
	 * 
	 * @return (where assigned count) the number of type users which are nodes that use this as a type definition or
	 *         base type
	 */
	@Override
	public int getWhereAssignedCount() {
		return getWhereAssignedHandler() != null ? whereAssignedHandler.getWhereAssignedCount() : 0;
	}

	@Override
	public WhereAssignedHandler getWhereAssignedHandler() {
		return whereAssignedHandler;
	}

	@Override
	public Collection<TypeUser> getWhereUsedAndDescendants() {
		return whereAssignedHandler.getWhereAssignedIncludingDescendants();
	}

	/**
	 * @return count of where this provider and descendants is used as type or extension
	 */
	@Override
	public int getWhereUsedAndDescendantsCount() {
		int count = 0;
		if (whereExtendedHandler != null)
			count += whereExtendedHandler.getWhereExtendedCount();
		if (whereAssignedHandler != null)
			count += whereAssignedHandler.getWhereAssignedIncludingDescendantsCount();
		return count;
	}

	/**
	 * Use the WhereUsedNode to return a count.
	 * 
	 * @return count of where all minor versions of this provider is used as type or extension
	 */
	@Override
	public int getWhereUsedCount() {
		return getWhereUsedNode() != null ? getWhereUsedNode().getWhereUsedCount() : 0;
	}

	@Override
	public TypeProviderWhereUsedNode getWhereUsedNode() {
		return (TypeProviderWhereUsedNode) whereAssignedHandler.getWhereUsedNode();
	}

	@Override
	public XsdObjectHandler getXsdObjectHandler() {
		return xsdObjectHandler;
	}

	// TODO - as this question either to the provider or user but not both
	/**
	 * @return true if this node can be assigned to an element reference
	 */
	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	/**
	 * @return true if this node can be assigned to an attribute or simple property
	 */
	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	/**
	 * @return true if this node can be assigned to an attribute, simple property or VWA attribute
	 */
	@Override
	public boolean isAssignableToVWA() {
		return false;
	}

	// ******************** Hierarchy methods **************************************************

	/**
	 * @return true if this object can be used as an assigned type or base type
	 */
	@Override
	public boolean isNamedEntity() {
		return true;
	}

	@Override
	public boolean isRenameable() {
		return isEditable_newToChain();
	}

	@Override
	public boolean isRenameableWhereUsed() {
		return false;
	}

	@Override
	public void removeAll(boolean force) {
		Collection<TypeUser> users = new ArrayList<>(getWhereAssigned());
		for (TypeUser user : users) {
			if (!force)
				user.setAssignedType(); // will not force type assignment
			else {
				// force the type to be set to Missing
				user.removeAssignedTLType();
				// Verify Results
				assert user.getAssignedType() != this;
			}
		}
	}

	/**
	 * {@link #removeWhereAssigned(TypeUser)}
	 */
	@Override
	public void removeListener(TypeUser user) {
		whereAssignedHandler.removeListener(user);
	}

	@Override
	public void removeWhereAssigned(TypeUser user) {
		whereAssignedHandler.removeListener(user);
		whereAssignedHandler.removeUser(user);
	}

	/**
	 * Replace all type assignments (base and assigned type) to this node with assignments to passed node. For every
	 * assignable descendant of sourceNode, find where the corresponding sourceNode children are used and change them as
	 * well. See {@link #replaceWith(Node)}.
	 * 
	 * @param this
	 *            - replace assignments to this node (sourceNode)
	 * @param replacement
	 *            - use replacement TypeProvider node to be assigned
	 * @param scope
	 *            (optional) - scope of the search or null for all libraries
	 */
	@Override
	public void replaceTypesWith(TypeProvider replacement, LibraryNode scope) {

		getWhereAssignedHandler().replaceAll(replacement, scope);

		// If this has been extended, replace where extended
		getWhereExtendedHandler().replace((Node) replacement, scope);
	}

	/**
	 * {@link #addTypeUser(TypeUser)}
	 */
	@Override
	public void setListener(TypeUser user) {
		whereAssignedHandler.setListener(user);
	}

	/**
	 * Set name to type users where this alias is assigned. Only if the parent is type that requires assigned users to
	 * use the owner's name
	 */
	public void setNameOnWhereAssigned(String n) {
		if (!isRenameableWhereUsed())
			for (TypeUser u : getWhereAssigned())
				u.setName(n);
	}

	@Override
	public void setXsdHandler(XsdObjectHandler xsdObjectHandler) {
		this.xsdObjectHandler = xsdObjectHandler;
	}

	/**
	 * Assign the name to all properties that use this type provider. Also update all type users contained by children.
	 * 
	 * @param name
	 */
	protected void updateNames(String name) {
		for (TypeUser user : getWhereAssigned()) {
			if (user instanceof PropertyNode)
				user.setName(NodeNameUtils.fixBusinessObjectName(name));
		}
		// Fix name for all facets of this provider
		for (Node child : getChildren()) {
			if (child instanceof ContributedFacetNode)
				child = ((ContributedFacetNode) child).getContributor();
			if (child instanceof TypeProvider)
				for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
					((Node) users).visitAllNodes(new NodeVisitors().new FixNames());
		}
	}

}
