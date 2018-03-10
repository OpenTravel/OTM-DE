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

import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for Choice Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class ChoiceFacetNode extends ContextualFacetNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceFacetNode.class);

	/**
	 * Create a TLContextual facet set to choice and use it to create this choice facet node.
	 */
	public ChoiceFacetNode() {
		super(new TLContextualFacet());
		getTLModelObject().setFacetType(TLFacetType.CHOICE);
	}

	public ChoiceFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
	}

	@Override
	public boolean canOwn(AbstractContextualFacet targetCF) {
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
		return (TLContextualFacet) tlObj;
	}

	@Override
	protected void addToTLParent(ContextualFacetOwnerInterface owner) {
		if (owner == null || owner.getTLModelObject() == null)
			return;
		TLFacetOwner tlOwner = owner.getTLModelObject();
		if (tlOwner instanceof TLChoiceObject)
			((TLChoiceObject) tlOwner).addChoiceFacet(getTLModelObject()); // will ignore duplicates
		else if (tlOwner instanceof TLContextualFacet)
			((TLContextualFacet) tlOwner).addChildFacet(getTLModelObject());

		// Make sure the owner refreshes its children
		if (owner.getChildrenHandler() != null)
			owner.getChildrenHandler().clear();
	}

	@Override
	protected void removeFromTLParent() {
		if (getTLModelObject() == null)
			return;

		if (getTLModelObject().getOwningEntity() instanceof TLChoiceObject)
			((TLChoiceObject) getTLModelObject().getOwningEntity()).removeChoiceFacet(getTLModelObject());
		else if (getTLModelObject().getOwningEntity() instanceof TLContextualFacet)
			((TLContextualFacet) getTLModelObject().getOwningEntity()).removeChildFacet(getTLModelObject());
		if (getParent() != null)
			getParent().getChildrenHandler().clear();
		getTLModelObject().setOwningEntityName("");
	}

}
