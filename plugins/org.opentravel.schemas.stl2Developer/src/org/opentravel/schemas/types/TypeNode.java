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
package org.opentravel.schemas.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a tree branch anchored to the type object on a Node to represent users of a NamedType. Leaves are computed
 * from the nodes where used data.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeNode extends Node {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeNode.class);

	public enum TypeNodeType {
		OWNER, USER, LIB
	}

	private NodeImageProvider imageProvider = simpleImageProvider("WhereUsed");
	private NodeLabelProvider labelProvider = simpleLabelProvider("Where Used");
	private Node owner = null;
	private TypeNodeType nodeType = TypeNodeType.OWNER;

	/**
	 * Create a new Where Used complete with new TL model and link to component
	 */
	public TypeNode(final TypeProvider parent) {
		this.owner = (Node) parent;
	}

	public TypeNode(final LibraryNode lib) {
		this.owner = lib;
	}

	public TypeNode(Node typeNode, TypeNodeType nodeType) {
		this.owner = typeNode; // The user of this type
		this.nodeType = nodeType;
		String label = "";
		if (typeNode != null && typeNode.getOwningComponent() != null)
			label = typeNode.getOwningComponent().getName();
		// if (typeNode.isNamedType())
		// label = typeNode.getComponentType();
		labelProvider = simpleLabelProvider(label + " : " + typeNode.getName());
		if (typeNode instanceof PropertyNode)
			imageProvider = nodeImageProvider(typeNode.getOwningComponent());
		else
			imageProvider = nodeImageProvider(typeNode);
	}

	/**
	 * Create type node for a library
	 */
	public TypeNode(Node typeNode, Node owner2, TypeNodeType nodeType) {
		this(typeNode, nodeType);
		this.parent = owner2;
		labelProvider = simpleLabelProvider(typeNode.getNameWithPrefix());
	}

	@Override
	public boolean isEditable() {
		return owner.isEditable();
	}

	@Override
	public Node getParent() {
		Node p = owner;
		return p;
	}

	@Override
	public String getLabel() {
		return owner instanceof TypeProvider ? labelProvider.getLabel() + " ("
				+ ((TypeProvider) owner).getWhereAssignedCount() + ")" : labelProvider.getLabel();
	}

	@Override
	public Image getImage() {
		return imageProvider.getImage();
	}

	@Override
	public void delete() {
	}

	@Override
	public String getComponentType() {
		return "WhereUsed:" + owner.getName();
	}

	/**
	 * Get all of the components that use any aspect of the owning component. DO NOT make this a getChildren or the tree
	 * will become invalid with nodes having multiple parents which will break lots of getChildren() users.
	 * 
	 * This is used directly by the Library tree content provider.
	 * 
	 * @return
	 */
	@Override
	public List<Node> getChildren() {
		List<Node> users = new ArrayList<Node>();
		if (owner == null)
			return Collections.emptyList();
		if (owner instanceof TypeProvider)
			for (TypeUser u : ((TypeProvider) owner).getWhereUsedAndDescendants())
				users.add(new TypeNode((Node) u, TypeNodeType.USER));
		else if (owner instanceof LibraryNode)
			if (nodeType.equals(TypeNodeType.OWNER))
				for (Node l : ((LibraryNode) owner).getWhereUsedHandler().getWhereUsed())
					users.add(new TypeNode(l, owner, TypeNodeType.LIB));
			else
				for (Node l : ((LibraryNode) parent).getWhereUsedHandler().getUsersOfTypesFromOwnerLibrary(
						(LibraryNode) owner))
					users.add(new TypeNode((Node) l, TypeNodeType.USER));
		return users;
	}

	@Override
	public boolean hasChildren() {
		return nodeType == TypeNodeType.OWNER;
		// return true; // fixme - only type OWNER has children
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

	private NodeImageProvider nodeImageProvider(final Node node) {
		final Node imageNode = node;
		return new NodeImageProvider() {

			@Override
			public Image getImage() {
				return imageNode.getImage();
			}
		};
	}

	private NodeLabelProvider simpleLabelProvider(final String txt) {
		final String label = txt;
		return new NodeLabelProvider() {

			@Override
			public String getLabel() {
				return label;
			}
		};
	}

	@Override
	public boolean hasNavChildren() {
		return true;
	}

	public boolean isUser() {
		return nodeType.equals(TypeNodeType.USER) ? true : false;
	}

	@Override
	public void sort() {
		getParent().sort();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeNode other = (TypeNode) obj;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

}
