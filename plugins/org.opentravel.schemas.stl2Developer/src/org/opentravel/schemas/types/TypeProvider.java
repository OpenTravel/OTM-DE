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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.XsdObjectHandler;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;

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
	 * Add to where assigned and set listener
	 * 
	 * @param user
	 */
	public void addTypeUser(TypeUser user);

	/**
	 * @param user
	 *            to add to the where used list
	 */
	public void addWhereUsed(TypeUser user);

	public String getDescription();

	public LibraryNode getLibrary();

	public String getName();

	// /**
	// * @return the component node used to represent users of this type.
	// */
	// public INode getTypeNode();

	public LibraryMemberInterface getOwningComponent();

	public Node getParent();

	public TLModelElement getTLModelObject();

	/**
	 * @return a unmodifiable collection of type users that use this as a type definition or base type
	 */
	public Collection<TypeUser> getWhereAssigned();

	/**
	 * @return where used count for all versions in this versioned object chain
	 */
	public int getWhereAssignedCount();

	/**
	 * @return (where used count) the number of type users which are nodes that use this as a type definition or base
	 *         type
	 */
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
	public TypeProviderWhereUsedNode getWhereUsedNode();

	public XsdObjectHandler getXsdObjectHandler();

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
	 * OTM Enforces properties to have the same name as their assigned type except for VWA and Open Enum. Exceptions
	 * must override this method. This method does <b>not</b> account for edit-ability or inheritance as those are
	 * characteristics of the property not assigned type.
	 * 
	 * @return true if a property assigned this type can be renamed.
	 */
	public boolean isRenameableWhereUsed();

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

	public void setXsdHandler(XsdObjectHandler xsdObjectHandler);

	int getWhereUsedCount();

}
