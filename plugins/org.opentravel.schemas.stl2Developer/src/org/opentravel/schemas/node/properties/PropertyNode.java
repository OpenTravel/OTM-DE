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
package org.opentravel.schemas.node.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.listeners.TypeUserListener;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property nodes control element (property), attribute and indicator property model objects Simple Attributes and
 * others. (See PropertyNodeType: Role, Literal, Alias, Simple)
 * 
 * Properties extends components by giving them the ability change nature (Indicator, attribute, element) without losing
 * data unique to each one. To do this, it has extra model objects. Also used for RoleProperty nodes.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class PropertyNode extends ComponentNode implements TypeUser {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyNode.class);

	/**
	 * A property within a facet can change roles. This class keeps track of the various implementations that may be
	 * used for any property. All properties that can be alternated between should share this class.
	 * 
	 * @author Dave Hollander
	 * 
	 */
	protected class AlternateRoles {
		public PropertyNodeType currentType;
		public ElementNode oldEleN = null;
		public IndicatorNode oldIndN = null;
		public AttributeNode oldAttrN = null;
		public IndicatorElementNode oldIndEleN = null;
		public ElementReferenceNode oldEleRefN = null;
		public AttributeReferenceNode oldAttrRefN = null;
		public IdNode oldIdN = null;

		public AlternateRoles(PropertyNode pn) {
			// Have to use role assigned not class because it is used in chained constructors
			// so the class type may be wrong.
			switch (pn.getPropertyType()) {
			case ELEMENT:
				oldEleN = (ElementNode) pn;
				break;
			case ATTRIBUTE:
				oldAttrN = (AttributeNode) pn;
				break;
			case ID:
				oldIdN = (IdNode) pn;
				break;
			case ID_REFERENCE:
				oldEleRefN = (ElementReferenceNode) pn;
				break;
			case ID_ATTR_REF:
				oldAttrRefN = (AttributeReferenceNode) pn;
				break;
			case INDICATOR:
				oldIndN = (IndicatorNode) pn;
				break;
			case INDICATOR_ELEMENT:
				oldIndEleN = (IndicatorElementNode) pn;
				break;
			default:
				break;
			}
			currentType = pn.getPropertyType();
		}

		/**
		 * If an old copy of this property has been saved, return it; otherwise create a new property. New properties
		 * are saved.
		 * 
		 * @param type
		 * @param propertyNode
		 * @return property or null if not supported for the property type or this was already that type.
		 */
		public PropertyNode oldOrNew(PropertyNodeType type) {
			assert getParent() instanceof PropertyOwnerInterface;
			PropertyOwnerInterface parent = (PropertyOwnerInterface) getParent();

			if (currentType.equals(type))
				return null;
			PropertyNode pn = null;
			switch (type) {
			case ELEMENT:
				// set pn to saved element if there is one or else create an element
				pn = oldEleN != null ? oldEleN : new ElementNode(parent, getName());
				oldEleN = (ElementNode) pn;
				break;
			case ID_REFERENCE:
				pn = oldEleRefN != null ? oldEleRefN : new ElementReferenceNode(parent);
				oldEleRefN = (ElementReferenceNode) pn;
				break;
			case ID_ATTR_REF:
				pn = oldAttrRefN != null ? oldAttrRefN : new AttributeReferenceNode(parent);
				oldAttrRefN = (AttributeReferenceNode) pn;
				break;
			case ATTRIBUTE:
				pn = oldAttrN != null ? oldAttrN : new AttributeNode(parent, getName());
				oldAttrN = (AttributeNode) pn;
				break;
			case INDICATOR:
				pn = oldIndN != null ? oldIndN : new IndicatorNode(parent, getName());
				oldIndN = (IndicatorNode) pn;
				break;
			case INDICATOR_ELEMENT:
				pn = oldIndEleN != null ? oldIndEleN : new IndicatorElementNode(parent, getName());
				oldIndEleN = (IndicatorElementNode) pn;
				break;
			case ID:
				pn = oldIdN != null ? oldIdN : new IdNode(parent, getName());
				oldIdN = (IdNode) pn;
				break;
			default:

			}
			if (pn != null)
				pn.alternateRoles = this; // in case there was a new node created.
			currentType = type;
			return pn;
		}

	}

	/**
	 * Move the property up/down in its current facet.
	 * 
	 * @param - direction (up/1 or down/2)
	 * @return false if it could not be moved (was at end of list of that type of properties).
	 */
	public static final int UP = 1;
	public static final int DOWN = 2;
	protected AlternateRoles alternateRoles;

	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;
	protected TypeUserHandler typeHandler = null;
	public ConstraintHandler constraintHandler = null;

	/**
	 * Only used for facade properties that have no TL Model Object.
	 */
	public PropertyNode() {
	}

	/**
	 * Create a property node to represent the passed TL Model object.
	 * 
	 * @param obj
	 */
	protected PropertyNode(final TLModelElement tlObj, PropertyOwnerInterface parent) {
		super(tlObj);
		this.alternateRoles = new AlternateRoles(this);

		if (parent != null)
			parent.addProperty(this);

		typeHandler = new TypeUserHandler(this);

		fixContext(); // fix context assures example and equivalent are in this context
	}

	/**
	 * Create a new property and add to the facet.
	 * 
	 * @param parent
	 *            facet node to attach to
	 * @param name
	 *            to give the property
	 */
	protected PropertyNode(final TLModelElement tlObj, final PropertyOwnerInterface parent, final String name) {
		this(tlObj, parent);
		setName(name);

		// Properties added to minor versions must be optional
		if (getLibrary() != null && getLibrary().isMinorVersion() && !isInherited())
			setMandatory(false);
	}

	/**
	 * Add this TLModelObject to the passed owner.
	 * 
	 * @param owner
	 *            - property owner (facet) to add this to
	 */
	public void addToTL(PropertyOwnerNode owner) {
		addToTL(owner, -1);
	}

	@Override
	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	/**
	 * Add this TLModelObject to the passed owner.
	 * 
	 * @param owner
	 *            - property owner (facet) to add this to
	 * @param index
	 *            - index into child array to set order or -1
	 */
	public abstract void addToTL(final PropertyOwnerInterface owner, final int index);

	@Override
	public boolean canAssign(Node type) {
		if (type == null || !(type instanceof TypeProvider))
			return false;
		return true;
	}

	/**
	 * Replace this property with one of the specified type. Uses the saved alternateRole or creates a new property. All
	 * values that can be copied will be. The old property is saved.
	 * 
	 * It this has a parent, the new role must be valid or else this is returned.
	 * 
	 * If this does not have a parent, then the new property is returned without parent.
	 * 
	 * All copies share the same alternateRoles instance.
	 * 
	 * @param toType
	 * @return the new property, or this property if change could not be made.
	 */
	public PropertyNode changePropertyRole(PropertyNodeType toType) {
		PropertyNode newProperty = null;

		if (getParent().isValidParentOf(toType)) {
			newProperty = alternateRoles.oldOrNew(toType);
			if (newProperty != null) {
				newProperty.copyDetails(this);
				swap(newProperty);

				// Now do type assignments
				if (newProperty.getRequiredType() == null) {
					// Has a user assigned type. Reuse previous assignment if any or else try type assigned to this.
					TypeProvider newType = newProperty.getAssignedType();
					if (newType == null || newType instanceof ImpliedNode)
						newType = getAssignedType();
					newProperty.setAssignedType(newType);
				}
				// Clear type assignments to <i>this</i> property to keep where used in sync.
				getAssignedType().getWhereAssignedHandler().remove(this);
				// Leave the type assigned to the TL Object for use as assigned type if they change back later.
			}
		}
		return newProperty == null ? this : newProperty;
	}

	/**
	 * Clone the TLModelObject and create clone node using factory. Assign clone to parent. Set whereUsed on assigned
	 * type.
	 * 
	 * @param parent
	 *            PropertyOwnerInterface to add clone to
	 * @param nameSuffix
	 *            added at end of name if not null
	 */
	@Override
	public PropertyNode clone(Node parent, String nameSuffix) {
		PropertyNode clone = null;
		if (parent instanceof PropertyOwnerInterface) {
			LibraryElement clonedTL = getTLModelObject().cloneElement();
			if (clonedTL instanceof TLModelElement) {
				clone = (PropertyNode) NodeFactory.newChild(parent, (TLModelElement) clonedTL);
				assert clone instanceof PropertyNode;
				if (nameSuffix != null)
					clone.setName(clone.getName() + nameSuffix);
				// Set the whereUsed for the type provider
				if (clone.getAssignedType() != null)
					clone.getAssignedType().addTypeUser(clone);
				else
					clone.setAssignedType();
			}
		}
		return clone;
	}

	public boolean isMandatory() {
		return false;
	}

	/**
	 * Make this have same values as source node (name, mandatory, doc, ex, eq). Does <b>not</b> set types.
	 * 
	 * @param target
	 */
	public void copyDetails(PropertyNode source) {
		TLModelElement tlSource = source.getTLModelObject();
		TLModelElement tlTarget = getTLModelObject();

		setName(getName());
		setMandatory(isMandatory());
		// setIdentity(source.getIdentity());

		TLDocumentation doc, sDoc;
		if (tlTarget instanceof TLDocumentationOwner && tlSource instanceof TLDocumentationOwner) {
			sDoc = ((TLDocumentationOwner) tlSource).getDocumentation();
			if (sDoc != null) {
				doc = (TLDocumentation) sDoc.cloneElement();
				((TLDocumentationOwner) tlTarget).setDocumentation(doc);
			}
		}

		if (tlTarget instanceof TLExampleOwner && tlSource instanceof TLExampleOwner)
			for (final TLExample ex : ((TLExampleOwner) tlSource).getExamples()) {
				((TLExampleOwner) tlTarget).addExample(ex);
			}

		if (tlTarget instanceof TLEquivalentOwner && tlSource instanceof TLEquivalentOwner)
			for (final TLEquivalent eq : ((TLEquivalentOwner) tlSource).getEquivalents()) {
				((TLEquivalentOwner) tlTarget).addEquivalent(eq);
			}
	}

	/**
	 * If <i>this</i> property does not have a type, assign it to the source node. Otherwise, create a new property
	 * after <i>this</i>node. Use the passed node as the type.
	 * 
	 * @param - the type to assign.
	 */
	@Override
	public INode createProperty(final Node type) {
		Node n = null;
		return n;
	}

	protected INode createProperty(final PropertyNode clone, final Node type) {
		// sub-type use this after making clone
		clone.addToTL((PropertyOwnerInterface) getParent(), indexOfNode());
		clone.setName(type.getName());
		clone.setDescription(type.getDescription());

		// Tell the parent to clear its children cache because it has a new child
		getParent().getChildrenHandler().clear();
		return clone;
	}

	/**
	 * Remove from TL owner and clear where assigned and parent's children handler.
	 */
	@Override
	public void delete() {
		if (isDeleted() || getParent() == null)
			return;
		setAssignedType();
		deleteTL();
		getParent().getChildrenHandler().clear(); // Must be last
	}

	@Override
	public void deleteTL() {
		if (getTLModelObject() != null) {
			removeFromTL();
			ListenerFactory.clearListners(getTLModelObject()); // remove any listeners
		}
	}

	/**
	 * Set the context to the owning library's defaultContextId.
	 */
	public void fixContext() {
		if (getLibrary() == null)
			return;
		// if (isEditable()) {
		if (getExampleHandler() != null)
			exampleHandler.fix(null);
		if (getEquivalentHandler() != null)
			equivalentHandler.fix(null);
		// }
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return null; // overriden in element and attribute nodes
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		return getTypeHandler().getAssignedTLModelElement();
	}

	/**
	 * Override to handle inherited properties by getting the inheritsFrom property.
	 * 
	 * @return
	 */
	@Override
	public TypeProvider getAssignedType() {
		assert !isInherited();
		return getTypeHandler().get();
	}

	public String getEquivalent(String context) {
		return getEquivalentHandler() != null ? getEquivalentHandler().get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		if (getTLModelObject() instanceof TLEquivalentOwner)
			if (equivalentHandler == null)
				equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler;
	}

	/**
	 * If context is null, get default example
	 */
	public String getExample(String context) {
		return getExampleHandler() != null ? getExampleHandler().get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	@Override
	public IValueWithContextHandler getExampleHandler() {
		if (getTLModelObject() instanceof TLExampleOwner)
			if (exampleHandler == null)
				exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler;
	}

	/**
	 * Return images for properties.
	 */
	@Override
	public abstract Image getImage();

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public abstract String getName();

	/**
	 * Properties are always in the library of their owning component.
	 * 
	 * @return library of the owning component
	 */
	@Override
	public LibraryNode getLibrary() {
		if (getOwningComponent() == null || getOwningComponent() == this)
			return null;
		return getOwningComponent().getLibrary() != null ? getOwningComponent().getLibrary() : null;
	}

	/**
	 * Return new array containing assigned type if an alias its parent
	 */
	@Override
	public List<Node> getNavChildren(boolean deep) {
		List<Node> kids = new ArrayList<Node>();
		if (deep) {
			Node typeNode = (Node) getAssignedType();
			kids.add(typeNode);

			// If it is an alias, add its object as well.
			if (typeNode instanceof AliasNode)
				kids.add(typeNode.getParent());
		}
		return kids;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeUserListener(this);
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		if (getParent() == null)
			return null;
		return getParent().getOwningComponent();
	}

	// Must override
	@Override
	public abstract Node getParent();

	/**
	 * Property Roles are displayed in the facet table and describe what role the item can play in constructing
	 * vocabularies.
	 * 
	 * @return
	 */
	@Override
	public String getPropertyRole() {
		return getPropertyType().getName();
	}

	/**
	 * @return the propertyType - an enumeration based on node class
	 */
	// TODO - add function to PropertyNodeType class to accept a node and return type
	public PropertyNodeType getPropertyType() {
		return PropertyNodeType.getPropertyType(this);
		// return propertyType;
	}

	@Override
	public TypeProvider getRequiredType() {
		return null; // override for properties with fixed types
	}

	/**
	 * Overridden to provide assigned type and aliases if deep is set.
	 */
	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	@Override
	@Deprecated
	public Node getType() {
		return (Node) getAssignedType();
	}

	@Override
	public String getTypeName() {
		return getTypeHandler() != null ? getTypeHandler().getName() : "";
	}

	@Override
	public String getTypeNameWithPrefix() {
		String typeName = getTypeName() == null ? "" : getTypeName();
		if (getAssignedType() == null)
			return "";
		if (getAssignedType() instanceof ImpliedNode)
			return typeName;
		if (getPrefix().equals(getAssignedPrefix()))
			return typeName;
		return getType().getPrefix() + " : " + typeName;
	}

	/**
	 * True if it has assigned type
	 */
	@Override
	public boolean hasNavChildren(boolean deep) {
		// return getTypeNode() != null;
		return deep && getAssignedType() != null;
	}

	/**
	 * Node index is the order in the node list. Node lists are not separated by node types as they are in TL Model
	 * objects.
	 * 
	 * @return
	 */
	// FIXME - one of these has to become private
	@Deprecated
	public int indexOfNode() {
		if (getParent() == null || getParent().getChildren() == null)
			return 0;
		int index = getParent().getChildren().indexOf(this) + 1;
		return ((index < 1) || (index > getParent().getChildren().size())) ? index = getParent().getChildren().size()
				: index;
	}

	/**
	 * Index of this property in the TL Model object. The TL Model keeps separate lists for elements, attributes and
	 * indicators.
	 * 
	 * @return - index between 0 and size() of the array containing like properties.
	 */
	// FIXME - one of these has to become private
	@Deprecated
	public int indexOfTLProperty() {
		return 0;
	}

	// @Override
	public boolean isAssignedComplexType() {
		// Need to test because inherited properties do not have assigned types.
		return getAssignedType() != null ? getAssignedType().isAssignedByReference() : false;
	}

	@Override
	public boolean isDeleteable() {
		if (getTLModelObject() == null)
			return false;
		if (this instanceof FacadeInterface)
			return false;
		if (isInherited())
			return false;
		return super.isDeleteable();
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return this != getOwningComponent() ? getOwningComponent().isEnabled_AddProperties() : false;
	}

	/**
	 * Properties can only be a navigation child in deep mode. Non-typed properties override with false.
	 */
	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	// Override if not re-nameable.
	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited() && getAssignedType().isRenameableWhereUsed();
	}

	/**
	 * @return true if this property is an attribute of a Value With Attributes object.
	 */
	public boolean isVWA_Attribute() {
		return getOwningComponent() instanceof VWA_Node;
	}

	public void moveDown() {
		if (!isEditable_newToChain())
			return;
		int index = parent.getChildren().indexOf(this);
		if (index < parent.getChildren().size() - 1) {
			moveDownTL();
			parent.getChildrenHandler().clear();
		}
	}

	/**
	 * Move this property from its current node to the new facet. Tested with Move_Tests.
	 * 
	 * @param newFacet
	 */
	public void moveProperty(FacetNode newFacet) {
		removeProperty();
		newFacet.addProperty(this);
	}

	public void moveUp() {
		if (!isEditable_newToChain())
			return;
		int index = parent.getChildren().indexOf(this);
		if (index > 0) {
			moveUpTL();
			parent.getChildrenHandler().clear();
		}
	}

	/**
	 * Remove this property from its parent facet and its TL Object from its TL Parent
	 */
	public void removeProperty() {
		removeFromTL();
		if (getParent() != null)
			getParent().getChildrenHandler().clear();
	}

	@Override
	public boolean setAssignedType() {
		return getTypeHandler().set();
	}

	@Override
	public abstract boolean setAssignedType(TLModelElement tlProvider);

	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return getTypeHandler().set(provider);
	}

	@Override
	public void setContext() {
		// Read/write forces to default context
		setEquivalent(getEquivalent(null));
		setExample(getExample(null));
	}

	public IValueWithContextHandler setEquivalent(String equivalent) {
		return null;
	}

	public IValueWithContextHandler setExample(String example) {
		return null;
	}

	@Override
	@Deprecated
	public void setLibrary(LibraryNode lib) {
		// getOwningComponent().setLibrary(lib);
		// LOGGER.debug("Obsolete - property set library - library is always from owner");
	}

	@Override
	public abstract void setName(final String name);

	/**
	 * Remove <i>this</i> property from the parent and add the <i>newProperty</i> in its place.
	 */
	// TODO - add junit tests to PropertyTests
	public void swap(PropertyNode newProperty) {
		if (getParent() instanceof PropertyOwnerNode)
			newProperty.addToTL((PropertyOwnerNode) getParent());
		removeFromTL();
		getParent().getChildrenHandler().clear();
	}

	public void setMandatory(final boolean selection) {
		// Override where supported
	}

	protected abstract void moveDownTL();

	protected abstract void moveUpTL();

	protected abstract void removeFromTL();

	/**
	 * @return the typeHandler
	 */
	protected TypeUserHandler getTypeHandler() {
		return typeHandler;
	}

}
