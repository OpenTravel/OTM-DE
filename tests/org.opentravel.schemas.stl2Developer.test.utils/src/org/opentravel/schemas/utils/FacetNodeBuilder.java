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
package org.opentravel.schemas.utils;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;

public class FacetNodeBuilder {
	interface TLCreator {
		Object create(String name);
	}

	// facets must have a parent and library to pass action prechecks
	// private FacetNode facet = new FacetNode(new TLFacet());
	private BusinessObjectNode bo = null;
	private FacetProviderNode facet = null;
	private static LibraryNode ln;

	// public static FacetNodeBuilder create() {
	// return new FacetNodeBuilder();
	// }

	public static FacetNodeBuilder create(LibraryNode library) {
		ln = library;
		return new FacetNodeBuilder();
	}

	private FacetNodeBuilder addObjects(TLCreator tlCreator, String... names) {
		buildFacet();
		for (String n : names) {
			Object obj = tlCreator.create(n);
			NodeFactory.newChild(facet, (TLModelElement) obj);
		}
		return this;
	}

	public FacetNodeBuilder addElements(String... names) {
		buildFacet();
		return addObjects(new TLCreator() {

			@Override
			public Object create(String name) {
				TLProperty prop = new TLProperty();
				prop.setName(name);
				return prop;
			}
		}, names);
	}

	public FacetNodeBuilder addAttributes(String... names) {
		buildFacet();
		return addObjects(new TLCreator() {

			@Override
			public Object create(String name) {
				TLAttribute prop = new TLAttribute();
				prop.setName(name);
				return prop;
			}
		}, names);
	}

	public FacetNodeBuilder addIndicators(String... names) {
		buildFacet();
		return addObjects(new TLCreator() {

			@Override
			public Object create(String name) {
				TLIndicator prop = new TLIndicator();
				prop.setName(name);
				return prop;
			}
		}, names);
	}

	public FacetNodeBuilder addAliases(String... names) {
		buildFacet();
		return addObjects(new TLCreator() {

			@Override
			public Object create(String name) {
				TLAlias prop = new TLAlias();
				prop.setName(name);
				return prop;
			}
		}, names);
	}

	public FacetProviderNode build() {
		buildFacet();
		return facet;
	}

	private void buildFacet() {
		if (facet == null) {
			bo = new BusinessObjectNode(new TLBusinessObject());
			bo.setName("buildFacetParent");
			ln.addMember(bo);
			facet = bo.getFacet_Summary();
		}

	}

}