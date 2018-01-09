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

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for inherited contributed facets.
 * 
 * @author Dave Hollander
 * 
 */
// Why is this deprecated??? I like having all inherited be sub-types
// @Deprecated
public class InheritedContributedFacetNode extends ContributedFacetNode implements InheritedInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritedContributedFacetNode.class);

	public ContributedFacetNode inheritedFrom = null;

	/**
	 * Create inheritance facade from an existing contributed facet to indicate that it is inherited.
	 * 
	 * @param owner
	 */
	public InheritedContributedFacetNode(ContributedFacetNode inheritedFrom) {
		super();
		this.inheritedFrom = inheritedFrom;

		assert inheritedFrom != null;
		// LOGGER.debug("Inherited Contributed Facet Created: " + inheritedFrom.getLocalName());
	}

	@Override
	public ContributedFacetNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public FacetProviderChildrenHandler getChildrenHandler() {
		return getContributor() != null ? getContributor().getChildrenHandler() : null;
	}

	@Override
	public ContextualFacetNode getContributor() {
		return getInheritedFrom().getContributor();
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return getInheritedFrom().getOwningComponent();
	}

	@Override
	public Node getParent() {
		return getInheritedFrom().getParent();
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return getInheritedFrom().getTLModelObject();
	}

	@Override
	public String getLabel() {
		return getInheritedFrom().getContributor().getLabel() + " (Inherited contributor)";
	}

	@Override
	public String getName() {
		return getInheritedFrom() == null ? "" : getInheritedFrom().getName();
	}

	/**
	 * Simple Setter
	 */
	public void setInheritedFrom(ContributedFacetNode baseContrib) {
		inheritedFrom = baseContrib;
	}

	@Override
	public void setDeleted(boolean value) {
		deleted = value;
	}

}
