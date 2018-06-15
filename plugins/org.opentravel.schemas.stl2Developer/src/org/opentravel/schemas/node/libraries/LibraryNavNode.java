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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.handlers.children.LibraryNavChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryOwner;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Library Navigation Node
 * 
 * The LibNavNode is used to provide a link between a library a specific project. Libraries exist only once in the model
 * but may be rendered under multiple projects. This node provides access to the library and from the library to a
 * specific project.
 * 
 * Methods in this class must have no knowledge of the contents of the library or chain.
 */

public class LibraryNavNode extends Node implements LibraryOwner, FacadeInterface, TypeProviderAndOwners {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNavNode.class);

	protected static final String DEFAULT_LIBRARY_TYPE = "Library Nav";

	/**
	 */
	public LibraryNavNode(LibraryInterface library, ProjectNode project) {
		assert (library != null);
		assert (project != null);

		childrenHandler = new LibraryNavChildrenHandler(this);
		getChildrenHandler().add((Node) library);

		parent = project;

		// Make sure the library is not in project's children list.
		if (library.getParent() != null && library.getParent() instanceof ProjectNode)
			((ProjectNode) library.getParent()).remove(library);

		// Insert between library and project
		library.setParent(this);
		project.add(this);

		// LOGGER.debug("Created library nav node for library " + library + " in project " + project);
	}

	@Override
	public LibraryNavChildrenHandler getChildrenHandler() {
		return (LibraryNavChildrenHandler) childrenHandler;
	}

	/**
	 * Create library navigation node. This LNN is added to the passed project.
	 * <p>
	 * <b>Note:</b> this constructor does <i>not</i> use or change the TL Project.
	 * 
	 * @param chain
	 *            - chain to associate with this project
	 * @param project
	 *            - project to set as this node's parent and add this LNN to as a child
	 */
	public LibraryNavNode(LibraryChainNode chain, ProjectNode project) {
		assert (chain != null);
		assert (project != null);

		childrenHandler = new LibraryNavChildrenHandler(this);
		getChildrenHandler().add(chain);

		parent = project;

		// Make sure the chain is not in project children list.
		if (chain.getParent() != null && chain.getParent() instanceof ProjectNode)
			chain.getParent().getChildren().remove(chain);

		// Insert between library chain and project
		project.add(this);

		// LOGGER.debug("Created library nav node for chain " + chain + " in project " + project);
	}

	/**
	 * 
	 * @return the LibraryNode or LibraryChainNode or null
	 */
	public LibraryInterface getThisLib() {
		return getChildrenHandler().getThisLibI();
	}

	@Override
	public boolean isEditable() {
		return getThisLib() != null ? ((Node) getThisLib()).isEditable() : false;
	}

	@Override
	public String getComponentType() {
		return DEFAULT_LIBRARY_TYPE;
	}

	@Override
	public String getName() {
		return getThisLib() != null ? getThisLib().getName() : "";
	}

	@Override
	public String getLabel() {
		return getThisLib() != null ? getThisLib().getLabel() : "";
	}

	@Override
	public Image getImage() {
		return getThisLib() != null ? getThisLib().getImage() : null;
	}

	@Override
	public ProjectNode getParent() {
		return (ProjectNode) parent;
	}

	@Override
	public ProjectNode getProject() {
		return (ProjectNode) parent;
	}

	/**
	 * Use the libraryModelManager to close the library in this project. Remove this libraryNavNode from its project.
	 */
	@Override
	public void close() {
		// Lib Mgr may change parent
		getModelNode().getLibraryManager().close(getThisLib(), getProject());
		deleted = true;
		getChildrenHandler().clear((Node) getThisLib());
		// Remove from project
		getParent().remove(this);
	}

	/**
	 * @return a library, unmanaged or the head of a chain
	 */
	@Override
	public LibraryNode getLibrary() {
		return getThisLib() != null ? getThisLib().getLibrary() : null;
	}

	/**
	 * Get the library or libraries from a chain.
	 */
	// @Override
	public List<LibraryNode> getLibraries() {
		List<LibraryNode> libs = new ArrayList<>();
		if (getThisLib() instanceof LibraryNode)
			libs.add((LibraryNode) getThisLib());
		if (getThisLib() instanceof LibraryChainNode)
			libs.addAll(((LibraryChainNode) getThisLib()).getLibraries());
		return libs;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	public void setThisLib(LibraryInterface library) {
		getChildrenHandler().add((Node) library);
	}

	/**
	 * Return true if the passed library or chain is contained in this nav node. Members of chains are tested as
	 * required.
	 */
	@Override
	public boolean contains(LibraryInterface li) {
		return getChildrenHandler().contains(li);
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	/**
	 * see {@link #getLibrary()}
	 * 
	 */
	@Override
	public Node get() {
		return getLibrary();
	}

	/**
	 * Returns library's TLModelObject
	 */
	@Override
	public TLModelElement getTLModelObject() {
		return getLibrary() != null ? getLibrary().getTLModelObject() : null;
	}
}
