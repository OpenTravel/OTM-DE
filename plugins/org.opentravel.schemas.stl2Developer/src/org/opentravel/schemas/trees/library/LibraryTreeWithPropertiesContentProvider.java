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
package org.opentravel.schemas.trees.library;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * 10/2016 dmh - replaced with deepMode on LibraryTreeContentProvider.
 * 
 * Used in new property wizard. Provides children and types assigned to properties.
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class LibraryTreeWithPropertiesContentProvider implements ITreeContentProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTreeContentProvider.class);

	private boolean includeInheritedChildren = false;

	public LibraryTreeWithPropertiesContentProvider(boolean includeInheritedChildren) {
		this.includeInheritedChildren = includeInheritedChildren;
	}

	@Override
	public Object[] getElements(final Object element) {
		return getChildren(element);
	}

	@Override
	public Object[] getChildren(final Object element) {
		if (element instanceof Node && ((Node) element).getChildrenHandler() != null) {
			final List<Node> nodeChildren = new ArrayList<>(
					((Node) element).getChildrenHandler().getTreeChildren(true));
			return nodeChildren.toArray();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof Node)
			return ((Node) element).hasTreeChildren(true);
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		return ((INode) element).getParent();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
	}

}
