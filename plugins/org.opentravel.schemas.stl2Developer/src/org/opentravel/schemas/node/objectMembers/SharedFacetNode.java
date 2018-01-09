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
package org.opentravel.schemas.node.objectMembers;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.facetOwners.FacetOwners;

/**
 * Used for Choice Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class SharedFacetNode extends FacetOMNode {

	/**
	 * Create a TLContextual facet set to choice and use it to create this choice facet node.
	 */
	public SharedFacetNode() {
		super(new TLFacet());
		getTLModelObject().setFacetType(TLFacetType.SHARED);
	}

	public SharedFacetNode(TLFacet facet) {
		super(facet);
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return type.equals(TLFacetType.SHARED);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	/**
	 * Use the owning component to find the extension base. Then find the child with the same type.
	 */
	@Override
	public Node getExtendsType() {
		LibraryMemberInterface owner = getOwningComponent();
		if (!(owner instanceof ExtensionOwner))
			return null;
		FacetInterface baseFacet = null;
		Node baseOwner = ((Node) owner).getExtendsType();
		if (baseOwner instanceof FacetOwners)
			baseFacet = ((FacetOwners) baseOwner).getFacet(TLFacetType.SHARED);
		return (Node) baseFacet;
	}

	/**
	 * Return true if this facet can contain the passed property node.
	 */
	@Override
	public boolean canOwn(PropertyNode pn) {
		return true;
	}

	/**
	 * Return true if this facet can contain the passed property node type.
	 */
	@Override
	public boolean canOwn(PropertyNodeType type) {
		return true;
	}

	// @Override
	// public boolean canOwn(ContextualFacetNode targetCF) {
	// return false;
	// }
	//
	// @Override
	// public boolean canOwn(TLFacetType type) {
	// switch (type) {
	// case CHOICE:
	// return true;
	// default:
	// return false;
	// }
	// }

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) tlObj;
	}

	// @Override
	// public void setOwner(ContextualFacetOwnerInterface owner) {
	// addToTLParent(owner.getTLModelObject());
	// super.add(owner, getTLModelObject()); // Adds to owning object's library!
	// }

	// @Override
	// protected void addToTLParent(TLFacetOwner tlOwner) {
	// if (tlOwner instanceof TLChoiceObject)
	// ((TLChoiceObject) tlOwner).addChoiceFacet(getTLModelObject()); // will ignore duplicates
	// else if (tlOwner instanceof TLContextualFacet)
	// ((TLContextualFacet) tlOwner).addChildFacet(getTLModelObject());
	// }
	//
	// @Override
	// protected void removeFromTLParent() {
	// if (getTLModelObject().getOwningEntity() instanceof TLChoiceObject)
	// ((TLChoiceObject) getTLModelObject().getOwningEntity()).removeChoiceFacet(getTLModelObject());
	// else if (getTLModelObject().getOwningEntity() instanceof TLContextualFacet)
	// ((TLContextualFacet) getTLModelObject().getOwningEntity()).removeChildFacet(getTLModelObject());
	// }

}
