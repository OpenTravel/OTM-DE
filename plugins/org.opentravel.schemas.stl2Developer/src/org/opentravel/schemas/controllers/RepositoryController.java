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

import java.util.List;

import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;

/**
 * @author Dave Hollander
 * 
 */
public interface RepositoryController {

	public boolean addRemoteRepository(String location, String userId, String password);

	public boolean changeCredentials(String location, String userId, String password);

	public boolean commit(LibraryNode source);

	/**
	 * Create a new chain from the new major version created from the passed library.
	 * 
	 * @param library
	 * @return the newly created major version library in the versions node of a new chain.
	 */
	public LibraryNode createMajorVersion(LibraryNode library);

	/**
	 * Add a minor version to the chain and return the new minor version.
	 * 
	 * @param library
	 * @return
	 */
	public LibraryNode createMinorVersion(LibraryNode library);

	/**
	 * Create a patch version of the passed library and return the patch version.
	 * 
	 * @param library
	 * @return
	 */
	public LibraryNode createPatchVersion(LibraryNode library);

	/**
	 * @return a new list of all repository nodes
	 */
	public List<RepositoryNode> getAll();

	/**
	 * @return The local repository node.
	 */
	public RepositoryNode getLocalRepository();

	/**
	 * @return the root of the repository tree. Repository root will be created if needed.
	 */
	public RepositoryNode getRoot();

	/**
	 * Get a list of all the namespaces in known repositories.
	 * 
	 * @return
	 */
	public List<String> getRootNamespaces();

	/**
	 * @return true if the namespace is within scope of the namespaces managed by the repository.
	 */
	public boolean isInManagedNS(String namespace, RepositoryNode repository);

	public boolean lock(LibraryNode library);

	/**
	 * Publish selected libraries and all dependencies to selected repository. If successful, a new chains are created
	 * and the libraries added to them.
	 * 
	 * @param repository
	 *            - target repository
	 * @param libraries
	 *            - selected libraries to be managed in repository
	 * @return list of all published libraries converted to library chain
	 */
	public List<LibraryChainNode> manage(RepositoryNode repository, List<LibraryNode> libraries);

	public boolean markFinal(LibraryNode library);

	public void removeRemoteRepository(RepositoryNode node);

	/**
	 * Searches the contents of the repository using the free-text keywords provided.
	 * 
	 * @param string
	 * @see Repository#search(String, boolean, boolean)
	 */
	public List<RepositoryItem> search(String phrase);

	/**
	 * Rebuild repository node tree.
	 * 
	 * @param n
	 */
	public void sync(INode n);

	/**
	 * Lock the selected libraries.
	 * 
	 * @param commitWIP
	 *            commit library before unlocking if true, otherwise revert
	 */
	public void unlock(boolean commitWIP);

	/**
	 * Public Only for testing
	 * 
	 * @param library
	 * @return
	 */
	public boolean unlock(LibraryNode library);

	public ProjectNode unlockAndRevert(LibraryNode library);

	/**
	 * Validate that the namespace is within the scope of namespaces managed by the available repositories.
	 * 
	 * @param namespace
	 * @return
	 */
	public boolean validateBaseNamespace(String namespace);

	/**
	 * Lock the selected libraries.
	 */
	void lock();

}
