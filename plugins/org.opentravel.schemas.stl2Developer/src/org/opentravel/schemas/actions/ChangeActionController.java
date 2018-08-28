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
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.SubType;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.wizards.ChangeWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the change node action. Runs the wizard and control change actions used by the wizard.
 * 
 */
public class ChangeActionController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeActionController.class);

	public abstract class HistoryItem {
		// TODO - change to array of nodes (Node ...)
		// public HistoryItem(Node... nodeList) {
		// this.nodeArray = nodeList;
		// }
		// TODO - remove OpType

		private final OpType opType;
		private INode previousNode;
		private INode newNode;
		private INode resultNode;

		// private OpType getOpType() {
		// return opType;
		// }

		public INode getResultNode() {
			return resultNode;
		}

		public INode getNewNode() {
			return newNode;
		}

		/**
		 * Maintain history of changes made
		 * 
		 * @param opType
		 * @param previousNode
		 *            value before change made
		 * @param newNode
		 *            subject of the change
		 */
		public HistoryItem(final OpType opType, final INode previousNode, final INode newNode) {
			this(opType, previousNode, newNode, null);
		}

		public HistoryItem(final OpType opType, final INode previousNode, final INode newNode, final INode tempNode) {
			super();
			this.opType = opType;
			this.previousNode = previousNode;
			this.newNode = newNode;
			this.resultNode = tempNode;
		}

	}

	public class ChangeFromSimpleHistoryItem extends HistoryItem {

		/**
		 * @param previousNode
		 */
		public ChangeFromSimpleHistoryItem(AttributeNode newAttribute) {
			super(OpType.OWNING_FACET_CHANGE_FROM_SIMPLE, newAttribute, null, null);
		}

		public PropertyNode getNewAttribute() {
			return (PropertyNode) super.previousNode;
		}
	}

	public class ChangeObjectTypeHistoryItem extends HistoryItem {
		/**
		 * @param opType
		 * @param previousNode
		 * @param newNode
		 */
		public ChangeObjectTypeHistoryItem(LibraryMemberInterface source, LibraryMemberInterface newNode) {
			// super(new Node[]{(Node) source, (Node) newNode});
			super(OpType.OBJECT_TYPE_CHANGE, source, newNode);
		}

		@Override
		public LibraryMemberInterface getNewNode() {
			return (LibraryMemberInterface) super.newNode;
		}

		public LibraryMemberInterface getSourceNode() {
			return (LibraryMemberInterface) super.previousNode;
		}
	}

	public class ChangeLibraryHistoryItem extends HistoryItem {

		/**
		 * @param opType
		 * @param previousNode
		 * @param newNode
		 */
		public ChangeLibraryHistoryItem(LibraryNode oldLibrary, LibraryNode newLibrary, LibraryMemberInterface member) {
			super(OpType.LIB_CHANGE, oldLibrary, newLibrary, member);
		}

		public LibraryNode getOldLibrary() {
			return (LibraryNode) super.previousNode;
		}

		public LibraryNode getNewLibrary() {
			return (LibraryNode) super.newNode;
		}

		public LibraryMemberInterface getMember() {
			return (LibraryMemberInterface) super.resultNode;
		}

	}

	public class ChangeOwningFacetHistoryItem extends HistoryItem {
		public ChangeOwningFacetHistoryItem(FacetInterface previousFacet, FacetInterface newFacet,
				PropertyNode property) {
			super(OpType.OWNING_FACET_CHANGE, (Node) previousFacet, (Node) newFacet, property);
		}

		public FacetInterface getPreviousFacet() {
			return (FacetInterface) super.previousNode;
		}

		public FacetInterface getNewFacet() {
			return (FacetInterface) super.newNode;
		}

		public PropertyNode getProperty() {
			return (PropertyNode) super.resultNode;
		}
	}

	public class ChangeToSimpleHistoryItem extends HistoryItem {
		private final FacetInterface owningFacet;

		public ChangeToSimpleHistoryItem(final PropertyNode removedProperty, final TypeProvider previousType,
				final SimpleAttributeOwner owner, FacetInterface owningFacet) {
			super(OpType.OWNING_FACET_CHANGE_TO_SIMPLE, removedProperty, (INode) previousType, (INode) owner);
			this.owningFacet = owningFacet;
		}

		public PropertyNode getRemovedProperty() {
			return (PropertyNode) super.previousNode;
		}

		public TypeProvider getPreviousType() {
			return (TypeProvider) super.newNode;
		}

		public SimpleAttributeOwner getOwner() {
			return (SimpleAttributeOwner) super.resultNode;
		}

		public FacetInterface getOwningFacet() {
			return owningFacet;
		}
	}

	public enum OpType {
		LIB_CHANGE,
		OBJECT_TYPE_CHANGE,
		OWNING_FACET_CHANGE,
		OWNING_FACET_CHANGE_TO_SIMPLE,
		OWNING_FACET_CHANGE_FROM_SIMPLE;
	}

	/**
	 * Run the change wizard.
	 * 
	 * @param nodeToReplace
	 */
	public Node runWizard(final LibraryMemberInterface nodeToReplace) {

		if (nodeToReplace == null || nodeToReplace.getLibrary() == null) {
			LOGGER.error("Null in change node.");
			return (Node) nodeToReplace;
		}
		if (nodeToReplace instanceof ServiceNode || !((Node) nodeToReplace).isInTLLibrary()) {
			LOGGER.warn("Invalid state. Cannot change " + nodeToReplace);
			return (Node) nodeToReplace;
		}

		// LOGGER.debug("Changing selected component: " + nodeToReplace);
		ComponentNode editedNode = (ComponentNode) nodeToReplace;

		// Wizard must maintain the editedComponent active in the library.
		final ChangeWizard wizard = new ChangeWizard(editedNode, this);
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			editedNode = wizard.getEditedComponent();
			if (editedNode != nodeToReplace) {
				// Use the visitor because without a library it will not be delete-able.
				NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
				nodeToReplace.visitAllNodes(visitor);
			}
		}
		return editedNode;
	}

	public void undo(HistoryItem item) {
		if (item instanceof ChangeLibraryHistoryItem)
			undo((ChangeLibraryHistoryItem) item);
		else if (item instanceof ChangeObjectTypeHistoryItem)
			undo((ChangeObjectTypeHistoryItem) item);
		else if (item instanceof ChangeOwningFacetHistoryItem)
			undo((ChangeOwningFacetHistoryItem) item);
		else if (item instanceof ChangeToSimpleHistoryItem)
			undo((ChangeToSimpleHistoryItem) item);
		else if (item instanceof ChangeFromSimpleHistoryItem)
			undo((ChangeFromSimpleHistoryItem) item);
	}

	/*******************************************************************
	 * Change library for the passed node.
	 * 
	 * @param editedNode
	 * @param destinationLibrary
	 * @return
	 */
	public HistoryItem changeLibrary(LibraryMemberInterface editedNode, LibraryNode destinationLibrary) {
		LibraryNode previousLibrary = editedNode.getLibrary();
		destinationLibrary.addMember(editedNode);
		return destinationLibrary.contains((Node) editedNode)
				? new ChangeLibraryHistoryItem(previousLibrary, destinationLibrary, editedNode) : null;
	}

	/**
	 * Undo changed library for the passed node using the passed history item.
	 * 
	 * @param item
	 * @param editedNode
	 */
	public void undo(ChangeLibraryHistoryItem item) {
		item.getOldLibrary().addMember(item.getMember());
	}

	/*******************************************************************
	 * Change the object to the type defined by the parameter. Used in Change Wizard.
	 * 
	 * @param SubType
	 * @return - new object created by changing this object
	 */
	public HistoryItem changeObject(LibraryMemberInterface source, SubType st) {
		if (source.getLibrary() == null)
			return null;
		if (this instanceof FacetInterface)
			return changeObject((LibraryMemberInterface) source.getParent(), st);

		// TODO - add choice object type
		// TODO - add custom facet type
		LibraryMemberInterface newNode = null;
		switch (st) {
		case BUSINESS_OBJECT:
			if (source instanceof BusinessObjectNode)
				newNode = source;
			else if (source instanceof CoreObjectNode)
				newNode = new BusinessObjectNode((CoreObjectNode) source);
			else if (source instanceof VWA_Node)
				newNode = new BusinessObjectNode(((VWA_Node) source));
			break;
		case CORE_OBJECT:
			if (source instanceof CoreObjectNode)
				newNode = source;
			else if (source instanceof BusinessObjectNode)
				newNode = new CoreObjectNode((BusinessObjectNode) source);
			else if (source instanceof VWA_Node)
				newNode = new CoreObjectNode((VWA_Node) source);
			break;
		case VALUE_WITH_ATTRS:
			if (source instanceof VWA_Node)
				newNode = source;
			else if (source instanceof BusinessObjectNode)
				newNode = new VWA_Node((BusinessObjectNode) source);
			else if (source instanceof CoreObjectNode)
				newNode = new VWA_Node((CoreObjectNode) source);
			break;
		default:
			throw new IllegalArgumentException("Change to " + st.toString() + " is not supported.");
		}

		((Node) source).replaceWith(newNode); // replace this node with the new one

		HistoryItem item = new ChangeObjectTypeHistoryItem(source, newNode);
		return item;
	}

	public void undo(ChangeObjectTypeHistoryItem item) {
		((Node) item.getNewNode()).replaceWith(item.getSourceNode());
	}

	/*******************************************************************
	*
	*/
	public HistoryItem changeOwningFacet(final PropertyNode property, final FacetInterface newFacet) {

		HistoryItem item = null;
		if (!(property.getParent() instanceof FacetInterface))
			return item; // Wrong parent type
		FacetInterface oldParent = (FacetInterface) property.getParent();

		// Move the property
		property.moveProperty(newFacet);

		// Return a history item if successful
		if (property.getParent() == newFacet)
			item = new ChangeOwningFacetHistoryItem(oldParent, newFacet, property);
		return item;
	}

	public void undo(ChangeOwningFacetHistoryItem item) {
		item.getProperty().moveProperty(item.getPreviousFacet());
	}

	/*******************************************************************
	 * Change the property to be the simple type of the owning object.
	 * 
	 * @param p
	 * @return history item to undo or null if error
	 */
	public HistoryItem changeToSimple(PropertyNode p) {
		HistoryItem item = null;
		// Assure the property has a parent facet
		if (!(p.getParent() instanceof FacetInterface))
			return item;
		FacetInterface owningFacet = (FacetInterface) p.getParent();

		// Get the type to assign to the simple
		if (!(p.getType() instanceof TypeProvider))
			return item;
		TypeProvider typeProvider = (TypeProvider) p.getType();

		// Get the owning Component
		if (p.getOwningComponent() == null)
			return item;
		LibraryMemberInterface owner = p.getOwningComponent();

		// Assure the owner is a SimpleAttributeOwner
		if (!(owner instanceof SimpleAttributeOwner))
			return item; // Owner does not have simple facet

		// Save then Assign type to simple attribute
		TypeProvider previousType = ((SimpleAttributeOwner) owner).getAssignedType();
		if (((SimpleAttributeOwner) owner).setAssignedType(typeProvider) == null)
			return item; // Failed to make assignment

		// Remove passed property from owner
		owningFacet.removeProperty(p);
		assert p.getParent() == null;
		assert !owningFacet.getChildren().contains(p);
		item = new ChangeToSimpleHistoryItem(p, previousType, (SimpleAttributeOwner) owner, owningFacet);
		return item;
	}

	public void undo(ChangeToSimpleHistoryItem item) {

		// Reinstate the removed property node
		item.getOwningFacet().add(item.getRemovedProperty());

		// Reinstate the previous simple type
		item.getOwner().setAssignedType(item.getPreviousType());
	}

	/*******************************************************************
	 * Create a new attribute property based on the name and type of the simple attribute.
	 */
	public HistoryItem changeFromSimple(SimpleAttributeOwner owner, FacetInterface destinationFacet) {
		ChangeFromSimpleHistoryItem item = null;
		if (owner == null || destinationFacet == null)
			return item;
		AttributeNode newAttr = new AttributeNode(destinationFacet, ((ComponentNode) owner).getName(),
				owner.getAssignedType());

		return destinationFacet.contains(newAttr) ? new ChangeFromSimpleHistoryItem(newAttr) : null;
	}

	public void undo(ChangeFromSimpleHistoryItem item) {
		item.getNewAttribute().delete();
	}
}
