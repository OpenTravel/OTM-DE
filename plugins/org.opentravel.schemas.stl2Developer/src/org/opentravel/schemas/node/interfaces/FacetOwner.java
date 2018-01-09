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
package org.opentravel.schemas.node.interfaces;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;

/**
 * Nodes implementing this interface have structural facets (attributes, summary, detail, default).
 * 
 * @author Dave Hollander
 * 
 */
public interface FacetOwner extends ComplexMemberInterface {

	/**
	 * @param facetType
	 * @return
	 */
	public FacetInterface getFacet(TLFacetType facetType);

	/**
	 * @return null or the attribute facet
	 */

	public AttributeFacetNode getFacet_Attributes(); // VWA only

	/**
	 * @return null or the default facet
	 */
	public FacetInterface getFacet_Default();

	/**
	 * @return null or the detail facet
	 */
	public FacetProviderNode getFacet_Detail();

	/**
	 * @return null or the detail facet
	 */
	public FacetProviderNode getFacet_ID();

	/**
	 * @return the simple facet or null if none.
	 */
	public FacetInterface getFacet_Simple();

	/**
	 * @return null or the summary facet
	 */
	public FacetProviderNode getFacet_Summary();

	// ?? should copy/clone be here?

	// public void createAliasesForProperties();

	// public boolean isNamedType();
}
