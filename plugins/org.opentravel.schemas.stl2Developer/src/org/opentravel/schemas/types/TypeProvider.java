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
package org.opentravel.schemas.types;

import java.util.Collection;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * Implementations of this interface are type definitions or other assignable nodes that represent type definitions such
 * as Aliases.
 * 
 * Methods include managing where used and access to values that can be assigned to the user from the provider.
 * 
 * Note - the only way to add a user is via the TypeUser interface. (user.setAssignedType())
 * 
 * @author Dave Hollander
 * 
 */
public interface TypeProvider {

	/**
	 * @param user
	 *            to add to the where used list
	 */
	public void addWhereUsed(TypeUser user);

	public String getDescription();

	public LibraryNode getLibrary();

	public String getName();

	public TLModelElement getTLModelObject();

	/**
	 * @return the component node used to represent users of this type.
	 */
	public INode getTypeNode();

	/**
	 * @return a unmodifiable collection of type users that use this as a type definition or base type
	 */
	public Collection<TypeUser> getWhereAssigned();

	/**
	 * @return (where used count) the number of type users which are nodes that use this as a type definition or base
	 *         type
	 */
	public int getWhereAssignedCount();

	public WhereAssignedHandler getWhereAssignedHandler();

	/**
	 * @return a unmodifiable collection of nodes that use this or any of its descendants as a type definition or base
	 *         type
	 */
	public Collection<TypeUser> getWhereUsedAndDescendants();

	/**
	 * @return count of users of this type provider and its descendants
	 */
	public int getWhereUsedAndDescendantsCount();

	/**
	 * @return a node suitable for use in navigator to represent the where used collection
	 */
	public TypeNode getWhereUsedNode();

	/**
	 * @return true if this node can be assigned to an element reference
	 */
	public boolean isAssignableToElementRef();

	/**
	 * @return true if this node can be assigned to an attribute or simple property
	 */
	public boolean isAssignableToSimple();

	/**
	 * @return true if this node can be assigned to an attribute, simple property or VWA attribute
	 */
	public boolean isAssignableToVWA();

	public boolean isAssignedByReference();

	/**
	 * @return true if this object can be used as an assigned type or base type
	 */
	public boolean isNamedEntity();

	/**
	 * Remove provider as type for all users.
	 */
	public void removeAll();

	/**
	 * Remove the listener for the type user
	 * 
	 * @param user
	 */
	public void removeListener(TypeUser user);

	/**
	 * Remove the type user from the where assigned list and its listener
	 * 
	 * @param user
	 */
	public void removeTypeUser(TypeUser user);

	public void setListener(TypeUser typeUser);

}
