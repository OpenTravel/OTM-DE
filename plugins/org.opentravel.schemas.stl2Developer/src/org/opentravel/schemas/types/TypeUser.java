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
	 * @return the type assigned tl named entity reported by modelObject<?>.getTLType() which may be null
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
	 * @return null if the assigned type can be any type provider otherwise the fixed type to assign
	 */
	public TypeProvider getRequiredType();

	public TLModelElement getTLModelObject();

	public String getTypeName();

	public boolean isEditable();

	/**
	 * Set Assigned Type. Sets the where assigned on the associated type provider.
	 */
	public boolean setAssignedType(TLModelElement tlProvider);

	/**
	 * Set Assigned Type. Sets the Assigned type node and add this owner to that user list. This method assures their is
	 * a target and that the owner is editable. Sets the type class properties as well as the TLModel type
	 * 
	 * @return true if assignment could be made, false otherwise
	 */
	public boolean setAssignedType(TypeProvider provider);

	/**
	 * Clear the assigned type by setting it to undefined.
	 */
	boolean setAssignedType();

	public void setName(String name);
}
