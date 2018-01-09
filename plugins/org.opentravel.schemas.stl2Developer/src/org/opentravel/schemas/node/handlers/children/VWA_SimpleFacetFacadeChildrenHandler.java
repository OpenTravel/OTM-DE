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
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets are facets that have one child taken from the owner's TL object.
 * 
 * A Simple Facet has one property node child whose assigned type must be a simple type.
 * 
 * @author Dave Hollander
 * 
 */
// TODO - why is this caching and not static
public class VWA_SimpleFacetFacadeChildrenHandler extends CachingChildrenHandler<Node, VWA_SimpleFacetFacadeNode> {
	private final static Logger LOGGER = LoggerFactory.getLogger(VWA_SimpleFacetFacadeChildrenHandler.class);

	public VWA_SimpleFacetFacadeChildrenHandler(VWA_SimpleFacetFacadeNode obj) {
		super(obj);
	}

	@Override
	public void initChildren() {
		initRunning = true;
		children = new ArrayList<Node>();
		children.add(owner.createAttributeFacade());
		initRunning = false;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		return Collections.emptyList();
	}
}
