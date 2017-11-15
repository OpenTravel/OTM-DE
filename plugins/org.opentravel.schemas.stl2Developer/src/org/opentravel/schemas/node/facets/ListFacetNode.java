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
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.handlers.children.ListFacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;

/**
 * Used for Detail and Summary List Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class ListFacetNode extends PropertyOwnerNode implements AliasOwner {

	public ListFacetNode(TLListFacet tlObj) {
		super(tlObj);

		childrenHandler = new ListFacetChildrenHandler(this);
	}

	@Override
	public void addAlias(AliasNode alias) {
		// if (!getTLModelObject().getAliases().contains(alias.getTLModelObject()))
		// getTLModelObject().addAlias(alias.getTLModelObject());
		getChildrenHandler().clear();
	}

	@Override
	public void remove(AliasNode alias) {
		// Not sure how-to or if i need-to associate this alias with owning compnent's
		getChildrenHandler().clear();
	}

	@Override
	public AliasNode addAlias(String name) {
		// NO-OP
		return null;
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		// NO-OP
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	@Override
	public boolean isSimpleListFacet() {
		return getTLModelObject().getFacetType().equals(TLFacetType.SIMPLE) ? true : false;
	}

	public boolean isDetailListFacet() {
		return getTLModelObject().getFacetType().equals(TLFacetType.DETAIL) ? true : false;
	}

	@Override
	@Deprecated
	public boolean isValidParentOf(PropertyNodeType type) {
		return false;
	}

	@Override
	public boolean isValidParentOf(PropertyNode pn) {
		return false;
	}

	@Override
	public boolean isSimpleAssignable() {
		return isSimpleListFacet();
	}

	@Override
	public TLListFacet getTLModelObject() {
		return (TLListFacet) tlObj;
		// return (TLListFacet) (getModelObject() != null ? getModelObject().getTLModelObj() : null);

	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject().getFacetType();
	}

	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
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

}
