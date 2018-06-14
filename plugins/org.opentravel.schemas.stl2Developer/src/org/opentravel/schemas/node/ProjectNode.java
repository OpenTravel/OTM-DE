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
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.handlers.children.ProjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ProjectNode extends Node implements INode, TypeProviderAndOwners, FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectNode.class);

	private final Project project; // underlying TL model object

	/**
	 * Can be used as a marker for a null project.
	 */
	public ProjectNode() {
		this.project = null;
		parent = Node.getModelNode();

		childrenHandler = new ProjectChildrenHandler(this);
		// libs = new ArrayList<Node>();
		assert (parent instanceof ModelNode);
	}

	/**
	 * Create a project node from the TL project model. Read all libraries in the project and create library nodes and
	 * their children. Link into the model node.
	 * 
	 * @param tlProject
	 *            - tl project to be represented
	 */
	public ProjectNode(Project tlProject) {
		super(tlProject.getName());
		this.project = tlProject; // Not a TLModelElement
		setName(tlProject.getName());

		childrenHandler = new ProjectChildrenHandler(this);

		Node.getModelNode().addProject(this);

		load(tlProject.getProjectItems());

		assert (parent instanceof ModelNode);
	}

	/**
	 * Each project items in the list is added to the library model manager. The library model manager will create a new
	 * node structure for the library if it was not already modeled.
	 * 
	 * @param piList
	 * @return
	 */
	public LibraryNavNode load(final List<ProjectItem> piList) {
		LibraryModelManager manager = getParent().getLibraryManager();
		LibraryNavNode lnn = null;

		// if (piList.isEmpty())
		// LOGGER.warn(this + " project item list is empty.");

		// FIXME - why pass project if it is not added ? Who needs it?
		for (ProjectItem pi : piList) {
			// LOGGER.debug("Adding project item " + pi.getLibraryName() + " to " + this);
			lnn = manager.add(pi, this);
			if (lnn != null) { // can fail to create chain
				add(lnn);
			}
		}
		return lnn;
	}

	/**
	 * Add the files to the project and models all new project items in the GUI model. NOTE - for performance reasons,
	 * always try to add multiple files at once. Does <b>not</b> run type resolver.
	 * <p>
	 * Use {@link org.opentravel.schemas.controllers.DefaultLibraryController#openLibrary(ProjectNode)} or run new
	 * TypeResolver().resolveTypes();
	 * 
	 */
	public void add(List<File> libraryFiles) {
		ProjectController pc = OtmRegistry.getMainController().getProjectController();
		pc.addLibrariesToTLProject(this.project, libraryFiles);
		load(this.project.getProjectItems());
	}

	@Override
	public ProjectChildrenHandler getChildrenHandler() {
		return (ProjectChildrenHandler) childrenHandler;
	}

	/**
	 * Add the child library to the children handler. <b>Note:</b> this method does <i>not</i> use or change the TL
	 * Project.
	 * 
	 * @param child
	 *            Must be LNN for <b>this</b> project
	 */
	public void add(LibraryNavNode child) {
		assert child.getProject() == this;
		getChildrenHandler().add(child);
		child.setParent(this);
	}

	/**
	 * Simply remove library from children handler.
	 * 
	 * @see #close(LibraryInterface)
	 * 
	 * @param library
	 */
	public void remove(LibraryInterface library) {
		getChildrenHandler().remove((Node) library);
	}

	/**
	 * Close each library. Does <b>not</b> close the TL Project. Does <b>not</b> remove project from model node.
	 * <p>
	 * Use {@link DefaultProjectController#close(ProjectNode)} to close TL Project and remove from model.
	 */
	@Override
	public void close() {
		// LOGGER.debug("Closing " + getName());
		List<Node> lnns = getChildrenHandler().getChildren_New();
		for (Node n : lnns)
			n.close();
	}

	/**
	 * Remove this library from this project then use library manager to attempt to close members
	 * 
	 * <b>Note:</b> use project controller to remove libraries from TL Project.
	 * 
	 * @param lib
	 *            - navigator node identifying which library or chain to close
	 */
	public void close(LibraryInterface lib) {
		// Find the affected LibraryNavNode
		LibraryNavNode lnn = find(lib);
		if (lnn != null)
			close(lnn);
	}

	/**
	 * LibraryNavNode children can be either libraries or library chains.
	 * 
	 * @param li
	 * @return the library nav node that associates the passed library with this project
	 */
	private LibraryNavNode find(LibraryInterface li) {
		for (Node n : getChildren())
			if (n instanceof LibraryNavNode) {
				LibraryNavNode lnn = (LibraryNavNode) n;
				if (lnn.getThisLib() == li)
					return lnn;
				if (lnn.getThisLib() instanceof LibraryChainNode)
					if (((LibraryChainNode) lnn.getThisLib()).contains((Node) li))
						return lnn;
			}
		return null;
	}

	/**
	 * Remove the associated library from this project. Using library manager, attempt to close members if this is the
	 * last project to use this library.
	 * <p>
	 * <b>Note:</b> use project controller to remove libraries from TL Project.
	 * 
	 * @param lib
	 *            - navigator node identifying which library or chain to close
	 */
	public void close(LibraryNavNode lnn) {
		assert lnn.getProject() == this;
		getChildrenHandler().remove(lnn);
		getParent().getLibraryManager().close(lnn.getThisLib(), this);
		lnn.setParent(null);
		lnn.deleted = true;
	}

	@Override
	public String getComponentType() {
		return "Project: " + getName();
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

	/**
	 * Get all libraries under <i>this</i> project.
	 * <p>
	 * Returns libraries in chains, not the chain.
	 * 
	 * @return new list of library nodes.
	 */
	// @Override
	public List<LibraryNode> getLibraries() {
		ArrayList<LibraryNode> libs = new ArrayList<>();
		if (getChildrenHandler() != null)
			for (Node n : getChildrenHandler().get()) {
				if (n instanceof LibraryNavNode) {
					LibraryNavNode lnn = (LibraryNavNode) n;
					libs.addAll(lnn.getLibraries());
				}
			}
		return libs;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	/**
	 * Try to find the project item for the library. If not found, try to add it to the project.
	 * 
	 * @return the project item associated with this library
	 */
	public ProjectItem addToTL(AbstractLibrary tlLib) {
		if (getTLProject() == null || getTLProject().getProjectManager() == null)
			return null;
		ProjectItem pi = getProjectItem(tlLib);
		if (pi == null)
			try {
				pi = getTLProject().getProjectManager().addUnmanagedProjectItem(tlLib, getTLProject());
			} catch (RepositoryException e1) {
				e1.printStackTrace();
				LOGGER.error("Repo Error adding " + tlLib.getName() + " to project. " + e1.getLocalizedMessage());
				// assert true;
			} catch (IllegalArgumentException e) {
				LOGGER.error(
						"Argument Exception adding " + tlLib.getName() + " to project. " + e.getLocalizedMessage());
				e.printStackTrace();
				// assert true;
			}
		return pi;
	}

	/**
	 * @return the project item associated with the passed TL AbstractLibrary or null
	 */
	public ProjectItem getProjectItem(AbstractLibrary tlLib) {
		if (getTLProject() == null)
			return null;
		for (ProjectItem item : getTLProject().getProjectItems())
			if (item.getContent() == tlLib)
				return item;
		return null;
	}

	@Override
	public String getPrefix() {
		return "";
	}

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
	public ModelNode getParent() {
		return (ModelNode) parent;
	}

	@Override
	public boolean isDeprecated() {
		// return project.isDeprecated();
		return false;
	}

	public boolean isDefaultProject() {
		return this == OtmRegistry.getMainController().getProjectController().getDefaultProject();
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	// @Override
	// public boolean isLibraryContainer() {
	// return true;
	// }

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	public void setNamespace(String namespace) {
		if (namespace != null && !namespace.isEmpty())
			project.setProjectId(namespace);
	}

	@Override
	public boolean isBuiltIn() {
		return project == null || project.getProjectId() == null ? false
				: project.getProjectId().equals(BuiltInProject.BUILTIN_PROJECT_ID);

	}

	/** Not Applicable for Project Interface *********************/

	@Override
	public Node getType() {
		return null;
	}

	@Override
	public List<LibraryNode> getUserLibraries() {
		List<LibraryNode> libList = new ArrayList<>();
		for (Node lib : getChildrenHandler().get()) {
			// Get libraryNodes from navNode
			assert lib instanceof LibraryNavNode;
			for (LibraryNode ln : ((LibraryNavNode) lib).getLibraries())
				if (ln.getTLModelObject() instanceof TLLibrary)
					libList.add(ln);
		}
		return libList;
	}

	/**
	 * @return null. A TL Project is not a TLModelElement
	 * @see #getTLProject()
	 */
	@Override
	public TLModelElement getTLModelObject() {
		return null;
	}

	// Return true because descendents may have type providers
	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0 ? true : false;
	}

	/**
	 * Remove LibraryNavNode from children handler.
	 * 
	 * @param l
	 */
	public void remove(LibraryNavNode l) {
		getChildrenHandler().clear(l);
	}

	/**
	 * Remove all project items from this project. NO save, tests or checks.
	 */
	public void removeAllFromTLProject() {
		for (Node lib : getChildren())
			// get past LibraryNavNode and VersionNodes
			if (lib.getLibrary() != null)
				project.remove(lib.getLibrary().getProjectItem());
		// TODO - close the project to remove libs from children handler and lib mgr
	}

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	/**
	 * @return the compiler model repository project
	 */
	public Project getTLProject() {
		return project;
	}

	/**
	 * This should only be used if the project was closed. Used in closeAll() to reestablish the builtInProject
	 * 
	 * @param p
	 * @return
	 */
	public Project setTLProject(Project p) {
		return p;
	}

	@Override
	public String toString() {
		return project.getName();
	}

	public String getPath() {
		return project.getProjectFile() != null ? project.getProjectFile().toString() : "";
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
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

	/**
	 * close() all children then remove from child list.
	 */
	public void closeAll() {
		for (Node n : getChildrenHandler().getChildren_New()) {
			n.close();
			getChildrenHandler().remove(n);
			n.setParent(null);
		}
		// TODO - inform libMgr
	}

	/**
	 * @return true if this project contains the library or chain, false otherwise
	 */
	public boolean contains(LibraryInterface li) {
		if (li == null)
			return false;
		for (Node n : getChildrenHandler().get())
			if (n instanceof LibraryNavNode)
				if (((LibraryNavNode) n).contains(li))
					return true;
		return false;
	}

	/**
	 * @return true if the TL project contains an item that contains the TL Library, false otherwise
	 */
	public boolean contains(final AbstractLibrary tlLib) {
		if (tlLib == null || getTLProject() == null)
			return false;
		for (ProjectItem p : getTLProject().getProjectItems())
			if (p.getContent() == tlLib)
				return true;
		return false;
	}

	@Override
	public Node get() {
		return null;
	}

}
