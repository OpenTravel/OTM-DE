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

import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.Node;

/**
 * Type selection filter that only allow the selection of later versions of the same object.
 * 
 * @author Dave Hollander
 */
public class TypeTreeVersionSelectionFilter extends TypeSelectionFilter {

	private Node node;
	private List<Node> versions = null;
	private List<Node> ancestors = new ArrayList<Node>();

	// private ModelObject<?> modelObject;
	// private Class<? extends ModelObject<?>> extensionType;

	/**
	 * Constructor that specifies the type of model object to be visible when the filter is applied.
	 * 
	 * @param modelObjectType
	 *            the type of model object that should be
	 */
	@SuppressWarnings("unchecked")
	public TypeTreeVersionSelectionFilter(Node node) {
		this.node = node;
		versions = node.getLaterVersions();
		for (Node v : versions)
			ancestors.addAll(v.getAncestors());

		// if (node instanceof ExtensionPointFacetMO) {
		// extensionType = FacetMO.class;
		// } else {
		// extensionType = (Class<? extends ModelObject<?>>) node.getClass();
		// }
	}

	/**
	 * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean isValidSelection(Node n) {
		return versions.contains(n);
		// boolean isValid = false;
		//
		// if (n != null) {
		// ModelObject<?> modelObject = n.getModelObject();
		//
		// if ((extensionType == null) || extensionType.equals(modelObject.getClass())) {
		// if (this.modelObject instanceof ExtensionPointFacetMO) {
		// // XP Facets must select extensions in a different namespace
		// // if (n.getParent().getModelObject().isExtendable()) {
		// isValid = (n.getNamespace() != null)
		// && !n.getNamespace().equals(this.modelObject.getNamespace());
		// // }
		// } else {
		// // commented out to allow extensions even if base is not extend-able.
		// // isValid = modelObject.isExtendable();
		// isValid = true;
		// }
		// }
		// }
		// return isValid;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element == null || !(element instanceof Node)) {
			return false;
		}
		Node n = (Node) element;
		return versions.contains(n) || ancestors.contains(n);
		// boolean result;
		//
		// if (n.getModelObject() == modelObject) {
		// result = false;
		// } else {
		// result = isValidSelection(n) || hasValidChildren(n);
		// }
		// return result;
	}

}
