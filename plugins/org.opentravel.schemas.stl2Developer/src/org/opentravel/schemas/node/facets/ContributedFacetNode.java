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
package org.opentravel.schemas.node.facets;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for custom, choice, query and update facets.
 * 
 * Contextual facets can be either in the library or in a different library from the object they contextualize.
 * 
 * contextual facets collaborate with their owning object. They add facets without having dependacies between the
 * object's library to the facet's library.
 * 
 * Contextual facets enable OTM to have dependency injection or Inversion of Control.
 * 
 * @author Dave Hollander
 * 
 */
public class ContributedFacetNode extends ContextualFacetNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContributedFacetNode.class);

	private ContextualFacetNode contributor = null;

	public ContributedFacetNode(ContextualFacetNode contributor) {
		// super();
		this.contributor = contributor;
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return contributor == null ? null : contributor.getTLModelObject();
	}

	@Override
	public FacetMO getModelObject() {
		return contributor == null ? null : contributor.getModelObject();
	}

	@Override
	public LibraryNode getLibrary() {
		return contributor == null ? null : contributor.getLibrary();
	}

	// FIXME - get children and other stuff from contributor.
	@SuppressWarnings("unchecked")
	@Override
	public List<Node> getChildren() {
		// contributor can be null and children called during constructor super() call.
		return (List<Node>) (contributor != null ? contributor.getChildren() : Collections.emptyList());
	}

	@Override
	public String getDecoration() {
		if (OTM16Upgrade.otm16Enabled) {
			return " : Contributed from " + getLibrary();
		}
		return "";
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ContributedFacet);
	}

	public void print() {
		if (OTM16Upgrade.otm16Enabled) {
			LOGGER.debug("Contributed facet: " + getName());
			LOGGER.debug("   Label: " + getLabel());
			LOGGER.debug("   Is Local? " + getTLModelObject().isLocalFacet());
			LOGGER.debug("   Owner: " + getOwningComponent());
			LOGGER.debug("   Type: " + getTLModelObject().getFacetType());
		} else
			LOGGER.debug("Contextual facet as Version 1.5 facet.");
	}

}
