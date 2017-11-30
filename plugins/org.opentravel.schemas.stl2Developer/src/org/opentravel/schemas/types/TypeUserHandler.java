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

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles type assignment. Type assignment constrains the value space of the property. It does not change its
 * structure. Type assignments are made to the undelying TL model object. Events from TL model manage the where
 * assigned.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeUserHandler extends AbstractAssignmentHandler<TypeProvider> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeUserHandler.class);

	private TypeUser owner = null;

	public TypeUserHandler(TypeUser n) {
		super();
		owner = n;
	}

	@Override
	public Node getOwner() {
		return (Node) owner;
	}

	/**
	 * Get the assigned type from the TLModelObject or ModelNode-UnassignedNode
	 */
	@Override
	public TypeProvider get() {
		Node n = null;
		TLModelElement assignedTL = getAssignedTLModelElement();
		if (assignedTL != null) {
			n = Node.GetNode(assignedTL.getListeners());
			if (n != null)
				return (TypeProvider) n;
		} else {
			// This is the "Missing" case.
			if (owner instanceof SimpleAttributeFacadeNode)
				n = (Node) ModelNode.getEmptyNode();
			else if ((owner instanceof TypeProvider) && (((TypeProvider) owner).getXsdObjectHandler() != null))
				// This is a simple type from an XSDSimpleType or XSDComplexType
				n = (Node) ((TypeProvider) owner).getXsdObjectHandler().getRequiredType();
		}

		// Try to recover.
		//
		// If the owning library has not been modeled, then return Unassigned.
		if (n == null && assignedTL != null) {
			Node ownerLib = Node.GetNode(((NamedEntity) assignedTL).getOwningLibrary());
			if (ownerLib == null) {
				LOGGER.debug("Owning library of assigned type  has not been modeled.");
				return ModelNode.getUnassignedNode();
			}
		}

		// Aliases may not be found because the parent's children cache was flushed.
		// re-hydrate the cache and try again.
		if (assignedTL instanceof TLAlias && n == null) {
			LOGGER.debug("Assigned an alias as type " + n);
			Node to = Node.GetNode(((TLAlias) assignedTL).getOwningEntity());
			if (to != null)
				to.getChildren(); // Inflate the children handler
			n = Node.GetNode(assignedTL.getListeners());
		}

		if (n == null)
			n = ModelNode.getUnassignedNode();

		return (TypeProvider) n;
	}

	/**
	 * @return the TL Model Element assigned to this type user as a type or null if none
	 */
	@Override
	public TLModelElement getAssignedTLModelElement() {
		// NamedEntity tlNE = getTLAssignedNamedEntity();
		NamedEntity tlNE = owner.getAssignedTLNamedEntity();
		if (tlNE == null)
			return null;
		if (!(tlNE instanceof TLModelElement))
			throw new IllegalStateException(this + " assigned type is not a TLModelElement.");
		return (TLModelElement) tlNE;
	}

	/**
	 * @return the TL named entity assigned to this type user as a type
	 */
	@Override
	public NamedEntity getTLAssignedNamedEntity() {
		return owner.getAssignedTLNamedEntity();
		// ModelObject<?> mo = ((Node) owner).getModelObject();
		// return mo.getTLType(); // null if built-in
	}

	public boolean set() {
		return set(ModelNode.getUnassignedNode());
	}

	/**
	 * Looks up the node associated with the tlProvider and uses that to set the type. If the provider does not have
	 * associated node or the node is not a type provider the type assignment is cleared.
	 * 
	 * @param tlProvider
	 * @return
	 */
	@Deprecated
	public boolean set(TLModelElement tlProvider) {
		assert false;
		Node target = Node.GetNode(tlProvider);
		if (target instanceof TypeProvider)
			return set((TypeProvider) target); // Set again to trigger where used behavior
		return set();
	}

	/**
	 * Set Assigned Type. Sets the Assigned type node and add this owner to that user list via where used listener. This
	 * method assures there is a target and that the owner is editable. Sets the TLModel type If the target represents a
	 * complex type (BO, Core, facet, alias) then the owner's name is changed.
	 * 
	 * @return true if assignment could be made, false otherwise
	 */
	public boolean set(TypeProvider target) {
		// LOGGER.debug("START - Assign type " + target + " to " + owner);
		if (owner == null || !owner.isEditable())
			return false;

		// Make any corrections need to the target
		//
		// Owner has a specific required type.
		if (owner.getRequiredType() != null) {
			target = owner.getRequiredType();
			return false;
		}
		if (target == null)
			target = ModelNode.getUnassignedNode();

		// Get the tl object
		TLModelElement tlTarget = target.getTLModelObject();
		// Compiler wants the actual XSD type not the tlSimple for XSD types.
		if (target.getLibrary() != null && target.getLibrary().isBuiltIn()
				&& target.getLibrary().getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
			if (target.getXsdObjectHandler() != null)
				tlTarget = target.getXsdObjectHandler().getTLLibraryMember(); // get srcTL not builtTL
		assert tlTarget != null;

		// Save old type assignment
		TypeProvider oldProvider = owner.getAssignedType();

		// Let the node handle assigning to the TL object
		boolean result = owner.setAssignedType(tlTarget);
		if (result) {
			// Remove old type assignment
			oldProvider.removeTypeUser(owner);
			// // Add where used and type assignment listener
			target.addTypeUser(owner);
		}
		// Confirm results
		if (result && get() != target) {
			LOGGER.debug("Failed to assign " + target + " to " + owner);
			return false;
		}
		return result;
	}

	@Deprecated
	public boolean setOLD(TypeProvider target) {
		if (owner == null || !owner.isEditable())
			return false;

		if (target == null)
			target = ModelNode.getUnassignedNode();

		// getRequiredType will return null if it can be assigned any type provider
		if (owner.getRequiredType() != null) {
			// Has a fixed type - just change the name if appropriate
			NodeNameUtils.fixName((Node) owner);
			// LOGGER.debug("No type set on " + owner + " because there is a required type.");
			return true;
		}

		// LOGGER.debug("START - Setting type of " + owner + " to " + target);

		assert owner instanceof TypeUser;
		assert target instanceof TypeProvider;

		// Set the tl type and let the listeners handle the rest
		TLModelElement tlOwner = owner.getTLModelObject();
		TLModelElement tlTarget = target.getTLModelObject();
		TypeProvider oldValue = owner.getAssignedType(); // hold onto to remove listener at end

		// Validation will not be correct if a built-in type is represented by the TLSimple
		if (target.getLibrary() != null && target.getLibrary().isBuiltIn()
				&& target.getLibrary().getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
			if (target.getXsdObjectHandler() != null)
				tlTarget = target.getXsdObjectHandler().getTLLibraryMember(); // get srcTL not builtTL

		// // Validation will not be correct if a built-in type is represented by the TLSimple
		// if (target.getLibrary() != null && target.getLibrary().isBuiltIn() && ((Node) target).isXsdType()
		// && target.getLibrary().getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
		// tlTarget = ((Node) target).getXsdNode().getTLModelObject();

		// Add handler listener
		target.setListener(owner);

		// TODO - either factor out as done on get() or migrate factored code into get()
		// FIXME - the switching logic is all wrong! use owner
		owner.setAssignedType(tlTarget);
		if (tlOwner instanceof TLSimple)
			owner.setAssignedType(tlTarget);
		// if (tlTarget instanceof TLAttributeType) {
		// ((TLSimple) tlOwner).setParentType((TLAttributeType) tlTarget);
		// } else
		// return false; // do nothing
		else if (tlOwner instanceof TLProperty)
			owner.setAssignedType(tlTarget);
		// if (tlTarget instanceof TLPropertyType)
		// ((TLProperty) tlOwner).setType((TLPropertyType) tlTarget);
		// else
		// return false; // do nothing
		else if (tlOwner instanceof TLAttribute) {
			owner.setAssignedType(tlTarget);
			// if (((TLAttribute) tlOwner).isReference()) {
			// if (owner.canAssign((Node) target)) {
			// ((TLAttribute) tlOwner).setType((TLPropertyType) tlTarget);
			// // ((TLAttribute) tlOwner).setName(NodeNameUtils.fixAttributeRefName(target.getName()));
			// } else
			// return false;
			// } else if (tlTarget instanceof TLAttributeType)
			// ((TLAttribute) tlOwner).setType((TLAttributeType) tlTarget);
			// else
			// return false;
		} else if (tlOwner instanceof TLSimpleFacet)
			if (tlTarget instanceof NamedEntity) {
				((TLSimpleFacet) tlOwner).setSimpleType((NamedEntity) tlTarget);
				// Listener will set the simple attribute node
			} else
				return false;
		// else if (tlOwner instanceof TLnSimpleAttribute) {
		// assert false; // No longer used
		// if (((TLnSimpleAttribute) tlOwner).getParentObject() instanceof TLValueWithAttributes)
		// if (tlTarget instanceof TLCoreObject)
		// return false; // Core is a tl attribute type but not allowed assigned to VWA
		// if (tlTarget instanceof TLAttributeType)
		// ((TLnSimpleAttribute) tlOwner).setType((NamedEntity) tlTarget);
		// else
		// return false;
		// }
		else if (tlOwner instanceof TLAbstractEnumeration) {
			TLExtension extension = new TLExtension();
			extension.setExtendsEntity((NamedEntity) tlTarget);
			((TLAbstractEnumeration) tlOwner).setExtension(extension);
		}
		// Safety check. Will set if:
		// 1. no listeners
		// 2. No event thrown because the tl object original and target types are the same.
		// 3. if it is an implied type (role, enum, etc)
		if (get() != target) {
			// if (owner.getAssignedType() != target) {
			TypeProvider at;
			// Could be the XSD case
			if (target.getXsdObjectHandler() != null)
				at = owner.getAssignedType();
			// set((Node) target, (Node) oldValue);
			LOGGER.debug("Trouble right here in river city. Assigned type was not read back as assigned.");
			// throw new IllegalStateException("Type was not assigned.");
		}

		// change the name if appropriate
		NodeNameUtils.fixName((Node) owner);

		// remove where used listener from old assigned type
		if (oldValue != null) {
			if (oldValue != target) {
				oldValue.removeListener(owner);
				oldValue.getWhereAssignedHandler().remove(owner);
			}
			// update library where used
			if (oldValue.getLibrary() != target.getLibrary()) {
				if (target.getLibrary() != null)
					target.getLibrary().getWhereUsedHandler().add(owner);
				if (oldValue.getLibrary() != null)
					oldValue.getLibrary().getWhereUsedHandler().remove(owner);
			}
		}

		// LOGGER.debug("END -" + owner + " changed assigment from " + oldValue + " to " + target);
		return true;
	}
}
