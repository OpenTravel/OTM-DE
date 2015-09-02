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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.modelObject.BusinessObjMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.modelObject.TLEmpty;
import org.opentravel.schemas.modelObject.XSDComplexMO;
import org.opentravel.schemas.modelObject.XSDElementMO;
import org.opentravel.schemas.modelObject.XSDSimpleMO;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.types.Type;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Main node structure for representing OTM objects.
 * 
 * For test validation, {@link visitAllNodes(NodeVisitors().new ValidateVisitor());}
 * 
 * @author Dave Hollander
 * 
 */
public abstract class Node implements INode {

	private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

	public static final String UNDEFINED_PROPERTY_TXT = "Undefined";
	/**
	 * Within the node classes, public adders and setters are responsible for keeping the nodes and underlying library
	 * model in sync.
	 * 
	 */
	// TO DO - eliminate root from node. It should be maintained by the model controller.
	protected static ModelNode root; // The root of the library catalog.
	protected static int nodeCount = 1; // used to assign nodeID
	protected String nodeID; // unique ID assigned to each node automatically

	// Ancestry
	private LibraryNode library; // link to the library node to which this node belongs
	protected Node parent; // link to the parentNode node
	private final ArrayList<Node> children; // links to the children
	protected String family; // name stripped at the first under bar

	protected VersionNode versionNode; // Link to the version node representing this node in a chain

	protected ModelObject<?> modelObject; // Generic interface to TL Model objects.
	protected boolean deleted = false;

	protected boolean local = false; // Local nodes are not named nodes and are not to made visible
										// in type assignment lists.
	protected XsdNode xsdNode = null; // Link to node containing imported XSD representation
	protected boolean xsdType = false; // True if this node represents an object that was created by
										// the XSD utilities but has not be imported.
	private Type type = null; // Type information associated with properties
	private String identity = ""; // just for debugging

	static final String EMPTY_TYPE = "Empty";
	public static final String OTA_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Common_v01_00";
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	private static final String Chameleon_NS = "http://chameleon.anonymous/ns";

	public Node() {
		parent = null;
		children = new ArrayList<Node>();
		nodeID = Integer.toString(nodeCount++);
		setLibrary(null);
		modelObject = newModelObject(new TLEmpty());
		if (!(this instanceof TypeNode))
			type = new Type(this);
		versionNode = null;
	}

	public Node(String identity) {
		this();
		this.setIdentity(identity);
	}

	/**
	 * Create a node containing modelObject for the tlObject and assign name and description fields. Can be used for
	 * LibraryMember elements which are top level members of the library.
	 * 
	 * The model object will NEVER be null. It may contain an EmptyMO. Model object factory links in the TLModelElement
	 * and sets edit-able flag
	 */
	public Node(final TLModelElement tlModelObject) {
		this();

		modelObject.delete();
		modelObject = newModelObject(tlModelObject);
		if (tlModelObject != null)
			setIdentity(tlModelObject.getValidationIdentity());
		else
			setIdentity(getName() + " (Null-TL-ModelObject)");
		if (getIdentity().isEmpty())
			setIdentity("Simple");
		// The TLnSimpleAttribute has no validation identity until later.
	}

	/**
	 * Create a model object for the passed TLModel element and back link from the modelObject to this node.
	 * 
	 * @param obj
	 * @return the created model object.
	 */
	@SuppressWarnings("unchecked")
	protected <TL> ModelObject<TL> newModelObject(final TLModelElement obj) {
		return (ModelObject<TL>) ModelObjectFactory.newModelObject(obj, this);
	}

	/**
	 * Public class for comparing nodes. Use: Collection.sort(list, node.new NodeComparable()) Uses node name and prefix
	 * in the comparison.
	 */
	public class NodeComparable implements Comparator<Node> {

		@Override
		public int compare(Node o1, Node o2) {
			return (o1.getNameWithPrefix().compareTo(o2.getNameWithPrefix()));
		}
	}

	@Override
	public void close() {
		if (getLibrary() != null)
			getLibrary().setEditable(true);
		NodeVisitor visitor = new NodeVisitors().new closeVisitor();
		this.visitAllNodes(visitor);
	}

	@Override
	public void delete() {
		// If a version-ed library, then also remove from aggregate
		// Library may be null! It is in some j-units.
		if (isDeleteable()) {
			NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
			// LOGGER.debug("Deleting " + this);
			this.visitAllNodes(visitor);
		}
	}

	@Override
	public boolean isDeleted() {
		if (deleted)
			return true;
		if ((modelObject == null) || (modelObject.getTLModelObj() == null))
			return true;
		return false;
	}

	/**
	 * Only removes the node from parent's children list, does not delete the node itself
	 * 
	 * @param n
	 */
	protected void removeChild(final Node n) {
		if (n == null) {
			return;
		}
		// Note - used in model creation to unlink nodes to add them to a family nav node.
		// LOGGER.debug("Removing child " + n + " from parent child list " + this);
		if (!getChildren().contains(n)) {
			// Warn in the family delete cycle is wrong -- tries to delete family node after it is
			// deleted.
			LOGGER.warn("Attempting to delete a child " + n.getName() + " that is not in children list of parent "
					+ this.getName());
			return;
		}
		children.remove(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#removeFromLibrary()
	 */
	@Override
	public void removeFromLibrary() {
		getLibrary().removeMember(this);
	}

	/**
	 * *************************** End Delete ********************************************
	 */

	/**
	 * Swap the replacement for this node. This node is replaced via {@link #replaceWith()} and removed from parent.
	 * Replacement is added to parent.
	 * 
	 * Note: this node's library is null on return. It can not be deleted.
	 * 
	 * @param replacement
	 *            - should be in library, named and with all properties.
	 * 
	 * @return ?? void ?? Returns false if the swap was not made or the replacement could not be assigned. If false, the
	 *         swapped node may still be used as a type but may not be in a library.
	 */
	public void swap(Node replacement) {
		assert (replacement != null) : "Null replacement node.";
		assert (getLibrary() != null) : "Null library.";
		assert (parent != null) : "Null parent";
		assert (getTLModelObject() instanceof LibraryMember) : "TL Object is not library member.";
		assert (replacement.getTLModelObject() instanceof LibraryMember) : "TL Object is not library member.";

		final Node thisParent = parent;

		// Add replacement to the parent if not already there.
		if (!thisParent.children.contains(replacement))
			thisParent.children.add(replacement);
		// Fail if in the list more than once.
		assert (replacement.getParent().getChildren().indexOf(replacement) == replacement.getParent().getChildren()
				.lastIndexOf(replacement));

		// Make sure the replacement model object is in the same library as this node.
		AbstractLibrary thisTlLibrary = ((LibraryMember) this.getTLModelObject()).getOwningLibrary();
		AbstractLibrary replacementTlLibrary = ((LibraryMember) replacement.getTLModelObject()).getOwningLibrary();
		if (thisTlLibrary != replacementTlLibrary) {
			LOGGER.debug("swap(): replacement TL object was not in same library.");
			replacement.getModelObject().addToLibrary(((LibraryMember) this.getTLModelObject()).getOwningLibrary());
		}

		replacement.setLibrary(this.getLibrary());
		replaceWith(replacement);
		// 1/20/15 NO - thisParent will be a family node
		// replacement.parent = thisParent; // NO NO NO
		// assert (thisParent.children.contains(replacement)) : "Replacement not in parent's list.";

		assert (this.library == null) : "This library should be null.";
		assert (this.parent == null) : "This parent should be null.";
	}

	/**
	 * Replace nodes, assigned types and library tree structures. Replaces node in the library then uses
	 * {@link #replaceWith(Node)} to replace assignments.
	 * 
	 * Does <b>not</b> delete this node. <i>Does</i> remove parent and library links. <i>Does</i> remove type usage
	 * links to this node.
	 * 
	 * @param replacement
	 */
	public void replaceWith(Node replacement) {
		if (replacement == null) {
			LOGGER.debug("Tried to replace " + this.getNameWithPrefix() + " with null replacement.");
			return;
		}
		// LOGGER.debug("Replacing " + this.getNameWithPrefix() + " \twith "
		// + replacement.getNameWithPrefix());

		// Hold onto library node because it will be cleared by removeMember.
		LibraryNode ln = this.getLibrary();
		if (getLibrary() == null) {
			LOGGER.error("The node being replaced is not in a library. " + this);
			return;
		}

		getLibrary().removeMember(this);

		if (replacement.getLibrary() == null)
			ln.addMember(replacement);
		if (replacement.getLibrary() != ln) {
			replacement.removeFromLibrary();
			ln.addMember(replacement);
		}
		// else
		// LOGGER.debug("replaceWith() - replacement is assumed to already be member and was not added to library");

		replaceTypesWith(replacement);
	}

	/**
	 * Use TypeNode to set the TL type and extension bases for this node and all children.
	 */
	protected void fixAssignments() {
		ArrayList<Node> users = new ArrayList<Node>(getTypeClass().getTypeUsers());
		for (Node user : getTypeClass().getBaseUsers()) {
			user.getModelObject().setExtendsType(this.getModelObject());
			// typeUsers list includes base type users so remove them before doing assignments.
			users.remove(user);
		}
		for (Node user : users)
			user.getModelObject().setTLType(this.getModelObject());

		for (Node child : getChildren_TypeProviders())
			child.fixAssignments();
	}

	/**
	 * Replace all type assignments to this node with assignments to passed node. For every assignable descendant of
	 * sourceNode, find where the corresponding sourceNode children are used and change them as well. See
	 * {@link #replaceWith(Node)}.
	 * 
	 * @param this - replace assignments to this node (sourceNode)
	 * @param replacement
	 *            - use replacement node instead of this node
	 * @param scope
	 *            (optional) - scope of the search (typically library or Node.getModelNode)
	 */
	public void replaceTypesWith(Node replacement) {
		replaceTypesWith(replacement, null);
	}

	public void replaceTypesWith(Node replacement, INode scope) {
		if (replacement == null)
			return;

		LibraryNode libScope = null;
		if (scope != null)
			libScope = ((Node) scope).getLibrary();
		getTypeClass().replaceTypeProvider(replacement, libScope);
	}

	/**
	 * Delete a list of nodes. The list can contain any combination of nodes so care must be taken to prevent concurrent
	 * modification. Note: facet nodes are NOT deleted except custom facets. 6/26 - removed null parent test. 627 -
	 * re-added it
	 */
	public static void deleteNodeList(final List<Node> list) {
		LOGGER.debug("Delete Node List with " + list.size() + " members.");
		for (final INode n : list) {
			n.delete();
		}
	}

	/**
	 * Find node under this node using ID value.
	 * 
	 * @param ID
	 */
	public Node findNodeID(final String ID) {
		Node kid = null;
		for (final Node n : getChildren()) {
			if (n.nodeID.equals(ID)) {
				return n; // this is it.
			}
			if ((kid = n.findNodeID(ID)) != null) {
				return kid; // recurse to check children
			}
		}
		return null;
	}

	/**
	 * Find node under this node using TL Model object validation identity.
	 * 
	 * @param ID
	 */
	public Node findNode(final String validationIdentity) {
		Node c;
		if (validationIdentity != null && !validationIdentity.isEmpty()) {
			for (final Node n : this.getChildren()) {
				if (n.getValidationIdentity().equals(validationIdentity)) {
					return n;
				} else if ((c = n.findNode(validationIdentity)) != null) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * find a named node starting from <i>this</i> node.
	 */
	public Node findNode(final String name, String ns) {
		// LOGGER.debug("findNode() - Testing: " + this.getNamespace() + " : " +
		// this);
		if (name == null || name.isEmpty())
			return null;
		if (ns.isEmpty())
			ns = Chameleon_NS;
		// Get past the model node.
		Node x;
		if (this instanceof ModelNode)
			for (Node n : getAllLibraries())
				if ((x = n.findNode(name, ns)) != null)
					return x;

		Node c = null;
		// test to see if the library is in the target namespace then just test
		// their kids for name
		// String nodeNS = getNamespace();
		if (!getNamespace().equals(ns) && !(getNamespace().equals(Chameleon_NS)))
			return null;

		for (final Node n : getChildren()) {
			if (n.getName().equals(name) && !n.isNavigation()) {
				return n;
			} else if ((c = n.findNode(name, ns)) != null) {
				return c;
			}
		}
		return null;
	}

	public Node findNode_TypeProvider(final String name, String ns) {
		if (name == null || name.isEmpty())
			return null;
		Node child = null;
		if (ns.isEmpty())
			ns = Chameleon_NS;
		if (this.isTypeProvider() && this.getName().equals(name) && this.getNamespace().equals(ns))
			return this;
		for (Node x : getChildren()) {
			if (x.isLibraryContainer())
				child = x.findNode_TypeProvider(name, ns);
			if (x.getNamespace().equals(ns) || (x.getNamespace().equals(Chameleon_NS))) {
				child = x.findNode_TypeProvider(name, ns);
			}
			if (child != null)
				return child;
		}
		// Not found
		return null;
	}

	/**
	 * Find the first node in the descendants of this node with the given name. The order searched is not guaranteed.
	 * Will not find family nodes.
	 * 
	 * @param name
	 * @return node found or null
	 */
	public Node findNodeByName(String name) {
		for (Node n : getDescendants_NamedTypes()) {
			if (n.getName().equals(name) && !(n instanceof FamilyNode))
				return n;
		}
		return null;
	}

	public String getValidationIdentity() {
		if (modelObject != null && modelObject.getTLModelObj() != null) {
			return ((Validatable) modelObject.getTLModelObj()).getValidationIdentity();
		}
		return "";
	}

	/**
	 * @return the version node representing this node in the specific library in a chain.
	 */
	public VersionNode getVersionNode() {
		return versionNode;
	}

	/**
	 * @param versionNode
	 *            to represent this node in a specific library in a chain.
	 */
	public void setVersionNode(VersionNode version) {
		this.versionNode = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getChildren()
	 */
	@Override
	public List<Node> getChildren() {
		return children;
	}

	/**
	 * @return a new list containing all children and their descendants. Includes aggregate, version and navNodes.
	 */
	public List<Node> getDescendants() {
		final ArrayList<Node> ret = new ArrayList<Node>();
		for (final Node n : getChildren()) {
			ret.add(n);
			ret.addAll(n.getDescendants());
		}
		return ret;
	}

	/**
	 * Gets the descendants that are type users (can be assigned a type). Does not return navigation nodes.
	 * {@link #getChildren_TypeUsers() Use getChildren_TypeUsers() for only immediate children.}
	 * 
	 * @return new list of all descendants that can be assigned a type.
	 */
	public List<Node> getDescendants_TypeUsers() {
		final ArrayList<Node> ret = new ArrayList<Node>();
		for (final Node n : getChildren()) {
			if (n.isTypeUser())
				ret.add(n);

			// Some type users may also have children
			if (n.hasChildren())
				ret.addAll(n.getDescendants_TypeUsers());
		}
		return ret;
	}

	public List<Node> getInheritedChildren() {
		return Collections.emptyList();
	}

	public boolean hasInheritedChildren() {
		List<Node> inheritedChildren = getInheritedChildren();
		return (inheritedChildren != null) && !inheritedChildren.isEmpty();
	}

	public void resetInheritedChildren() {
		// No action required - override as required in sub-classes
	}

	/**
	 * Get the family name of the node.
	 * 
	 * @return string of the name
	 */
	public String getFamily() {
		return family;
	}

	@Override
	public Image getImage() {
		final ImageRegistry imageRegistry = Images.getImageRegistry();
		return imageRegistry.get("file");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getModelObject()
	 */
	@Override
	public ModelObject<?> getModelObject() {
		return modelObject;
	}

	/**
	 * Get the TL model object from the node's model object or null if there is no underlying model object or tl model
	 * object.
	 * 
	 * @return
	 */
	public TLModelElement getTLModelObject() {
		return (TLModelElement) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	/**
	 * @return - the TL model object assigned as the type to this node's object or else null.
	 * @see getType()
	 */
	public NamedEntity getTLTypeObject() {
		return (modelObject != null ? modelObject.getTLType() : null);
	}

	public NamedEntity getTLBaseType() {
		if (modelObject.getTLModelObj() instanceof TLEmpty)
			return null;
		return (modelObject != null ? modelObject.getTLBase() : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getLabel()
	 */
	@Override
	public String getLabel() {
		return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getName()
	 */
	@Override
	public String getName() {
		if (modelObject == null)
			return "";
		if (modelObject.getTLModelObj() instanceof TLFacet
				&& ((TLFacet) modelObject.getTLModelObj()).getOwningEntity() == null)
			return "";
		return modelObject.getName() == null ? "" : modelObject.getName();
	}

	/**
	 * @return the type node from the type object.
	 */
	public Node getTypeNode() {
		return getTypeClass().getTypeNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getNamePrefix()
	 */
	@Override
	public String getNamePrefix() {
		return getLibrary() == null ? "" : getLibrary().getNamePrefix();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return getLibrary() == null ? "" : getLibrary().getNamespace();
	}

	public String getNamespaceWithPrefix() {
		return getLibrary() == null ? "" : getLibrary().getNamespaceWithPrefix();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getNameWithPrefix()
	 */
	@Override
	public String getNameWithPrefix() {
		return getLibrary() == null ? getName() : getLibrary().getNamePrefix() + ":" + getName();
	}

	/**
	 * @param testNode
	 * @return
	 */
	protected boolean nameEquals(final INode other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (getNamespace() == null) {
			if (other.getNamespace() != null) {
				return false;
			}
		} else if (!getNamespace().equals(other.getNamespace())) {
			return false;
		}
		return true;
	}

	public String getNodeID() {
		return nodeID;
	}

	/**
	 * @return the component (named object) owner of this node or else this node.
	 */
	public Node getOwningComponent() {
		return this;
	}

	/*****************************************************************************
	 * Static getters
	 */

	/**
	 * @return the nodeCount which is the current number of allocated nodes.
	 */
	public static int getNodeCount() {
		return nodeCount;
	}

	/**
	 * Get the static root model node.
	 */
	public static ModelNode getModelNode() {
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getParent()
	 */

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public INode getParentAggregate() {
		if (getLibrary().isInChain()) {
			LibraryChainNode lcn = getLibrary().getChain();
			if (this instanceof SimpleComponentInterface)
				return lcn.getSimpleAggregate();
			else if (this instanceof ComplexComponentInterface)
				return lcn.getComplexAggregate();
			else if (isService() || isOperation())
				return lcn.getServiceAggregate();
		}
		return null;
	}

	/**
	 * Get the type class for this node. This should <b>not</b> be used except when creating a node set representing an
	 * existing TL library.
	 * 
	 * @return - the type class representing the type assignments or else null
	 */
	public Type getTypeClass() {
		return type;
	}

	/*****************************************************************************
	 * Children
	 */

	/**
	 * @return list of the children to be used for navigation purposes.
	 */
	public List<Node> getNavChildren() {
		return getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getChildren_TypeProviders()
	 */
	@Override
	public List<Node> getChildren_TypeProviders() {
		final ArrayList<Node> kids = new ArrayList<Node>();
		for (final Node n : getChildren()) {
			if (n.isTypeProvider() || n.hasChildren_TypeProviders()) {
				if (n instanceof VersionNode && ((VersionNode) n).getVersionedObject() != null)
					kids.add(((VersionNode) n).getVersionedObject());
				else
					kids.add(n);
			}
		}
		return kids;
	}

	/**
	 * Traverse via hasChildren. For version chains, it uses the version node and does not touch aggregates.
	 */
	@Override
	public List<Node> getDescendants_NamedTypes() {
		// keep duplicates out of the list that version aggregates may introduce
		HashSet<Node> namedKids = new HashSet<Node>();
		for (Node c : getChildren()) {
			// TL model considers services as named library member
			if (c.isService())
				namedKids.add(c);
			else if (c.isTypeProvider()) {
				namedKids.add(c);
			} else if (c.hasChildren())
				// If it is named type, do not go into it.
				namedKids.addAll(c.getDescendants_NamedTypes());
		}
		return new ArrayList<Node>(namedKids);
	}

	/**
	 * Gets the children that are type users (can be assigned a type). Does not return navigation nodes.
	 * {@link #getDescendants_TypeUsers() Use getDescendants_TypeUsers() for all children.}
	 * 
	 * @return all immediate children that can be assigned a type.
	 */
	public List<Node> getChildren_TypeUsers() {
		return new ArrayList<Node>();
	}

	/**
	 * Gets all the types assigned to this type and all the types assigned to those types, etc. Sends back a list of
	 * unique types. Types used recursively are only added to the list once.
	 * 
	 * @param currentLibraryOnly
	 *            - only list types in this library.
	 * @return new list of assigned types or empty list.
	 */
	public List<Node> getDescendants_AssignedTypes(boolean currentLibraryOnly) {
		HashSet<Node> foundTypes = new HashSet<Node>();
		foundTypes = getDescendants_AssignedTypes(currentLibraryOnly, foundTypes);
		foundTypes.remove(this); // may have been found in an addAll iteration
		return new ArrayList<Node>(foundTypes);
	}

	// the public method uses this then removes the original object from the list.
	private HashSet<Node> getDescendants_AssignedTypes(boolean currentLibraryOnly, HashSet<Node> foundTypes) {
		Node assignedType = null;
		for (Node n : getDescendants_TypeUsers()) {
			if (n.getAssignedType() != null) {
				assignedType = n.getAssignedType().getOwningComponent();
				if (!currentLibraryOnly || (assignedType.getLibrary() == getLibrary()))
					if (foundTypes.add(assignedType)) {
						foundTypes.addAll(assignedType.getDescendants_AssignedTypes(currentLibraryOnly, foundTypes));
					}
			}
		}
		return foundTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return getChildren().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return false;
	}

	public abstract boolean hasNavChildren();

	/**
	 * @return true if there are children that are properties that can be assigned a type. Does not include indicators,
	 *         enumeration literals, or roles Does not include properties whose model object or TL Type are NULL.
	 *         (modelObject != null && modelObject.getTLType() != null;)
	 */
	public boolean hasNavChildrenWithProperties() {
		return hasNavChildren();
	}

	/**
	 * @return - the repeat count for an element property
	 */
	public int getRepeat() {
		return 0;
	}

	public RoleFacetNode getRoleFacet() {
		// Find the roles facet
		return getOwningComponent() != this ? getOwningComponent().getRoleFacet() : null;
	}

	/**
	 * @return a new list of children of the parent after this node is removed
	 */
	public List<Node> getSiblings() {
		if (parent == null)
			return null;
		final List<Node> siblings = new LinkedList<Node>(parent.getChildren());
		siblings.remove(this);
		return siblings;
	}

	/*****************************************************************************
	 * is Properties
	 */
	public boolean isAlias() {
		return this instanceof AliasNode;
	}

	public boolean isBusinessObject() {
		return this instanceof BusinessObjectNode;
	}

	public boolean isCoreObject() {
		return this instanceof CoreObjectNode;
	}

	/**
	 * @return true if instance of component node.
	 */
	public Boolean isComponent() {
		return this instanceof ComponentNode;
	}

	public boolean isFacet() {
		return this instanceof FacetNode;
	}

	public Boolean isFamily() {
		return this instanceof FamilyNode;
	}

	public Boolean isLibrary() {
		return this instanceof LibraryNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#isLibraryContainer()
	 */
	@Override
	public boolean isLibraryContainer() {
		return false;
	}

	public boolean isProperty() {
		return this instanceof PropertyNode;
	}

	@Override
	public boolean isTypeUser() {
		return this instanceof TypeUser;
	}

	public boolean isFacetAlias() {
		return false;
	}

	public boolean isAliasable() {
		return false;
	}

	public boolean isImportable() {
		return false;
	}

	/**
	 * @return true if this property will become an XSD element.
	 */
	public boolean isElement() {
		return false;
	}

	/**
	 * @return true if indicator element or attribute.
	 */
	public boolean isIndicator() {
		return false;
	}

	public boolean isInheritedProperty() {
		return false;
	}

	public boolean isID_Reference() {
		return false;
	}

	/**
	 * Implied nodes and nodes without libraries are always editable.
	 * 
	 * @return true if the node's library is editable and is not inherited.
	 * @see Node#isInheritedProperty()
	 */
	@Override
	public boolean isEditable() {
		boolean result = false;
		if (getChain() != null)
			result = getChain().isEditable();
		else if (this instanceof ImpliedNode)
			result = true;
		else if (isInheritedProperty())
			result = false;
		else if (getLibrary() == null)
			result = true;
		else
			result = getLibrary().isEditable();
		// LOGGER.debug("Is " + this + " editable? " + result);
		return result;
	}

	public boolean isSimpleFacet() {
		return false;
	}

	public boolean isListFacet() {
		return false;
	}

	// @Deprecated
	// public boolean isExtendable() {
	// return false;
	// }

	/**
	 * Extensible objects have the ability to create extension points when compiled into schemas. These include core and
	 * business objects as well as operations and extension points. {@link #isExtensible()}
	 * 
	 * @return true if this object has the characteristic of being extensible
	 */
	public boolean isExtensibleObject() {
		return true;
	}

	/**
	 * Enable the extend-able property for this object. For faceted objects, this instructs compiler to create extension
	 * points. For enumerations, this creates an open enumeration.
	 * 
	 * @return - returns this node, or the created open/closed enumeration node {@link #isExtensible()}
	 */
	public Node setExtensible(boolean extensible) {
		return this;
	}

	/**
	 * 
	 * @return true if this object can extend another object.
	 */
	public boolean canExtend() {
		if ((this instanceof CoreObjectNode) || (this instanceof BusinessObjectNode)
				|| (this instanceof ExtensionPointNode) || (this instanceof OperationNode))
			return true;
		return false;
	}

	/**
	 * Extensible objects have the ability to create extension points when compiled into schemas. These include core and
	 * business objects as well as operations and extension points. {@link #isExtensibleObject()}
	 * 
	 * @return true if this object has the characteristic of being extensible and the model object is set to create
	 *         extension points on compile.
	 */
	public boolean isExtensible() {
		return true;
	}

	/**
	 * @return False for node that can not be deleted: not-editable, facets, simpleFacets. Custom and Query Facets are
	 *         delete-able. Libraries are <b>always</b> delete-able
	 */
	public boolean isDeleteable() {
		if (getLibrary() == null)
			return false;
		// If it doesn't have a parent then it is not linked and can be deleted.
		if (getOwningComponent().getParent() == null)
			return true;
		// Services always return false for inhead().
		if (getOwningComponent().getParent().isOperation())
			return isEditable();
		return getLibrary().isManaged() ? isInHead() && isEditable() : isEditable();
	}

	/**
	 * @return - true if this node is in the current default library
	 */
	public boolean isDefaultLibrary() {
		ProjectNode pNode = getLibrary().getProject();
		return pNode.getProject().getDefaultItem() == getLibrary().getProjectItem();
	}

	/**
	 * @return true if this is a simple object type.
	 */
	public boolean isSimpleType() {
		return false;
	}

	public boolean isMessage() {
		return false;
	}

	protected boolean isNavChild() {
		return isBusinessObject() || isCoreObject() || isValueWithAttributes() || isService()
				|| (isFacet() && isAssignable()) || isAlias() || isOperation() || isSimpleType();
	}

	@Override
	public boolean isNavigation() {
		return false;
	}

	public boolean isBaseTypeUser() {
		return false;
	}

	/**
	 * @return true if in an XSD type and element assignable.
	 */
	public boolean isXsdElementAssignable() {
		return xsdType ? xsdNode.isElementAssignable() : false;
	}

	/**
	 * @return true if this is an XSD node for an XSD atomic type (impliedType == XSD_Atomic).
	 */
	public boolean isXSD_Atomic() {
		if (getTypeClass().getTypeNode() instanceof ImpliedNode)
			LOGGER.debug("is " + this + " Atomic? "
					+ ((ImpliedNode) getTypeClass().getTypeNode()).getImpliedType().equals(ImpliedNodeType.XSD_Atomic));

		return getTypeClass().getTypeNode() instanceof ImpliedNode ? ((ImpliedNode) getTypeClass().getTypeNode())
				.getImpliedType().equals(ImpliedNodeType.XSD_Atomic) : false;
	}

	/**
	 * @return - true if is in TL, built-in or xsd library
	 */
	public boolean isInModel() {
		return isInTLLibrary() || isInBuiltIn() || isInXSDSchema();
	}

	public boolean isExtensionPointFacet() {
		return false;
	}

	/**
	 * @return true if the node has a complex type assigned as the type of the property
	 */
	public boolean isAssignedComplexType() {
		return false;
	}

	/**
	 * @return - true if the component node represent a local anonymous type.
	 */
	public boolean isLocal() {
		return false;
	}

	public boolean isXSDSchema() {
		return false;
	}

	public boolean isBuiltIn() {
		LibraryNode ln = getLibrary();
		return ln != null && ln.isBuiltIn();
	}

	public boolean isTLLibrary() {
		return false;
	}

	public boolean isTopLevelObject() {
		return (getTLModelObject() instanceof LibraryMember);
	}

	/**
	 * @return true if the node represents a non-imported XSD type. Note - the library is a TLLibrary, but the type is
	 *         not editable.
	 */
	public boolean isXsdType() {
		return xsdType;
	}

	/**
	 * @param Set
	 *            to true if the node represents an non-imported XSD type.
	 */
	public void setXsdType(boolean xsdType) {
		this.xsdType = xsdType;
	}

	/*****************************************************************************
	 * is Properties - model object facade
	 */
	/**
	 * @return - true if the node object is either open or closed enumeration ??? is enumeration always a facetNode?
	 */
	public boolean isEnumeration() {
		return false;
	}

	public boolean isRoleFacet() {
		return false;
	}

	public boolean isRoleProperty() {
		return this instanceof RoleNode;
	}

	public boolean isEnumerationLiteral() {
		return this instanceof EnumLiteralNode;
	}

	public boolean isOperation() {
		return this instanceof OperationNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#isTypeProvider()
	 */
	@Override
	public boolean isTypeProvider() {
		if (this instanceof ImpliedNode)
			return false;
		return getTLModelObject() != null ? getTLModelObject() instanceof NamedEntity : false;
	}

	/**
	 * @return true if a named top level object (business, core, enum, vwa, simple, extension point facet)
	 */
	public boolean isNamedType() {
		return false;
	}

	public boolean isElementAssignable() {
		return modelObject instanceof XSDElementMO;
	}

	/**
	 * @return true if this object can be assigned as a simple type (not by reference).
	 */
	public boolean isSimpleAssignable() {
		return modelObject.isSimpleAssignable();
	}

	public boolean isCustomFacet() {
		return false;
	}

	public boolean isDefaultFacet() {
		return false;
	}

	public boolean isQueryFacet() {
		return false;
	}

	public boolean isService() {
		return false;
	}

	/**
	 * @return true if <b>only</b> simple types can be assigned to this type user.
	 * 
	 *         Note: if parent is not known, attributes are assumed to not be part of a ValueWithAttribute and therefore
	 *         are simpleTypeUsers
	 * 
	 *         Overridden where true.
	 */
	public boolean isOnlySimpleTypeUser() {
		return false;
	}

	public boolean isVWASimpleAssignable() {
		return getTLModelObject() instanceof TLAttributeType;
	}

	public boolean isAssignable() {
		return isElementAssignable() || isTypeProvider() || isSimpleAssignable();
	}

	@Override
	public boolean isVWA_AttributeFacet() {
		return false;
	}

	public boolean isValueWithAttributes() {
		return this instanceof VWA_Node;
	}

	/**
	 * Returns true if the node name and namespace is unique compared against all of <i>this</i> children. If the child
	 * is in a query facet, then the test is only across that facet.
	 * 
	 * @param test
	 *            node to test, <i>this</i> node to check children of. If property or facet, will go to parentNode to
	 *            start check.
	 * @return true if unique
	 * 
	 *         FIXME - this assumes that we are only comparing properties of complex objects Will not work for nav nodes
	 */
	public boolean isUnique(final INode testNode) {
		Node n = this;
		if (this instanceof PropertyNode) {
			n = n.parent;
		}
		if (n.isFacet() && !n.isCustomFacet() && !n.isQueryFacet() && !n.isRoleFacet()) {
			n = n.parent; // compare across all facets.
		}
		for (final Node facet : n.getChildren()) {
			if (facet.nameEquals(testNode)) {
				return false;
			}
			for (final Node prop : facet.getChildren()) {
				if (prop.nameEquals(testNode)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isFacetUnique(final INode testNode) {
		Node n = this;
		if (n instanceof PropertyNode) {
			n = n.parent;
		}
		if (n.isFacet() && !n.isQueryFacet() && !n.isRoleFacet()) {
			n = n.parent; // compare across all facets.
		}
		// FIXME - TEST - OTA-54 - query facets can have same property names as
		// properties in other
		// facets, including other query facets.
		if (n.nameEquals(testNode)) {
			return false;
		}
		for (final Node facet : n.getChildren()) {
			if (facet.nameEquals(testNode)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Simply link in the child node to <i>this</i> node at the index location. No family match test is made. Assures
	 * the child is not already in the list.
	 * 
	 * @param child
	 *            - node to add to <i>this</i>
	 * @param index
	 *            - integer ordinal value where to add the child if in range. If out of range, added to end.
	 */
	public boolean linkChild(final Node child, final int index) {
		if (child == null) {
			return false;
		}
		if (getChildren().contains(child))
			return false;

		if (index < 0 || index > getChildren().size()) {
			getChildren().add(child);
		} else {
			getChildren().add(index, child);
		}
		child.setParent(this);
		if (!child.isLibrary()) {
			child.setLibrary(getLibrary());
		}
		// LOGGER.debug("Linked child " + child + " to parent " + this);
		return true;
	}

	/**
	 * Add the <i>child</i> node parameter to <i>this</i> node. Sets parentNode link of child. NOTE - must have a name
	 * to support family processing. Does family processing. Does <b>not</b> check for version or aggregates.
	 * 
	 * @param child
	 *            - node to be added
	 */
	public boolean linkChild(final Node child) {
		return linkChild(child, true);
	}

	/**
	 * Add the <i>child</i> node parameter to <i>this</i> node. Sets parentNode link of child. NOTE - must have a name
	 * to support family processing.
	 * 
	 * @param child
	 *            - node to be added
	 * @param doFamily
	 *            - if present and true, the child is added to the a family node if the name matches family conditions.
	 *            If child.family is not set it will make a family name. Checks all the children of this node to see if
	 *            the child should be added to the family instead of directly to this node.
	 * @return false if child could not be linked.
	 */
	public boolean linkChild(final Node child, final boolean doFamily) {
		if (child == null) {
			return false;
		}

		if (!linkChild(child, -1))
			return false;
		if (doFamily) {
			// family = NodeNameUtils.makeFamilyName(getName());
			child.family = NodeNameUtils.makeFamilyName(child.getName());
			if (child.family.isEmpty()) {
				return true;
			}

			// makeFamilyName returns empty if family prefix not found
			for (final Node peer : child.getSiblings()) {
				if (child.family.equals(peer.family)) {
					return child.addChildToFamily(peer);
				}
			}
		}
		return true;
	}

	/**
	 * Add <i>this</i> child to a family node for it and its "peer" node. If the peer is a Family node then just add it,
	 * otherwise create the family node.
	 * 
	 * @return
	 */
	protected boolean addChildToFamily(Node peer) {
		assert (parent != null) : "Assert: parent is null"; //
		assert (peer.getParent() != null) : "Null peer parent.";

		if (peer instanceof FamilyNode) {
			parent.removeChild(this); // take the child out of the parentNode's list.
			peer.getChildren().add(this); // add it to the family node
			parent = peer;
		} else {
			peer = new FamilyNode(this, peer);
		}
		// LOGGER.debug("Added a child " + this + " to a family " + peer.family);
		return true;
	}

	/**
	 * Adds the passed node as a child of <i>this</i> node, if its name and namespace are unique amongst the children.
	 * Used to link properties to facets.
	 * 
	 * @param child
	 *            - node to link in
	 * @return true if linked, false if not unique
	 */
	public boolean linkIfUnique(final Node child) {
		if (child == null) {
			return false;
		}
		if (!this.isUnique(child)) {
			return false;
		}
		getChildren().add(child);
		child.setParent(this);
		return true;
	}

	/**
	 * 
	 * @return true if not in the correct family or family name matches any siblings
	 */
	public boolean shouldCreateFamily() {
		final String family = NodeNameUtils.makeFamilyName(this.getName());
		if (parent != null) {
			// Node already in a correct family - return false to avoid relinking
			if (parent.isFamily() && parent.getName().equals(family)) {
				return false;
			}
			// if any of the siblings in the same family - return true
			for (final Node n : getSiblings()) {
				if (n.getFamily() != null && n.getFamily().equals(family)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Walk the nodes under <i>this</i> node and set the library value to this library node.
	 * 
	 */
	protected void setKidsLibrary() {
		for (final Node n : this.getChildren()) {
			n.setLibrary(library);
			if (n.getChildren() != null) {
				n.setKidsLibrary();
			}
		}
	}

	/**
	 * Sets the library field in this node and all of its kids.
	 * 
	 * @param ln
	 */
	public void setLibrary(final LibraryNode ln) {
		library = ln;
		// Library chains use the library node to point to the head library.
		if (!(this instanceof LibraryChainNode))
			setKidsLibrary();
	}

	/**
	 * Set the name in the node and underlying model object for <i>this</i> node. If this node has parentNode and
	 * grandparent, unlink and re-link to execute family logic. Overridden in complex type nodes to propagated name
	 * change type users. NOTE - not safe to use in constructors until a model object is instantiated.
	 * 
	 * @param n
	 *            - new name
	 */
	public void setName(final String n) {
		setName(n, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#setName(java.lang.String, boolean)
	 */
	@Override
	public void setName(final String n, final boolean doFamily) {
		String newName = n;
		if (n == null || n.isEmpty()) {
			newName = UNDEFINED_PROPERTY_TXT;
		}

		if (getModelObject() != null) {
			getModelObject().setName(newName);
		} else {
			LOGGER.warn("Missing model object - Can not set name to :" + n);
		}

		String oldFamily = family;
		family = NodeNameUtils.makeFamilyName(newName);

		// A name change may also change the family.
		// Parent may not be set yet - GUI construction sets the name then links
		// to do family
		if ((doFamily) && (parent != null) && (parent.parent != null)) {
			final Node gp = parent.parent;
			if (shouldCreateFamily()) {
				Node par = parent;
				if (parent.isFamily()) {
					par = gp; // link in above the family node
				}
				unlinkNode(); // remove from parentNode
				par.linkChild(this); // will re-apply family logic
			} else if (parent instanceof FamilyNode) {
				if (!family.equals(parent.getName())) {
					unlinkNode();
					gp.linkChild(this);
				}
			} else if ((parent instanceof VersionNode) && (parent.getParent() instanceof FamilyNode)) {
				VersionNode vn = (VersionNode) parent;
				Node family = parent.getParent(); // is also vn parent
				if (!family.equals(family.getName())) {
					// do the aggregate
					this.family = oldFamily;
					if (vn.head == this) {
						// remove from aggregate family, add to aggregate
						getLibrary().getChain().removeAggregate((ComponentNode) this);
						getLibrary().getChain().add((ComponentNode) this);
					}
					// Move the version node
					final Node newParent = family.getParent();
					vn.unlinkNode(); // TEST ME
					newParent.linkChild(vn);
					// if (family.getParent() != null)
					// family.getParent().linkChild(vn);
					// else
					// LOGGER.error("Error: family does not have parent: " + family);
					this.family = NodeNameUtils.makeFamilyName(newName);
					vn.family = this.family;
				}
			}
			// }
			// TODO - if they change a family name ask then apply changes to all
			// members of the family.
		}
		// Enhancement - control duplicates. Check to see if one was created and
		// Check the duplicates and remove if appropriate.
		// if (ModelNode.getDuplicateTypesNode().getChildren().contains(this))
		// ModelNode.getDuplicateTypesNode().getChildren().remove(this);
	}

	/**
	 * Set the parentNode for this node. Set to null if it is the root node.
	 * 
	 * @param the
	 *            parentNode node
	 */
	public void setParent(final Node n) {
		parent = n;
	}

	/**
	 * Unlink this node from its parent and updates family node as needed. If this is a versioned object, it unlinks the
	 * verionNode as well. Does not change the TL model. Does not delete the node; caller is responsible to free
	 * resources.
	 * 
	 */
	public void unlinkNode() {
		if (parent == null) {
			LOGGER.error("unlinkNode ERROR - null parent. EXIT");
			return;
		}
		VersionNode vn = null;
		if (parent instanceof VersionNode)
			vn = (VersionNode) parent;
		if (!parent.children.remove(this))
			LOGGER.debug("unlinkNode Error - child " + getName() + " was not in parent's " + parent.getName()
					+ " child list.");
		parent.updateFamily();
		parent = null;
		// recurse to remove version parent as well.
		if (vn != null) {
			// unlink from the chain aggregate node
			if (this instanceof ComponentNode)
				getChain().removeAggregate((ComponentNode) this);
			// unlink the version node it self
			vn.unlinkNode();
		}
	}

	/**
	 * Checks to make sure the family structure is OK and adjusts if not. Overridden in FamilyNode.
	 */
	protected void updateFamily() {
	}

	/**
	 * For each target node, if any of its children refer to this node, replace the assignment.
	 * 
	 * @param targets
	 *            - list of nodes to test and change assignments
	 * @return - list of changed nodes.
	 */
	public List<Node> setTLTypeOfTargetChildren(List<Node> targets, Node replacement) {
		List<Node> changedNodes = new ArrayList<Node>();
		for (Node t : targets) {
			// test above the facet to get to the object level
			if ((t.getParent() == replacement) || (t.getParent().getParent() == replacement)) {
				// TODO - TEST - This seems all wrong.
				LOGGER.debug("TODO - TEST - setTLTypeOfTargetChildren() - This seems all wrong. t = " + t.getName());
				t.setAssignedType(replacement);
				changedNodes.add(t);
			}
		}
		return changedNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#setAssignedType(org.opentravel.schemas.node.INode )
	 */
	@Override
	public boolean setAssignedType(Node typeNode, boolean refresh) {
		return (this instanceof TypeUser) ? setAssignedType(typeNode, refresh) : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#setAssignedType(org.opentravel.schemas.node.INode )
	 */
	@Override
	public boolean setAssignedType(Node typeNode) {
		return false;
		// return (this instanceof TypeUser) ? setAssignedType(typeNode) : false;
	}

	/**
	 * Return the node used as the assigned type. NOTE: does not return node assigned as base types for core and
	 * business objects.
	 * 
	 * @return
	 */
	public Node getAssignedType() {
		LOGGER.debug("Get assigned type for " + this.getClass().getSimpleName() + ":" + this);
		return (this instanceof TypeUser) ? getAssignedType() : null;
	}

	/**
	 * Return the base type - the node displayed in select Extends field.
	 * 
	 * @return
	 */
	public Node getExtendsType() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getType()
	 */
	@Override
	public Node getType() {
		return (this instanceof TypeUser) ? getAssignedType() : null;
	}

	@Override
	public String getTypeName() {
		return "";
	}

	@Override
	public String getTypeNameWithPrefix() {
		return "";
	}

	/**
	 * @return the count of where this node is assigned as a type. Includes count of where children are used.
	 */
	public int getTypeUsersCount() {
		return 0;
	}

	public int getComponentUsersCount() {
		return 0;
	}

	/**
	 * @return true if this node could be assigned a type but is unassigned.
	 */
	public boolean isUnAssigned() {
		if (!(this instanceof TypeUser))
			return false;
		if (getAssignedType() instanceof ImpliedNode)
			if (((ImpliedNode) getAssignedType()).getImpliedType() == ImpliedNodeType.UnassignedType)
				return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getTypeUsers()
	 */
	@Override
	public List<Node> getTypeUsers() {
		return getTypeClass() == null ? new ArrayList<Node>() : getTypeClass().getTypeUsers();
	}

	/**
	 * Set the extension base to the passed source node. If null, remove assignment. Extension base is maintained in the
	 * TypeClass.typeNode.
	 * 
	 * @param sourceNode
	 */
	public void setExtendsType(final INode sourceNode) {
		getTypeClass().setBaseType(sourceNode);
	}

	public void setRepeat(final int i) {
		getModelObject().setRepeat(i);
		return;
	}

	@Override
	public abstract String getComponentType();

	// TODO - why this and getComponentType???
	public String getSimpleComponentType() {
		// return for VWA Simple Facet Base as label
		if (this.getOwningComponent().isValueWithAttributes())
			return "Base";
		return modelObject.getComponentType();
	}

	@Override
	public LibraryNode getLibrary() {
		return library;
	}

	/**
	 * Return the library chain that this node belongs to.
	 * 
	 * @return the chain or null if not in a chain.
	 */
	public LibraryChainNode getChain() {
		return getLibrary() != null ? getLibrary().getChain() : null;
	}

	public void setExtendable(final boolean state) {
	}

	/**
	 * Get the editing status of the node based on chain head library or unmanaged library. Use to check if an object
	 * can be acted upon by the user.
	 * 
	 * To find out the specific status of the actual library and not the chain, see {@link LibraryNode#getEditStatus()}
	 */
	public NodeEditStatus getEditStatus() {
		NodeEditStatus status = NodeEditStatus.FULL;
		if (getLibrary() == null)
			return status; // if there is no library, allow anything.

		if (getChain() == null) {
			if (getLibrary().isEditable())
				status = NodeEditStatus.FULL;
			else
				status = NodeEditStatus.NOT_EDITABLE;
		} else {
			if (!getChain().isEditable())
				status = NodeEditStatus.MANAGED_READONLY;
			else if (getChain().getHead().isMajorVersion())
				status = NodeEditStatus.FULL;
			else if (getChain().getHead().isMinorOrMajorVersion())
				status = NodeEditStatus.MINOR;
			else
				status = NodeEditStatus.PATCH;
		}
		// LOGGER.debug(this + " library has " + status + " edit status.");
		return status;
	}

	public String getEditStatusMsg() {
		return Messages.getString(getEditStatus().msgID());
	}

	public String getExtendsTypeName() {
		return modelObject.getExtendsType();
	}

	public String getExtendsTypeNS() {
		return modelObject.getExtendsTypeNS();
	}

	/**
	 * @param node
	 * @return true if this is extended by the passed node
	 */
	public boolean isExtendedBy(Node node) {
		if (node.getTLModelObject() instanceof NamedEntity) {
			return modelObject.isExtendedBy((NamedEntity) node.getTLModelObject());
		}
		return false;
	}

	/**
	 * @param node
	 * @return true if this is extended by the passed node
	 */
	public boolean isInstanceOf(Node node) {
		if (isExtendedBy(node)) {
			return true;
		} else {
			Node baseNode = Node.getModelNode().findNode(getExtendsTypeName(), getExtendsTypeNS());
			if (baseNode == null) {
				// LOGGER.warn("Could not find the base node: [" + getExtendsTypeNS() + ":" + getExtendsTypeName() +
				// "]");
				return false;
			}
			return baseNode.isInstanceOf(node);
		}
	}

	/**
	 * ************************ Documentation ****************************
	 * 
	 * TODO - reconcile with the documentation view use of the documentation nodeManager. TODO - do the enum/string[]
	 * right
	 */
	public static enum DocTypes {
		Description, Deprecation, MoreInformation, Implementer, ReferenceLink
	};

	public static String[] docTypeStrings = { "Description", "Deprecation", "MoreInformation", "Implementer",
			"ReferenceLink" };

	public static DocTypes docTypeFromString(String type) {
		if (type.equals(docTypeStrings[0]))
			return DocTypes.Description;
		if (type.equals(docTypeStrings[1]))
			return DocTypes.Deprecation;
		if (type.equals(docTypeStrings[2]))
			return DocTypes.MoreInformation;
		if (type.equals(docTypeStrings[3]))
			return DocTypes.Implementer;
		if (type.equals(docTypeStrings[4]))
			return DocTypes.ReferenceLink;
		return null;
	}

	public String getDescription() {
		return modelObject != null ? modelObject.getDescriptionDoc() : "";
	}

	public void setDescription(final String string) {
		if (modelObject != null) {
			modelObject.setDescriptionDoc(string);
		}
	}

	public List<TLDocumentationItem> getDevelopers() {
		return modelObject != null ? modelObject.getDeveloperDoc() : null;
	}

	public boolean isDocumentationOwner() {
		return modelObject != null ? modelObject.isDocumentationOwner() : false;
	}

	public TLDocumentation getDocumentation() {
		return modelObject == null ? new TLDocumentation() : modelObject.getDocumentation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#isDeprecated()
	 */
	@Override
	public boolean isDeprecated() {
		boolean ret = false;
		if (getDocumentation() == null)
			return ret;
		if (getDocumentation().getDeprecations() == null)
			return ret;
		if (getDocumentation().getDeprecations().size() > 0)
			for (TLDocumentationItem deprecation : getDocumentation().getDeprecations()) {
				if (deprecation.getText().isEmpty())
					continue;
				ret = true;
				break;
			}
		return ret;
	}

	public void addDeprecated(String text) {
		modelObject.addDeprecation(text);
	}

	public void addDescription(String text) {
		modelObject.addDescription(text);
	}

	public void addReference(String text) {
		modelObject.addReference(text);
	}

	public void addImplementer(String text) {
		modelObject.addImplementer(text);
	}

	public void addMoreInfo(String text) {
		modelObject.addMoreInfo(text);
	}

	public void setDevelopers(String doc, int index) {
		if (modelObject != null)
			modelObject.setDeveloperDoc(doc, index);
	}

	public void setMoreInfo(String info, int index) {
		if (modelObject != null)
			modelObject.setMoreInfo(info, index);
	}

	//
	public void setReferenceLink(String link, int index) {
		if (modelObject != null)
			modelObject.setReferenceDoc(link, index);
	}

	// public INode newAlias(final TLAlias tla) {
	// return null;
	// }

	/**
	 * Change all context users to use targetId. Iterates on all children. If the context would be duplicated, it is
	 * added as an implementors documentation item.
	 * 
	 * @param contextId
	 *            - remove all uses of this contextId
	 * @param targetId
	 *            - replace with this contextId
	 */
	public void mergeContext(String contextId, String targetId) {
		if (modelObject == null) {
			LOGGER.error("Model Object is null.");
			return;
		}
		// Change if one does not exist using targetId. Otherwise, copy the value to new implementers
		if (modelObject.getEquivalent(contextId) != null && !modelObject.getEquivalent(contextId).isEmpty()) {
			if (modelObject.getEquivalent(targetId) == null || modelObject.getEquivalent(targetId).isEmpty())
				modelObject.changeEquivalentContext(contextId, targetId);
			else {
				addImplementer("Equivalent value: " + contextId + " = " + modelObject.getEquivalent(contextId));
				LOGGER.debug("Created Implementers doc for value: " + modelObject.getEquivalent(contextId));
			}
		}
		if (modelObject.getExample(contextId) != null && !modelObject.getExample(contextId).isEmpty()) {
			if (modelObject.getExample(targetId) == null || modelObject.getExample(targetId).isEmpty())
				modelObject.changeExampleContext(contextId, targetId);
			else {
				addImplementer("Example value: " + contextId + " = " + modelObject.getExample(contextId));
				LOGGER.debug("Created Implementers doc for value: " + modelObject.getExample(contextId));
			}
		}

		if (isBusinessObject())
			((BusinessObjMO) modelObject).changeBusinessObjectContext(contextId, targetId);

		List<TLAdditionalDocumentationItem> odList;
		boolean hasTargetOd = false;
		if (getDocumentation() != null) {
			if ((odList = getDocumentation().getOtherDocs()) != null) {
				// If there already is one in target context then make into implementors doc instead
				for (TLAdditionalDocumentationItem od : odList) {
					if (od.getContext().equals(targetId)) {
						hasTargetOd = true;
						break;
					}
				}
				for (TLAdditionalDocumentationItem od : odList) {
					if (od.getContext().equals(contextId)) {
						if (!hasTargetOd)
							od.setContext(targetId);
						else
							addImplementer("Other doc: " + contextId + " = " + od.getText());
					}
				}
			}
		}
		// Iterate through all children
		for (Node n : getChildren())
			n.mergeContext(contextId, targetId);
	}

	public boolean moveProperty(final int i) {
		return false;
	}

	/**
	 * NOTE - this does not detect nodes that are created by the xsd utilities that represent local or anonymous types.
	 * Use {@code isXsdType()} instead.
	 * 
	 * @return true if the model object one of the XSD model objects
	 */
	public boolean isInXSDSchema() {
		return modelObject instanceof XSDElementMO || modelObject instanceof XSDComplexMO
				|| modelObject instanceof XSDSimpleMO;
	}

	public boolean isInBuiltIn() {
		return getLibrary() != null ? getLibrary().isBuiltIn() : false;
	}

	public boolean isInTLLibrary() {
		return getLibrary() != null ? getLibrary().isTLLibrary() : false;
	}

	/**
	 * @return true only if this object is in the version head library. false if not, false if owner is a service, or
	 *         unmanaged.
	 */
	public boolean isInHead() {
		Node owner = getOwningComponent();
		if (owner instanceof OperationNode) {
			owner = owner.getOwningComponent();
		}
		if (owner instanceof ServiceNode) {
			// service do not have versionNode
			return true;
		}
		// False if unmanaged.
		if (owner == null || owner.versionNode == null)
			return false;
		return getChain().getHead().getDescendants_NamedTypes().contains(owner);
	}

	/**
	 * Is the object new to the chain. The object is represented by one or more nodes with the same name within the
	 * chain.
	 * 
	 * @return True if this node is not in a chain, OR it is in the latest library of the chain AND not in a previous
	 *         version. Note that node may or may not be editable.
	 * 
	 */
	public boolean isNewToChain() {
		if (getChain() == null)
			return true;
		if (getVersionNode() == null)
			return true;

		if (getLibrary() != getChain().getHead())
			return false;
		// It is in head of chain. Is it new to the chain?
		return getVersionNode().getPreviousVersion() == null ? true : false;
	}

	@Override
	public String toString() {
		return modelObject != null ? getName() : "EmptyMO";
	}

	public XsdNode getXsdNode() {
		return xsdNode;
	}

	/**
	 * @return - a list of the descendants that have the this type assigned to them.
	 */
	public List<Node> getWhereUsed() {
		return getTypeClass().getTypeUsers();
	}

	/** ******************** Library access methods ******************/

	/**
	 * Get all libraries under <i>this</i> node. Note - only searches library containers. Libraries in the tree with an
	 * ancestor that is not a library container will not be found.
	 * 
	 * @return new list of library nodes.
	 */
	@Override
	public List<LibraryNode> getLibraries() {
		ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>();
		for (Node n : getChildren()) {
			if (n instanceof LibraryNode)
				libs.add((LibraryNode) n);
			else if (n.isLibraryContainer())
				libs.addAll(n.getLibraries());
		}
		return libs;
	}

	@Override
	public List<ProjectNode> getProjects() {
		ArrayList<ProjectNode> libs = new ArrayList<ProjectNode>();
		for (Node n : getChildren()) {
			if (n instanceof ProjectNode)
				libs.add((ProjectNode) n);
		}
		return libs;
	}

	/**
	 * Get all user libraries (OTM TLLibrary) under <i>this</i> node. Note - only searches library containers. Libraries
	 * in the tree with an ancestor that is not a library container will not be found.
	 * 
	 * @return new list of library nodes.
	 */
	@Override
	public List<LibraryNode> getUserLibraries() {
		final List<LibraryNode> tlLibs = new ArrayList<LibraryNode>();
		for (final Node n : getChildren()) {
			if (n.isTLLibrary()) {
				tlLibs.add((LibraryNode) n);
			}
			if (n.isLibraryContainer())
				tlLibs.addAll(n.getUserLibraries());
		}
		return tlLibs;
	}

	/**
	 * Static method to return all libraries in the model.
	 * 
	 * @return new list of library nodes for all libraries in the model
	 */
	public static List<LibraryNode> getAllLibraries() {
		return getModelNode().getLibraries();
	}

	/**
	 * Static method to return all projects in the model.
	 * 
	 * @return new list of projects nodes for all projects in the model
	 */
	public static List<ProjectNode> getAllProjects() {
		return getModelNode().getProjects();
	}

	/**
	 * Static method to return all libraries in the model.
	 * 
	 * @return new list of library nodes for all libraries in the model
	 */
	public static List<LibraryNode> getAllUserLibraries() {
		return getModelNode().getUserLibraries();
	}

	/**
	 * Return the single node that represents this object. Null if none. For VWA and Core, the simple property node is
	 * returned.
	 * 
	 * @return
	 */
	public Node getAssignable() {
		return null;
	}

	/**
	 * @return - list of unique Context IDs used by any child of this node. Empty list if none.
	 */
	public List<String> getContextIds() {
		final Map<String, String> ctxMap = new LinkedHashMap<String, String>();
		ArrayList<String> ret = new ArrayList<String>();
		List<TLContext> list = getCtxList();
		for (TLContext tlc : list) {
			if ((tlc != null && tlc.getApplicationContext() != null))
				ctxMap.put(tlc.getApplicationContext(), tlc.getContextId());
		}
		ret.addAll(ctxMap.values());
		// LOGGER.debug("Found "+ret.size()+" contexts in "+this.getName());
		return ret;
	}

	/**
	 * @return - list of unique TLContexts used by any child of this node. Empty list if none.
	 */
	public List<TLContext> getContexts() {
		final Map<String, TLContext> ctxMap = new LinkedHashMap<String, TLContext>();
		ArrayList<TLContext> ret = new ArrayList<TLContext>();
		List<TLContext> list = getCtxList();
		for (TLContext tlc : list) {
			if ((tlc != null && tlc.getApplicationContext() != null))
				ctxMap.put(tlc.getApplicationContext(), tlc);
		}
		ret.addAll(ctxMap.values());
		// LOGGER.debug("Found "+ret.size()+" contexts in "+this.getName());
		return ret;
	}

	private List<TLContext> getCtxList() {
		ArrayList<TLContext> list = new ArrayList<TLContext>();
		List<TLContext> cList = getModelObject().getContexts();
		if (cList.size() > 0) {
			list.addAll(cList);
		}
		for (Node child : getChildren()) {
			list.addAll(child.getCtxList());
		}
		// LOGGER.debug("Found "+list.size()+" contexts in "+this.getName());
		return list;
	}

	public ArrayList<Node> typeUsers() {
		return isTypeProvider() ? getTypeClass().getTypeUsers() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeUser#getAssignedModelObject()
	 */
	public ModelObject<?> getAssignedModelObject() {
		return isTypeUser() ? getTypeClass().getTypeOwner().getModelObject() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeUser#getAssignedTLObject()
	 */
	// @Deprecated
	public NamedEntity getAssignedTLObject() {
		return getTLTypeObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeUser#removeAssignedType()
	 */
	public void removeAssignedType() {
		if (isTypeUser())
			getTypeClass().removeAssignedType();
	}

	/**
	 * Can Assign - can the type be assigned to node?
	 * 
	 * @param type
	 *            - the node representing the type to be assigned
	 * @return - true if the assignment conforms to the rules.
	 */
	// TODO - look at how the TLnSimpleAttribute uses exception.
	// This approach may simplify assignment logic.
	public boolean canAssign(Node type) {
		return false;
	}

	/**
	 * Clone a node. Clone this node and all of its children. Creates new ModelObject, TL source object. Sets types for
	 * all the properties. Creates type node. Types are assigned to this component. Clones the TL and Model objects.
	 * Must be a library member. Assigns libraries and types. Added to parent and a family node may be created to
	 * contain <i>this</i> and the clone.
	 * 
	 * Note: the new component is <b>not</b> used to replace type users of this node (see {@link replaceTypesWith()}
	 * 
	 * @param library
	 *            to assign the new node to. If null, new node is not in a library.
	 * @param nameSuffix
	 *            Append to the new node's name.
	 * @return
	 */
	@Override
	public Node clone() {
		return clone(this.getLibrary(), null);
	}

	// returns owning navNode if it is a component node. Family aware - if in a family it returns
	// the family parent. Null otherwise.
	protected Node getOwningNavNode() {
		Node srcParent = null;
		if (this instanceof ComponentNode)
			srcParent = ((ComponentNode) this).getOwningNavNode();
		return srcParent;
	}

	public Node clone(String nameSuffix) {
		if (!(this instanceof ComponentNode))
			return null;

		// If this is a version chain, then set the library to the latest in the chain.
		if (getLibrary().isInChain()) {
			// Can't use library because of clone behavior.
			// Clone is always in -this- library. remove this then and it back library.
			LibraryNode thisLib = this.getLibrary();
			LibraryNode targetLib = this.getLibrary().getChain().getHead();
			this.removeFromLibrary();
			targetLib.addMember(this);

			Node clone = clone(this, nameSuffix);

			this.removeFromLibrary();
			thisLib.addMember(this);
			return clone;
		} else
			return clone(this.getLibrary(), nameSuffix);
	}

	/**
	 * Clone this node. If parent is null, the new node is only added to this library. If the parent is a library then
	 * the new node is added to <i>this</i> node's parent adjacent to this node if a property. If this is a
	 * namedMember() then the clone is added to this.library. Otherwise, parent is used to contain the new node.
	 * 
	 * @param parent
	 * @param nameSuffix
	 * @return
	 */
	public Node clone(Node parent, String nameSuffix) {
		LibraryNode lib = this.getLibrary();
		if (parent != null)
			lib = parent.getLibrary();
		if (parent instanceof LibraryNode)
			parent = this.getParent();

		if (lib == null || !lib.isEditable()) {
			LOGGER.warn("Could not clone node because library " + lib + " it is not editable.");
			return null;
		}
		Node newNode = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = cloneTLObj();
		if (newLM == null)
			return null;

		// Use the node factory to create the gui representation.
		if (this instanceof PropertyNode) {
			newNode = NodeFactory.newComponentMember(null, newLM);
			if (nameSuffix != null)
				newNode.setName(newNode.getName() + nameSuffix);
			if (parent != null && parent instanceof ComponentNode) {
				((ComponentNode) parent).addProperty(newNode, ((PropertyNode) this).indexOfTLProperty());
			}
		} else if (newLM instanceof LibraryMember) {
			newNode = NodeFactory.newComponent_UnTyped((LibraryMember) newLM);
			if (nameSuffix != null)
				newNode.setName(newNode.getName() + nameSuffix);
			if (getLibrary() != null)
				getLibrary().addMember(newNode);
		} else {
			LOGGER.warn("clone not supported for this node: " + this);
			return null;
		}

		// Now, use the source to set typeClass contents on node and descendants
		// if (!(isEnumeration() || isEnumerationLiteral()))
		this.cloneTypeAssignments(newNode);

		return newNode;
	}

	/**
	 * @return the cloned copy of a TL Model object.
	 */
	public TLModelElement cloneTLObj() {
		if (getLibrary() == null) {
			LOGGER.error("Can not clone without having a library.");
			return null;
		}

		TLModelElement newLM = null;
		try {
			newLM = (TLModelElement) getTLModelObject().cloneElement(getLibrary().getTLaLib());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Can not clone " + this + ". Exception: " + e.getLocalizedMessage());
			return null;
		}
		return newLM;
	}

	/**
	 * Set all the type users in newNode to match this nodes assignments. Only does typeClass assignments, not TL model
	 * assignments.
	 * 
	 * @param newNode
	 */
	protected void cloneTypeAssignments(Node newNode) {
		// LOGGER.debug(this + " type is " + this.getType());
		if (this instanceof PropertyNode) {
			newNode.setAssignedType(this.getType());
			return;
		}
		if (this.isSimpleType()) {
			if (newNode.getTLTypeObject() != this.getTLTypeObject()) {
				// LOGGER.debug("Fixing type mismatch!." + newNode);
				newNode.getModelObject().setTLType(this.getAssignedModelObject());
			}
			newNode.getTypeClass().setTypeNode(getAssignedType());
			return;
		}

		List<Node> srcUsers = this.getDescendants_TypeUsers();
		for (Node n : newNode.getDescendants_TypeUsers()) {
			for (Node u : srcUsers) {
				if ((n instanceof SimpleAttributeNode && u instanceof SimpleAttributeNode)
						|| u.getName().equals(n.getName())) {
					if (n.getTLTypeObject() != u.getTLTypeObject()) {
						// LOGGER.debug("Fixing type mismatch!." + n);
						n.getModelObject().setTLType(u.getAssignedModelObject());
					}
					if (u.getTLTypeObject() == null)
						n.getTypeClass().setTypeNode(ModelNode.getUnassignedNode());
					else
						n.getTypeClass().setTypeNode(u.getAssignedType());
					srcUsers.remove(u);
					break;
				}
			}
		}
	}

	public void sort() {
	}

	/**
	 * Merge source properties into <i>this</i> node. Does not change source node.
	 * 
	 * @param target
	 */
	public void merge(Node source) {
	}

	public boolean isMergeSupported() {
		return false;
	}

	@Override
	public boolean isAssignedByReference() {
		return false;
	}

	public boolean isSimpleListFacet() {
		return false;
	}

	public boolean isDetailListFacet() {
		return false;
	}

	/**
	 * 
	 * @param libraryNode
	 */
	public void linkLibrary(LibraryNode libraryNode) {
		LOGGER.error("addLibrary not Implmented for this class." + this.getClass());
	}

	/**
	 * Use the compiler to validate a node.
	 */
	public ValidationFindings validate() {
		ValidationFindings findings = TLModelCompileValidator.validateModelElement(this.getTLModelObject());
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT)) {
			LOGGER.debug("Finding: " + f);
		}
		return findings;
	}

	/**
	 * Generate validation results starting with this node.
	 * 
	 * @return true if no errors.
	 */
	public boolean isValid() {
		return validate().count(FindingType.ERROR) == 0 ? true : false;
	}

	public Document compileExampleDOM() {
		final ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder();
		ExampleGeneratorOptions options = new ExampleGeneratorOptions();
		exampleBuilder.setOptions(options);
		exampleBuilder.setModelElement((NamedEntity) this.getTLModelObject());
		Document domDoc = null;
		try {
			domDoc = exampleBuilder.buildDomTree();
		} catch (ValidationException e) {
			LOGGER.debug("Validation Exception on " + this + " : " + e);
			for (String finding : e.getFindings().getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT))
				LOGGER.debug("Finding: " + finding);
		} catch (CodeGenerationException e) {
			LOGGER.debug("CodeGen Exception on " + this + " : " + e);
		}
		return domDoc;
	}

	public String compileExampleXML(boolean quiet) {
		final ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder();
		ExampleGeneratorOptions options = new ExampleGeneratorOptions();
		exampleBuilder.setOptions(options);
		String xml = "ERROR";
		exampleBuilder.setModelElement((NamedEntity) this.getTLModelObject());
		try {
			xml = exampleBuilder.buildString();
		} catch (ValidationException e) {
			if (!quiet)
				LOGGER.debug("Validation Exception on " + this + " : " + e);
			for (String finding : e.getFindings().getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT))
				if (!quiet)
					LOGGER.debug("Finding: " + finding);
		} catch (CodeGenerationException e) {
			LOGGER.debug("CodeGen Exception on " + this + " : " + e);
		}
		// LOGGER.debug("XML example generated: " + xml);
		return xml;
	}

	/**
	 * @return the diagnostic identity
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Set the node's identity for diagnostics purposes
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * @return null if a type can be assigned, otherwise an implied node.
	 */
	public Node getDefaultType() {
		return null;
	}

	/**
	 * Property and simple type nodes have types with qNames.
	 * 
	 * @return
	 */
	public QName getTLTypeQName() {
		return null;
	}

	/**
	 * Visitors *********************************************
	 * 
	 * Sample Usage: thisModel.getModelNode().visitAllNodes(this.new count()); where: public class count implements
	 * NodeVisitor {
	 * 
	 * NodeVisitor visitor = new NodeVisitors().new NodeVisitors().new ValidateNodeTypes();
	 * curNode.visitAllNodes(visitor);
	 */
	public interface NodeVisitor {
		public void visit(INode n);
	}

	@Override
	public void visitChildren(NodeVisitor visitor) {
		for (Node c : getChildren())
			visitor.visit(c);
		visitor.visit(this);
	}

	@Override
	public void visitAllNodes(NodeVisitor visitor) {
		ArrayList<Node> kids = new ArrayList<Node>(getChildren());
		for (Node child : kids)
			child.visitAllNodes(visitor);
		visitor.visit(this);
	}

	@Override
	public void visitAllTypeProviders(NodeVisitor visitor) {
		for (Node c : getChildren_TypeProviders()) {
			if (c != null)
				c.visitAllTypeProviders(visitor);
		}
		if (isTypeProvider()) {
			visitor.visit(this);
		}
	}

	@Override
	public void visitAllTypeUsers(NodeVisitor visitor) {
		for (Node c : getChildren()) {
			c.visitAllTypeUsers(visitor);
		}
		if (isTypeUser()) {
			visitor.visit(this);
		}
	}

	public void visitAllBaseTypeUsers(NodeVisitor visitor) {
		for (Node c : getChildren()) {
			c.visitAllBaseTypeUsers(visitor);
		}
		// TODO - make interface for baseTypeusers
		if (isCoreObject() || isBusinessObject() || isExtensionPointFacet()) {
			visitor.visit(this);
		}
	}

	public void visitList(List<Node> list, NodeVisitor visitor) {
		for (Node n : list) {
			visitor.visit(n);
		}
	}

	/**
	 * @return true if children of the type are allowed in this node.
	 */
	public boolean isValidParentOf(Node node) {
		return false;
	}

	public boolean isValidParentOf(PropertyNodeType type) {
		return false;
	}

}
