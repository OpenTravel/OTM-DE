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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.PropertyNode;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTreeNameFilter extends ViewerFilter {

	private String txtFilter = "";
	private boolean exactFiltering = false;

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (txtFilter == null || txtFilter.isEmpty()) {
			return true;
		}
		if (element == null || !(element instanceof Node)) {
			return false;
		}
		final INode n = (INode) element;
		if (shouldBeDisplayed(n)) {
			return true;
		}
		return childMatches(n);
	}

	// 3/30/2015 dmh - found stack overflow in log file
	private boolean childMatches(final INode cn) {
		for (final INode n : cn.getChildren()) {
			if (n != cn && childMatches(n)) {
				return true;
			}
			if (shouldBeDisplayed(n)) {
				return true;
			}
		}
		return false;
	}

	private boolean shouldBeDisplayed(final INode cn) {
		return textMatches(cn) || assignmentMatches(cn);
	}

	private boolean textMatches(final INode cn) {
		if (cn.getName() == null) {
			return false;
		}
		if (isExactFiltering()) {
			return cn.getName().toLowerCase().startsWith(txtFilter);
		}
		return cn.getName().toLowerCase().contains(txtFilter);
	}

	private boolean assignmentMatches(final INode cn) {
		boolean matches = false;
		if (!isExactFiltering() && cn instanceof PropertyNode) {
			PropertyNode p = (PropertyNode) cn;
			String assignedType = p.getTypeNameWithPrefix();
			matches = assignedType != null && assignedType.toLowerCase().contains(txtFilter);
		}
		return matches;
	}

	public void setText(final String txt) {
		if (txt == null) {
			txtFilter = "";
		} else {
			txtFilter = txt.toLowerCase();
		}
	}

	/**
	 * @return the exactFiltering
	 */
	public boolean isExactFiltering() {
		return exactFiltering;
	}

	/**
	 * @param exactFiltering
	 *            if true filter will consider only nodes which names start with filter text; otherwise filter will also
	 *            try to match any names that contain (not only start with) given text and also assigned types (whether
	 *            their names match filter)
	 */
	public void setExactFiltering(boolean exactFiltering) {
		this.exactFiltering = exactFiltering;
	}
}
