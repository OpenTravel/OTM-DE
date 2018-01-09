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
package org.opentravel.schemas.node.objectMembers;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * Used for Request, Response and Notification Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class OperationFacetNode extends FacetOMNode {

	public OperationFacetNode(TLFacet tlObj) {
		super(tlObj);
	}

	// @Override
	// public boolean isDeleteable() {
	// return super.isDeleteable(true);
	// }

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

	@Override
	public boolean isExtensionPointTarget() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#isFacet(org.opentravel.schemacompiler.model.TLFacetType)
	 */
	@Override
	public boolean isFacet(TLFacetType facetType) {
		switch (facetType) {
		case NOTIFICATION:
		case REQUEST:
		case RESPONSE:
			return true;
		default:
			break;
		}
		return false;
	}

}
