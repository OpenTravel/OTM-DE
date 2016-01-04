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

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.modelObject.XsdModelingUtils;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ImpliedNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

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
public class PropertyNode extends ComponentNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(PropertyNode.class);

	protected PropertyNodeType propertyType;
	protected AlternateRoles alternateRoles;
	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;

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
		if (parent != null) {
			parent.getModelObject().addChild(tlObj); // link to TL model
			((Node) parent).linkChild(this, false); // link to node model
			setLibrary(parent.getLibrary());
		}
		// This clears out the assigned type!
		if (getDefaultType() != null)
			getTypeClass().setAssignedType(this.getDefaultType());
		ListenerFactory.setListner(this);
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

		// Allow assignment to non-type-user properties to simplify interface.
		if (!isTypeUser())
			if (type == ModelNode.getUndefinedNode() || type == ModelNode.getIndicatorNode())
				return true;
			else
				return false;

		return true;
		// }
	}

	/**
	 * @return
	 */
	public QName getDefaultXmlElementName() {
		boolean idRef = this instanceof ElementReferenceNode;
		return PropertyCodegenUtils.getDefaultXmlElementName(getTLTypeObject(), idRef);
	}

	/**
	 * @return the propertyType
	 */
	public PropertyNodeType getPropertyType() {
		return propertyType;
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

	@Override
	public Node getType() {
		return getAssignedType();
	}

	@Override
	public String getTypeName() {
		if (!(this instanceof TypeUser))
			return "";

		if (getTypeClass().getTypeNode() == null) {
			// Inherited nodes are not assigned a type class. If they were
			if (inherited)
				return getModelObject().getAssignedName();

			// LOGGER.warn("Trying to fix missing type assignment for " + getName());
			getTypeClass().setAssignedTypeForThisNode(this);
		}

		String name = getTypeClass().getTypeNode().getName();

		// For implied nodes, use the name they provide.
		if (getTypeClass().getTypeNode() instanceof ImpliedNode) {
			ImpliedNode in = (ImpliedNode) getTypeClass().getTypeNode();
			name = in.getImpliedType().getImpliedNodeType();
			// If the implied node is a union, add that to its assigned name
			if (in.getImpliedType().equals(ImpliedNodeType.Union))
				name += ": " + XsdModelingUtils.getAssignedXsdUnion(this);
			else if (in.getImpliedType().equals(ImpliedNodeType.UnassignedType))
				// add to the name a clue from the TL model of what should be assigned
				name += ": " + this.getModelObject().getTypeName();
		}
		return name == null ? "" : name;
	}

	@Override
	public String getTypeNameWithPrefix() {
		String typeName = getTypeName() == null ? "" : getTypeName();
		if (getAssignedType() == null)
			return "";
		if (getAssignedType() instanceof ImpliedNode)
			return typeName;
		if (getNamePrefix().equals(getAssignedPrefix()))
			return typeName;
		return getType().getNamePrefix() + " : " + typeName;
	}

	public ArrayList<Node> getNavChildren() {
		Node type = getTypeNode();
		ArrayList<Node> kids = new ArrayList<Node>();
		if (type != null) {
			// inherited properties do not have tNodes.
			kids.add(type);
			if (type.isAlias()) {
				// If it is an alias, list its object as well.
				kids.add(type.getParent());
			}
		}
		return kids;
	}

	@Override
	public Node getOwningComponent() {
		if (getParent() == null || getParent().getParent() == null)
			return this;
		if (getParent().isMessage())
			return getParent();
		if (getParent().isExtensionPointFacet())
			return getParent();
		// EnumLiterals are overridden.

		// Otherwise Properties are always owned by a facet.
		return getParent().getParent().isTypeProvider() ? getParent().getParent() : this;
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
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	public IValueWithContextHandler getEquivalentHandler() {
		return equivalentHandler;
	}

	@Override
	public String getEquivalent(String context) {
		return equivalentHandler != null ? equivalentHandler.get(context) : "";
	}

	public IValueWithContextHandler setEquivalent(String equivalent) {
		return null;
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	public IValueWithContextHandler getExampleHandler() {
		return exampleHandler;
	}

	@Override
	public String getExample(String context) {
		return exampleHandler != null ? exampleHandler.get(context) : "";
	}

	public IValueWithContextHandler setExample(String example) {
		return null;
	}

	/**
	 * Return images for properties.
	 */
	@Override
	public Image getImage() {
		throw new IllegalAccessError("Tried to get image from abstract property.");
		// return Images.getImageRegistry().get("file");
	}

	@Override
	public String getLabel() {
		return modelObject.getLabel() == null ? "" : modelObject.getLabel();
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

	@Override
	public boolean isAssignedComplexType() {
		// Need to test because inherited properties do not have assigned types.
		return getAssignedType() != null ? getAssignedType().isAssignedByReference() : false;
	}

	/**
	 * Move the property up/down in its current facet.
	 * 
	 * @param - direction (up/1 or down/2)
	 * @return false if it could not be moved (was at end of list of that type of properties).
	 */
	public static final int UP = 1;
	public static final int DOWN = 2;

	@Override
	public boolean moveProperty(final int direction) {
		if (!isEditable_newToChain())
			return false;
		// we don't have to sort children since their are always sorted
		if (direction == UP)
			return modelObject.moveUp(); // move the actual TL Property.
		else if (direction == DOWN)
			return modelObject.moveDown(); // move the actual TL Property.
		else {
			// LOGGER.warn("Do not understand direction: " + direction);
			return false;
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

	/**
	 * Remove this property from its parent facet and its TL Object from its TL Parent
	 */
	public void removeProperty() {
		this.unlinkNode();
		this.getModelObject().removeFromTLParent();
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
	public void setName(final String name) {
		setName(name, false); // Override family behavior
	}

	public void setMandatory(final boolean selection) {
		if (isEditable_newToChain())
			getModelObject().setMandatory(selection);
	}

	@Override
	public boolean isDeleteable() {
		if (modelObject == null)
			return false;
		if (getModelObject().getTLModelObj() instanceof TLnSimpleAttribute)
			return false;
		if (isInheritedProperty())
			return false;
		return super.isDeleteable();
	}

	@Override
	public boolean hasNavChildrenWithProperties() {
		return isProperty() && !(this instanceof IndicatorNode) && !(this instanceof EnumLiteralNode)
				&& !(this instanceof RoleNode) && modelObject != null && modelObject.getTLType() != null;
	}

	@Override
	public Node getAssignedType() {
		if (isInheritedProperty()) {
			// Inherited nodes are not assigned a type class. If they were the where-used count would be wrong.
			if (getInheritsFrom() != null) {
				return getInheritsFrom().getAssignedType();
			}
			return null;
		}
		return getTypeClass().getTypeNode();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getAssignedModelObject()
	 */
	@Override
	public ModelObject<?> getAssignedModelObject() {
		return getTypeClass().getTypeNode() != null ? getTypeClass().getTypeNode().getModelObject() : null;
	}

	/**
	 * @return true if this property is an attribute of a Value With Attributes object.
	 */
	public boolean isVWA_Attribute() {
		return getOwningComponent() instanceof VWA_Node;
	}

	@Override
	public boolean setAssignedType(Node replacement) {
		// return setAssignedType(replacement, false);
		if (replacement == null) {
			getTypeClass().setAssignedType(null);
			return false;
		}
		// GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases
		// are not TLAttributeType members so the GUI assist must convert before assignment.
		if (this instanceof AttributeNode && replacement instanceof AliasNode)
			replacement = replacement.getOwningComponent();

		// Valid assignment tests will be done in type node.
		return getTypeClass().setAssignedType(replacement);
	}

	@Override
	@Deprecated
	public void removeAssignedType() {
		getTypeClass().removeAssignedType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#sort()
	 */
	@Override
	public void sort() {
		getParent().sort();
	}

	/**
	 * @return the type QName from the TL model object or XSD assigned type.
	 */
	@Override
	public QName getTLTypeQName() {
		QName typeQname = null;

		NamedEntity type = getTLTypeObject();
		if (type != null) {
			String ns = type.getNamespace();
			String ln = type.getLocalName();
			if (ns != null && ln != null)
				typeQname = new QName(type.getNamespace(), type.getLocalName());
		} else
			// Try getting name from the TLModelObject typeName field.
			typeQname = getTLTypeNameField();

		// If still empty, try the XSD type information in the documentation
		if (typeQname == null)
			typeQname = XsdModelingUtils.getAssignedXsdType(this);

		return typeQname;
	}

	/**
	 * Only needed for facets (see JIRA 510). 4/15/2013 - no longer needed.
	 * 
	 * @return QName or null if not found and correct.
	 */
	// TODO: delete if not needed
	private QName getTLTypeNameField() {
		if (getTLModelObject() instanceof TLProperty) {
			TLProperty tlProp = (TLProperty) getTLModelObject();
			String tName = tlProp.getTypeName();
			// Use prefix if it has one to get namespace.
			if (tName != null) {
				int prefixIndex = tName.indexOf(":");
				String prefix = "", name = "", ns = "";
				if (prefixIndex > 0) {
					prefix = tName.substring(0, prefixIndex);
					name = tName.substring(prefixIndex + 1);
				} else
					name = tName;
				if (tlProp.getOwningLibrary() != null) {
					if (prefix.isEmpty())
						ns = tlProp.getOwningLibrary().getNamespace();
					else
						ns = tlProp.getOwningLibrary().getNamespaceForPrefix(prefix);
				}
				QName qName = new QName(ns, name);
				return qName;
			}
		}
		return null;
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

		if (getPropertyType().equals(toType))
			newProperty = this;
		else if (getParent() == null)
			newProperty = alternateRoles.oldOrNew(toType);
		else {
			if (getParent().isValidParentOf(toType)) {
				// List<Node> oldKids = new ArrayList(getParent().getChildren());
				// int childCount = getParent().getChildren().size();
				newProperty = alternateRoles.oldOrNew(toType);
				if (newProperty != null) {
					newProperty.copyDetails(this);
					if (getParent() != null)
						swap(newProperty);
				}
				// if (childCount != getParent().getChildren().size())
				// LOGGER.error("Change Property Role changed child count.");
			}
		}
		return newProperty == null ? this : newProperty;
	}

	public void swap(PropertyNode newProperty) {
		getParent().linkChild(newProperty, false);
		newProperty.modelObject.addChild(newProperty.getTLModelObject());
		getParent().getChildren().remove(this);
		if (!(getAssignedType() instanceof ImpliedNode)) {
			newProperty.setAssignedType(getAssignedType());
			getTypeClass().clearWhereUsed();
		}
		// Remove from current TL parent and add to new. Model object will ignore if no parent.
		modelObject.removeFromTLParent();
		getParent().getModelObject().addChild(newProperty.getTLModelObject());
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
		setIdentity(source.getIdentity());

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
			// Have to use type assigned not class because it is used in chained constructors
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

}
