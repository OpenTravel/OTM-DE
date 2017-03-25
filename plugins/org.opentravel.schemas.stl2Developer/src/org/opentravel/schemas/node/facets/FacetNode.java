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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.StringComparator;

/**
 * Facets are containers for properties (elements and attributes) as well as simple facets for core and VWA objects.
 * Operation RQ, RS and Notif messages are also modeled with facets. (See library sorter for all the variations in facet
 * types.)
 * 
 * @author Dave Hollander
 * 
 */
public class FacetNode extends PropertyOwnerNode implements PropertyOwnerInterface, TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacetNode() {
	}

	public FacetNode(final TLFacet obj) {
		super(obj);
		addMOChildren();

		assert (modelObject instanceof FacetMO);
	}

	@Override
	public String getComponentType() {
		TLFacetType facetType = getTLModelObject().getFacetType();
		if (facetType == null)
			return "FIXME-delegate needed???";
		return facetType.getIdentityName();
	}

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) modelObject.getTLModelObj();
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject().getFacetType();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getPropertyRole() {
		return getComponentType();
	}

	@Override
	public String getLabel() {
		if (inherited)
			return getComponentType() + " (Inherited)";
		return getComponentType();
	}

	@Override
	public String getName() {
		return XsdCodegenUtils.getSubstitutableElementName(getTLModelObject()).getLocalPart();
	}

	@Override
	public boolean isDefaultFacet() {
		// FIXME - should never be VWA
		if (getOwningComponent() instanceof VWA_Node)
			return true;
		return getTLModelObject().getFacetType() == TLFacetType.SUMMARY;
	}

	public boolean isDetailFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.DETAIL) : false;
	}

	public boolean isIDFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.ID) : false;
	}

	@Override
	public boolean isNamedType() {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	}

	@Override
	public void sort() {
		Collections.sort(getChildren(), new StringComparator<Node>() {

			@Override
			protected String getString(Node object) {
				return object.getName();
			}
		});
		modelObject.sort();
	}

	/**
	 * Add nodes for all TLAliases that do not have nodes. This ONLY adds a node for an existing TLFacet. It does not
	 * create a TLFacet.
	 * 
	 * @param name
	 */
	public void updateAliasNodes() {
		List<String> knownAliases = new ArrayList<String>();
		for (Node n : getChildren()) {
			if (n instanceof AliasNode)
				knownAliases.add(n.getName());
		}
		// model object can be null for contributed facets
		if (getTLModelObject() != null)
			for (TLAlias tla : getTLModelObject().getAliases()) {
				if (!knownAliases.contains(tla.getName())) {
					new AliasNode(this, tla);
					knownAliases.add(tla.getName());
				}
			}
	}

}
