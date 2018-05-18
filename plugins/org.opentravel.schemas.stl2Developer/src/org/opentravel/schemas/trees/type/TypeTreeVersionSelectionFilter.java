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
package org.opentravel.schemas.trees.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNavNode;

/**
 * Type selection filter that only allow the selection of later versions of the same object.
 * 
 * @author Dave Hollander
 */
public class TypeTreeVersionSelectionFilter extends TypeSelectionFilter {

	// private Node node;
	private List<Node> versions = null;
	private List<Node> ancestors = new ArrayList<>();

	/**
	 * Constructor that specifies the type of model object to be visible when the filter is applied.
	 * 
	 * @param modelObjectType
	 *            the type of model object that should be
	 */
	@SuppressWarnings("unchecked")
	public TypeTreeVersionSelectionFilter(Node node) {
		// this.node = node;
		if (node != null)
			versions = node.getLaterVersions();
		if (versions != null)
			for (Node v : versions)
				ancestors.addAll(v.getAncestors());
	}

	/**
	 * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean isValidSelection(Node n) {
		return versions.contains(n);
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element == null || !(element instanceof Node)) {
			return false;
		}
		Node n = (Node) element;
		if (versions == null)
			return false;

		if (n instanceof LibraryNavNode)
			n = ((LibraryNavNode) n).get();

		boolean ans = versions.contains(n) || ancestors.contains(n);
		return versions.contains(n) || ancestors.contains(n);
	}

}
