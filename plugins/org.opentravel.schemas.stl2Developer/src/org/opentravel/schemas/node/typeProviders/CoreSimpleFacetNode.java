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
/**
 * 
 */
package org.opentravel.schemas.node.typeProviders;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.CoreSimpleFacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.CoreSimpleAttributeFacadeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Simple facets have only one attribute property.
 * <p>
 * Unlike a VWA simple facet, core simple facets can be assigned as a type and have a matching TLObject.
 * <p>
 * The TLSimpleFacet does not have attribute child but rather has a simpleType property. A SimpleAttributeFacade is
 * created to represent this simple type.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreSimpleFacetNode extends TypeProviders implements FacetInterface, SimpleAttributeOwner {
	// public class Core_SimpleFacetFacadeNode extends SimpleFacetFacadeNode
	// implements TypeProvider, SimpleAttributeOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetOMNode.class);

	public CoreSimpleFacetNode(CoreObjectNode owner) {
		parent = owner;

		// Set the tlObj to be the TLCore's simple list facet,
		tlObj = owner.getTLModelObject().getSimpleFacet();
		// Set the tlObj to get the listener correct
		ListenerFactory.setIdentityListner(this, tlObj);

		// TLObj with listener is needed to create child
		childrenHandler = new CoreSimpleFacetChildrenHandler(this);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		// NO-OP
	}

	// // @Override
	public SimpleAttributeFacadeNode createAttributeFacade() {
		return new CoreSimpleAttributeFacadeNode(this);
	}

	// @Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getSimpleType() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.facets.PropertyOwnerNode#getComponentType()
	 */
	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
	}

	// @Override
	// public boolean isAssignableToSimple() {
	// return !getParent().getAssignedType().equals(ModelNode.getEmptyNode());
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.facets.PropertyOwnerNode#getFacetType()
	 */
	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getLabel() {
		return getFacetType().toString();
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getLocalName());
	}

	@Override
	public CoreObjectNode getParent() {
		return (CoreObjectNode) parent;
	}

	@Override
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return (SimpleAttributeFacadeNode) childrenHandler.get().get(0);
	}

	@Override
	public TLSimpleFacet getTLModelObject() {
		return (TLSimpleFacet) tlObj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#copy(org.opentravel.schemas.node.interfaces.FacetInterface)
	 */
	@Override
	public void copy(FacetInterface facet) {
		// NO-OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#add(java.util.List, boolean)
	 */
	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		// NO-OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode)
	 */
	@Override
	public void add(PropertyNode property) {
		// NO-OP
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
		// NO-OP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public PropertyNode createProperty(Node type) {
		// NO-OP
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#findChildByName(java.lang.String)
	 */
	@Override
	public PropertyNode findChildByName(String name) {
		return getSimpleAttribute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#get(java.lang.String)
	 */
	@Override
	public PropertyNode get(String name) {
		return getSimpleAttribute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#getProperties()
	 */
	@Override
	public List<PropertyNode> getProperties() {
		return Collections.singletonList((PropertyNode) getSimpleAttribute());
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return getParent();
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return type.equals(TLFacetType.SIMPLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.SimpleAttributeOwner#getAssignedType()
	 */
	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.SimpleAttributeOwner#setAssignedType(org.opentravel.schemas.types.TypeProvider)
	 */
	@Override
	public boolean setAssignedType(TypeProvider type) {
		return getSimpleAttribute().setAssignedType(type);
	}
}
