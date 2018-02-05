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
package org.opentravel.schemas.node.handlers.children;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.AttributeFacetNode;

/**
 * Simple facets are facets that have one implied child.
 * 
 * A Simple Facet has one property node child whose assigned type must be a simple type.
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeFacetChildrenHandler extends CachingChildrenHandler<Node, AttributeFacetNode> {
	// private final static Logger LOGGER = LoggerFactory.getLogger(AttributeFacetChildrenHandler.class);

	/**
	 * Create a caching children handler for the attribute facet.
	 * 
	 * @param obj
	 */
	public AttributeFacetChildrenHandler(AttributeFacetNode obj) {
		super(obj);
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.addAll(owner.getTLModelObject().getAttributes());
		kids.addAll(owner.getTLModelObject().getIndicators());
		return kids;
	}
}
