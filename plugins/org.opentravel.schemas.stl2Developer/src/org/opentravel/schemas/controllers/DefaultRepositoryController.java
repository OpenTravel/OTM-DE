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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.custom.BusyIndicator;
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
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryTreeRoot;
import org.opentravel.schemas.views.OtmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to the repository manager content in response to requests from repository and other views.
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultRepositoryController implements RepositoryController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRepositoryController.class);

	RepositoryManager repositoryManager;
	RepositoryTreeRoot repositoryRoot = null;
	MainController mc;

	public DefaultRepositoryController(MainController mc, RepositoryManager localRepositoryManager) {
		this.mc = mc;
		this.repositoryManager = localRepositoryManager;
		// 5/27/2015 dmh - for performance reasons when the network connection is bad skip this refresh
		// try {
		// localRepositoryManager.refreshRemoteRepositories();
		// } catch (RepositoryException e) {
		// postRepoException(e);
		// }
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
	public RepositoryTreeRoot getRoot() {
		if (repositoryRoot == null) {
			repositoryRoot = new RepositoryTreeRoot(repositoryManager);
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
			Collection<ProjectItem> publishedItems = publishItems(pm, items, repository.getRepository());
			if (!publishedItems.isEmpty()) {
				Collection<LibraryNode> publishedLibs = findLibraries(publishedItems);
				List<LibraryChainNode> chains = convertToChains(publishedLibs);
				refreshAll(repository);
				return chains;
			}
		}
		return Collections.emptyList();
	}

	private Collection<ProjectItem> publishItems(ProjectManager pm, List<ProjectItem> items, Repository repository) {
		Collection<ProjectItem> publishedItems = Collections.emptyList();
		try {
			try {
				publishedItems = publish(pm, items, repository);
			} catch (PublishWithLocalDependenciesException e) {
				try {
					boolean process = DialogUserNotifier.openConfirm(
							Messages.getString("repository.warning"),
							Messages.getString(
									"repository.warning.loadDependentLibs",
									toString(getNewPublications(e.getRequestedPublications(),
											e.getRequiredPublications()))));
					if (process) {
						publishedItems = publish(pm, e.getRequiredPublications(), repository);
					}
				} catch (PublishWithLocalDependenciesException e1) {
					postRepoError("manage");
				}
			}
		} catch (RepositoryException e) {
			postRepoException(e);
		}
		return publishedItems;
	}

	private Collection<ProjectItem> publish(ProjectManager pm, Collection<ProjectItem> items, Repository repository)
			throws RepositoryException, PublishWithLocalDependenciesException {
		Collection<ProjectItem> managed = getManagedItems(repository, items);
		if (!managed.isEmpty()) {
			postRepoError("alreadyManaged");
		}
		Collection<ProjectItem> invalid = getItemsWithInvalidNamespace(repository, items);
		if (!invalid.isEmpty()) {
			postRepoError("managedNS");
			return Collections.emptyList();
		}

		pm.publish(items, repository);
		return items;
	}

	private Collection<ProjectItem> getManagedItems(Repository repository, Collection<ProjectItem> items) {
		List<ProjectItem> managed = new ArrayList<ProjectItem>();
		for (ProjectItem i : items) {
			if (!RepositoryItemState.UNMANAGED.equals(i.getState())) {
				managed.add(i);
			}
		}
		return managed;
	}

	private Collection<ProjectItem> getItemsWithInvalidNamespace(Repository repository, Collection<ProjectItem> items) {
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

	private Collection<ProjectItem> getNewPublications(Collection<ProjectItem> requestedPublications,
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
			postRepoWarning("alreadyManaged");
			return false;
		}
		CommitThread ct = new CommitThread(ln);
		BusyIndicator.showWhile(mc.getMainWindow().getDisplay(), ct);
		refreshAll(ln);
		return ct.getResult();
	}

	@Override
	public void lock() {
		for (LibraryNode ln : mc.getSelectedLibraries()) {
			lock(ln);
		}
	}

	@Override
	public boolean lock(LibraryNode ln) {
		LockThread lt = new LockThread(ln);
		BusyIndicator.showWhile(mc.getMainWindow().getDisplay(), lt);
		if (lt.getException() != null) {
			LOGGER.debug("TODO - handle exception: " + lt.getException().getLocalizedMessage());
		}
		refreshAll(ln);
		return true;
	}

	// Used by the action.
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

	// TODO - should be private - only used internally and in testing
	// return value only used in versionPreperation
	@Override
	public boolean unlock(LibraryNode ln) {
		UnlockThread ut = new UnlockThread(ln, mc);
		BusyIndicator.showWhile(mc.getMainWindow().getDisplay(), ut);
		refreshAll(ln);
		LOGGER.debug("UnLocked library " + this);
		return ut.getResult();
	}

	@Override
	public ProjectNode unlockAndRevert(LibraryNode ln) {
		RevertThread rt = new RevertThread(ln, mc);
		BusyIndicator.showWhile(mc.getMainWindow().getDisplay(), rt);
		refreshAll(ln);
		LOGGER.debug("UnLocked and reverted library " + ln);
		return rt.getLoaded();

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
			postRepoException(e);
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
			postRepoException(e);
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
				postRepoException(e);
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
			postRepoError("password");
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
			// Post directly to get message and exception message
			String msg = Messages.getString(REPO_MESSAGE_PREFIX + "invalidLocation");
			msg += "\n\n" + e.getLocalizedMessage();
			DialogUserNotifier.openError(Messages.getString(REPO_ERROR_TITLE), msg);
			LOGGER.debug("Repository Error: " + msg);
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
			postRepoError("password");
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
			postRepoException(e);
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
				if (ln.isMinorOrMajorVersion() && ln.getNsHandler().getNS_Minor(ln.getNamespace()).equals(minor)) {
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
			postRepoException(e);
		} catch (ValidationException e) {
			postRepoException(e);
		} catch (LibrarySaveException e) {
			postRepoException(e);
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
			postRepoException(e);
		} catch (ValidationException e) {
			postRepoException(e);
		} catch (LibrarySaveException e) {
			postRepoException(e);
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
	 * Prepare the library for new version. Preparation includes committing, unlocking and finalizing. User interactions
	 * is sought before making changes.
	 * 
	 * @param library
	 * @return true if ready.
	 */
	private boolean versionPreparation(LibraryNode library) {
		if (library.getProject() == null) {
			postRepoError("noProject");
			return false;
		}
		RepositoryNode rn = find(library.getProjectItem().getRepository());
		if (rn == null) {
			postRepoError("notManaged");
			return false;
		}
		if (!library.isReadyToVersion()) {
			postRepoError("notValid");
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
			return repositoryManager.search(string, false, true);
		} catch (RepositoryException e) {
			postRepoException(e);
		}
		return Collections.emptyList();
	}

	@Override
	public void removeRemoteRepository(RepositoryNode node) {
		try {
			repositoryManager.removeRemoteRepository((RemoteRepository) node.getRepository());
			getRoot().removeRepository(node.getRepository());
			refreshAll();
		} catch (RepositoryException e) {
			postRepoException(e);
		}
	}

	public static String REPO_ERROR_TITLE = "repository.error.title";
	public static String REPO_WARNING_TITLE = "repository.warning";
	public static String REPO_MESSAGE_PREFIX = "repository.warning.";

	public static void postRepoException(Exception e) {
		DialogUserNotifier.openError(Messages.getString(REPO_ERROR_TITLE), e.getLocalizedMessage());
		LOGGER.debug("Repository Exception: " + e.getLocalizedMessage());
	}

	public static void postRepoWarning(String message) {
		final String msg = Messages.getString(REPO_MESSAGE_PREFIX + message);
		if (msg.equals('!' + message + '!'))
			LOGGER.error("Bad warning message: " + message);
		DialogUserNotifier.openWarning(Messages.getString(REPO_WARNING_TITLE), msg);
		LOGGER.debug("Repository Warning: " + msg);
	}

	public static void postRepoError(String message) {
		final String msg = Messages.getString(REPO_MESSAGE_PREFIX + message);
		if (msg.equals('!' + message + '!'))
			LOGGER.error("Bad error message: " + message);
		DialogUserNotifier.openError(Messages.getString(REPO_ERROR_TITLE), msg);
		LOGGER.debug("Repository Error: " + msg);
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

	public void run() {
		try {
			ln.lock();
			DialogUserNotifier.syncWithUi("Locked " + ln);
		} catch (RepositoryException e) {
			exception = e;
			DefaultRepositoryController.postRepoException(e);
		}
	}

	public Exception getException() {
		return exception;
	}
}

class UnlockThread extends Thread {
	private LibraryNode ln;
	private MainController mc;
	private boolean result = false;

	public UnlockThread(LibraryNode ln, MainController mc) {
		this.ln = ln;
		this.mc = mc;
	}

	public boolean getResult() {
		return result;
	}

	public void run() {
		try {
			final LibraryModelSaver lms = new LibraryModelSaver();
			lms.saveLibrary(ln.getTLLibrary());
			ProjectManager pm = ((DefaultProjectController) mc.getProjectController()).getDefaultProject().getProject()
					.getProjectManager();
			pm.unlock(ln.getProjectItem(), true);
			ln.updateLibraryStatus();
			result = true;
		} catch (RepositoryException e) {
			result = false;
			DefaultRepositoryController.postRepoException(e);
			// DialogUserNotifier.openError(Messages.getString("repository.error.title"), e.getLocalizedMessage());
		} catch (LibrarySaveException e) {
			result = false;
			DefaultRepositoryController.postRepoException(e);
			// DialogUserNotifier.openError(Messages.getString("repository.error.title"), e.getLocalizedMessage());
		}
	}
}

class CommitThread extends Thread {
	private LibraryNode ln;
	private boolean result = true;

	public CommitThread(LibraryNode ln) {
		this.ln = ln;
	}

	public boolean getResult() {
		return result;
	}

	public void run() {
		try {
			ln.commit();
		} catch (RepositoryException e) {
			result = false;
			DefaultRepositoryController.postRepoException(e);
		}
	}
}

class RevertThread extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRepositoryController.class);

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

	public void run() {
		try {
			final LibraryModelSaver lms = new LibraryModelSaver();
			lms.saveLibrary(ln.getTLLibrary());
			String projectFile = ln.getProject().getProject().getProjectFile().toString();
			ProjectManager pm = ((DefaultProjectController) mc.getProjectController()).getDefaultProject().getProject()
					.getProjectManager();
			pm.unlock(ln.getProjectItem(), false);
			boolean isDefault = ln.getProject() == mc.getProjectController().getDefaultProject();
			mc.getProjectController().close(ln.getProject());
			if (isDefault) {
				loaded = mc.getProjectController().getDefaultProject();
			} else {
				loaded = mc.getProjectController().openProject(projectFile);
			}
			LOGGER.debug("RevertThread completed. Loaded = " + loaded);
		} catch (RepositoryException e) {
			DefaultRepositoryController.postRepoException(e);

		} catch (LibrarySaveException e) {
			DefaultRepositoryController.postRepoException(e);
		}
	}
}
