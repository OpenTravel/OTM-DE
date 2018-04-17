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
package org.opentravel.schemas.trees.REST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.resources.ResourceNode;

/**
 * Tree content provider for the REST Resource View.
 * 
 * @author Dave Hollander
 * 
 */
public class RestTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(final Object element) {
		List<Node> kids = new ArrayList<>();
		if (element instanceof LibraryChainNode)
			kids = ((LibraryChainNode) element).getResourceAggregate().getChildrenHandler().getTreeChildren(false);
		else if (element instanceof NavNode && ((NavNode) element).isResourceRoot())
			kids = ((Node) element).getChildren();
		return kids.toArray();
	}

	@Override
	public Object[] getChildren(final Object element) {
		List<Node> navChildren = null;
		Node node = null;
		if (element instanceof Node)
			node = (Node) element;
		if (element instanceof VersionNode)
			node = ((VersionNode) element).get();

		if (node instanceof ResourceNode)
			navChildren = ((ResourceNode) node).getTreeChildren(); // no nav children for navigator menu
		else if (node instanceof ResourceMemberInterface)
			navChildren = node.getNavChildren(true);

		return navChildren != null ? navChildren.toArray() : Collections.EMPTY_LIST.toArray();
	}

	@Override
	public boolean hasChildren(final Object element) {
		Node node = null;
		if (element instanceof VersionNode)
			node = ((VersionNode) element).get();
		else if (element instanceof Node)
			node = (Node) element;

		if (node instanceof ResourceNode)
			return !node.getChildren().isEmpty();
		if (node instanceof ResourceMemberInterface)
			return !node.getNavChildren(true).isEmpty();
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		Node node = null;
		if (element instanceof VersionNode)
			node = ((VersionNode) element).get();
		if (node instanceof ResourceMemberInterface)
			return node.getParent();
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
	}

}
