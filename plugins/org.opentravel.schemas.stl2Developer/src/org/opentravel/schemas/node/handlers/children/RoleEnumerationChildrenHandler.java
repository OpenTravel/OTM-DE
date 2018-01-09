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
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class RoleEnumerationChildrenHandler extends CachingChildrenHandler<Node, RoleFacetNode> {

	public RoleEnumerationChildrenHandler(final RoleFacetNode obj) {
		super(obj);
	}

	public List<TLRole> getChildren_TLRoles() {
		return owner.getTLModelObject().getRoles();
	}

	// // It does not seem like roles are inherited! There was no mention of them in the doc and there are no codegen
	// utils.
	// @Override
	// public List<TLModelElement> getInheritedChildren_TL() {
	// final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
	// // // false prevents Codegen utils from returning non-inherited values
	// inheritedKids.addAll(EnumCodegenUtils.getInheritedValues(owner.getTLModelObject(), false));
	// return inheritedKids;
	// }

	@Override
	public List<TLModelElement> getChildren_TL() {
		List<TLModelElement> roles = new ArrayList<TLModelElement>();
		for (TLRole r : getChildren_TLRoles())
			roles.add(r);
		return roles;
	}

}
