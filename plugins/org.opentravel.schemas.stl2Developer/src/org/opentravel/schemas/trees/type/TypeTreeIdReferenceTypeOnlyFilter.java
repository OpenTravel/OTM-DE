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
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;

/**
 * Filter types for Id Reference Objects. For release 2.2 it will allow type assignment with list of VWA, Core and
 * Business objects and their aliases.
 * 
 * @author Pawel Jedruch
 * 
 */
public class TypeTreeIdReferenceTypeOnlyFilter extends TypeSelectionFilter {

	@Override
	public boolean isValidSelection(Node n) {
		return n instanceof VWA_Node || n instanceof CoreObjectNode || n instanceof BusinessObjectNode
				|| n instanceof AliasNode;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof Node)) {
			return false;
		}
		Node n = (Node) element;
		return isValidSelection(n) || hasValidChildren(n);
	}

}
