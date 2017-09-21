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

import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;

/**
 * Used for Choice Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class ChoiceFacetNode extends ContextualFacetNode {

	/**
	 * Create a TLContextual facet set to choice and use it to create this choice facet node.
	 */
	public ChoiceFacetNode() {
		super(new TLContextualFacet());
		((TLContextualFacet) getTLModelObject()).setFacetType(TLFacetType.CHOICE);
	}

	public ChoiceFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
		assert (modelObject instanceof FacetMO);
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		if (targetCF instanceof ChoiceFacetNode)
			return targetCF != this;
		return false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case CHOICE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return (TLContextualFacet) modelObject.getTLModelObj();
	}

	@Override
	public void setOwner(ContextualFacetOwnerInterface owner) {
		TLContextualFacet tlFacet = getTLModelObject();
		tlFacet.setOwningEntity(owner.getTLModelObject());
		// Do NOT set Owning library because that the is owner of the facet not the library of the facet owner.
		// tlFacet.setOwningLibrary(owner.getLibrary().getTLLibrary());
		if (owner.getTLModelObject() instanceof TLChoiceObject)
			((TLChoiceObject) owner.getTLModelObject()).addChoiceFacet(tlFacet);
		else if (owner.getTLModelObject() instanceof TLContextualFacet)
			((TLContextualFacet) owner.getTLModelObject()).addChildFacet(tlFacet);

		super.add(owner, tlFacet); // Adds to owning object's library!
	}

	@Override
	protected void addToTLParent(TLFacetOwner tlOwner) {
		if (tlOwner instanceof TLChoiceObject)
			((TLChoiceObject) tlOwner).addChoiceFacet(getTLModelObject());
		else if (tlOwner instanceof TLContextualFacet)
			((TLContextualFacet) tlOwner).addChildFacet(getTLModelObject());
	}

	@Override
	protected void removeFromTLParent() {
		if (getTLModelObject().getOwningEntity() instanceof TLChoiceObject)
			((TLChoiceObject) getTLModelObject().getOwningEntity()).removeChoiceFacet(getTLModelObject());
		else if (getTLModelObject().getOwningEntity() instanceof TLContextualFacet)
			((TLContextualFacet) getTLModelObject().getOwningEntity()).removeChildFacet(getTLModelObject());
	}

}
