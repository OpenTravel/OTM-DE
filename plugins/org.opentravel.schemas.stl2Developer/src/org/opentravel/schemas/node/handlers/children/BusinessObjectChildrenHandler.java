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
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;

public class BusinessObjectChildrenHandler extends CachingChildrenHandler<Node, BusinessObjectNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectChildrenHandler.class);

	public BusinessObjectChildrenHandler(BusinessObjectNode owner) {
		super(owner);
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		final List<TLModelElement> kids = new ArrayList<>();
		kids.add(owner.getTLModelObject().getIdFacet());
		kids.add(owner.getTLModelObject().getSummaryFacet());
		kids.add(owner.getTLModelObject().getDetailFacet());
		kids.addAll(owner.getTLModelObject().getQueryFacets());
		kids.addAll(owner.getTLModelObject().getCustomFacets());
		kids.addAll(owner.getTLModelObject().getUpdateFacets());
		kids.addAll(owner.getTLModelObject().getAliases());
		return kids;
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		TLFacetOwner trueOwner = FacetCodegenUtils.getFacetOwnerExtension(owner.getTLModelObject());
		if (trueOwner == null)
			return Collections.emptyList();

		List<TLModelElement> facets = new ArrayList<>();
		for (TLContextualFacet cf : getInheritedChildren_TL_CFs()) {
			facets.add(cf);
		}
		return facets;
	}

	/**
	 * <b>NOTE</b> - these are newly created facets with no listeners
	 * <p>
	 * They may have the wrong owner giving them the wrong name
	 * 
	 * @return
	 */
	private List<TLContextualFacet> getInheritedChildren_TL_CFs() {
		List<TLContextualFacet> tlCfs = new ArrayList<>();
		tlCfs.addAll(FacetCodegenUtils.findGhostFacets(owner.getTLModelObject(), TLFacetType.CUSTOM));
		tlCfs.addAll(FacetCodegenUtils.findGhostFacets(owner.getTLModelObject(), TLFacetType.QUERY));
		tlCfs.addAll(FacetCodegenUtils.findGhostFacets(owner.getTLModelObject(), TLFacetType.UPDATE));
		return tlCfs;
	}

}
