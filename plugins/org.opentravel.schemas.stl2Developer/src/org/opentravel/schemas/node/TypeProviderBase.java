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

import java.util.Collection;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all type providers.
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
	public WhereAssignedHandler getWhereAssignedHandler() {
		return whereAssignedHandler;
	}

	@Override
	public void addWhereUsed(TypeUser user) {
		whereAssignedHandler.add(user);
	}

	@Override
	public Collection<Node> getWhereUsed() {
		return whereAssignedHandler.getWhereAssigned();
	}

	/**
	 * @return (where used count) the number of type users which are nodes that use this as a type definition or base
	 *         type
	 */
	@Override
	public int getWhereUsedCount() {
		return whereAssignedHandler.getWhereAssigned().size();
	}

	@Override
	public Collection<Node> getWhereUsedAndDescendants() {
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

	@Override
	public void setListener(TypeUser user) {
		whereAssignedHandler.setListener(user);
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

	/**
	 * Replace this provider with replacement for all users of this provider as a type. Also replaces type usage of
	 * descendants of this owner node. Also does the TL properties. Note - user counts may change when business replace
	 * core objects because core is also a valid simple type.
	 * 
	 * It is OK for the owner to not be in a library. This happens when it is being replaced.
	 * 
	 * @param replacement
	 *            is the TypeProvider to use instead. Must be a typeProvider. skips assignment if replacement is not
	 *            compatible with user.
	 * @param libraryScope
	 *            - if null to entire model, otherwise only replace users within specified library.
	 */
	public void replace(TypeProvider replacement) {
		// if (whereUsedHandler.getWhereUsed().isEmpty()) &&
		// if (getTypeUsers().isEmpty() && getBaseUsers().isEmpty())
		// return;
		LOGGER.debug("Replacing " + this + " with " + replacement);
		throw new IllegalStateException("REPLACE user handler not implemented.");
	}

	// //////////////////////////////////////////////////////////////

	/**
	 * @return true if this object can be used as an assigned type or base type
	 */
	@Override
	public boolean isTypeProvider() {
		return true;
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
	 * @return true if this node can be assigned to an element reference
	 */
	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

}
