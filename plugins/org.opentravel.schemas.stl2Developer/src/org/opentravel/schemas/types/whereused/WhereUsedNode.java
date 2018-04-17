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

import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.node.handlers.children.WhereUsedChildrenHandler;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a tree branch anchored to the type object on a Node to represent users of a NamedType. Leaves are computed
 * from the nodes where used data.
 * 
 * @author Dave Hollander
 * 
 */
@SuppressWarnings("unchecked")
public abstract class WhereUsedNode<O> extends Node implements WhereUsedNodeInterface, FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(WhereUsedNode.class);

	protected NodeImageProvider imageProvider = simpleImageProvider("WhereUsed");
	protected NodeLabelProvider labelProvider = simpleLabelProvider("Where Used");
	protected O owner = null;

	public WhereUsedNode(final LibraryNode lib, LibraryNode parent) {
		this.owner = (O) lib;
		this.parent = parent;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	@Override
	public WhereUsedChildrenHandler getChildrenHandler() {
		return (WhereUsedChildrenHandler) childrenHandler;
	}

	public WhereUsedNode(final LibraryNode lib) {
		this.owner = (O) lib;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	public WhereUsedNode(final TypeProvider provider) {
		this.owner = (O) provider;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	public WhereUsedNode(final ContextualFacetNode owner) {
		this.owner = (O) owner;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	public WhereUsedNode(final ExtensionOwner owner) {
		this.owner = (O) owner;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	public WhereUsedNode(final TypeUser user) {
		this.owner = (O) user;
		childrenHandler = new WhereUsedChildrenHandler(this);
	}

	@Override
	public void delete() {
		LOGGER.debug("Deleting where used node: " + this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WhereUsedNode other = (WhereUsedNode) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacadeInterface#get()
	 */
	@Override
	public Node get() {
		return (Node) getOwner();
	}

	/**
	 * Get all of the components that use any aspect of the owning component. DO NOT make this a getChildren or the tree
	 * will become invalid with nodes having multiple parents which will break lots of getChildren() users.
	 * 
	 * @return new list of children
	 */
	@Override
	public abstract List<Node> getChildren();

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.NAVIGATION;
	}

	@Override
	public String getComponentType() {
		return "WhereUsed:" + ((Node) owner).getName();
	}

	@Override
	public abstract String getDecoration();

	@Override
	public Image getImage() {
		return imageProvider.getImage();
	}

	@Override
	public String getLabel() {
		return labelProvider.getLabel();
	}

	@Override
	public String getName() {
		return labelProvider.getLabel();
	}

	public O getOwner() {
		return owner;
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return ((Node) owner).getOwningComponent();
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getChildren();
	}

	@Override
	public abstract boolean hasChildren();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	/**
	 * Always true because lazy evaluation of children.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return !getChildren().isEmpty();
		// return true;
	}

	@Override
	public boolean isEditable() {
		return owner != null ? ((Node) owner).isEditable() : false;
	}

	// @Override
	// public void sort() {
	// getParent().sort();
	// }

	@Override
	public boolean isLibraryMemberContainer() {
		return false;
	}

	protected NodeImageProvider nodeImageProvider(final Node node) {
		final Node imageNode = node;
		return new NodeImageProvider() {

			@Override
			public Image getImage() {
				return imageNode != null ? imageNode.getImage() : null;
			}
		};
	}

	protected NodeLabelProvider simpleLabelProvider(final String txt) {
		final String label = txt;
		return new NodeLabelProvider() {

			@Override
			public String getLabel() {
				return label;
			}
		};
	}

	private NodeImageProvider simpleImageProvider(final String imageName) {
		return new NodeImageProvider() {

			@Override
			public Image getImage() {
				final ImageRegistry imageRegistry = Images.getImageRegistry();
				return imageRegistry.get(imageName);
			}
		};
	}

}
