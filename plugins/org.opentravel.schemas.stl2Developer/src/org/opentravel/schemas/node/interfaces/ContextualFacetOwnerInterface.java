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

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Objects implementing this interface can own contextual facets (choice, custom, query, etc) *
 * 
 * @author Dave Hollander
 * 
 */
public interface ContextualFacetOwnerInterface {

	public TLFacetOwner getTLModelObject();

	public LibraryNode getLibrary();

	public Node getParent();

	/**
	 * 
	 * @param tlObj
	 * @return the contributed facet with this TL Contextual facet or NULL
	 */
	public ContributedFacetNode getContributedFacet(TLContextualFacet tlObj);

	/**
	 * Return True if this owner can own the passed ContextualFacet
	 * 
	 * @param targetCF
	 * @return
	 */
	public boolean canOwn(ContextualFacetNode targetCF);

}
