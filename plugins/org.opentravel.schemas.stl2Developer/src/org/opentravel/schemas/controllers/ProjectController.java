
package org.opentravel.schemas.controllers;

import java.io.File;
import java.util.List;

import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ProjectNode;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Interface for all the project related activity. Note that the global model is controlled by
 * {@link ModelController}
 * 
 * @author Dave Hollander
 * 
 */
public interface ProjectController {

    public List<ProjectItem> addLibrariesToTLProject(Project project, List<File> libraryFiles);

    /**
     * Add a library from a repository to the project.
     * 
     * @param project
     * @param ri
     */
    public ProjectItem add(ProjectNode projectNode, RepositoryItem repositoryItem);

    /**
     * Add a TL Library to the project and create a LibraryNode to represent it.
     * 
     * @param pn
     * @param tlLib
     */
    public LibraryNode add(ProjectNode pn, AbstractLibrary tlLib);

    /**
     * Saves, closes and removes a project from the model
     * 
     * @param project
     *            {@link ProjectNode} to be closed
     */
    void close(ProjectNode project);

    /**
     * Saves, closes and removes all projects from the model
     * 
     */
    void closeAll();

    /**
     * Creates new project complete with TL model
     */
    ProjectNode create(File file, String ID, String name, String description);

    /**
     * @return new list of open projects. Returns empty list if there are no projects.
     */
    List<ProjectNode> getAll();

    /**
     * @return the builtInProject
     */
    public ProjectNode getBuiltInProject();

    ProjectNode getDefaultProject();

    /**
     * @return namespace of the project
     */
    String getNamespace();

    /**
     * Opens already existing project(s) using a selection dialog and adds it to the model. Opens
     * all the libraries in the project.
     */
    void open();

    /**
     * Create a new project with TL model using the Wizard to have user enter projectFile, ID which
     * defines the governed namespace, name and description.
     */
    public ProjectNode newProject();

    /**
     * @param defaultName
     * @param selectedRoot
     * @param selectedExt
     */
    public void newProject(String defaultName, String selectedRoot, String selectedExt);

    /**
     * Open a directory. Let the user select a directory. Create a project for that directory. Load
     * all OTM files into the new project.
     */
    void openDir();

    // public void publish(LibraryNode node);

    /**
     * Save the project.
     */
    void save();

    /**
     * Saves the given projects to the physical files / repository
     * 
     * @param projects
     *            is list of {@link ProjectNode}s to be saved
     */
    void save(List<ProjectNode> projects);

    /**
     * Saves the given project to the physical file / repository
     * 
     * @param project
     *            {@link ProjectNode} to be saved
     */
    void save(ProjectNode project);

    /**
     * Saves all projects
     * 
     */
    void saveAll();

    public void saveState();

    /**
     * Projects govern a specific namespace. This method returns namespaces that are eligible to be
     * governed.
     */
    public List<String> getSuggestedNamespaces();

    /**
     * Governed namespaces are namespaces that are protected via the repositories. Open governed
     * namespaces are assigned to open projects.
     * 
     * @return list of governed namespaces in currently open projects.
     */
    public List<String> getOpenGovernedNamespaces();

    public String getDefaultUnmanagedNS();

    LibraryNode add(LibraryNode ln, AbstractLibrary tlLib);

    public ProjectNode openProject(String projectFile);

}
