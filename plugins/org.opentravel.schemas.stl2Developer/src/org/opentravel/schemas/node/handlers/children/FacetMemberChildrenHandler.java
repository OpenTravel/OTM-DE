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

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetMemberChildrenHandler extends CachingChildrenHandler<Node, FacetOMNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetMemberChildrenHandler.class);

	public FacetMemberChildrenHandler(final FacetOMNode obj) {
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
				// inheritedOwner = (TLModelElement) attribute.getOwner();
			}
		}
		for (TLIndicator indicator : PropertyCodegenUtils.getInheritedFacetIndicators(owner.getTLModelObject())) {
			if (!declaredKids.contains(indicator)) {
				inheritedKids.add(indicator);
				// inheritedOwner = (TLModelElement) indicator.getOwner();
			}
		}
		for (TLProperty element : PropertyCodegenUtils.getInheritedFacetProperties(owner.getTLModelObject())) {
			if (!declaredKids.contains(element)) {
				inheritedKids.add(element);
				// inheritedOwner = (TLModelElement) element.getOwner();
			}
		}

		// Restore owner
		owner.getTLModelObject().setOwningEntity(savedOwner);
		return inheritedKids;
	}

	@Override
	protected void initInherited() {
		// if (owner instanceof SharedFacetNode) {
		// super.initInherited();
		// return;
		// }
		if (owner.getExtendsType() != null) {
			super.initInherited();
			return;
		}

		// FIXME - implement getExtenedType() then use super-type method
		Node et = owner.getExtendsType();
		if (owner.getExtendsType() == null)
			return;
		assert false;
		// SharedFacetNode
		initRunning = true;
		String fName = "";

		// Use the utils to find the actual base facet
		TLFacetOwner tlBaseFacetOwner = null;
		TLFacetOwner ptlfo = null;
		if (owner.getParent().getTLModelObject() instanceof TLFacetOwner)
			ptlfo = (TLFacetOwner) owner.getParent().getTLModelObject();
		// else
		// LOGGER.warn("Invalid facet owner.");
		if (ptlfo != null)
			tlBaseFacetOwner = FacetCodegenUtils.getFacetOwnerExtension(ptlfo);

		// use the utils to find the matching facet in the base object
		TLFacet tlBaseFacet = null;
		TLFacet tlf = owner.getTLModelObject();
		if (tlf instanceof TLContextualFacet)
			fName = ((TLContextualFacet) tlf).getName();
		if (tlBaseFacetOwner != null)
			tlBaseFacet = FacetCodegenUtils.getFacetOfType(tlBaseFacetOwner, tlf.getFacetType(), fName);

		if (tlBaseFacet == null && owner.getInheritedFrom() != null)
			LOGGER.warn("Matching facet should have been found.");

		// Hold on to the matching base facet
		inheritedOwner = Node.GetNode(tlBaseFacet);

		// get the inherited children and model them
		if (inheritedOwner != null)
			inherited = modelTLs(getInheritedChildren_TL(), inheritedOwner);
		else
			inherited = Collections.emptyList();

		initRunning = false;
	}
}
