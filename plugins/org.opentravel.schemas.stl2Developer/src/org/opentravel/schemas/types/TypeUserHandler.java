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

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.node.objectMembers.SharedFacetNode;
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
				if (n instanceof TypeProvider)
					return (TypeProvider) n;
				else
					LOGGER.debug("Error - not type provider: " + n);
		} else {
			// This is the "Missing" case.
			if (owner instanceof SimpleAttributeFacadeNode)
				n = ModelNode.getEmptyNode();
			else if ((owner instanceof TypeProvider) && (((TypeProvider) owner).getXsdObjectHandler() != null))
				// This is a simple type from an XSDSimpleType or XSDComplexType
				n = (Node) ((TypeProvider) owner).getXsdObjectHandler().getRequiredType();
		}

		// AssignedTL found but not the associated node.
		// Try to recover.
		//
		// If the owning library has not been modeled, then return Unassigned.
		// Assignment will be made when its library is modeled.
		if (n == null && assignedTL != null) {
			Node ownerLib = Node.GetNode(((NamedEntity) assignedTL).getOwningLibrary());
			if (ownerLib == null) {
				// LOGGER.debug("Owning library of assigned type  has not been modeled.");
				return ModelNode.getUnassignedNode();
			}
		}

		// Aliases may not be found because the parent's children cache was flushed.
		// re-hydrate the cache and try again.
		if (assignedTL instanceof TLAlias && n == null) {
			// LOGGER.debug("Assigned an alias as type " + n);
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
		assert ModelNode.getUnassignedNode() != null;
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

		// 2/2018 - trouble with shared choice facets being assigned.
		if (target instanceof SharedFacetNode) {
			target = (TypeProvider) target.getParent();
			LOGGER.debug("Shared facet not used.");
		}

		// Save old type assignment
		TypeProvider oldProvider = owner.getAssignedType();

		// Owner has a specific required type.
		if (owner.getRequiredType() != null) {
			return false;
		}
		if (oldProvider == target) {
			// LOGGER.debug("No change to assignment to " + owner);
			if (!target.getWhereAssigned().contains(owner)) {
				// LOGGER.debug("OOPS...target does not have " + owner + " in whereAssigned list.");
				target.addTypeUser(owner);
			}
			if (target != ModelNode.getUnassignedNode())
				return false;
		}

		// Skip if "Unassigned" in an attempt to preserve actual assignment even if that library is not loaded.
		if (target == null || target == ModelNode.getUnassignedNode())
			return false;

		// Get the tl object
		TLModelElement tlTarget = target.getTLModelObject();

		// Make any corrections need to the target
		// Compiler wants the actual XSD type not the tlSimple for XSD types.
		if (target.getLibrary() != null && target.getLibrary().isBuiltIn()
				&& target.getLibrary().getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
			if (target.getXsdObjectHandler() != null)
				tlTarget = target.getXsdObjectHandler().getTLLibraryMember(); // get srcTL not builtTL

		// Let the node handle assigning to the TL object.
		boolean result = owner.setAssignedType(tlTarget);

		if (result) {
			// Remove old type assignment
			oldProvider.removeWhereAssigned(owner);
			// Add where used and type assignment listener
			target.addTypeUser(owner);
		}

		// Confirm results
		if (get().getTLModelObject() != tlTarget) {
			TypeProvider actual = get();
			if (actual.getXsdObjectHandler() == null || actual.getXsdObjectHandler().getTLLibraryMember() != tlTarget) {
				LOGGER.debug("Failed to assign " + ((Node) target).getNameWithPrefix() + " to " + owner + " got "
						+ ((Node) get()).getNameWithPrefix());
				return false;
			}
		}

		// LOGGER.debug("Assigned " + ((Node) target).getNameWithPrefix() + " to " + owner);
		return result;
	}

	// ONLY public to simplify JUnits
	public TypeUserAssignmentListener getAssignmentListeners() {
		List<TypeUserAssignmentListener> listeners = new ArrayList<TypeUserAssignmentListener>();
		for (ModelElementListener l : owner.getTLModelObject().getListeners())
			if (l instanceof TypeUserAssignmentListener)
				listeners.add((TypeUserAssignmentListener) l);
		assert listeners.size() == 1;
		return listeners.get(0);
	}

}
