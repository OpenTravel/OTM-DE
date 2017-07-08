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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemas.modelObject.EmptyMO;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ComponentNode class handles nodes that represent model objects. It is overridden for most types and properties.
 * 
 * This does not implement type provider -- sub-classes implement TypeProvider
 * 
 * @author Dave Hollander
 * 
 */

public abstract class ComponentNode extends Node {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	/**
	 * The list of inherited children for this node. Inherited nodes are not assigned a type class. If they were the
	 * where-used count would be wrong.
	 */
	protected List<Node> inheritedChildren;

	/**
	 * Actual node where the inherited child is declared.
	 */
	protected Node inheritsFrom = null;

	/**
	 * Indicates whether the underlying model object was declared or inherited by its parent.
	 */
	protected boolean inherited = false;
	private boolean childrenInitialized = false;
	public ConstraintHandler constraintHandler = null;

	/**
	 * ComponentNode constructor.
	 * 
	 * @see ComponentNode(library), ComponentNode(JaxB element), ComponentNode (name, getNamespace)
	 */
	public ComponentNode() {
		super();
		setListner();
	}

	/**
	 * Top Level component node construction. Create model object, set name and description, set TL library member
	 * 
	 * @param tlModelObject
	 *            - the model object for the node
	 */
	public ComponentNode(final TLLibraryMember tlModelObject) {
		//
		super(tlModelObject);
		// sub-types must render their children into nodes. See addMOChildren();
		// LOGGER.debug("New component node for "+tlModelObject.getLocalName());
		if (modelObject instanceof FacetMO)
			throw new IllegalStateException("Unexpected model object in cn construction.");

		setListner();
	}

	/**
	 * Create a component node for a <b>non</b>-top-level model element. Only creates node and sets name and
	 * description.
	 * 
	 * @param obj
	 */
	public ComponentNode(final TLModelElement obj) {
		super(obj);
		setListner();
	}

	public void addProperty(final Node property) {
		if (property == null)
			return;
		addProperty(property, -1);
	}

	/**
	 * Add a new property to <i>this</i>facet.
	 * 
	 * @param property
	 *            node representing a TLProperty, TLIndicator, etc
	 * @param index
	 *            where to add the property in the child list.
	 */
	// FIXME - move/remove/refactor - then remove addToTLParent() in MO
	public void addProperty(final Node property, int index) {
		if (!(this instanceof PropertyOwnerInterface) && !(this instanceof Enumeration)) {
			// LOGGER.error("ERROR - Exit - Tried to add property to a non-FacetNode or enumeration " + this);
			return;
		}
		if (this instanceof SimpleFacetNode) {
			if (property instanceof PropertyNode)
				((TypeUser) getChildren().get(0)).setAssignedType(((TypeUser) property).getAssignedType());
			else
				((TypeUser) getChildren().get(0)).setAssignedType((TypeProvider) property);
		} else {
			property.setParent(this);
			if (index >= 0)
				linkChild(property, index);
			else
				linkChild(property);
			final ModelObject<?> propMO = property.getModelObject();
			final ModelObject<?> mo = getModelObject();
			if (propMO != null && mo != null) {
				// index is < 0 when adding to end, and is hard to calculate the last index.
				if (index < 0) {
					propMO.addToTLParent(mo);
				} else {
					propMO.addToTLParent(mo, index);
				}
			}
		}
	}

	/**
	 * 
	 * @param SubType
	 *            to change to
	 * @return - new object created by changing this object
	 */
	public ComponentNode changeObject(SubType st) {
		if (getLibrary() == null)
			return null;
		if (this instanceof FacetNode)
			return ((ComponentNode) getParent()).changeObject(st);

		// TODO - add choice object type
		// ToDO - add custom facet type
		ComponentNode newNode = null;
		switch (st) {
		case BUSINESS_OBJECT:
			if (this instanceof BusinessObjectNode)
				return this;
			if (this instanceof CoreObjectNode)
				newNode = new BusinessObjectNode((CoreObjectNode) this);
			else if (this instanceof VWA_Node)
				newNode = new BusinessObjectNode(((VWA_Node) this));
			break;
		case CORE_OBJECT:
			if (this instanceof CoreObjectNode)
				return this;
			if (this instanceof BusinessObjectNode)
				newNode = new CoreObjectNode((BusinessObjectNode) this);
			else if (this instanceof VWA_Node)
				newNode = new CoreObjectNode((VWA_Node) this);
			break;
		case VALUE_WITH_ATTRS:
			if (this instanceof VWA_Node)
				return this;
			if (this instanceof BusinessObjectNode)
				newNode = new VWA_Node((BusinessObjectNode) this);
			else if (this instanceof CoreObjectNode)
				newNode = new VWA_Node((CoreObjectNode) this);
			break;
		default:
			throw new IllegalArgumentException("Change to SubType: " + st.toString() + " is not supporeted.");
		}

		if (newNode != null)
			swap(newNode);
		return newNode;
	}

	/**
	 * Create aliases for all complex properties of this object that have the same type. Used in importing from XSD.
	 */
	public void createAliasesForProperties() {

		Map<ComponentNode, List<Node>> typeMap = new HashMap<ComponentNode, List<Node>>(getDescendants().size());
		List<Node> list;
		for (Node d : getDescendants()) {
			if (d.getType() == null)
				continue;
			if (d.getType() instanceof CoreObjectNode || d.getType() instanceof BusinessObjectNode) {
				if (typeMap.containsKey(d.getType()))
					list = typeMap.get(d.getType());
				else {
					list = new ArrayList<Node>();
					typeMap.put((ComponentNode) d.getType(), list);
				}
				list.add(d);
			}
		}

		for (ComponentNode type : typeMap.keySet()) {
			list = typeMap.get(type);
			if (list == null)
				continue;
			if (list.size() > 1) {
				for (Node property : list) {
					String aliasName = property.getName() + "_" + type.getName();
					((TypeUser) property).setAssignedType((TypeProvider) new AliasNode(type, aliasName));
					property.setName(aliasName);
				}
			}
		}
	}

	/**
	 * Create a new object in a patch version library. Creates an empty extension point facet. Adds the new node to the
	 * owner's chain head library.
	 * 
	 * @return new extension point facet, of this one if it was an extension point
	 */
	public ComponentNode createPatchVersionComponent() {
		if (this.getOwningComponent() instanceof ExtensionPointNode)
			return (ComponentNode) this.getOwningComponent();

		ExtensionPointNode newNode = null;
		newNode = new ExtensionPointNode(new TLExtensionPointFacet());

		if (this instanceof FacetNode)
			newNode.setExtension(this);
		else if (this instanceof PropertyNode)
			newNode.setExtension(getParent());
		else if (this instanceof CoreObjectNode)
			newNode.setExtension((Node) getFacet_Summary());
		else if (this instanceof BusinessObjectNode)
			newNode.setExtension((Node) getFacet_Summary());
		// else
		// LOGGER.error("Can't add a property to this: " + this);
		// If there already is an EP, then return that.
		LibraryChainNode chain = getChain();
		Node existing = ((Node) chain.getComplexAggregate()).findNodeByName(newNode.getName());
		if (existing != null && (existing instanceof ExtensionPointNode))
			newNode = (ExtensionPointNode) existing;
		else
			chain.getHead().addMember(newNode); // needs extends type to know name for family.

		return newNode;
	}

	/*
	 * ComponentNode Utilities
	 */

	public INode createProperty(final Node dropTarget) {
		return null;
	}

	// overridden where a simple type exists.
	@Override
	public Node getAssignable() {
		return getSimpleProperty();
	}

	/**
	 * @return the assigned namespace prefix from the model object.
	 */
	public String getAssignedPrefix() {
		return getModelObject().getAssignedPrefix();
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> kids = new ArrayList<Node>();
		for (Node child : getChildren()) {
			if (child instanceof TypeUser)
				kids.add(child);
		}
		return kids;
	}

	// TOOD - let the view code actually use the handler
	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public PropertyOwnerInterface getFacet_Default() {
		// should be overridden
		for (final INode n : getChildren()) {
			if (((Node) n).isDefaultFacet()) {
				return (PropertyOwnerInterface) n;
			}
		}
		return null;
	}

	/**
	 * @return - Node for ID facet if it exists, null otherwise.
	 */
	public PropertyOwnerNode getFacet_Detail() {
		return (PropertyOwnerNode) getFacetOfType(TLFacetType.DETAIL);
	}

	// Override in propeprtyNode
	public String getEquivalent(final String context) {
		return "";
	}

	// Override in propeprtyNode
	public String getExample(final String context) {
		return "";
	}

	public TLFacetType getFacetType() {
		return null;
	}

	/**
	 * @return - Node for ID facet if it exists, null otherwise.
	 */
	public PropertyOwnerNode getIDFacet() {
		return (PropertyOwnerNode) getFacetOfType(TLFacetType.ID);
	}

	@Override
	public List<Node> getInheritedChildren() {
		synchronized (this) {
			if (inheritedChildren != null)
				inheritedChildren.clear();
			// Do not keep a local copy until adding to the base forces update of all extensions.
			initInheritedChildren();
		}
		if (inheritedChildren == null)
			inheritedChildren = Collections.emptyList();
		return inheritedChildren;
	}

	/**
	 * Actual node where the inherited child is declared.
	 * 
	 * @return type inherited from or null if no inheritance. Note, Open Enumerations have an inherited attribute but
	 *         null for inherits from.
	 */
	public Node getInheritsFrom() {
		return inheritsFrom;
	}

	@Override
	public String getName() {
		return "";
	}

	/**
	 * returns owning navNode if it is a component node. Family aware - if in a family it returns the family parent.
	 * Null otherwise.
	 */
	@Override
	public Node getOwningNavNode() {
		Node owner = this.getParent();
		if (owner instanceof VersionNode)
			owner = owner.getParent();
		return owner;
	}

	/**
	 * Property Roles are displayed in the facet table and describe what role the item can play in constructing
	 * vocabularies.
	 * 
	 * @return - string from enumerated list of roles, or empty string if not property.
	 */
	public String getPropertyRole() {
		return "";
	}

	private ComponentNode getFacetOfType(final TLFacetType facetType) {
		for (final INode n : getChildren()) {
			if (n instanceof FacetNode) {
				final ComponentNode facet = (ComponentNode) n;
				final TLFacetType ft = facet.getFacetType();
				if (ft != null && ft.equals(facetType)) {
					return facet;
				}
			}
		}
		return null;
	}

	public ComponentNode getFacet_Simple() {
		return getFacetOfType(TLFacetType.SIMPLE);
	}

	// overridden where a simple property exists.
	public Node getSimpleProperty() {
		return null;
	}

	/**
	 * @return - Node for SUMMARY facet if it exists, null otherwise.
	 */
	public PropertyOwnerNode getFacet_Summary() {
		return (PropertyOwnerNode) getFacetOfType(TLFacetType.SUMMARY);
	}

	/** Lazy initialization of the inherited children list. */
	public void initInheritedChildren() {
		List<?> inheritedMOChildren = modelObject.getInheritedChildren();
		if ((inheritedMOChildren == null) || inheritedMOChildren.isEmpty()) {
			inheritedChildren = Collections.emptyList();
		} else {
			for (final Object obj : inheritedMOChildren) {
				// null parent allows us to control linkage.
				ComponentNode nn = NodeFactory.newMember(null, obj);
				if (nn != null) {
					linkInheritedChild(nn);
					nn.addMOChildren();
				}
			}
		}
	}

	@Override
	public boolean isInherited() {
		return inherited;
	}

	// /**
	// * Returns true if the 'otherNode's' library meets both of the following conditions:
	// * <ul>
	// * <li>The other library is assigned to the same version scheme and base namespace as this one.</li>
	// * <li>The version of the other library is considered to be later than this library's version according to the
	// * version scheme.</li>
	// * </ul>
	// *
	// * @see org.opentravel.schemacompiler.model.AbstractLibrary.isLaterVersion
	// * @param n
	// * node whose library to compare to this nodes library
	// * @return boolean
	// */
	// public boolean isLaterVersion(Node n) {
	// return (this.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib()));
	// }

	@Override
	public boolean isLocal() {
		return local;
	}

	public boolean isMandatory() {
		return false;
	}

	public boolean isMissingAssignedType() {
		if (modelObject.getTLType() == null)
			return true;
		return false;
	}

	// @Override
	// public boolean isNamedType() {
	// return false;
	// }

	/**
	 * Test this node against those in the parentNode to and return true if the name is unique within its parent and
	 * inherited parent. NOTE: does not check other facets!
	 * 
	 * @return
	 */
	// FIXME - why here and in Node? Why different?
	public boolean isUnique() {
		// LOGGER.debug("ComponentNode:isUnique() - test "+getNamespace+":"+name);
		List<Node> siblings = new ArrayList<Node>(getParent().getChildren());
		if (getParent().getInheritedChildren() != null)
			siblings.addAll(0, getParent().getInheritedChildren());
		int occurrence = 0; // look for second occurrence since this list is not a live list.

		for (final Node n : siblings) {
			if (n.getName().equals(getName()))
				if (occurrence++ > 0)
					return false;
		}
		return true;
	}

	@Override
	public boolean isXsdType() {
		// Local anonymous types may not have xsdType set.
		return local ? true : xsdType;
	}

	/**
	 * Adds passed child node to inheritedChildren array. Sets where inherited from using the listener on the child's
	 * TLObject. Set parent, library and inherited flag.
	 * 
	 * @param child
	 * @return
	 */
	public boolean linkInheritedChild(final ComponentNode child) {
		if (child == null) {
			return false;
		}
		if ((inheritedChildren == null) || inheritedChildren.isEmpty()) {
			inheritedChildren = new ArrayList<Node>();
		}
		child.inheritsFrom = Node.GetNode(child.getTLModelObject());
		if (child instanceof PropertyNode && child.inheritsFrom == child) {
			if (!(this instanceof EnumerationOpenNode))
				LOGGER.debug("ERROR - child inherits from itself: " + child);
			child.inheritsFrom = null;
			child.inherited = false;
			// assert child.inheritsFrom != child; // Only prop nodes?
		} else {
			inheritedChildren.add(child);
			child.setParent(this);
			child.inherited = true;
			child.setLibrary(getLibrary());
		}
		// LOGGER.debug("Linked inherited child " + child + " to parent " + this);
		return true;
	}

	public void removeProperty(final Node property) {
		// throw new IllegalStateException("Remove property in component node should never run.");
	}

	@Override
	public void resetInheritedChildren() {
		// Recursively reset the inherited children of all child nodes
		for (final Node n : getChildren()) {
			n.resetInheritedChildren();
		}

		// The list of inherited children for this node will be re-populated
		// with the next call to getInheritedChildren().
		inheritedChildren = null;
	}

	public void setContext() {
		// Override where needed
	}

	/**
	 * @return true if the documentation could be set, false otherwise
	 */
	public boolean setDocumentation(TLDocumentation documentation) {
		if (getTLModelObject() instanceof TLDocumentationOwner)
			getModelObject().setDocumentation(documentation);
		else
			return false;
		return true;
	}

	/**
	 * @param local
	 *            the local to set
	 */
	public void setLocal(boolean local) {
		this.local = local;
		this.xsdType = local;
	}

	public void setMandatory(final boolean selection) {
		// Override where supported
	}

	/** TYPE Interfaces **/

	@Override
	public void sort() {
		getModelObject().sort();
	}

	/**
	 * Create nodes for all of the children of the model object in this node. Also creates nodes for GUI only synthetic
	 * children such as roles and aliases.
	 * 
	 */
	protected void addMOChildren() {
		if (childrenInitialized || (modelObject.getChildren() == null)) {
			return; // All Done.
		}
		childrenInitialized = true;

		// Build list of direct children of the model object
		ComponentNode nn = null;
		for (final Object obj : modelObject.getChildren()) {
			nn = (ComponentNode) GetNode((ModelElement) obj);
			if (nn != null) {
				if (nn instanceof ContextualFacetNode && ((ContextualFacetNode) nn).isNamedEntity())
					new ContributedFacetNode((TLContextualFacet) obj, (ContextualFacetOwnerInterface) this);
				else
					// Just link it in
					linkChild(nn);
			} else
				nn = NodeFactory.newMember(this, obj);
			if (nn != null)
				nn.addMOChildren();
		}
	}

	protected LibraryMember createMinorTLVersion(VersionedObjectInterface node) {
		MinorVersionHelper helper = new MinorVersionHelper();
		Versioned v = null;
		TLLibrary tlLib = getChain().getHead().getTLLibrary();
		try {
			v = helper.createNewMinorVersion((Versioned) getTLModelObject(), tlLib);
		} catch (VersionSchemeException | ValidationException e) {
			LOGGER.debug("Exception creating minor TL version in: " + tlLib.getPrefix() + ":" + tlLib.getName() + " "
					+ e.getLocalizedMessage());
			return null;
		}
		return (LibraryMember) v;
	}

	// newNode is the node constructed from the TL object returned from createMinorTLVersion()
	protected ComponentNode createMinorVersionComponent(ComponentNode newNode) {
		assert !getOwningComponent().isInHead();
		// assert newNode instanceof ExtensionOwner;
		// TODO - should resource be an extension owner? Is it versioned that way?
		// 3/2/2017 - resources are not versioned via gui
		if (newNode.getModelObject() instanceof EmptyMO) {
			LOGGER.debug("Empty minor version created");
			return null;
		}
		Node owner = this.getOwningComponent();
		owner.getLibrary().checkExtension(owner);

		// exit if it is already in the head of the chain.
		if (owner.isInHead())
			return null;

		if (newNode instanceof ExtensionOwner)
			((ExtensionOwner) newNode).setExtension(owner);
		if (newNode.getName() == null || newNode.getName().isEmpty())
			newNode.setName(owner.getName()); // Some of the version handlers do not set name

		// Add the new node to the library at the head of the chain
		LibraryChainNode chain = owner.getChain();
		chain.getHead().addMember(newNode);
		chain.getComplexAggregate().getChildren().remove(owner);

		owner.getLibrary().checkExtension(owner);
		// TODO - should old properties be set to inherited?
		return newNode;
	}

	private void setListner() {
		ListenerFactory.setListner(this);
	}

}
