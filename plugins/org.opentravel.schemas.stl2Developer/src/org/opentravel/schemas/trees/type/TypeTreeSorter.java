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

import org.eclipse.jface.viewers.ViewerSorter;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

/**
 * @author Dave Hollander
 * 
 */
public class TypeTreeSorter extends ViewerSorter {
	@Override
	public int category(final Object element) {
		// System.out.println("TypeTreeSorter:category() - n = "+((Node)element).getName());
		final Node n = (Node) element;
		if (n.isBuiltIn()) {
			return 3;
		}
		if (n.isXSDSchema()) {
			return 3;
		}
		if (n instanceof LibraryNode) {
			return 1;
		}
		return 0;
	}

}
