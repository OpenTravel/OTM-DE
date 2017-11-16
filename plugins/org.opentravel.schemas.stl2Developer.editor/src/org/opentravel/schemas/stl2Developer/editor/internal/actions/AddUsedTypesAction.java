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
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram.Position;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddUsedTypesAction extends ShowHideNodeAction {

	public AddUsedTypesAction(AbstractEditPartViewer viewer, String label) {
		super(viewer, label);
	}

	@Override
	protected List<Node> getNewNodes(UINode n) {
		return getOwningComponents(getTypeUsersWithoutUnnasiged(n.getNode()));
	}

	@Override
	protected boolean isValidSelection(List<UINode> nodes) {
		for (UINode n : nodes) {
			if (!getTypeUsersWithoutUnnasiged(n.getNode()).isEmpty())
				return true;
		}
		return false;
	}

	private List<Node> getTypeUsersWithoutUnnasiged(Node node) {
		List<Node> ret = new ArrayList<Node>();
		for (Node n : node.getChildren_TypeUsers()) {
			if (validNodeType(n) && !n.isUnAssigned() && shouldDisplay(n.getType()))
				ret.add(n.getType());
		}
		return ret;

	}

	private boolean shouldDisplay(Node typeNode) {
		if (typeNode == null) {
			return false;
		}
		return Features.showAsUsedType(typeNode);
	}

	private boolean validNodeType(Node n) {
		boolean ret = n instanceof ElementNode;
		ret = ret || n instanceof AttributeNode;
		ret = ret || n instanceof SimpleAttributeFacadeNode;
		return ret;
	}

	@Override
	protected Position getInitialPosition(Node newNode, Node referance) {
		if (newNode.isInstanceOf(referance)) {
			return Position.BOTTOM;
		} else if (referance.isInstanceOf(newNode)) {
			return Position.TOP;
		}
		return Position.RIGHT;
	}

}
