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
 * @author Dave Hollander
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryTreeContentProvider implements ITreeContentProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTreeContentProvider.class);

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
			Node node = (Node) element;
			// if (node.isDeleted()) return null;

			List<Node> navChildren = new ArrayList<Node>();
			navChildren.addAll(node.getNavChildren());
			if (node.isNamedType())
				navChildren.add(node.getTypeClass().getTypeTreeNode());
			navChildren.addAll(node.getInheritedChildren());
			return navChildren != null ? navChildren.toArray() : null;

			// Include the types assigned to properties -- these may be filtered out

			// breaks: node instanceof AggregateNode ||
			// TODO - This should be based on overrides not if tests.
			// if ( node instanceof PropertyNode || node instanceof VersionAggregateNode
			// || node instanceof VersionNode) {
			// navChildren.addAll(node.getNavChildren());
			// } else {
			// navChildren.addAll(node.getChildren());
			//
			// // Additional view tree node providers supplying node that are
			// // not part of the node model tree
			// if (node.isNamedType())
			// navChildren.add(node.getTypeClass().getTypeTreeNode());
			// navChildren.addAll(node.getInheritedChildren());
			//
			// // 3/11/2015 dmh - moved logic to overriden method in ModelNode class.
			// // if (node instanceof ModelNode) {
			// // if (!navChildren.contains(ModelNode.getUnassignedNode())) {
			// // navChildren.add(ModelNode.getUnassignedNode());
			// // // Enhancement - enable duplicate display. Needs controls to keep it up to
			// // // date with changes throughout the model before enabling.
			// // //
			// // // navChildren.add(ModelNode.getDuplicateTypesNode());
			// // }
			// // }
			// }
			// return navChildren != null ? navChildren.toArray() : null;
			// } else if (element instanceof TypeNode) {
			// ((TypeNode) element).getChildren();
		} else
			throw new IllegalArgumentException("getChildren was not passed a node. Element is " + element);
		// return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;
			boolean ret = false;
			// Do not display under version nodes unless user selects the node or it has been
			// extended. This allows focus refreshes to work correctly.
			if ((Node) element instanceof VersionNode)
				return false;
			return node.hasNavChildrenWithProperties() ? true : node.hasInheritedChildren();
		}
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof Node) {
			return ((INode) element).getParent();
		}
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
	}

}
