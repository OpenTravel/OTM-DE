/**
 * 
 */
package com.sabre.schemas.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.ProjectManager;
import com.sabre.schemacompiler.repository.PublishWithLocalDependenciesException;
import com.sabre.schemacompiler.repository.RemoteRepository;
import com.sabre.schemacompiler.repository.Repository;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemacompiler.repository.RepositoryManager;
import com.sabre.schemacompiler.repository.impl.RemoteRepositoryClient;
import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationException;
import com.sabre.schemacompiler.version.MajorVersionHelper;
import com.sabre.schemacompiler.version.MinorVersionHelper;
import com.sabre.schemacompiler.version.PatchVersionHelper;
import com.sabre.schemacompiler.version.VersionSchemeException;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryRoot;
import com.sabre.schemas.views.OtmView;

/**
 * Provide access to the repository manager content in response to requests from repository and
 * other views.
 * 
 * To remove all content from test repository: http://dart.dev.sabre.com:9191/ota2
 * -repository-service-2.2-SNAPSHOT/service/clean-repository
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultRepositoryController implements RepositoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRepositoryController.class);

    RepositoryManager repositoryManager;
    RepositoryRoot repositoryRoot = null;
    MainController mc;

    public DefaultRepositoryController(MainController mc, RepositoryManager localRepositoryManager) {
        this.mc = mc;
        this.repositoryManager = localRepositoryManager;
        try {
            localRepositoryManager.refreshRemoteRepositories();
        } catch (RepositoryException e) {
            LOGGER.debug("Could not get repository Manger. " + e.getLocalizedMessage());
        }
    }

    /**
     * Find a repository node from its native model.
     * 
     * @param repository
     * @return
     */
    public RepositoryNode find(Repository repository) {
        return getRoot().find(repository);
    }

    /**
     * @return the root of the repository tree. Repository root will be created if needed.
     */
    @Override
    public RepositoryRoot getRoot() {
        if (repositoryRoot == null) {
            repositoryRoot = new RepositoryRoot(repositoryManager);
        }
        return repositoryRoot;
    }

    @Override
    public void sync(INode node) {
        if (node instanceof RepositoryNode) {
            updateNode((RepositoryNode) node);
        }
        mc.refresh();
    }

    private void updateNode(RepositoryNode node) {
        node.refresh();
        refreshView(node);
    }

    private void refreshView(INode node) {
        OtmView repositoryView = OtmRegistry.getRepositoryView();
        repositoryView.refresh(node, true);
    }

    /**
     * @return a new list of all repository nodes
     */
    @Override
    public List<RepositoryNode> getAll() {
        List<RepositoryNode> repos = new ArrayList<RepositoryNode>();
        if (getRoot() == null)
            throw new IllegalStateException("Repository root is null.");

        for (INode n : getRoot().getChildren()) {
            if (n instanceof RepositoryNode)
                repos.add((RepositoryNode) n);
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
            if (rn.getRepository().getId().equals(localID))
                return rn;
        }
        return null;
    }

    @Override
    public List<LibraryChainNode> manage(RepositoryNode repository, List<LibraryNode> libs) {
        List<ProjectItem> items = toProjectItems(libs);
        if (!items.isEmpty()) {
            ProjectManager pm = items.get(0).getProjectManager();
            Collection<ProjectItem> publishedItems = publishItems(pm, items,
                    repository.getRepository());
            if (!publishedItems.isEmpty()) {
                Collection<LibraryNode> publishedLibs = findLibraries(publishedItems);
                List<LibraryChainNode> chains = convertToChains(publishedLibs);
                refreshAll(repository);
                return chains;
            }
        }
        return Collections.emptyList();
    }

    private Collection<ProjectItem> publishItems(ProjectManager pm, List<ProjectItem> items,
            Repository repository) {
        Collection<ProjectItem> publishedItems = Collections.emptyList();
        try {
            try {
                publishedItems = publish(pm, items, repository);
            } catch (PublishWithLocalDependenciesException e) {
                try {
                    boolean process = DialogUserNotifier.openConfirm(Messages
                            .getString("repository.warning"), Messages.getString(
                            "repository.warning.loadDependentLibs",
                            toString(getNewPublications(e.getRequestedPublications(),
                                    e.getRequiredPublications()))));
                    if (process) {
                        publishedItems = publish(pm, e.getRequiredPublications(), repository);
                    }
                } catch (PublishWithLocalDependenciesException e1) {
                    DialogUserNotifier.openError(Messages.getString("repository.error.title"),
                            Messages.getString("repository.error.manage"));
                }
            }
        } catch (RepositoryException e) {
            warnUser(e);
        }
        return publishedItems;
    }

    private Collection<ProjectItem> publish(ProjectManager pm, Collection<ProjectItem> items,
            Repository repository) throws RepositoryException,
            PublishWithLocalDependenciesException {
        Collection<ProjectItem> managed = getManagedItems(repository, items);
        if (!managed.isEmpty()) {
            DialogUserNotifier.openError(
                    Messages.getString("repository.warning"),
                    Messages.getString("repository.warning.alreadyManaged",
                            repository.getDisplayName(), toString(managed)));
        }
        Collection<ProjectItem> invalid = getItemsWithInvalidNamespace(repository, items);
        if (!invalid.isEmpty()) {
            DialogUserNotifier.openError(Messages.getString("repository.error.title"), Messages
                    .getString("repository.warning.managedNS", repository.getDisplayName(),
                            toString(invalid)));
            return Collections.emptyList();
        }

        pm.publish(items, repository);
        return items;
    }

    private Collection<ProjectItem> getManagedItems(Repository repository,
            Collection<ProjectItem> items) {
        List<ProjectItem> managed = new ArrayList<ProjectItem>();
        for (ProjectItem i : items) {
            if (!RepositoryItemState.UNMANAGED.equals(i.getState())) {
                managed.add(i);
            }
        }
        return managed;
    }

    private Collection<ProjectItem> getItemsWithInvalidNamespace(Repository repository,
            Collection<ProjectItem> items) {
        List<ProjectItem> invalid = new ArrayList<ProjectItem>();
        for (ProjectItem i : items) {
            if (!isInManagedNS(i.getNamespace(), repository)) {
                invalid.add(i);
            }
        }
        return invalid;
    }

    private String toString(Collection<ProjectItem> newPublications) {
        StringBuilder sb = new StringBuilder();
        String delimenter = ", ";
        for (ProjectItem p : newPublications) {
            sb.append(p.getLibraryName());
            sb.append(delimenter);
        }
        sb.delete(sb.lastIndexOf(delimenter), sb.length());
        return sb.toString();
    }

    private Collection<ProjectItem> getNewPublications(
            Collection<ProjectItem> requestedPublications,
            Collection<ProjectItem> requiredPublications) {
        Set<ProjectItem> items = new HashSet<ProjectItem>();
        for (ProjectItem req : requiredPublications) {
            if (!requestedPublications.contains(req)) {
                items.add(req);
            }
        }
        return items;
    }

    private Collection<LibraryNode> findLibraries(Collection<ProjectItem> publishedItems) {
        List<LibraryNode> libraries = new ArrayList<LibraryNode>(publishedItems.size());
        for (LibraryNode l : mc.getLibraryController().getUserLibraries()) {
            if (publishedItems.contains(l.getProjectItem())) {
                libraries.add(l);
            }
        }
        return libraries;
    }

    private List<LibraryChainNode> convertToChains(Collection<LibraryNode> publishedLibs) {
        List<LibraryChainNode> chains = new ArrayList<LibraryChainNode>(publishedLibs.size());
        for (LibraryNode l : publishedLibs) {
            // TODO: need to execute l.editable () ?? previously it was
            chains.add(new LibraryChainNode(l));
        }
        return chains;
    }

    private List<ProjectItem> toProjectItems(List<LibraryNode> libs) {
        List<ProjectItem> items = new ArrayList<ProjectItem>(libs.size());
        for (LibraryNode l : libs) {
            items.add(l.getProjectItem());
        }
        return items;
    }

    @Override
    public boolean commit(LibraryNode ln) {
        if (!ln.isManaged()) {
            DialogUserNotifier.openWarning(Messages.getString("repository.warning"),
                    Messages.getString("repository.warning.alreadyManaged"));
            return false;
        }

        boolean result = true;
        try {
            ln.commit();
        } catch (RepositoryException e) {
            result = false;
            warnUser(e);
        }
        refreshAll(ln);
        return result;
    }

    @Override
    public void lock() {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            lock(ln);
        }
    }

    @Override
    public boolean lock(LibraryNode ln) {
        boolean result = true;
        try {
            ln.lock();
        } catch (RepositoryException e) {
            result = false;
            warnUser(e);
        }
        LOGGER.debug("Locked library " + ln.getLabel() + " " + result);
        refreshAll(ln);
        return result;
    }

    @Override
    public void unlock(boolean commitWIP) {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            if (commitWIP) {
                unlock(ln);
            } else {
                unlockAndRevert(ln);
            }
        }
    }

    @Override
    public boolean unlock(LibraryNode ln) {
        boolean result = true;
        try {
            final LibraryModelSaver lms = new LibraryModelSaver();
            lms.saveLibrary(ln.getTLLibrary());
            ProjectManager pm = ((DefaultProjectController) mc.getProjectController())
                    .getDefaultProject().getProject().getProjectManager();
            pm.unlock(ln.getProjectItem(), true);
            ln.updateLibraryStatus();
        } catch (RepositoryException e) {
            result = false;
            warnUser(e);
        } catch (LibrarySaveException e) {
            result = false;
            warnUser(e);
        }
        refreshAll(ln);
        LOGGER.debug("UnLocked library " + this);
        return result;
    }

    @Override
    public ProjectNode unlockAndRevert(LibraryNode library) {
        ProjectNode loaded = null;
        try {
            final LibraryModelSaver lms = new LibraryModelSaver();
            lms.saveLibrary(library.getTLLibrary());
            String projectFile = library.getProject().getProject().getProjectFile().toString();
            ProjectManager pm = ((DefaultProjectController) mc.getProjectController())
                    .getDefaultProject().getProject().getProjectManager();
            pm.unlock(library.getProjectItem(), false);
            boolean isDefault = library.getProject() == mc.getProjectController()
                    .getDefaultProject();
            mc.getProjectController().close(library.getProject());
            if (isDefault) {
                loaded = mc.getProjectController().getDefaultProject();
            } else {
                loaded = mc.getProjectController().openProject(projectFile);
            }
        } catch (RepositoryException e) {
            warnUser(e);
        } catch (LibrarySaveException e) {
            warnUser(e);
        }
        refreshAll(library);
        LOGGER.debug("UnLocked library " + this);
        return loaded;

    }

    @Override
    public boolean markFinal(LibraryNode ln) {
        boolean result = true;
        // must be unlocked to be promoted to final.
        if (ln.isLocked())
            if (!unlock(ln))
                return false;

        // Libraries are unexpectedly getting set to final. Do confirm here until this is resolved.
        if (!DialogUserNotifier.openConfirm("Finalize Library",
                "Warning, making a library final can not be undone. Do you want to continue?"))
            return false;

        try {
            ln.markFinal();
        } catch (RepositoryException e) {
            result = false;
            warnUser(e);
        }
        refreshAll(ln);
        return result;
    }

    private void refreshAll(LibraryNode ln) {
        if (ln != null) {
            RepositoryNode repoNode = find(ln.getProjectItem().getRepository());
            refreshAll(repoNode);
        } else {
            refreshAll();
        }
    }

    private void refreshAll(RepositoryNode repoNode) {
        if (repoNode != null) {
            updateNode(repoNode);
            mc.refresh(repoNode);
        } else {
            refreshAll();
        }
    }

    private void refreshAll() {
        refreshView(null);
        mc.refresh();
    }

    private void warnUser(Exception e) {
        DialogUserNotifier.openError(Messages.getString("repository.error.title"),
                e.getLocalizedMessage());
    }

    @Override
    public boolean isInManagedNS(String testNS, RepositoryNode repository) {
        return isInManagedNS(testNS, repository.getRepository());
    }

    private boolean isInManagedNS(String testNS, Repository repository) {
        try {
            for (String rootNS : repository.listRootNamespaces()) {
                if (testNS.startsWith(rootNS))
                    return true;
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not get root ns list: " + e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public List<String> getRootNamespaces() {
        List<String> rootNSs = new ArrayList<String>();
        for (RepositoryNode rn : getAll()) {
            try {
                rootNSs.addAll(rn.getRepository().listRootNamespaces());
            } catch (RepositoryException e) {
                LOGGER.error("Error getting root ns: " + e.getLocalizedMessage());
            }
        }
        return rootNSs;
    }

    @Override
    public boolean validateBaseNamespace(String namespace) {
        for (RepositoryNode rn : getAll()) {
            if (isInManagedNS(namespace, rn))
                return true;
        }
        return false;
    }

    @Override
    public boolean addRemoteRepository(String location, String userId, String password) {
        if (!userId.isEmpty() && password.isEmpty()) {
            LOGGER.debug("Password cannot be empty");
            return false;
        } else if (userId.isEmpty()) {
            userId = null;
        }
        location = stipLocation(location);
        try {
            RemoteRepository repo = repositoryManager.addRemoteRepository(location);
            repositoryManager.setCredentials(repo, userId, password);
            getRoot().addRepository(repo);
            refreshAll();
        } catch (RepositoryException e) {
            LOGGER.debug("Error adding new repository: " + e.getMessage());
            return false;
        }
        return true;
    }

    private String stipLocation(String location) {
        if (location.endsWith("/"))
            return location.substring(0, location.length() - 1);
        return location;
    }

    @Override
    public boolean changeCredentials(String location, String userId, String password) {
        if (!userId.isEmpty() && password.isEmpty()) {
            LOGGER.debug("Password cannot be empty");
            return false;
        } else if (userId.isEmpty()) {
            userId = null;
        }
        RemoteRepository repo = getRemoteRepository(location);
        try {
            repositoryManager.setCredentials(repo, userId, password);
            RepositoryNode repoNode = find(repo);
            updateNode(repoNode);
        } catch (RepositoryException e) {
            LOGGER.debug("Error changing credentials: " + e.getMessage() + ", for repository:"
                    + location);
            return false;
        }
        return true;
    }

    private RemoteRepository getRemoteRepository(String location) {
        for (RemoteRepository rep : repositoryManager.listRemoteRepositories()) {
            if (rep instanceof RemoteRepositoryClient) {
                if (location.equals(((RemoteRepositoryClient) rep).getEndpointUrl())) {
                    return rep;
                }
            }
        }
        return null;
    }

    @Override
    public LibraryNode createPatchVersion(LibraryNode library) {
        if (!versionPreparation(library))
            return null;
        return createVersion(library, true);
    }

    @Override
    public LibraryNode createMinorVersion(LibraryNode library) {
        LibraryNode ln;
        if (!versionPreparation(library))
            return null;
        if (library.isPatchVersion()) {
            // must create from latest minor not the patch.
            String minor = library.getNsHandler().getNS_Minor(library.getNamespace());
            for (Node n : library.getParent().getChildren()) {
                if (!(n instanceof LibraryNode))
                    continue;
                ln = (LibraryNode) n;
                if (ln.isMinorOrMajorVersion()
                        && ln.getNsHandler().getNS_Minor(ln.getNamespace()).equals(minor)) {
                    library = ln;
                    break;
                }
            }
        }
        return createVersion(library, false);
    }

    private LibraryNode createVersion(LibraryNode library, boolean isPatch) {

        RepositoryNode rn = find(library.getProjectItem().getRepository());
        PatchVersionHelper patchVH = new PatchVersionHelper(library.getProject().getProject());
        MinorVersionHelper minorVH = new MinorVersionHelper(library.getProject().getProject());
        TLLibrary newTLLib = null;
        LibraryNode newLib = null;

        try {
            if (isPatch)
                newTLLib = patchVH.createNewPatchVersion(library.getTLLibrary());
            else
                newTLLib = minorVH.createNewMinorVersion(library.getTLLibrary());
        } catch (VersionSchemeException e) {
            warnUser(e);
        } catch (ValidationException e) {
            warnUser(e);
        } catch (LibrarySaveException e) {
            warnUser(e);
        }

        if (newTLLib != null) {
            newLib = mc.getProjectController().add(library, newTLLib);
            manage(rn, Collections.singletonList(newLib));
            library.getChain().add(newLib.getProjectItem());
            lock(newLib);
            sync(rn);
        }
        return newLib;
    }

    @Override
    public LibraryNode createMajorVersion(LibraryNode library) {

        if (!versionPreparation(library))
            return null;

        RepositoryNode rn = find(library.getProjectItem().getRepository());
        MajorVersionHelper mvh = new MajorVersionHelper(library.getProject().getProject());
        TLLibrary major = null;
        LibraryNode newLib = null;

        try {
            major = mvh.createNewMajorVersion(library.getTLLibrary());
        } catch (VersionSchemeException e) {
            warnUser(e);
        } catch (ValidationException e) {
            warnUser(e);
        } catch (LibrarySaveException e) {
            warnUser(e);
        }

        LibraryChainNode lcn = null;
        if (major != null) {
            newLib = new LibraryNode(major, library.getProject());
            List<LibraryChainNode> chains = manage(rn, Collections.singletonList(newLib));
            lcn = findLibrary(chains, newLib.getLibrary().getName());
            if (lcn != null) {
                lock(newLib);
            }
            sync(rn);
        }
        return newLib;
    }

    private LibraryChainNode findLibrary(List<LibraryChainNode> chains, String name) {
        for (LibraryChainNode chain : chains) {
            if (name.equals(chain.getHead().getName()))
                return chain;
        }
        return null;
    }

    /**
     * Prepare the library for new version. Preparation includes committing, unlocking and
     * finalizing. User interactions is sought before making changes.
     * 
     * @param library
     * @return true if ready.
     */
    private boolean versionPreparation(LibraryNode library) {
        if (library.getProject() == null) {
            DialogUserNotifier.openError(Messages.getString("repository.error.title"),
                    Messages.getString("repository.version.noProject"));
            LOGGER.debug("Version Error." + library + " is not in a project.");
            return false;
        }
        RepositoryNode rn = find(library.getProjectItem().getRepository());
        if (rn == null) {
            DialogUserNotifier.openError(Messages.getString("repository.error.title"),
                    Messages.getString("repository.version.notManaged"));
            LOGGER.debug("Version Error." + library + " is not manged in a repository.");
            return false;
        }
        if (!library.isReadyToVersion()) {
            DialogUserNotifier.openError(Messages.getString("repository.error.title"),
                    Messages.getString("repository.warning.notValid"));
            LOGGER.debug("Version Error." + library + " is not valid.");
            return false;
        }
        if (library.getChain() == null) {
            LOGGER.debug("Missing chain for library " + library);
            return false;
        }

        String msgID = "repository.version.check.all";
        boolean isOK = false;
        boolean needsFinal = library.getStatus().equals(TLLibraryStatus.DRAFT);
        boolean editable = library.getEditStatus().equals(NodeEditStatus.FULL);
        if (needsFinal && !editable)
            msgID = "repository.version.check.final";
        else if (!needsFinal)
            msgID = "repository.version.check.OK";

        isOK = DialogUserNotifier.openConfirm(Messages.getString("repository.version.check.title"),
                Messages.getString(msgID));
        if (isOK && needsFinal) {
            if (!library.isLocked())
                lock(library);
            isOK = commit(library);
            if (isOK)
                isOK = unlock(library);
            if (isOK && needsFinal)
                isOK = markFinal(library);
        }
        return isOK;
    }

    /**
     * Searches the contents of the repository using the free-text keywords provided. This method is
     * calling {@link Repository#search(String, boolean, boolean)} by passing as
     * latestVersionsOnly=false and includeDraftVersions=true
     * 
     * @param string
     * @see Repository#search(String, boolean, boolean)
     */
    @Override
    public List<RepositoryItem> search(String string) {
        try {
            // TODO: what parameter pass to search false or true ?
            return repositoryManager.search(string, false, true);
        } catch (RepositoryException e) {
            LOGGER.error("Error during searching  repositories for: " + string + ". Details: "
                    + e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public void removeRemoteRepository(RepositoryNode node) {
        try {
            repositoryManager.removeRemoteRepository((RemoteRepository) node.getRepository());
            getRoot().removeReposutory(node.getRepository());
            refreshAll();
        } catch (RepositoryException e) {
            LOGGER.error("Error when removing  repository: " + node.getIdentity() + ". Details: "
                    + e.getMessage());
        }
    }
}
