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
package org.opentravel.schemas.node.handlers.children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetProviderChildrenHandler extends CachingChildrenHandler<Node, FacetProviderNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetProviderChildrenHandler.class);

	public FacetProviderChildrenHandler(final FacetProviderNode obj) {
		super(obj);
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		if (owner.getTLModelObject() == null)
			return Collections.emptyList();

		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.addAll(owner.getTLModelObject().getAttributes());
		kids.addAll(owner.getTLModelObject().getIndicators());
		kids.addAll(owner.getTLModelObject().getElements());
		kids.addAll(owner.getTLModelObject().getAliases());
		if (owner.getTLModelObject() instanceof TLContextualFacet)
			kids.addAll(((TLContextualFacet) owner.getTLModelObject()).getChildFacets());
		return kids;
	}

	/**
	 * Return just the inherited children.
	 */
	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		if (owner.getTLModelObject() == null)
			return Collections.emptyList();

		final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
		final List<?> declaredKids = getChildren_TL();

		// Make sure the facet owner is the extension facet owner to have codegen work.
		TLFacetOwner savedOwner = owner.getTLModelObject().getOwningEntity();
		if (owner.getParent().getTLModelObject() instanceof TLFacetOwner)
			owner.getTLModelObject().setOwningEntity(((TLFacetOwner) owner.getParent().getTLModelObject()));

		// owners returned are correct as the inherited facet
		for (TLAttribute attribute : PropertyCodegenUtils.getInheritedFacetAttributes(owner.getTLModelObject())) {
			if (!declaredKids.contains(attribute)) {
				inheritedKids.add(attribute);
			}
		}
		for (TLIndicator indicator : PropertyCodegenUtils.getInheritedFacetIndicators(owner.getTLModelObject())) {
			if (!declaredKids.contains(indicator)) {
				inheritedKids.add(indicator);
			}
		}
		for (TLProperty element : PropertyCodegenUtils.getInheritedFacetProperties(owner.getTLModelObject())) {
			if (!declaredKids.contains(element)) {
				inheritedKids.add(element);
			}
		}

		// Restore owner
		owner.getTLModelObject().setOwningEntity(savedOwner);
		return inheritedKids;
	}

}
