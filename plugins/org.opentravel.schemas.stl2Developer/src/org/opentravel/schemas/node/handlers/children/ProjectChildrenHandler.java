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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;

/**
 * Project children handler using a static children handler for Library, Library Chain and LibraryNavNode children and
 * NavNode owners.
 * <p>
 * 
 * @author Dave
 *
 */
public class ProjectChildrenHandler extends StaticChildrenHandler<Node, ProjectNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ProjectChildrenHandler.class);

	public ProjectChildrenHandler(ProjectNode owner) {
		super(owner);
	}

	@Override
	public void add(Node item) {
		// Check to see if the library or library chain is already in the project
		assert item instanceof LibraryNavNode;
		LibraryNavNode lnn = (LibraryNavNode) item;
		for (Node child : children)
			if (lnn.get() == ((LibraryNavNode) child).get())
				return;
		if (!children.contains(item))
			children.add(item);
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
