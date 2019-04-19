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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.handlers.children.LibraryChainChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexMemberInterface;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Library chains are all libraries based on the same major release. Their content is aggregated in this node.
 * 
 *
 * <p>
 * Get Child handler returns the versions aggregate.
 * <p>
 * GetTreeChildren returns version node children plus the where used.
 * <p>
 * Roots are direct local variables. LCN has no Children.
 * <p>
 * TODO - should this have its own children handler? Why not?
 * <p>
 * TODO - how does navigator get the aggregates?
 * <p>
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryChainNode extends Node implements FacadeInterface, TypeProviderAndOwners, LibraryInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger( LibraryChainNode.class );

    protected static final String LIBRARY_CHAIN = "Library Collection";

    public static String makeIdentity(String name, String baseNS, String majorNS) {
        return name + ":" + baseNS + ":" + majorNS;
    }

    // Library Chains collect content from all chain libraries organized by the nav-node.
    protected AggregateNode complexRoot;
    protected AggregateNode simpleRoot;
    protected AggregateNode serviceRoot;
    protected AggregateNode resourceRoot;

    protected VersionAggregateNode versions;
    protected RepositoryItem repoItem;

    protected List<LibraryNode> chain;
    protected LibraryNode library;
    protected ProjectItem projectItem; // The TL Project Item wrapped around this library

    /**
     * Create a new chain and move the passed library from its parent to the chain. The library parent will be used as
     * the chain's parent.
     * 
     * 
     * {@link org.opentravel.schemas.controllers.DefaultRepositoryController#convertToChains(Collection<LibraryNode>)}
     * 
     * @param ln - library to add to new chain.
     */
    public LibraryChainNode(LibraryNode ln) {
        super();
        if (ln == null)
            return;
        if (ln.isInChain())
            // throw new IllegalStateException("Library is already in a chain.");
            return;

        if (ln.getProject() == null)
            throw new IllegalStateException( "Library did not have project." );
        if (ln.getParent() == null || (!(ln.getParent() instanceof LibraryNavNode)))
            throw new IllegalStateException( "Library parent is not a LibraryNavNode." );

        // Inform the LibraryModelManager of the change
        ((LibraryNavNode) ln.getParent()).setThisLib( this );
        getModelNode().getLibraryManager().replace( ln, this );

        childrenHandler = new LibraryChainChildrenHandler( this );

        parent = ln.getParent();
        setHead( ln );
        createAggregates();// add aggregates and set parent
        versions.add( ln ); // put library into versions aggregates

        aggregateChildren( ln );
        ln.updateLibraryStatus();

        // LOGGER.debug("Created library chain " + this.getLabel());
    }

    /**
     * Create a new chain and add to passed project. Model the project item(s) and add to the new chain. All versions of
     * libraries in the chain are modeled.
     * 
     * @param pi - project item to be modeled and added to chain
     * @param projNode - parent of the chain
     */
    public LibraryChainNode(ProjectItem pi, ProjectNode projNode) {
        super();
        if (pi == null || pi.getContent() == null) {
            LOGGER.debug( "Null project item content!" );
            return;
        }
        if (pi.getContent().getOwningModel() == null) {
            LOGGER.debug( "Project item " + pi.getContent().getName() + " does not have owning model." );
            return;
        }

        // LOGGER.debug("Creating chain for project item " + pi.getLibraryName());

        setParent( new LibraryNavNode( this, projNode ) );
        // getModelNode().getLibraryManager().add(this);

        childrenHandler = new LibraryChainChildrenHandler( this );

        setHead( null );

        chain = new ArrayList<>();
        createAggregates();
        List<ProjectItem> piChain = null;
        try {
            piChain = pi.getProjectManager().getVersionChain( pi );
        } catch (VersionSchemeException e1) {
            throw (new IllegalStateException(
                "Could not get chain from project manager. " + e1.getLocalizedMessage() ));
        }

        for (ProjectItem item : piChain)
            add( item );

        // Now that we know what is the head library, set that in the aggregates
        setAggregateLibrary( getHead() );
        // Must have a head library to determine the canonical name
        getModelNode().getLibraryManager().add( this );

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
            return; // happens when new library from PI has no service
        if (node.getLibrary() == null)
            throw new IllegalArgumentException( "Tried to add node with null library. " + node );

        if (node instanceof ContributedFacetNode)
            node = ((ContributedFacetNode) node).get();

        // LOGGER.debug("Adding " + node + " to library chain.");

        // Add to chain object aggregates.
        // For services and resource, just add to the appropriate root.
        if (node instanceof ServiceNode)
            serviceRoot.add( node );
        else if (node instanceof ResourceNode)
            resourceRoot.add( node );
        else if (node instanceof ComplexMemberInterface)
            complexRoot.add( node );
        else if (node instanceof SimpleMemberInterface)
            simpleRoot.add( node );
        // else
        // LOGGER.warn("add skipped: " + node);
    }

    /**
     * Add this project item to the version chain if not already found in versions. Sets library to head if it is a
     * later version than current head library.
     * 
     * @param pi - project item for the library.
     * @return the library node associated with the project item or null if pi was null.
     */
    public LibraryNode add(ProjectItem pi) {
        if (pi == null)
            return null;
        // If the chain already has this PI, skip it.
        LibraryNode newLib = versions.get( pi );
        if (newLib == null) {
            // LOGGER.debug("Adding pi " + pi.getFilename() + " to chain " + getLabel());
            // Just add the library node if it already has been modeled.
            if (Node.GetNode( pi.getContent() ) instanceof LibraryNode)
                newLib = (LibraryNode) Node.GetNode( pi.getContent() );
            if (newLib == null)
                // No need to register with library manager since the chain is registered
                newLib = new LibraryNode( pi, this );
            versions.add( newLib ); // simply add this library to library list.
            newLib.updateLibraryStatus();
        }
        // Make passed library head if it is newer (later version)
        if (getHead() == null || newLib.getTLModelObject().isLaterVersion( getHead().getTLModelObject() ))
            setHead( newLib );

        return newLib;
    }

    /**
     * Add each named-type descendant to the chain.
     * 
     * @param lib
     */
    private void aggregateChildren(LibraryNode lib) {
        for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers()) {
            add( (ComponentNode) n );
        }
    }

    /**
     * Walk all members and close them. Caller is responsible to assure this library chain is not used in other projects
     * {@link #LibraryModelManager}
     */
    @Override
    public void close() {
        closeLibraryInterface();
    }

    @Override
    public void closeLibraryInterface() {
        // Attempt to use the parent to close this library
        if (getParent() instanceof LibraryNavNode) {
            ((LibraryNavNode) getParent()).close();
        } else {
            for (Node kid : getChildrenHandler().getChildren_New())
                kid.close();
            // closeAggregates();
            deleted = true;
            setParent( null );
            setLibrary( null );

            assert (isEmpty());
            assert (getHead() == null);
        }
    }

    /**
     * Sets the library in all the aggregate nodes.
     */
    private void closeAggregates() {
        versions.close();
        complexRoot.close();
        simpleRoot.close();
        serviceRoot.close();
        resourceRoot.close();
    }

    /**
     * @return true if this chain contains the node's library
     */
    @Override
    public boolean contains(Node node) {
        return versions != null ? versions.getChildren().contains( node.getLibrary() ) : false;
    }

    private void createAggregates() {
        versions = getChildrenHandler().getVersions();
        complexRoot = getChildrenHandler().getComplexRoot();
        simpleRoot = getChildrenHandler().getSimpleRoot();
        serviceRoot = getChildrenHandler().getServiceRoot();
        resourceRoot = getChildrenHandler().getResourceRoot();
    }

    /**
     * Find the "latest" previous version of the node if not deleted.
     * 
     * @param node
     */
    private ComponentNode findPreviousVersion(ComponentNode node) {
        if (node != null && node.getVersionNode() != null && !node.getVersionNode().isDeleted())
            return node.getVersionNode().getPreviousVersion();
        return null;
    }

    @Override
    public LibraryNode get() {
        return getHead();
    }

    @Override
    public LibraryChainNode getChain() {
        return this;
    }

    @Override
    public LibraryChainChildrenHandler getChildrenHandler() {
        return (LibraryChainChildrenHandler) childrenHandler;
    }

    public AggregateNode getComplexAggregate() {
        return complexRoot;
    }

    @Override
    public String getComponentType() {
        return LIBRARY_CHAIN;
    }

    /**
     * Same as lcn.getLibrary().
     * 
     * @return library at the head of the chain.
     */
    public LibraryNode getHead() {
        return library;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get( Images.libraryChain );
    }

    @Override
    public String getLabel() {
        return getHead() != null ? getHead().getLabel() : "VersionChain";
    }

    /**
     * @return new list of libraries in this chain
     */
    // @Override
    public List<LibraryNode> getLibraries() {
        ArrayList<LibraryNode> libs = new ArrayList<>();
        if (versions != null)
            for (Node n : versions.getChildren())
                if (n instanceof LibraryNode)
                    libs.add( (LibraryNode) n );
        return libs;
    }

    /**
     * @return the head library node
     */
    @Override
    public LibraryNode getLibrary() {
        return get();
    }

    @Override
    public LibraryNavNode getLibraryNavNode() {
        return (LibraryNavNode) getParent();
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

    @Override
    public String getName() {
        String label = "Version Chain";
        if (getHead() != null) {
            NamespaceHandler handler = getHead().getNsHandler();
            if (handler != null)
                label = getHead().getName() + "-" + handler.getNSVersion( getHead().getNamespace() );
        }
        return label;
    }

    /**
     * @return the project containing this chain. Null if no project is found.
     */
    @Override
    public ProjectNode getProject() {
        if (getParent() instanceof LibraryNavNode)
            return ((LibraryNavNode) getParent()).getProject();
        else
            return getParent() instanceof ProjectNode ? (ProjectNode) getParent() : null;
    }

    public AggregateNode getResourceAggregate() {
        return resourceRoot;
    }

    public AggregateNode getServiceAggregate() {
        return serviceRoot;
    }

    public AggregateNode getSimpleAggregate() {
        return simpleRoot;
    }

    @Override
    public TLModelElement getTLModelObject() {
        return getHead() != null ? getHead().getTLModelObject() : null;
    }

    /**
     * Get the parent of the actual libraries in the chain.
     * 
     * @return - the version aggregate node
     */
    public VersionAggregateNode getVersions() {
        return versions;
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return versions != null && versions.getChildren().size() > 0 ? true : false;
    }

    public boolean hasResources() {
        return resourceRoot != null && !resourceRoot.getChildren().isEmpty();
    }

    /**
     * @return true if any library in the chain has a service
     */
    public boolean hasService() {
        for (LibraryNode lib : getLibraries())
            if (lib.hasService())
                return true;
        return false;

        // return !serviceRoot.getChildren().isEmpty();
    }

    @Override
    public boolean hasTreeChildren(boolean deep) {
        return true; // include where used and uses from
    }

    @Override
    public boolean isEditable() {
        // True if any library is editable.
        for (Node ln : versions.getChildren())
            if (ln.isEditable())
                return true;
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

    @Override
    public boolean isEnabled_AddProperties() {
        return false;
    }

    /**
     * Return true if 1st node is from a later version that node2. For example: (v01:flight, v00:flight) returns true.
     * 
     * @param node1
     * @param node2
     */
    public boolean isLaterVersion(Node node1, Node node2) {
        return node1.getLibrary().getTLModelObject().isLaterVersion( node2.getLibrary().getTLModelObject() );
    }

    // @Override
    // public boolean isLibraryContainer() {
    // return true;
    // }

    /**
     * @return true if head library is a major version
     */
    public boolean isMajor() {
        return getHead() != null && getHead().isMajorVersion();
    }

    public boolean isMinor() {
        return getHead() != null && getHead().isMinorOrMajorVersion();
    }

    @Override
    public boolean isNavChild(boolean deep) {
        return true;
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    public boolean isPatch() {
        return getHead() != null && getHead().isPatchVersion();
    }

    /**
     * See also {@link ProjectNode#makeChainIdentity(ProjectItem)} 9/23/2013 - this method does not use the repository
     * for managed base namespaces. It matches the behavior or makeChainIdentity in ProjectNode.
     */
    public String makeChainIdentity() {
        if (getHead() == null)
            return "";

        String name = getHead().getName();
        NamespaceHandler handler = getHead().getNsHandler();
        String baseNS = handler.removeVersion( getHead().getNamespace() );
        return makeIdentity( name, baseNS, handler.getNS_Major( getHead().getNamespace() ) );
    }

    /**
     * See also {@link ProjectNode#makeChainIdentity(ProjectItem)}
     */
    // TODO - see if users of this method should be using chainIdentity()
    public String makeIdentity() {
        String name = getHead().getName();
        NamespaceHandler handler = getHead().getNsHandler();
        return makeIdentity( name, handler.getNSBase( getHead().getNamespace() ),
            handler.getNS_Major( getHead().getNamespace() ) );
    }

    /**
     * Remove the node from the appropriate aggregate node. This does not delete the node, just remove it from aggregate
     * list and takes care of family if needed. Replaces with previous version if found.
     * 
     * @param n
     */
    public void removeFromAggregate(ComponentNode node) {
        // Remove this version.
        if (node instanceof FacetOwner)
            complexRoot.remove( node );
        else if (node instanceof SimpleMemberInterface)
            simpleRoot.remove( node );
        else if (node instanceof ResourceNode)
            resourceRoot.remove( node );
        else if (node instanceof ServiceNode)
            serviceRoot.remove( node );

        // LOGGER.debug("Adding back the previous version of " + node);
        add( findPreviousVersion( node ) );
    }

    /**
     * Sets the library in all the aggregate nodes.
     */
    private void setAggregateLibrary(LibraryNode ln) {
        // LOGGER.debug("Setting library in chain to " + ln.getNameWithPrefix());
        // versions.setLibrary(ln);
        complexRoot.setLibrary( ln );
        simpleRoot.setLibrary( ln );
        serviceRoot.setLibrary( ln );
        resourceRoot.setLibrary( ln );
    }

    private void setHead(LibraryNode newHead) {
        setLibrary( newHead );
    }

    @Override
    public void setLibrary(LibraryNode ln) {
        library = ln;
        // super.setLibrary(ln); // sets the library in all the children.
    }

    /**
     * Simple parent setter. Set to null if it is the root node.
     */
    public void setParent(final LibraryNavNode n) {
        parent = n;
    }

    @Override
    public ValidationFindings validate() {
        return ValidationManager.validate( this );
    }

}
