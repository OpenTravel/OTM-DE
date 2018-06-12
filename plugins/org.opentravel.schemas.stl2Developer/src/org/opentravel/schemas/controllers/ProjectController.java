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
package org.opentravel.schemas.controllers;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemas.controllers.DefaultProjectController.OpenedProject;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Interface for all the project related activity. Note that the global model is controlled by {@link ModelController}
 * 
 * @author Dave Hollander
 * 
 */
public interface ProjectController {

	/**
	 * @param lcn
	 * @param tlLib
	 * @return
	 */
	public LibraryNode add(LibraryChainNode lcn, AbstractLibrary tlLib);

	/**
	 * Add a TL Library to the project and create a LibraryNode to represent it.
	 * 
	 * @param pn
	 * @param tlLib
	 */
	public LibraryNavNode add(ProjectNode pn, AbstractLibrary tlLib);

	/**
	 * Add a library from a repository to the project.
	 * 
	 * @param project
	 * @param ri
	 */
	public ProjectItem add(ProjectNode projectNode, RepositoryItem repositoryItem);

	/**
	 * Get a list of project items from the compiler repository manager. Project items contain AbstractLibraries.
	 * 
	 * Does NOT add them to the Node project model.
	 * 
	 * @see {@link ProjectNode#addToTL(FileList)}
	 */
	public List<ProjectItem> addLibrariesToTLProject(Project project, List<File> libraryFiles);

	/**
	 * Save and close the project. If it is the default project all children are closed without saving.
	 * 
	 * @return
	 * 
	 * @see {@link org.opentravel.schemas.commands.CloseProjectHandler#execute(ExecutionEvent)}
	 */
	boolean close(ProjectNode project);

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

	public String getDefaultUnmanagedNS();

	/**
	 * @return namespace of the project
	 */
	String getNamespace();

	/**
	 * Governed namespaces are namespaces that are protected via the repositories. Open governed namespaces are assigned
	 * to open projects.
	 * 
	 * @return list of governed namespaces in currently open projects.
	 */
	public List<String> getOpenGovernedNamespaces();

	/**
	 * Projects govern a specific namespace. This method returns namespaces that are eligible to be governed.
	 */
	public List<String> getSuggestedNamespaces();

	/**
	 * Create a new project with TL model using the Wizard to have user enter projectFile, ID which defines the governed
	 * namespace, name and description.
	 */
	public ProjectNode newProject();

	/**
	 * @param defaultName
	 * @param selectedRoot
	 * @param selectedExt
	 */
	public void newProject(String defaultName, String selectedRoot, String selectedExt);

	/**
	 * Opens already existing project(s) using a selection dialog and adds it to the model. Opens all the libraries in
	 * the project.
	 */
	void open();

	public OpenedProject open(String fileName, IProgressMonitor monitor);

	public OpenedProject openTLProject(String projectFile);

	public void refreshMaster();

	// /**
	// * Remove the passed library from the passed project.
	// */
	// public void remove(LibraryInterface library, ProjectNode pn);

	/**
	 * Remove the associated library from the associated project both in the TL and GUI models.
	 */
	public void remove(LibraryNavNode libraryNavNode);

	/**
	 * Remove the associated library from the associated project both in the TL and GUI models.
	 */
	public void remove(List<LibraryNavNode> libNavlist);

	/**
	 * Save the project.
	 */
	void save();

	/**
	 * Saves the given project to the physical file / repository
	 * 
	 * @param project
	 *            {@link ProjectNode} to be saved
	 */
	public boolean save(ProjectNode project);

	public void saveState();

}
