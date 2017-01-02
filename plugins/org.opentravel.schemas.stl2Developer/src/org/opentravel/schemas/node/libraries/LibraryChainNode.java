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
package org.opentravel.schemas.node.libraries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.AggregateNode.AggregateType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.NamespaceHandler;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Library chains are all libraries based on the same major release. Their content is aggregated in this node.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryChainNode extends Node implements LibraryInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryChainNode.class);

	protected static final String LIBRARY_CHAIN = "Library Collection";

	// Library Chains collect content from all chain libraries organized by the nav-node.
	protected AggregateNode complexRoot;
	protected AggregateNode simpleRoot;
	protected AggregateNode serviceRoot;
	protected AggregateNode resourceRoot;
	protected VersionAggregateNode versions;

	protected RepositoryItem repoItem;
	protected List<LibraryNode> chain;

	protected ProjectItem projectItem; // The TL Project Item wrapped around this library

	/**
	 * Create a new chain and move the passed library from its parent to the chain. The library parent will be used as
	 * the chain's parent. Children are the 4 aggregate nodes linked by their constructors.
	 * 
	 * {@link org.opentravel.schemas.controllers.DefaultRepositoryController#convertToChains(Collection<LibraryNode>)}
	 * 
	 * @param ln
	 *            - library to add to new chain.
	 */
	public LibraryChainNode(LibraryNode ln) {
		super();
		if (ln == null)
			return;
		if (ln.isInChain())
			throw new IllegalStateException("Library is already in a chain.");
		if (ln.getProject() == null)
			throw new IllegalStateException("Library did not have project.");
		if (ln.getParent() == null || (!(ln.getParent() instanceof LibraryNavNode)))
			throw new IllegalStateException("Library parent is not a LibraryNavNode.");

		// Inform the LibraryModelManager of the change
		((LibraryNavNode) ln.getParent()).setThisLib(this);
		getModelNode().getLibraryManager().replace(ln, this);

		parent = ln.getParent();
		setHead(ln);
		createAggregates();
		versions.add(ln); // add to children and set parent

		aggregateChildren(ln);
		ln.updateLibraryStatus();

		// LOGGER.debug("Created library chain " + this.getLabel());
	}

	private void createAggregates() {
		versions = new VersionAggregateNode(AggregateType.Versions, this);
		complexRoot = new AggregateNode(AggregateType.ComplexTypes, this);
		simpleRoot = new AggregateNode(AggregateType.SimpleTypes, this);
		serviceRoot = new AggregateNode(AggregateType.Service, this);
		resourceRoot = new AggregateNode(AggregateType.RESOURCES, this);
	}

	/**
	 * Create a new chain and add to passed project. Model the project item and add to the new chain.
	 * 
	 * @param pi
	 *            - project item to be modeled and added to chain
	 * @param projNode
	 *            - parent of the chain
	 */
	public LibraryChainNode(ProjectItem pi, ProjectNode projNode) {
		super();
		if (pi == null || pi.getContent() == null) {
			// LOGGER.debug("Null project item content!");
			return;
		}
		if (pi.getContent().getOwningModel() == null) {
			// LOGGER.debug("Project item does not have owning model.");
			return;
		}

		// LOGGER.debug("Creating chain for project item " + pi.getLibraryName());

		setParent(new LibraryNavNode(this, projNode));
		getModelNode().getLibraryManager().add(this);

		setHead(null);

		chain = new ArrayList<LibraryNode>();
		createAggregates();
		List<ProjectItem> piChain = null;
		try {
			piChain = pi.getProjectManager().getVersionChain(pi);
		} catch (VersionSchemeException e1) {
			throw (new IllegalStateException("Could not get chain from project manager. " + e1.getLocalizedMessage()));
		}

		for (ProjectItem item : piChain)
			add(item);

		// Now that we know what is the head library, set that in the aggregates
		// setAggregateLibrary(getHead());
	}

	/**
	 * Add this project item to the version chain.
	 * 
	 * @return the library node added to the chain or null if it already was in the chain.
	 * @param pi
	 */
	public LibraryNode add(ProjectItem pi) {
		// If the chain already has this PI, skip it.
		LibraryNode newLib = versions.get(pi);

		if (newLib == null) {
			// LOGGER.debug("Adding pi " + pi.getFilename() + " to chain " + getLabel());
			newLib = new LibraryNode(pi, this);
			versions.add(newLib); // simply add this library to library list.
			newLib.updateLibraryStatus();
		}
		if (getHead() == null || newLib.getTLaLib().isLaterVersion(getHead().getTLaLib()))
			setHead(newLib);

		return newLib;
	}

	/**
	 * Same as lcn.getLibrary().
	 * 
	 * @return library at the head of the chain.
	 */
	public LibraryNode getHead() {
		return getLibrary();
	}

	/**
	 * @return the major version that anchors this chain
	 */
	public LibraryNode getMajor() {
		for (LibraryNode ln : getLibraries())
			if (ln.isMajorVersion())
				return ln;
		return null;
	}

	private void setHead(LibraryNode newHead) {
		setLibrary(newHead);
	}

	@Override
	public void setLibrary(LibraryNode ln) {
		library = ln;
		// super.setLibrary(ln); // sets the library in all the children.
	}

	// /**
	// * Sets the library in all the aggregate nodes.
	// */
	// private void setAggregateLibrary(LibraryNode ln) {
	// LOGGER.debug("Setting library in chain to " + ln.getNameWithPrefix());
	// // versions.setLibrary(ln);
	// complexRoot.setLibrary(ln);
	// simpleRoot.setLibrary(ln);
	// serviceRoot.setLibrary(ln);
	// resourceRoot.setLibrary(ln);
	// }

	/**
	 * Return true if 1st node is from a later version that node2. For example: (v01:flight, v00:flight) returns true.
	 * 
	 * @param node1
	 * @param node2
	 */
	public boolean isLaterVersion(Node node1, Node node2) {
		return node1.getLibrary().getTLaLib().isLaterVersion(node2.getLibrary().getTLaLib());
	}

	/**
	 * Add the passed node to the appropriate chain aggregate. Wrap the node in a version node in the library's children
	 * list.
	 * 
	 * NOTE: may create invalid chain as there may be undetected name collisions
	 * 
	 * @param node
	 */
	public void add(ComponentNode node) {
		if (node == null)
			return;
		if (node.getLibrary() == null)
			throw new IllegalArgumentException("Tried to add node with null library. " + node);

		// LOGGER.debug("Adding " + node + " to library chain.");

		// For services and resource, just add to the appropriate root.
		if (node instanceof ServiceNode)
			serviceRoot.add(node);
		else if (node instanceof ResourceNode)
			resourceRoot.add(node);
		// Otherwise add version wrapper if not already wrapped
		else if (!(node.getParent() instanceof VersionNode))
			new VersionNode(node);

		// Add to chain object aggregates.
		if (node instanceof ComplexComponentInterface)
			complexRoot.add(node);
		else if (node instanceof SimpleComponentInterface)
			simpleRoot.add(node);
		// else
		// LOGGER.warn("add skipped: " + node);
	}

	/**
	 * Remove the node from the appropriate aggregate node. This does not delete the node, just remove it from aggregate
	 * list and takes care of family if needed. Replaces with previous version if found.
	 * 
	 * @param n
	 */
	public void removeAggregate(ComponentNode node) {
		// Remove this version.
		if (node instanceof ComplexComponentInterface)
			complexRoot.remove(node);
		else if (node instanceof SimpleComponentInterface)
			simpleRoot.remove(node);
		else if (node instanceof ResourceNode)
			resourceRoot.remove(node);
		else if (node instanceof ServiceNode || node instanceof OperationNode)
			serviceRoot.remove(node);

		// LOGGER.debug("Adding back the previous version of " + node);
		add(findPreviousVersion(node));
	}

	/**
	 * @return true if this chain contains the node's library
	 */
	public boolean contains(Node node) {
		return versions.getChildren().contains(node.getLibrary());
	}

	/**
	 * Find the "latest" previous version of the node if not deleted.
	 * 
	 * @param node
	 */
	private ComponentNode findPreviousVersion(ComponentNode node) {
		if (node.getVersionNode() != null && !node.getVersionNode().isDeleted())
			return node.getVersionNode().getPreviousVersion();
		return null;
	}

	/**
	 * Add each named-type descendant to the chain.
	 * 
	 * @param lib
	 */
	private void aggregateChildren(LibraryNode lib) {
		if (lib.getServiceRoot() != null) {
			add((ComponentNode) lib.getServiceRoot());
		}
		for (Node n : lib.getDescendentsNamedTypes()) {
			add((ComponentNode) n);
		}
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	public boolean isEmpty() {
		if (complexRoot.isEmpty())
			if (simpleRoot.isEmpty())
				if (serviceRoot.isEmpty())
					if (resourceRoot.isEmpty())
						return true;
		return false;
	}

	/**
	 * Walk all members and close them. Caller is responsible to assure this library chain is not used in other projects
	 * {@link #LibraryModelManager}
	 */
	@Override
	public void close() {
		// LOGGER.debug("Closing " + getNameWithPrefix());
		if (getParent() instanceof LibraryNavNode) {
			((LibraryNavNode) getParent()).close();
		} else {
			// Take kids out of chain then close them
			for (Node n : getChildren_New()) {
				n.setParent(null);
				n.close();
			}
			deleted = true;
			setParent(null);
			setLibrary(null);
		}
		assert (isEmpty());
		assert (getHead() == null);
	}

	@Override
	public String getComponentType() {
		return LIBRARY_CHAIN;
	}

	@Override
	public LibraryChainNode getChain() {
		return this;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.libraryChain);
	}

	@Override
	public String getLabel() {
		return getHead() != null ? getHead().getLabel() : "VersionChain";
	}

	@Override
	public List<LibraryNode> getLibraries() {
		ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>();
		if (versions != null)
			for (Node n : versions.getChildren())
				if (n instanceof LibraryNode)
					libs.add((LibraryNode) n);
		return libs;
	}

	@Override
	public String getName() {
		String label = "Version Chain";
		if (getHead() != null) {
			NamespaceHandler handler = getHead().getNsHandler();
			if (handler != null)
				label = getHead().getName() + "-" + handler.getNSVersion(getHead().getNamespace());
		}
		return label;
	}

	/**
	 * See also {@link ProjectNode#makeChainIdentity(ProjectItem)} 9/23/2013 - this method does not use the repository
	 * for managed base namespaces. It matches the behavior or makeChainIdentity in ProjectNode.
	 */
	public String makeChainIdentity() {
		assert (getHead() != null);

		String name = getHead().getName();
		NamespaceHandler handler = getHead().getNsHandler();
		String baseNS = handler.removeVersion(getHead().getNamespace());
		return makeIdentity(name, baseNS, handler.getNS_Major(getHead().getNamespace()));
	}

	/**
	 * See also {@link ProjectNode#makeChainIdentity(ProjectItem)}
	 */
	// TODO - see if users of this method should be using chainIdentity()
	public String makeIdentity() {
		String name = getHead().getName();
		NamespaceHandler handler = getHead().getNsHandler();
		return makeIdentity(name, handler.getNSBase(getHead().getNamespace()),
				handler.getNS_Major(getHead().getNamespace()));
	}

	public static String makeIdentity(String name, String baseNS, String majorNS) {
		return name + ":" + baseNS + ":" + majorNS;
	}

	/**
	 * @return the project containing this chain. Null if no project is found.
	 */
	public ProjectNode getProject() {
		if (getParent() instanceof LibraryNavNode)
			return ((LibraryNavNode) getParent()).getProject();
		else
			return getParent() instanceof ProjectNode ? (ProjectNode) getParent() : null;
	}

	/**
	 * Get the parent of the actual libraries in the chain.
	 * 
	 * @return - the version aggregate node
	 */
	public Node getVersions() {
		return versions;
	}

	@Override
	public boolean isEditable() {
		// True if any library is editable.
		for (Node ln : versions.getChildren())
			if (ln.isEditable())
				return true;
		return false;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

	public INode getSimpleAggregate() {
		return simpleRoot;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> treeKids = getNavChildren(deep);
		if (!treeKids.contains(getHead().getWhereUsedHandler().getWhereUsedNode()))
			treeKids.add(getHead().getWhereUsedHandler().getWhereUsedNode());
		if (!treeKids.contains(getHead().getWhereUsedHandler().getUsedByNode()))
			treeKids.add(getHead().getWhereUsedHandler().getUsedByNode());
		return treeKids;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true; // include where used and uses from
	}

	public boolean isPatch() {
		return getHead().isPatchVersion();
	}

	public boolean isMinor() {
		return getHead().isMinorOrMajorVersion();
	}

	/**
	 * @return true if head library is a major version
	 */
	public boolean isMajor() {
		return getHead().isMajorVersion();
	}

	public INode getResrouceAggregate() {
		return resourceRoot;
	}

	public INode getServiceAggregate() {
		return serviceRoot;
	}

	public INode getComplexAggregate() {
		return complexRoot;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return versions.getChildren().size() > 0 ? true : false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	/**
	 * Return the Simple/Complex/Service navNode in the latest library that matches the type of this node. *
	 * 
	 * @param parent
	 */
	public NavNode getLatestNavNode(ComponentNode node) {
		Node parent = node.getOwningNavNode();
		for (Node nav : getHead().getChildren()) {
			if (parent.getComponentType().equals(nav.getComponentType()))
				return (NavNode) nav;
		}
		return null;
	}

	@Override
	public ValidationFindings validate() {
		ValidationFindings findings = new ValidationFindings();

		for (LibraryNode ln : getLibraries())
			findings.addAll(TLModelCompileValidator.validateModelElement(ln.getTLaLib()));

		return findings;
	}

	public boolean hasService() {
		return !serviceRoot.getChildren().isEmpty();
	}

	public boolean hasResources() {
		return !resourceRoot.getChildren().isEmpty();
	}

	@Override
	public LibraryNavNode getLibraryNavNode() {
		return (LibraryNavNode) getParent();
	}

}
