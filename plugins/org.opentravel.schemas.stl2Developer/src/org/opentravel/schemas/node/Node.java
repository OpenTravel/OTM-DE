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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.modelObject.TLEmpty;
import org.opentravel.schemas.modelObject.XSDComplexMO;
import org.opentravel.schemas.modelObject.XSDElementMO;
import org.opentravel.schemas.modelObject.XSDSimpleMO;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.OperationFacetNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.VWA_AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.INodeListener;
import org.opentravel.schemas.node.listeners.NamedTypeListener;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereExtendedHandler;
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

	/**
	 * ************************ Documentation ****************************
	 * 
	 * TODO - reconcile with the documentation view use of the documentation nodeManager. TODO - do the enum/string[]
	 * right
	 */
	public static enum DocTypes {
		Description, Deprecation, MoreInformation, Implementer, ReferenceLink
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

	private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
	// See ImpliedNodeType
	public static final String UNDEFINED_PROPERTY_TXT = "Missing";

	/**
	 * Within the node classes, public adders and setters are responsible for keeping the nodes and underlying library
	 * model in sync.
	 * 
	 */
	// TO DO - eliminate root from node. It should be maintained by the model controller.
	protected static ModelNode root; // The root of the library catalog.

	protected static int nodeCount = 1; // used to assign nodeID
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
	 * Get the static root model node.
	 */
	public static ModelNode getModelNode() {
		return root;
	}

	/**
	 * Get the static root library model manager.
	 */
	public static LibraryModelManager getLibraryModelManager() {
		return root.getLibraryManager();
	}

	/**
	 * @return - return the node from the identity listener from the collection tlObject's listeners. If a facade, get
	 *         it's wrapped node.
	 */
	static public Node GetNode(Collection<ModelElementListener> listeners) {
		Node n = null;
		for (ModelElementListener listener : listeners)
			if (listener instanceof NodeIdentityListener) {
				n = ((NodeIdentityListener) listener).getNode();
				break;
			}
		if (n instanceof FacadeInterface)
			n = ((FacadeInterface) n).get();
		return n;
	}

	// TODO - why are these static? why have both static and non-static?
	/**
	 * @return - return the node from the tlObject's identity listener. If a facade, get it's wrapped node.
	 */
	static public Node GetNode(ModelElement tlObj) {
		return tlObj != null ? GetNode(((TLModelElement) tlObj).getListeners()) : null;
	}

	/**
	 * If the listener has a null node, don't use it for finding node.
	 * 
	 * @param listeners
	 *            collection from the TL Object
	 * @return the node associated with the first NodeIdentityListener.
	 */
	public Node getNode(Collection<ModelElementListener> listeners) {
		Node n = null;
		for (ModelElementListener listener : listeners)
			if (listener instanceof NodeIdentityListener) {
				if (((NodeIdentityListener) listener).getNode() != null)
					n = ((NodeIdentityListener) listener).getNode();
				break;
			}
		if (n instanceof FacadeInterface)
			n = ((FacadeInterface) n).get();
		return n;
	}

	protected String nodeID; // unique ID assigned to each node automatically

	// Facets and library members can be extended by an extension owner.
	protected WhereExtendedHandler whereExtendedHandler = null;

	// Ancestry
	protected LibraryNode library; // link to the library node to which this node belongs
	protected Node parent; // link to the parentNode node
	private final ArrayList<Node> children; // links to the children
	protected VersionNode versionNode; // Link to the version node representing this node in a chain

	public ModelObject<?> modelObject; // Generic interface to TL Model objects.
	protected boolean deleted = false;
	public boolean local = false; // Local nodes are not named nodes and are not to made visible in type assignment
	// lists.
	public XsdNode xsdNode = null; // Link to node containing imported XSD representation
	public boolean xsdType = false; // True if this node represents an object that was created by
									// the XSD utilities but has not be imported.

	public Node() {
		parent = null;
		children = new ArrayList<Node>();
		nodeID = Integer.toString(nodeCount++);
		setLibrary(null);
		modelObject = newModelObject(new TLEmpty());
		versionNode = null;
	}

	public Node(String identity) {
		this();
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
	}

	public void addDeprecated(String text) {
		if (isEditable())
			modelObject.addDeprecation(text);
	}

	public void addDescription(String text) {
		if (isEditable())
			modelObject.addDescription(text);
	}

	public void addImplementer(String text) {
		if (isEditable())
			modelObject.addImplementer(text);
	}

	public void addMoreInfo(String text) {
		if (isEditable())
			modelObject.addMoreInfo(text);
	}

	public void addReference(String text) {
		if (isEditable())
			modelObject.addReference(text);
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

	/**
	 * Clone this node. If parent is null, the new node is only added to this library. If the parent is a library then
	 * the new node is added to <i>this</i> node's parent adjacent to this node if a property. If this is a
	 * namedMember() then the clone is added to this.library. Otherwise, parent is used to contain the new node.
	 * 
	 * @param parent
	 *            - will try to use "this" parent if null
	 * @param nameSuffix
	 * @return null if error
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
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM == null)
			return null;

		// Use the node factory to create the gui representation.
		if (this instanceof PropertyNode) {
			newNode = NodeFactory.newMember(null, newLM);
			if (nameSuffix != null)
				newNode.setName(newNode.getName() + nameSuffix);
			if (parent instanceof ComponentNode) {
				((ComponentNode) parent).addProperty(newNode, ((PropertyNode) this).indexOfTLProperty());
			}
			// set assigned type using the type on the cloned tl object
			((TypeUser) newNode).setAssignedType(((TypeUser) newNode).getAssignedTLObject());

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
		// this.cloneTypeAssignments(newNode);

		return newNode;
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
	 * Clone object including type. Resulting object has no owner or listeners.
	 * 
	 * @return the cloned copy of a TL Model object.
	 */
	public LibraryElement cloneTLObj() {
		if (getLibrary() == null) {
			LOGGER.error("Can not clone without having a library.");
			return null;
		}

		LibraryElement newLM = null;
		try {
			newLM = getTLModelObject().cloneElement(getLibrary().getTLaLib());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Can not clone " + this + ". Exception: " + e.getLocalizedMessage());
			return null;
		}
		return newLM;
	}

	@Override
	public void close() {
		if (getLibrary() != null)
			getLibrary().setEditable(true);
		this.visitAllNodes(new NodeVisitors().new closeVisitor());
	}

	public Document compileExampleDOM() {
		final ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder(new ExampleGeneratorOptions())
				.setModelElement((NamedEntity) this.getTLModelObject());
		Document domDoc = null;
		try {
			domDoc = exampleBuilder.buildTree();
		} catch (ValidationException e) {
			LOGGER.debug("Validation Exception on " + this + " : " + e);
			// for (String finding : e.getFindings().getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT))
			// LOGGER.debug("Finding: " + finding);
		} catch (CodeGenerationException e) {
			LOGGER.debug("CodeGen Exception on " + this + " : " + e);
		}
		return domDoc;
	}

	public String compileExampleXML(boolean quiet) {
		final ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder(new ExampleGeneratorOptions())
				.setModelElement((NamedEntity) this.getTLModelObject());
		String xml = "ERROR";
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

	@Override
	public void delete() {
		// If a version-ed library, then also remove from aggregate
		// Library may be null! It is in some j-units.
		if (isDeleteable()) {
			NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
			// LOGGER.debug("Deleting " + this);
			this.visitAllNodes(visitor);
		} else
			LOGGER.debug("Not Deleteable: " + this);
	}

	public String emptyIfNull(String s) {
		return s == null ? "" : s;
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
		if (ns == null || ns.isEmpty())
			ns = ModelNode.Chameleon_NS;
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
		if (!getNamespace().equals(ns) && !(getNamespace().equals(ModelNode.Chameleon_NS)))
			return null;

		for (final Node n : getChildren()) {
			if (n.getName().equals(name) && !n.isNavigation()) {
				if (n instanceof XsdNode)
					return ((XsdNode) n).getOtmModel();
				return n;

			} else if ((c = n.findNode(name, ns)) != null) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Find the first library member node in the named type descendants of this node with the given name. The order
	 * searched is not guaranteed. Will not find family nodes.
	 * 
	 * @param name
	 * @return node found or null
	 */
	public Node findNodeByName(String name) {
		for (Node n : getDescendants_LibraryMembers()) {
			if (n.getName().equals(name))
				return n;
		}
		return null;
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

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.NONE;
	}

	public List<Node> getAncestors() {
		List<Node> ancestors = new ArrayList<Node>();
		Node n = this;
		do {
			ancestors.add(n);
			n = n.getParent();
		} while (!(n instanceof ModelNode));
		return ancestors;
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
	 * Return the library chain that this node belongs to.
	 * 
	 * @return the chain or null if not in a chain.
	 */
	public LibraryChainNode getChain() {
		return getLibrary() != null ? getLibrary().getChain() : null;
	}

	@Override
	public List<Node> getChildren() {
		return children;
	}

	/**
	 * @return a new ArrayList containing the children
	 */
	public List<Node> getChildren_New() {
		return new ArrayList<Node>(children);
	}

	/*
	 * @see org.opentravel.schemas.node.Node#getChildren_TypeProviders()
	 */
	// FIXME - why is this using isTypeProvider() ? Should instanceof test.
	@Override
	public List<Node> getChildren_TypeProviders() {
		final ArrayList<Node> kids = new ArrayList<Node>();
		for (final Node n : getChildren()) {
			if (n.isNamedEntity() || n.hasChildren_TypeProviders()) {
				if (n instanceof VersionNode && ((VersionNode) n).get() != null)
					kids.add(((VersionNode) n).get());
				else
					kids.add(n);
			}
		}
		return kids;
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
	 * Get Component Node Type
	 * 
	 * @return the enumerated value associated with this node or null
	 */
	public ComponentNodeType getComponentNodeType() {
		return null;
	}

	@Override
	public String getComponentType() {
		return getComponentNodeType() == null ? "" : getComponentNodeType().getDescription();
	}

	public String getDecoration() {
		// if it is a named entity in a versioned library get version.
		String decoration = " ";

		// The number of users for this type provider
		if (this instanceof TypeProvider && !(this instanceof ImpliedNode))
			decoration += " (" + ((TypeProvider) this).getWhereUsedAndDescendantsCount() + " users)";

		if (isDeleted())
			decoration += " (*) ";

		if (isDeprecated())
			decoration += " (Deprecated)";

		// Extension
		if (this instanceof ExtensionOwner) {
			if (getLibrary() != null)
				getLibrary().checkExtension(this);

			String extensionTxt = "";

			// The only true extension will be to the oldest version of this object.
			// All other extensions will be for versions.
			ComponentNode exBase = (ComponentNode) getExtendsType();
			if (exBase != null) {
				extensionTxt += "Extends: " + exBase.getNameWithPrefix() + " ";
				if (getChain() != null)
					extensionTxt += " - ";
			}
			// Version and edit-ability
			if (getChain() != null) {
				// if (versionBase != null)
				if (isInHead())
					if (getLibrary().isMajorVersion())
						extensionTxt += "Major Version";
					else if (isNewToChain())
						extensionTxt += "New to this version";
					else
						extensionTxt += "Current Version";
				else
					extensionTxt += "Version: " + getTlVersion();
				if (isEditable())
					extensionTxt += " - ";
			}
			if (isEditable()) {
				if (getChain() == null) // Not in chain
					extensionTxt += "Full Editing";
				else if (isInHead())
					if (isNewToChain())
						extensionTxt += "Full Editing"; // Is newly added to chain
					else
						extensionTxt += "Minor Editing";
				else if (this instanceof VersionedObjectInterface)
					extensionTxt += "Minor Editing will create new version";
			}
			decoration += surround(extensionTxt);
		}
		return decoration;
	}

	public String surround(String txt) {
		if (txt != null && !txt.isEmpty()) {
			return " (" + txt + ")";
		}
		return "";
	}

	public PropertyOwnerInterface getDefaultFacet() {
		return this instanceof ComplexComponentInterface ? ((ComplexComponentInterface) this).getDefaultFacet() : null;
	}

	/**
	 * @return a new list containing all children nodes and their descendants. No filtering; includes aggregate, version
	 *         and navNodes.
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

	/**
	 * Gets the descendants that are extension owners.
	 * 
	 * @return new list of all descendants that are extension owners.
	 */
	public List<ExtensionOwner> getDescendants_ExtensionOwners() {
		final ArrayList<ExtensionOwner> ret = new ArrayList<ExtensionOwner>();
		for (final Node n : getChildren()) {
			if (n instanceof ExtensionOwner)
				ret.add((ExtensionOwner) n);

			// Some type users may also have children
			if (n.hasChildren() && !(n instanceof WhereUsedNodeInterface))
				ret.addAll(n.getDescendants_ExtensionOwners());
		}
		return ret;
	}

	public List<ContributedFacetNode> getDescendants_ContributedFacets() {
		final ArrayList<ContributedFacetNode> ret = new ArrayList<ContributedFacetNode>();
		for (final Node n : getChildren()) {
			if (n instanceof ContributedFacetNode)
				ret.add((ContributedFacetNode) n);

			// Some children may have contributed facets
			if (n.hasChildren() && !(n instanceof WhereUsedNodeInterface))
				ret.addAll(n.getDescendants_ContributedFacets());
		}
		return ret;

	}

	/**
	 * 
	 * @return new list of all contextual facets including contributed facets
	 */
	public List<ContextualFacetNode> getDescendants_ContextualFacets() {
		final ArrayList<ContextualFacetNode> ret = new ArrayList<ContextualFacetNode>();
		for (final Node n : getChildren()) {
			if (n instanceof ContextualFacetNode)
				ret.add((ContextualFacetNode) n);

			// Some children may have contributed facets
			if (n.hasChildren() && !(n instanceof WhereUsedNodeInterface))
				ret.addAll(n.getDescendants_ContextualFacets());
		}
		return ret;

	}

	/*****************************************************************************
	 * Static getters
	 */

	/**
	 * return new list of NamedEntities. Traverse via hasChildren. For version chains, it returns the newest version
	 * using the version node and does not touch aggregates.
	 */
	@Override
	public List<Node> getDescendants_LibraryMembers() {
		// keep duplicates out of the list that version aggregates may introduce
		HashSet<Node> namedKids = new HashSet<Node>();
		for (Node c : getChildren()) {
			// TL model considers services as named library member
			if (c.isLibraryMember())
				if (c instanceof FacadeInterface)
					namedKids.add(((FacadeInterface) c).get());
				else
					namedKids.add(c);
			else if (c.hasChildren())
				namedKids.addAll(c.getDescendants_LibraryMembers());
		}
		return new ArrayList<Node>(namedKids);
	}

	/**
	 * Gets the descendants that are type SimpleComponentNode. Does not return navigation nodes.
	 * 
	 * @return new list of all descendants that simple components.
	 */
	public List<SimpleComponentNode> getDescendants_SimpleComponents() {
		final ArrayList<SimpleComponentNode> ret = new ArrayList<SimpleComponentNode>();
		for (final Node n : getChildren()) {
			if (n instanceof SimpleComponentNode)
				ret.add((SimpleComponentNode) n);

			// check children
			if (n.hasChildren() && !(n instanceof WhereUsedNodeInterface))
				ret.addAll(n.getDescendants_SimpleComponents());
		}
		return ret;
	}

	/*****************************************************************************
	 * Children
	 */

	/**
	 * Gets the descendants that are type providers (can be assigned as a type). Does not return navigation nodes.
	 * 
	 * @return new list of all descendants that can be assigned as a type.
	 */
	public List<TypeProvider> getDescendants_TypeProviders() {
		final ArrayList<TypeProvider> ret = new ArrayList<TypeProvider>();
		for (final Node n : getChildren()) {
			if (n instanceof TypeProvider)
				ret.add((TypeProvider) n);

			// Some type users may also have children
			if (n.hasChildren() && !(n instanceof WhereUsedNodeInterface))
				ret.addAll(n.getDescendants_TypeProviders());
		}
		return ret;
	}

	/**
	 * Gets the descendants that are type users (can be assigned a type). Does not return navigation nodes.
	 * {@link #getChildren_TypeUsers() Use getChildren_TypeUsers() for only immediate children.}
	 * 
	 * @return new list of all descendants that can be assigned a type.
	 */
	public List<TypeUser> getDescendants_TypeUsers() {
		final ArrayList<TypeUser> ret = new ArrayList<TypeUser>();
		for (final Node n : getChildren()) {
			if (n instanceof TypeUser)
				ret.add((TypeUser) n);

			// Do not traverse UserNodes
			if (n instanceof WhereUsedNodeInterface)
				continue;

			// Some type users may also have children
			if (n.hasChildren())
				ret.addAll(n.getDescendants_TypeUsers());
		}
		return ret;
	}

	public String getDescription() {
		return modelObject != null ? modelObject.getDescriptionDoc() : "";
	}

	public List<TLDocumentationItem> getDevelopers() {
		return modelObject != null ? modelObject.getDeveloperDoc() : null;
	}

	public TLDocumentation getDocumentation() {
		return modelObject == null ? new TLDocumentation() : modelObject.getDocumentation();
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

	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		return null;
	}

	@Override
	public IValueWithContextHandler getExampleHandler() {
		return null;
	}

	/**
	 * Return the actual extension object. Will not return objects that are using extension for version relationships.
	 * This method will examine the whole chain to find the oldest version of the object and return its base type if
	 * any.
	 * 
	 * @see ExtensionOwner#getExtensionBase()
	 */
	public Node getExtendsType() {
		if (this instanceof ExtensionOwner) {
			Node oldestVersion = this;
			if (getVersionNode() != null)
				oldestVersion = getVersionNode().getOldestVersion();
			if (oldestVersion instanceof ExtensionOwner && !oldestVersion.isVersioned())
				return ((ExtensionOwner) oldestVersion).getExtensionBase();
		}
		return null;
	}

	/**
	 * Find the actual extension object and return its name. Will not find objects that are using extension for version
	 * relationships.
	 * 
	 * @see #getExtendsType()
	 * @return name of the extension entity or empty string
	 */
	public String getExtendsTypeName() {
		return getExtendsType() != null ? getExtendsType().getName() : "";
	}

	@Override
	public Image getImage() {
		final ImageRegistry imageRegistry = Images.getImageRegistry();
		return imageRegistry.get("file");
	}

	public List<Node> getInheritedChildren() {
		return Collections.emptyList();
	}

	@Override
	public String getLabel() {
		return getName();
	}

	/**
	 * @return list of later versions in a minor chain or null for the type assigned to this node.
	 */
	public List<Node> getLaterVersions() {
		List<Versioned> versions = null;
		List<Node> vNodes = new ArrayList<Node>();
		if (!(this instanceof TypeUser))
			return null;
		Node assignedType = (Node) ((TypeUser) this).getAssignedType();
		if (assignedType == null)
			return null;

		if (assignedType.getTLModelObject() instanceof Versioned && !(assignedType instanceof ImpliedNode)) {
			try {
				versions = new MinorVersionHelper().getLaterMinorVersions((Versioned) assignedType.getTLModelObject());
				for (Versioned v : versions) {
					for (ModelElementListener l : ((TLModelElement) v).getListeners())
						if (l instanceof INodeListener)
							vNodes.add(((INodeListener) l).getNode()); // could be duplicates if multiple listeners
				}
			} catch (VersionSchemeException e) {
				LOGGER.debug("Error: " + e.getLocalizedMessage());
				return null;
			}
		} else
			return null;
		return vNodes.isEmpty() ? null : vNodes;

	}

	/**
	 * Get all libraries under <i>this</i> node. Note - only searches library containers. Libraries in the tree with an
	 * ancestor that is not a library container will not be found. Returns libraries in chains, not the chain.
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
	public LibraryNode getLibrary() {
		return library;
	}

	@Override
	public ModelObject<?> getModelObject() {
		return modelObject;
	}

	@Override
	public abstract String getName();

	@Override
	public String getPrefix() {
		return getLibrary() == null ? "" : getLibrary().getPrefix();
	}

	@Override
	public String getNamespace() {
		return getLibrary() == null ? "" : getLibrary().getNamespace();
	}

	public String getNamespaceWithPrefix() {
		return getLibrary() == null ? "" : getLibrary().getNamespaceWithPrefix();
	}

	@Override
	public String getNameWithPrefix() {
		String prefix = "";
		if (getLibrary() == null) {
			// owning library might have been closed
			if (getTLModelObject() instanceof NamedEntity
					&& ((NamedEntity) getTLModelObject()).getOwningLibrary() != null)
				prefix = ((NamedEntity) getTLModelObject()).getOwningLibrary().getPrefix();
		} else
			prefix = getLibrary().getPrefix();
		return prefix + ":" + getName();
	}

	/**
	 * Get a new list of child nodes that are to be displayed in navigator trees.
	 * 
	 * @param deep
	 *            when true some nodes will return more children such as properties
	 * 
	 * @see {@link #isNavChild()}
	 * @see {@link org.opentravel.schemas.node.Node_NavChildren_Tests#getNavChildrenTests()}
	 * 
	 * @return new list of children to be used for navigation purposes.
	 */
	public List<Node> getNavChildren(boolean deep) {
		ArrayList<Node> kids = new ArrayList<Node>();
		for (Node c : getChildren())
			if (c.isNavChild(deep))
				kids.add(c);
		return kids;
	}

	/**
	 * The string to present in the navigator tree and other library trees. Is label unless overridden.
	 * 
	 * @return
	 */
	public String getNavigatorName() {
		return getLabel();
	}

	/**
	 * Get a new listener for this type of node.
	 * 
	 * @return
	 */
	public BaseNodeListener getNewListener() {
		return new NamedTypeListener(this);
	}

	/**
	 * Used in drag-n-drop
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * Return the owning named entity. For contextual facets that have been contributed to a named entity then the owner
	 * of all children will be the named entity.
	 */
	public Node getOwningComponent() {
		return this;
	}

	/**
	 * Return actual parent.
	 */
	@Override
	public Node getParent() {
		return parent;
	}

	// TODO - move to model node
	@Override
	public List<ProjectNode> getProjects() {
		ArrayList<ProjectNode> libs = new ArrayList<ProjectNode>();
		for (Node n : getChildren()) {
			if (n instanceof ProjectNode)
				libs.add((ProjectNode) n);
		}
		return libs;
	}

	// public RoleFacetNode getRoleFacet() {
	// // Find the roles facet
	// return getOwningComponent() != this ? getOwningComponent().getRoleFacet() : null;
	// }

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
	 * Get all immediate navChildren that are to be presented in the OTM Object Tree. Includes where used nodes.
	 * Overridden on nodes that add nodes such as where used to the tree view.
	 * 
	 * @see {@link #getNavChildren()}
	 * 
	 * @param deep
	 *            - include properties
	 * 
	 * @return new list
	 */
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	/**
	 * returns If a type user then return getAssignedType() node else null
	 */
	@Override
	public Node getType() {
		return (Node) ((this instanceof TypeUser) ? ((TypeUser) this).getAssignedType() : null);
	}

	@Override
	public String getTypeName() {
		return getType() != null ? getType().getName() : "";
	}

	@Override
	public String getTypeNameWithPrefix() {
		return "";
	}

	/**
	 * @return - list of unique TLContexts used by any child of this node. Empty list if none.
	 */
	public List<TLContext> getUsedContexts() {
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

	/**
	 * Get all user libraries (OTM TLLibrary) from the LibraryModelManager.
	 * 
	 * @return new list of library nodes.
	 */
	@Override
	public List<LibraryNode> getUserLibraries() {
		return getModelNode().getLibraryManager().getUserLibraries();
	}

	public String getValidationIdentity() {
		if (modelObject != null && modelObject.getTLModelObj() != null) {
			return ((Validatable) modelObject.getTLModelObj()).getValidationIdentity();
		}
		return "";
	}

	/**
	 * Simple getter of the versionNode field.
	 * 
	 * @return the version node representing this node in the specific library in a chain.
	 */
	public VersionNode getVersionNode() {
		return versionNode;
	}

	/**
	 * @return where extended handler. Will create one if null.
	 */
	public WhereExtendedHandler getWhereExtendedHandler() {
		if (whereExtendedHandler == null)
			whereExtendedHandler = new WhereExtendedHandler(this);
		return whereExtendedHandler;
	}

	public XsdNode getXsdNode() {
		return xsdNode;
	}

	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return false;
	}

	public boolean hasInheritedChildren() {
		List<Node> inheritedChildren = getInheritedChildren();
		return (inheritedChildren != null) && !inheritedChildren.isEmpty();
	}

	/**
	 * Fast (no array creation) method to determine if there are navChildren that should be displayed in navigator
	 * trees.
	 * 
	 * @param deep
	 *            enable the "deep" property mode
	 */
	public boolean hasNavChildren(boolean deep) {
		for (final Node n : getChildren())
			if (n.isNavChild(deep))
				return true;
		return false;
	}

	// Override on classes that add to getNavChildren()
	public boolean hasTreeChildren(boolean deep) {
		return hasNavChildren(deep);
	}

	// TODO - move to add alias action
	@Override
	public boolean isAliasable() {
		// overloaded on aliasable owning components.
		return getOwningComponent() != null && this != getOwningComponent() ? getOwningComponent().isAliasable()
				: false;
	}

	public boolean isAssignable() {
		return isElementAssignable() || isNamedEntity() || isSimpleAssignable();
	}

	@Override
	public boolean isAssignedByReference() {
		return false;
	}

	// /**
	// * @return true if the node has a complex type assigned as the type of the property
	// *
	// * Should use Node.isRenamable()
	// */
	// @Deprecated
	// public boolean isAssignedComplexType() {
	// return false;
	// }

	public boolean isBuiltIn() {
		LibraryNode ln = getLibrary();
		return ln != null && ln.isBuiltIn();
	}

	public boolean isDefaultFacet() {
		return false;
	}

	/**
	 * @return - true if this node is in the current default library
	 */
	public boolean isDefaultLibrary() {
		if (getLibrary() == null)
			return false;
		ProjectNode pNode = getLibrary().getProject();
		return pNode != null ? pNode.getTLProject().getDefaultItem() == getLibrary().getProjectItem() : false;
	}

	/**
	 * @return False for node that can not be deleted: not-editable, facets, simpleFacets. Custom and Query Facets are
	 *         delete-able. Libraries are <b>always</b> delete-able
	 */
	public boolean isDeleteable() {
		if (getLibrary() == null)
			return false;

		// If it doesn't have a parent then it is not linked and can be deleted.
		if (getOwningComponent() == null || getOwningComponent().getParent() == null)
			return true;

		// You can't delete anything from a patch except an extension point OR a newly added object
		if (getLibrary().getChain() != null)
			if (getOwningComponent().isInHead() && getLibrary().getChain().getHead().isPatchVersion()) {
				if (!getOwningComponent().isVersioned())
					return true; // new to the patch
				else if ((getOwningComponent() instanceof ExtensionPointNode))
					return true;
				else
					return false; // nothing else can be deleted
			}

		// Services always return false for inhead(). Make sure it is in the head library.
		if (isInService() && getChain() != null)
			return getLibrary().getChain().getHead() == getLibrary() && isEditable();

		// Library members can only be deleted if they are new to the chain.
		if (getLibrary().isManaged()) {
			if (isInHead() && isEditable()) {
				if (this instanceof LibraryMemberInterface)
					return isNewToChain();
				return true;
			} else
				return false;
		}
		return isEditable();
		// return getLibrary().isManaged() ? isInHead() && isEditable() && isNewToChain() : isEditable();
	}

	@Override
	public boolean isDeleted() {
		if ((modelObject == null) || (modelObject.getTLModelObj() == null))
			deleted = true;
		return deleted;
	}

	@Override
	public boolean isDeprecated() {
		return modelObject != null && modelObject.getDeprecation() != null ? true : false;
	}

	public boolean isDocumentationOwner() {
		return modelObject != null ? modelObject.isDocumentationOwner() : false;
	}

	/**
	 * Implied nodes and nodes without libraries are always editable. Nodes in chains return if chain is editable.
	 * 
	 * @return true if the node's library is editable and is not inherited.
	 * @see Node#isInherited()
	 */
	@Override
	public boolean isEditable() {
		boolean result = false;
		if (getChain() != null)
			result = getChain().isEditable();
		else if (this instanceof ImpliedNode)
			result = true;
		else if (isInherited())
			result = false;
		else if (getLibrary() == null)
			result = true;
		else
			result = getLibrary().isEditable();
		// LOGGER.debug("Is " + this + " editable? " + result);
		return result;
	}

	/**
	 * @return Can the description field be edited?
	 */
	public boolean isEditable_description() {
		return getTLModelObject() instanceof TLDocumentationOwner ? isInHead2() && !isInherited() && isEditable()
				: false;
	}

	public boolean isEditable_equivalent() {
		return getTLModelObject() instanceof TLEquivalentOwner ? !isInherited() && isEditable() : false;
	}

	public boolean isEditable_example() {
		return getTLModelObject() instanceof TLExampleOwner ? !isInherited() && isEditable() : false;
	}

	/**
	 * Could this object be edited if a minor version was created? Use in GUI enabling actions that are allowed for
	 * minor versions. Objects that pass this test may have to have a minor version created before the model is modified
	 * {see isEditable_inMinor()}
	 * 
	 * @return
	 */
	public boolean isEditable_ifMinorCreated() {
		if (getLibrary() == null || isDeleted() || !isEditable())
			return false; // not editable

		if (getChain() == null)
			return true; // editable and not in a chain

		if (getOwningComponent().getVersionNode() == null)
			return true; // editable, and not in a chain (duplicate logic?)

		if (getChain().getHead() == null)
			return false; // error
		if (getChain().getHead() == getLibrary())
			return false; // is already in head library so we can't make a minor version of the object.

		// is head library editable and a minor version?
		return getChain().getHead().isEditable() && getChain().getHead().equals(NodeEditStatus.MINOR);
	}

	/**
	 * Is the owning object in a Minor version library and ready for model changes. Used in the model to assure editing
	 * is allowed. The test does NOT consider if the action is allowed. Is the owning object editable and either new to
	 * the chain OR in an editable minor version. Used for model change tests for object characteristics that are
	 * allowed to be changed in a minor version of an object that is extends an older version.
	 * 
	 * Use for characteristics that may be changed in a minor version of an object
	 */
	public boolean isEditable_inMinor() {
		if (getLibrary() == null || isDeleted() || !isEditable())
			return false; // not editable

		if (getChain() == null)
			return true; // editable and not in a chain

		if (getOwningComponent().getVersionNode() == null)
			return true; // editable, and not in a chain (duplicate logic?)

		if (getLibrary() != getChain().getHead())
			return false; // is not in the head library of the chain.

		// It is in head of chain. Return true if there are no previous versions
		if (getOwningComponent().getVersionNode().getPreviousVersion() == null)
			return true;

		// It is in editable head library and is a minor with previous versions.
		return getEditStatus().equals(NodeEditStatus.MINOR);
	}

	/**
	 * @return true if this node editable, not a patch and either a service, operation, message or message property.
	 */
	// TODO - reconile with isInService()
	public boolean isEditable_inService() {
		if (!isEditable())
			return false;
		if (getLibrary() == null)
			return false;
		if (getLibrary().getChain() != null)
			if (getLibrary().getChain().getHead().isPatchVersion())
				return false;
		if (this instanceof ServiceNode)
			return true;
		if (this instanceof OperationNode)
			return true;
		if (this instanceof OperationFacetNode)
			return true;
		if (getParent() != null)
			if (this instanceof PropertyNode && getParent() instanceof OperationFacetNode)
				return true;
		return false;
	}

	/**
	 * Is this owner editable based on not being in a chain, in the editable head library, or could be used to create a
	 * minor version.
	 * 
	 * @return true if it is editable or could be edited.
	 */
	public boolean isEditable_isNewOrAsMinor() {
		if (getLibrary() == null || isDeleted() || !isEditable() || getOwningComponent() == null)
			return false; // not editable

		// Service nodes are not in a chain
		if (!(this instanceof ServiceNode) && !(this instanceof OperationNode) && !(this instanceof OperationFacetNode)) {
			if (getChain() == null || getOwningComponent().getVersionNode() == null)
				return true; // editable because it is not in a chain

			if (getChain().getHead() == null || !getChain().getHead().isEditable())
				return false; // not editable head library
		}
		if (getChain() == null)
			return true;

		if (getLibrary() == getChain().getHead())
			return true; // editable by being in the head library

		if (getChain().getHead().isMinorVersion() && (getOwningComponent() instanceof VersionedObjectInterface))
			return true; // could have a component created that is editable

		return false;

	}

	/**
	 * Is the owning object editable and new to the chain. The object is represented by one or more nodes with the same
	 * name within the chain. Non-inherited properties in an object in a head library are new and therefore editable.
	 * 
	 * @return True if this node is editable AND is not in a chain, OR it is in the latest library of the chain AND not
	 *         in a previous version.
	 * 
	 */
	public boolean isEditable_newToChain() {
		if (getLibrary() == null || isDeleted() || !isEditable())
			return false; // not editable

		if (getChain() == null)
			return true; // editable and not in a chain

		// Only properties in an extension point may be edited in a patch version
		if (getChain().getHead().isPatchVersion())
			if (getOwningComponent() instanceof ExtensionPointNode)
				return !isInherited();
			else
				return false; // no editing in a patch version

		// Service components are only editable if they are in the head library. inHead() does not work.
		if (isInService())
			return getLibrary().getChain().getHead() == getLibrary();

		if (getOwningComponent().getVersionNode() == null)
			return true; // will be true for service descendants, editable, and not in a chain (duplicate logic?)

		if (getLibrary() != getChain().getHead())
			return false; // is not in the head library of the chain.

		if (this instanceof PropertyNode)
			return !isInherited(); // properties in the head library are editable

		// It is in head of chain. Return true if there are no previous versions
		return !getOwningComponent().isVersioned();
		// return getOwningComponent().getVersionNode().getPreviousVersion() == null;
	}

	public boolean isElementAssignable() {
		return modelObject instanceof XSDElementMO;
	}

	/**
	 * Tests if a node can be added to based edit-ability and version status. Used in global selection tester.
	 * <b>Note,</b> a minor version might have to be created before properties can be added. (use isNewToChain() to
	 * test).
	 * 
	 * • Values with Attributes (VWA) • Core Object • Business Object • Operation
	 * 
	 * @return true if the node can have properties added.
	 */
	// Override to DISABLE adding properties
	public boolean isEnabled_AddProperties() {
		if (library == null || parent == null || !isEditable() || isDeleted())
			return false;

		// service, operation, message or message property
		// Adding to service will automatically create correct service operation to add to.
		// overridden
		// if (this instanceof ServiceNode)
		// if (!getLibrary().isInChain())
		// return true;
		// else
		// return !getLibrary().getChain().getHead().getEditStatus().equals(NodeEditStatus.PATCH);
		// else
		if (isEditable_inService() && getLibrary().getChain() != null
				&& getLibrary().getChain().getHead() == getLibrary())
			// Only add properties to service in the head library.
			return !getLibrary().getChain().getHead().getEditStatus().equals(NodeEditStatus.PATCH);

		// Operations, business, core, vwa, open enums and extension points - allow major, minor, or unmanaged and
		if (this instanceof VersionedObjectInterface)
			return isEditable_isNewOrAsMinor();

		if (this instanceof Enumeration)
			return isEditable_isNewOrAsMinor();

		if (this instanceof ExtensionPointNode)
			return isEditable_newToChain();

		if (this instanceof ContextualFacetNode)
			return isEditable_newToChain();

		// Facets - same as parent unless a simple or list
		if (this instanceof SimpleFacetNode || this instanceof ListFacetNode)
			return false;
		if (this instanceof FacetNode)
			return getOwningComponent().isEnabled_AddProperties();
		if (this instanceof VWA_AttributeFacetNode)
			return getOwningComponent().isEnabled_AddProperties();
		if (this instanceof RoleFacetNode)
			return getOwningComponent().isEnabled_AddProperties();

		// Properties - same as parent
		if (this instanceof SimpleAttributeNode)
			return false;
		if (this instanceof PropertyNode)
			return this != getOwningComponent() ? getOwningComponent().isEnabled_AddProperties() : false;

		return false;
	}

	/**
	 * @return true if this property (or simple attribute) is enabled for setting assigned type
	 */
	public boolean isEnabled_AssignType() {
		boolean enabled = false;
		if (isEditable() && this instanceof TypeUser)
			// if (isEditable() && this instanceof TypeUser && !isInheritedProperty())
			if (getChain() == null || getChain().isMajor())
				enabled = true; // Unmanaged or major - allow editing.
			else if (getChain().isPatch())
				enabled = false; // no changes in a patch
			else if (this instanceof SimpleAttributeNode)
				enabled = isNewToChain(); // only allow editing if owner is new to the minor
			else if (isInHead2() && !isInherited())
				enabled = true; // Allow unless this property also exists in prev version
			else if (getLaterVersions() != null)
				enabled = true; // If the assigned type has a newer version then allow them to select that.
		return enabled;
	}

	/**
	 * @return true if this is extended by the passed base node
	 */
	public boolean isExtendedBy(Node base) {
		if (this instanceof ExtensionOwner)
			return modelObject.isExtendedBy((NamedEntity) base.getTLModelObject());
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
	 * Extensible objects have the ability to create extension points when compiled into schemas. These include core and
	 * business objects as well as operations and extension points. {@link #isExtensible()}
	 * 
	 * @return true if this object has the characteristic of being extensible
	 */
	public boolean isExtensibleObject() {
		return true;
	}

	public boolean isFacetAlias() {
		return false;
	}

	public boolean isFacetUnique(final INode testNode) {
		Node n = this;
		if (n instanceof PropertyNode) {
			n = n.parent;
		}
		if (n instanceof FacetNode && !(n instanceof QueryFacetNode) && !(n instanceof RoleFacetNode)) {
			n = n.parent; // compare across all facets.
		}
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

	public boolean isImportable() {
		return false;
	}

	public boolean isInBuiltIn() {
		return getLibrary() != null ? getLibrary().isBuiltIn() : false;
	}

	/**
	 * @return true only if this object is in the version head library. false if not, false if owner is a service, or
	 *         unmanaged. See also: isInHead2()
	 * 
	 */
	// TODO Compare results of this from the commonly used:
	// if (selectedNode.getLibrary() != selectedNode.getChain().getHead())
	public boolean isInHead() {
		Node owner = getOwningComponent();
		if (owner instanceof OperationNode)
			owner = owner.getOwningComponent();

		// service do not have versionNode
		if (owner instanceof ServiceNode)
			return true;

		// False if unmanaged.
		if (owner == null || owner.versionNode == null)
			return false;

		// List<Node> x = getChain().getHead().getDescendants_NamedTypes();
		if (getChain() == null || getChain().getHead() == null)
			return false;
		// List<Node> members = getChain().getHead().getDescendants_LibraryMembers();
		return getChain().getHead().getDescendants_LibraryMembers().contains(owner);
	}

	/**
	 * @return true if unmanaged (no chain) or head of the chain.
	 */
	public boolean isInHead2() {
		if (getChain() == null)
			return true;
		return getLibrary() == getChain().getHead();
	}

	/**
	 * @return the inherited field value or else false.
	 */
	public boolean isInherited() {
		return false;
	}

	/**
	 * @return - true if is in TL, built-in or xsd library
	 */
	public boolean isInModel() {
		return isInTLLibrary() || isInBuiltIn() || isInXSDSchema();
	}

	public boolean isInService() {
		if (this instanceof ServiceNode)
			return true;
		if (this instanceof OperationNode)
			return true;
		if (this instanceof FacetNode)
			return getParent() instanceof OperationNode;
		else
			return getOwningComponent().getParent() instanceof OperationNode;
	}

	/**
	 * Is <i>this</i> node an instance of the passed node? Does this tl object have an tlExtension with an extended
	 * entity of node's tl object?
	 * 
	 * @param node
	 * @return true if this is extended by the passed node
	 */
	public boolean isInstanceOf(Node node) {
		if (isExtendedBy(node)) {
			if (!node.getWhereExtendedHandler().getWhereExtended().contains(this))
				LOGGER.warn("Base node " + node.getNameWithPrefix() + " does not have extension "
						+ this.getNameWithPrefix() + " in its where extended list. ");
			return true;
		} else {
			Node baseNode = Node.GetNode((TLModelElement) modelObject.getTLBase());
			if (baseNode == null)
				return false;
			return baseNode.isInstanceOf(node);
		}
	}

	public boolean isInTLLibrary() {
		return getLibrary() != null ? getLibrary().isTLLibrary() : false;
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

	/*****************************************************************************
	 * is Properties
	 */
	/**
	 * @return true if this node or its descendants can contain libraries
	 */
	@Override
	public boolean isLibraryContainer() {
		return false;
	}

	/**
	 * True if is a compiler LibraryMember and not an implied node. False for version 1.5 contextual facets.
	 */
	public boolean isLibraryMember() {
		if (this instanceof ImpliedNode)
			return false;
		if (this instanceof ContextualFacetNode && !OTM16Upgrade.otm16Enabled)
			return false;
		return getTLModelObject() instanceof LibraryMember;
	};

	/**
	 * @return - true if the component node represent a local anonymous type.
	 */
	public boolean isLocal() {
		return false;
	}

	public boolean isMergeSupported() {
		return false;
	}

	/**
	 * Do NOT use instanceof TypeProvider because implied node is a type provider!
	 */
	@Override
	public boolean isNamedEntity() {
		if (this instanceof ImpliedNode)
			return false;
		return getTLModelObject() instanceof NamedEntity;
	}

	/**
	 * @return true if a named top level object (business, core, enum, vwa, simple, extension point facet)
	 */
	public boolean isNamedType() {
		return false;
	}

	/**
	 * Fast method to determine if this node should be displayed in navigation views
	 * 
	 * @return true if this node should be displayed in navigator view tree with no filters
	 * @see {@link org.opentravel.schemas.node.Node_NavChildren_Tests#hasTests() }
	 */
	public boolean isNavChild(boolean deep) {
		return this instanceof LibraryMemberInterface;
	}

	@Override
	public boolean isNavigation() {
		return false;
	}

	/**
	 * 
	 * @return true <b>only</b> if owning components is in chain and new to the chain
	 */
	public boolean isNewToChain() {
		assert getOwningComponent() != null;
		return !getOwningComponent().isVersioned();
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

	@Override
	public boolean isRenameable() {
		return false;
	}

	/**
	 * @return true if this object can be assigned as a simple type (not by reference).
	 */
	public boolean isSimpleAssignable() {
		return modelObject.isSimpleAssignable();
	}

	/**
	 * @return true if this is a simple object type.
	 * 
	 *         Use instanceof SimpleComponentNode
	 */
	// TODO - after testing proves the xsd node override never asserts
	@Deprecated
	public boolean isSimpleType() {
		return false;
	}

	public boolean isTLLibrary() {
		return false;
	}

	/**
	 * @return true if tl model object is a library member
	 */
	public boolean isTLLibraryMember() {
		return getTLModelObject() instanceof LibraryMember;
	}

	/**
	 * @return true if this node could be assigned a type but is unassigned.
	 */
	public boolean isUnAssigned() {
		if (!(this instanceof TypeUser))
			return false;
		if (getType() instanceof ImpliedNode)
			if (((ImpliedNode) getType()).getImpliedType() == ImpliedNodeType.UnassignedType)
				return true;
		return false;
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
		if (n instanceof FacetNode && !(n instanceof CustomFacetNode) && !(n instanceof QueryFacetNode)
				&& !(n instanceof RoleFacetNode)) {
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

	/**
	 * Generate validation results starting with this node.
	 * 
	 * @return true if no errors.
	 */
	public boolean isValid() {
		if (isBuiltIn())
			return true; // skip built in libraries and their content
		ValidationFindings findings = validate();
		// if (findings.count(FindingType.ERROR) > 0)
		// LOGGER.debug("Validation found errors.");
		return findings != null && findings.count(FindingType.ERROR) == 0 ? true : false;
	}

	/**
	 * Can this object contain properties of the specified type? Only FacetNodes can be containers.
	 * 
	 * @param type
	 * @return
	 */
	public boolean isValidParentOf(PropertyNodeType type) {
		return false;
	}

	/**
	 * @return true if this object is a later version of another object. True if has same base ns and name as the object
	 *         it extends.
	 */
	public boolean isVersioned() {
		// return (this instanceof ExtensionOwner) ? getExtendsTypeName().equals(getName()) : false;
		ExtensionHandler handler = null;
		if (this instanceof ExtensionOwner)
			handler = ((ExtensionOwner) this).getExtensionHandler();
		return handler != null ? handler.isVersioned() : false;
	}

	public boolean isVWASimpleAssignable() {
		return getTLModelObject() instanceof TLAttributeType;
	}

	/**
	 * @return true if in an XSD type and element assignable.
	 */
	public boolean isXsdElementAssignable() {
		return xsdType ? xsdNode.isElementAssignable() : false;
	}

	public boolean isXSDSchema() {
		return false;
	}

	/**
	 * @return true if the node represents a non-imported XSD type. Note - the library is a TLLibrary, but the type is
	 *         not editable.
	 */
	public boolean isXsdType() {
		return xsdType;
	}

	/**
	 * Add the <i>child</i> node parameter to <i>this</i> node. Sets parentNode link of child.
	 * 
	 * @param child
	 *            - node to be added
	 * @return false if child could not be linked.
	 */
	public boolean linkChild(final Node child) {
		return linkChild(child, -1);
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
		if (child == null)
			return false;

		// Exit if the child or its containing version node is already in children list
		if (getChildren().contains(child))
			return false;
		if (child.getParent() instanceof VersionNode)
			if (getChildren().contains(child.getParent()))
				return false;

		if (index < 0 || index > getChildren().size())
			getChildren().add(child);
		else
			getChildren().add(index, child);

		child.setParent(this);
		if (!(child instanceof LibraryNode))
			child.setLibrary(getLibrary());

		// LOGGER.debug("Linked child " + child + " to parent " + this);
		return true;
	}

	/**
	 * 
	 * @param libraryNode
	 */
	public void linkLibrary(LibraryNode libraryNode) {
		LOGGER.error("addLibrary not Implmented for this class." + this.getClass());
	}

	/** ******************** Library access methods ******************/

	/**
	 * Merge source properties into <i>this</i> node. Does not change source node.
	 * 
	 * @param target
	 */
	public void merge(Node source) {
	}

	/**
	 * Change all context users to use targetId. Iterates on all children. If the context would be duplicated, it is
	 * added as an implementors documentation item.
	 * 
	 * @param targetId
	 *            - replace with this contextId
	 */
	public void mergeContext(String targetId) {
		// if (modelObject == null) {
		// LOGGER.error("Model Object is null.");
		// return;
		// }
		if (this instanceof PropertyNode && ((PropertyNode) this).getEquivalentHandler() != null)
			((PropertyNode) this).getEquivalentHandler().fix(targetId);
		if (this instanceof PropertyNode && ((PropertyNode) this).getExampleHandler() != null)
			((PropertyNode) this).getExampleHandler().fix(targetId);

		if (this instanceof ContextualFacetNode)
			((ContextualFacetNode) this).setContext(targetId);

		if (getDocumentation() != null && getDocumentation().getOtherDocs() != null) {
			// Avoid concurrent modification
			List<TLAdditionalDocumentationItem> odList = new ArrayList<TLAdditionalDocumentationItem>(
					getDocumentation().getOtherDocs());
			// If the target exists, then use it. All others get converted to implementation documentation.
			TLAdditionalDocumentationItem targetOD = getDocumentation().getOtherDoc(targetId);
			for (TLAdditionalDocumentationItem od : odList) {
				if (targetOD == null)
					od.setContext(targetId);
				else
					addImplementer("Other doc: " + od.getContext() + " = " + od.getText());
			}
		}

		// Iterate through all children
		for (Node n : getChildren())
			n.mergeContext(targetId);
	}

	/**
	 * calls LibraryNode.removeMember()
	 * 
	 * @see LibraryNode
	 */
	@Override
	public void removeFromLibrary() {
		if (getLibrary() != null)
			getLibrary().removeMember(this);
		// LOGGER.debug("Removed " + this + " from " + getLibrary().getNameWithPrefix());
	}

	/**
	 * Replace all type assignments (base and assigned type) to this node with assignments to passed node. For every
	 * assignable descendant of sourceNode, find where the corresponding sourceNode children are used and change them as
	 * well. See {@link #replaceWith(Node)}.
	 * 
	 * @param this - replace assignments to this node (sourceNode)
	 * @param replacement
	 *            - use replacement node instead of this node
	 * @param scope
	 *            (optional) - scope of the search (typically library or Node.getModelNode)
	 */
	public void replaceTypesWith(Node replacement, INode scope) {
		if (replacement == null)
			return;

		LibraryNode libScope = null;
		if (scope != null)
			libScope = scope.getLibrary();
		// throw new IllegalStateException("Not Implemented Yet -  type replacement.");

		if (this instanceof TypeProvider && replacement instanceof TypeProvider)
			((TypeProvider) this).getWhereAssignedHandler().replaceAll((TypeProvider) replacement, libScope);

		// If this has been extended, replace where extended
		getWhereExtendedHandler().replace(replacement, libScope);
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

		// Hold onto library node because it will be cleared by removeMember.
		if (getLibrary() == null) {
			LOGGER.error("The node being replaced is not in a library. " + this);
			return;
		}

		if (!getLibrary().contains(replacement))
			getLibrary().addMember(replacement); // does nothing in swap because replacement lib is already set to
													// this.getLibrary()

		replaceTypesWith(replacement, null);

		// 9/28/2015 dmh - moved to end to preserve linkages. Listeners will remove the whereUsed links.
		getLibrary().removeMember(this);
	}

	public void resetInheritedChildren() {
		// No action required - override as required in sub-classes
	}

	public void setDescription(final String string) {
		if (modelObject != null)
			modelObject.setDescriptionDoc(string);
	}

	public void setDevelopers(String doc, int index) {
		if (modelObject != null && isEditable())
			modelObject.setDeveloperDoc(doc, index);
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
	 * Walk the nodes under <i>this</i> node and set the library value to this library node.
	 * 
	 */
	public void setKidsLibrary() {
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
		// if (!(this instanceof LibraryChainNode))
		setKidsLibrary();
	}

	public void setMoreInfo(String info, int index) {
		if (modelObject != null && isEditable())
			modelObject.setMoreInfo(info, index);
	}

	/**
	 * Set the name in the node and underlying TL model object for <i>this</i> node. Complex types propagate name change
	 * type users.
	 * 
	 * NOTE - not safe to use in constructors until a TL model object is instantiated.
	 * 
	 * @param name
	 *            - new name
	 */
	public void setName(final String name) {
		// Do Nothing
	}

	/**
	 * Simple parent setter. Set to null if it is the root node.
	 */
	public void setParent(final Node n) {
		parent = n;
	}

	//
	public void setReferenceLink(String link, int index) {
		if (modelObject != null && isEditable())
			modelObject.setReferenceDoc(link, index);
	}

	/**
	 * @param versionNode
	 *            to represent this node in a specific library in a chain.
	 */
	public void setVersionNode(VersionNode version) {
		this.versionNode = version;
	}

	/**
	 * @param Set
	 *            to true if the node represents an non-imported XSD type.
	 */
	public void setXsdType(boolean xsdType) {
		this.xsdType = xsdType;
	}

	public void sort() {
	}

	/**
	 * Swap the replacement for this node. This node is replaced via {@link #replaceWith()} and removed from parent.
	 * Replacement is added to parent.
	 * 
	 * Note: this node's library is null on return. It can not be deleted.
	 * 
	 * @param replacement
	 *            - should be in library, named and with all properties.
	 * 
	 */
	public void swap(Node replacement) {
		assert (replacement != null) : "Null replacement node.";
		assert (getLibrary() != null) : "Null library.";
		assert (parent != null) : "Null parent";
		assert (isTLLibraryMember()) : "TL Object is not library member.";
		assert (replacement.isTLLibraryMember()) : "TL Object is not library member.";
		assert (this instanceof LibraryMemberInterface);

		final Node thisParent = parent;

		// Add replacement to the parent if not already there.
		parent.linkChild(replacement); // ignored if already linked, skip family processing

		// Fail if in the list more than once.
		assert (replacement.getParent().getChildren().indexOf(replacement) == replacement.getParent().getChildren()
				.lastIndexOf(replacement));
		// Force the replacement model object to be in the same library as this node.
		AbstractLibrary thisTlLibrary = ((LibraryMember) this.getTLModelObject()).getOwningLibrary();
		AbstractLibrary replacementTlLibrary = ((LibraryMember) replacement.getTLModelObject()).getOwningLibrary();
		if (thisTlLibrary != replacementTlLibrary) {
			// LOGGER.debug("swap(): replacement TL object was not in same library.");
			replacement.getModelObject().addToLibrary(((LibraryMember) this.getTLModelObject()).getOwningLibrary());
		}

		replacement.setLibrary(this.getLibrary());
		replaceWith(replacement); // Removes this from library

		// Post-checks
		assert (this.library == null) : "This library should be null.";
		assert (this.parent == null) : "This parent should be null.";
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Unlink this node from its parent. If this is a versioned object, it unlinks the verionNode as well. Does not
	 * change the TL model. Does not delete the node; caller is responsible to free resources.
	 * 
	 */
	public void unlinkNode() {
		if (parent == null) {
			if (!getChildren().isEmpty())
				LOGGER.error("unlinkNode ERROR - null parent or no children. EXIT");
			return;
		}
		VersionNode vn = null;
		if (parent instanceof VersionNode)
			vn = (VersionNode) parent;
		if (!parent.children.remove(this))
			LOGGER.debug("unlinkNode Error - child " + getName() + " was not in parent's " + parent.getName()
					+ " child list.");
		parent = null;
		// recurse to remove version parent as well.
		if (vn != null) {
			// unlink from the chain aggregate node
			if (this instanceof ComponentNode)
				getChain().removeAggregate((ComponentNode) this);
			// unlink the version node it self
			if (vn.getParent() != null)
				vn.unlinkNode();
		}
	}

	/**
	 * Use the compiler to validate a node.
	 */
	public ValidationFindings validate() {
		// Only do deep dependencies validation on libraries.
		if (this.isDeleted())
			return null;
		ValidationFindings findings = TLModelCompileValidator.validateModelElement(this.getTLModelObject(),
				this instanceof LibraryNode);
		// for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
		// LOGGER.debug("Finding: " + f);

		return findings;
	}

	/**
	 * Visit all descendants that are instances of ExtensionOwner
	 * 
	 * @param visitor
	 *            to run on each ExtensionOwner
	 */
	public void visitAllExtensionOwners(NodeVisitor visitor) {
		for (Node c : getChildren()) {
			c.visitAllExtensionOwners(visitor);
		}
		if (this instanceof ExtensionOwner)
			visitor.visit(this);
	}

	// Depth First node traversal
	@Override
	public void visitAllNodes(NodeVisitor visitor) {
		ArrayList<Node> kids = new ArrayList<Node>(getChildren());
		for (Node child : kids)
			child.visitAllNodes(visitor);
		visitor.visit(this);
	}

	@Override
	public void visitAllTypeUsers(NodeVisitor visitor) {
		for (Node c : getChildren()) {
			c.visitAllTypeUsers(visitor);
		}
		if (this instanceof TypeUser) {
			visitor.visit(this);
		}
	}

	@Override
	public void visitChildren(NodeVisitor visitor) {
		for (Node c : getChildren())
			visitor.visit(c);
		visitor.visit(this);
	}

	// returns owning navNode if it is a component node.
	protected Node getOwningNavNode() {
		Node srcParent = null;
		if (this instanceof ComponentNode)
			srcParent = ((ComponentNode) this).getOwningNavNode();
		return srcParent;
	}

	/**
	 * Get the version from the TLLibrary
	 * 
	 * @return string of the version
	 */
	protected String getTlVersion() {
		String version = "";
		if (getTLModelObject() instanceof LibraryElement) {
			LibraryElement le = (NamedEntity) getTLModelObject();
			if (le != null && le.getOwningLibrary() != null)
				version = le.getOwningLibrary().getVersion();
		}
		return version;
	}

	/**
	 * @return true if name and namespace are equal to other node
	 */
	protected boolean nameEquals(final INode other) {
		if (this == other)
			return true;

		if (other == null)
			return false;

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
	 * Only remove the node from this children list. Not family or version aware. Does not delete the node.
	 */
	// Note - used in model creation to unlink nodes to add them to a family nav node.
	protected void remove(final Node n) {
		if (n == null)
			return;
		if (!children.remove(n)) {
			LOGGER.warn("Attempting to delete a child " + n + " that is not in children list of parent " + this);
		}
	}

	private List<TLContext> getCtxList() {
		ArrayList<TLContext> list = new ArrayList<TLContext>();
		if (getModelObject() != null) {
			List<TLContext> cList = getModelObject().getContexts();
			if (cList.size() > 0) {
				list.addAll(cList);
			}
		}
		for (Node child : getChildren()) {
			list.addAll(child.getCtxList());
		}
		// LOGGER.debug("Found "+list.size()+" contexts in "+this.getName());
		return list;
	}

	// the public method uses this then removes the original object from the list.
	private HashSet<Node> getDescendants_AssignedTypes(boolean currentLibraryOnly, HashSet<Node> foundTypes) {
		Node assignedType = null;
		for (TypeUser n : getDescendants_TypeUsers()) {
			if (n.getAssignedType() != null) {
				assignedType = ((Node) n.getAssignedType()).getOwningComponent();
				if (!currentLibraryOnly || (assignedType.getLibrary() == getLibrary()))
					if (foundTypes.add(assignedType)) {
						foundTypes.addAll(assignedType.getDescendants_AssignedTypes(currentLibraryOnly, foundTypes));
					}
			}
		}
		return foundTypes;
	}
}
