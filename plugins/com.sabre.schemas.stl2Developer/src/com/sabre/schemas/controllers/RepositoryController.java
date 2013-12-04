/**
 * 
 */
package com.sabre.schemas.controllers;

import java.util.List;

import com.sabre.schemacompiler.repository.Repository;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.trees.repository.RepositoryNode;

/**
 * @author Dave Hollander
 * 
 */
public interface RepositoryController {

    /**
     * @return the root of the repository tree. Repository root will be created if needed.
     */
    public RepositoryNode getRoot();

    /**
     * @return a new list of all repository nodes
     */
    public List<RepositoryNode> getAll();

    /**
     * @return The local repository node.
     */
    public RepositoryNode getLocalRepository();

    /**
     * Get a list of all the namespaces in known repositories.
     * 
     * @return
     */
    public List<String> getRootNamespaces();

    /**
     * Publish selected libraries and all dependencies to selected repository. If successful, a new
     * chains are created and the libraries added to them.
     * 
     * @param repository
     *            - target repository
     * @param libraries
     *            - selected libraries to be managed in repository
     * @return list of all published libraries converted to library chain
     */
    public List<LibraryChainNode> manage(RepositoryNode repository, List<LibraryNode> libraries);

    /**
     * Lock the selected libraries.
     */
    void lock();

    /**
     * Lock the selected libraries.
     * @param commitWIP TODO
     */
    void unlock(boolean commitWIP);

    /**
     * Rebuild repository node tree.
     * 
     * @param n
     */
    public void sync(INode n);

    public boolean markFinal(LibraryNode library);

    public boolean lock(LibraryNode library);

    public boolean unlock(LibraryNode library);

    public ProjectNode unlockAndRevert(LibraryNode library);

    /**
     * Create a patch version of the passed library and return the patch version.
     * 
     * @param library
     * @return
     */
    public LibraryNode createPatchVersion(LibraryNode library);

    /**
     * Add a minor version to the chain and return the new minor version.
     * 
     * @param library
     * @return
     */
    public LibraryNode createMinorVersion(LibraryNode library);

    /**
     * Create a new chain from the new major version created from the passed library.
     * 
     * @param library
     * @return the newly created major version library in the versions node of a new chain.
     */
    public LibraryNode createMajorVersion(LibraryNode library);

    /**
     * Validate that the namespace is within the scope of namespaces managed by the available
     * repositories.
     * 
     * @param namespace
     * @return
     */
    public boolean validateBaseNamespace(String namespace);

    /**
     * @return true if the namespace is within scope of the namespaces managed by the repository.
     */
    public boolean isInManagedNS(String namespace, RepositoryNode repository);

    public boolean addRemoteRepository(String location, String userId, String password);

    public boolean changeCredentials(String location, String userId, String password);

    /**
     * Searches the contents of the repository using the free-text keywords provided.
     * 
     * @param string
     * @see Repository#search(String, boolean, boolean)
     */
    public List<RepositoryItem> search(String phrase);

    public void removeRemoteRepository(RepositoryNode node);

    public boolean commit(LibraryNode source);

}
