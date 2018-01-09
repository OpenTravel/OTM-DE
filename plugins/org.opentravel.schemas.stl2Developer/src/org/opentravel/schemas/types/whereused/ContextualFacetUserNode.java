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
package org.opentravel.schemas.types.whereused;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaf node describing an Contextual Facet. Exposes where a contextual facet is contributed.
 * 
 * @author Dave Hollander
 * 
 */
public class ContextualFacetUserNode extends WhereUsedNode<ContextualFacetNode> implements WhereUsedNodeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetUserNode.class);

	public ContextualFacetUserNode(ContextualFacetNode user) {
		super(user); // sets owner

		String label = "--no owner--";
		// if (owner.getOwningComponent() != null)
		// label = owner.getOwningComponent().getNameWithPrefix();
		if (owner != null)
			label = owner.getNameWithPrefix();
		labelProvider = simpleLabelProvider(label);
		imageProvider = nodeImageProvider(user);

		// LOGGER.debug("Create CF User Node for" + user);
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		decoration += "contributes to";
		decoration += " " + owner.getWhereContributed().getOwningComponent().getNameWithPrefix();
		return decoration;
	}

	@Override
	public List<Node> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return false;
	}

}
