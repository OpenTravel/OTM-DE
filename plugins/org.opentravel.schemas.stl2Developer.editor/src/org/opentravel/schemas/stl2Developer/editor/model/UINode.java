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
package org.opentravel.schemas.stl2Developer.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;

/**
 * @author Pawel Jedruch
 * 
 */
public class UINode {
	// TODO: remove getters and setters
	private final Diagram owner;
	private final Node node;
	private final UINode parent;
	private Point location;
	private Dimension size;
	private List<UINode> children = new ArrayList<UINode>();

	UINode(Node node, Diagram owner, UINode parent) {
		this.node = node;
		this.owner = owner;
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public Node getNode() {
		return node;
	}

	public UINode getParent() {
		return parent;
	}

	public Point getLocation() {
		return location;
	}

	public Dimension getSize() {
		return size;
	}

	public List<UINode> getChildren() {
		return children;
	}

	public void addChild(UINode child) {
		children.add(child);
	}

	public Rectangle getBoundry() {
		return new Rectangle(location, size);
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public void setLocation(Point location) {
		Point oldLocation = this.location;
		this.location = location;
		owner.publish(this, "location", oldLocation, location);
	}

	public Diagram getOwner() {
		return owner;
	}

	/**
	 * Returns the List of the connection nodes objects for which this node model is the <b>target</b>
	 */
	public Collection<UINode> getConnectedAsTarget() {
		List<UINode> ret = new ArrayList<UINode>();
		// If a contextual facet, find the contributor
		if (node instanceof ContextualFacetNode) {
			UINode uiCF = owner.findUINode(((ContextualFacetNode) node).getWhereContributed());
			if (uiCF != null)
				ret.add(uiCF);
		} else {
			if (node instanceof TypeProvider)
				for (TypeUser u : ((TypeProvider) node).getWhereAssigned()) {
					UINode uiTypeUser = owner.findUINode((Node) u);
					if (uiTypeUser != null)
						ret.add(uiTypeUser);
				}
		}
		return ret;
	}

	/**
	 * Returns the List of the connection nodes objects for which this node model is the <b>source</b>
	 */
	public Collection<UINode> getConnectedAsSource() {
		List<UINode> ret = new ArrayList<UINode>();
		// If a contributor find the contextual facet
		if (node instanceof ContributedFacetNode) {
			UINode uiContributor = owner.findUINode(((ContributedFacetNode) node).getContributor());
			if (uiContributor != null)
				ret.add(uiContributor);
		} else {
			UINode uiTypeUser = owner.findUINode(node.getType());
			if (uiTypeUser != null)
				ret.add(uiTypeUser);
		}
		return ret;
	}

	public boolean isTopLevel() {
		return parent == null;
	}

	public UINode getTopLevelParent() {
		return getTopLevelParent(this);
	}

	private UINode getTopLevelParent(UINode n) {
		if (n.isTopLevel()) {
			return n;
		} else {
			return getTopLevelParent(n.getParent());
		}
	}

	/**
	 * Get the owning component node.
	 * 
	 * @param node
	 * @return
	 */
	public static Node getOwner(Node node) {
		if (node instanceof OperationNode)
			return node;
		else if (node == node.getOwningComponent()) {
			return node;
		} else {
			return getOwner((Node) node.getOwningComponent());
		}
	}

	public Image getTypeImage() {
		Node type = TypeResolver.getNodeType(getNode());
		// TODO: inherited properties dosn't have a typeNode.
		if (type != null) {
			return type.getImage();
		}
		return null;
	}

	public boolean isUnlinked() {
		return node.isDeleted();
	}

}
