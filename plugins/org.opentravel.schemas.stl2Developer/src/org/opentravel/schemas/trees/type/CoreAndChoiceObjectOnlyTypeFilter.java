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

import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;

/**
 * Provide only business objects to the type selection wizard.
 * 
 * @author Dave
 *
 */
public class CoreAndChoiceObjectOnlyTypeFilter extends TypeSelectionFilter {

	private String targetNamespace = null;

	@Override
	public boolean isValidSelection(Node n) {
		return (n != null) && (n instanceof CoreObjectNode || n instanceof ChoiceObjectNode);
	}

	/**
	 * Filter to select only business object nodes.
	 * 
	 * @param namespace
	 *            null for any namespace or string namespace that must be matched
	 */
	public CoreAndChoiceObjectOnlyTypeFilter(String namespace) {
		targetNamespace = namespace;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element == null || !(element instanceof Node))
			return false;

		final Node n = (Node) element;
		if (n instanceof LibraryNode || n instanceof LibraryChainNode)
			return targetNamespace == null || n.getNamespace().equals(targetNamespace);
		if (n instanceof AggregateNode) // these extend NavNode
			return n instanceof VersionAggregateNode;
		if (n instanceof NavNode)
			return ((NavNode) n).isComplexRoot();

		if (n.isNavigation())
			return true;
		if (targetNamespace != null && !n.getNamespace().equals(targetNamespace))
			return false;
		return n instanceof CoreObjectNode || n instanceof ChoiceObjectNode;
	}
}
