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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.SimpleFacetFacadeChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.SimpleFacetNodeListener;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets have only one attribute property. Used on ValueWithAtributes and Core Objects.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class SimpleFacetFacadeNode extends PropertyOwnerNode implements TypeProvider, SimpleAttributeOwner,
		FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public SimpleFacetFacadeNode(Node owner) {
		super(); // no listeners set
		parent = owner;
		childrenHandler = new SimpleFacetFacadeChildrenHandler(this);
	}

	public void addProperty(final Node property, int index) {
	}

	/**
	 * Create and return a facade node for the correct simple attribute.
	 * 
	 * @return
	 */
	public abstract SimpleAttributeFacadeNode createAttributeFacade();

	@Override
	public INode createProperty(final Node sn) {
		return null;
	}

	@Override
	public Node get() {
		return Node.GetNode(getTLModelObject());
	}

	@Override
	public Node getAssignable() {
		return getChildren().get(0);
	}

	@Override
	public LibraryNode getLibrary() {
		return getParent().getLibrary();
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
	public abstract boolean isAssignableToSimple();

	// {
	// Node owner = getOwningComponent();
	// if (owner instanceof CoreObjectNode)
	// return !((CoreObjectNode) owner).getSimpleType().equals(ModelNode.getEmptyNode());
	// return true;
	// }

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	// TODO - where should this method be declared? higher than propertyNode
	// @Override
	public abstract NamedEntity getAssignedTLNamedEntity();

	/**
	 * This maps to the TLVWA parent.
	 */
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		if (getChildren().isEmpty())
			return null;
		return (SimpleAttributeFacadeNode) getChildren().get(0);
		// return getChildren().isEmpty() ? ModelNode.getEmptyType() : (SimpleAttributeFacadeNode) getChildren().get(0);
	}

	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	// @Override
	// public List<Node> getTreeChildren(boolean deep) {
	// List<Node> kids = getNavChildren(deep);
	// if (parent instanceof CoreObjectNode)
	// kids.add(getWhereUsedNode());
	// return kids;
	// }
	//
	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// if (parent instanceof CoreObjectNode)
	// return true; // where used node
	// return deep;
	// }

	@Override
	public boolean hasNavChildren(boolean deep) {
		return deep; // only show attribute in deep mode
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	@Override
	public abstract String getName();

	@Override
	public abstract TLModelElement getTLModelObject();

	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
	}

}
