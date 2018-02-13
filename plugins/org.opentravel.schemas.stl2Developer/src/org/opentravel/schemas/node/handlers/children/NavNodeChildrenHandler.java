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

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NavNode children handler using a static children handler for Node children and NavNode owners.
 * <p>
 * Design note: making this class non-generic simplifies access. If it was generic then instanceof tests do not work.
 * See {@link NavNode#add(Node)} for example.
 * 
 * @author Dave
 *
 */
public class NavNodeChildrenHandler extends StaticChildrenHandler<Node, NavNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NavNodeChildrenHandler.class);

	public NavNodeChildrenHandler(NavNode owner) {
		super(owner);
	}

	@Override
	public void add(Node item) {
		if (!children.contains(item))
			children.add(item);

		// Services are not versioned, so leave their parent and library unchanged
		if (owner instanceof AggregateNode && item instanceof ServiceNode)
			return;

		item.setParent(owner);
		// May be a member or a version node
		if (item instanceof LibraryMemberInterface)
			((LibraryMemberInterface) item).setLibrary(owner.getLibrary());
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

}
