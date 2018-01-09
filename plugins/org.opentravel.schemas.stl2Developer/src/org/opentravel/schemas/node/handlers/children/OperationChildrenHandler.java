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

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;

/**
 * Service children handler using a static children handler for Operations children.
 * <p>
 * 
 * @author Dave
 *
 */
public class OperationChildrenHandler extends StaticChildrenHandler<Node, OperationNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(NavNodeChildrenHandler.class);

	public OperationChildrenHandler(OperationNode owner) {
		super(owner);
		initChildren();

		assert owner.getTLModelObject() instanceof TLOperation;
	}

	@Override
	public void initChildren() {
		initRunning = true;
		children = modelTLs(getChildren_TL());
		initRunning = false;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.add(owner.getTLModelObject().getRequest());
		kids.add(owner.getTLModelObject().getResponse());
		kids.add(owner.getTLModelObject().getNotification());
		return kids;
	}

	protected List<Node> modelTLs(List<TLModelElement> list) {
		List<Node> kids = new ArrayList<Node>();
		for (TLModelElement t : list) {
			assert t instanceof TLFacet;
			FacetOMNode fn = new OperationFacetNode((TLFacet) t);
			fn.setParent(owner);
			kids.add(fn);
		}
		return kids;
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	protected void initInherited() {
	}

}
