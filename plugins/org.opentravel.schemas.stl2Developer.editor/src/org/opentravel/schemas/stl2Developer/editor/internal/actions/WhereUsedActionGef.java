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
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram.Position;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * @author Pawel Jedruch
 * 
 */
public class WhereUsedActionGef extends ShowHideNodeAction {

	public WhereUsedActionGef(AbstractEditPartViewer viewer, String label) {
		super(viewer, label);
	}

	@Override
	protected List<Node> getNewNodes(UINode n) {
		Node nn = n.getNode();
		List<Node> usedNodes = new ArrayList<Node>();
		// 2/27/18 dmh - added contextual facet owner
		if (nn instanceof ContextualFacetNode)
			if (((ContextualFacetNode) nn).getWhereContributed() != null)
				usedNodes.add((Node) ((ContextualFacetNode) nn).getWhereContributed().getOwningComponent());
		// Changed to use where assigned - 3/28/16 - dmh
		if (nn instanceof TypeProvider) {
			for (TypeUser u : ((TypeProvider) nn).getWhereAssigned())
				usedNodes.add((Node) u);
		}
		if (!usedNodes.isEmpty())
			return getOwningComponents(usedNodes);
		return null;
	}

	@Override
	protected boolean isValidSelection(List<UINode> nodes) {
		for (UINode n : nodes) {
			Node nn = n.getNode();
			if (nn instanceof ContextualFacetNode)
				return ((ContextualFacetNode) nn).getWhereContributed() != null;
			if (nn instanceof TypeProvider)
				if (!((TypeProvider) nn).getWhereAssigned().isEmpty())
					return true;
		}
		return false;
	}

	@Override
	protected Position getInitialPosition(Node newNode, Node referance) {
		if (newNode.isInstanceOf(referance)) {
			return Position.BOTTOM;
		} else if (referance.isInstanceOf(newNode)) {
			return Position.TOP;
		}
		return Position.LEFT;
	}
}
