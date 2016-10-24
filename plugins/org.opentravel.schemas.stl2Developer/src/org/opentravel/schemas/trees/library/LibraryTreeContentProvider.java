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

/**
 * Tree view content provider for the model navigator tree view.
 * It returns not only direct children, but also inherited and
 * property types. Inherited and property types may be filtered
 * out.
 * 
 * Deep mode can be set which will passed onto the getTreeChildren(deepMode). 
 * In this mode properties and assigned types are returned.
 * 
 * @author Dave Hollander
 */

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

public class LibraryTreeContentProvider implements ITreeContentProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTreeContentProvider.class);

	private boolean deepMode = false;

	public LibraryTreeContentProvider() {
		deepMode = false;
	}

	public LibraryTreeContentProvider(boolean deep) {
		deepMode = deep;
	}

	public boolean isDeepMode() {
		return deepMode;
	}

	public void setDeepMode(boolean deepMode) {
		this.deepMode = deepMode;
	}

	/**
	 * Get top level elements under the top library element - used for initial display Preconditions - we have library
	 * and it has a valid index.
	 */
	@Override
	public Object[] getElements(final Object element) {
		return getChildren(element);
	}

	/**
	 * getChildren is used when a node that hasChildren() is selected
	 */
	@Override
	public Object[] getChildren(final Object element) {
		if (element instanceof Node) {
			// LOGGER.debug("Getting children of ", ((Node) element));
			// List<Node> tKids = ((Node) element).getTreeChildren(false);
			// return tKids.toArray();
			return ((Node) element).getTreeChildren(deepMode).toArray();
		}
		throw new IllegalArgumentException("getChildren was not passed a node. Element is " + element);
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof Node)
			return ((Node) element).hasTreeChildren(deepMode);
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof Node)
			return ((INode) element).getParent();
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
	}

}
