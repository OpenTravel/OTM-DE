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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a list of libraries for the model node. All libraries are managed here. Project children are
 * LibraryNavNodes which link to libraries managed here.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryModelManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryModelManager.class);

	Collection<LibraryInterface> libraries = new ArrayList<LibraryInterface>();
	ModelNode parent = null;

	public LibraryModelManager(ModelNode parent) {
		this.parent = parent;
	}

	public LibraryModelManager(final MainController mainController) {
	}

	/**
	 * @return new list of all TLLibrary (user) library nodes in the model, including those in a chain
	 */
	public List<LibraryNode> getUserLibraries() {
		List<LibraryNode> libList = new ArrayList<LibraryNode>();
		for (LibraryInterface lib : libraries)
			if (lib instanceof LibraryNode && ((LibraryNode) lib).getTLModelObject() instanceof TLLibrary)
				libList.add(((LibraryNode) lib));
			else if (lib instanceof LibraryChainNode)
				libList.addAll(((LibraryChainNode) lib).getLibraries());
		return libList;
	}

	/**
	 * @return new list of all TLLibrary (user) library nodes in the model, including those in a chain
	 */
	public List<LibraryChainNode> getUserChains() {
		List<LibraryChainNode> chains = new ArrayList<LibraryChainNode>();
		for (LibraryInterface lib : libraries)
			if (lib instanceof LibraryChainNode)
				chains.add(((LibraryChainNode) lib));
		return chains;
	}

	/**
	 * @return new list of all library nodes in the model, including those in a chain
	 */
	public List<LibraryNode> getAllLibraries() {
		List<LibraryNode> libList = new ArrayList<LibraryNode>();
		for (LibraryInterface lib : libraries)
			if (lib instanceof LibraryNode)
				libList.add(((LibraryNode) lib));
			else if (lib instanceof LibraryChainNode)
				libList.addAll(((LibraryChainNode) lib).getLibraries());
		return libList;
	}

	/**
	 * Simply add this library or chain to the list.
	 */
	public void add(LibraryInterface lib) {
		assert (lib.getParent() instanceof LibraryNavNode);
		assert (lib.getProject() instanceof ProjectNode);
		if (!libraries.contains(lib))
			libraries.add(lib);
	}

	/**
	 * Create libraries and library chains from the project item. Will create library or chain if it has not already be
	 * added. Newly created libraries will have their parent set. Types are set but <b>not</b> resolved.
	 * 
	 * @param pi
	 *            - project item to model
	 * @param project
	 *            - project node to associate with the Nav node (and newly create libraries and chains)
	 * @return Return a LibraryNavNode to use as a child in a tree.
	 */
	public LibraryNavNode add(ProjectItem pi, ProjectNode project) {
		// LOGGER.debug("Adding library to model from project item: " + pi.getVersion() + " " + pi.getLibraryName());
		LibraryInterface li = null;
		LibraryNavNode newLNN = null;

		// See if the tlLibrary has a node listener. Null if not.
		Node n = Node.GetNode((TLModelElement) pi.getContent());
		if (n instanceof LibraryInterface)
			li = ((LibraryInterface) n);

		// Safety check - was the library/chain found already deleted?
		if (li != null)
			assert (!((Node) li).isDeleted());

		// All Done - the library or chain is in the project.
		if (project.contains(li)) {
			LOGGER.debug("Did not add project item " + li + " because it was already in project.");
			return null;
		}
		// if (li != null && project.getLibraries().contains(li)) {
		// // - do NOT do this - have caller handle null or return error condition
		// // If they are trying to open an version, the expose the version directly.
		// // if (li.getParent() instanceof VersionAggregateNode)
		// // newLNN = (LibraryNavNode) new LibraryChainNode(pi, project).getParent();
		// // LOGGER.debug("Skipping adding " + li + " to libraryModelManager.");
		// return null;
		// }

		if (li == null) {
			// First time this library has been modeled.
			newLNN = modelLibraryInterface(pi, project);
		} else {
			// Already modeled - add new LibraryNavNode to this project
			if (li.getChain() == null)
				newLNN = new LibraryNavNode((LibraryNode) li, project);
			else
				newLNN = new LibraryNavNode(li.getChain(), project);
		}
		if (newLNN == null || newLNN.getLibrary() == null) {
			LOGGER.warn("Error creating new library nav node.");
			return null;
		}
		assert (newLNN != null);
		assert (newLNN.getLibrary() != null);

		// Finally, update the status as a new project assignment may change it
		newLNN.getLibrary().updateLibraryStatus(); // new project may change status

		// Post checks
		assert (newLNN.getParent() == project);
		if (li instanceof LibraryNode) {
			if (!((LibraryNode) li).isInChain())
				assert (newLNN.getLibrary() == li);
			else {
				assert (newLNN.getThisLib() instanceof LibraryChainNode);
				assert ((LibraryChainNode) newLNN.getThisLib()).getLibraries().contains(li);
			}
		} else if (li instanceof LibraryChainNode)
			assert (((LibraryChainNode) li).getLibraries().contains(newLNN.getLibrary()));

		// LOGGER.debug("Adding library to model from project item: " + pi.getLibraryName());
		return newLNN;
	}

	private LibraryNavNode modelLibraryInterface(ProjectItem pi, ProjectNode project) {
		LibraryInterface li = null;
		LibraryNavNode newLNN = null;
		// LOGGER.debug("First time library has been model: " + pi.getLibraryName());

		if (pi.getRepository() == null)
			// Library is Unmanaged - create library node.
			li = new LibraryNode(pi.getContent(), project);
		else {
			// Library is managed - make into or add to a chain
			String chainName = project.makeChainIdentity(pi);
			LibraryChainNode chain = getChain(chainName);

			if (chain == null) {
				li = createNewChain(pi, project);
			} else {
				// Managed library that belongs to a chain.
				// First, see if the chain has already been managed here
				// If the chain was not found, try to create one.
				if (!libraries.contains(chain)) {
					// LOGGER.debug("Create chain for a minor version.");
					li = new LibraryChainNode(pi, project);
				} else {
					// LOGGER.debug("Add to existing chain.");
					li = chain;
					if (!(li.getParent() instanceof LibraryNavNode) || li.getParent().getParent() != project)
						newLNN = new LibraryNavNode(chain, project);
				}
			}
		}

		// Add the library to the list
		if (li != null && newLNN == null) {
			if (!libraries.contains(li))
				libraries.add(li);

			// Get the LibraryNavNode to return
			if (li.getParent() instanceof LibraryNavNode)
				newLNN = (LibraryNavNode) li.getParent();
			// else
			// LOGGER.error("Newly modeled library " + li + " is missing nav node!");
		}
		if (li == null)
			LOGGER.error("Did not successfully model the library: " + pi.getLibraryName());

		// assert (newLNN != null);
		return newLNN;
	}

	/**
	 * @return all projects that contain this library interface
	 */
	public List<ProjectNode> findProjects(LibraryInterface li) {
		List<ProjectNode> projects = new ArrayList<ProjectNode>();
		for (ProjectNode pn : parent.getProjects())
			if (pn.contains(li))
				projects.add(pn);
		return projects;
	}

	/**
	 * Create a chain from the project item.
	 * 
	 * @param pi
	 * @param project
	 * @return chain (library interface) or null on error
	 */
	private LibraryInterface createNewChain(ProjectItem pi, ProjectNode project) {
		// LOGGER.debug("No projects contain a chain for the project item. Create new chain.");
		LibraryInterface li = new LibraryChainNode(pi, project);
		if (li == null || li.getParent() == null) {
			LOGGER.warn("Failed to create valid library chain.");
			li = null;
		} // FIXME - should the chain's LNN be in project?
		return li;
	}

	/**
	 * String chainName = project.makeChainIdentity(pi);
	 * 
	 * @return if this item should be added to a chain, return that chain, null otherwise.
	 */
	private LibraryChainNode getChain(String chainName) {
		// See if any of the managed libraries have the chain identity.
		for (LibraryInterface n : libraries)
			if (n instanceof LibraryChainNode) {
				String ci = ((LibraryChainNode) n).makeChainIdentity();
				if (((LibraryChainNode) n).makeChainIdentity().equals(chainName))
					return (LibraryChainNode) n;
			}
		// if (n.getName().equals(chainName))
		// return (LibraryChainNode) n;
		return null;
	}

	/**
	 * @return if the library is used in any other projects.
	 */
	public boolean isUsedElsewhere(LibraryInterface lib, ProjectNode project) {
		return getFirstOtherProject(lib, project) != null;
	}

	public ProjectNode getFirstOtherProject(LibraryInterface lib, ProjectNode project) {
		ProjectNode pn = null;
		for (Node n : parent.getChildren())
			if (n instanceof ProjectNode && n != project)
				for (Node l : n.getChildren()) {
					if (l instanceof LibraryNavNode)
						l = (Node) ((LibraryNavNode) l).getThisLib();
					if (l instanceof LibraryChainNode)
						if (((LibraryChainNode) l).contains((Node) lib)) {
							pn = (ProjectNode) n;
							break;
						}
					if (l == lib) {
						pn = (ProjectNode) n;
						break;
					}
				}
		// if (pn != null)
		// LOGGER.debug(((Node) lib).getName() + " is also used in " + pn);
		// else
		// LOGGER.debug(((Node) lib).getName() + " is only used in passsed project " + project);
		return pn;
	}

	/**
	 * Close passed library or chain if not used in other projects. Does not unlink. Will reset parent if needed.
	 * 
	 * @param lib
	 * @param projectNode
	 *            - caller is expected to remove library from project
	 */
	public void close(LibraryInterface lib, ProjectNode projectNode) {
		// LOGGER.debug("Closing " + ((Node) lib).getName());
		ProjectNode pn = getFirstOtherProject(lib, projectNode);
		if (pn != null) {
			// LOGGER.debug("Only remove from project. " + ((Node) lib).getName());
			lib.setParent(pn);
		} else {
			// If reached then the library is not used elsewhere
			// LOGGER.debug("Not used elsewhere...close. " + ((Node) lib).getName());
			// Must remove parent for close to work
			lib.setParent(null);
			lib.close();

			// Remove from list
			libraries.remove(lib);
		}
	}

	/**
	 * Replace the old library with the new one. Save replacement in list and change in all projects. Used to convert a
	 * library to library chain.
	 * 
	 * @param old
	 * @param replacement
	 */
	public void replace(LibraryInterface old, LibraryInterface replacement) {
		libraries.remove(old);
		libraries.add(replacement);

		// Update any LibraryNavNodes in other projects
		for (Node n : parent.getChildren())
			if (n instanceof ProjectNode)
				// If project has old nav node in it, update its library
				for (Node l : n.getChildren())
					if (l instanceof LibraryNavNode)
						if (((LibraryNavNode) l).getThisLib() == old)
							((LibraryNavNode) l).setThisLib(replacement);
	}

	/**
	 * @return a copy of the library and chain list
	 */
	public List<LibraryInterface> getLibraries() {
		return new ArrayList<LibraryInterface>(libraries);
	}

	/**
	 * Return the library with the name and namespace
	 * 
	 * @param namespace
	 * @param libraryName
	 * @return libraryNode or null if not found
	 */
	public LibraryNode get(String namespace, String libraryName) {
		for (LibraryNode lib : getUserLibraries()) {
			// LOGGER.debug(" test " + lib.getNamespace() + " " + lib.getName());
			if (lib.getName().equals(libraryName))
				if (lib.getNamespace().equals(namespace))
					return (LibraryNode) lib;
		}
		return null;
	}

	/**
	 * Force removal of all libraries. Removes TLLibrary from model and closes the library.
	 * 
	 * @param builtIns
	 *            do built in libraries if true
	 */
	public void clear(boolean builtIns) {
		// Close the chains first then any libraries left over.
		List<LibraryChainNode> lcns = getUserChains();
		for (LibraryChainNode lcn : lcns)
			lcn.close();

		List<LibraryNode> libs;
		if (builtIns)
			libs = getAllLibraries();
		else
			libs = getUserLibraries();
		for (LibraryNode lib : libs) {
			if (lib != null) {
				if (lib.getTLModelObject() != null)
					if (lib.getTLModelObject().getOwningModel() != null)
						lib.getTLModelObject().getOwningModel().removeLibrary(lib.getTLModelObject());
				lib.close();
			}
		}
	}

}
