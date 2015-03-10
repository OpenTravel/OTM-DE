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
package org.opentravel.schemas.node;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ProjectNode extends Node implements INode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectNode.class);

	private final Project project; // underlying TL model object

	/**
	 * Create a project node from the TL project model. Read all libraries in the project and create library nodes and
	 * their children. Link into the model node.
	 * 
	 * @param tlProject
	 *            - tl project to be represented
	 */
	public ProjectNode(Project tlProject) {
		super(tlProject.getName());
		this.project = tlProject;
		setName(tlProject.getName());
		Node.getModelNode().addProject(this);
		load(tlProject);
	}

	/**
	 * Link the library as a child to the project node. Does NOT add to the underlying model. Does not resolve types.
	 * 
	 * @param lib
	 */
	@Override
	public void linkLibrary(LibraryNode lib) {
		if (lib != null && !getChildren().contains(lib))
			getChildren().add(lib);
	}

	/**
	 * Load the GUI model with all unmodeled libraries from the TL Project into this project. Resolves types after all
	 * the libraries are loaded.
	 */
	protected void load(final Project tlProject) {
		// LOGGER.debug("Load libraries from project: " + tlProject.getName());

		// Create library nodes under the project for each new project item
		ArrayList<LibraryNode> newLibs = new ArrayList<LibraryNode>();
		ArrayList<ProjectItem> existingItems = new ArrayList<ProjectItem>();
		Map<String, LibraryChainNode> chainMap = new HashMap<String, LibraryChainNode>();
		LibraryChainNode lcn = null;
		LibraryNode ln = null;

		// Record what libraries including those in chains already have been modeled.
		for (INode n : getChildren()) {
			if (n instanceof LibraryNode)
				existingItems.add(((LibraryNode) n).getProjectItem());
			else if (n instanceof LibraryChainNode) {
				lcn = (LibraryChainNode) n;
				chainMap.put(makeChainIdentity(lcn.getHead().getProjectItem()), lcn);
				for (Node lib : lcn.getLibraries())
					existingItems.add(((LibraryNode) lib).getProjectItem());
			}
		}

		// Add new libraries to project or chains.
		for (ProjectItem pi : tlProject.getProjectItems()) {
			if (!existingItems.contains(pi) && pi.getContent() != null) {
				if (pi.getRepository() == null) {
					// If not from repository, just create library.
					newLibs.add(new LibraryNode(pi, this));
				} else {
					// If in repo, create a chain if not already created.
					String chainName = makeChainIdentity(pi);
					if (chainMap.containsKey(chainName)) {
						// Add this library to its chain
						lcn = chainMap.get(chainName);
						if ((ln = lcn.add(pi)) != null)
							newLibs.add(ln);
					} else {
						// Create new chain for this project item.
						lcn = new LibraryChainNode(pi, this);
						chainMap.put(chainName, lcn);
						newLibs.addAll(lcn.getLibraries());
					}
				}
			}
		}

		// Resolve XSD and property types.
		TypeResolver tr = new TypeResolver();
		tr.resolveTypes();

		// LOGGER.debug("Loaded project containing " + newLibs.size() + " libraries.");
	}

	/**
	 * Add the files to the project and models all new project items in the GUI model. NOTE - for performance reasons,
	 * always try to add multiple files at once.
	 */
	public void add(List<File> libraryFiles) {
		ProjectController pc = OtmRegistry.getMainController().getProjectController();
		pc.addLibrariesToTLProject(this.project, libraryFiles);
		load(this.project);
	}

	public void add(String path) {
		List<File> files = new ArrayList<File>();
		files.add(new File(path));
		add(files);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getChildren()
	 */
	@Override
	public List<Node> getChildren() {
		return super.getChildren();
	}

	@Override
	public String getComponentType() {
		return "Project: " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getModelObject()
	 */
	@Override
	public ModelObject<?> getModelObject() {
		return modelObject;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Project);
	}

	@Override
	public String getLabel() {
		if (OtmRegistry.getMainController() != null && OtmRegistry.getMainController().getProjectController() != null) {
			ProjectController pc = OtmRegistry.getMainController().getProjectController();
			if (pc.getDefaultProject() == this)
				return getName();
			if (pc.getBuiltInProject() == this)
				return getName();
		}

		NamespaceHandler nsHandler = NamespaceHandler.getNamespaceHandler(this);
		String prefix = "";
		if (nsHandler != null && nsHandler.getPrefix(project.getProjectId()) != null)
			prefix = nsHandler.getPrefix(project.getProjectId()) + " = ";
		return getName() + " [" + prefix + project.getProjectId() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getName()
	 */
	@Override
	public String getName() {
		return project.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getNamePrefix()
	 */
	@Override
	public String getNamePrefix() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return project.getProjectId();
	}

	/**
	 * @return the portion of the project namespace that is managed/governed by a repository. Empty string if not
	 *         managed.
	 */
	public String getNSRoot() {
		String ns = getNamespace();
		for (String managedRoot : OtmRegistry.getMainController().getRepositoryController().getRootNamespaces())
			if (ns.startsWith(managedRoot)) {
				return managedRoot;
			}
		return "";
	}

	/**
	 * @return the portion of the project namespace that is not managed/governed by a repository. Returns the entire
	 *         namespace if unmanaged.
	 */
	public String getNSExtension() {
		if (getNSRoot().isEmpty())
			return getNamespace();
		String extension = getNamespace().substring(getNSRoot().length());
		if (extension.startsWith("/"))
			extension = extension.substring(1);
		return extension;
	}

	@Override
	public String getNameWithPrefix() {
		return project.getProjectId() + "/" + project.getName();
	}

	@Override
	public List<Node> getNavChildren() {
		return super.getChildren();
	}

	@Override
	public Node getParent() {
		return super.getParent();
	}

	@Override
	public boolean hasChildren() {
		return getChildren().isEmpty() ? false : true;
	}

	@Override
	public boolean hasNavChildren() {
		return getChildren().isEmpty() ? false : true;
	}

	@Override
	public boolean isDeprecated() {
		// return project.isDeprecated();
		return false;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEditable() {
		// return ((ProjectMO)getModelObject()).isEditable();
		return true;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public void setName(String n, boolean doFamily) {
		if (!isBuiltIn())
			project.setName(n);
	}

	public void setNamespace(String namespace) {
		if (namespace != null && !namespace.isEmpty())
			project.setProjectId(namespace);
	}

	@Override
	public boolean isBuiltIn() {
		return project.getProjectId().equals(BuiltInProject.BUILTIN_PROJECT_ID);

	}

	/** Not Applicable for Project Interface *********************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getType()
	 */
	@Override
	public Node getType() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0 ? true : false;
	}

	// /* (non-Javadoc)
	// * @see org.opentravel.schemas.node.INode#isTypeUser()
	// */
	// @Override
	// public boolean isTypeUser() {
	// return false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#removeFromLibrary()
	 */
	@Override
	public void removeFromLibrary() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#setAssignedType(org.opentravel.schemas.node.INode )
	 */
	@Override
	public boolean setAssignedType(Node typeNode) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#isTypeProvider()
	 */
	@Override
	public boolean isTypeProvider() {
		return false;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public String toString() {
		return project.getName();
	}

	public String getPath() {
		return project.getProjectFile() != null ? project.getProjectFile().toString() : "";
	}

	/**
	 * Get the chain that the project item should belong to. Null if no chains with same base namespace are open.
	 * 
	 * @param pi
	 * @return
	 */
	public LibraryChainNode getChain(ProjectItem pi) {
		return getChain(makeChainIdentity(pi));
	}

	public LibraryChainNode getChain(String identity) {
		for (Node n : getChildren())
			if (n instanceof LibraryChainNode) {
				// LOGGER.debug("Does " + ((LibraryChainNode) n).makeChainIdentity() + " = " +
				// identity);
				if (((LibraryChainNode) n).makeChainIdentity().equals(identity))
					return (LibraryChainNode) n;
			}
		return null;
	}

	/**
	 * Also see {@link LibraryChainNode#makeIdentity(String, String)}
	 */
	public String makeChainIdentity(ProjectItem pi) {
		NamespaceHandler nsHandler = NamespaceHandler.getNamespaceHandler(this);
		return LibraryChainNode.makeIdentity(pi.getContent().getName(), pi.getBaseNamespace(),
				nsHandler.getNS_Major(pi.getNamespace()));
	}

	public static String appendExtension(String base, String extension) {
		String ns = base;
		if (extension != null && !extension.isEmpty()) {
			if (base.endsWith("/"))
				ns = base + extension;
			else
				ns = base + "/" + extension;
		}
		return RepositoryNamespaceUtils.normalizeUri(ns);
	}

}
