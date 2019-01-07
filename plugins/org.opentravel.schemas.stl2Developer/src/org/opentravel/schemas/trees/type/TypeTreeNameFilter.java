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
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * Used in type trees to filter out non-matching named objects.
 * 
 * @author dmh
 *
 */
public class TypeTreeNameFilter extends ViewerFilter {
	private String txtFilter = "";

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (txtFilter == null || txtFilter.isEmpty())
			return true;

		if (!(element instanceof Node))
			return false;

		final Node n = (Node) element;
		if (shouldBeDisplayed(n))
			return true;

		return childMatches(n);
	}

	private boolean childMatches(final INode cn) {
		for (final Node n : cn.getChildren()) {
			if (childMatches(n)) {
				return true;
			}
			if (shouldBeDisplayed(n)) {
				return true;
			}
		}
		return false;
	}

	private boolean shouldBeDisplayed(final Node cn) {
		return (textMatches(cn) && cn.isAssignable()) || (cn.getParent() != null && textMatches(cn.getParent()));
	}

	private boolean textMatches(final INode cn) {
		// return cn.getName().matches(txtFilter);
		return cn.getName().toLowerCase().contains(txtFilter);
	}

	public void setText(final String txt) {
		// txtFilter = "(?i)*" + txt + "*";
		if (txt == null) {
			txtFilter = "";
		} else {
			txtFilter = txt.toLowerCase();
		}
	}
}
