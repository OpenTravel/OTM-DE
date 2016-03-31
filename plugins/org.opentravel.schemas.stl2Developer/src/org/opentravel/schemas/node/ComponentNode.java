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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemas.modelObject.EmptyMO;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * The ComponentNode class handles nodes that represent model objects. It is overridden for most types and properties.
 * 
 * This does not implement type provider -- sub-classes implement TypeProvider
 * 
 * @author Dave Hollander
 * 
 */

public class ComponentNode extends Node {
	// private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

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
		family = "";
		setListner();
	}

	/**
	 * Top Level component node construction. Create model object, set name and description, set TL library member
	 * 
	 * @param tlModelObject
	 *            - the model object for the node
	 */
	public ComponentNode(final LibraryMember tlModelObject) {
		//
		super(tlModelObject);
		// sub-types must render their children into nodes. See addMOChildren();
		// LOGGER.debug("New component node for "+tlModelObject.getLocalName());
		if (modelObject instanceof FacetMO) {
			throw new IllegalStateException("Unexpected model object in cn construction.");
			// addMOChildren();
		}
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
		if (obj instanceof TLFacet)
			addMOChildren();
		setListner();
	}

	private void setListner() {
		ListenerFactory.setListner(this);
	}

	// @Override
	// public Node getTypeNode() {
	// return getTypeClass().getTypeNode();
	// }

	@Override
	public boolean isNamedType() {
		return false;
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
		for (final Object obj : modelObject.getChildren()) {
			final ComponentNode nn = NodeFactory.newComponentMember(this, obj);
			if (nn != null)
				nn.addMOChildren();
		}
	}

	/**
	 * Navigation Component nodes are: complex, facet, service, simpleAssignable
	 */
	@Override
	public List<Node> getNavChildren() {
		final List<Node> ret = new ArrayList<Node>();
		for (final Node n : getChildren()) {
			if (n.isNavChild()) {
				ret.add(n);
			}
		}
		return ret;
	}

	@Override
	public boolean hasNavChildren() {
		for (final Node n : getChildren()) {
			if (n.isNavChild()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasNavChildrenWithProperties() {
		return !getChildren().isEmpty();
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

	/**
	 * returns owning navNode if it is a component node. Family aware - if in a family it returns the family parent.
	 * Null otherwise.
	 */
	@Override
	protected Node getOwningNavNode() {
		Node owner = this.getParent();
		if (owner instanceof VersionNode)
			owner = owner.getParent();
		if (owner instanceof FamilyNode)
			owner = owner.getParent();
		return owner;
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

	/** Lazy initialization of the inherited children list. */
	public void initInheritedChildren() {
		List<?> inheritedMOChildren = modelObject.getInheritedChildren();
		if ((inheritedMOChildren == null) || inheritedMOChildren.isEmpty()) {
			inheritedChildren = Collections.emptyList();
		} else {
			for (final Object obj : inheritedMOChildren) {
				// null parent allows us to control linkage.
				ComponentNode nn = NodeFactory.newComponentMember(null, obj);
				if (nn != null) {
					linkInheritedChild(nn);
					nn.addMOChildren();
				}
			}
		}
	}

	public boolean linkInheritedChild(final ComponentNode child) {
		if (child == null) {
			return false;
		}
		if ((inheritedChildren == null) || inheritedChildren.isEmpty()) {
			inheritedChildren = new ArrayList<Node>();
		}
		child.inheritsFrom = Node.GetNode(child.getTLModelObject());
		inheritedChildren.add(child);
		child.setParent(this);
		child.inherited = true;

		if (!child.isLibrary()) {
			child.setLibrary(getLibrary());
		}
		// LOGGER.debug("Linked inherited child " + child + " to parent " +
		// this);
		return true;
	}

	/*
	 * ComponentNode Utilities
	 */

	public Boolean isMandatory() {
		return modelObject.isMandatory();
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

	// TOOD - let the view code actually use the handler
	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public String getPattern() {
		return constraintHandler == null ? "" : constraintHandler.getPattern();
	}

	public int getMaxLen() {
		return constraintHandler == null ? -1 : constraintHandler.getMaxLen();
	}

	public int getMinLen() {
		return constraintHandler == null ? -1 : constraintHandler.getMinLen();
	}

	public int getFractionDigits() {
		return constraintHandler == null ? -1 : constraintHandler.getFractionDigits();
	}

	public int getTotalDigits() {
		return constraintHandler == null ? -1 : constraintHandler.getTotalDigits();
	}

	public String getMinInclusive() {
		return constraintHandler == null ? null : constraintHandler.getMinInclusive();
	}

	public String getMaxInclusive() {
		return constraintHandler == null ? null : constraintHandler.getMaxInclusive();
	}

	public String getMinExclusive() {
		return constraintHandler == null ? null : constraintHandler.getMinExclusive();
	}

	public String getMaxExclusive() {
		return constraintHandler == null ? null : constraintHandler.getMaxExclusive();
	}

	@Override
	public int getRepeat() {
		return modelObject.getRepeat();
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

	@Override
	public String getComponentType() {
		return modelObject.getComponentType();
	}

	public boolean isMissingAssignedType() {
		if (modelObject.getTLType() == null)
			return true;
		return false;
	}

	@Override
	public boolean isInheritedProperty() {
		return inherited;
	}

	/**
	 * Test this node against those in the parentNode to and return true if the name is unique within its parent and
	 * inherited parent. NOTE: does not check other facets!
	 * 
	 * @return
	 */
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

	public void setMandatory(final boolean selection) {
		if (isEditable_newToChain() && modelObject != null) {
			modelObject.setMandatory(selection);
		}
	}

	public void setPattern(final String pattern) {
		if (constraintHandler != null)
			constraintHandler.setPattern(pattern);
	}

	public void setMinLength(final int length) {
		if (constraintHandler != null)
			constraintHandler.setMinLength(length);
	}

	public void setMaxLength(final int length) {
		if (constraintHandler != null)
			constraintHandler.setMaxLength(length);
	}

	public void setFractionDigits(final int length) {
		if (constraintHandler != null)
			constraintHandler.setFractionDigits(length);
	}

	public void setTotalDigits(final int length) {
		if (constraintHandler != null)
			constraintHandler.setTotalDigits(length);
	}

	public void setMinInclusive(final String value) {
		if (constraintHandler != null)
			constraintHandler.setMinInclusive(value);
	}

	public void setMaxInclusive(final String value) {
		if (constraintHandler != null)
			constraintHandler.setMaxInclusive(value);
	}

	public void setMinExclusive(final String value) {
		if (constraintHandler != null)
			constraintHandler.setMinExclusive(value);
	}

	public void setMaxExclusive(final String value) {
		if (constraintHandler != null)
			constraintHandler.setMaxExclusive(value);
	}

	/**
	 * @return the assigned namespace prefix from the model object.
	 */
	public String getAssignedPrefix() {
		return getModelObject().getAssignedPrefix();
	}

	@Override
	public Node getAssignable() {
		if (isValueWithAttributes())
			return getSimpleProperty();
		else if (isCoreObject())
			return getSimpleProperty();
		else if (isSimpleFacet())
			return getChildren().get(0);
		return null;
	}

	private Node getSimpleProperty() {
		for (Node n : getChildren()) {
			if (n.isSimpleFacet())
				return n.getChildren().get(0);
		}
		return null;
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

	public PropertyOwnerInterface getDefaultFacet() {
		if (this instanceof Enumeration || isExtensionPointFacet()) {
			return (PropertyOwnerInterface) this;
		}
		for (final INode n : getChildren()) {
			if (((Node) n).isDefaultFacet()) {
				return (PropertyOwnerInterface) n;
			}
		}
		return null;
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

	/**
	 * @return - Node for ID facet if it exists, null otherwise.
	 */
	public PropertyOwnerInterface getIDFacet() {
		return (PropertyOwnerInterface) getFacetOfType(TLFacetType.ID);
	}

	/**
	 * @return - Node for SUMMARY facet if it exists, null otherwise.
	 */
	public PropertyOwnerInterface getSummaryFacet() {
		return (PropertyOwnerInterface) getFacetOfType(TLFacetType.SUMMARY);
	}

	public ComponentNode getSimpleFacet() {
		return getFacetOfType(TLFacetType.SIMPLE);
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

	protected LibraryMember createMinorTLVersion(VersionedObjectInterface node) {
		MinorVersionHelper helper = new MinorVersionHelper();
		Versioned v = null;
		try {
			v = helper.createNewMinorVersion((Versioned) getTLModelObject(), getChain().getHead().getTLLibrary());
		} catch (VersionSchemeException | ValidationException e) {
			// LOGGER.debug("Exception creating minor TL version: " + e.getLocalizedMessage());
			return null;
		}
		return (LibraryMember) v;
	}

	protected ComponentNode createMinorVersionComponent(ComponentNode newNode) {
		if (newNode.getModelObject() instanceof EmptyMO) {
			// LOGGER.debug("Empty minor version created");
			return null;
		}
		Node owner = this.getOwningComponent();
		// exit if it is already in the head of the chain.
		if (owner.getLibrary() == owner.getChain().getHead())
			return null;

		if (newNode instanceof ExtensionOwner)
			((ExtensionOwner) newNode).setExtension(owner);
		if (newNode.getName() == null || newNode.getName().isEmpty())
			newNode.setName(owner.getName()); // Some of the version handlers do not set name
		owner.getChain().getHead().addMember(newNode);

		return newNode;
	}

	/**
	 * Create a new object in a patch version library. Creates an empty extension point facet. Adds the new node to the
	 * owner's chain head library.
	 * 
	 * @return new extension point facet, of this one if it was an extension point
	 */
	public ComponentNode createPatchVersionComponent() {
		if (this.getOwningComponent().isExtensionPointFacet())
			return (ComponentNode) this.getOwningComponent();

		ExtensionPointNode newNode = null;
		newNode = new ExtensionPointNode(new TLExtensionPointFacet());

		if (isFacet())
			newNode.setExtension(this);
		else if (this instanceof PropertyNode)
			newNode.setExtension(getParent());
		else if (this instanceof CoreObjectNode)
			newNode.setExtension((Node) getSummaryFacet());
		else if (this instanceof BusinessObjectNode)
			newNode.setExtension((Node) getSummaryFacet());
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

	/**
	 * @return - Node for ID facet if it exists, null otherwise.
	 */
	public PropertyOwnerInterface getDetailFacet() {
		return (PropertyOwnerInterface) getFacetOfType(TLFacetType.DETAIL);
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
	 * 
	 * @param SubType
	 *            to change to
	 * @return - new object created by changing this object
	 */
	public ComponentNode changeObject(SubType st) {
		switch (st) {
		case BUSINESS_OBJECT:
			return changeToBusinessObject();
		case CORE_OBJECT:
			return changeToCoreObject();
		case VALUE_WITH_ATTRS:
			return changeToVWA();
		default:
			throw new IllegalArgumentException("SubType: " + st.toString() + " is not supporeted.");
		}
	}

	/**
	 * Change Node to VWA_Node using properties of this node as templates. Create new node and assign type users.
	 * Replace this node in the library. Removes type users from old node and its children. NOTES: 1) Does <b>not</b>
	 * delete this node.
	 * 
	 */
	public ComponentNode changeToVWA() {
		if (this.isValueWithAttributes()) {
			return this;
		}
		if (this.isFacet()) {
			return ((ComponentNode) getParent()).changeToVWA();
		}

		// LOGGER.debug("Generating Value With Attributes out of " + this.getName());

		VWA_Node newCN = null;
		if (this instanceof BusinessObjectNode)
			newCN = new VWA_Node((BusinessObjectNode) this);
		else if (this instanceof CoreObjectNode)
			newCN = new VWA_Node((CoreObjectNode) this);
		if (newCN != null)
			swap(newCN);
		return newCN;
	}

	public ComponentNode changeToCoreObject() {
		if (this.isCoreObject()) {
			return this;
		}
		if (this.isFacet()) {
			return ((ComponentNode) getParent()).changeToCoreObject();
		}
		// LOGGER.debug("Generating Core Object out of " + this.getName());

		ComponentNode newCN = null;
		if (this instanceof BusinessObjectNode) {
			newCN = new CoreObjectNode((BusinessObjectNode) this);
		} else if (this instanceof VWA_Node) {
			newCN = new CoreObjectNode((VWA_Node) this);
		}
		if (newCN != null)
			swap(newCN);
		return newCN;
	}

	/**
	 * Replace this node with a newly created business object. Anywhere this node is used as a type is changed to use
	 * the new node. This node is removed from its library and the new business object is added. Used by changeObject
	 * wizard.
	 * 
	 * NOTE - this does not remove this node.
	 * 
	 * @return
	 */
	public ComponentNode changeToBusinessObject() {
		if (this.isBusinessObject()) {
			return this;
		}
		if (this.isFacet()) {
			return ((ComponentNode) getParent()).changeToBusinessObject();
		}
		if (getLibrary() == null) {
			return null;
		}

		ComponentNode newNode = null;
		if (this instanceof CoreObjectNode)
			newNode = new BusinessObjectNode((CoreObjectNode) this);
		else if (this instanceof VWA_Node)
			newNode = new BusinessObjectNode(((VWA_Node) this));

		if (newNode != null)
			swap(newNode);
		return newNode;

	}

	@Override
	public boolean isAliasable() {
		return isBusinessObject() || isCoreObject();
	}

	// @Override
	// public boolean isAssignableToSimple() {
	// return false;
	// }
	//
	// @Override
	// public boolean isAssignableToVWA() {
	// return false;
	// }
	//
	// @Override
	// public boolean isAssignableToElementRef() {
	// return false;
	// }

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
	public void addProperty(final Node property, int index) {
		if (!(this instanceof FacetNode) && !(this instanceof Enumeration) && !(this instanceof ExtensionPointNode)) {
			// LOGGER.error("ERROR - Exit - Tried to add property to a non-FacetNode or enumeration " + this);
			return;
		}
		if (this.isSimpleFacet()) {
			if (property instanceof PropertyNode)
				((TypeUser) getChildren().get(0)).setAssignedType(((TypeUser) property).getAssignedType());
			else
				((TypeUser) getChildren().get(0)).setAssignedType((TypeProvider) property);
		} else {
			property.setParent(this);
			if (index >= 0)
				linkChild(property, index);
			else
				linkChild(property, false);
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
	 * Add list of properties to a facet.
	 * 
	 * @param properties
	 * @param clone
	 *            - if true, the properties are cloned before adding.
	 */
	public void addProperties(List<Node> properties, boolean clone) {
		// LOGGER.debug("addProperties not implemented for this class: " + this.getClass());
	}

	/**
	 * Add a new property from a Drag-and-Drop action. If version rules require, a new component is created.
	 * 
	 * @param isCopy
	 *            - force a copy even if this node does not have assigned type
	 * @return the new node or null if the type was only assigned.
	 */
	public Node addPropertyFromDND(Node sourceNode, boolean isCopy) {
		// XSD types can only be the library member, not facets
		if (sourceNode.isXsdType())
			sourceNode = sourceNode.getOwningComponent();

		ComponentNode newNode = null;
		if (isCopy || !isUnAssigned()) {
			// Create a new property of the same type as this
			// If dropping on a patch or minor version, create the component for the new property.
			if (getChain() != null) {
				if (getChain().isPatch())
					newNode = createPatchVersionComponent();
				else if (getChain().isMinor() && this instanceof VersionedObjectInterface)
					newNode = ((VersionedObjectInterface) this).createMinorVersionComponent();
			}
			if (newNode != null)
				newNode = (ComponentNode) newNode.createProperty(sourceNode);
			else
				newNode = (ComponentNode) createProperty(sourceNode);

			// In minor versions, all new properties must be optional.
			if (this instanceof FacetNode && ((FacetNode) this).isSummary())
				if (!getLibrary().isMinorVersion())
					newNode.setMandatory(true); // make summary facet properties mandatory by default.
		} else {
			if (this instanceof TypeUser && sourceNode instanceof TypeProvider)
				((TypeUser) this).setAssignedType((TypeProvider) sourceNode);
		}
		return newNode;
	}

	public INode createProperty(final Node dropTarget) {
		return null;
	}

	public void removeProperty(final Node property) {
		// throw new IllegalStateException("Remove property in component node should never run.");
	}

	/**
	 * Returns true if the 'otherNode's' library meets both of the following conditions:
	 * <ul>
	 * <li>The other library is assigned to the same version scheme and base namespace as this one.</li>
	 * <li>The version of the other library is considered to be later than this library's version according to the
	 * version scheme.</li>
	 * </ul>
	 * 
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary.isLaterVersion
	 * @param n
	 *            node whose library to compare to this nodes library
	 * @return boolean
	 */
	public boolean isLaterVersion(Node n) {
		return (this.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib()));
	}

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public boolean isXsdType() {
		// Local anonymous types may not have xsdType set.
		return local ? true : xsdType;
	}

	/**
	 * @param local
	 *            the local to set
	 */
	public void setLocal(boolean local) {
		this.local = local;
		this.xsdType = local;
	}

	/** TYPE Interfaces **/

	// @Override
	// public ArrayList<Node> typeUsers() {
	// return getTypeClass().getTypeUsers();
	// }

	// @Override
	// public List<Node> getWhereUsed_OLD() {
	// return getTypeClass().getTypeUsers();
	// }

	@Override
	public int getWhereAssignedCount() {
		int cnt = 0;
		if (this instanceof TypeProvider)
			cnt = ((TypeProvider) this).getWhereAssignedCount();
		return cnt;

		// if (getTypeClass() == null)
		// return 0;
		//
		// // return total count for all facets. Overloaded in facet class.
		// int cnt = getTypeClass().getTypeUsersCount();
		// for (Node n : getChildren())
		// cnt += n.getTypeUsersCount();
		// return cnt;
		// return getTypeClass() != null ? getTypeClass().getTypeUsersCount() : 0;
	}

	// @Override
	// public int getComponentUsersCount() {
	// return getTypeClass().getTypeUsersAndDescendantsCount();
	// }

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.opentravel.schemas.types.TypeProvider#getTypeOwner()
	// */
	// @Override
	// public Node getTypeOwner() {
	// return getTypeClass().getTypeOwner();
	// }

	/**
	 * Each access of children is sorting them based on order of MO's children.
	 */
	@Override
	public List<Node> getChildren() {
		return synchChildrenWithMO(super.getChildren());
	}

	/**
	 * Synchronize order of children with ModelObject children order.
	 * 
	 * @param children
	 * @return sorted list of children based on order of ModelObject.
	 */
	private List<Node> synchChildrenWithMO(List<Node> children) {
		if (getModelObject() == null)
			return Collections.emptyList(); // happens during delete.
		final List<?> tlChildrenOrder = getModelObject().getChildren();
		Collections.sort(children, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				Integer idx1 = tlChildrenOrder.indexOf(o1.getModelObject().getTLModelObj());
				Integer idx2 = tlChildrenOrder.indexOf(o2.getModelObject().getTLModelObj());
				return idx1.compareTo(idx2);
			}
		});
		return children;
	}

	@Override
	public void sort() {
		getModelObject().sort();
	}

}
