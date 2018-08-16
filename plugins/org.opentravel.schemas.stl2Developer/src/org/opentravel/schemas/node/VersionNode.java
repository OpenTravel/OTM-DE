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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Version nodes are used in the Versions aggregate to isolate actual component nodes from their parent library. For
 * libraries that are part of a chain, all links to component nodes will be through a version node. For the non-version
 * aggregate nodes, the links are directly to the most current component node.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionNode extends ComponentNode implements FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionNode.class);

	protected VersionManager vm = null;

	/**
	 * Create a version node to wrap the passed node.
	 * 
	 * @param node
	 */
	public VersionNode(ComponentNode node) {
		vm = new VersionManager();
		versionNode = this; // make getVersionNode() tests simpler
		add(node);
	}

	// /**
	// * Create an empty version node linked to its parent.
	// */
	// @Deprecated
	// public VersionNode(AggregateNode parent) {
	// vm = new VersionManager();
	// setLibrary(parent.getLibrary());
	// setParent(parent);
	// parent.getChildren().add(this);
	// versionNode = this; // make getVersionNode() tests simpler
	// }

	// /**
	// * Create a new version node and add the node.
	// *
	// * @param parent
	// * @param nodeToAdd
	// */
	// @Deprecated
	// public VersionNode(AggregateNode parent, ComponentNode nodeToAdd) {
	// this(parent);
	// add(nodeToAdd);
	// }

	/**
	 * Add the passed node to the versioned object chain and set this as the versionNode in the nodeToAdd
	 */
	public void add(Node nodeToAdd) {
		if (nodeToAdd != null) {
			vm.add(nodeToAdd);
			nodeToAdd.setVersionNode(this);
		}
	}

	@Override
	public void close() {
		vm.close();
		deleted = true;
	}

	/**
	 * Return the head (newest) node of the chain represented by this version node.
	 * 
	 * @return node or null
	 */
	@Override
	public Node get() {
		return vm.get();
		// TODO - why are some children empty?
		// return getChildren().isEmpty() ? vm.get() : getChildren().get(0);
		// return vm.get();
	}

	@Override
	/**
	 * Get head's parameterized children handler
	 */
	public ChildrenHandlerI<?> getChildrenHandler() {
		return get() != null ? get().getChildrenHandler() : null;
	}

	@Override
	public String getDecoration() {
		return vm.get().getDecoration();
	}

	@Override
	public TLModelElement getTLModelObject() {
		return vm != null && vm.get() != null ? vm.get().getTLModelObject() : null;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return null; // tl object already points to head.
	}

	public List<Node> getAllVersions() {
		return vm.getAll();
	}

	@Override
	public String getComponentType() {
		return vm.get() != null ? vm.get().getComponentType() : "";
	}

	@Override
	public Image getImage() {
		return vm.get() != null ? vm.get().getImage() : null;
	}

	/**
	 * @return library of head version
	 */
	@Override
	public LibraryNode getLibrary() {
		return vm.get() != null ? vm.get().getLibrary() : null;
		// return parent != null ? parent.getLibrary() : null;
	}

	/**
	 * @return parent of head version
	 */
	@Override
	public Node getParent() {
		return vm.get() != null ? vm.get().getParent() : null;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		// Type providers are delivered from their version nodes.
		return vm.get() != null;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		// this simplifies links from validation, user experience and showing families in the other aggregates.
		return vm.get() != null ? vm.get().getNavChildren(deep) : Collections.EMPTY_LIST;
	}

	// @Override
	// public List<Node> getTreeChildren(boolean deep) {
	// // this simplifies links from validation, user experience and showing families in the other aggregates.
	// return vm.get() != null ? vm.get().getChildrenHandler().getTreeChildren(deep) : Collections.EMPTY_LIST;
	// }

	@Override
	public boolean hasNavChildren(boolean deep) {
		return vm.get() != null ? vm.get().hasNavChildren(deep) : false;
	}

	/**
	 * Insert node in versions list. Update all the newest object links.
	 * 
	 * @param newNode
	 *            is node not in version list to be inserted
	 */
	public void insert(ComponentNode newNode) {
		vm.add(newNode);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return vm.get() != null ? vm.get().isNavChild(deep) : false;
	}

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	/**
	 * @return true if this is new to the chain (prevNode == null). Fast and efficient.
	 */
	@Override
	public boolean isNewToChain() {
		return vm.getPreviousVersion() == null ? true : false;
	}

	/**
	 * Version node is considered deleted when its head object is deleted
	 */
	@Override
	public boolean isDeleted() {
		return get() != null ? get().isDeleted() : true;
	}

	@Override
	public boolean isEditable() {
		return get() != null ? get().isEditable() : false;
	}

	/**
	 * Return owning component of head object
	 */
	@Override
	public LibraryMemberInterface getOwningComponent() {
		return vm.get() != null ? vm.get().getOwningComponent() : null;
	}

	/**
	 * @return the oldest version of this object in the chain
	 */
	public Node getOldestVersion() {
		return vm.getOldestVersion();
	}

	/**
	 * @return the newest version of the object (version head).
	 */
	public Node getNewestVersion() {
		return vm.get();
	}

	/**
	 * Deprecated - let version manager figure out newest/oldest
	 * 
	 * @param head
	 */
	public ComponentNode getPreviousVersion() {
		return (ComponentNode) vm.getPreviousVersion();
	}

	public void setPreviousVersion(ComponentNode previous) {
		vm.add(previous);
	}

	/**
	 * Remove passed child from this version node's version list. If there are no objects remaining in the version list,
	 * the version node is removed from the aggregate parent.
	 */
	protected void remove(final Node node) {
		assert node != null;
		assert getChain() != null;
		assert (node.getLibrary().getChain() == getChain());

		vm.remove(node);

		// If no more versions, then remove version node
		if (vm.get() == null) {
			if (getParent() != null) {
				// NavNode is static -- remove not clear
				if (getParent() instanceof NavNode && node instanceof LibraryMemberInterface)
					((NavNode) getParent()).removeLM((LibraryMemberInterface) node);
				setParent(null);
			}
			deleted = true;
			node.setVersionNode(null);
		}
	}

	@Override
	public String getName() {
		return vm.get() != null ? vm.get().getName() : "";
	}

	public VersionManager getVersionManager() {
		return vm;
	}

	public List<Node> getOlderVersions() {
		return vm.getOlderVersions(get());
	}

	/**
	 * @return true if this version object chain contains the passed node
	 */
	@Override
	public boolean contains(Node node) {
		return vm.contains(node);
	}

	public boolean hasOlder() {
		return vm.getAll().size() > 1;
	}

}
