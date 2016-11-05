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
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a tree branch anchored to the type object on a Node to represent libraries used in type assignments for
 * object properties in a library. Leaves are computed from library traversal of type users.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeUserNode extends Node implements WhereUsedNodeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeUserNode.class);

	public enum TypeUserNodeType {
		OWNER, PROVIDER_LIB
	}

	private NodeImageProvider imageProvider = simpleImageProvider("WhereUsed");
	private NodeLabelProvider labelProvider = simpleLabelProvider("Uses Objects From");
	private LibraryNode owner = null;
	private TypeUserNodeType nodeType = TypeUserNodeType.OWNER;

	/**
	 * Create a "Uses" node to add to navigator tree. Children are libraries that contain type providers used by any
	 * type user in the passed library.
	 * 
	 * @param lib
	 */
	public TypeUserNode(final LibraryNode lib) {
		this.owner = lib;
	}

	/**
	 * Create a node to represent the provider library.
	 * 
	 * @param typeNode
	 * @param nodeType
	 */
	public TypeUserNode(LibraryNode providerLib, LibraryNode userLib) {
		owner = providerLib; // library with type providers used in parent library
		nodeType = TypeUserNodeType.PROVIDER_LIB;
		String label = "";
		parent = userLib;
		labelProvider = simpleLabelProvider(providerLib.getLabel());
		imageProvider = nodeImageProvider(providerLib.getOwningComponent());
	}

	@Override
	public boolean isEditable() {
		// return owner.isEditable();
		// 5/26/2016 - restored line below
		return nodeType == TypeUserNodeType.OWNER ? owner.isEditable() : parent.isEditable();
		// Owner - is the "uses objects from"
		// Provider_LIB is a library that provides types
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.NAVIGATION;
	}

	public boolean isProviderLib() {
		return nodeType == TypeUserNodeType.PROVIDER_LIB;
	}

	@Override
	public Node getParent() {
		Node p = owner;
		if (nodeType == TypeUserNodeType.PROVIDER_LIB)
			return parent;
		return p;
	}

	@Override
	public boolean isLibraryMemberContainer() {
		return false;
	};

	@Override
	public String getLabel() {
		return labelProvider.getLabel();
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
		return "Uses:" + owner.getName();
	}

	/**
	 * If this is the owner library, get all of the libraries containing type providers.
	 * 
	 * If this is provider library, return empty array.
	 * 
	 * This is used directly by the Library tree content provider.
	 * 
	 * @return
	 */
	@Override
	public List<Node> getChildren() {
		if (owner != null) {
			// if (owner != null && nodeType.equals(TypeUserNodeType.OWNER)) {
			List<Node> providerLibs = new ArrayList<Node>();
			for (LibraryNode l : owner.getAssignedLibraries())
				providerLibs.add(new TypeUserNode(l, owner));
			return providerLibs;
		} else
			return Collections.emptyList();
	}

	@Override
	public boolean hasChildren() {
		return nodeType == TypeUserNodeType.OWNER;
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
	public boolean hasNavChildren(boolean deep) {
		return nodeType == TypeUserNodeType.PROVIDER_LIB ? false : true;
		// FIXME - only return true if there are provider libs
		// return true;
	}

	/**
	 * Always true because lazy evaluation of children.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getChildren();
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

	public LibraryNode getOwner() {
		return owner;
	}

	@Override
	public String getName() {
		return labelProvider.getLabel();
	}

	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (getClass() != obj.getClass())
	// return false;
	// TypeNode other = (TypeNode) obj;
	// if (owner == null) {
	// if (other.owner != null)
	// return false;
	// } else if (!owner.equals(other.owner))
	// return false;
	// return true;
	// }
}
