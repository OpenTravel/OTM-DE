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
import org.opentravel.schemas.modelObject.ModelObject;
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
	 * @return the node of the assigned type
	 */
	@Deprecated
	public ModelObject<?> getAssignedModelObject();

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

	public String getTypeName();

	/**
	 * @return null if the assigned type can be any type provider otherwise the fixed type to assign
	 */
	public TypeProvider getRequiredType();

	public TLModelElement getTLModelObject();

	public boolean isEditable();

	// /**
	// * @return the node of the assigned type
	// */
	// public NamedEntity getAssignedTLObject();

	// /**
	// * @return true if a type can be assigned to this node.
	// */
	// public boolean isTypeUser();

	// /**
	// * Remove the assigned type. Removes this node from typeNode's list of typeUsers. Sets typeNode
	// * = null.
	// */
	// public void removeAssignedType();

	/**
	 * Sets the type assigned to this node if appropriate. Sets TL type, type node and type users on the target node.
	 * 
	 * Restrictions enforced: 1) Simple Facets and Attribute Properties must have simple type. 2) VWA Attribute facets
	 * may have simple type or VWA or Open Enum. ) SimpleProperties may not be circularly assigned to their owning
	 * components. 4) node must be editable or in XSD library to set the TL type.
	 * 
	 * If typeNode is implied, the TL type is cleared.
	 * 
	 * @param typeNode
	 *            to assign, or null to clear assignments.
	 * @return true if set
	 */

	/**
	 * Set Assigned Type. Sets the Assigned type node and add this owner to that user list. This method assures their is
	 * a target and that the owner is editable. Sets the type class properties as well as the TLModel type
	 * 
	 * @return true if assignment could be made, false otherwise
	 */
	public boolean setAssignedType(TypeProvider provider);

	/**
	 * Set Assigned Type. Sets the where assigned on the associated type provider.
	 */
	public boolean setAssignedType(TLModelElement tlProvider);

	/**
	 * Clear the assigned type by setting it to undefined.
	 */
	boolean setAssignedType();
}
