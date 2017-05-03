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

import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.TLnValueWithAttributesFacet;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.PropertyNodeType;

/**
 * Used for Request, Response and Notification Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_AttributeFacetNode extends PropertyOwnerNode {

	public VWA_AttributeFacetNode(TLnValueWithAttributesFacet tlObj) {
		super(tlObj);

		assert (modelObject instanceof ValueWithAttributesAttributeFacetMO);
	}

	@Override
	public boolean isAssignable() {
		return false; // vwa facet can't be assigned independently of the VWA
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return getOwningComponent().isEnabled_AddProperties();
	}

	@Override
	public boolean isNamedEntity() {
		return false; // can't be assigned therefore is not a type provider
	}

	@Override
	public boolean isValidParentOf(PropertyNodeType type) {
		return PropertyNodeType.getVWA_PropertyTypes().contains(type);
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return hasNavChildren(deep); // override facet
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	@Override
	public String getNavigatorName() {
		return getFacetType().getIdentityName();
	}

	@Override
	public String getName() {
		return "Attributes";
		// return getParent().getName();
		// return emptyIfNull(getTLModelObject().getLocalName());
		// return getComponentType();
	}

	@Override
	public TLnValueWithAttributesFacet getTLModelObject() {
		return (TLnValueWithAttributesFacet) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.ATTRIBUTES.toString();
	}

}
