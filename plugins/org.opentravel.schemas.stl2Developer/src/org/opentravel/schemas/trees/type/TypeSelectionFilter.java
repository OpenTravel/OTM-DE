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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeProviderAndOwners;

/**
 * Viewer filter that adds an additional method to verify that the selection chosen by the user is a valid type.
 * 
 * @author S. Livezey
 */
public class TypeSelectionFilter extends ViewerFilter {

	/**
	 * Returns true if the given node is a valid selection for the type selection page.
	 * 
	 * @param n
	 *            the node instance to evaluate
	 * @return boolean
	 */
	public boolean isValidSelection(Node n) {
		return n instanceof TypeProvider;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof Node) {
			final Node n = (Node) element;
			if (n instanceof VersionAggregateNode)
				return true;
			if (n instanceof AggregateNode) // must test before NavNode because these extend NavNode
				return false;
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the given node has one or more immediate children that would be considered valid selections by
	 * this filter.
	 * 
	 * @param n
	 *            the node to analyze
	 * @return boolean
	 */
	protected boolean hasValidChildren(Node n) {
		boolean hasValidChild = false;

		if (n instanceof AggregateNode) // these extend NavNode
			if (n instanceof VersionAggregateNode)
				return true;
			else
				return false;

		// Only the top level member of an xsd type can be used.
		if (n.isXsdType())
			return false;

		for (TypeProviderAndOwners child : n.getChildren_TypeProviders()) {
			if (isValidSelection((Node) child)) {
				hasValidChild = true;
				break;
			}
		}
		if (!hasValidChild && n instanceof ServiceNode) {
			for (Node child : n.getChildren()) {
				if (isValidSelection(child)) {
					hasValidChild = true;
					break;
				}
			}
		}

		// If we could not find an immediate child that was valid, recurse to see if any of the
		// deeper ancestors are valid
		if (!hasValidChild) {
			Set<TypeProviderAndOwners> children = new HashSet<TypeProviderAndOwners>(n.getChildren_TypeProviders());
			List<Node> navChildren = n.getNavChildren(false);

			// if (navChildren != null)
			// children.addAll(navChildren);

			// if (n instanceof LibraryNode) {
			// LibraryNode libNode = (LibraryNode) n;
			//
			// if (libNode.getServiceRoot() != null) {
			// children.add(libNode.getServiceRoot());
			// }
			// } else if (n instanceof ServiceNode) {
			// children.addAll(n.getChildren());
			// }

			for (TypeProviderAndOwners child : children) {
				if (hasValidChildren((Node) child)) {
					hasValidChild = true;
					break;
				}
			}
		}

		return hasValidChild;
	}

}
