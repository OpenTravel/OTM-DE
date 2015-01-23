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
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.views.FacetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregate nodes are navigation nodes that collect types from all libraries in a version chain.
 * 
 * @author Dave Hollander
 * 
 */
public class AggregateNode extends NavNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetView.class);

	private AggregateType aggType;

	public enum AggregateType {
		SimpleTypes("Simple Objects"), ComplexTypes("Complex Objects"), Service("Service"), Versions("Versions");
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
	public boolean add(ComponentNode nodeToAdd) {
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
		default:
			throw new IllegalStateException("Unknown object type: " + nodeToAdd.getClass().getSimpleName());
		}
		if (nodeToAdd.getLibrary() == null)
			throw new IllegalArgumentException("Tried to add node with null library. " + nodeToAdd);

		final String familyName = NodeNameUtils.makeFamilyName(nodeToAdd.getName());
		// children families and components that have same family prefix
		final List<Node> familyMatches = findFamilyNameMatches(getChildren(), familyName);
		AggregateFamilyNode family = findFamilyNode(familyMatches, familyName);
		final List<Node> duplicates = findExactMatches(getChildren(), nodeToAdd.getName());

		if (familyMatches.isEmpty()) {
			// simply add the node
			getChildren().add(nodeToAdd);
		} else {
			if (duplicates.isEmpty()) {
				// Add to a family
				if (family == null) {
					// Start a new family and add the match(s)
					family = new AggregateFamilyNode(this, familyName);
					List<Node> kids = new ArrayList<Node>(familyMatches);
					for (Node n : kids) {
						getChildren().remove(n);
						family.getChildren().add(n);
					}
				}
				family.getChildren().add(nodeToAdd);
			} else {
				// Try to replace the existing name matching node
				for (Node n : duplicates) {
					if (nodeToAdd.getLibrary() == n.getLibrary()) {
						// Add to aggregate so user can fix the problem.
						if (!getChildren().contains(nodeToAdd)) {
							getChildren().add(nodeToAdd);
							// LOGGER.debug("AggregateNode: " + n + " Same name object found in the same library.");
						}
					} else {
						// Replace older object with same name with the node to be added
						if (nodeToAdd.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
							getChildren().remove(n);
							insertPreviousVersion(nodeToAdd, (ComponentNode) n);
							getChildren().add(nodeToAdd);
						} else
							// Happens on startup - i don't know why
							// assert (false) : "Error - added to an older library.";
							return false;
					}
				}
			}
		}
		// These assertions may fail on loading a library into a chain.
		// assert (familyMatches.size() <= 1) : "unexpected familyMatch count";
		// assert (duplicates.size() <= 1) : "unexpected duplicate count";
		return true;
	}

	private AggregateFamilyNode findFamilyNode(List<Node> children, String familyName) {
		for (Node child : children) {
			if (child instanceof AggregateFamilyNode) {
				if (familyName.equals(child.getName())) {
					return (AggregateFamilyNode) child;
				}
			}
		}
		return null;
	}

	/**
	 * Return a list of component and family nodes whose names begin with the <i>prefix</i> string.
	 */
	private List<Node> findFamilyNameMatches(List<Node> children, String prefix) {
		List<Node> ret = new ArrayList<Node>();
		for (Node c : children) {
			if (c.getName().startsWith(prefix)) {
				ret.add(c);
			}
		}
		return ret;
	}

	/**
	 * examine all children and children of families for exact name match
	 */
	private List<Node> findExactMatches(List<Node> children, String matchName) {
		List<Node> ret = new ArrayList<Node>();
		for (Node c : children) {
			if (c instanceof FamilyNode)
				ret.addAll(findExactMatches(c.getChildren(), matchName));
			else if (c.getName().startsWith(matchName))
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
			if (toBePlaced.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
				n.getVersionNode().setPreviousVersion(toBePlaced);
				toBePlacedVN.setPreviousVersion(n.getVersionNode().getPreviousVersion());
				n = toBePlaced;
			}
			n = n.getVersionNode().getPreviousVersion();
		}

	}

	protected void remove(Node node) {
		if (!getChildren().remove(node)) {
			// if it was not found, it may be in a family node
			ArrayList<Node> kids = new ArrayList<Node>(getChildren());
			for (Node n : kids) {
				if ((n instanceof AggregateFamilyNode) && (n.family.equals(node.family))) {
					((AggregateFamilyNode) n).remove(node);
					if (n.getChildren().isEmpty())
						getChildren().remove(n);
				}
			}
		}
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
		return parent.getLibrary();
	}

	/*
	 * For the non-version aggregates, skip over the version node These are used in the navigator menul.
	 */
	@Override
	public List<Node> getNavChildren() {
		if (aggType.equals(AggregateType.Versions)) {
			return super.getChildren();
		} else {
			ArrayList<Node> kids = new ArrayList<Node>();
			for (Node child : getChildren()) {
				if (child instanceof VersionNode)
					kids.add(((VersionNode) child).getNewestVersion());
			}
			return kids;
		}
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
		return parent.isInTLLibrary();
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
