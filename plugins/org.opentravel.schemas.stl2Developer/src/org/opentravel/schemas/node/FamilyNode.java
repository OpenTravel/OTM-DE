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
package org.opentravel.schemas.node;

@Deprecated
public class FamilyNode extends NavNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNode.class);
	// protected String family; // name stripped at the first under bar

	/**
	 * Constructor that creates a family node based on the node parameter. Move the node and its peer into the family
	 * node. <b>Does</b> move version nodes but <b>does not</b> do the aggregate node.
	 * 
	 * @param n
	 *            - one of the family. Must have parent and is used to find the new family node parent.
	 * @param peer
	 *            - node to add to the family
	 */
	public FamilyNode(final Node n, final Node peer) {
		super(n.getName(), n.getParent());
		// super(NodeNameUtils.makeFamilyName(n.getName()), n.getParent());
		assert (peer != null) : "Family Node functions have been removed.";
		// assert (parent != null) : "Parent is null.";
		// assert (!(n.getParent() instanceof FamilyNode)) : "wrong parent type";
		// assert (!(peer.getParent() instanceof FamilyNode)) : "wrong parent type";
		//
		// // setName(n.family.isEmpty() ? NodeNameUtils.makeFamilyName(n.getName()) : n.family);
		// // setIdentity("Family:" + getName());
		// // done in super() - setLibrary(n.getLibrary());
		//
		// // link n and peer to this family node.
		// // If managed library, move their version nodes instead.
		// Node move1 = n;
		// Node move2 = peer;
		// if (n.getVersionNode() != null)
		// move1 = n.getVersionNode();
		// if (peer.getVersionNode() != null)
		// move2 = peer.getVersionNode();
		//
		// move1.setParent(this);
		// move2.setParent(this);
		// getChildren().add(move1);
		// getChildren().add(move2);
		// getParent().remove(move1);
		// getParent().remove(move2);

		// LOGGER.debug("Family "+getName()+" created for "+n.getName());
	}

	/**
	 * Constructor for use with sub-types that do not maintain 2 way links with children (AggregateFamilyNode).
	 * 
	 * @param name
	 * @param parent
	 */
	public FamilyNode(final String name, final Node parent) {
		super(name, parent);
		assert (parent != null) : "Family Node functions have been removed.";
		// setIdentity("Family2:" + getName());
	}

	// public void add(Node newNode) {
	// assert newNode.getParent() != null;
	//
	// newNode.getParent().remove(newNode);
	// getChildren().add(newNode);
	// newNode.setParent(this);
	// }

	@Override
	public String getComponentType() {
		return "Family";
	}

	// @Override
	// public List<Node> getNavChildren() {
	// ArrayList<Node> kids = new ArrayList<Node>();
	// for (Node child : getChildren()) {
	// if (child instanceof VersionNode)
	// kids.add(((VersionNode) child).getNewestVersion());
	// else
	// kids.add(child);
	// }
	// return kids;
	// }

	// @Override
	// public List<Node> getDescendants_NamedTypes() {
	// ArrayList<Node> kids = new ArrayList<Node>();
	// for (Node c : getChildren()) {
	// if (c.isTypeProvider())
	// kids.add(c);
	// if (c instanceof VersionNode && c.getChildren().size() > 0)
	// kids.add(c.getChildren().get(0));
	// }
	// return kids;
	// }

	// @Override
	// public String getName() {
	// return family;
	// }
	//
	// @Override
	// public String getLabel() {
	// return family;
	// }

	// @Override
	// public Image getImage() {
	// final ImageRegistry imageRegistry = Images.getImageRegistry();
	// return imageRegistry != null ? imageRegistry.get("family") : null;
	// }

	// @Override
	// public boolean hasNavChildren() {
	// return !getChildren().isEmpty();
	// }

	// @Override
	// public boolean hasChildren_TypeProviders() {
	// return getChildren().size() > 0;
	// }

	// @Override
	// public boolean isNavigation() {
	// return true;
	// }

	// @Override
	// public boolean isImportable() {
	// // False if any child is not importable.
	// boolean importable = true;
	// for (final Node n : getChildren()) {
	// if (!n.isImportable()) {
	// importable = false;
	// break;
	// }
	// }
	// return importable;
	// }

	// @Override
	// public void setName(final String name) {
	// family = name;
	// // Rename all the children just the first part of the name
	// for (Node kid : getChildren())
	// kid.setName(name + "_" + NodeNameUtils.getGivenName(kid.getName()));
	// }

	// protected void updateFamily() {
	// if (getParent() == null)
	// return; // During construction or delete, may not have a parent, do nothing.
	// if (getChildren().size() > 1)
	// return; // if more than 2 siblings in the family and there is nothing to do.
	//
	// // If only one is left, move it up.
	// final Node parent = getParent();
	// if (getChildren().size() == 1) {
	// final Node child = getChildren().get(0);
	// parent.getChildren().add(child);
	// child.setParent(parent);
	// }
	// // Delete the family node. Don't use delete() because that does
	// // children.
	// if (!parent.getChildren().remove(this))
	// LOGGER.info("Error removing " + this.getName() + " from " + parent.getName());
	// this.getChildren().clear();
	// this.setParent(null);
	// }

	// /**
	// * Remove passed child from this family node. If the family is left with one member, then that member is moved to
	// * the parent and this family is deleted.
	// */
	// @Override
	// protected void remove(final Node n) {
	// if (getChildren().contains(n)) {
	// getChildren().remove(n);
	// if (getChildren().size() == 1) {
	// // No longer a family. Link last child the delete this
	// final Node lastChild = getChildren().get(0);
	// parent.getChildren().add(lastChild);
	// lastChild.setParent(parent);
	// delete();
	// }
	// } else
	// LOGGER.warn(n + " can not be removed because it is not a child of " + this);
	// }

	/**
	 * Delete this family node. Remove all children if any and remove this family from parent.
	 */
	public void delete() {
		if (!getChildren().isEmpty())
			getChildren().clear();
		if (parent != null)
			if (parent.getChildren() != null)
				if (parent.getChildren().contains(this))
					parent.getChildren().remove(this);
		parent = null;
		deleted = true;
		setLibrary(null);
	}
}
