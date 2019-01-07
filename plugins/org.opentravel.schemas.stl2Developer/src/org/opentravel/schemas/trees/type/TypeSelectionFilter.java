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
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeProviderAndOwners;

/**
 * Viewer filter that adds an additional method to verify that the selection chosen by the user is a valid type.
 * 
 * @author S. Livezey
 */
//
// TODO - create JUnit test and use that to simplify and performane tune.
// Note - test must include variations of isValidSelection()
// Get rid of service node exception.
// Consider using getDescendents_TypeProviders() instead or is recursion used here better?
//
public class TypeSelectionFilter extends ViewerFilter {

	/**
	 * Returns true if the given node is a valid selection for the type selection page.
	 * <p>
	 * Called directly from TypeSelectionPage
	 * 
	 * @param n
	 *            the node instance to evaluate
	 * @return true if instanceof TypeProvider
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
			if (n instanceof NavNode)
				if (((NavNode) n).isEmpty())
					return false;
				else
					return ((NavNode) n).isComplexRoot() || ((NavNode) n).isSimpleRoot();

			return !(n instanceof ImpliedNode);
			// if (n instanceof ImpliedNode)
			// return false;
			// return true;
		}
		return false;
	}

	/**
	 * Returns true if the given node has one or more immediate children that would be considered valid selections by
	 * this filter.
	 * <p>
	 * Utility for use by other filters. Uses overridden isValidSelection() on all descendants (type providers or
	 * containers that can hold type providers), returns true on finding first valid selection is found.
	 * 
	 * @param n
	 *            the node to analyze
	 * @return boolean
	 */
	protected boolean hasValidChildren(Node n) {
		boolean hasValidChild = false;

		// these extend NavNode - only allow the version aggregates
		if (n instanceof AggregateNode)
			return n instanceof VersionAggregateNode;

		// Only the top level member of an xsd type can be used.
		if (n.isXsdType())
			return false;

		//
		// Scan children and descendants to see if any of them are valid
		for (TypeProviderAndOwners child : n.getChildren_TypeProviders()) {
			if (isValidSelection((Node) child)) {
				hasValidChild = true;
				break;
			}
		}
		// How can a service node have a valid selection? It only has properties not types.
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
			Set<TypeProviderAndOwners> children = new HashSet<>(n.getChildren_TypeProviders());
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
