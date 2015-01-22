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
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ComponentNode class handles nodes that represent model objects. It is overridden for most types and properties.
 * 
 * @author Dave Hollander
 * 
 */

// TODO - this should not implement type provider -- sub-classes should.
public class ComponentNode extends Node implements TypeProvider {

	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	/**
	 * Inherited nodes are not assigned a type class. If they were the where-used count would be wrong.
	 * 
	 * /** The list of inherited children for this node.
	 */
	private List<Node> inheritedChildren;

	/** Actual node where the inherited child is declared. */
	protected Node inheritsFrom = null;

	/**
	 * Indicates whether the underlying model object was declared or inherited by its parent.
	 */
	protected boolean inherited = false;
	private boolean childrenInitialized = false;

	/**
	 * ComponentNode constructor.
	 * 
	 * @see ComponentNode(library), ComponentNode(JaxB element), ComponentNode (name, getNamespace)
	 */

	public ComponentNode() {
		super();
		family = "";
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
		if (modelObject.isComplexFacet()) {
			throw new IllegalStateException("Unexpected model object in cn construction.");
			// addMOChildren();
		}
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getTypeNode()
	 */
	@Override
	public Node getTypeNode() {
		return getTypeClass().getTypeNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isNamedType()
	 */
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
			if (nn != null) {
				nn.addMOChildren();
			} else {
				LOGGER.debug("addMOChildren() - skipping not supported source object type. "
						+ obj.getClass().getSimpleName());
			}
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
		// for (final INode n : getChildren()) {
		// // if (n.isNavChildWithProperties()) {
		// return true;
		// // }
		// }
		// return false;
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
			// if (inheritedChildren == null) {
			initInheritedChildren();
			// }
		}
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
	private void initInheritedChildren() {
		List<?> inheritedMOChildren = modelObject.getInheritedChildren();
		if ((inheritedMOChildren == null) || inheritedMOChildren.isEmpty()) {
			inheritedChildren = Collections.emptyList();
		} else {
			for (final Object obj : inheritedMOChildren) {
				// ComponentNode nn = newComponentNode(obj);
				// null parent allows us to control linkage.
				ComponentNode nn = NodeFactory.newComponentMember(null, obj);

				if (nn != null) {
					linkInheritedChild(nn);
					// Link to the actual node.
					// Use a finder to locate node since there are no back-links.
					if (obj instanceof TLModelElement) {
						Node searchRoot = ModelNode.getModelNode();
						nn.inheritsFrom = searchRoot.findNode(((TLModelElement) obj).getValidationIdentity());
						if (nn.inheritsFrom != null && nn.inheritsFrom.getParent() != null)
							// quicker this way - all others will have same parent
							searchRoot = nn.inheritsFrom.getParent();
					}
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

	/**
	 * Create a new component node and model object and link it to <i>this</i>library's Complex or Simple Root node.
	 * Used for creating model objects from nodes constructed by GUI otmHandlers and wizards.
	 * 
	 * @see {@link NewComponent_Tests.java}
	 * @param component
	 *            objectType strings as defined in ComponentNode
	 * @return node created
	 * 
	 */
	public Node newComponent(final ComponentNodeType type) {
		if (getLibrary() == null) {
			LOGGER.error("Missing Library - can't create new component of type " + type + ".");
			return null;
		}
		// LOGGER.debug("Creating new " + type + " \tcomponent " + getName() + " in " + library);

		ComponentNode cn = null;

		switch (type) {
		case SERVICE:
			cn = new ServiceNode(this);
			break;
		case ALIAS:
			return new AliasNode(this, this.getName());
		case BUSINESS:
			cn = NodeFactory.newComponent(new TLBusinessObject());
			cn.setExtensible(true);
			break;
		case CORE:
			cn = NodeFactory.newComponent(new TLCoreObject());
			cn.setExtensible(true);
			break;
		case VWA:
			cn = NodeFactory.newComponent(new TLValueWithAttributes());
			break;
		case EXTENSION_POINT:
			cn = NodeFactory.newComponent(new TLExtensionPointFacet());
			break;
		case SIMPLE:
			cn = NodeFactory.newComponent(new TLSimple());
			break;
		case OPEN_ENUM:
			cn = NodeFactory.newComponent(new TLOpenEnumeration());
			new EnumLiteralNode(cn, "NewValue");
			break;
		case CLOSED_ENUM:
			cn = NodeFactory.newComponent(new TLClosedEnumeration());
			new EnumLiteralNode(cn, "NewValue");
			break;
		default:
			LOGGER.debug(type + " not supported by newComponent().");
		}
		if (cn != null) {
			cn.setName(getName());
			cn.setDescription(getDescription());

			if (getLibrary() != null)
				getLibrary().addMember(cn);
		}

		return cn;
	}

	/*
	 * ComponentNode Utilities
	 */

	public Boolean isMandatory() {
		return modelObject.isMandatory();
	}

	public String getEquivalent(final String context) {
		return modelObject.getEquivalent(context);
	}

	public String getExample(final String context) {
		return modelObject.getExample(context);
	}

	public TLFacetType getFacetType() {
		return null;
	}

	public int getMaxLen() {
		return modelObject.getMaxLength();
	}

	public int getMinLen() {
		return modelObject.getMinLength();
	}

	public String getPattern() {
		return modelObject.getPattern();
	}

	public int getFractionDigits() {
		return modelObject.getFractionDigits();
	}

	public int getTotalDigits() {
		return modelObject.getTotalDigits();
	}

	public String getMinInclusive() {
		return modelObject.getMinInclusive();
	}

	public String getMaxInclusive() {
		return modelObject.getMaxInclusive();
	}

	public String getMinExclusive() {
		return modelObject.getMinExclusive();
	}

	public String getMaxExclusive() {
		return modelObject.getMaxExclusive();
	}

	@Override
	public int getRepeat() {
		return modelObject.getRepeat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getChildren_TypeUsers()
	 */
	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> kids = new ArrayList<Node>();
		for (Node child : getChildren()) {
			if (child.isTypeUser())
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
		if (modelObject != null) {
			modelObject.setMandatory(selection);
		}
	}

	public void setPattern(final String pattern) {
		if (modelObject != null) {
			modelObject.setPattern(pattern);
		}
	}

	public void setMinLength(final int length) {
		if (modelObject != null) {
			modelObject.setMinLength(length);
		}
	}

	public void setMaxLength(final int length) {
		if (modelObject != null) {
			modelObject.setMaxLength(length);
		}
	}

	public void setFractionDigits(final int length) {
		if (modelObject != null) {
			modelObject.setFractionDigits(length);
		}
	}

	public void setTotalDigits(final int length) {
		if (modelObject != null) {
			modelObject.setTotalDigits(length);
		}
	}

	public void setMinInclusive(final String value) {
		if (modelObject != null) {
			modelObject.setMinInclusive(value);
		}
	}

	public void setMaxInclusive(final String value) {
		if (modelObject != null) {
			modelObject.setMaxInclusive(value);
		}
	}

	public void setMinExclusive(final String value) {
		if (modelObject != null) {
			modelObject.setMinExclusive(value);
		}
	}

	public void setMaxExclusive(final String value) {
		if (modelObject != null) {
			modelObject.setMaxExclusive(value);
		}
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

	public ComponentNode getDefaultFacet() {
		if (isEnumeration() || isExtensionPointFacet()) {
			return this;
		}
		for (final INode n : getChildren()) {
			if (((Node) n).isDefaultFacet()) {
				return (ComponentNode) n;
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
	public ComponentNode getIDFacet() {
		return getFacetOfType(TLFacetType.ID);
	}

	/**
	 * @return - Node for ID facet if it exists, null otherwise.
	 */
	public ComponentNode getSummaryFacet() {
		return getFacetOfType(TLFacetType.SUMMARY);
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
					property.setAssignedType(new AliasNode(type, aliasName), false);
					property.setName(aliasName);
				}
			}
		}
	}

	/**
	 * Create a new object in a minor version library. Creates an empty copy of this node's owner. Adds the new node to
	 * the owner's chain head library.
	 * 
	 * @return the new node summary facet or its detail if this node was the detail facet node.
	 */
	public ComponentNode createMinorVersionComponent() {
		Node owner = this.getOwningComponent();
		ComponentNode newNode = null;
		if (owner.getLibrary() == owner.getChain().getHead())
			return null;

		if (owner.isBusinessObject()) {
			newNode = new BusinessObjectNode(new TLBusinessObject());
		} else if (owner instanceof CoreObjectNode) {
			newNode = new CoreObjectNode(new TLCoreObject());
			// Version extensions of core objects must have the same simple type.
			((CoreObjectNode) newNode).setSimpleType(((CoreObjectNode) owner).getSimpleType());
		} else if (owner instanceof VWA_Node) {
			newNode = new VWA_Node(new TLValueWithAttributes());
		} else {
			LOGGER.error("Can not create minor version of: " + owner);
			return null;
		}
		newNode.setExtendsType(owner);
		newNode.setName(owner.getName());
		owner.getChain().getHead().addMember(newNode);

		if (this.getFacetType() != null) {
			if (this.getFacetType().equals(TLFacetType.DETAIL))
				newNode = newNode.getDetailFacet();
			else
				newNode = newNode.getSummaryFacet();
		}
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
			newNode.setExtendsType(this);
		else if (isProperty())
			newNode.setExtendsType(getParent());
		else if (isCoreObject())
			newNode.setExtendsType(getSummaryFacet());
		else if (isBusinessObject())
			newNode.setExtendsType(getSummaryFacet());
		else
			LOGGER.error("Can't add a property to this: " + this);
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
	public ComponentNode getDetailFacet() {
		return getFacetOfType(TLFacetType.DETAIL);
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

		swap(newNode);
		return newNode;

	}

	@Override
	public boolean isAliasable() {
		return isBusinessObject() || isCoreObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#isAssignableToSimple()
	 */
	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#isAssignableToVWA()
	 */
	@Override
	public boolean isAssignableToVWA() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#isAssignableToElementRef()
	 */
	@Override
	public boolean isAssignableToElementRef() {
		return false;
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
	public void addProperty(final Node property, int index) {
		if (!(this instanceof FacetNode) && !(this.isEnumeration())) {
			LOGGER.error("ERROR - Exit - Tried to add property to a non-FacetNode or enumeration " + this);
			return;
			// TODO - move this logic to FacetNode
		}
		if (this.isSimpleFacet()) {
			if (property.isProperty())
				getChildren().get(0).setAssignedType(property.getAssignedType());
			else
				getChildren().get(0).setAssignedType(property);
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
		LOGGER.debug("addProperties not implemented for this class: " + this.getClass());
	}

	/**
	 * Add a new property from a Drag-and-Drop action. If version rules require, a new component (extension point facet)
	 * is created.
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
				else if (getChain().isMinor())
					newNode = createMinorVersionComponent();
			}
			if (newNode != null)
				newNode = (ComponentNode) newNode.createProperty(sourceNode);
			else
				newNode = (ComponentNode) createProperty(sourceNode);
			if (this instanceof FacetNode && ((FacetNode) this).isSummary())
				newNode.setMandatory(true); // make summary facet properties mandatory by default.
		} else {
			setAssignedType(sourceNode);
		}
		return newNode;
	}

	public INode createProperty(final Node dropTarget) {
		return null;
	}

	/**
	 * Remove the properties in the list from this node and underlying tl model object. Use to move the property to a
	 * different facet.
	 * 
	 * @param property
	 */
	public void removeProperty(final Node property) {
		// throw new IllegalStateException("Remove property in component node should never run.");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getWhereUsedAsType()
	 * 
	 * @deprecated - use getTypeUsers()
	 */
	@Override
	public ArrayList<Node> typeUsers() {
		return getTypeClass().getTypeUsers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getWhereUsed()
	 */
	@Override
	public List<Node> getWhereUsed() {
		return getTypeClass().getTypeUsers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#typeUsersCount()
	 */
	@Override
	public int getTypeUsersCount() {
		if (getTypeClass() == null) {
			return 0;
		}
		return getTypeClass().getTypeUsersCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getComponentUsersCount()
	 */
	@Override
	public int getComponentUsersCount() {
		return getTypeClass().getComponentUsersCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getTypeOwner()
	 */
	@Override
	public Node getTypeOwner() {
		return getTypeClass().getTypeOwner();
	}

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
