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
import org.opentravel.schemas.modelObject.ExtensionPointFacetMO;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * Type selection filter that only allow the selection of a particular type of model object. This is used during the
 * selection of extensions since business objects can only extend business objects, cores can only extend cores, etc.
 * 10/18/2012 - this behavior is being removed: Extendible only impacts compiler; it does not imply "final". In addition
 * to the type of object being filtered on, the entities themselves must be extendible in order for this filter to
 * return an affirmative condition.
 * 
 * @author S. Livezey
 */
public class TypeTreeExtensionSelectionFilter extends TypeSelectionFilter {

	private ModelObject<?> modelObject;
	private Class<? extends ModelObject<?>> extensionType;

	/**
	 * Constructor that specifies the type of model object to be visible when the filter is applied.
	 * 
	 * @param modelObjectType
	 *            the type of model object that should be
	 */
	// TODO - make this use Nodes not ModelObjects
	@SuppressWarnings("unchecked")
	public TypeTreeExtensionSelectionFilter(ModelObject<?> modelObject) {
		this.modelObject = modelObject;

		if (modelObject instanceof ExtensionPointFacetMO) {
			extensionType = FacetMO.class;
		} else {
			extensionType = (Class<? extends ModelObject<?>>) modelObject.getClass();
		}
	}

	/**
	 * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean isValidSelection(Node n) {
		boolean isValid = false;

		// Do same as commented out below using Nodes not MO
		// 11/10/2016 dmh
		if (n != null) {
			INode thisNode = this.modelObject.getNode();
			if ((extensionType == null) || extensionType.equals(n.getModelObject().getClass())) {
				if (n instanceof ExtensionPointNode)
					// XP Facets must select extensions in a different namespace
					isValid = n.getNamespace() != null && !n.getNamespace().equals(thisNode.getNamespace());
				else
					isValid = true;
			}
		}

		// if (n != null) {
		// ModelObject<?> modelObject = n.getModelObject();
		//
		// if ((extensionType == null) || extensionType.equals(modelObject.getClass())) {
		// if (this.modelObject instanceof ExtensionPointFacetMO) {
		// // XP Facets must select extensions in a different namespace
		// // if (n.getParent().getModelObject().isExtendable()) {
		// isValid = (n.getNamespace() != null) && !n.getNamespace().equals(this.modelObject.getNamespace());
		// // }
		// } else {
		// // commented out to allow extensions even if base is not extend-able.
		// // isValid = modelObject.isExtendable();
		// isValid = true;
		// }
		// }
		// }
		return isValid;
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
		boolean result;

		if (n.getModelObject() == modelObject) {
			result = false;
		} else {
			result = isValidSelection(n) || hasValidChildren(n);
		}
		return result;
	}

}
