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
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all property node. Subtypes add abilities to have type assigned and change from one role to another.
 * <p>
 * Property nodes control element (property), attribute and indicator property model objects Simple Attributes and
 * others. (See PropertyNodeType: Role, Literal, Alias, Simple)
 * 
 * Properties extends components by giving them the ability change nature (Indicator, attribute, element) without losing
 * data unique to each one. To do this, it has extra model objects. Also used for RoleProperty nodes.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class PropertyNode extends ComponentNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyNode.class);

	/**
	 * Move the property up/down in its current facet.
	 * 
	 * @param -
	 *            direction (up/1 or down/2)
	 * @return false if it could not be moved (was at end of list of that type of properties).
	 */
	public static final int UP = 1;
	public static final int DOWN = 2;

	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;
	protected TypeUserHandler typeHandler = null;
	public ConstraintHandler constraintHandler = null;
	protected PropertyRoleChangeHandler changeHandler = null;

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
	protected PropertyNode(final TLModelElement tlObj, FacetInterface parent) {
		super(tlObj);

		if (parent != null)
			parent.add(this);

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
	protected PropertyNode(final TLModelElement tlObj, final FacetInterface parent, final String name) {
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
	public void addToTL(FacetInterface owner) {
		addToTL(owner, -1);
	}

	// TODO - refactor to sub-types
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
	public abstract void addToTL(final FacetInterface owner, final int index);

	// TODO - make sure all sub-type impls call super first
	@Override
	public abstract boolean canAssign(Node type);

	/**
	 * Replace this property with one of the specified type. Uses the saved alternateRole or creates a new property. All
	 * values that can be copied will be. The old property is saved. All copies share the same alternateRoles instance.
	 * <p>
	 * The parent is checked to assure it can own the requested type.If not, ATTRIBUTE is used as toType.
	 * 
	 * @param toType
	 * @param parent
	 *            - optional property checked to assure it can own the toType. getParent() used if omitted.
	 * @return the new property, or this property if change could not be made.
	 */
	public PropertyNode changePropertyRole(PropertyNodeType toType) {
		if (getChangeHandler() != null)
			return getChangeHandler().changePropertyRole(toType);
		return this;
	}

	public PropertyNode changePropertyRole(PropertyNodeType toType, FacetInterface parent) {
		if (getChangeHandler() != null)
			return getChangeHandler().changePropertyRole(toType, parent);
		return this;
	}

	/**
	 * Clone the TLModelObject and create clone node using factory. Assign clone to parent. Set whereUsed on assigned
	 * type.
	 * 
	 * @param parent
	 *            FacetInterface to add clone to
	 * @param nameSuffix
	 *            added at end of name if not null
	 */
	@Override
	public PropertyNode clone(Node parent, String nameSuffix) {
		// sub-types must assign type
		PropertyNode clone = null;
		// if (parent instanceof FacetInterface) {
		LibraryElement clonedTL = getTLModelObject().cloneElement();
		if (clonedTL instanceof TLModelElement) {
			clone = (PropertyNode) NodeFactory.newChild(parent, (TLModelElement) clonedTL);
			assert clone instanceof PropertyNode;
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);

		}
		if (parent instanceof FacetInterface)
			((FacetInterface) parent).add(clone);
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
	 * @param -
	 *            the type to assign.
	 */
	@Override
	public INode createProperty(final Node type) {
		Node n = null;
		return n;
	}

	protected INode createProperty(final PropertyNode clone, final Node type) {
		// sub-type use this after making clone
		clone.addToTL((FacetInterface) getParent(), indexOfNode());
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
		// delegate to type handling sub-classes to assign type
		if (isDeleted() || getParent() == null)
			return;
		deleteTL();
		deleted = true;
	}

	@Override
	public void deleteTL() {
		if (getTLModelObject() != null) {
			removeFromTL();
			ListenerFactory.clearListners(getTLModelObject()); // remove any listeners
			getParent().getChildrenHandler().clear(); // tell parent to update list
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

	public PropertyRoleChangeHandler getChangeHandler() {
		return changeHandler;
	}

	public void setChangeHandler(PropertyRoleChangeHandler handler) {
		changeHandler = handler;
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
		List<Node> kids = new ArrayList<>();
		return kids;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new NodeIdentityListener(this);
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		if (getParent() == null)
			return null;
		return getParent().getOwningComponent();
	}

	@Override
	public abstract Node getParent();

	/**
	 * Get the parent from the passed TLModelElement's identity listener. Should be called by all children of facets
	 * because the parent may have failed to rebuild children Create change handler if it is not present.
	 * 
	 * @param owner
	 *            the TLModelElement of the property owner
	 * @return
	 */
	public Node getParent(TLModelElement owner, boolean createChangeHandler) {
		if ((parent == null || parent.isDeleted()) && getTLModelObject() != null)
			parent = Node.GetNode(owner);
		if (createChangeHandler && parent instanceof FacetInterface && changeHandler == null)
			changeHandler = new PropertyRoleChangeHandler(this, parent);
		return parent;
	}

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
	}

	/**
	 * Overridden to provide assigned type and aliases if deep is set.
	 */
	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	/**
	 * True if it has assigned type
	 */
	@Override
	public abstract boolean hasNavChildren(boolean deep);
	// {
	// return false;
	// }

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
		// return getAssignedType() != null ? getAssignedType().isAssignedByReference() : false;
		return false;
	}

	@Override
	public boolean isDeleteable() {
		if (getTLModelObject() == null)
			return false;
		if (this instanceof FacadeInterface)
			// Core simple attribute facade node
			return false;
		if (isInherited())
			return false;
		return super.isDeleteable();
	}

	@Override
	public boolean isEnabled_AddProperties() {
		if (getOwningComponent() == null)
			return false;
		return this != getOwningComponent() ? getOwningComponent().isEnabled_AddProperties() : false;
	}

	/**
	 * Properties can only be a navigation child in deep mode. Non-typed properties override with false.
	 */
	@Override
	public abstract boolean isNavChild(boolean deep);
	// {
	// return deep;
	// }

	// Override if not re-nameable.
	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited();
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
	public void moveProperty(FacetInterface newFacet) {
		removeProperty();
		((ComponentNode) newFacet).addProperty(this);
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
		setParent(null);
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
	public void swap(PropertyNode newProperty) {
		if (getParent() instanceof FacetInterface)
			newProperty.addToTL((FacetInterface) getParent());
		removeProperty();
	}

	public void setMandatory(final boolean selection) {
		// Override where supported
	}

	protected abstract void moveDownTL();

	protected abstract void moveUpTL();

	protected abstract void removeFromTL();

}
