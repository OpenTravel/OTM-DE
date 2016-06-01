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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles type assignment. Type assignment constrains the value space of the property. It does not change its
 * structure.
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
	 * Get the assigned type or NULL
	 */
	public TypeProvider get() {
		if (getTLModelElement() == null)
			if (getOwner().getXsdNode() != null)
				return (TypeProvider) ModelNode.getUndefinedNode();
			else if (((TypeUser) getOwner()).getRequiredType() != null)
				return ((TypeUser) getOwner()).getRequiredType();
			else
				return (TypeProvider) ModelNode.getUnassignedNode();

		Node n = Node.GetNode(getTLModelElement());
		// if (n == null)
		// LOGGER.debug("get(); of assigned type is null. Perhaps containing library has not been loaded and modeled yet.");
		// throw new IllegalStateException("TypeUser.get() is null.");
		return (TypeProvider) n;
	}

	/**
	 * @return the TL Model Element assigned to this type user as a type or null if none
	 */
	@Override
	public TLModelElement getTLModelElement() {
		NamedEntity tlNE = getTLNamedEntity();
		if (tlNE == null)
			return null;
		if (!(tlNE instanceof TLModelElement))
			throw new IllegalStateException(this + " assigned type is not a model element.");
		return (TLModelElement) tlNE;
	}

	/**
	 * @return the TL named entity assigned to this type user as a type
	 */
	@Override
	public NamedEntity getTLNamedEntity() {
		ModelObject<?> mo = ((Node) owner).getModelObject();
		return mo.getTLType(); // null if built-in
	}

	public boolean set() {
		return set(ModelNode.getUnassignedNode());
	}

	public boolean set(TLModelElement tlProvider) {
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
		if (!owner.isEditable())
			return false;

		if (target == null)
			target = ModelNode.getUnassignedNode();

		// getDefaultType will return null if it can be assigned to any type provider
		if (owner.getRequiredType() != null) {
			// change the name if appropriate
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
		if (target.getLibrary().isBuiltIn() && ((Node) target).isXsdType()
				&& target.getLibrary().getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
			tlTarget = ((Node) target).getXsdNode().getTLModelObject();

		// Add handler listener
		((TypeProvider) target).setListener(owner);

		if (tlOwner instanceof TLSimple)
			if (tlTarget instanceof TLAttributeType) {
				((TLSimple) tlOwner).setParentType((TLAttributeType) tlTarget);
			} else
				return false; // do nothing
		else if (tlOwner instanceof TLProperty)
			if (tlTarget instanceof TLPropertyType)
				((TLProperty) tlOwner).setType((TLPropertyType) tlTarget);
			else
				return false; // do nothing
		else if (tlOwner instanceof TLAttribute)
			if (tlTarget instanceof TLAttributeType)
				((TLAttribute) tlOwner).setType((TLAttributeType) tlTarget);
			else
				return false;
		else if (tlOwner instanceof TLSimpleFacet)
			if (tlTarget instanceof NamedEntity) {
				((TLSimpleFacet) tlOwner).setSimpleType((NamedEntity) tlTarget);
				// Listener will set the simple attribute node
			} else
				return false;
		else if (tlOwner instanceof TLnSimpleAttribute)
			if (tlTarget instanceof TLAttributeType)
				((TLnSimpleAttribute) tlOwner).setType((NamedEntity) tlTarget);
			else
				return false;
		else if (tlOwner instanceof TLAbstractEnumeration) {
			TLExtension extension = new TLExtension();
			extension.setExtendsEntity((NamedEntity) tlTarget);
			((TLAbstractEnumeration) tlOwner).setExtension(extension);
		}
		// Safety check. Will set if:
		// 1. no listeners
		// 2. No event thrown because the tl object original and target types are the same.
		// 3. if it is an implied type (role, enum, etc)
		if (owner.getAssignedType() != target) {
			// set((Node) target, (Node) oldValue);
			LOGGER.debug("Trouble right here in river city. Assigned type was not read back as assigned.");
			// throw new IllegalStateException("Type was not assigned.");
		}

		// change the name if appropriate
		NodeNameUtils.fixName((Node) owner);

		// remove where used listener from old assigned type
		if (oldValue != null) {
			if (oldValue != target) {
				((TypeProvider) oldValue).removeListener(owner);
				((TypeProvider) oldValue).getWhereAssignedHandler().remove(owner);
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
