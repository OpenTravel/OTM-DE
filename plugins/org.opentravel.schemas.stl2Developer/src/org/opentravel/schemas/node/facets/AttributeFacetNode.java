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
package org.opentravel.schemas.node.facets;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.AttributeFacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.FacadeBase;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.VWA_Node;

/**
 * Property owner that can contain only attributes and indicators.
 * <p>
 * It is a facade because there is no underlying TLfacet, just the parent's tlModelObject.
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeFacetNode extends FacadeBase implements FacetInterface {

	private VWA_Node owner;

	public AttributeFacetNode(VWA_Node owner) {
		super();
		parent = owner;
		this.owner = owner;
		tlObj = owner.getTLModelObject();
		childrenHandler = new AttributeFacetChildrenHandler(this);
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	@Override
	public LibraryNode getLibrary() {
		return getParent().getLibrary();
	}

	@Override
	public boolean isAssignable() {
		return false; // vwa facet can't be assigned independently of the VWA
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return getOwningComponent().isEnabled_AddProperties();
	}

	@Override
	public boolean isNamedEntity() {
		return false; // can't be assigned therefore is not a type provider
	}

	@Override
	public boolean canOwn(PropertyNodeType type) {
		return PropertyNodeType.getVWA_PropertyTypes().contains(type);
	}

	@Override
	public boolean canOwn(PropertyNode pn) {
		return PropertyNodeType.getVWA_PropertyTypes().contains(pn.getPropertyType());
	}

	@Override
	public VWA_Node get() {
		return owner;
	}

	@Override
	public String getNavigatorName() {
		return getFacetType().getIdentityName();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public String getName() {
		return "Attributes";
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) tlObj;
	}

	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getLabel() {
		return ComponentNodeType.ATTRIBUTES.toString();
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.ATTRIBUTES.toString();
	}

	/**
	 * @param facet
	 *            containing properties to clone and add to this facet.
	 */
	@Override
	@Deprecated
	// Use add(list, true)
	public void copy(FacetInterface facet) {
		add(facet.getProperties(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#add(java.util.List, boolean)
	 */
	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		for (PropertyNode np : properties) {
			if (clone)
				np = np.clone(this, null); // add to clone not parent
			else
				add(np);
			assert this.contains(np);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode)
	 */
	@Override
	public void add(PropertyNode property) {
		add(property, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode,
	 * int)
	 */
	@Override
	public void add(PropertyNode pn, int index) {
		// Add to children list
		pn.setParent(this);

		// Add to the tl model
		if (index < 0)
			pn.addToTL(this);
		else
			pn.addToTL(this, index);

		// Events are not being thrown (10/14/2017) so force their result
		// childrenHandler.clear();
		// clear handlers on any inherited "ghost" facets
		for (ModelElementListener l : getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener)
				((InheritanceDependencyListener) l).run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public PropertyNode createProperty(Node type) {
		// TODO
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#findChildByName(java.lang.String)
	 */
	@Override
	public PropertyNode findChildByName(String name) {
		return get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#get(java.lang.String)
	 */
	@Override
	public PropertyNode get(String name) {
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode && n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#getProperties()
	 */
	@Override
	public List<PropertyNode> getProperties() {
		List<PropertyNode> pns = new ArrayList<PropertyNode>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode)
				pns.add((PropertyNode) n);
		return pns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#isFacet(org.opentravel.schemacompiler.model.TLFacetType)
	 */
	@Override
	public boolean isFacet(TLFacetType facetType) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#removeProperty(org.opentravel.schemas.node.properties.
	 * PropertyNode)
	 */
	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof AttributeNode)
			getTLModelObject().removeAttribute(((AttributeNode) pn).getTLModelObject());
	}

	/**
	 * 
	 */
	public void sort() {
		assert false;
		// TODO Auto-generated method stub

	}

}
