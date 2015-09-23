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
package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Used for Custom and Query Facets.
 * 
 * @author Pawel Jedruch
 * 
 */
public class RenamableFacet extends FacetNode {

	public RenamableFacet(TLFacet tlObj) {
		super(tlObj);
	}

	public String getContext() {
		return ((TLFacet) getTLModelObject()).getContext();
	}

	/**
	 * Set the context for this renamable facet. If context is null, then set to the default context for the library.
	 */
	public void setContext(String context) {
		if (context == null)
			context = getLibrary().getDefaultContextId();
		((TLFacet) getTLModelObject()).setContext(context);
	}

	@Override
	public void setName(String n) {
		String name = n;
		// Strip the object name and "query" string if present.
		name = NodeNameUtils.stripFacetPrefix(this, name);
		if (getModelObject() != null) {
			// compiler doesn't allow empty context -
			// ((TLFacet) getTLModelObject()).setContext("");
			((TLFacet) getTLModelObject()).setLabel(name);
			// rename their type users as well.
			for (Node user : getTypeUsers()) {
				user.setName(getName());
			}
		}
	}

}
