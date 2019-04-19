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

package org.opentravel.schemas.controllers;

import org.eclipse.swt.custom.BusyIndicator;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryTreeRoot;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.wizards.SetDocumentationWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provide access to the repository manager content in response to requests from repository and other views.
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultRepositoryController implements RepositoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultRepositoryController.class );

    RepositoryManager repositoryManager;
    RepositoryTreeRoot repositoryRoot = null;
    MainController mc;

    public DefaultRepositoryController(MainController mc, RepositoryManager localRepositoryManager) {
        this.mc = mc;
        this.repositoryManager = localRepositoryManager;
    }

    public File getRepositoryFileLocation() {
        return repositoryManager.getRepositoryLocation();
    }

    /**
     * Save the passed library then unlock it. Intended for use by background thread or in JUNIT foreground.
     * 
     * @param ln - library to unlock
     * @param remark - string to save with library as history, can be null
     * @return true if successful, false with user dialog error if not
     */
    static boolean doUnlock(LibraryNode ln, String remark) {
        boolean result = false;
        if (ln == null) {
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), "Unlock has invalid parameters.",
                null );
            return false;
        }
        if (ln.getProject() == null || ln.getProject().getTLProject() == null || ln.getProjectItem() == null) {
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), "Unlock could not get project.",
                null );
            return false;
        }
        final LibraryModelSaver lms = new LibraryModelSaver();
        final ProjectManager pm = ln.getProject().getTLProject().getProjectManager();

        if (lms != null && pm != null)
            try {
                lms.saveLibrary( ln.getTLLibrary() );
                pm.unlock( ln.getProjectItem(), true, remark );
                ln.updateLibraryStatus();
                result = true;
            } catch (RepositoryException e) {
                DefaultRepositoryController.postRepoException( e );
            } catch (LibrarySaveException e) {
                DefaultRepositoryController.postRepoException( e );
            } catch (IllegalStateException e) {
                DefaultRepositoryController.postRepoException( e );
            }
        else
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ),
                "Unlock could not get project manager.", null );
        return result;
    }

    /**
     * Find a repository node from its native model.
     * 
     * @param repository
     * @return
     */
    public RepositoryNode find(Repository repository) {
        return getRoot().find( repository );
    }

    /**
     * @return the root of the repository tree. Repository root will be created if needed.
     */
    @Override
    public RepositoryTreeRoot getRoot() {
        if (repositoryRoot == null) {
            repositoryRoot = new RepositoryTreeRoot( repositoryManager );
        }
        return repositoryRoot;
    }

    @Override
    public void sync(INode node) {
        if (node instanceof RepositoryNode) {
            updateNode( (RepositoryNode) node );
        }
        mc.refresh();
    }

    private void updateNode(RepositoryNode node) {
        try {
            node.refresh();
        } catch (RepositoryException e) {
            String msg = "Error refreshing " + node + " - " + e.getLocalizedMessage();
            LOGGER.error( msg );
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), msg, e );
        }
        LOGGER.debug( "Updated repository node  " + node );
        refreshView( node );
    }

    private void refreshView(INode node) {
        OtmView repositoryView = OtmRegistry.getRepositoryView();
        if (repositoryView != null)
            repositoryView.refresh( node, true );
    }

    /**
     * @return a new list of all repository nodes
     */
    @Override
    public List<RepositoryNode> getAll() {
        List<RepositoryNode> repos = new ArrayList<>();
        if (getRoot() == null)
            throw new IllegalStateException( "Repository root is null." );

        for (INode n : getRoot().getChildren()) {
            if (n instanceof RepositoryNode)
                repos.add( (RepositoryNode) n );
        }
        return repos;
    }

    /**
     * @return Repository Node representing the local repository.
     */
    @Override
    public RepositoryNode getLocalRepository() {
        String localID = repositoryManager.getLocalRepositoryId();
        for (Node n : getRoot().getChildren()) {
            RepositoryNode rn = (RepositoryNode) n;
            if (rn.getRepository().getId().equals( localID ))
                return rn;
        }
        return null;
    }

    @Override
    public List<LibraryChainNode> manage(RepositoryNode repository, List<LibraryNode> libs) {
        List<ProjectItem> items = toProjectItems( libs );
        if (!items.isEmpty()) {
            ProjectManager pm = items.get( 0 ).getProjectManager();
            Collection<ProjectItem> publishedItems = publishItems( pm, items, repository.getRepository() );

            if (!publishedItems.isEmpty()) {
                Collection<LibraryNode> publishedLibs = findLibraries( publishedItems );
                List<LibraryChainNode> chains = convertToChains( publishedLibs );
                refreshAll( repository );
                return chains;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Publish passed items to passed repository. If the repository reports there are dependent items, ask the user if
     * they want them published as well.
     * 
     * @return list of published items
     */
    private Collection<ProjectItem> publishItems(ProjectManager pm, List<ProjectItem> items, Repository repository) {
        Collection<ProjectItem> publishedItems = Collections.emptyList();
        try {
            try {
                publishedItems = publish( pm, items, repository );
            } catch (PublishWithLocalDependenciesException e) {
                try {
                    boolean process = DialogUserNotifier.openConfirm( Messages.getString( "repository.warning" ),
                        Messages.getString( "repository.warning.loadDependentLibs", toString(
                            getNewPublications( e.getRequestedPublications(), e.getRequiredPublications() ) ) ) );
                    if (process) {
                        publishedItems = publish( pm, e.getRequiredPublications(), repository );
                    }
                } catch (PublishWithLocalDependenciesException e1) {
                    postRepoError( "manage", e1 );
                }
            }
        } catch (RepositoryException e) {
            postRepoException( e );
        }
        return publishedItems;
    }

    /**
     * Check to see if any items are already published. Check to assure items are in the correct namespace for the
     * repository.
     * 
     * @return
     * @throws RepositoryException
     * @throws PublishWithLocalDependenciesException
     */
    private Collection<ProjectItem> publish(ProjectManager pm, Collection<ProjectItem> items, Repository repository)
        throws RepositoryException, PublishWithLocalDependenciesException {
        // Remove already managed items
        Collection<ProjectItem> managed = getManagedItems( repository, items );
        if (!managed.isEmpty()) {
            postRepoError( "alreadyManaged" );
            return Collections.emptyList(); // added 11/21/2016
        }
        // Remove items whose namespace is not manage in repository
        Collection<ProjectItem> invalid = getItemsWithInvalidNamespace( repository, items );
        if (!invalid.isEmpty()) {
            postRepoError( "managedNS" );
            return Collections.emptyList();
        }

        // Publish project items in the repository.
        pm.publish( items, repository );

        // Verify results - there are multiple managed states
        for (ProjectItem i : items)
            assert !RepositoryItemState.UNMANAGED.equals( i.getState() );

        return items;
    }

    private Collection<ProjectItem> getManagedItems(Repository repository, Collection<ProjectItem> items) {
        List<ProjectItem> managed = new ArrayList<>();
        for (ProjectItem i : items) {
            if (!RepositoryItemState.UNMANAGED.equals( i.getState() )) {
                managed.add( i );
            }
        }
        return managed;
    }

    private Collection<ProjectItem> getItemsWithInvalidNamespace(Repository repository, Collection<ProjectItem> items) {
        List<ProjectItem> invalid = new ArrayList<>();
        for (ProjectItem i : items) {
            if (!isInManagedNS( i.getNamespace(), repository )) {
                invalid.add( i );
            }
        }
        return invalid;
    }

    private String toString(Collection<ProjectItem> newPublications) {
        StringBuilder sb = new StringBuilder();
        String delimenter = ", ";
        for (ProjectItem p : newPublications) {
            sb.append( p.getLibraryName() );
            sb.append( delimenter );
        }
        sb.delete( sb.lastIndexOf( delimenter ), sb.length() );
        return sb.toString();
    }

    private Collection<ProjectItem> getNewPublications(Collection<ProjectItem> requestedPublications,
        Collection<ProjectItem> requiredPublications) {
        Set<ProjectItem> items = new HashSet<>();
        for (ProjectItem req : requiredPublications) {
            if (!requestedPublications.contains( req )) {
                items.add( req );
            }
        }
        return items;
    }

    private Collection<LibraryNode> findLibraries(Collection<ProjectItem> publishedItems) {
        List<LibraryNode> libraries = new ArrayList<>( publishedItems.size() );

        for (LibraryNode l : Node.getAllUserLibraries())
            if (publishedItems.contains( l.getProjectItem() ))
                libraries.add( l );

        return libraries;
    }

    /**
     * Assure all libraries are in chains. If it is not, a new chain is created to contain the library.
     * 
     * @param publishedLibs
     * @return
     */
    private List<LibraryChainNode> convertToChains(Collection<LibraryNode> publishedLibs) {
        List<LibraryChainNode> chains = new ArrayList<>( publishedLibs.size() );
        for (LibraryNode l : publishedLibs) {
            // Could have duplicates in the list
            if (!l.isInChain())
                chains.add( new LibraryChainNode( l ) );
        }
        return chains;
    }

    private List<ProjectItem> toProjectItems(List<LibraryNode> libs) {
        List<ProjectItem> items = new ArrayList<>( libs.size() );
        for (LibraryNode l : libs) {
            if (l != null)
                items.add( l.getProjectItem() );
        }
        return items;
    }

    @Override
    public boolean commit(LibraryNode ln, String remark) {
        if (!ln.isManaged()) {
            postRepoWarning( "alreadyManaged" );
            return false;
        }
        if (!post16UpgradeConfirmation())
            return false;

        CommitThread ct = new CommitThread( ln, remark );
        BusyIndicator.showWhile( mc.getMainWindow().getDisplay(), ct );
        refreshAll( ln );
        mc.postStatus( "Library " + ln + " committed." );
        LOGGER.debug( "Committed " + ln + " result = " + ct.getResult() );
        return ct.getResult();
    }

    @Deprecated
    public boolean commit(LibraryNode ln) {
        return commit( ln, "" );
    }

    @Override
    public void lock() {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            lock( ln );
        }
    }

    @Override
    public boolean lock(LibraryNode ln) {
        if (ln == null)
            return false;

        LockThread lt = new LockThread( ln );
        BusyIndicator.showWhile( mc.getMainWindow().getDisplay(), lt );
        if (lt.getException() != null) {
            LOGGER.debug( "TODO - handle exception: " + lt.getException().getLocalizedMessage() );
        }
        ln.updateLibraryStatus();
        refreshAll( ln );
        mc.postStatus( "Library " + ln + " locked." );
        return true;
    }

    // Used by the action.
    @Override
    public void unlock(boolean commitWIP) {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            if (commitWIP)
                unlock( ln );
            else
                unlockAndRevert( ln );
        }
    }

    // TODO - should be private - only used internally and in testing
    // return value only used in versionPreperation
    @Override
    public boolean unlock(LibraryNode ln) {
        if (!post16UpgradeConfirmation())
            return false;

        if (OtmRegistry.getActiveShell() != null) {
            // Is in the UI thread.
            SetDocumentationWizard wizard = new SetDocumentationWizard( ln );
            wizard.run( OtmRegistry.getActiveShell() );
            if (!wizard.wasCanceled()) {
                String remark = wizard.getDocText();

                UnlockThread ut = new UnlockThread( ln, mc, remark );
                BusyIndicator.showWhile( mc.getMainWindow().getDisplay(), ut );
                refreshAll( ln );
                LOGGER.debug( "UnLocked library " + this );
                mc.postStatus( "Library " + ln + " unlocked." );
                return ut.getResult();
            }
        } else
            // Not in UI thread (junit), just do the unlock
            return doUnlock( ln, "Testing Remark " + new Date() );
        return false;
    }
    //
    // TODO - turn lock/unlock (and maybe others) into monitored background tasks not just busy indicators
    //

    // public void syncWithUi(final String msg) {
    // Display.getDefault().asyncExec(new Runnable() {
    // @Override
    // public void run() {
    // OtmRegistry.getMainController().postStatus(msg);
    // DialogUserNotifier.openInformation("Repository Results", msg);
    // OtmRegistry.getMainController().refresh();
    // refreshAll();
    // mc.postStatus(msg);
    //
    // }
    // });
    // }

    @Override
    public ProjectNode unlockAndRevert(LibraryNode ln) {
        RevertThread rt = new RevertThread( ln, mc );
        BusyIndicator.showWhile( mc.getMainWindow().getDisplay(), rt );
        refreshAll( ln );
        // LOGGER.debug("UnLocked and reverted library " + ln);
        mc.postStatus( "Library " + ln + " unlocked." );
        return rt.getLoaded();
    }

    @Deprecated
    @Override
    public boolean markFinal(LibraryNode ln) {
        assert (ln.getProjectItem() != null);
        boolean result = true;

        // must be unlocked to be promoted to final.
        if (ln.isLocked())
            if (!unlock( ln ))
                return false;

        // Libraries are unexpectedly getting set to final. Do confirm here until this is resolved.
        if (!DialogUserNotifier.openConfirm( "Finalize Library",
            "Warning, making a library final can not be undone. Do you want to continue?" ))
            return false;
        try {
            ln.getProjectItem().getProjectManager().promote( ln.getProjectItem() );
        } catch (RepositoryException e) {
            result = false;
            postRepoException( e );
        }
        refreshAll( ln );
        if (result)
            mc.postStatus( "Library " + ln + " finalized." );
        return result;
    }

    @Override
    public boolean promote(LibraryNode ln, TLLibraryStatus targetStatus) {
        assert (ln.getProjectItem() != null);
        boolean result = true;

        // must be unlocked to be promoted.
        if (ln.isLocked())
            if (!unlock( ln )) {
                DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), "Could not unlock library.",
                    null );
                return false;
            }

        // Items must be in the MANAGED_UNLOCKED state in order to be promoted.
        ProjectItem pi = ln.getProjectItem();
        if (!pi.getState().equals( RepositoryItemState.MANAGED_UNLOCKED )) {
            String msg = Messages.getString( REPO_ERROR_PREFIX + "notManagedUnlocked" );
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), msg, null );
            return false;
        }
        // Assure the intended promotion is allowed
        if (!pi.getStatus().nextStatus().equals( targetStatus )) {
            String msg = Messages.getString( REPO_ERROR_PREFIX + "incorrectStatus" );
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), msg, null );
            return false;
        }
        // Confirm Use intends to promote.
        if (!DialogUserNotifier.openConfirm( "Promote Library",
            "Warning, promoting a library to " + targetStatus + " can not be undone. Do you want to continue?" ))
            return false;

        // DO IT
        try {
            pi.getProjectManager().promote( ln.getProjectItem() );
        } catch (RepositoryException e) {
            result = false;
            postRepoException( e );
        }
        refreshAll( ln );
        if (result)
            mc.postStatus( "Library " + ln + " promoted to " + targetStatus + "." );
        return result;
    }

    private void refreshAll(LibraryNode ln) {
        if (ln != null) {
            RepositoryNode repoNode = find( ln.getProjectItem().getRepository() );
            refreshAll( repoNode );
        } else {
            refreshAll();
        }
    }

    private void refreshAll(RepositoryNode repoNode) {
        if (repoNode != null) {
            updateNode( repoNode );
            mc.refresh( repoNode );
        } else {
            refreshAll();
        }
    }

    private void refreshAll() {
        refreshView( null );
        mc.refresh();
    }

    @Override
    public boolean isInManagedNS(String testNS, RepositoryNode repository) {
        return isInManagedNS( testNS, repository.getRepository() );
    }

    private boolean isInManagedNS(String testNS, Repository repository) {
        try {
            for (String rootNS : repository.listRootNamespaces()) {
                if (testNS.startsWith( rootNS ))
                    return true;
            }
        } catch (RepositoryException e) {
            postRepoException( e );
        }
        return false;
    }

    @Override
    public List<String> getRootNamespaces() {
        List<String> rootNSs = new ArrayList<>();
        for (RepositoryNode rn : getAll()) {
            try {
                rootNSs.addAll( rn.getRepository().listRootNamespaces() );
            } catch (RepositoryException e) {
                postRepoException( e );
            }
        }
        return rootNSs;
    }

    public LibraryNode getLatestVersion(LibraryNode lib, boolean includeDrafts) throws RepositoryException {
        LibraryNode replacement = null;
        ProjectItem projItem = lib.getProjectItem();
        Repository lRepo = lib.getProjectItem().getRepository();
        String baseNS = projItem.getBaseNamespace();
        if (projItem == null || lRepo == null || baseNS.isEmpty())
            return null;

        String message = "Checking repository for the latest version of " + lib;
        mc.postStatus( message );
        mc.showBusy( true );
        mc.refresh();
        LOGGER.debug( message );

        // Get the latest from the Repository.
        // Assume chronologically draft, then Under_Review then final.
        List<RepositoryItem> ll;
        RepositoryItem replacementRI = null;
        String targetName = projItem.getContent().getName();
        if (includeDrafts)
            replacementRI = getLatestRepoItem( lRepo, baseNS, TLLibraryStatus.DRAFT, targetName );
        else
            replacementRI = getLatestRepoItem( lRepo, baseNS, TLLibraryStatus.UNDER_REVIEW, targetName );
        if (replacementRI == null)
            replacementRI = getLatestRepoItem( lRepo, baseNS, TLLibraryStatus.FINAL, targetName );

        // Nothing found
        if (replacementRI == null) {
            mc.postStatus( "" );
            mc.showBusy( false );
            return replacement;
        }

        // Look up the Repository Item to get the library if open in the GUI
        replacement = Node.getLibraryModelManager().get( replacementRI.getNamespace(), replacementRI.getLibraryName() );

        if (replacement == null) {
            // Library is not open in GUI, so open it.
            message = "Opening " + replacementRI.getNamespace() + " - " + replacementRI.getLibraryName();
            mc.postStatus( message );

            ProjectItem newPI = mc.getProjectController().add( lib.getProject(), replacementRI );

            // could open a lot of dependent libraries, find the right one
            replacement =
                Node.getLibraryModelManager().get( replacementRI.getNamespace(), replacementRI.getLibraryName() );
        }
        // LOGGER.debug("getLatestVersion found " + replacement.getNamespace() + " " + replacement.getName());
        mc.postStatus( "" );
        mc.showBusy( false );
        return replacement;
    }

    private RepositoryItem getLatestRepoItem(Repository lRepo, String baseNS, TLLibraryStatus status, String name)
        throws RepositoryException {
        List<RepositoryItem> ll;
        ll = lRepo.listItems( baseNS, status, true );
        // Resulting list may have other libraries from same namespace
        for (RepositoryItem ri : ll)
            if (ri.getLibraryName().equals( name ))
                return ri;
        return null;
    }

    /**
     * For each passed library find the latest version from the same repository.
     * 
     * If different, add the passed library and latest version in the returned map.
     * 
     * WARNING: each library will invoke a slow process on the repository
     * 
     * @param usedLibs
     * @param includeDrafts if true, draft libraries will be included as replacement candidate
     * @return maps of libraries, key is passed library and value is later version of library.
     * @throws RepositoryException
     */
    // public HashMap<LibraryNode, LibraryNode> getVersionUpdateMap(List<LibraryNode> usedLibs) throws
    // RepositoryException {
    // return getVersionUpdateMap(usedLibs, true);
    // }
    //
    @Deprecated
    public HashMap<LibraryNode,LibraryNode> getVersionUpdateMap(List<LibraryNode> usedLibs, boolean includeDrafts)
        throws RepositoryException {
        // Map to return
        HashMap<LibraryNode,LibraryNode> replacementMap = new HashMap<>();
        //
        // HashMap<LibraryNode, RepositoryItem> itemMap = new HashMap<>();
        // List<RepositoryItem> ll;
        // // List<RepositoryItem> ll = new ArrayList<RepositoryItem>();
        //
        // // For each of the passed libraries
        // for (LibraryNode lib : usedLibs) {
        // ProjectItem projItem = lib.getProjectItem();
        // Repository lRepo = lib.getProjectItem().getRepository();
        // String baseNS = projItem.getBaseNamespace();
        // if (projItem == null || lRepo == null || baseNS.isEmpty())
        // continue;
        //
        // // For each used library, lookup the latest version.
        // // TODO - update listItems call to use libraryStatus
        // // baseNamespace the base namespace that does not include the trailing version component of the URI path
        // // includeStatus indicates the latest library status to include in the results (null = all statuses)
        // // latestVersionsOnly flag indicating whether the results should include all matching versions or just the
        // // latest version of each library
        // // ll.addAll(lRepo.listItems(baseNS, TLLibraryStatus.UNDER_REVIEW, true));
        // // ll.addAll(lRepo.listItems(baseNS, TLLibraryStatus.FINAL, true));
        // // if (includeDrafts)
        // // ll = lRepo.listItems(baseNS, TLLibraryStatus.DRAFT, true);
        // // // Now - how to find the latest?
        //
        // ll = lRepo.listItems(baseNS, true, includeDrafts);
        // // list contains all library chains in that namespace
        // if (ll.size() > 0) {
        // for (RepositoryItem latest : ll)
        // if (latest != null && latest.getLibraryName().equals(projItem.getLibraryName())
        // && !latest.getNamespace().equals(projItem.getNamespace()))
        // itemMap.put(lib, latest);
        // }
        // }
        //
        // // Create library map of namespace to library node for all libraries open in the GUI
        // HashMap<String, LibraryNode> libraryMap = new HashMap<>();
        // for (LibraryNode lib : ModelNode.getAllUserLibraries())
        // libraryMap.put(lib.getNameWithPrefix(), lib);
        //
        // // Now map the "latest versions" repository items to actual libraries.
        // for (Entry<LibraryNode, RepositoryItem> entry : itemMap.entrySet()) {
        // String entryNwPrefix = entry.getKey().getNameWithPrefix();
        // // Get the prefix for the repo item namespace for lookup in library map
        // String latestNS = entry.getValue().getNamespace();
        // String latestNwPrefix = entry.getKey().getNsHandler().getPrefix(latestNS) + ":"
        // + entry.getValue().getLibraryName();
        //
        // if (libraryMap.containsKey(latestNwPrefix))
        // replacementMap.put(libraryMap.get(entryNwPrefix), libraryMap.get(latestNwPrefix));
        // else {
        // String message = "Opening " + entry.getValue().getNamespace() + " - "
        // + entry.getValue().getLibraryName() + " library.";
        // // LOGGER.debug(message);
        // mc.postStatus(message);
        //
        // // Open the repository item into the entry key's project.
        // ProjectItem newPI = mc.getProjectController().add(entry.getKey().getProject(), entry.getValue());
        // // could open a lot of libraries, find the right one
        // for (LibraryNode lib : ModelNode.getAllUserLibraries())
        // if (lib.getNameWithPrefix().equals(latestNwPrefix))
        // replacementMap.put(libraryMap.get(entryNwPrefix), lib);
        // }
        // }
        //
        // // Print out replacement map
        // for (Entry<LibraryNode, LibraryNode> entry : replacementMap.entrySet())
        // LOGGER.debug("ReplacementMap Entry: " + entry.getKey() + " value = " + entry.getValue());
        //
        return replacementMap;
    }

    @Override
    public boolean validateBaseNamespace(String namespace) {
        for (RepositoryNode rn : getAll()) {
            if (isInManagedNS( namespace, rn ))
                return true;
        }
        return false;
    }

    @Override
    public boolean addRemoteRepository(String location, String userId, String password) {
        if (!userId.isEmpty() && password.isEmpty()) {
            postRepoError( "password" );
            return false;
        } else if (userId.isEmpty()) {
            userId = null;
        }
        location = stipLocation( location );
        try {
            RemoteRepository repo = repositoryManager.addRemoteRepository( location );
            repositoryManager.setCredentials( repo, userId, password );
            getRoot().addRepository( repo );
            refreshAll();
        } catch (RepositoryException e) {
            // Post directly to get message and exception message
            String msg = Messages.getString( REPO_MESSAGE_PREFIX + "invalidLocation" );
            msg += "\n\n" + e.getLocalizedMessage();
            DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), msg, e );
            LOGGER.warn( "Repository Error: " + msg );
            return false;
        }
        return true;
    }

    private String stipLocation(String location) {
        if (location.endsWith( "/" ))
            return location.substring( 0, location.length() - 1 );
        return location;
    }

    @Override
    public boolean changeCredentials(String location, String userId, String password) {
        if (!userId.isEmpty() && password.isEmpty()) {
            postRepoError( "password" );
            return false;
        } else if (userId.isEmpty()) {
            userId = null;
        }
        RemoteRepository repo = getRemoteRepository( location );
        try {
            repositoryManager.setCredentials( repo, userId, password );
            RepositoryNode repoNode = find( repo );
            updateNode( repoNode );
        } catch (RepositoryException e) {
            postRepoException( e );
            return false;
        }
        return true;
    }

    private RemoteRepository getRemoteRepository(String location) {
        for (RemoteRepository rep : repositoryManager.listRemoteRepositories()) {
            if (rep instanceof RemoteRepositoryClient) {
                if (location.equals( ((RemoteRepositoryClient) rep).getEndpointUrl() )) {
                    return rep;
                }
            }
        }
        return null;
    }

    @Override
    public LibraryNode createPatchVersion(LibraryNode library) {
        if (!versionPreparation( library ))
            return null;
        return createVersion( library, true );
    }

    @Override
    public LibraryNode createMinorVersion(LibraryNode library) {
        LibraryNode ln;
        if (!versionPreparation( library ))
            return null;
        if (library.isPatchVersion()) {
            // must create from latest minor not the patch.
            String minor = library.getNsHandler().getNS_Minor( library.getNamespace() );
            for (Node n : library.getParent().getChildren()) {
                if (!(n instanceof LibraryNode))
                    continue;
                ln = (LibraryNode) n;
                if (ln.isMinorOrMajorVersion() && ln.getNsHandler().getNS_Minor( ln.getNamespace() ).equals( minor )) {
                    library = ln;
                    break;
                }
            }
        }
        return createVersion( library, false );
    }

    private LibraryNode createVersion(LibraryNode library, boolean isPatch) {

        RepositoryNode rn = find( library.getProjectItem().getRepository() );
        PatchVersionHelper patchVH = new PatchVersionHelper( library.getProject().getTLProject() );
        MinorVersionHelper minorVH = new MinorVersionHelper( library.getProject().getTLProject() );
        TLLibrary newTLLib = null;
        LibraryNode newLib = null;

        try {
            if (isPatch)
                newTLLib = patchVH.createNewPatchVersion( library.getTLLibrary() );
            else
                newTLLib = minorVH.createNewMinorVersion( library.getTLLibrary() );
        } catch (VersionSchemeException e) {
            postRepoException( e );
        } catch (ValidationException e) {
            postRepoException( e );
        } catch (LibrarySaveException e) {
            postRepoException( e );
        }

        if (newTLLib != null) {
            // Verify helper behavior
            assert newTLLib.getOwningModel() != null;
            ProjectItem foundPI = null;
            for (ProjectItem pi : library.getProject().getTLProject().getProjectItems())
                if (pi.getContent() == newTLLib)
                    foundPI = pi;
            assert foundPI != null;

            // new TL library is unmanaged, un-modeled and no listeners
            // newLib = mc.getProjectController().add(library.getChain(), newTLLib);
            // try this instead -- skips addUnmangedProjectItem() call
            if (foundPI != null)
                newLib = new LibraryNode( foundPI, library.getChain() );

            manage( rn, Collections.singletonList( newLib ) );

            // make the new library head of chain
            library.getChain().add( newLib.getProjectItem() );

            lock( newLib );
            sync( rn );
        }
        return newLib;
    }

    /**
     * {@link org.opentravel.schemas.actions.VersionMajorAction#run()}
     */
    @Override
    public LibraryNode createMajorVersion(LibraryNode library) {

        if (!versionPreparation( library ))
            return null;

        RepositoryNode rn = find( library.getProjectItem().getRepository() );
        // LibraryModelManager libMrg = Node.getModelNode().getLibraryManager();
        MajorVersionHelper mvh = new MajorVersionHelper( library.getProject().getTLProject() );

        TLLibrary tlMajor = null;
        LibraryNode newLib = null;
        LibraryChainNode lcn = null;
        ProjectNode thisProject = library.getProject();

        try {
            tlMajor = mvh.createNewMajorVersion( library.getTLLibrary() );
        } catch (VersionSchemeException e) {
            postRepoException( e );
        } catch (ValidationException e) {
            postRepoException( e );
        } catch (LibrarySaveException e) {
            postRepoException( e );
        } catch (IllegalArgumentException e) {
            postRepoException( e );
        }

        if (tlMajor != null) {
            // Create Library from new TL library
            newLib = new LibraryNode( tlMajor, library.getProject() );
            // Create chain by managing the new library
            List<LibraryChainNode> chains = manage( rn, Collections.singletonList( newLib ) );

            // Replace passed library with new NavNode for chain in all projects
            lcn = findLibrary( chains, newLib.getLibrary().getName() );
            if (lcn != null) {
                LibraryModelManager libMrg = Node.getLibraryModelManager();
                ProjectController pc = mc.getProjectController();
                List<ProjectNode> pList = libMrg.findProjects( library );
                for (ProjectNode pn : pList) {
                    // NavNode will be created with library in the original project(s)
                    if (pn != thisProject)
                        new LibraryNavNode( lcn, pn );
                }
                // Lock the new library
                lock( newLib );
                // Use this as suffix to contextual facets
                String suffix = newLib.getVersion_Major();
                for (ContextualFacetNode cf : newLib.getDescendants_ContextualFacets())
                    cf.setName( cf.getName() + suffix );
            }
            sync( rn );
        }
        return newLib;
    }

    private LibraryChainNode findLibrary(List<LibraryChainNode> chains, String name) {
        for (LibraryChainNode chain : chains) {
            if (name.equals( chain.getHead().getName() ))
                return chain;
        }
        return null;
    }

    /**
     * Prepare the library for new version. Preparation includes committing, unlocking and finalizing. User interactions
     * is sought before making changes.
     * 
     * @param library
     * @return true if ready.
     */
    // FIXME - make this into a wizard or progress monitor so the user can track progress
    private boolean versionPreparation(LibraryNode library) {
        if (library == null) {
            LOGGER.warn( "version preperation passed a null library." );
            return false;
        }

        if (library.getProject() == null) {
            postRepoError( "noProject" );
            return false;
        }
        if (library.getProjectItem() == null) {
            postRepoError( "noProject" );
            return false;
        }
        RepositoryNode rn = find( library.getProjectItem().getRepository() );
        if (rn == null) {
            postRepoError( "notManaged" );
            return false;
        }
        if (!library.getStatus().equals( TLLibraryStatus.FINAL )) {
            postRepoError( "notFinal" );
            return false;
        }
        if (!library.isReadyToVersion()) {
            postRepoError( "notValid" );
            return false;
        }
        if (library.getChain() == null) {
            LOGGER.warn( "Missing chain for library " + library );
            return false;
        }

        String msgID = "repository.version.check.all";
        boolean isOK = false;

        boolean editable = library.getEditStatus().equals( NodeEditStatus.FULL );

        msgID = "repository.version.check.OK";

        isOK = DialogUserNotifier.openConfirm( Messages.getString( "repository.version.check.title" ),
            Messages.getString( msgID ) );
        return isOK;
    }

    /**
     * Searches the contents of the repository using the free-text keywords provided. This method is calling
     * {@link Repository#search(String, boolean, boolean)} by passing as latestVersionsOnly=false and
     * includeDraftVersions=true
     * 
     * @param string
     * @see Repository#search(String, boolean, boolean)
     */
    @Override
    public List<RepositoryItem> search(String string) {
        try {
            // TODO: what parameter pass to search false or true ?
            return repositoryManager.search( string, false, true );
        } catch (RepositoryException e) {
            postRepoException( e );
        }
        return Collections.emptyList();
    }

    @Override
    public void removeRemoteRepository(RepositoryNode node) {
        try {
            repositoryManager.removeRemoteRepository( (RemoteRepository) node.getRepository() );
            getRoot().removeRepository( node.getRepository() );
            refreshAll();
        } catch (RepositoryException e) {
            postRepoException( e );
        }
    }

    public static String REPO_ERROR_TITLE = "repository.error.title";
    public static String REPO_WARNING_TITLE = "repository.warning";
    public static String REPO_MESSAGE_PREFIX = "repository.warning.";
    public static String REPO_ERROR_PREFIX = "repository.error.";

    public static void postRepoException(Exception e) {
        DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), e.getLocalizedMessage(), e );
        LOGGER.warn( "Repository Exception: " + e.getLocalizedMessage() );
    }

    public static void postRepoWarning(String message) {
        final String msg = Messages.getString( REPO_MESSAGE_PREFIX + message );
        if (msg.equals( '!' + message + '!' ))
            LOGGER.error( "Bad warning message: " + message );
        DialogUserNotifier.openWarning( Messages.getString( REPO_WARNING_TITLE ), msg );
        LOGGER.warn( "Repository Warning: " + msg );
    }

    public static void postRepoError(String message) {
        postRepoError( message, null );
    }

    public static void postRepoError(String message, Throwable e) {
        final String msg = Messages.getString( REPO_MESSAGE_PREFIX + message );
        // if (msg.equals('!' + message + '!'))
        // LOGGER.error("Bad error message: " + message);
        DialogUserNotifier.openError( Messages.getString( REPO_ERROR_TITLE ), msg, e );
    }

    /**
     * @return true if older than version 1.6 OR user confirms saving
     */
    public static boolean post16UpgradeConfirmation() {
        // 5/17/2018 - removed. everybody moved to 1.6 by now.
        // // Post user warning
        // if (!DialogUserNotifier.openConfirm("Warning", Messages.getString("action.saveAll.version16")))
        // return false;
        // return true;
        return true;
    }
}


/**
 * If you just want to show busy indicator while running long operation, your code would typically look like this:
 * 
 * BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() { public void run() { // Long
 * operation goes here } });
 * 
 * Unlike point 5, here the code inside run() method is not executed in worker thread. The code is run by UI thread
 * only. So, make sure not to execute too long running operations here.
 * 
 * http://rajakannappan.blogspot.com/2010/01/eclipse-rcp-prevent-ui-freezing-while.html
 */
// mc.showBusy() does not work consistently.
class LockThread extends Thread {
    private LibraryNode ln;
    private Exception exception = null;

    public LockThread(LibraryNode ln) {
        this.ln = ln;
    }

    @Override
    public void run() {
        try {
            ln.lock();
            DialogUserNotifier.syncWithUi( "Locked " + ln );
        } catch (RepositoryException e) {
            exception = e;
            DefaultRepositoryController.postRepoException( e );
        } catch (LibraryLoaderException e) {
            exception = e;
            DefaultRepositoryController.postRepoException( e );
        }
    }

    public Exception getException() {
        return exception;
    }
}


/**
 * Class to unlock a library in a background thread. Uses compilier's project manager to do the unlocking. Saves the
 * library before unlocking.
 */
class UnlockThread extends Thread {
    private LibraryNode ln;
    private MainController mc;
    private boolean result = false;
    private String remark;

    public UnlockThread(LibraryNode ln, MainController mc, String remark) {
        this.ln = ln;
        this.mc = mc;
        this.remark = remark;
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public void run() {
        result = DefaultRepositoryController.doUnlock( ln, remark );
    }

}


class CommitThread extends Thread {
    private LibraryNode ln;
    private String remark;
    private boolean result = true;

    public CommitThread(LibraryNode ln, String remark) {
        this.ln = ln;
        this.remark = remark;
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public void run() {
        try {
            ln.getProjectItem().getProjectManager().commit( ln.getProjectItem(), remark );
        } catch (RepositoryException e) {
            result = false;
            DefaultRepositoryController.postRepoException( e );
        }
    }
}


class RevertThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultRepositoryController.class );

    private LibraryNode ln;
    private MainController mc;
    private ProjectNode loaded = null;

    public RevertThread(LibraryNode ln, MainController mc) {
        this.ln = ln;
        this.mc = mc;
    }

    public ProjectNode getLoaded() {
        return loaded;
    }

    @Override
    public void run() {
        try {
            final LibraryModelSaver lms = new LibraryModelSaver();
            lms.saveLibrary( ln.getTLLibrary() );
            String projectFile = ln.getProject().getTLProject().getProjectFile().toString();
            ProjectManager pm = ((DefaultProjectController) mc.getProjectController()).getDefaultProject()
                .getTLProject().getProjectManager();
            pm.unlock( ln.getProjectItem(), false );
            boolean isDefault = ln.getProject() == mc.getProjectController().getDefaultProject();
            mc.getProjectController().close( ln.getProject() );
            if (isDefault) {
                loaded = mc.getProjectController().getDefaultProject();
            } else {
                // loaded = mc.getProjectController().openAndLoadProject(projectFile);
                loaded = mc.getProjectController().open( projectFile, null ).project;
            }
            LOGGER.debug( "RevertThread completed. Loaded = " + loaded );
        } catch (RepositoryException e) {
            DefaultRepositoryController.postRepoException( e );

        } catch (LibrarySaveException e) {
            DefaultRepositoryController.postRepoException( e );
        }
    }
}
