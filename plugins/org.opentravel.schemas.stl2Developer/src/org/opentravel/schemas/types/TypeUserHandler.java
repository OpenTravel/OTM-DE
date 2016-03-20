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

import javax.xml.namespace.QName;

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
import org.opentravel.schemas.modelObject.XsdModelingUtils;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
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
		if (n == null)
			LOGGER.debug("get(); of assigned type is null. Perhaps containing library has not been loaded and modeled yet.");
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
	public void replaceTypeProvider(Node replacement, LibraryNode scopeLibrary) {
		// Nothing to do.
		// if (getTypeUsers().isEmpty() && getBaseUsers().isEmpty())
		// return;
		// LOGGER.debug("Replacing " + owner + " used " + getTypeUsers().size() + " times.");
		//
		// if (replacement == null || !replacement.isTypeProvider())
		// return;
		//
		// // Replace the users of this object as a type.
		// replaceUsers(replacement, scopeLibrary);
		//
		// // Replace users of all descendants.
		// // Create map of replacement candidates
		// HashMap<String, Node> replacementTypes = new HashMap<String, Node>();
		// for (Node r : replacement.getDescendants_NamedTypes())
		// replacementTypes.put(r.getName(), r);
		//
		// for (Node n : owner.getDescendants_NamedTypes()) {
		// // Try to find a replacement equivalent from replacement object
		// Node r = replacementTypes.get(n.getName());
		// if (r == null)
		// r = ModelNode.getUnassignedNode();
		// n.getTypeClass().replaceUsers(r, scopeLibrary);
		// }
		throw new IllegalStateException("REPLACE user handler not implemented.");
	}

	/**
	 * Set Where Used assigned type to a new value.
	 * 
	 * @param newValue
	 *            is node to use as this onwer's type
	 * @param oldValue
	 *            node that was this owner's type
	 */
	public void set(Node newValue, Node oldValue) {
		// FIXME = why is this here? Where should it be? Is it needed?
		// Use unassigned instead of null. Provides location to find all unassigned types.
		if (newValue == null)
			newValue = ModelNode.getUnassignedNode();

		// remove owner from old value's user list
		if (oldValue != null)
			oldValue.getTypeUsers().remove(owner);

		// Add owner to new value's type user list
		if (!newValue.getTypeUsers().contains(owner))
			newValue.getTypeUsers().add((Node) owner); // FIXME - no cast should be needed

		// LOGGER.debug("type.set() " + newValue + " replaced " + oldValue + " on " + owner);
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
			LOGGER.debug("No type set on " + owner + " because there is a required type.");
			return true;
		}

		LOGGER.debug("START - Setting type of " + owner + " to " + target);

		assert owner instanceof TypeUser;
		assert target instanceof TypeProvider;

		// Set the tl type and let the listeners handle the rest
		TLModelElement tlOwner = owner.getTLModelObject();
		TLModelElement tlTarget = target.getTLModelObject();
		TypeProvider oldValue = owner.getAssignedType(); // hold onto to remove listener at end

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
			set((Node) target, (Node) oldValue);
			LOGGER.debug("Trouble right here in river city.");
			throw new IllegalStateException("Type was not assigned.");
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

		LOGGER.debug("END -" + owner + " changed assigment from " + oldValue + " to " + target);
		return true;
	}

	/**
	 * Find the type to assign to this node. Used when TL assignment has not been made. Uses defaultType and
	 * documentation to find the type to assign.
	 * 
	 * @param node
	 * @return
	 */
	public TypeProvider findTypeToAssign(Node node) {
		if (!(node instanceof TypeUser))
			return null;
		QName typeQname = null;
		TypeUser user = (TypeUser) node;

		// assign a default type if no QName
		TypeProvider target = user.getRequiredType();

		// try to get qname from documentation
		typeQname = node.getTLTypeQName();
		if (typeQname != null && !typeQname.getLocalPart().isEmpty())
			target = (TypeProvider) NodeFinders.findTypeProviderByQName(typeQname);
		else
			LOGGER.debug("Qname is null for " + node);

		return target;
	}

	/**
	 * @return true only if the assigned type is in a library and not implied.
	 */
	public boolean hasAssignedType() {
		Node type = (Node) owner.getAssignedType();
		if (type == null)
			return false;
		if (type.getLibrary() == null)
			return false;
		return (type instanceof ImpliedNode) ? false : true;
	}

	// Replace type assignments to all type and base user nodes IFF in scope and editable.
	private void replaceUsers(Node replacement, LibraryNode scope) {
		throw new IllegalStateException("replace users is not implemented yet.");
		// FixNames nameFixer = new NodeVisitors().new FixNames();
		// if (!(replacement instanceof ImpliedNode) && replacement.getLibrary() == null) {
		// LOGGER.warn("Error - replacement " + replacement + " is not in a library.");
		// replacement = ModelNode.getUnassignedNode();
		// }
		// // base users
		// ArrayList<Node> users = new ArrayList<Node>(getBaseUsers());
		// for (Node n : users) {
		// if (n.isEditable() && (scope == null || n.getLibrary().equals(scope))) {
		// n.getTypeClass().setAssignedBaseType(replacement);
		// baseUsers.remove(n);
		// }
		// }
		//
		// users = new ArrayList<Node>(getTypeUsers());
		// for (Node n : users) {
		// if (n.isEditable() && (scope == null || n.getLibrary().equals(scope))) {
		// if (n.getTypeClass().setAssignedType((TypeProvider) replacement)) {
		// nameFixer.visit(n);
		// getTypeUsers().remove(n);
		// } else {
		// // setAssignedType if replacement failed, try unassigned.
		// if (n.getTypeClass().setAssignedType(ModelNode.getUnassignedNode())) {
		// getTypeUsers().remove(n);
		// // LOGGER.debug("Set type to " + ModelNode.getUnassignedNode() + " for " +
		// // n);
		// } else {
		// LOGGER.warn("ERROR Setting type to " + replacement + " failed for " + n);
		// }
		// }
		// }
		// }
	}

	public QName getQName() {
		QName typeQname = null;

		NamedEntity type = getTLNamedEntity();
		if (type != null) {
			String ns = type.getNamespace();
			String ln = type.getLocalName();
			if (ns != null && ln != null)
				typeQname = new QName(type.getNamespace(), type.getLocalName());

			// If still empty, try the XSD type information in the documentation
			if (typeQname == null)
				typeQname = XsdModelingUtils.getAssignedXsdType((Node) owner);
		}
		return typeQname;

	}

}
