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
package org.opentravel.schemas.node.typeProviders;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.FacetOwners;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.StringComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FacetsProviderNode are containers for properties (elements, indicators and attributes) that can be used as types.
 * These facets include ID, Summary and Detail. Sub-types are used for contextual facets.
 * 
 * @author Dave Hollander
 * 
 */
public class FacetProviderNode extends FacetProviders implements Sortable {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetProviderNode.class);

	public FacetProviderNode() {
		childrenHandler = new FacetProviderChildrenHandler(this);
	}

	public FacetProviderNode(final TLFacet obj) {
		super(obj);

		childrenHandler = new FacetProviderChildrenHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public void add(final List<PropertyNode> properties, boolean clone) {
		for (PropertyNode np : properties) {
			if (clone)
				np = np.clone(this, null); // add to clone not parent
			if (canOwn(np.getPropertyType()))
				add(np);
		}
	}

	@Override
	public void add(PropertyNode property) {
		add(property, -1);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isExtensionPointTarget() {
		return true;
	}

	@Override
	public void add(final PropertyNode pn, final int index) {
		// Add to children list
		pn.setParent(this);

		// Add to the tl model
		if (index < 0)
			pn.addToTL(this);
		else
			pn.addToTL(this, index);

		// // Events are not being thrown (10/14/2017) so force their result
		// childrenHandler.clear();
		// clear handlers on any inherited "ghost" facets
		for (ModelElementListener l : getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener)
				((InheritanceDependencyListener) l).run();
	}

	@Override
	public boolean canOwn(PropertyNode pn) {
		return canOwn(pn.getPropertyType());
	}

	@Override
	public boolean canOwn(PropertyNodeType type) {
		if (type == null)
			return false;

		switch (type) {
		case ELEMENT:
		case ATTRIBUTE:
		case INDICATOR:
		case ID:
		case ID_ATTR_REF:
		case ID_REFERENCE:
		case INDICATOR_ELEMENT:
			return true;
		default:
			return false;
		}
	}

	@Override
	public PropertyNode createProperty(Node type) {
		// Assume that all FacetProviders can have elements
		// FIXME - only does single behavior, not assign type if un-typed
		TypedPropertyNode pn = null;
		if (this.canOwn(PropertyNodeType.ELEMENT))
			pn = new ElementNode(new TLProperty(), this);
		else
			pn = new AttributeNode(new TLAttribute(), this);
		pn.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			pn.setAssignedType((TypeProvider) type);
		pn.setName(type.getName());
		this.add(pn);
		getChildrenHandler().clear();
		return pn;
	}

	/**
	 * Get the property with the passed name or null.
	 * 
	 * @param string
	 * @return
	 */
	@Override
	public PropertyNode get(String name) {
		for (Node n : getChildren())
			if (n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public FacetProviderChildrenHandler getChildrenHandler() {
		return (FacetProviderChildrenHandler) childrenHandler;
	}

	@Override
	public String getComponentType() {
		return getTLModelObject().getFacetType().toString();
	}

	/**
	 * Use the owning component to find the extension base. Then find the child with the same type. Will <b>not</b> find
	 * inherited facet from version base.
	 */
	@Override
	public Node getExtendsType() {
		assert !(this instanceof AbstractContextualFacet); // MUST be overridden
		LibraryMemberInterface owner = getOwningComponent();
		if (!(owner instanceof ExtensionOwner))
			return null;
		FacetInterface baseFacet = null;
		Node baseOwner = ((Node) owner).getExtendsType();
		if (baseOwner instanceof FacetOwners)
			baseFacet = ((FacetOwners) baseOwner).getFacet(this.getTLModelObject().getFacetType());
		return (Node) baseFacet;
	}

	/**
	 * Get the version base from the parent then find this facet's match
	 * 
	 * @return
	 */
	public FacetInterface getVersionBase() {
		if (this instanceof AbstractContextualFacet)
			return null; // should override
		FacetInterface baseFacet = null;
		if (getOwningComponent().getVersionNode() != null) {
			Node baseOwner = getOwningComponent().getVersionNode().getPreviousVersion();
			// Works for non-contextual facets
			if (baseOwner instanceof FacetOwners)
				baseFacet = ((FacetOwners) baseOwner).getFacet(this.getTLModelObject().getFacetType());
		}
		return baseFacet;
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject() != null ? getTLModelObject().getFacetType() : null;
	}

	@Override
	public String getLabel() {
		if (isInherited())
			return getComponentType() + " (Inherited)";
		return getComponentType();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getName() {
		// codegenUtils need an owning entity to not throw error
		if (getTLModelObject() == null) {
			LOGGER.debug("Missing tl object.");
			return "";
		}
		if (getTLModelObject().getOwningEntity() == null) {
			LOGGER.debug("Missing owning entity.");
			return getComponentType();
		}
		return XsdCodegenUtils.getSubstitutableElementName(getTLModelObject()).getLocalPart();
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		// Node p = null;
		// do
		// p = getParent();
		// while (!(p instanceof LibraryMemberInterface) && p != null);
		return (LibraryMemberInterface) getParent();
	}

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) tlObj;
	}

	/**
	 * Only Contextual facets are deletable.
	 */
	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof ElementNode)
			getTLModelObject().removeProperty(((ElementNode) pn).getTLModelObject());
		else if (pn instanceof AttributeNode)
			getTLModelObject().removeAttribute(((AttributeNode) pn).getTLModelObject());
		else if (pn instanceof IndicatorNode)
			getTLModelObject().removeIndicator(((IndicatorNode) pn).getTLModelObject());
		getChildrenHandler().clear();
		pn.setParent(null);
	}

	/**
	 * Set name to type users where this alias is assigned. Only if the parent is type that requires assigned users to
	 * use the owner's name
	 */
	@Override
	public void setNameOnWhereAssigned(String n) {
		if (getParent() instanceof TypeProvider && !((TypeProvider) getParent()).isRenameableWhereUsed())
			for (TypeUser u : getWhereAssigned())
				u.setName(n);
	}

	@Override
	public void sort() {

		// sort the TL lists
		getTLModelObject().sortIndicators(new StringComparator<TLIndicator>() {
			@Override
			protected String getString(TLIndicator object) {
				return object.getName();
			}
		});
		getTLModelObject().sortAttributes(new StringComparator<TLAttribute>() {
			@Override
			protected String getString(TLAttribute object) {
				return object.getName();
			}
		});
		getTLModelObject().sortElements(new StringComparator<TLProperty>() {
			@Override
			protected String getString(TLProperty object) {
				return object.getName();
			}
		});

		getChildrenHandler().clear();
	}

}
