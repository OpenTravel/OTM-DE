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
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ResourceNode;
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

	public AggregateNode(AggregateType type, Node parent) {
		super(type.label(), parent);
		this.aggType = type;
		setLibrary(parent.getLibrary());

		assert (parent instanceof LibraryChainNode) : "Invalid argument.";
	}

	/**
	 * Adds node to the aggregate node's children list if appropriate. This only does the aggregate structure, not
	 * library or parent links.
	 * 
	 * @param nodeToAdd
	 * @return
	 */
	public void add(ComponentNode nodeToAdd) {
		addPreTests(nodeToAdd); // Type safety
		// LOGGER.debug("Adding " + nodeToAdd + " to aggregate of" + getLibrary());

		// If this is a service then just add to the service root as services are not versioned objects
		if (nodeToAdd instanceof ServiceNode) {
			getChildren().add(nodeToAdd);
			return;
		} else if (nodeToAdd instanceof ResourceNode) {
			if (!getChildren().contains(nodeToAdd))
				getChildren().add(nodeToAdd);
			return;
		}

		// If the nodeToAdd name is already in the chain then we need to handle the version logic.
		final List<Node> duplicates = findExactMatches(getChildren(), nodeToAdd.getName());
		if (!duplicates.isEmpty())
			addVersionedNode(nodeToAdd, duplicates);
		else
			getChildren().add(nodeToAdd); // simply add the node
	}

	// Try to replace the existing name matching node
	private void addVersionedNode(ComponentNode nodeToAdd, List<Node> duplicates) {
		// If the duplicate is in the same library then the user made a mistake.
		// Leave the node in the aggregate so the user can fix the problem.
		Node parent = this;
		for (Node dup : duplicates) {
			parent = this;

			if (nodeToAdd.getLibrary() == dup.getLibrary()) {
				// same library means not different version so add the duplicate and let user fix the error
				if (!getChildren().contains(nodeToAdd)) {
					getChildren().add(nodeToAdd);
				}
			} else {
				// Get existing version node or create one
				VersionNode vn = dup.getVersionNode();
				if (vn == null)
					vn = nodeToAdd.getVersionNode();
				if (vn == null)
					vn = new VersionNode((ComponentNode) dup);

				if (nodeToAdd.isLaterVersion(dup)) {
					// Replace older object(dup) with same name with the node to be added
					getChildren().remove(dup);
					insertPreviousVersion(nodeToAdd, (ComponentNode) dup);
					if (!getChildren().contains(nodeToAdd))
						getChildren().add(nodeToAdd);
				} else {
					// 4/8/2017 - added linking in older versions. (dup is newer)
					vn.setPreviousVersion(nodeToAdd);
					if (((ComponentNode) dup).isLaterVersion(nodeToAdd.getVersionNode().getHead()))
						dup.getVersionNode().setNewestVersion((ComponentNode) dup);
					if (!dup.getVersionNode().getChildren().contains(nodeToAdd))
						dup.getVersionNode().getChildren().add(nodeToAdd);
				}
			}
		}
	}

	/**
	 * Make sure node to add's component type matches aggregate type
	 * 
	 * @param nodeToAdd
	 */
	private void addPreTests(ComponentNode nodeToAdd) {
		// Type safety
		switch (aggType) {
		case ComplexTypes:
			if (!(nodeToAdd instanceof ComplexComponentInterface))
				throw new IllegalStateException("Can't add to complex aggregate.");
			break;
		case SimpleTypes:
			if (!(nodeToAdd instanceof SimpleComponentInterface))
				throw new IllegalStateException("Can't add to simple aggregate.");
			break;
		case Service:
			if (!(nodeToAdd instanceof ServiceNode || (nodeToAdd instanceof OperationNode)))
				throw new IllegalStateException("Can't add to service aggregate.");
			break;
		case RESOURCES:
			if (!(nodeToAdd instanceof ResourceNode))
				throw new IllegalStateException("Can't add to resource aggregate.");
			break;
		default:
			throw new IllegalStateException("Unknown object type: " + nodeToAdd.getClass().getSimpleName());
		}
		if (nodeToAdd.getLibrary() == null)
			throw new IllegalArgumentException("Tried to add node with null library. " + nodeToAdd);
	}

	/**
	 * examine all children for exact name match
	 */
	private List<Node> findExactMatches(List<Node> children, String matchName) {
		List<Node> ret = new ArrayList<Node>();
		for (Node c : children) {
			if (c.getName().equals(matchName))
				ret.add(c);
		}
		return ret;
	}

	/**
	 * Insert node in versions list. Update all the newest object links.
	 * 
	 * @param newest
	 * @param toBePlaced
	 */
	private void insertPreviousVersion(ComponentNode newest, ComponentNode toBePlaced) {
		toBePlaced.getVersionNode().setNewestVersion(newest);
		if (toBePlaced.getVersionNode().getPreviousVersion() == null) {
			newest.getVersionNode().setPreviousVersion(toBePlaced);
			return;
		}

		toBePlaced.getVersionNode().setNewestVersion(newest);
		VersionNode toBePlacedVN = toBePlaced.getVersionNode();
		ComponentNode n = toBePlacedVN.getPreviousVersion();
		while (n != null) {
			n.getVersionNode().setNewestVersion(newest);
			if (toBePlaced.isLaterVersion(n)) {
				// if (toBePlaced.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
				n.getVersionNode().setPreviousVersion(toBePlaced);
				toBePlacedVN.setPreviousVersion(n.getVersionNode().getPreviousVersion());
				n = toBePlaced;
			}
			if (n == n.getVersionNode().getPreviousVersion())
				n = null;
			else
				n = n.getVersionNode().getPreviousVersion();
		}
	}

	public void remove(Node node) {
		if (!getChildren().remove(node)) {
			// if it was not found, it may be in a family node
			ArrayList<Node> kids = new ArrayList<Node>(getChildren());
			for (Node n : kids) {
				if (n instanceof AggregateFamilyNode)
					((AggregateFamilyNode) n).remove(node);
				// if ((n instanceof AggregateFamilyNode) && (n.family.equals(node.family))) {
				// ((AggregateFamilyNode) n).remove(node);
				// if (n.getChildren().isEmpty())
				// getChildren().remove(n);
				// }
			}
		}
	}

	@Override
	public void close() {
		if (getParent() != null)
			getParent().getChildren().remove(this);
		getChildren().clear();
		setLibrary(null);
		modelObject = null;
		deleted = true;
	}

	public boolean contains(Node node) {
		return getChildren().contains(node);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get("aggregateFolder");
	}

	/*
	 * (non-Javadoc) // * @see org.opentravel.schemas.node.Node#getLibrary() //
	 */
	@Override
	public LibraryNode getLibrary() {
		return parent != null ? parent.getLibrary() : null;
	}

	/*
	 * For the non-version aggregates, skip over the version node These are used in the navigator menul.
	 */
	// 3/11/2015 - this should go away...just return children via super type
	// @Override
	// public List<Node> getNavChildren() {
	// ArrayList<Node> kids = new ArrayList<Node>();
	// for (Node child : getChildren()) {
	// if (child instanceof AggregateFamilyNode) {
	// kids.add(child);
	// } else if (child.getParent() instanceof VersionNode)
	// kids.add(((VersionNode) child.getParent()).getNewestVersion());
	// else if (child.getParent().getParent() instanceof FamilyNode)
	// LOGGER.error("Aggregate children contain a family node.");
	// // // Only put families in once
	// // if (!kids.contains(child.getParent().getParent()))
	// // kids.add(child.getParent().getParent());
	// else
	// LOGGER.warn("Unknown child in aggregate node: " + child.getClass().getSimpleName());
	// }
	// return kids;
	// }

	/**
	 * To get all providers, we only want to get type providers from their original libraries. If we used the other
	 * aggregates, the earlier versions would not be found. If we include them types in the other aggregates would be
	 * duplicates.
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return aggType.equals(AggregateType.Versions) && getChildren().size() > 0 ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isEditable()
	 */
	@Override
	public boolean isEditable() {
		if (getParent() != null)
			return getParent().isEditable();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isInTLLibrary()
	 */
	@Override
	public boolean isInTLLibrary() {
		return parent != null ? parent.isInTLLibrary() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.NavNode#isLibraryContainer()
	 */
	@Override
	public boolean isLibraryContainer() {
		return aggType == AggregateType.Versions ? true : false;
	}

}
