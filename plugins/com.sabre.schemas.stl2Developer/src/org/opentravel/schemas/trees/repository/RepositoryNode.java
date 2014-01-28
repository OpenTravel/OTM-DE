/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.trees.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.PatternFilter;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.repository.RemoteRepository;
import com.sabre.schemacompiler.repository.Repository;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryManager;
import com.sabre.schemacompiler.repository.RepositoryNamespaceUtils;
import com.sabre.schemacompiler.repository.impl.RemoteRepositoryClient;

//TODO: move repository actions to RepositoryController
public abstract class RepositoryNode extends Node implements Comparable<RepositoryNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryNode.class);
    private static final String REPOSITORY = "Repository";
    private Repository repository;
    private volatile List<Node> children = null;

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
        if (children == null) {
            synchronized (this) {
                if (children == null) {
                    children = initChildren();
                }
            }
        }
        return children;
    }

    @Override
    protected void setKidsLibrary() {
        // do nothing
    }

    public void refresh() {
        children = null;
    }

    /**
     * This method is used by {@link PatternFilter} to don't check children of this children if not
     * needed.
     * 
     * @return true if children were initialized. This method should return false if called
     *         immediately after refresh() and before first calls of getChildren().
     */
    public boolean wasVisited() {
        return children != null;
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
            // ns = repository.listBaseNamespaces().get(0);
        } catch (RepositoryException e) {
        }
        return ns;
    }

    @Override
    public List<Node> getNavChildren() {
        return null;
    }

    @Override
    public boolean hasNavChildren() {
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
        return 0;
    }

    public static class RepositoryRoot extends RepositoryNode {
        public RepositoryRoot(Repository repository) {
            super(repository);
        }

        @Override
        public String getName() {
            return null;
        }

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

        private RepositoryNameNode create(Repository newRepo) {
            return new RepositoryNameNode(newRepo);
        }

        public void addRepository(Repository newRepo) {
            getChildren().add(create(newRepo));
        }

        public void removeReposutory(Repository toDelete) {
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
                ret = ret || repository.getId().equals(repository2.getId());
            }
            return ret;
        }

    }

    public static class RepositoryNameNode extends RepositoryNode {

        public RepositoryNameNode(Repository repo) {
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
        public void refresh() {
            if (isRemote()) {
                try {
                    ((RemoteRepository) getRepository()).refreshRepositoryMetadata();
                } catch (RepositoryException e) {
                    LOGGER.error("Error on refreshin repository: "
                            + getRepository().getDisplayName());
                }
            }
            super.refresh();
        }

        @Override
        protected List<Node> initChildren() {
            List<Node> namespaces = new ArrayList<Node>();
            try {
                for (String root : getRepository().listRootNamespaces()) {
                    NamespaceNode nn = new NamespaceNode(root, getRepository());
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

        // override equals to make sure that viewer will be able to restore selected items after
        // refresh
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

    }

    public static class NamespaceNode extends RepositoryNode {
        private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceNode.class);

        private String namespace;
        private RepositoryPermission permission;

        public NamespaceNode(String name, Repository repository) {
            super(repository);
            this.namespace = name;
        }

        @Override
        public void refresh() {
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
            List<Node> baseNamespaces = new ArrayList<Node>();
            for (String baseName : getBaseNamespaces()) {
                if (baseName.startsWith(getName())) {
                    BaseNamespaceNode bs = new BaseNamespaceNode(baseName, getRootBasename(),
                            getRepository());
                    if (bs.getName().isEmpty()) {
                        for (Node child : bs.getChildren()) {
                            baseNamespaces.add(child);
                            child.setParent(this);
                        }
                    } else {
                        baseNamespaces.add(bs);
                        bs.setParent(this);
                    }
                }
            }
            return baseNamespaces;
        }

        private List<String> getBaseNamespaces() {
            try {
                return getRepository().listBaseNamespaces();
            } catch (RepositoryException e) {
                LOGGER.error("Could not fetch base-namespaces from repository: "
                        + getRepository().getDisplayName() + ", reason: " + e.getMessage());
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

        // override equals to make sure that viewer will be able to restore selected items after
        // refresh
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NamespaceNode other = (NamespaceNode) obj;
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
                                LOGGER.warn("Could not get permissions for base name:"
                                        + getNamespace() + ".");
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

    }

    public static class BaseNamespaceNode extends NamespaceNode {

        private String rootBase;
        private String baseName;

        @Override
        public String getNamespace() {
            return baseName;
        }

        @Override
        public String getRootBasename() {
            return rootBase;
        }

        public BaseNamespaceNode(String baseName, String rootBaseName, Repository repository) {
            super(RepositoryNamespaceUtils.normalizeUri(baseName), repository);
            this.rootBase = rootBaseName;
            this.baseName = baseName;
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
            List<RepositoryItem> items = listItems(baseName);
            List<Node> namespaces = new ArrayList<Node>();
            Map<RepositoryItem, List<RepositoryItem>> libItems = filterLibItems(items);
            for (Entry<RepositoryItem, List<RepositoryItem>> e : libItems.entrySet()) {
                RepositoryChainNode ri = new RepositoryChainNode(getRepository(), e.getKey(),
                        e.getValue());
                ri.setParent(this);
                namespaces.add(ri);
            }
            return namespaces;
        }

        private List<RepositoryItem> listItems(String baseName) {
            try {
                return getRepository().listItems(baseName, false, true);
            } catch (RepositoryException e) {
                LOGGER.error("Couldn't fetch items under basename: " + baseName);
            }
            return Collections.emptyList();
        }

        private static Map<RepositoryItem, List<RepositoryItem>> filterLibItems(
                List<RepositoryItem> items2) {
            Map<RepositoryItem, List<RepositoryItem>> ret = new HashMap<RepositoryItem, List<RepositoryItem>>();
            Map<String, LinkedList<RepositoryItem>> libName = new HashMap<String, LinkedList<RepositoryItem>>();
            for (RepositoryItem i : items2) {
                LinkedList<RepositoryItem> libarries = libName.get(getKey(i));
                if (libarries == null) {
                    libarries = new LinkedList<RepositoryItem>();
                    libName.put(getKey(i), libarries);
                }
                libarries.add(i);
            }

            for (LinkedList<RepositoryItem> libs : libName.values()) {
                Collections.sort(libs, new Comparator<RepositoryItem>() {

                    @Override
                    public int compare(RepositoryItem o1, RepositoryItem o2) {
                        Version v1 = new Version(o1.getVersion());
                        Version v2 = new Version(o2.getVersion());
                        return v2.compareTo(v1);
                    }
                });
                ret.put(libs.peekFirst(), libs);
            }
            return ret;
        }

        private static String getKey(RepositoryItem i) {
            return i.getLibraryName() + new Version(i.getVersion()).getMajor();
        }

    }

    public static class RepositoryChainNode extends RepositoryNode {

        private List<RepositoryItem> items;
        private RepositoryItem head;

        public RepositoryChainNode(Repository repository, RepositoryItem head,
                List<RepositoryItem> items) {
            super(repository);
            this.head = head;
            this.items = items;
        }

        @Override
        public int compareTo(RepositoryNode r2) {
            if (r2 instanceof RepositoryChainNode) {
                Version v1 = new Version(this.head.getVersion());
                Version v2 = new Version(((RepositoryChainNode) r2).head.getVersion());
                int versionCompare = v2.compareTo(v1);
                if (versionCompare == 0) {
                    // compare by name
                    return super.compareTo(r2);
                } else {
                    return versionCompare;
                }
            }
            return super.compareTo(r2);
        }

        @Override
        public String getNamespace() {
            return head.getBaseNamespace();
        }

        public RepositoryItem getItem() {
            return head;
        }

        @Override
        public Image getImage() {
            return Images.getImageRegistry().get(Images.libraryChain);
        }

        @Override
        public String getName() {
            return head.getLibraryName();
        }

        @Override
        protected List<Node> initChildren() {
            List<Node> namespaces = new ArrayList<Node>();
            for (RepositoryItem i : items) {
                RepositoryItemNode item = new RepositoryItemNode(i);
                item.setParent(this);
                namespaces.add(item);
            }
            return namespaces;
        }

        @Override
        public String getPermission() {
            RepositoryNode parent = (RepositoryNode) getParent();
            return parent.getPermission();
        }

        @Override
        public void refresh() {
            ((RepositoryNode) getParent()).refresh();
        }
    }

    public static class RepositoryItemNode extends RepositoryChainNode {

        public RepositoryItemNode(RepositoryItem repo) {
            super(repo.getRepository(), repo, Collections.<RepositoryItem> emptyList());
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public Image getImage() {
            return Images.getImageRegistry().get(Images.library);
        }

    }

}
