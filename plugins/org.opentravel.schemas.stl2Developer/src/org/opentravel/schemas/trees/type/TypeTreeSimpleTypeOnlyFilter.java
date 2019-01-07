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
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeProviders;

public class TypeTreeSimpleTypeOnlyFilter extends TypeSelectionFilter {

	/**
	 * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean isValidSelection(Node n) {
		return (n != null) && n.isAssignable() && n.isSimpleAssignable();
	}

	// /**
	// * Establish the filter to select only nodes that are navigation or isSimpleAssignable()==true.
	// */
	// public TypeTreeSimpleTypeOnlyFilter() {
	// }

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (!(element instanceof Node))
			return false;

		final Node n = (Node) element;
		if (n instanceof AggregateNode) // these extend NavNode
			return n instanceof VersionAggregateNode;
		if (n instanceof NavNode)
			return ((NavNode) n).isComplexRoot() || ((NavNode) n).isSimpleRoot();

		// // Temporary Patch
		// return (n.isNavigation()) ? true : n instanceof SimpleTypeNode;

		// This should be the real code when the compiler does not flag error when assigned.
		if (n instanceof ImpliedNode)
			return false;
		return n.isNavigation() || n instanceof SimpleTypeProviders;
		// return (n.isNavigation()) ? true : n instanceof SimpleTypeProviders;
	}
}
