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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.types.TypeProviderAndOwners;

/**
 * Content provider used to gather tree elements for the extension selection wizard.
 * 
 * @author S. Livezey
 */
public class ExtensionTreeContentProvider implements ITreeContentProvider {

	public ExtensionTreeContentProvider() {
	}

	@Override
	public Object[] getElements(final Object element) {
		return getChildren(element);
	}

	@Override
	public Object[] getChildren(final Object element) {
		List<TypeProviderAndOwners> children = new ArrayList<TypeProviderAndOwners>();
		Node n = (Node) element;

		children.addAll(n.getChildren_TypeProviders());

		// if (n instanceof LibraryNode) {
		// LibraryNode libNode = (LibraryNode) n;
		//
		// if (libNode.getServiceRoot() != null) {
		// children.add(libNode.getServiceRoot());
		// }
		// } else if (n instanceof ServiceNode) {
		// children.addAll(n.getChildren());
		// }
		return children.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang. Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		return (getElements(element).length > 0);
	}

	@Override
	public Object getParent(final Object element) {
		return ((INode) element).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface .viewers.Viewer, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

}
