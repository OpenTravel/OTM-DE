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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.OperationFacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeUserListener;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;

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
public class PropertyNode extends ComponentNode implements TypeUser {
	// private static final Logger LOGGER = LoggerFactory.getLogger(PropertyNode.class);

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
				pn = oldEleRefN != null ? oldEleRefN : new ElementReferenceNode(parent, getName());
				oldEleRefN = (ElementReferenceNode) pn;
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
	protected PropertyNodeType propertyType;
	protected AlternateRoles alternateRoles;

	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;
	protected TypeUserHandler typeHandler = null;

	/**
	 * Create a property node to represent the passed TL Model object.
	 * 
	 * @param obj
	 * @param propertyType
	 */
	protected PropertyNode(final TLModelElement tlObj, INode parent, final PropertyNodeType propertyType) {
		super(tlObj);
		this.propertyType = propertyType;
		this.alternateRoles = new AlternateRoles(this);
		if (parent instanceof ContributedFacetNode)
			parent = ((ContributedFacetNode) parent).getContributor();
		if (parent != null) {
			parent.getModelObject().addChild(tlObj); // link to TL model
			((Node) parent).linkChild(this); // link to node model
			setLibrary(parent.getLibrary());
		}

		typeHandler = new TypeUserHandler(this);

		if (getRequiredType() != null)
			typeHandler.set(this.getRequiredType());
	}

	/**
	 * Create a new property and add to the facet.
	 * 
	 * @param parent
	 *            facet node to attach to
	 * @param name
	 *            to give the property
	 */
	protected PropertyNode(final TLModelElement tlObj, final Node parent, final String name, final PropertyNodeType type) {
		this(tlObj, parent, type);
		setName(name);

		// Properties added to minor versions must be optional
		if (getLibrary() != null && getLibrary().isMinorVersion() && !inherited)
			setMandatory(false);
	}

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

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return (modelObject != null ? modelObject.getTLType() : null);
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		return typeHandler.getTLModelElement();
	}

	/**
	 * Override to handle inherited properties by getting the inheritsFrom property.
	 * 
	 * @return
	 */
	@Override
	public TypeProvider getAssignedType() {
		if (isInherited()) {
			// Inherited nodes are not assigned a type class. If they were the where-used count would be wrong.
			assert getInheritsFrom() != this;
			if (getInheritsFrom() != null) {
				return ((TypeUser) getInheritsFrom()).getAssignedType();
			}
			return null;
		}
		return typeHandler.get();
	}

	@Override
	public String getEquivalent(String context) {
		return equivalentHandler != null ? equivalentHandler.get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		return equivalentHandler;
	}

	/**
	 * If context is null, get default example
	 */
	@Override
	public String getExample(String context) {
		return exampleHandler != null ? exampleHandler.get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	public IValueWithContextHandler getExampleHandler() {
		return exampleHandler;
	}

	/**
	 * Return images for properties.
	 */
	@Override
	public Image getImage() {
		throw new IllegalAccessError("Tried to get image from abstract property.");
	}

	@Override
	public String getLabel() {
		return getName();
		// return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeUserListener(this);
	}

	/**
	 * Overridden to provide assigned type and aliases if deep is set.
	 */
	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	/**
	 * Return new array containing assigned type if an alias its parent
	 */
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
	public Node getOwningComponent() {
		if (getParent() == null || getParent().getParent() == null)
			return this;
		if (getParent() instanceof OperationFacetNode)
			return getParent();
		if (getParent() instanceof ExtensionPointNode)
			return getParent();
		// EnumLiterals are overridden.
		if (getParent() instanceof ContextualFacetNode)
			return getParent().getOwningComponent();

		// Otherwise Properties are always owned by a facet.
		// TODO - delegate as is done with contextual facets
		return getParent().getParent().isNamedEntity() ? getParent().getParent() : this;
	}

	/**
	 * Property Roles are displayed in the facet table and describe what role the item can play in constructing
	 * vocabularies.
	 * 
	 * @return
	 */
	@Override
	public String getPropertyRole() {
		return propertyType.getName();
	}

	/**
	 * @return the propertyType
	 */
	public PropertyNodeType getPropertyType() {
		return propertyType;
	}

	@Override
	public TypeProvider getRequiredType() {
		return null; // override for properties with fixed types
	}

	@Override
	public Node getType() {
		return (Node) getAssignedType();
	}

	@Override
	public String getTypeName() {
		return typeHandler.getName();
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
	public int indexOfNode() {
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
	public int indexOfTLProperty() {
		return 0;
	}

	// @Override
	public boolean isAssignedComplexType() {
		// Need to test because inherited properties do not have assigned types.
		return getAssignedType() != null ? getAssignedType().isAssignedByReference() : false;
	}

	/**
	 * @return true if indicator element or attribute.
	 */
	public boolean isIndicator() {
		return false;
	}

	@Override
	public boolean isDeleteable() {
		if (modelObject == null)
			return false;
		if (getModelObject().getTLModelObj() instanceof TLnSimpleAttribute)
			return false;
		if (isInherited())
			return false;
		return super.isDeleteable();
	}

	@Override
	public boolean isMissingAssignedType() {
		// LOGGER.debug("check property node "+getName()+" for missing type. "+modelObject);
		if (modelObject == null || modelObject.getTLModelObj() == null)
			return true;
		if (modelObject.getTLType() == null)
			return true;
		return false;
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
		return isEditable() && !inherited && getAssignedType().isRenameableWhereUsed();
	}

	/**
	 * @return true if this property is an attribute of a Value With Attributes object.
	 */
	public boolean isVWA_Attribute() {
		return getOwningComponent() instanceof VWA_Node;
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

	/**
	 * 
	 * @param direction
	 * @return true if moved, false otherwise
	 */
	public boolean moveProperty(final int direction) {
		if (!isEditable_newToChain())
			return false;
		boolean ret = false;
		// we don't have to sort children since their are always sorted
		if (direction == UP) {
			ret = modelObject.moveUp(); // move the actual TL Property.
			if (ret)
				moveUp();
		} else if (direction == DOWN) {
			ret = modelObject.moveDown(); // move the actual TL Property.
			if (ret)
				moveDown();
		}
		// LOGGER.warn("Do not understand direction: " + direction);
		return ret;
	}

	private void moveDown() {
		int index = parent.getChildren().indexOf(this);
		if (index < parent.getChildren().size() - 1) {
			parent.getChildren().remove(index++);
			parent.getChildren().add(index, this);
		}
	}

	private void moveUp() {
		int index = parent.getChildren().indexOf(this);
		if (index > 0) {
			parent.getChildren().remove(index--);
			parent.getChildren().add(index, this);
		}
	}

	/**
	 * Remove this property from its parent facet and its TL Object from its TL Parent
	 */
	public void removeProperty() {
		this.unlinkNode();
		this.getModelObject().removeFromTLParent();
	}

	@Override
	public boolean setAssignedType() {
		return typeHandler.set();
	}

	@Override
	public boolean setAssignedType(TLModelElement tlProvider) {
		return typeHandler.set(tlProvider);
	}

	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return typeHandler.set(provider);
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
	public void setName(final String name) {
		setName(name);
	}

	@Override
	public void sort() {
		getParent().sort();
	}

	/**
	 * Remove <i>this</i> property from the parent and add the <i>newProperty</i> in its place.
	 */
	public void swap(PropertyNode newProperty) {
		// Link new property to the parent node.
		getParent().linkChild(newProperty); // no family processing needed
		// Add the new property TL element to its TL Parent
		newProperty.modelObject.addChild(newProperty.getTLModelObject());
		// Remove this TL element from its TL parent.
		modelObject.removeFromTLParent();
		// Remove this property from its parent
		getParent().getChildren().remove(this);

		// Remove from current TL parent and add to new. Model object will ignore if no parent.
		modelObject.removeFromTLParent();
		getParent().getModelObject().addChild(newProperty.getTLModelObject());
	}

}
