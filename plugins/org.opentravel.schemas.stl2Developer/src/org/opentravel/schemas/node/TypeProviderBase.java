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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all type providers. Primary role is to be a facade to WhereAssignedHandler.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class TypeProviderBase extends ComponentNode implements TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeProviderBase.class);
	private WhereAssignedHandler whereAssignedHandler = null;

	public TypeProviderBase() {
		// FIXME - why does facetNode need this constructor?
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public TypeProviderBase(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> navChildren = getNavChildren(deep);
		navChildren.addAll(getInheritedChildren());
		navChildren.add(getWhereUsedNode());
		return navChildren;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true; // allow where used nodes
	}

	@Override
	public void addWhereUsed(TypeUser user) {
		whereAssignedHandler.add(user);
	}

	@Override
	public Collection<TypeUser> getWhereAssigned() {
		return whereAssignedHandler.getWhereAssigned();
	}

	/**
	 * @return (where used count) the number of type users which are nodes that use this as a type definition or base
	 *         type
	 */
	@Override
	public int getWhereAssignedCount() {
		return whereAssignedHandler.getWhereAssignedCount();
	}

	@Override
	public WhereAssignedHandler getWhereAssignedHandler() {
		return whereAssignedHandler;
	}

	@Override
	public Collection<TypeUser> getWhereUsedAndDescendants() {
		return whereAssignedHandler.getWhereAssignedIncludingDescendants();
	}

	@Override
	public int getWhereUsedAndDescendantsCount() {
		return whereAssignedHandler.getWhereAssignedIncludingDescendantsCount();
	}

	@Override
	public TypeNode getWhereUsedNode() {
		return whereAssignedHandler.getWhereUsedNode();
	}

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
	public void removeAll() {
		Collection<TypeUser> users = new ArrayList<TypeUser>(getWhereAssigned());
		for (TypeUser user : users)
			removeTypeUser(user);
	}

	@Override
	public void removeListener(TypeUser user) {
		whereAssignedHandler.removeListener(user);
	}

	@Override
	public void removeTypeUser(TypeUser user) {
		whereAssignedHandler.removeListener(user);
		whereAssignedHandler.remove(user);
	}

	@Override
	public void setListener(TypeUser user) {
		whereAssignedHandler.setListener(user);
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

		for (Node child : getChildren()) {
			for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
				((Node) users).visitAllNodes(new NodeVisitors().new FixNames());
		}

	}
}
