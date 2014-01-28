/**
 * 
 */
package org.opentravel.schemas.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.preferences.DefaultPreferences;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.Activator;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.FindingsDialog;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.wizards.NewProjectValidator;
import org.opentravel.schemas.wizards.NewProjectWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.loader.LibraryLoaderException;
import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.repository.Project;
import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.ProjectManager;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryManager;
import com.sabre.schemacompiler.repository.RepositoryNamespaceUtils;
import com.sabre.schemacompiler.repository.impl.BuiltInProject;
import com.sabre.schemacompiler.repository.impl.RemoteRepositoryClient;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Default Project Controller.
 * 
 * Reads/Saves open projects in session store. Creates built in project for built in libraries.
 * Creates and manages access to default project if not restored from session.
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultProjectController implements ProjectController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectController.class);

    public static final String PROJECT_EXT = "otp";
    private final MainController mc;
    private final ProjectManager projectManager;
    protected ProjectNode defaultProject = null;
    protected ProjectNode builtInProject;

    private String defaultNS = "http://www.opentravel.org/OTM2/DefaultProject";
    private String defaultPath;

    public String getDefaultPath() {
        return defaultPath;
    }

    // override with ns of local repository
    private final String dialogText = "Select a project file";

    public DefaultProjectController(final MainController mc, ProjectManager projectManager) {
        this.mc = mc;
        this.projectManager = projectManager;

        // Find the Built-in project
        for (Project p : projectManager.getAllProjects()) {
            if (p.getProjectId().equals(BuiltInProject.BUILTIN_PROJECT_ID)) {
                builtInProject = loadProject(p);
            }
        }

        // Open up any projects that were open last session.
        loadSavedState();

        // FIXME - what should be the key for finding the default project?
        defaultNS = OtmRegistry.getMainController().getRepositoryController().getLocalRepository()
                .getNamespace();
        for (ProjectNode pn : getAll()) {
            if (pn.getProject().getProjectId().equals(defaultNS)) {
                defaultProject = pn;
                break;
            }
        }
        if (defaultProject == null)
            createDefaultProject();

        LOGGER.debug("Project Controller Initialized");
    }

    public DefaultProjectController(final MainController mc, RepositoryManager repositoryManager) {
        this(mc, new ProjectManager(mc.getModelController().getTLModel(), true, repositoryManager));
    }

    protected void createDefaultProject() {
        defaultPath = DefaultPreferences.getDefaultProjectPath();
        defaultProject = create(new File(defaultPath), defaultNS, "Default Project",
                "Used for libraries not loaded into a project.");

        if (defaultProject == null) {
            DialogUserNotifier.openError("Default Project Error",
                    Messages.getString("error.openProject.defaultProject", defaultPath));
        }
    }

    @Override
    public ProjectNode openProject(String fileName) {
        Project project = openProject(fileName, new ValidationFindings());
        return loadProject(project);
    }

    protected void fixElementNames(LibraryNode ln) {
        int fixNeeded = 0;
        if (!ln.isEditable())
            return;

        for (Node n : ln.getDescendants_TypeUsers()) {
            if (n instanceof ElementNode)
                if (!(n.getName().equals(NodeNameUtils.fixElementName(n))))
                    fixNeeded++;
        }
        if (fixNeeded > 0) {
            if (DialogUserNotifier
                    .openConfirm(
                            "Name Rules",
                            fixNeeded
                                    + " errors in element naming were detected. Should these be fixed automatically?"))
                for (Node n : ln.getDescendants_TypeUsers()) {
                    if (n instanceof ElementNode)
                        if (!(n.getName().equals(NodeNameUtils.fixElementName(n))))
                            ((ElementNode) n).setName("");
                }
        }
    }

    public List<ProjectItem> openLibrary(Project project, List<File> fileName,
            ValidationFindings findings) throws LibraryLoaderException, RepositoryException {
        List<ProjectItem> newItems = null;
        LOGGER.debug("Adding to project: " + project.getName() + " library files: " + fileName);
        newItems = projectManager.addUnmanagedProjectItems(fileName, project, findings);
        return newItems;
    }

    /**
     * Does NOT add them to the Node project model. Use {projectNode}.add(FileList)
     */
    @Override
    public List<ProjectItem> addLibrariesToTLProject(Project project, List<File> libraryFiles) {
        ValidationFindings findings = new ValidationFindings();
        List<ProjectItem> newItems = Collections.emptyList();
        try {
            newItems = openLibrary(project, libraryFiles, findings);
            ValidationFindings loadingFindings = getLoadingFindings(findings);
            if (!loadingFindings.isEmpty()) {
                showFindings(loadingFindings);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not add library file to project.");
            DialogUserNotifier.openError("Project Error", "Could not add to project.");
        } catch (LibraryLoaderException e) {
            LOGGER.error("Could not add library file to project.");
            DialogUserNotifier.openError("Project Error", "Could not add to project.");
        } catch (Throwable e) {
            LOGGER.error("Error when adding to project", e);
            DialogUserNotifier.openError("Project Error", "Could not add to project.");
        }
        LOGGER.debug("Added libraries to " + project.getName());
        return newItems;
    }

    /**
     * @param loadingErros
     */
    private void showFindings(final ValidationFindings loadingErros) {
        // do not block UI
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                FindingsDialog.open(OtmRegistry.getActiveShell(),
                        Messages.getString("dialog.findings.title"),
                        Messages.getString("dialog.findings.message"),
                        loadingErros.getAllFindingsAsList());

            }
        });
    }

    @Override
    public LibraryNode add(ProjectNode pn, AbstractLibrary tlLib) {
        if (pn == null || tlLib == null)
            throw new IllegalArgumentException("Null argument.");

        ProjectItem pi = null;
        LibraryNode ln = null;
        try {
            pi = pn.getProject().getProjectManager()
                    .addUnmanagedProjectItem(tlLib, pn.getProject());
            if (pi != null) {
                ln = new LibraryNode(pi, pn);
                fixElementNames(ln);
                mc.refresh(pn);
                LOGGER.debug("Added library " + ln.getName() + " to " + pn);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", e.getLocalizedMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Could not add repository item to project. " + ex.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", ex.getLocalizedMessage());
        }
        return ln;
    }

    @Override
    public LibraryNode add(LibraryNode ln, AbstractLibrary tlLib) {
        ProjectNode pn = ln.getProject();
        if (pn == null || tlLib == null)
            throw new IllegalArgumentException("Null argument.");

        ProjectItem pi = null;
        try {
            pi = pn.getProject().getProjectManager()
                    .addUnmanagedProjectItem(tlLib, pn.getProject());
        } catch (RepositoryException e) {
            LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", e.getLocalizedMessage());
        }
        if (pi != null)
            ln = new LibraryNode(pi, ln.getChain());
        mc.refresh(pn);
        LOGGER.debug("Added library " + ln.getName() + " to " + pn);
        return ln;
    }

    /**
     * Add the modeled library to the project.
     * 
     * @param pn
     * @param ln
     */
    public ProjectItem add(ProjectNode pn, LibraryNode ln) {
        if (pn == null || ln == null)
            return null;

        ProjectItem pi = null;
        try {
            pi = pn.getProject().getProjectManager()
                    .addUnmanagedProjectItem(ln.getTLaLib(), pn.getProject());
        } catch (RepositoryException e) {
            LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", e.getLocalizedMessage());
        }
        ln.setProjectItem(pi);
        return pi;
    }

    @Override
    public ProjectItem add(ProjectNode project, RepositoryItem ri) {
        if (project == null)
            return null;

        ProjectItem pi = null;
        List<ProjectItem> piList = null;
        ValidationFindings findings = new ValidationFindings();
        try {
            // workaround to make sure the local version of library will be up-to-date with remote
            // repository
            RefreshPolicy refreshPolicy = null;
            if (ri.getRepository() instanceof RemoteRepositoryClient) {
                RemoteRepositoryClient client = (RemoteRepositoryClient) ri.getRepository();
                refreshPolicy = client.getRefreshPolicy();
                client.setRefreshPolicy(RefreshPolicy.ALWAYS);
            }
            piList = project
                    .getProject()
                    .getProjectManager()
                    .addManagedProjectItems(Arrays.asList(new RepositoryItem[] { ri }),
                            project.getProject(), findings);

            // restore refresh policy
            if (ri.getRepository() instanceof RemoteRepositoryClient) {
                RemoteRepositoryClient client = (RemoteRepositoryClient) ri.getRepository();
                client.setRefreshPolicy(refreshPolicy);
            }
            if (findings.hasFinding()) {
                if (PlatformUI.isWorkbenchRunning())
                    FindingsDialog.open(OtmRegistry.getActiveShell(),
                            Messages.getString("dialog.findings.title"),
                            Messages.getString("dialog.findings.message"),
                            findings.getAllFindingsAsList());
            }
            if (!piList.isEmpty())
                pi = piList.get(0);
        } catch (LibraryLoaderException e) {
            LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", e.getLocalizedMessage());

        } catch (RepositoryException e) {
            LOGGER.error("Could not add repository item to project. " + e.getLocalizedMessage());
            DialogUserNotifier.openError("Project Error", e.getLocalizedMessage());
        }

        // TEST - make sure any member of the chain is not already opened.
        // piList now has all of the project items for this chain.
        List<LibraryChainNode> chains = new ArrayList<LibraryChainNode>();
        if (!piList.isEmpty()) {
            for (ProjectItem item : piList) {
                if (pi == null)
                    pi = item; // return first one
                if (item != null) {
                    LibraryChainNode lcn = project.getChain(item);
                    // FIXME - getChain is always returning null causing each chain member to create
                    // a new chain.
                    if (lcn == null) {
                        lcn = new LibraryChainNode(item, project);
                        chains.add(lcn);
                    } else {
                        lcn.add(item);
                    }
                }

                try {
                    project.getProject().getProjectManager().saveProject(project.getProject());
                } catch (LibrarySaveException e) {
                    e.printStackTrace();
                    DialogUserNotifier
                            .openError("Could not save project.", e.getLocalizedMessage());
                }
            }
            TypeResolver tr = new TypeResolver();
            tr.resolveTypes(); // do the whole model to check if new libraries resolved broken
                               // links.
            // for (LibraryChainNode chain : chains) {
            // tr.resolveTypes(chain.getLibraries());
            // }
            mc.refresh(project);
        } else
            LOGGER.warn("Repository item " + ri.getLibraryName() + " not added to project.");

        // TODO - sync the repository view
        LOGGER.debug("Added repository item " + ri.getLibraryName() + " to " + project);
        return pi;
    }

    @Override
    public ProjectNode create(File file, String ID, String name, String description) {
        Project newProject;
        try {
            newProject = projectManager.newProject(file, ID, name, description);
        } catch (Exception e) {
            LOGGER.error("Could not create new project: ", e);
            DialogUserNotifier.openError("New Project Error", e.getLocalizedMessage());
            return null;
        }
        return new ProjectNode(newProject);
    }

    @Override
    public List<ProjectNode> getAll() {
        List<ProjectNode> projects = new ArrayList<ProjectNode>();
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
        return builtInProject;
    }

    /**
     * @return the defaultProject
     */
    @Override
    public ProjectNode getDefaultProject() {
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
     * @param defaultProject
     *            the defaultProject to set
     */
    public void setDefaultProject(ProjectNode defaultProject) {
        this.defaultProject = defaultProject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ProjectController#getNamespace()
     */
    @Override
    public String getNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    // public List<String> getRepositories() {
    // ArrayList<String> repos = new ArrayList<String>();
    // for (RemoteRepository repo : repositoryManager.listRemoteRepositories())
    // repos.add(repo.getId());
    // return repos;
    // }

    // July 1, 2013 - dmh - removed from all menus.
    // TODO - remove
    @Override
    public void openDir() {
        final String dd = FileDialogs.postDirDialog(Messages
                .getString("fileDialog.directory.projectPath"));
        if (dd == null)
            return; // dialog was cancelled.

        Path dir = new Path(dd);
        File directory = new File(dir.toString());
        String projectName = directory.getName();
        LOGGER.debug("Opening project: " + dir.toString());

        // Create a project
        Date date = new Date(System.currentTimeMillis());
        Project newTL_Project = null;
        try {
            newTL_Project = projectManager.newProject(new File(dir.toString() + File.separator
                    + projectName + ".otp"), "http://www.sabre.com/OTM/Project/" + projectName,
                    projectName,
                    "Project " + projectName + " created from directory " + directory.getPath()
                            + File.separator + "*.otm" + " on " + date.toString());
        } catch (Exception e) {
            LOGGER.error("Could not create project.");
            DialogUserNotifier.openError("Project Error", "Could not create project");
            return;
        }
        LOGGER.debug("Created TL Project: " + dir.toString());

        // Get all the files and add them to the project.
        List<File> paths = new ArrayList<File>();
        for (String fd : directory.list(null)) {
            if (fd.endsWith(".otm"))
                paths.add(new File(directory.getPath() + File.separator + fd));
        }
        addLibrariesToTLProject(newTL_Project, paths);

        // Set the project ID to the base namespace
        String baseNS = "";
        for (ProjectItem pi : newTL_Project.getProjectItems()) {
            baseNS = pi.getBaseNamespace();
            LOGGER.debug("Base namespace :" + baseNS);
            if (!baseNS.isEmpty())
                break;
        }
        newTL_Project.setProjectId(baseNS);

        // Add the project to the GUI node model
        ProjectNode pn = loadProject(newTL_Project);
        save(pn);
        mc.refresh();
    }

    @Override
    public void open() {
        String[] extensions = { "*." + PROJECT_EXT };
        String fn = FileDialogs.postFileDialog(extensions, dialogText);
        if (fn == null)
            return;
        open(fn);
    }

    public Project openProject(String fileName, ValidationFindings findings) {
        LOGGER.debug("Opening Project. Filename = " + fileName);
        File projectFile = new File(fileName);
        Project project = null;
        try {
            project = projectManager.loadProject(projectFile, findings);
        } catch (RepositoryException e) {
            DialogUserNotifier.openError(
                    "Project Error",
                    MessageFormat.format(
                            Messages.getString("error.openProject.invalidRemoteProject"),
                            e.getMessage(), projectFile.toString()));
        } catch (LibraryLoaderException e) {
            // happens when default project is created.
            DialogUserNotifier.openError("Project Error", "Could not load project libraries.");
        } catch (IllegalArgumentException e) {
            DialogUserNotifier.openError("Error opening project. ", e.getMessage());
        } catch (Throwable e) {
            DialogUserNotifier.openError("Error opening project. ", e.getMessage());
        }
        return project;
    }

    public ProjectNode open(String fileName) {
        if (fileName == null || fileName.isEmpty())
            LOGGER.error("Tried to open null or empty file.");
        LOGGER.debug("Opening project from file: " + fileName);
        mc.showBusy(true);
        mc.postStatus("Opening Project from file: " + fileName);
        ValidationFindings findings = new ValidationFindings();

        Project project = openProject(fileName, findings);

        ValidationFindings loadingFindings = getLoadingFindings(findings);
        if (!loadingFindings.isEmpty()) {
            showFindings(loadingFindings);
            for (String finding : loadingFindings
                    .getAllValidationMessages(FindingMessageFormat.MESSAGE_ONLY_FORMAT))
                LOGGER.debug("Finding: " + finding);
        }

        ProjectNode pn = loadProject(project); // Create gui model for the project

        final ValidationResultsView view = OtmRegistry.getValidationResultsView();
        if (view != null) {
            view.setFindings(findings, pn);
        }
        mc.selectNavigatorNodeAndRefresh(pn);
        mc.refresh();
        mc.showBusy(false);
        return pn;
    }

    /**
     * @param findings
     * @return
     */
    private ValidationFindings getLoadingFindings(ValidationFindings findings) {
        return LibraryModelLoader.filterLoaderFindings(findings);
    }

    /**
     * Load a TL project into the GUI model. Creates Project Node which adds all the libraries in
     * the project.
     * 
     * @param project
     */
    public ProjectNode loadProject(Project project) {
        if (project == null)
            return null;
        mc.showBusy(true);
        mc.postStatus("Loading Project: " + project.getName());
        mc.refresh();
        ProjectNode pn = new ProjectNode(project);
        for (LibraryNode ln : pn.getLibraries())
            fixElementNames(ln);
        mc.selectNavigatorNodeAndRefresh(pn);
        mc.postStatus("Loaded Project: " + pn);
        mc.showBusy(false);
        return pn;
    }

    @Override
    public void close(ProjectNode pn) {
        if (pn.isBuiltIn()) {
            return;
        }

        save(pn);
        pn.getProject().getProjectManager().closeProject(pn.getProject());
        pn.close();
        // reload default project
        if (pn == getDefaultProject()) {
            ProjectNode newDefaultProject = openProject(getDefaultProject().getProject()
                    .getProjectFile().toString());
            defaultProject = newDefaultProject;
        }
        mc.refresh();
        LOGGER.debug("Closed project: " + pn);
    }

    @Override
    public void closeAll() {
        for (ProjectNode project : getAll()) {
            close(project);
        }
        // to re-initializes the contents of the model
        projectManager.closeAll();
    }

    @Override
    public void save() {
        save(getDefaultProject());
    }

    @Override
    public void save(ProjectNode pn) {
        if (pn == null)
            return;

        try {
            pn.getProject().getProjectManager().saveProject(pn.getProject());
            // save the project file only, not the libraries and ignore findings
            // pn.getProject().getProjectManager().saveProject(pn.getProject(), true, null);
        } catch (LibrarySaveException e) {
            e.printStackTrace();
            LOGGER.error("Could not save project");
            DialogUserNotifier.openError("Project Error", "Could not save project.");
        }
    }

    @Override
    public void save(List<ProjectNode> projects) {
        for (ProjectNode pn : projects)
            save(pn);
    }

    @Override
    public void saveAll() {
        ProjectManager pm = getDefaultProject().getProject().getProjectManager();
        for (Project p : pm.getAllProjects())
            try {
                pm.saveProject(p);
            } catch (LibrarySaveException e) {
                e.printStackTrace();
                LOGGER.error("Could not save project");
            }

    }

    @Override
    public ProjectNode newProject() {
        // Run the wizard
        final NewProjectWizard wizard = new NewProjectWizard();
        wizard.setValidator(new NewProjectValidator());
        wizard.run(OtmRegistry.getActiveShell());
        if (!wizard.wasCanceled()) {
            create(wizard.getFile(), wizard.getNamespace(), wizard.getName(),
                    wizard.getDescription());
            mc.refresh();
        }
        return null;
    }

    @Override
    public void newProject(String defaultName, String selectedRoot, String selectedExt) {
        // Run the wizard
        final NewProjectWizard wizard = new NewProjectWizard(defaultName, selectedRoot, selectedExt);
        wizard.setValidator(new NewProjectValidator());
        wizard.run(OtmRegistry.getActiveShell());
        if (!wizard.wasCanceled()) {
            create(wizard.getFile(), wizard.getNamespace(), wizard.getName(),
                    wizard.getDescription());
            mc.refresh();
        }
    }

    @Override
    public List<String> getSuggestedNamespaces() {
        List<String> allowedNSs = new ArrayList<String>();
        allowedNSs.add(getDefaultUnmanagedNS());
        allowedNSs.addAll(getOpenGovernedNamespaces());
        return allowedNSs;
    }

    @Override
    public List<String> getOpenGovernedNamespaces() {
        List<String> projects = new ArrayList<String>();
        for (Project p : projectManager.getAllProjects()) {
            if (p instanceof BuiltInProject) {
                continue;
            }
            projects.add(RepositoryNamespaceUtils.normalizeUri(p.getProjectId()));
        }
        return projects;
    }

    /**
     * ** Manage Saving/Retrieving State
     * 
     * TODO - refactor into its own class/file
     */

    private boolean loadSavedState() {
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return false;

        FileReader reader = null;
        try {
            reader = new FileReader(getOTM_StateFile());
            loadSavedState(XMLMemento.createReadRoot(reader));
        } catch (FileNotFoundException e) {
            // Ignored... no items exist yet.
            return false;
        } catch (Exception e) {
            // Log the exception and move on.
            LOGGER.error("LoadSavedState: " + getOTM_StateFile().toString() + " e= " + e);
            return false;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                LOGGER.error("LoadState: " + e);
                return false;
            }
        }
        return true;
    }

    public void loadSavedState(XMLMemento memento) {
        if (memento == null)
            LOGGER.error("Memento is null.");
        IMemento[] children = memento.getChildren(OTM_PROJECT);
        for (int i = 0; i < children.length; i++) {
            open(children[i].getString(OTM_PPOJECT_LOCATION));
        }
    }

    Collection<IProjectToken> openProjects = new ArrayList<DefaultProjectController.IProjectToken>();
    private static final String OTM_PROJECTS = "OTM_Projects";
    // private static final String TAG_FAVORITES = "Favorites";
    private static final String OTM_PROJECT = "Project";
    private static final String OTM_PROJECT_NAME = "Name";
    private static final String OTM_PPOJECT_LOCATION = "Location";

    /**
     * Save the currently open project state using the Eclipse utilities. Saved to:
     * <workspace>/.metadata/.plugins/com.sabre...
     */
    @Override
    public void saveState() {
        List<ProjectNode> projects = getAll();
        // if (projects.isEmpty()) return;
        for (ProjectNode p : projects) {
            // Filter out built-in and default project?
            // Make into Items
            if (p.getProject().getProjectFile() != null) {
                IProjectToken item = new ProjectToken(p.getProject().getProjectFile());
                openProjects.add(item);
            }
        }
        LOGGER.debug("Made project list into project tokens.");

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
        try {
            LOGGER.debug("Saved project state to file: " + getOTM_StateFile().getCanonicalPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    private File getOTM_StateFile() {
        return Activator.getDefault().getStateLocation().append("OTM_Developer.xml").toFile();
    }

    private void saveFavorites(XMLMemento memento) {
        Iterator<IProjectToken> iter = openProjects.iterator();
        while (iter.hasNext()) {
            IProjectToken item = iter.next();
            IMemento child = memento.createChild(OTM_PROJECT);
            child.putString(OTM_PROJECT_NAME, item.getName());
            child.putString(OTM_PPOJECT_LOCATION, item.getLocation());
        }
    }

    // TODO - eliminate this extra class - just load into the project
    public interface IProjectToken extends IAdaptable {
        String getName();

        void setName(String newName);

        String getLocation();

        // boolean isFavoriteFor(Object obj);
        // FavoriteItemType getType();
        // String getInfo();
        static IProjectToken[] NONE = new IProjectToken[] {};
    }

    public class ProjectToken implements IProjectToken {
        private String name;
        private String location;

        // private final String id;
        // private final int ordinal;

        public ProjectToken(File file) {
            try {
                location = file.getCanonicalPath();
            } catch (IOException e) {
                LOGGER.error("Could not create project token due to bad path.");
                // e.printStackTrace();
            }
        }

        @Override
        public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String newName) {
            name = newName;
        }

        @Override
        public String getLocation() {
            return location;
        }

    }

    // dead code. Moved to repository Controller.
    // public void versionLibrary(Node node) {
    // TODO - handle cases where there is no library chain .. start versioning
    // this lib
    // LOGGER.debug("Versioning Library " + node);
    // LibraryNode lib = null;
    // LibraryNode newLib = null;
    // ProjectNode project = null;

    // if (node instanceof LibraryChainNode) {
    // DEAD CODE - handler does a getLibrary();
    // LibraryChainNode lc = (LibraryChainNode) node;
    // lib = lc.getLibrary();
    // newLib = lc.incrementMinorVersion();
    // project = lib.getProject();
    // lc.getVersions().linkLibrary(newLib);
    // } else if (node instanceof LibraryNode) {
    // lib = (LibraryNode) node;
    // newLib = lib.createNewMinorVerison(lib.getProject());
    // project = lib.getProject();
    // lib.getParent().linkLibrary(newLib);
    // // FIXME - update containing chain head.
    // } else
    // LOGGER.error("Can't version Library " + node);
    //
    // if (newLib != null) {
    // // add(project, newLib);
    // if (lib.getProjectItem().getRepository() != null) {
    // try {
    // lib.getProjectItem().getProjectManager()
    // .publish(newLib.getProjectItem(), lib.getProjectItem().getRepository());
    // } catch (RepositoryException e) {
    // LOGGER.debug("Error publishing new version of library " + lib);
    // }
    // }
    // // TODO - is the project file updated correctly???
    // // NO. both the unmanaged and managed are added.
    // // TEST - Trying the add here instead of in front of publish
    // add(project, newLib);

    // }
    // FIXME - do not know what repo to sync.
    // mc.getRepositoryController().sync(null);
    // }

    // public void finalizeLibrary(LibraryNode lib) {
    // LOGGER.debug("Finalizing Library " + lib);
    //
    // try {
    // lib.getProjectItem().getProjectManager().promote(lib.getProjectItem());
    // } catch (RepositoryException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

}
