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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contextual Facets.
 * 
 * These facets extend a business or choice object. They may or may not be in the same library
 * 
 * Contextual facet extends TLFacet and TLModelElement (not TLLibraryMember)
 * 
 * @author Dave Hollander
 * 
 */
public class UpdateFacetNode extends ContextualFacetNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFacetNode.class);

	// Testing constructor
	public UpdateFacetNode() {
		super(new TLContextualFacet());
	}

	public UpdateFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		if (targetCF instanceof UpdateFacetNode)
			return targetCF != this;
		return false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case UPDATE:
			return true;
		default:
			return false;
		}
	}

	public void setOwner(ContextualFacetOwnerInterface owner) {
		TLContextualFacet newFacet = getTLModelObject();
		// newFacet.setOwningEntity(owner.getTLModelObject());
		// newFacet.setOwningLibrary(owner.getLibrary().getTLLibrary());
		if (owner.getTLModelObject() instanceof TLBusinessObject)
			((TLBusinessObject) owner.getTLModelObject()).addUpdateFacet(newFacet);
		else if (owner.getTLModelObject() instanceof TLContextualFacet)
			((TLContextualFacet) owner.getTLModelObject()).addChildFacet(newFacet);

		super.add(owner, newFacet);
	}

	@Override
	protected void addToTLParent(TLFacetOwner tlOwner) {
		if (tlOwner instanceof TLBusinessObject)
			((TLBusinessObject) tlOwner).addUpdateFacet(getTLModelObject());
		else if (tlOwner instanceof TLContextualFacet)
			((TLContextualFacet) tlOwner).addChildFacet(getTLModelObject());
	}

	@Override
	protected void removeFromTLParent() {
		if (getTLModelObject().getOwningEntity() instanceof TLBusinessObject)
			((TLBusinessObject) getTLModelObject().getOwningEntity()).removeUpdateFacet(getTLModelObject());
		else if (getTLModelObject().getOwningEntity() instanceof TLContextualFacet)
			((TLContextualFacet) getTLModelObject().getOwningEntity()).removeChildFacet(getTLModelObject());
	}

}
