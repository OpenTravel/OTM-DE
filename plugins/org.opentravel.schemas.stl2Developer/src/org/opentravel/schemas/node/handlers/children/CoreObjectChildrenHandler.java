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
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreObjectChildrenHandler extends StaticChildrenHandler<Node, CoreObjectNode> {
	@SuppressWarnings("unused")
	private final static Logger LOGGER = LoggerFactory.getLogger(CoreObjectChildrenHandler.class);

	public CoreObjectChildrenHandler(final CoreObjectNode obj) {
		super(obj);
		initChildren();
	}

	// Since the TL Model and Node model are so different do the mapping here.
	@Override
	public void initChildren() {
		initRunning = true;
		super.initChildren(); // initialize children and load facets
		children.add(0, new CoreSimpleFacetNode(owner));
		children.addAll(modelTLs(getChildren_TL()));
		initRunning = false;
	}

	@Override
	public void clear() {
		// NO-OP
	}

	/**
	 * Get everything EXCEPT the simple facet
	 */
	@Override
	public List<TLModelElement> getChildren_TL() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.add(owner.getTLModelObject().getSummaryFacet());
		kids.add(owner.getTLModelObject().getDetailFacet());
		kids.add(owner.getTLModelObject().getDetailListFacet());
		kids.add(owner.getTLModelObject().getSimpleListFacet());
		kids.addAll(owner.getTLModelObject().getAliases());
		kids.add(owner.getTLModelObject().getRoleEnumeration());
		return kids;
	}

	protected List<Node> modelTLs(List<TLModelElement> list) {
		List<Node> kids = new ArrayList<Node>();
		for (TLModelElement t : list) {
			ComponentNode fn = NodeFactory.newChild(owner, t);
			fn.setParent(owner);
			kids.add(fn);
		}
		return kids;
	}

}
