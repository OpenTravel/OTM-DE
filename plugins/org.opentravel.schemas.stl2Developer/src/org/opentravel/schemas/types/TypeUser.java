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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * These nodes can be assigned a type.
 * 
 * This does NOT include base type extensions. See ExtensionOwner interface for base type users.
 * 
 * @author Dave
 *
 */
public interface TypeUser {

	/**
	 * @return true if the passed node can be assigned as the type.
	 */
	public boolean canAssign(Node type);

	/**
	 * @return the type assigned tl named entity reported by getTLModelObject().get*() which may be null
	 */
	public NamedEntity getAssignedTLNamedEntity();

	/**
	 * @return the type assigned tl model element reported by modelObject<?>.getTLType() which may be null
	 */
	public TLModelElement getAssignedTLObject();

	/**
	 * @return the node of the assigned type
	 */
	public TypeProvider getAssignedType();

	/**
	 * @return the type name field from the TLModelObject
	 */
	public String getAssignedTLTypeName();

	// public String getAssignedTypeName();

	public LibraryNode getLibrary();

	public String getName();

	public LibraryMemberInterface getOwningComponent();

	/**
	 * @return
	 */
	public Node getParent();

	/**
	 * @return null if the assigned type can be any type provider otherwise the fixed type to assign
	 */
	public TypeProvider getRequiredType();

	public TLModelElement getTLModelObject();

	/**
	 * @return
	 */
	TypeUserHandler getTypeHandler();

	public boolean isEditable();

	/**
	 * Remove the type assigned to the TL Model object. This method is the only method that nulls out the TL model
	 * object. Before the TL object is set to null, {@link #setAssignedType()} is called to removed where used links.
	 * 
	 * @see #setAssignedType()
	 */
	public void removeAssignedTLType();

	/**
	 * Set Assigned Type. Sets the where assigned on the associated tl model object provider.
	 * 
	 * @return true if state of owner changed, false otherwise
	 */
	public boolean setAssignedTLType(TLModelElement tlProvider);

	/**
	 * Clear the assigned type. This does <b>not</b> change the type in the TL property or attribute. This <b>does</b>
	 * remove the user from the assigned types where used list and adds the user to the unassignedNode's where used
	 * list.
	 * <p>
	 * The TypeUserHandler will skip setting TL assignment in an attempt to preserve actual assignment even if that
	 * library is not loaded.
	 * <p>
	 * The GUI does not provide the user with any means to clear an assignment. This method is used when libraries are
	 * closed to preserve the type assignment even though the provider is no longer loaded into the active model. The
	 * GUI will present the type name with prefix along with "Missing" if possible.
	 * 
	 * @see #removeAssignedTLType()
	 * @return true if state of owner changed, false otherwise
	 */
	public boolean setAssignedType();

	/**
	 * Set Assigned Type. Sets the Assigned type node and add this owner to that user list. This method assures their is
	 * a target and that the owner is editable. Sets the type class properties as well as the TLModel type
	 * <p>
	 * <b>NOTE:</b> (Conditional behavior to provide GUI assist) Since attributes can be renamed, there is no need to
	 * use the alias. Aliases are not TLAttributeType members so the GUI assist must convert before assignment. If the
	 * provider is an alias and the user is a simple type or attribute then the actual type provider is assigned, not
	 * the alias.
	 * <p>
	 * <b>NOTE:</b>
	 * 
	 * @param provider
	 *            is the type to attempt to assign. It may be substituted if necessary.
	 * 
	 * @return the actual or substituted provider assigned or else null
	 */
	public TypeProvider setAssignedType(TypeProvider provider);

	public void setName(String name);
}
