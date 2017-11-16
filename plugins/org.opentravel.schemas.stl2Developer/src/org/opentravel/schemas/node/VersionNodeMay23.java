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


/**
 * 
 * Version nodes are used in the Versions aggregate to isolate actual component nodes from their parent library. For
 * libraries that are part of a chain, all links to component nodes will be through a version node. For the non-version
 * aggregate nodes, the links are directly to the most current component node.
 * 
 * @author Dave Hollander
 * 
 */
//
// TODO - make the subject a LibraryMemberInterface
// TODO - try to convert this to a handler
// TODO - do NOT keep model object - return head.gettlmodelobject()
//
//@Deprecated
//public class VersionNodeMay23 extends ComponentNode implements FacadeInterface {
//	private static final Logger LOGGER = LoggerFactory.getLogger(VersionNodeMay23.class);
//
//	protected ComponentNode head; // link to the latest/newest version of this object
//	protected ComponentNode prevVersion; // link to the preceding version. If null, it is new to the
//											// chain.
//
//	// ***** VersionManager vm = new VersionManager(this);
//
//	/**
//	 * Creates the version node and inserts into the library before the passed node. This does NOT place this node into
//	 * the Aggregates. Set previous version to null (new to chain).
//	 */
//	public VersionNodeMay23(ComponentNode node) {
//		super(node.getTLModelObject()); // creates 2nd listener
//		if (node.getParent() == null)
//			throw new IllegalStateException("Version node - " + node + " parent is null.");
//		if (node.getLibrary() == null)
//			throw new IllegalStateException("Version Head library is null.");
//		// added 4/10/2017 dmh
//		if (node.getVersionNode() != null)
//			// throw new IllegalStateException(node + " is already wrapped by a version node.");
//			LOGGER.debug(node + " is already wrapped by a version node.");
//
//		// Fail if in the list more than once.
//		assert (node.getParent().getChildren().indexOf(node) == node.getParent().getChildren().lastIndexOf(node));
//
//		getChildren().add(node);
//		head = node;
//		prevVersion = null;
//		// node.setVersionNode(this);
//		setLibrary(node.getLibrary());
//
//		// Insert this between parent and node.
//		setParent(node.getParent());
//		node.getParent().getChildren().remove(node);
//		node.getParent().getChildren().add(this);
//		node.setParent(this);
//
//		// Replace listener on the head node's tl Model Element
//		// ListenerFactory.setListner(head); // creates 3rd listener
//		assert GetNode(getTLModelObject()) == head; // make sure listener is correct.
//		assert (getParent() != null);
//		assert (!getParent().getChildren().contains(node)) : "Parent still contains node.";
//		assert (getChildren().contains(node)) : "Version node does not contain node.";
//		assert (node.getParent() == this) : "Node is not linked to version node.";
//	}
//
//	// TODO - TEST/FIX ME
//	// this creates a model object with the initial TLModelElement. Why?
//	// Does getTLModelElement need to be trapped here? Get ModelObject?
//
//	/**
//	 * Return the actual node wrapped by this version node.
//	 * 
//	 * @return node or null
//	 */
//	// ***** vm.get()
//	public Node get() {
//		// Older versions will have head set to the latest version.
//		return getChildren().isEmpty() ? null : getChildren().get(0);
//		// return head;
//	}
//
//	// ***** vm.get().getTLModelObject();
//	@Override
//	public TLModelElement getTLModelObject() {
//		if (head != null)
//			return head.getTLModelObject();
//		// Head is not set until after needed in super() constructor
//		if (getModelObject() == null)
//			assert false;
//		return (TLModelElement) getModelObject().getTLModelObj();
//	}
//
//	@Override
//	public BaseNodeListener getNewListener() {
//		return null; // tl object already points to head.
//	}
//
//	// ***** vm.getAll()
//	public List<Node> getAllVersions() {
//		List<Node> versions = new ArrayList<Node>();
//		Node v = head;
//		do {
//			versions.add(v);
//			v = v.getVersionNode().getPreviousVersion();
//		} while (v != null);
//		return versions;
//	}
//
//	@Override
//	public String getComponentType() {
//		if (getNewestVersion() == null || getNewestVersion().getComponentNodeType() == null)
//			return "";
//		return getNewestVersion().getComponentNodeType().getDescription();
//	}
//
//	@Override
//	public Image getImage() {
//		return Images.getImageRegistry().get(Images.libraryChain);
//		// return head.getImage();
//	}
//
//	@Override
//	public boolean hasChildren_TypeProviders() {
//		// Type providers are delivered from their version nodes.
//		return head != null;
//	}
//
//	@Override
//	public List<Node> getNavChildren(boolean deep) {
//		// this simplifies links from validation, user experience and showing families in the other aggregates.
//		return getNewestVersion().getNavChildren(deep);
//	}
//
//	@Override
//	public boolean hasNavChildren(boolean deep) {
//		return getNewestVersion().hasNavChildren(deep);
//	}
//
//	/**
//	 * Insert node in versions list. Update all the newest object links.
//	 * 
//	 * @param newNode
//	 *            is node not in version list to be inserted
//	 */
//	// ***** vm.add(newNode);
//	public void insert(ComponentNode newNode) {
//		boolean isHead = false;
//		// Find out if it is the head version
//		for (Node n : getChildren())
//			if (!newNode.isLaterVersion(n)) {
//				isHead = false; // It is not head, so just add to list
//				break;
//			}
//
//		getChildren().add(newNode);
//		if (isHead) {
//			// Make head
//			setPreviousVersion(head);
//			setNewestVersion(newNode);
//			// } else {
//			// // Just add to children
//		}
//		// if (newNode.isLaterVersion(newest))
//		// toBePlaced.getVersionNode().setNewestVersion(newest);
//		// if (toBePlaced.getVersionNode().getPreviousVersion() == null) {
//		// newest.getVersionNode().setPreviousVersion(toBePlaced);
//		// return;
//		// }
//
//		// toBePlaced.getVersionNode().setNewestVersion(newest);
//		// VersionNode toBePlacedVN = toBePlaced.getVersionNode();
//		// ComponentNode n = toBePlacedVN.getPreviousVersion();
//		// while (n != null) {
//		// n.getVersionNode().setNewestVersion(newest);
//		// if (toBePlaced.isLaterVersion(n)) {
//		// // if (toBePlaced.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
//		// n.getVersionNode().setPreviousVersion(toBePlaced);
//		// toBePlacedVN.setPreviousVersion(n.getVersionNode().getPreviousVersion());
//		// n = toBePlaced;
//		// }
//		// n = n.getVersionNode().getPreviousVersion();
//		// }
//	}
//
//	@Override
//	public boolean isNavChild(boolean deep) {
//		return getNewestVersion().isNavChild(deep);
//	}
//
//	@Override
//	public boolean isNamedEntity() {
//		return false;
//	}
//
//	// public boolean isHead(Node candidate) {
//	// return candidate == head;
//	// }
//
//	/**
//	 * @return true if this is new to the chain (prevNode == null). Fast and efficient.
//	 */
//	// ***** vm.getprevVersion == null ? true : false;
//	public boolean isNewToChain() {
//		return prevVersion == null ? true : false;
//	}
//
//	@Override
//	public boolean isEditable() {
//		return false;
//	}
//
//	/**
//	 * Return owning component of head object
//	 */
//	// ***** vm.get().getOwningComponent();
//	@Override
//	public Node getOwningComponent() {
//		return head != null ? head.getOwningComponent() : null;
//	}
//
//	/**
//	 * @return the oldest version of this object in the chain
//	 */
//	// public Node getOldestVersion() {
//	// VersionNodeMay23 vn = this;
//	// while (!vn.isNewToChain())
//	// if (vn == vn.getPreviousVersion().getVersionNode())
//	// return vn.get(); // FIXME - bad version chain
//	// else
//	// vn = vn.getPreviousVersion().getVersionNode();
//	// // vn = prevVersion.getVersionNode();
//	// return vn.get();
//	// }
//
//	/**
//	 * Simple getter of the head field.
//	 * 
//	 * @return the newest version of the object (version head).
//	 */
//	// ***** vm.get()
//	public Node getNewestVersion() {
//		return head;
//	}
//
//	/**
//	 * Simple getter of the head field.
//	 * 
//	 * @return the newest version of the object (version head).
//	 */
//	// ***** vm.get()
//	public Node getHead() {
//		return head;
//	}
//
//	public void setNewestVersion(ComponentNode head) {
//		this.head = head;
//	}
//
//	/**
//	 * @return the previous version of the object (if any).
//	 */
//	public ComponentNode getPreviousVersion() {
//		return prevVersion;
//	}
//
//	// ***** vm.add()
//	public void setPreviousVersion(ComponentNode previous) {
//		this.prevVersion = previous;
//	}
//
//	/**
//	 * Remove passed child from this version node's children list AND aggregate.
//	 */
//	// ***** vm.remove()
//	@Override
//	protected void remove(final Node node) {
//		assert node != null;
//		assert getChildren() != null;
//		assert getChain() != null;
//		assert (node.getLibrary().getChain() == getChain());
//
//		// Remove from this version node
//		if (getChildren().contains(node))
//			getChildren().remove(node);
//		head = getPreviousVersion(); // will be null if there is no previous version
//
//		// Remove from the library
//		// if (head == null)
//		// if (getParent() != null)
//		// getParent().remove(this);
//
//		// delete copy in the version aggregate
//		getChain().removeAggregate((ComponentNode) node);
//	}
//
//	@Override
//	public String getName() {
//		return head != null ? head.getName() + " (v)" : "";
//	}
//
// }
