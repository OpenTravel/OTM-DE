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
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.ServiceNode;

/**
 * Service children handler using a static children handler for Operations children.
 * <p>
 * 
 * @author Dave
 *
 */
public class ServiceChildrenHandler extends StaticChildrenHandler<Node, ServiceNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(NavNodeChildrenHandler.class);

	public ServiceChildrenHandler(ServiceNode owner) {
		super(owner);

		assert owner.getTLModelObject() instanceof TLService;
		initChildren();
	}

	/**
	 * Initialize service children (operations)
	 */
	@Override
	public void initChildren() {
		initRunning = true;
		super.initChildren(); // initialize children and load facets
		children.addAll(modelTLs(getChildren_TL()));
		initRunning = false;
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

	@Override
	public List<TLModelElement> getChildren_TL() {
		ArrayList<TLModelElement> kids = new ArrayList<TLModelElement>();
		for (TLOperation op : owner.getTLModelObject().getOperations())
			kids.add(op);
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
