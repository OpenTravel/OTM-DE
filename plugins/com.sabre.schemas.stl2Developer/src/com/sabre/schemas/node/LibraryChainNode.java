package com.sabre.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.compile.TLModelCompileValidator;
import com.sabre.schemacompiler.version.VersionSchemeException;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.AggregateNode.AggregateType;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * Library chains are all libraries based on the same major release. Their content is aggregated in
 * this node.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryChainNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryChainNode.class);

    protected static final String LIBRARY_CHAIN = "Library Collection";

    // Library Chains collect content from all chain libraries organized by the nav-node.
    protected AggregateNode complexRoot;
    protected AggregateNode simpleRoot;
    protected AggregateNode serviceRoot;
    protected VersionAggregateNode versions;
    protected RepositoryItem repoItem;
    protected List<LibraryNode> chain;

    public LibraryNode getHead() {
        return getLibrary();
    }

    private void setHead(LibraryNode newHead) {
        setLibrary(newHead);
    }

    @Override
    public void setLibrary(LibraryNode ln) {
        super.setLibrary(ln); // sets the library in all the children.
    }

    protected ProjectItem projectItem; // The TL Project Item wrapped around this library

    /**
     * Create a new chain and move the passed library from its parent to the chain. The library
     * parent will be used as the chain's parent. Children are the 4 aggregate nodes linked by their
     * constructors.
     * 
     * @param ln
     *            - library to add to new chain.
     */
    public LibraryChainNode(LibraryNode ln) {
        super();
        if (ln == null)
            return;
        if (ln.isInChain())
            return;

        ProjectNode pn = ln.getProject();
        if (pn == null) {
            LOGGER.error("Library Chains must be made from libraries in a project.");
            return;
        }
        setParent(pn);
        pn.getChildren().remove(ln);
        pn.getChildren().add(this);
        setIdentity(ln.getProjectItem().getBaseNamespace());

        setHead(ln);
        createAggregates();
        versions.add(ln);

        aggregateChildren(ln);
        ln.updateLibraryStatus();

        LOGGER.debug("Created library chain " + this.getLabel());
    }

    private void createAggregates() {
        versions = new VersionAggregateNode(AggregateType.Versions, this);
        complexRoot = new AggregateNode(AggregateType.ComplexTypes, this);
        simpleRoot = new AggregateNode(AggregateType.SimpleTypes, this);
        serviceRoot = new AggregateNode(AggregateType.Service, this);
    }

    /**
     * Create a new chain and add to passed project. Model the project item and add to the new
     * chain.
     * 
     * @param pi
     *            - project item to be modeled and added to chain
     * @param project
     *            - parent of the chain
     */
    public LibraryChainNode(ProjectItem pi, ProjectNode project) {
        super();
        if (pi == null || pi.getContent() == null) {
            LOGGER.debug("Null project item content!");
            return;
        }
        setIdentity(pi.getBaseNamespace());

        setParent(project);
        project.getChildren().add(this);

        setHead(null);

        chain = new ArrayList<LibraryNode>();
        createAggregates();

        List<ProjectItem> piChain = null;
        try {
            piChain = pi.getProjectManager().getVersionChain(pi);
        } catch (VersionSchemeException e1) {
            throw (new IllegalStateException("Could not get chain from project manager."));
        }

        for (ProjectItem item : piChain) {
            add(item);
        }
        setLibrary(getHead());
    }

    /**
     * Add this project item to the version chain.
     * 
     * @return the library node added to the chain or null if it already was in the chain.
     * @param pi
     */
    public LibraryNode add(ProjectItem pi) {
        // If the chain already has this PI, skip it.
        LibraryNode newLib = null;
        for (Node n : versions.getChildren()) {
            if ((n instanceof LibraryNode))
                if (pi.equals(((LibraryNode) n).getProjectItem())) {
                    newLib = (LibraryNode) n;
                    break;
                }
        }

        if (newLib == null) {
            LOGGER.debug("Adding pi " + pi.getFilename() + " to chain " + getLabel());
            newLib = new LibraryNode(pi, this);
            versions.add(newLib); // simply add this library to library list.
            newLib.updateLibraryStatus();
        }
        if (getHead() == null || newLib.getTLaLib().isLaterVersion(getHead().getTLaLib()))
            setHead(newLib);

        return newLib;
    }

    /**
     * Return true if 1st node is from a later version that node2. For example: (v01:flight,
     * v00:flight) returns true.
     * 
     * @param node1
     * @param node2
     */
    public boolean isLaterVersion(Node node1, Node node2) {
        return node1.getLibrary().getTLaLib().isLaterVersion(node2.getLibrary().getTLaLib());
    }

    // public boolean isNewer(LibraryNode ref, LibraryNode test) {
    // return ref == null || test.getTLaLib().isLaterVersion(ref.getTLaLib()) ? true : false;
    // }

    /**
     * Add the passed node to the appropriate chain aggregate. Wrap the node in a version node in
     * the library's children list.
     * 
     * NOTE: may create invalid chain as there may be undetected name collisions
     * 
     * @param node
     */
    protected void add(ComponentNode node) {
        if (node == null)
            return;

        // For services, just add to the service root.
        if (node instanceof ServiceNode)
            serviceRoot.add(node);

        // If not already wrapped in a version, add version wrapper
        else if (!(node.getParent() instanceof VersionNode))
            new VersionNode(node);

        // Add to chain object aggregates.
        if (node instanceof ComplexComponentInterface)
            complexRoot.add(node);
        else if (node instanceof SimpleComponentInterface)
            simpleRoot.add(node);
        // else if (node instanceof OperationNode)
        // serviceRoot.add(node);
        else
            LOGGER.warn("add skipped: " + node);
    }

    /**
     * Remove the node from the appropriate aggregate node. This does not delete the node, just
     * remove it from aggregate list and takes care of family if needed.
     * 
     * @param n
     */
    public void removeAggregate(ComponentNode node) {
        // Remove this version.
        if ((node instanceof ComplexComponentInterface))
            complexRoot.remove(node);
        else if ((node instanceof SimpleComponentInterface))
            simpleRoot.remove(node);
        else if ((node instanceof ServiceNode || (node instanceof OperationNode)))
            serviceRoot.remove(node);

        add(findPreviousVersion(node));
    }

    /**
     * @return true if this chain contains this node
     */
    public boolean contains(Node node) {
        return versions.getChildren().contains(node.getLibrary());
    }

    /**
     * Find the "latest" previous version of the node.
     * 
     * @param node
     */
    private ComponentNode findPreviousVersion(ComponentNode node) {
        return node.getVersionNode() != null ? node.getVersionNode().getPreviousVersion() : null;

        // 12/19/13 dmh - replaced by linked list of prev nodes.
        // ComponentNode n, vn = null;
        // assume node is in this chain.
        // for (Node ln : versions.getChildren()) {
        // n = (ComponentNode) ln.findNodeByName(node.getName());
        // if (vn == null && n != node)
        // vn = n;
        // else if (n != null && n != node)
        // if (isLaterVersion(n, vn))
        // vn = n;
        // }
        // return vn;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#close()
     */
    @Override
    public void close() {
        ProjectNode project = (ProjectNode) getParent();
        super.close();
        // Super will close all libraries in the chain. Save the project since it will have changed.
        ProjectController pc = OtmRegistry.getMainController().getProjectController();
        pc.save(project);
    }

    @Override
    public String getComponentType() {
        return LIBRARY_CHAIN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getChain()
     */
    @Override
    public LibraryChainNode getChain() {
        return this;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.libraryChain);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getLabel()
     */
    @Override
    public String getLabel() {
        String label = "Version Chain";
        if (getHead() != null) {
            label = getHead().getLabel();
        }
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getLibraries()
     */
    @Override
    public List<LibraryNode> getLibraries() {
        ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>();
        for (Node n : versions.getChildren())
            if (n instanceof LibraryNode)
                libs.add((LibraryNode) n);
        return libs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getName()
     */
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
     * All members of the chain must have the same name, base namespace and major version number.
     */
    @Override
    public String getIdentity() {
        String identity = "UNIDENTIFIED-CHAIN:someNS:9";
        if (getHead() != null) {
            NamespaceHandler handler = getHead().getNsHandler();
            if (handler != null)
                identity = makeIdentity();
        }
        return identity;
    }

    /**
     * See also {@link ProjectNode#makeChainIdentity(ProjectItem)} 9/23/2013 - this method does not
     * use the repository for managed base namespaces. It matches the behavior or makeChainIdentity
     * in ProjectNode.
     */
    public String makeChainIdentity() {
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

    @Override
    public List<Node> getNavChildren() {
        return getChildren();
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
    public boolean hasNavChildren() {
        return getChildren().size() <= 0 ? false : true;
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

    public INode getServiceAggregate() {
        return serviceRoot;
    }

    public INode getComplexAggregate() {
        return complexRoot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return versions.getChildren().size() > 0 ? true : false;
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    /**
     * Return the Simple/Complex/Service navNode in the latest library that matches the type of this
     * node. *
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

        for (String f : findings.getValidationMessages(FindingType.ERROR,
                FindingMessageFormat.MESSAGE_ONLY_FORMAT)) {
            LOGGER.debug("Finding: " + f);
        }
        return findings;
    }

    public boolean hasService() {
        return !serviceRoot.getChildren().isEmpty();
    }

}
