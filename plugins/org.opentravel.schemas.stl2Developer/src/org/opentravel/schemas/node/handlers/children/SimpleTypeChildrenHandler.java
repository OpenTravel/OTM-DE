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

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleType children handler using a static children handler overrides getTreeChildren to provide where used node.
 * 
 * @author Dave
 *
 */
public class SimpleTypeChildrenHandler extends StaticChildrenHandler<Node, SimpleTypeNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeChildrenHandler.class);

	public SimpleTypeChildrenHandler(SimpleTypeNode owner) {
		super(owner);
	}

	@Override
	public void add(Node item) {
	}

	/**
	 * Add a tree child for the where used node
	 */
	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> kids = new ArrayList<>();
		if (owner.getWhereUsedCount() > 0)
			kids.add(owner.getWhereUsedNode());
		return kids;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	protected void initInherited() {
	}

	/**
	 * Override to provide where used when appropriate. Needed because this object has no navChildren.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return owner.getWhereUsedCount() > 0 ? true : false;
	}

}
