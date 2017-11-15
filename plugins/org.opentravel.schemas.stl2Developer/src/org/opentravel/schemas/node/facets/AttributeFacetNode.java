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

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.handlers.children.AttributeFacetChildrenHandler;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;

/**
 * Property owner that can contain only attributes and indicators. It is a facade because there is no TLfacet underlying
 * it, just the parent's tlModelObject.
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeFacetNode extends PropertyOwnerNode {

	public AttributeFacetNode(VWA_Node owner) {
		super();
		parent = owner;
		tlObj = owner.getTLModelObject();
		childrenHandler = new AttributeFacetChildrenHandler(this);
	}

	@Override
	public LibraryNode getLibrary() {
		return getParent().getLibrary();
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
	@Deprecated
	public boolean isValidParentOf(PropertyNodeType type) {
		return PropertyNodeType.getVWA_PropertyTypes().contains(type);
	}

	@Override
	public boolean isValidParentOf(PropertyNode pn) {
		return PropertyNodeType.getVWA_PropertyTypes().contains(pn.getPropertyType());
	}

	@Override
	public String getNavigatorName() {
		return getFacetType().getIdentityName();
	}

	@Override
	public String getName() {
		return "Attributes";
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) tlObj;
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
