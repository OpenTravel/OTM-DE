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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.opentravel.ns.ota2.project_v01_00.ManagedProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ProjectItemType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.preferences.DefaultPreferences;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.Activator;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.FindingsDialog;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.wizards.NewProjectWizard;
import org.opentravel.schemas.wizards.validators.NewProjectValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Project Controller.
 * 
 * Reads/Saves open projects in session store. Creates built in project for built in libraries. Creates and manages
 * access to default project if not restored from session.
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultProjectController implements ProjectController {

	// TODO - consider eliminate this extra class - just load into the project. it was used to make saving to memento
	// generic.
	public interface IProjectToken extends IAdaptable {
		static IProjectToken[] NONE = new IProjectToken[] {};

		String getLocation();

		String getName();

		void setName(String newName);
	}

	public class ProjectToken implements IProjectToken {
		private String name;
		private String location;

		public ProjectToken(File file) {
			try {
				location = file.getCanonicalPath();
			} catch (IOException e) {
				LOGGER.error("Could not create project token due to bad path.");
				e.printStackTrace();
			}
		}

		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			return null;
		}

		@Override
		public String getLocation() {
			return location;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String newName) {
			name = newName;
		}

	}

	/**
	 * Class for grouping TL Project, ProjectNode, validation findings and result messages.
	 */
	public class OpenedProject {
		public String resultMsg = "";
		public ValidationFindings findings;
		public Project tlProject;
		public ProjectNode project;
		public Throwable exception;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectController.class);
	public static final String PROJECT_EXT = "otp";
	private static final String OTM_PROJECTS = "OTM_Projects";

	// private static final String TAG_FAVORITES = "Favorites";
	public static final String OTM_PROJECT = "Project";
	private static final String OTM_PROJECT_NAME = "Name";
	private static final String OTM_PROJECT_LOCATION = "Location";

	public static String MementoFileName = "OTM_DE_Startup.xml";

	private final MainController mc;
	private final ProjectManager projectManager;
	protected ProjectNode defaultProject = null;
	protected ProjectNode builtInProject = null;

	private String defaultNS = "http://www.opentravel.org/OTM2/DefaultProject";

	private String defaultPath;

	// override with ns of local repository
	private final String dialogText = "Select a project file";

	Collection<IProjectToken> openProjects = new ArrayList<>();

	/**
	 * Create controller and open projects in background thread.
	 */
	public DefaultProjectController(final MainController mc, RepositoryManager repositoryManager) {
		this.mc = mc;
		this.projectManager = new ProjectManager(mc.getModelController().getTLModel(), true, repositoryManager);
		getBuiltInProject(); // make sure these exist
		getDefaultProject(); // make sure these exist
	}

	/**
	 * Add tlLibrary to the chain
	 * {@link org.opentravel.schemas.controllers.DefaultRepositoryController#createVersion(LibraryNode, boolean)}
	 */
	// Removed call from createVersion 6/7/2018. Leave until further testing confirms it was not needed.
	@Deprecated
	@Override
	public LibraryNode add(LibraryChainNode lcn, AbstractLibrary tlLib) {
		// FIXME - libraries do not know all the projects they might be in
		// FIXME - new library is not added to library model manager
		// Only used by DefaultRepositoryManager#createVersion()
		LibraryNode ln = null;
		// ProjectNode pn = lcn.getProject();
		// if (lcn == null || pn == null || tlLib == null)
		// throw new IllegalArgumentException("Null argument.");
		//
		// ProjectItem pi = null;
		// try { // NOT NEEDED - done when version created
		// pi = pn.getTLProject().getProjectManager().addUnmanagedProjectItem(tlLib, pn.getTLProject());
		// } catch (RepositoryException e) {
		// // LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
		// DialogUserNotifier.openError("Add Project Error", e.getLocalizedMessage(), e);
		// }
		// if (pi != null)
		// ln = new LibraryNode(pi, lcn);
		// // mc.refresh(pn);
		// // LOGGER.debug("Added library " + ln.getName() + " to " + pn);
		return ln;
	}

	/**
	 * {@link org.opentravel.schemas.controllers.DefaultLibraryController#createNewLibraryFromPrototype(LibraryNode)}
	 * {@link org.opentravel.schemas.controllers.DefaultLibraryController#createLibrary(String, String, URL, String, ProjectNode)}
	 */
	@Override
	public LibraryNavNode add(ProjectNode pn, AbstractLibrary tlLib) {
		if (pn == null || tlLib == null)
			throw new IllegalArgumentException("Null argument.");

		ProjectItem pi = null;
		LibraryNavNode lnn = null;
		try {
			pi = pn.getTLProject().getProjectManager().addUnmanagedProjectItem(tlLib, pn.getTLProject());
			if (pi != null) {
				List<ProjectItem> piList = new ArrayList<>();
				piList.add(pi);
				lnn = pn.load(piList);
				mc.refresh(pn);
				// LOGGER.debug("Added library " + ln.getName() + " to " + pn);
			}
		} catch (RepositoryException e) {
			// LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
			DialogUserNotifier.openError("Add Project Error", e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException ex) {
			// LOGGER.error("Could not add repository item to project. " + ex.getLocalizedMessage());
			DialogUserNotifier.openError("Add Project Error", ex.getLocalizedMessage(), ex);
		}
		return lnn;
	}

	/**
	 * Add passed repository item to managed project.
	 * 
	 * The project items are then loaded into the ProjectNode which creates the modeled libraries.
	 * 
	 * Forces repository refresh policy to ALWAYS for this action.
	 * 
	 * Displays findings. Types are resolved and the gui is refreshed.
	 * 
	 * @see {@link org.opentravel.schemas.controllers.DefaultRepositoryController#getVersionUpdateMap()}
	 *      {@link org.opentravel.schemas.actions.AddToProjectAction#run()}
	 * 
	 * @return the first project item returned from project manager
	 * 
	 */
	@Override
	public ProjectItem add(ProjectNode project, RepositoryItem ri) {
		if (project == null)
			return null;

		ProjectItem pi = null;
		List<ProjectItem> piList = null;
		ValidationFindings findings = new ValidationFindings();
		try {
			// workaround to make sure the local version of library will be up-to-date with remote repository
			RefreshPolicy refreshPolicy = null;
			if (ri.getRepository() instanceof RemoteRepositoryClient) {
				RemoteRepositoryClient client = (RemoteRepositoryClient) ri.getRepository();
				refreshPolicy = client.getRefreshPolicy();
				client.setRefreshPolicy(RefreshPolicy.ALWAYS);
			}

			// Add RepoItem to managed project
			piList = project.getTLProject().getProjectManager().addManagedProjectItems(
					Arrays.asList(new RepositoryItem[] { ri }), project.getTLProject(), findings);

			// restore refresh policy
			if (ri.getRepository() instanceof RemoteRepositoryClient) {
				RemoteRepositoryClient client = (RemoteRepositoryClient) ri.getRepository();
				client.setRefreshPolicy(refreshPolicy);
			}
			if (findings.hasFinding()) {
				if (PlatformUI.isWorkbenchRunning())
					// pi list limits to only findings relevant to the opened libraries
					FindingsDialog.open(OtmRegistry.getActiveShell(), Messages.getString("dialog.findings.title"),
							Messages.getString("dialog.findings.message"), findings.getAllFindingsAsList(), piList);
			}
		} catch (LibraryLoaderException e) {
			// LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
			DialogUserNotifier.openError("Add Project Error", e.getLocalizedMessage(), e);

		} catch (RepositoryException e) {
			// LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
			DialogUserNotifier.openError("Add Project Error", e.getLocalizedMessage(), e);
		}

		// piList now has all of the project items for this chain.
		if (!piList.isEmpty()) {
			pi = piList.get(0);
			project.load(piList);
			// TODO - catch and report on errors
			try {
				project.getTLProject().getProjectManager().saveProject(project.getTLProject());
			} catch (LibrarySaveException e) {
				e.printStackTrace();
				DialogUserNotifier.openError("Could not save project.", e.getLocalizedMessage(), e);
			}
			// }
			// do the whole model to check if new libraries resolved broken links.
			new TypeResolver().resolveTypes();
			mc.refresh(project);
		} else
			LOGGER.warn(
					"Repository item " + ri.getLibraryName() + " not added to project. No ManagedProjectItems found.");

		// TODO - sync the repository view
		// LOGGER.debug("Added repository item " + ri.getLibraryName() + " to " + project);
		// mc.showBusy(false);
		return pi;
	}

	/**
	 * @see {@link org.opentravel.schemas.node.ProjectNode#add()}
	 */
	@Override
	public List<ProjectItem> addLibrariesToTLProject(Project project, List<File> libraryFiles) {
		ValidationFindings findings = new ValidationFindings();
		List<ProjectItem> newItems = Collections.emptyList();
		try {
			newItems = openLibrary(project, libraryFiles, findings);
			ValidationFindings loadingFindings = getLoadingFindings(findings);
			if (!loadingFindings.isEmpty()) {
				// LOGGER.error("Validation findings opening: " + libraryFiles);
				showFindings(loadingFindings);
			}
		} catch (RepositoryException e) {
			// LOGGER.error("Could not add library file to project.");
			DialogUserNotifier.openError("Add Project Error", "Could not add to project.", e);
		} catch (LibraryLoaderException e) {
			// LOGGER.error("Could not add library file to project.");
			DialogUserNotifier.openError("Add Project Error", "Could not add to project.", e);
		} catch (Throwable e) {
			// LOGGER.error("Error when adding to project", e);
			DialogUserNotifier.openError("Add Project Error", "Could not add to project.", e);
		}
		// LOGGER.debug("Added libraries to " + project.getName());
		return newItems;
	}

	// /**
	// * @param defaultProject
	// * the defaultProject to set
	// */
	// public void setDefaultProject(ProjectNode defaultProject) {
	// this.defaultProject = defaultProject;
	// }

	@Override
	public boolean close(ProjectNode pn) {
		boolean result = true;
		if (pn == null || pn.isBuiltIn()) {
			return result;
		}
		if (pn == getDefaultProject()) {
			pn.closeAll();
			for (ProjectItem item : pn.getTLProject().getProjectItems())
				pn.getTLProject().remove(item);
		} else {
			// LOGGER.debug("Closing project " + pn);
			result = save(pn); // Try to save the project file
			if (result) // If successful, try to close the TL Project
				result = closeTL(pn.getTLProject().getProjectManager(), pn.getTLProject());

			// No matter what, close the project node and remove from model
			pn.close();
			Node.getModelNode().removeProject(pn);

			if (!result)
				DialogUserNotifier.openError("Error Closing Project.", "Please restart.", null);
			if (Display.getCurrent() != null)
				mc.refresh();
			if (!result)
				LOGGER.warn("Error closing project " + pn);
		}
		return result;
		// LOGGER.debug("Closed project: " + pn);
	}

	/**
	 * Close all projects.
	 * <P>
	 * Use the TL project manager to close all TL libraries.
	 * <p>
	 * Close the model node to close projects, clear the library manager and reset implied nodes.
	 */
	@Override
	public void closeAll() {
		mc.getModelNode().close(false); // First close user projects. Some of the close handlers will need simple
										// built-in types.
		mc.getModelNode().close(true); // Now close the rest.

		// re-initializes the contents of the model, removes default project and creates new builtInProject
		mc.getModelNode().getTLModel().clearModel();
		projectManager.closeAll();
		assert projectManager.getAllProjects().size() == 1;
		// List<Project> projects = projectManager.getAllProjects();

		// Model the new built in library
		for (Project p : projectManager.getAllProjects())
			if (p instanceof BuiltInProject) {
				builtInProject = new ProjectNode(p);
				break;
			}
		assert builtInProject != null;
		assert builtInProject.getTLProject().getProjectManager() == projectManager;
		assert projectManager.getModel() != null;
		assert projectManager.getAllProjects().contains(builtInProject.getTLProject());

		// Create a new default project
		createDefaultProject();
		assert defaultProject.getTLProject().getProjectManager() == projectManager;
		assert projectManager.getModel() != null;
		assert projectManager.getAllProjects().contains(defaultProject.getTLProject());

		// Assure the built-ins do not need to be remodeled
		for (LibraryNode ln : getBuiltInProject().getLibraries()) {
			assert ln == Node.GetNode(ln.getTLModelObject());
			for (TypeProvider n : ln.getDescendants_TypeProviders()) {
				((Node) n).setDeleted(false);
				n.getWhereAssignedHandler().clear(); // Excessive, but safe
				assert !((Node) n).isDeleted();
				assert n.getWhereAssignedCount() == 0;
			}
		}
	}

	// for some reason closing project is not reliable. Try multiple times
	private boolean closeTL(ProjectManager pm, Project tlProject) {
		int tryCount = 1;
		int maxTries = 10; // 3/23/2018 - increased count from 5 to 10 after team having close problems.
		while (tryCount < maxTries) {
			try {
				pm.closeProject(tlProject);
				// LOGGER.debug("Closed project " + tlProject.getName() + " on the " + tryCount + " try.");
				return true;
			} catch (ConcurrentModificationException e) {
				LOGGER.error("ConcurrentModification error closing project - trying again " + tryCount);
				tryCount++;
				e.printStackTrace();
			} catch (Exception ie) {
				LOGGER.error("Error on retry closing project: " + ie.getLocalizedMessage());
				tryCount++;
				ie.printStackTrace();
			}
		}
		return false;
	}

	// /**
	// * Open and load project in current thread.
	// */
	// // Used by Revert
	// @Override
	// public ProjectNode openAndLoadProject(String fileName) {
	// OpenedProject project = openTLProject(fileName);
	// return loadProject(project.tlProject);
	// }

	@Override
	public ProjectNode create(File file, String ID, String name, String description) {
		Project newProject;
		// for (Project tlp : projectManager.getAllProjects())
		// if (tlp.getProjectId().equals(ID)) {
		// LOGGER.debug("Found project " + ID);
		// }
		try {
			newProject = projectManager.newProject(file, ID, name, description);
		} catch (Exception e) {
			// LOGGER.error("Could not create new project: ", e);
			DialogUserNotifier.openError("New Project Error", e.getLocalizedMessage(), e);
			return null;
		}
		return new ProjectNode(newProject);
		// TODO - what to do if the default project is corrupt. The user will have no way to fix it.
	}

	protected void createDefaultProject() {
		// FIXME - TESTME - this should be using the local repository ns.
		defaultNS = mc.getRepositoryController().getLocalRepository().getNamespace();
		defaultPath = DefaultPreferences.getDefaultProjectPath();

		File df = new File(defaultPath);
		if (df.isFile() && df.canWrite()) {
			df.delete();
			LOGGER.debug("Default project existed and was deleted.");
		}
		defaultProject = create(new File(defaultPath), defaultNS, "Default Project",
				"Used for libraries not loaded into a project.");

		if (defaultProject == null) {
			DialogUserNotifier.openError("Default Project Error",
					Messages.getString("error.openProject.defaultProject", defaultPath), null);
		}
		defaultPath = defaultProject.getTLProject().getProjectFile().getPath();
		mc.getModelNode().addProject(defaultProject);
	}

	@Override
	public List<ProjectNode> getAll() {
		List<ProjectNode> projects = new ArrayList<>();
		for (INode n : mc.getModelNode().getChildren()) {
			if (n instanceof ProjectNode)
				projects.add((ProjectNode) n);
		}
		return projects;
	}

	/**
	 * @return the builtInProject
	 */
	@Override
	public ProjectNode getBuiltInProject() {
		if (builtInProject == null)
			loadProject_BuiltIn();
		return builtInProject;
	}

	/**
	 * @return the defaultProject
	 */
	@Override
	public ProjectNode getDefaultProject() {
		// 9/9/2015 - dmh - the if statement was commented out but will not have default project until started twice.
		if (defaultProject == null)
			createDefaultProject();
		return defaultProject;
	}

	/**
	 * @return the namespace managed by the default project.
	 */
	@Override
	public String getDefaultUnmanagedNS() {
		return defaultProject == null ? "" : defaultProject.getNamespace();
	}

	/**
	 * @param findings
	 * @return
	 */
	private ValidationFindings getLoadingFindings(ValidationFindings findings) {
		return LibraryModelLoader.filterLoaderFindings(findings);
	}

	/**
	 * Get the memento with project data in them. Must be done in UI thread.
	 * 
	 * @return
	 */
	public XMLMemento getMemento() {
		if (Display.getCurrent() == null)
			LOGGER.warn("Warning - getting memento from thread that is not UI thread.");
		XMLMemento memento = null;
		FileReader reader = null;
		try {
			reader = new FileReader(getOTM_StateFile());
			memento = XMLMemento.createReadRoot(reader);
		} catch (FileNotFoundException e) {
			// Ignored... no items exist yet.
			return null;
		} catch (Exception e) {
			// Log the exception and move on.
			LOGGER.error("getMemento error: " + getOTM_StateFile().toString() + " e= " + e);
			return null;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				LOGGER.error("getMemento error: " + e);
				return null;
			}
		}
		// printout projects
		// IMemento[] children = memento.getChildren(OTM_PROJECT);
		// for (int i = 0; i < children.length; i++) {
		// LOGGER.debug("GetMemento found project: " + children[i].getString(OTM_PROJECT_LOCATION));
		// }

		return memento;
	}

	@Override
	public String getNamespace() {
		return null;
	}

	@Override
	public List<String> getOpenGovernedNamespaces() {
		List<String> projects = new ArrayList<>();
		for (Project p : projectManager.getAllProjects()) {
			if (p instanceof BuiltInProject) {
				continue;
			}
			projects.add(RepositoryNamespaceUtils.normalizeUri(p.getProjectId()));
		}
		return projects;
	}

	private File getOTM_StateFile() {
		File ota2File = null;
		File file = ((DefaultRepositoryController) OtmRegistry.getMainController().getRepositoryController())
				.getRepositoryFileLocation();
		if (file != null) {
			String ota2FileName = file.getParentFile().getAbsolutePath() + File.separator + MementoFileName;
			ota2File = new File(ota2FileName);
			// LOGGER.debug("Repo Path = " + ota2File.getAbsolutePath());
		} else {
			ota2File = Activator.getDefault().getStateLocation().append(MementoFileName).toFile();
		}
		return ota2File;
		// 9/11/2015 - FIXME - TESTME - use this path instead of plug-in location
		// return Activator.getDefault().getStateLocation().append("OTM_Developer.xml").toFile();
	}

	@Override
	public List<String> getSuggestedNamespaces() {
		List<String> allowedNSs = new ArrayList<>();
		allowedNSs.add(getDefaultUnmanagedNS());
		allowedNSs.addAll(getOpenGovernedNamespaces());
		return allowedNSs;
	}

	/**
	 * Initialize projects - open built-in and any defined in the system memento. MUST be in UI thread to pick up the
	 * memento.
	 * 
	 * Designed to be run from the Application Workbench Advisor.
	 * 
	 */
	public void initProjects() {
		final XMLMemento memento = getMemento();

		// Find and open the project for Built-in libraries
		if (builtInProject == null)
			loadProject_BuiltIn();

		// Get the ui thread to create the progress monitor and run the open projects in background.
		if (memento != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// Start the non-ui thread Job to do initial load of projects in background
					Job job = new Job("Opening Saved Projects") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("Opening Project", memento.getChildren().length * 2);
							monitor.worked(1);
							IStatus status = loadProjects(memento, monitor);

							monitor.worked(1);
							new TypeResolver().resolveTypes();
							testAndSetDefaultProject();

							monitor.done();
							DialogUserNotifier.syncWithUi("Project Opened");
							return status;
						};
					};
					job.setUser(true);
					job.schedule();
				}
			});
		}
		// LOGGER.info("Done initializing " + this.getClass());
	}

	/**
	 * Load a TL project into the GUI model. Creates Project Node which adds all the libraries in the project.
	 * 
	 * @param project
	 */
	public ProjectNode loadProject(Project project) {
		if (project == null)
			return null;
		if (Display.getCurrent() != null) {
			mc.showBusy(true);
			mc.postStatus("Loading Project: " + project.getName());
			mc.refresh();
		}
		// LOGGER.debug("Creating Project Node.");
		ProjectNode pn = new ProjectNode(project);
		if (Display.getCurrent() != null) {
			mc.selectNavigatorNodeAndRefresh(pn);
			mc.postStatus("Loaded Project: " + pn);
			mc.showBusy(false);
		}
		return pn;
	}

	/**
	 * Load the built-in project. Reads tl project from project manager.
	 */
	public void loadProject_BuiltIn() {
		builtInProject = new ProjectNode(); // leave empty project to note we have tried to load
		for (Project p : projectManager.getAllProjects()) {
			if (p.getProjectId().equals(BuiltInProject.BUILTIN_PROJECT_ID)) {
				builtInProject = loadProject(p);
			}
		}
	}

	public IStatus loadProjects(XMLMemento memento, IProgressMonitor monitor) {
		if (memento != null) {
			IMemento[] children = memento.getChildren(OTM_PROJECT);
			monitor.beginTask("Opening Projects", memento.getChildren(OTM_PROJECT).length * 2);
			for (IMemento mProject : children) {
				monitor.subTask(mProject.getString(OTM_PROJECT_LOCATION));
				// Skip the default project which is opened earlier
				if (mProject.getString(OTM_PROJECT_LOCATION).equals(defaultPath))
					continue;
				open(mProject.getString(OTM_PROJECT_LOCATION), monitor);
				monitor.worked(1);
				if (monitor.isCanceled()) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * {@link org.opentravel.schemas.actions.NewProjectAction#run()}
	 */
	@Override
	public ProjectNode newProject() {
		// Run the wizard
		final NewProjectWizard wizard = new NewProjectWizard();
		wizard.setValidator(new NewProjectValidator());
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			create(wizard.getFile(), wizard.getNamespace(), wizard.getName(), wizard.getDescription());
			mc.refresh();
		}
		return null;
	}

	// TODO - collapse these into one
	/**
	 * {@link org.opentravel.schemas.commands.CreateProjectFromRepo#execute(ExecutionEvent)}
	 */
	@Override
	public void newProject(String defaultName, String selectedRoot, String selectedExt) {
		// Run the wizard
		final NewProjectWizard wizard = new NewProjectWizard(defaultName, selectedRoot, selectedExt);
		wizard.setValidator(new NewProjectValidator());
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			create(wizard.getFile(), wizard.getNamespace(), wizard.getName(), wizard.getDescription());
			mc.refresh();
		}
	}

	/**
	 * Entry point for command handler.
	 * 
	 * Prompt the user for the file path. Creates job to run open(filePath, progressMonitor) then runs type resolver. If
	 * there is no current display the project is simply opened and type resolver run.
	 */
	@Override
	public void open() {
		// LOGGER.debug("Open Project.");
		String[] extensions = { "*." + PROJECT_EXT };
		final String fn = FileDialogs.postFileDialog(extensions, dialogText);
		if (fn == null)
			return;

		if (Display.getCurrent() == null) {
			open(fn, null); // not in UI Thread
			new TypeResolver().resolveTypes();
		} else {
			// run in a background job
			mc.postStatus("Opening " + fn);
			Job job = new Job("Opening Projects") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Opening Project: " + fn, 3);
					monitor.worked(1);
					OpenedProject op = open(fn, monitor);
					monitor.worked(1);
					monitor.subTask("Resolving types");
					if (op == null) {
						return Status.CANCEL_STATUS;
					}
					new TypeResolver().resolveTypes();

					monitor.done();
					DialogUserNotifier.syncWithUi(op.resultMsg);
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	/**
	 * Open Projects using the file names. Update UI and monitor twice for each file. Then loadProject() Used in open()
	 * and refreshMaster()
	 */
	public OpenedProject open(ArrayList<String> projectFiles, IProgressMonitor monitor) {
		// LOGGER.debug("Opening project from file: " + fileName);
		OpenedProject op = null;
		if (projectFiles == null || projectFiles.isEmpty()) {
			op = new OpenedProject();
			op.resultMsg = "Tried to open null or empty file.";
			return op;
		}
		for (String fileName : projectFiles) {
			if (monitor != null) {
				monitor.subTask("Opening file " + fileName);
			}
			op = openTLProject(fileName);
			if (op.tlProject == null) {
				DialogUserNotifier.syncErrorWithUi("Failed to open project " + fileName + "\n" + op.resultMsg,
						op.exception);
				return null;
			}

			// Determine success/failure messages for the user
			int itemCnt = op.tlProject.getProjectItems().size();
			int failedCnt = 0;
			String failures = "";
			if (op.tlProject.getFailedProjectItems() != null)
				failedCnt = op.tlProject.getFailedProjectItems().size();
			if (failedCnt > 0) {
				failures = "Project " + op.tlProject.getName() + ": ";
				failures += "Read " + itemCnt + " items. Failed to Read " + failedCnt + " items.\n";
				for (ProjectItemType item : op.tlProject.getFailedProjectItems())
					if (item instanceof ManagedProjectItemType) {
						failures += ((ManagedProjectItemType) item).getBaseNamespace();
						failures += " " + ((ManagedProjectItemType) item).getFilename() + "\n";
					}
				LOGGER.warn(failures);
			}
			if (monitor != null) {
				monitor.worked(1);
				if (failedCnt > 0)
					monitor.subTask(failures + "Attempting to create model.");
				else
					monitor.subTask("Read " + itemCnt + " items. Creating model");
			}
			op.project = loadProject(op.tlProject); // null param safe

			// If project is null and monitor is not null then an error occurred in background
			if (monitor != null) {
				monitor.worked(1);
				if (op.project == null)
					DialogUserNotifier.syncErrorWithUi(op.resultMsg, op.exception);
				else if (!failures.isEmpty())
					DialogUserNotifier.syncErrorWithUi(failures, op.exception);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						mc.refresh(); // update the user interface asynchronously
					}
				});
			}
		}
		return op;
	}

	/**
	 * Convenience function for {@link #open(ArrayList, IProgressMonitor)}. Opens project using the file name. Update
	 * UI. Then loadProject()
	 */
	@Override
	public OpenedProject open(String fileName, IProgressMonitor monitor) {
		ArrayList<String> fileNames = new ArrayList<>();
		fileNames.add(fileName);
		return open(fileNames, monitor);
	}

	public List<ProjectItem> openLibrary(Project project, List<File> fileName, ValidationFindings findings)
			throws LibraryLoaderException, RepositoryException {
		List<ProjectItem> newItems = null;
		// LOGGER.debug("Adding to project: " + project.getName() + " library files: " + fileName);
		newItems = projectManager.addUnmanagedProjectItems(fileName, project, findings);
		return newItems;
	}

	/**
	 * Open the TL Project using the file name. If in the GUI thread will show busy.
	 * 
	 * @param fileName
	 *            project file to open
	 * @return new OpenedProject containing the opened TL Project and error and success messages and findings.
	 */
	@Override
	public OpenedProject openTLProject(String fileName) {
		// LOGGER.debug("Opening Project. Filename = " + fileName);
		String resultMsg = "";
		Project project = null;
		File projectFile = new File(fileName);
		Throwable ex = null;
		ValidationFindings findings = new ValidationFindings();

		// LoadProject will attempt to save it as well
		if (projectFile.canWrite()) {
			boolean isUI = Display.getCurrent() != null;
			if (isUI)
				mc.showBusy(true);
			try {
				project = projectManager.loadProject(projectFile, findings);
			} catch (RepositoryException e) {
				resultMsg = postLoadError(e, fileName);
				project = null;
				ex = e;
			} catch (LibraryLoaderException e) {
				resultMsg = postLoadError(e, fileName);
				project = null;
				ex = e;
			} catch (IllegalArgumentException e) {
				// This is where project ID already in use error is handled
				resultMsg = postLoadError(e, fileName);
				project = null;
				ex = e;
			} catch (NullPointerException e) {
				resultMsg = postLoadError(e, fileName);
				project = null;
				ex = e;
				// e.printStackTrace();
				// LOGGER.debug("NPE from project manager load project. " + e);
			} catch (Throwable e) {
				resultMsg = postLoadError(e, fileName);
				project = null;
				ex = e;
			}

			if (isUI)
				mc.showBusy(false);

			if (project != null) {
				if (project.getProjectItems().isEmpty()) {
					resultMsg += "Could not load project. Project could not read libraries.";
					if (findings != null && !findings.isEmpty())
						for (ValidationFinding finding : findings.getAllFindingsAsList()) {
							LOGGER.debug(finding.getFormattedMessage(FindingMessageFormat.DEFAULT));
							resultMsg += "\n" + finding.getFormattedMessage(FindingMessageFormat.DEFAULT);
						}
					if (Display.getCurrent() != null)
						DialogUserNotifier.openError("Open Project Error", resultMsg, ex);
				} else
					resultMsg = project.getProjectItems().size() + " project items read.";
				// LOGGER.debug("Read " + project.getProjectItems().size() + " items from project: " + fileName);
			}
		} else {
			resultMsg = postLoadError("Project files must be writable.", null, fileName);
			project = null;
		}

		OpenedProject op = new OpenedProject();
		op.resultMsg = resultMsg;
		op.tlProject = project;
		op.findings = findings;
		op.exception = ex;
		return op;
	}

	private String postLoadError(String msg, Throwable e, String fileName) {
		String title = "Project Error";
		String message = MessageFormat.format(Messages.getString("error.openProject.invalidRemoteProject"), msg,
				fileName);

		if (Display.getCurrent() != null) {
			mc.showBusy(false);
			DialogUserNotifier.openError(title, message, e);
		}
		LOGGER.error(title + " : " + message);
		return message;
	}

	private String postLoadError(Throwable e, String fileName) {
		return postLoadError(e.getLocalizedMessage(), e, fileName);
	}

	/**
	 * Create a list of all project files then close all projects.
	 * 
	 * Then, in a background job, refresh the TL Projects then reopen the projects.
	 */
	@Override
	public void refreshMaster() {
		ArrayList<String> projectFiles = new ArrayList<>();
		LOGGER.debug("Master Refresh Starting.");

		// Clear validation view
		final ValidationResultsView vView = OtmRegistry.getValidationResultsView();
		if (vView != null)
			vView.setFindings(null, Node.getModelNode());

		// Close all project nodes
		for (ProjectNode p : ModelNode.getAllProjects()) {
			if (p == getBuiltInProject() || p == getDefaultProject())
				continue;
			if (p.getTLProject().getProjectFile() != null)
				projectFiles.add(p.getTLProject().getProjectFile().getAbsolutePath());
			if (Display.getCurrent() == null)
				close(p); // Not in UI thread (debugging)
		}
		mc.refresh(null);

		/**
		 * Open a list of project files with a progress monitor
		 */
		if (Display.getCurrent() == null) {
			// not in UI Thread
			for (String fn : projectFiles)
				open(fn, null);
			return;
		}

		final int jobcount = projectFiles.size() * 2 + 1;
		final ArrayList<String> projects = new ArrayList<>(projectFiles);
		mc.postStatus("Opening Projects");

		// run in a background job
		Job job = new Job("Refreshing Projects") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Refreshing Projects ", jobcount);

				// Close all projects
				monitor.subTask("Closing Projects ");
				ValidationManager.block(); // MUST RELEASE

				for (ProjectNode p : ModelNode.getAllProjects()) {
					if (p == getBuiltInProject() || p == getDefaultProject())
						continue;
					if (!close(p)) {
						DialogUserNotifier.syncErrorWithUi("Error - could not close projects. Please restart.", null);
						monitor.done();
						ValidationManager.unblock(); // RELEASE
						return Status.CANCEL_STATUS;
					}
					ValidationManager.unblock(); // RELEASE
				}
				// Refresh the navigator view in UI thread
				DialogUserNotifier.syncWithUi("Projects closed.");
				monitor.worked(1);

				monitor.subTask("Refreshing Projects ");
				try {
					monitor.subTask("Refresh all managed libraries from repository.");
					projectManager.refreshManagedProjectItems();
				} catch (LibraryLoaderException | RepositoryException e) {
					monitor.done();
					DialogUserNotifier.syncWithUi("Error refreshing from repository.");
					return Status.CANCEL_STATUS;
				}
				monitor.worked(1);
				open(projects, monitor);

				monitor.subTask("Resolving types.");
				new TypeResolver().resolveTypes();

				monitor.done();
				DialogUserNotifier.syncWithUi("Projects Refreshed");
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	// @Override
	// public void remove(LibraryInterface library, ProjectNode pn) {
	// // The LibraryNavNode for this library may point to ANY project, don't use it
	// // Search the project for the nav node for this library
	// LibraryNavNode lnn = null;
	// for (Node n : pn.getChildren())
	// if (n instanceof LibraryNavNode)
	// if (((LibraryNavNode) n).contains(library)) {
	// lnn = (LibraryNavNode) n;
	// break;
	// }
	// remove(lnn);
	// }

	@Override
	public void remove(LibraryNavNode libraryNav) {
		remove(libraryNav, true);
	}

	/**
	 * Remove the library from the project indicated by the libraryNavNode. The library or chain is removed from the
	 * ProjectNode and TL Project.
	 * <p>
	 * If the library or chain is also in another project, the library's parent is set to the LNN that relates to that
	 * project.
	 * 
	 * @param lnn
	 *            libraryNavNode that relates the library or chain to a project
	 * @param refreshAndSave
	 *            if true, when complete refresh the navigator view and save project
	 * @return the impacted project
	 */
	public ProjectNode remove(LibraryNavNode lnn, boolean refreshAndSave) {
		ProjectNode pn = lnn.getProject();
		for (LibraryNode ln : lnn.getLibraries()) {
			try {
				removeTL(ln.getTLModelObject(), ln.getProjectItem(), pn.getTLProject());
			} catch (IllegalStateException e) {
				LOGGER.debug("Error removing " + ln + " from project " + pn + " : " + e.getLocalizedMessage());
				e.printStackTrace();
				DialogUserNotifier.openWarning("Warning", "There was an error closing " + ln + " in project " + pn);
			}
		}
		lnn.close();

		if (refreshAndSave) {
			OtmRegistry.getNavigatorView().refresh(pn, true);
			save(pn);
		}

		assert (!pn.getChildren().contains(lnn));
		return pn;
	}

	/**
	 * Remove each library from Project node and tlProject. Close each library and save impacted projects.
	 */
	@Override
	public void remove(List<LibraryNavNode> list) {
		Set<ProjectNode> impactedProjects = new HashSet<>();
		if (list.isEmpty())
			return;

		for (LibraryNavNode lnn : list)
			impactedProjects.add(remove(lnn, false));

		for (ProjectNode imp : impactedProjects) {
			OtmRegistry.getNavigatorView().refresh(imp, true);
			save(imp);
			// LOGGER.debug("Saved project: " + imp + " impacted by closing libraries.");
		}
	}

	private void removeTL(AbstractLibrary tlLibrary, ProjectItem tlPI, Project tlProject) throws IllegalStateException {
		if (tlLibrary.getOwningModel() == null)
			LOGGER.warn("Library " + tlLibrary.getName() + " has null owning model.");
		if (tlPI.getContent() != tlLibrary)
			throw new IllegalStateException("Project Item content is not same as passed library.");
		if (!tlProject.getProjectItems().contains(tlPI))
			throw new IllegalStateException("Project does not contain project item.");

		//
		tlProject.remove(tlLibrary);

		// If for any reason the remove didn't work, try removing the PI directly.
		if (tlProject.getProjectItems().contains(tlPI)) {
			LOGGER.error("Removed " + tlLibrary.getName() + " from project " + tlProject.getName()
					+ " but it was still contained in project. Removing project item.");
			tlProject.remove(tlPI); // Redundant but might recover
			// throw new IllegalStateException("Project still contains project item that was removed.");
		}
		// Check removal
		if (tlProject.getProjectItems().contains(tlPI))
			throw new IllegalStateException("Project still contains project item that was removed.");

		// LOGGER.debug("Removed " + tlLibrary.getName() + " from project " + tlProject.getName());
	}

	@Override
	public void save() {
		save(getDefaultProject());
	}

	@Override
	public boolean save(ProjectNode pn) {
		// Pre-checks
		if (pn == null || pn.getTLProject() == null || pn.getTLProject().getProjectManager() == null) {
			LOGGER.error("Could not save project because of missing project manager.");
			DialogUserNotifier.openError("Save Project Error", "Could not get project manager.", null);
			return false;
		}
		// Assure project has writable project file
		if (pn.getTLProject().getProjectFile() == null) {
			LOGGER.error("Could not get project file.");
			DialogUserNotifier.openError("Save Project Error", "Could not get project file.\n", null);
			return false;
		}
		if (!pn.getTLProject().getProjectFile().canWrite()) {
			LOGGER.error("Could not write to project file: ", pn.getTLProject().getProjectFile().getPath());
			DialogUserNotifier.openError("Save Project Error",
					"Could not write to project file. \n" + pn.getTLProject().getProjectFile().getPath(), null);
			return false;
		}

		mc.showBusy(true);
		try {
			pn.getTLProject().getProjectManager().saveProject(pn.getTLProject());
		} catch (LibrarySaveException e) {
			LOGGER.error("Could not save project: " + e.getLocalizedMessage());
			mc.showBusy(false);
			DialogUserNotifier.openError("Save Project Error", "Could not save project. \n" + e.getLocalizedMessage(),
					e);
			return false;
		}
		mc.showBusy(false);

		return true;
	}

	private void saveFavorites(XMLMemento memento) {
		Iterator<IProjectToken> iter = openProjects.iterator();
		while (iter.hasNext()) {
			IProjectToken item = iter.next();
			IMemento child = memento.createChild(OTM_PROJECT);
			child.putString(OTM_PROJECT_NAME, item.getName());
			child.putString(OTM_PROJECT_LOCATION, item.getLocation());
		}
	}

	/**
	 * Save the currently open project state using the Eclipse utilities. Saved to:
	 * <workspace>/.metadata/.plugins/org.opentravel...
	 */
	@Override
	public void saveState() {
		List<ProjectNode> projects = getAll();
		// if (projects.isEmpty()) return;
		for (ProjectNode p : projects) {
			// Filter out built-in and default project?
			// Make into Items
			if (p.getTLProject().getProjectFile() != null) {
				IProjectToken item = new ProjectToken(p.getTLProject().getProjectFile());
				openProjects.add(item);
			}
		}
		// LOGGER.debug("Made project list into project tokens.");

		XMLMemento memento = XMLMemento.createWriteRoot(OTM_PROJECTS);
		saveFavorites(memento);
		FileWriter writer = null;
		try {
			writer = new FileWriter(getOTM_StateFile());
			memento.save(writer);
		} catch (IOException e) {
			LOGGER.error("IO Error saving state.");
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				LOGGER.error("IO Error closing state file.");
			}
		}
		// try {
		// LOGGER.debug("Saved project state to file: " + getOTM_StateFile().getCanonicalPath());
		// } catch (IOException e) {
		// }
	}

	/**
	 * @param loadingErrors
	 */
	private void showFindings(final ValidationFindings loadingErrors) {
		if (!OtmRegistry.getMainWindow().hasDisplay()) {
			LOGGER.debug("Showing " + loadingErrors.count() + " findings.");
			for (String msg : loadingErrors.getAllValidationMessages(FindingMessageFormat.MESSAGE_ONLY_FORMAT))
				LOGGER.debug("   " + msg);
		} else {
			// do not block UI
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					FindingsDialog.open(OtmRegistry.getActiveShell(), Messages.getString("dialog.findings.title"),
							Messages.getString("dialog.findings.message"), loadingErrors.getAllFindingsAsList());

				}
			});
		}
	}

	/**
	 * If you don't have a default project, make one.
	 */
	public void testAndSetDefaultProject() {
		defaultNS = OtmRegistry.getMainController().getRepositoryController().getLocalRepository().getNamespace();
		for (ProjectNode pn : getAll()) {
			if (pn.getTLProject().getProjectId().equals(defaultNS)) {
				defaultProject = pn;
				break;
			}
		}
		if (defaultProject == null) {
			createDefaultProject();
		}
	}
}
