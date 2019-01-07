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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;

/**
 * Type selection filter that only allow the selection of a particular type of model object. This is used during the
 * selection of extensions since business objects can only extend business objects, cores can only extend cores, etc.
 * <p>
 * 10/18/2012 - this behavior is being removed: Extendible only impacts compiler; it does not imply "final". In addition
 * to the type of object being filtered on, the entities themselves must be extendible in order for this filter to
 * return an affirmative condition.
 * 
 * @author S. Livezey, D. Hollander
 */
public class TypeTreeExtensionSelectionFilter extends TypeSelectionFilter {

	private Node filterNode = null;
	private boolean exNode = false;

	/**
	 * Constructor that specifies the type of object to be visible when the filter is applied.
	 * 
	 * @param filter
	 *            the type of node to match
	 */
	public TypeTreeExtensionSelectionFilter(Node filter) {
		this.filterNode = filter;
		if (filter instanceof ExtensionPointNode)
			exNode = true;
	}

	@Override
	public boolean isValidSelection(Node n) {
		boolean isValid = false;
		// boolean isValid = n.isNavigation();
		if (!exNode) {
			if (n instanceof LibraryMemberInterface)
				isValid = filterNode.getClass().equals(n.getClass());
		} else if (n != null) {
			if (exNode == false || n instanceof FacetInterface)
				if (filterNode instanceof ExtensionPointNode)
					if (((FacetInterface) n).isExtensionPointTarget())
						// XP Facets must select extensions in a different namespace
						isValid = n.getNamespace() != null && !n.getNamespace().equals(filterNode.getNamespace());
					else
						isValid = filterNode.getClass().equals(n.getClass());
		}
		return isValid;

	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof Node))
			return false;

		Node n = (Node) element;
		// boolean result;
		if (n == filterNode)
			return false;
		else
			return isValidSelection(n) || hasValidChildren(n);
	}

}
