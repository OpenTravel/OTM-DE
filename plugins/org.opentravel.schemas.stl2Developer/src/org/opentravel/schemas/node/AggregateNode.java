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
/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregate nodes are navigation nodes that collect types from all libraries in a version chain.
 * 
 * @author Dave Hollander
 * 
 */
public class AggregateNode extends NavNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(AggregateNode.class);

	private AggregateType aggType;

	public enum AggregateType {
		RESOURCES("Resources"),
		SimpleTypes("Simple Objects"),
		ComplexTypes("Complex Objects"),
		Service("Service"),
		Versions("Versions");
		private final String label;

		private AggregateType(String label) {
			this.label = label;
		}

		private String label() {
			return label;
		}
	}

	public AggregateNode(AggregateType type, LibraryChainNode parent) {
		super(type.label(), parent);
		this.aggType = type;
		setLibrary(parent.getLibrary());
	}

	/**
	 * Adds node to the aggregate node's children list if appropriate. This only does the aggregate structure, not
	 * library or parent links.
	 * 
	 * @param nodeToAdd
	 * @return
	 */
	public void add(ComponentNode nodeToAdd) {
		// If this is a service then just add to the service root as services are not versioned objects
		if (nodeToAdd instanceof ServiceNode) {
			// getChildren().add(nodeToAdd);
			getChildrenHandler().add(nodeToAdd);
			return;
		}

		VersionNode vn = null;

		// If already in aggregate, just exit.
		if (nodeToAdd.getVersionNode() != null)
			if (getChildren().contains(nodeToAdd.getVersionNode()))
				return;

		// contextual facets are not versioned but are displayed in aggregate.
		// Add a version node to make the handling consistent.
		if (nodeToAdd instanceof ContextualFacetNode) {
			getChildrenHandler().add(new VersionNode(nodeToAdd));
			// vn = new VersionNode(this, nodeToAdd);
			return;
		}

		// Get a list of children with the same name as nodeToAdd
		final List<Node> duplicates = findExactMatches(getChildren(), nodeToAdd);

		// Get the version node for this resource chain if it exists
		if (duplicates.isEmpty())
			// Add a version node to this aggregates children
			getChildrenHandler().add(new VersionNode(nodeToAdd));
		else {
			vn = duplicates.get(0).getVersionNode();
			// Add this node to the version chain
			vn.add(nodeToAdd);
		}

		// LOGGER.debug("Added " + nodeToAdd.getNameWithPrefix() + " to version chain.");
		return;
	}

	/**
	 * Examine all children for exact name match and object type. Match must be of the same object type and in a
	 * different library.
	 */
	private List<Node> findExactMatches(List<Node> versionNodes, Node match) {
		String matchName = match.getName();
		List<Node> ret = new ArrayList<>();
		for (Node c : versionNodes) {
			assert c instanceof VersionNode;
			if (c.getName() != null && c.getName().equals(matchName))
				if (((VersionNode) c).get().getClass() == match.getClass())
					if (((VersionNode) c).get().getLibrary() != match.getLibrary())
						ret.add(c);
		}
		return ret;
	}

	/**
	 * Remove the version node or version node associated with the passed node from the aggregate child list.
	 */
	public void remove(Node node) {
		if (!(node instanceof ServiceNode) && (!(node instanceof VersionNode)))
			node = node.getVersionNode();
		getChildrenHandler().remove(node);
	}

	/**
	 * Close all the children then clear children handler.
	 */
	@Override
	public void close() {
		List<Node> kids = getChildrenHandler().getChildren_New();
		for (Node n : kids) {
			n.close();
			n.setParent(null);
			getChildrenHandler().clear(n);
		}
		setParent(null);
		deleted = true;
	}

	/**
	 * Does the child list contain the passed node.
	 * <p>
	 * Simply checks if nodes is in array and does <b>not</b> get the version node if an object is passed.
	 */
	@Override
	public boolean contains(Node node) {
		return getChildren().contains(node);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get("aggregateFolder");
	}

	/**
	 * To get all providers, we only want to get type providers from their original libraries. If we used the other
	 * aggregates, the earlier versions would not be found. If we include them types in the other aggregates would be
	 * duplicates.
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return aggType.equals(AggregateType.Versions) && getChildren().size() > 0 ? true : false;
	}

	@Override
	public boolean isEditable() {
		if (getParent() != null)
			return getParent().isEditable();
		return false;
	}

	@Override
	public boolean isInTLLibrary() {
		return parent != null ? parent.isInTLLibrary() : false;
	}

	// @Override
	// public boolean isLibraryContainer() {
	// return aggType == AggregateType.Versions ? true : false;
	// }

}
