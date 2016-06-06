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
/**
 * 
 */
package org.opentravel.schemas.trees.library;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.PropertyNode;

/**
 * Filter out types assigned to properties by not selecting nodes whose parents are properties. This filter is initially
 * on for the model navigator view.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryPropertyOnlyFilter extends ViewerFilter {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(LibraryTreeContentProvider.class);

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if ((parentElement instanceof Node) && (element instanceof Node)) {
			final Node n = (Node) parentElement;
			// LOGGER.debug( "Is node property type? "+n.isProperty());
			if (n instanceof PropertyNode)
				return false;
			else
				return true;
		}
		return false;
	}

}
