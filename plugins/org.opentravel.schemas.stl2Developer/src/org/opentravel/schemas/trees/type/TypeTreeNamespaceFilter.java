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
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;

public class TypeTreeNamespaceFilter extends ViewerFilter {
	private Class<? extends AbstractLibrary> library = null;

	public TypeTreeNamespaceFilter() {
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (library == null) {
			return true;
		}
		if (element == null || !(element instanceof Node)) {
			return false;
		}
		if (element instanceof ProjectNode)
			return true; // they contain libraries to be checked.
		final Node n = (Node) element;
		if (n.getLibrary() == null)
			return false; // could be implied
		final AbstractLibrary libClass = n.getLibrary().getTLModelObject();
		final boolean isInstance = library.isInstance(libClass);
		return isInstance;
	}

	public void setLibrary(final Class<? extends AbstractLibrary> lib) {
		library = lib;
	}
}
