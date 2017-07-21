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

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;

/**
 * Used for Custom Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class CustomFacetNode extends ContextualFacetNode {

	// Testing constructor
	public CustomFacetNode() {
		super(new TLContextualFacet());
		getTLModelObject().setFacetType(TLFacetType.CUSTOM);
	}

	public CustomFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		if (targetCF instanceof CustomFacetNode)
			return targetCF != this;
		return false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case CUSTOM:
			return true;
		default:
			return false;
		}
	}

	public void setOwner(ContextualFacetOwnerInterface owner) {
		// Add to TL Object
		TLContextualFacet tlFacet = getTLModelObject();
		tlFacet.setOwningEntity(owner.getTLModelObject());
		// tlFacet.setOwningLibrary(owner.getLibrary().getTLLibrary());
		// v1.5 and earlier throws ignored library member added event - NodeModelEventListener.
		if (owner.getTLModelObject() instanceof TLBusinessObject)
			((TLBusinessObject) owner.getTLModelObject()).addCustomFacet(tlFacet);
		super.add(owner, tlFacet);
	}

	@Override
	protected void addToTLParent(TLFacetOwner tlOwner) {
		if (tlOwner instanceof TLBusinessObject)
			((TLBusinessObject) tlOwner).addCustomFacet(getTLModelObject());
		else if (tlOwner instanceof TLContextualFacet)
			((TLContextualFacet) tlOwner).addChildFacet(getTLModelObject());
	}

	@Override
	protected void removeFromTLParent() {
		if (getTLModelObject().getOwningEntity() instanceof TLBusinessObject)
			((TLBusinessObject) getTLModelObject().getOwningEntity()).removeCustomFacet(getTLModelObject());
		else if (getTLModelObject().getOwningEntity() instanceof TLContextualFacet)
			((TLContextualFacet) getTLModelObject().getOwningEntity()).removeChildFacet(getTLModelObject());
	}
}
