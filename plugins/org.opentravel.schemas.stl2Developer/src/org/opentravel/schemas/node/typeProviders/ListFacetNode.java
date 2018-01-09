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

import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.ListFacetChildrenHandler;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;

/**
 * Used for Detail and Summary List Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class ListFacetNode extends FacetProviders {

	public ListFacetNode(TLListFacet tlObj) {
		super(tlObj);

		childrenHandler = new ListFacetChildrenHandler(this);
	}

	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		// NO-OP
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	@Override
	public void add(PropertyNode property) {
		// NO-OP
	}

	@Override
	public void add(PropertyNode pn, int index) {
		// NO-OP
	}

	@Override
	public boolean canOwn(PropertyNode pn) {
		return false;
	}

	@Override
	public boolean canOwn(PropertyNodeType type) {
		return false;
	}

	@Override
	public PropertyNode createProperty(Node type) {
		return null; // NO-OP
	}

	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject().getFacetType();
	}

	@Override
	public String getLabel() {
		if (isDetailListFacet())
			return ComponentNodeType.DETAIL_LIST.getDescription();
		return ComponentNodeType.SIMPLE_LIST.getDescription();
	}

	@Override
	public String getName() {
		return getTLModelObject().getLocalName();
	}

	@Override
	public TLListFacet getTLModelObject() {
		return (TLListFacet) tlObj;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	public boolean isDetailListFacet() {
		return getTLModelObject().getFacetType().equals(TLFacetType.DETAIL) ? true : false;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isSimpleAssignable() {
		return isSimpleListFacet();
	}

	// @Override
	public boolean isSimpleListFacet() {
		return getTLModelObject().getFacetType().equals(TLFacetType.SIMPLE) ? true : false;
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		// NO-OP
	}

}
