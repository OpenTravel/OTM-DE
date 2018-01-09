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

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;

/**
 * Model node children handler using a static children handler for projects and implied types.
 * <p>
 * 
 * @author Dave
 *
 */
public class ModelNodeChildrenHandler extends StaticChildrenHandler<Node, ModelNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ModelNodeChildrenHandler.class);

	public ModelNodeChildrenHandler(ModelNode owner) {
		super(owner);
	}

	// Assure children include the default project.
	@Override
	public List<Node> get() {
		if (owner.getDefaultProject() != null && !children.contains(owner.getDefaultProject()))
			children.add(owner.getDefaultProject());
		return children;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

}
