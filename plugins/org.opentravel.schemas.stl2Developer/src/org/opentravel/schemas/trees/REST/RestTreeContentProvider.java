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
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;

/**
 * Tree content provider for the REST Resource View.
 * 
 * @author Dave Hollander
 * 
 */
public class RestTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(final Object element) {
		List<Node> kids = new ArrayList<Node>();
		if (element instanceof NavNode && ((NavNode) element).isResourceRoot())
			kids = ((Node) element).getChildren();
		return kids.toArray();
	}

	@Override
	public Object[] getChildren(final Object element) {
		List<Node> navChildren = null;
		if (element instanceof ResourceMemberInterface)
			navChildren = ((Node) element).getChildren();
		return navChildren != null ? navChildren.toArray() : Collections.EMPTY_LIST.toArray();
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof ResourceMemberInterface)
			return !((Node) element).getChildren().isEmpty();
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof ResourceMemberInterface)
			return ((Node) element).getParent();
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
	}

}
