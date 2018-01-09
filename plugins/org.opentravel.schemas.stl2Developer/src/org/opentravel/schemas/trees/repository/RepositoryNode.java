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
package org.opentravel.schemas.trees.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.PatternFilter;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository Node Structure
 * 
 * The node structure models the repository content. It uses the base Node, but maintains its own volatile children (I
 * think to allow decoration in background thread without conflict). The node tree is lazily built triggered by
 * accessing the children (getChildren). Note, the PathNodes are built using paths identified when the repository root
 * node scans all namespaces.
 * 
 * @author Dave
 */

// Parent -> child relationships.
// * Tree Root -> Instance -> Root Ns -> Path -> Chain -> Item

// Repository related Objects:
// * (Default) Repository controller - manage the repository interface
// * Repository Menus - manage the GUI menus
// * Repository View - manage the eclipse GUI
// * RepositoryPropertySource
// * Repository Tree Content Provider - impl of ITreeContentProvider
// * Library Decorator - add decorations after names

public abstract class RepositoryNode extends Node implements Comparable<RepositoryNode> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryNode.class);
	private static final String REPOSITORY = "Repository";
	private Repository repository; // Repository Impl from schema compiler
	private volatile List<Node> vChildren = null;

	public RepositoryNode(Repository repository) {
		this.repository = repository;
	}

	@Override
	public abstract String getName();

	@Override
	public Image getImage() {
		return null;
	}

	public Repository getRepository() {
		return repository;
	}

	@Override
	public final List<Node> getChildren() {
		if (vChildren == null) {
			synchronized (this) {
				if (vChildren == null) {
					vChildren = initChildren();
				}
			}
		}
		return vChildren;
	}

	/**
	 * Get all repository item nodes for all libraries in the repository. WARNING: this uses the lazy evaluation based
	 * tree so will be slow the first time called.
	 * 
	 * @return list of all repository item nodes from all repository namespaces
	 */
	public List<Node> getDescendents_RepositoryItems() {
		List<Node> rnKids = new ArrayList<Node>();
		for (Node rnChild : getChildren())
			rnKids.addAll(getDescendents_RepositoryItems(rnChild));
		return rnKids;
	}

	private List<Node> getDescendents_RepositoryItems(Node n) {
		List<Node> kids = new ArrayList<Node>();
		for (Node m : n.getChildren())
			if (m instanceof RepositoryItemNode)
				kids.add(m);
			else if (m.hasChildren())
				kids.addAll(getDescendents_RepositoryItems(m));
		return kids;
	}

	/**
	 * @return list of latest repository versions for passed namespace
	 */
	protected List<RepositoryItem> listLatestItems(String baseName) {
		try {
			return getRepository().listItems(baseName, true, true); // 1 item per name
		} catch (RepositoryException e) {
			LOGGER.error("Couldn't fetch items under basename: " + baseName);
		}
		return Collections.emptyList();
	}

	public void refresh() throws RepositoryException {
		vChildren = null;
	}

	/**
	 * This method is used by {@link PatternFilter} to don't check children of this children if not needed.
	 * 
	 * @return true if children were initialized. This method should return false if called immediately after refresh()
	 *         and before first calls of getChildren().
	 */
	public boolean wasVisited() {
		return vChildren != null;
	}

	protected List<Node> initChildren() {
		return Collections.emptyList();
	}

	public boolean isRemote() {
		return repository instanceof RemoteRepository;
	}

	public String getLocation() {
		if (repository instanceof RemoteRepositoryClient) {
			return ((RemoteRepositoryClient) repository).getEndpointUrl();
		}
		return "";
	}

	@Override
	public String getNamespace() {
		String ns = "Unknown";
		try { // the first of the managed namespaces.
			ns = repository.listRootNamespaces().get(0);
		} catch (RepositoryException e) {
		}
		return ns;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public String getComponentType() {
		return REPOSITORY;
	}

	public String getPermission() {
		return "";
	}

	@Override
	public int compareTo(RepositoryNode r2) {
		return this.getName().compareTo(r2.getName());
		// return 0;
	}

	/**
	 * Repository Root.
	 * 
	 * Contains list of RepositoryInstanceNodes, one for each repository.
	 */
	public static class RepositoryTreeRoot extends RepositoryNode {
		public RepositoryTreeRoot(Repository repository) {
			super(repository);
		}

		@Override
		public String getName() {
			return null;
		}

		/**
		 * @return local repo + other remote repos(non-Javadoc)
		 */
		@Override
		public List<Node> initChildren() {
			if (getRepository() instanceof RepositoryManager) {
				ArrayList<Node> repos = new ArrayList<Node>();
				RepositoryManager mgr = (RepositoryManager) getRepository();
				repos.add(create(mgr));
				for (RemoteRepository rr : mgr.listRemoteRepositories()) {
					repos.add(create(rr));
				}
				return repos;
			}
			return Collections.emptyList();
		}

		private RepositoryInstanceNode create(Repository newRepo) {
			RepositoryInstanceNode rn = new RepositoryInstanceNode(newRepo);
			rn.setParent(this);
			return rn;
		}

		public void addRepository(Repository newRepo) {
			getChildren().add(create(newRepo));
		}

		public void removeRepository(Repository toDelete) {
			RepositoryNode toDeleteChild = find(toDelete);
			if (toDelete != null) {
				getChildren().remove(toDeleteChild);
			}
		}

		public RepositoryNode find(Repository repository) {
			for (Node n : getChildren()) {
				if (n instanceof RepositoryNode) {
					if (isEqual(((RepositoryNode) n).getRepository(), repository))
						return (RepositoryNode) n;
				}
			}
			return null;
		}

		private boolean isEqual(Repository repository, Repository repository2) {
			boolean ret = false;
			if (repository != null) {
				ret = ret || repository == repository2;
				if (repository2 != null)
					ret = ret || repository.getId().equals(repository2.getId());
			}
			return ret;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.opentravel.schemas.node.Node#getTLModelObject()
		 */
		@Override
		public TLModelElement getTLModelObject() {
			return null;
		}

	}

	/**
	 * Repository Instance Node - one of these nodes per local and remote repository
	 * 
	 * Contains list of "root namespaces" reported back from the repository. The repository is configured with one or
	 * more "root namespaces".
	 *
	 */
	public static class RepositoryInstanceNode extends RepositoryNode {

		public RepositoryInstanceNode(Repository repo) {
			super(repo);
		}

		@Override
		public String getName() {
			return getRepository().getDisplayName();
		}

		@Override
		public Image getImage() {
			return Images.getImageRegistry().get(Images.Repository);
		}

		@Override
		public void refresh() throws RepositoryException {
			if (isRemote()) {
				((RemoteRepository) getRepository()).refreshRepositoryMetadata();
			}
			super.refresh();
		}

		@Override
		protected List<Node> initChildren() {
			List<Node> namespaces = new ArrayList<Node>();
			try {
				for (String root : getRepository().listRootNamespaces()) {
					RepositoryRootNsNode nn = new RepositoryRootNsNode(root, getRepository());
					nn.setParent(this);
					namespaces.add(nn);
				}
				return namespaces;
			} catch (RepositoryException e) {
				return Collections.emptyList();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getRepository() == null) ? 0 : getRepository().hashCode());
			return result;
		}

		// override to make sure that viewer will be able to restore selected items after refresh
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RepositoryNode other = (RepositoryNode) obj;
			if (getRepository() == null) {
				if (other.getRepository() != null)
					return false;
			} else if (!getRepository().equals(other.getRepository()))
				return false;
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.opentravel.schemas.node.Node#getTLModelObject()
		 */
		@Override
		public TLModelElement getTLModelObject() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * Root Namespace - represents one of the "root namespaces" configured in a repository.
	 * 
	 * Contains a list of RepositoryPathNodes and possibly ChainNodes.
	 *
	 */
	public static class RepositoryRootNsNode extends RepositoryNode {
		// private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryRootNsNode.class);

		private String namespace;
		private RepositoryPermission permission;

		public RepositoryRootNsNode(String name, Repository repository) {
			super(repository);
			this.namespace = name;
			// LOGGER.debug("Created Namespace Node: " + name);
		}

		@Override
		public void refresh() throws RepositoryException {
			permission = null;
			super.refresh();
		}

		@Override
		public Image getImage() {
			return Images.getImageRegistry().get(Images.NamespaceManaged);
		}

		@Override
		public String getName() {
			return getNamespace();
		}

		@Override
		public String getNamespace() {
			return namespace;
		}

		public String getRootBasename() {
			return namespace;
		}

		@Override
		protected List<Node> initChildren() {
			List<Node> baseNsNodes = new ArrayList<Node>();

			// Get "baseNamespaces" from repository. e.g. http://www.opentravel.org/OTM/demo/test/SolutionOTA
			List<String> namespaces = getBaseNamespaces();

			// Trim list down to just ns roots
			Map<String, RepositoryPathNode> baseNsMap = new HashMap<String, RepositoryPathNode>();
			for (String ns : namespaces) {
				if (!(ns.equals(namespace)) && ns.startsWith(namespace)) {
					String baseName = ns.substring(namespace.length() + 1); // remove leading slash
					if (baseName.indexOf('/') < 0) {
						// this is a base namespace in the target root namespace
						if (!baseNsMap.containsKey(baseName)) {
							RepositoryPathNode bs = new RepositoryPathNode(ns, getRootBasename(), getRepository());
							baseNsMap.put(baseName, bs);
							bs.setParent(this);
							baseNsNodes.add(bs); // save node
						}
					} else {
						// The ns is a child of this base namespace - create a chain node for it
						String parentName = baseName.substring(0, baseName.indexOf('/'));
						RepositoryPathNode parent = baseNsMap.get(parentName);
						if (parent != null) {
							baseName = baseName.substring(baseName.indexOf('/'));
							parent.addChildPath(baseName);
							// LOGGER.debug("Need child " + baseName + " of parent " + parentName);
						}
					}
				}
			}

			// finally, create chains if there are any directly managed libraries.
			for (RepositoryItem ri : listLatestItems(getNamespace())) {
				RepositoryChainNode cn = new RepositoryChainNode(getRepository(), this, "");
				baseNsNodes.add(cn);
			}
			return baseNsNodes;
		}

		private List<String> getBaseNamespaces() {
			try {
				return getRepository().listBaseNamespaces();
			} catch (RepositoryException e) {
				LOGGER.error("Could not fetch base-namespaces from repository: " + getRepository().getDisplayName()
						+ ", reason: " + e.getMessage());
				return Collections.emptyList();
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getRepository() == null) ? 0 : getRepository().hashCode());
			result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
			return result;
		}

		// override to make sure that viewer will be able to restore selected items after refresh
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RepositoryRootNsNode other = (RepositoryRootNsNode) obj;
			if (getRepository() == null) {
				if (other.getRepository() != null)
					return false;
			} else if (!getRepository().equals(other.getRepository()))
				return false;
			if (namespace == null) {
				if (other.namespace != null)
					return false;
			} else if (!namespace.equals(other.namespace))
				return false;
			return true;
		}

		@Override
		public String getPermission() {
			if (getRepository() instanceof RemoteRepository) {
				if (permission == null) {
					synchronized (this) {
						if (permission == null) {
							RemoteRepository rr = (RemoteRepository) getRepository();
							try {
								permission = rr.getUserAuthorization(getNamespace());
							} catch (RepositoryException e) {
								// TODO - either pass back the error for DialogManager display OR set permission to
								// "error"
								LOGGER.warn("Could not get permissions for base name:" + getNamespace() + ".");
							}
						}
					}
				}
				return toString(permission);
			}
			return "";
		}

		private String toString(RepositoryPermission permission) {
			if (permission == null)
				return "None";
			switch (permission) {
			case READ_FINAL:
				return "Read Final Only";
			case READ_DRAFT:
				return "Read";
			case WRITE:
				return "Read/Write";
			case NONE:
			default:
				return "None";
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.opentravel.schemas.node.Node#getTLModelObject()
		 */
		@Override
		public TLModelElement getTLModelObject() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * Namespace Path - one node per first path element in each "root namespace". (i.e. one for ROOTNS/demo and one for
	 * ROOTNS/schema)
	 *
	 * Children are chains for all sub-paths containing libraries.
	 */
	public static class RepositoryPathNode extends RepositoryRootNsNode {

		private String rootBase;
		private String baseName;
		private List<String> childPaths = new ArrayList<String>();

		public RepositoryPathNode(String baseName, String rootBaseName, Repository repository) {
			super(RepositoryNamespaceUtils.normalizeUri(baseName), repository);
			this.rootBase = rootBaseName;
			this.baseName = baseName;
			// LOGGER.debug("Created Base Namespace Node -- root - base: " + rootBase + " - " + baseName);
		}

		/**
		 * Collected from creating the RootNs since it has to scan all paths in the repository.
		 * 
		 * @param path
		 */
		protected void addChildPath(String path) {
			childPaths.add(path);
		}

		@Override
		public String getNamespace() {
			return baseName;
		}

		@Override
		public String getRootBasename() {
			return rootBase;
		}

		@Override
		public String getName() {
			if (rootBase.length() < baseName.length())
				return baseName.substring(rootBase.length() + 1);
			return "";
		}

		@Override
		public Image getImage() {
			return Images.getImageRegistry().get(Images.Namespace);
		}

		@Override
		protected List<Node> initChildren() {
			List<Node> chains = new ArrayList<Node>();

			// See if there are any libraries directly under this base ns.
			List<RepositoryItem> items = listLatestItems(baseName);
			if (!items.isEmpty())
				childPaths.add(""); // will create a chain node in next for loop

			// For each namespace under this namespace get the repo items and create chains
			for (String ns : childPaths) {
				items.addAll(listLatestItems(baseName + ns));
				RepositoryChainNode ci = new RepositoryChainNode(getRepository(), this, ns);
				ci.setParent(this);
				chains.add(ci);
			}
			// LOGGER.debug("Init Children of BaseNsNode " + getName());
			return chains;
		}
	}

	/**
	 * Node class for library chains. Chains contain all patch and minor versions of a libraries with the same major
	 * version and name.
	 *
	 */
	public static class RepositoryChainNode extends RepositoryNode {
		private String path;

		public RepositoryChainNode(Repository repository, Node parent, String path) {
			super(repository);
			this.parent = parent;
			this.path = path;
		}

		@Override
		public int compareTo(RepositoryNode r2) {
			// check for chains with no path component and list them at top
			if (getName().isEmpty())
				return -1;
			return super.compareTo(r2);
		}

		@Override
		public String getNamespace() {
			return parent.getNamespace();
		}

		@Override
		public Image getImage() {
			return Images.getImageRegistry().get(Images.libraryChain);
		}

		@Override
		public String getName() {
			return (parent instanceof RepositoryRootNsNode) ? path : parent.getName() + path;
		}

		@Override
		protected List<Node> initChildren() {
			List<Node> items = new ArrayList<Node>();
			for (RepositoryItem i : listItems(parent.getNamespace() + path)) {
				RepositoryItemNode item = new RepositoryItemNode(i);
				item.setParent(this);
				items.add(item);
			}
			return items;
		}

		private List<RepositoryItem> listItems(String baseName) {
			try {
				return getRepository().listItems(baseName, false, true);
			} catch (RepositoryException e) {
				LOGGER.error("Couldn't fetch items under basename: " + baseName);
			}
			return Collections.emptyList();
		}

		@Override
		public String getPermission() {
			RepositoryNode parent = (RepositoryNode) getParent();
			return parent.getPermission();
		}

		@Override
		public void refresh() throws RepositoryException {
			((RepositoryNode) getParent()).refresh();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.opentravel.schemas.node.Node#getTLModelObject()
		 */
		@Override
		public TLModelElement getTLModelObject() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * Node class for actual libraries
	 *
	 */
	public static class RepositoryItemNode extends RepositoryNode {
		protected RepositoryItem ri = null;

		public RepositoryItemNode(RepositoryItem item) {
			super(item.getRepository());
			this.ri = item;
		}

		@Override
		public String getName() {
			return ri.getFilename();
		}

		@Override
		public Image getImage() {
			return Images.getImageRegistry().get(Images.library);
		}

		public RepositoryItem getItem() {
			return ri;
		}

		@Override
		public String getNamespace() {
			return ri.getNamespace();
		}

		@Override
		public String getPermission() {
			RepositoryNode parent = (RepositoryNode) getParent();
			return parent.getPermission();
		}

		@Override
		public void refresh() throws RepositoryException {
			((RepositoryNode) getParent()).refresh();
		}

		@Override
		public int compareTo(RepositoryNode r2) {
			if (r2 instanceof RepositoryItemNode) {
				Version v1 = new Version(this.ri.getVersion());
				Version v2 = new Version(((RepositoryItemNode) r2).ri.getVersion());
				String n1 = ri.getLibraryName();
				String n2 = ((RepositoryItemNode) r2).ri.getLibraryName();

				int nameCompare = n1.compareTo(n2);
				// int nameCompare = super.compareTo(r2);
				int versionCompare = v2.compareTo(v1);
				if (nameCompare == 0)
					return versionCompare;
				else
					return nameCompare;

				// if (versionCompare == 0) {
				// // compare by name
				// return super.compareTo(r2);
				// } else {
				// return versionCompare;
				// }
			}
			return super.compareTo(r2);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.opentravel.schemas.node.Node#getTLModelObject()
		 */
		@Override
		public TLModelElement getTLModelObject() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
