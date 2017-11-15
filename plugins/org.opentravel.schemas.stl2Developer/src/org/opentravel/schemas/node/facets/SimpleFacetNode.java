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
package org.opentravel.schemas.node.facets;

import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.SimpleFacetNodeListener;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets have only one attribute property. Used on ValueWithAtributes and Core Objects.
 * 
 * @author Dave Hollander
 * 
 */

// TODO
// The simple facet and simple attribute are handled differently between core and VWA.
// Find a base type and/or Interface that can rationalize them.
//

// In VWA it has a simple attribute node child
@Deprecated
public class SimpleFacetNode extends PropertyOwnerNode implements TypeProvider, SimpleAttributeOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	@Deprecated
	public SimpleFacetNode(TLSimpleFacet obj) {
		super(obj);
		assert false;
		// childrenHandler = new SimpleFacetFacadeChildrenHandler(this);

		// assert (modelObject instanceof SimpleFacetMO);
		// TLSimpleFacet - SimpleFacetMO
		// parent = Core Object Node, VWA
	}

	/**
	 * Set the type.
	 * 
	 * @param property
	 *            if a type provider it is assigned, if a property that property's type is assigned
	 * @param index
	 */
	public void addProperty(final Node property, int index) {
		if (property instanceof PropertyNode)
			((TypeUser) getChildren().get(0)).setAssignedType(((TypeUser) property).getAssignedType());
		else
			((TypeUser) getChildren().get(0)).setAssignedType((TypeProvider) property);
	}

	/**
	 * Create a new property and assign its type to the passed node. Simple facets should always have one child.
	 * 
	 * @param sn
	 * @return
	 */
	@Override
	public INode createProperty(final Node sn) {
		if (!getChildren().isEmpty()) {
			INode child = getChildren().get(0);
			if (child instanceof TypeUser && sn instanceof TypeProvider)
				((TypeUser) child).setAssignedType((TypeProvider) sn);
		} else {
			LOGGER.warn("Simple Facet (" + this + ")  does not have children.");
		}
		return null;
	}

	@Override
	public Node getAssignable() {
		return getChildren().get(0);
	}

	@Override
	public LibraryNode getLibrary() {
		if (getOwningComponent() == null || getOwningComponent() == this)
			return null;
		return getOwningComponent().getLibrary();
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new SimpleFacetNodeListener(this);
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		// 6/21/2013 - this was true in Node.java. I dont think it should be.
		return false;
	}

	@Override
	public void removeProperty(Node property) {
		LOGGER.debug("removeProperty() - Setting simple facet " + getName() + " to empty.");
		getSimpleAttribute().setAssignedType();
	}

	@Override
	public boolean isAssignedByReference() {
		// must override since super-type facet will return true.
		return false;
	}

	@Override
	public boolean isAssignableToSimple() {
		Node owner = getOwningComponent();
		if (owner instanceof CoreObjectNode)
			return !((CoreObjectNode) owner).getAssignedType().equals(ModelNode.getEmptyNode());
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	// TODO - where should this method be declared? higher than propertyNode
	// @Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getSimpleType() : null;
	}

	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return null;
		// return getChildren().isEmpty() ? null : (SimpleAttributeNode) getChildren().get(0);
	}

	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> kids = getNavChildren(deep);
		if (parent instanceof CoreObjectNode)
			kids.add(getWhereUsedNode());
		return kids;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		if (parent instanceof CoreObjectNode)
			return true; // where used node
		return deep;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return deep; // only show attribute in deep mode
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	@Override
	public String getName() {
		if (getParent() instanceof VWA_Node)
			return "Value";
		return emptyIfNull(getTLModelObject().getLocalName());
	}

	@Override
	public TLSimpleFacet getTLModelObject() {
		return (TLSimpleFacet) tlObj;
		// return (TLSimpleFacet) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
	}

}
