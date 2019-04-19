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

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryOwner;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Maintains a list of libraries for the model node. All libraries are managed here. Project children are
 * LibraryNavNodes which link to libraries managed here.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryModelManager {
    private static final Logger LOGGER = LoggerFactory.getLogger( LibraryModelManager.class );

    @Deprecated
    Collection<LibraryInterface> libraries = new ArrayList<>();
    ModelNode parent = null;
    HashMap<String,LibraryInterface> libMap = new HashMap<>();

    public LibraryModelManager(ModelNode parent) {
        this.parent = parent;
    }

    // public LibraryModelManager(final MainController mainController) {
    // }

    /**
     * Simply add this library or chain to the list.
     */
    public void add(LibraryInterface lib) {
        assert (lib.getParent() instanceof LibraryNavNode);
        // assert (lib.getProject() instanceof ProjectNode);
        if (lib.getLibrary() == null) {
            assert false;
            LOGGER.warn( "Missing library" );
        }
        // String cn = getCanonicalName(lib.getLibrary().getProjectItem());
        // if (cn != null)
        addToMap( lib.getLibrary().getProjectItem(), lib );
        // if (libMap.get(cn) == null)
        // libMap.put(cn, lib);

        if (!libraries.contains( lib ))
            libraries.add( lib );
    }

    /**
     * Create libraries and library chains from the project item. Will create library or chain if it has not already be
     * added. Newly created libraries will have their parent set. Types are set but <b>not</b> resolved.
     * 
     * @param pi - project item to model
     * @param project - project node to associate with the Nav node (and newly create libraries and chains)
     * @return Return a LibraryNavNode to use as a child in a tree.
     */
    public LibraryNavNode add(ProjectItem pi, ProjectNode project) {
        // LOGGER.debug("Adding library to model from project item: " + pi.getVersion() + " " + pi.getLibraryName());
        LibraryInterface li = null;
        LibraryNavNode newLNN = null;

        // See if the tlLibrary has a node listener. Null if not.
        Node n = Node.GetNode( pi.getContent() );
        if (n instanceof LibraryInterface)
            li = ((LibraryInterface) n);

        // Safety check - was the library/chain found already deleted?
        if (li != null)
            assert (!((Node) li).isDeleted());

        // All Done - the library or chain is in the project.
        if (project.contains( li )) {
            LOGGER.debug( "Did not add project item " + li + " because it was already in project." );
            return null;
        }

        if (li == null) {
            // First time this library has been modeled.
            newLNN = modelLibraryInterface( pi, project );
        } else {
            // Already modeled - add new LibraryNavNode to this project
            if (li.getChain() == null)
                newLNN = new LibraryNavNode( li, project );
            else
                newLNN = new LibraryNavNode( li.getChain(), project );
        }
        if (newLNN == null || newLNN.getLibrary() == null) {
            LOGGER.warn( "Error creating new library nav node." );
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
                assert ((LibraryChainNode) newLNN.getThisLib()).getLibraries().contains( li );
            }
        } else if (li instanceof LibraryChainNode)
            assert (((LibraryChainNode) li).getLibraries().contains( newLNN.getLibrary() ));

        // LOGGER.debug("Adding library to model from project item: " + pi.getLibraryName());
        return newLNN;
    }

    private void addToMap(ProjectItem pi, LibraryInterface li) {
        String cn = getCanonicalName( pi );
        LibraryInterface entry = libMap.get( cn );
        if (!libMap.containsKey( getCanonicalName( pi ) ))
            libMap.put( getCanonicalName( pi ), li );
        // else
        // LOGGER.warn("Avoided duplicating canonical names in map. " + getCanonicalName(pi));
        // FIXME - this is run too often on startup
    }

    /**
     * Force removal of all libraries. Removes TLLibrary from model and closes the library.
     * 
     * @param builtIns do built in libraries if true
     */
    public void clear(boolean builtIns) {
        // Close the chains first then any libraries left over.
        List<LibraryChainNode> lcns = getUserChains();
        TLModel tlModel = null;
        for (LibraryChainNode lcn : lcns)
            close( lcn, null );
        // lcn.close(); // Fixme - closes chain but does not remove from table and list

        List<LibraryNode> libs;
        if (builtIns)
            libs = getAllLibraries();
        else
            libs = getUserLibraries();

        if (!libs.isEmpty() && libs.get( 0 ).getTLModelObject() != null)
            tlModel = libs.get( 0 ).getTLModelObject().getOwningModel();

        for (LibraryNode lib : libs) {
            if (lib != null) {
                if (lib.getTLModelObject() != null)
                    if (lib.getTLModelObject().getOwningModel() != null)
                        lib.getTLModelObject().getOwningModel().removeLibrary( lib.getTLModelObject() );
                // TODO - set parent to null first or use close(lib, null)
                close( lib, null );
                // lib.setParent(null); // needed to force library closure
                // lib.close();
            }
        }
        if (tlModel != null)
            tlModel.clearModel();

        if (builtIns) {
            libraries.clear();
            libMap.clear();
        }
        // TODO - assure builtins where used are all cleared
        // getWhereAssignedHandler().clear();

    }

    /**
     * Close passed library or chain if not used in other projects. Does not unlink. Will reset parent if needed.
     * <p>
     * Since libraries and chains are not direct children of projects, removal from the project is <b>not</b> done.
     * Caller is expected to remove library from project
     * 
     * @param lib
     * @param projectNode
     */
    public void close(LibraryInterface lib, ProjectNode projectNode) {
        // LOGGER.debug("Closing " + ((Node) lib).getName());
        LibraryOwner alternateOwner = getFirstOtherProject( lib, projectNode, true );
        if (alternateOwner != null) {
            assert alternateOwner.getProject() != projectNode;
            // LOGGER.debug("Only remove from project. " + ((Node) lib).getName());
            lib.setParent( (Node) alternateOwner );
        } else {
            // LOGGER.debug("Not used elsewhere...close. " + ((Node) lib).getName());
            // If reached then the library is not used elsewhere
            // Must remove parent for close to work
            if (lib != null) {
                lib.setParent( null );
                lib.closeLibraryInterface();
            }
            // Remove all examples
            // TODO - make this more selective
            // if (OtmRegistry.getExampleView() != null)
            // OtmRegistry.getExampleView().clearExamples();
            if (OtmRegistry.getValidationResultsView() != null)
                OtmRegistry.getValidationResultsView().clearFindings();
            // TODO - how to clear the graphical view?

            // Remove from list
            libraries.remove( lib );
            libMap.remove( findKey( lib ) );
        }
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
        LibraryInterface li = new LibraryChainNode( pi, project );
        if (li == null || li.getParent() == null) {
            LOGGER.warn( "Failed to create valid library chain." );
            li = null;
        } // FIXME - should the chain's LNN be in project?
        return li;
    }

    /**
     * 
     * @return the key that contains this library interface or null if not found
     */
    public String findKey(LibraryInterface li) {
        for (Entry<String,LibraryInterface> e : libMap.entrySet()) {
            if (e.getValue() == li)
                return e.getKey();
        }
        return null;
    }

    /**
     * @return all projects that contain this library interface
     */
    public List<ProjectNode> findProjects(LibraryInterface li) {
        List<ProjectNode> projects = new ArrayList<>();
        for (ProjectNode pn : parent.getProjects())
            if (pn.contains( li ))
                projects.add( pn );
        return projects;
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
            if (lib.getName().equals( libraryName ))
                if (lib.getNamespace().equals( namespace ))
                    return lib;
        }
        return null;
    }

    /**
     * @return new list of all library nodes in the model, including those in chains
     */
    public List<LibraryNode> getAllLibraries() {
        List<LibraryNode> libList = new ArrayList<>();
        // for (LibraryInterface lib : libraries)
        for (LibraryInterface lib : libMap.values())
            if (lib instanceof LibraryNode)
                libList.add( ((LibraryNode) lib) );
            else if (lib instanceof LibraryChainNode)
                libList.addAll( ((LibraryChainNode) lib).getLibraries() );
        return libList;
    }

    /**
     * This name is the same for all minor versions of a library.
     * 
     * @param pi
     * @return
     */
    public String getCanonicalName(ProjectItem pi) {
        if (pi == null)
            return null;
        VersionScheme scheme = null;
        String versionSchemeName = "OTA2";
        versionSchemeName = pi.getVersionScheme();

        try {
            scheme = VersionSchemeFactory.getInstance().getVersionScheme( pi.getVersionScheme() );
        } catch (VersionSchemeException e) {
            LOGGER.error( e.getLocalizedMessage() );
            return "";
        }
        String namespace = pi.getNamespace();
        String version = "";
        try {
            version = scheme.getMajorVersion( scheme.getVersionIdentifier( namespace ) );
        } catch (IllegalArgumentException e) {
            LOGGER.error( e.getLocalizedMessage() + " " + namespace );
        }

        String s = pi.getBaseNamespace();
        s += "/" + pi.getLibraryName();
        s += ".V" + version;
        return s;
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
                if (((LibraryChainNode) n).makeChainIdentity().equals( chainName ))
                    return (LibraryChainNode) n;
            }
        // if (n.getName().equals(chainName))
        // return (LibraryChainNode) n;
        return null;
    }

    /**
     * Return the library owner for the passed library that is not in the passed project or null if not found.
     * <p>
     * <b>Note:</b> returned library owner <i>may</i> have the skipped project as its parent!
     * 
     * @param lib
     * @param project the project to skip. If null, all projects will be searched.
     * @param setProject if true, and the found library owner is a LibraryNavNode (not a library in a chain) then the
     *        navNode will be removed from its current parent and parent will be set to the found project.
     * @return
     */
    public LibraryOwner getFirstOtherProject(LibraryInterface lib, ProjectNode project, boolean setProject) {
        LibraryOwner lo = null;

        for (ProjectNode pn : parent.getUserProjects()) {
            if (pn == project)
                continue; // find other project
            if (pn.contains( lib )) {
                // Another project was found, find and return the correct owner
                for (Node n : pn.getChildren())
                    if (n instanceof LibraryOwner)
                        if (((LibraryOwner) n).contains( lib )) {
                            if (n.getParent() != pn)
                                LOGGER.error( "Library Owner has wrong parent " + n );
                            // if (n instanceof LibraryNavNode && ((LibraryOwner) n).getProject() == project)
                            // ((LibraryNavNode) n).setProject(pn, true);
                            return (LibraryOwner) n;
                        }
            }
        }
        return null;
        // FIXME - make sure this works as designed - junit ???

        // for (Node n : parent.getChildren())
        // if (n instanceof ProjectNode && n != project)
        // for (Node l : n.getChildren()) {
        // if (l instanceof LibraryNavNode) {
        // lo = (LibraryOwner) l;
        // break;
        // } else if (l instanceof LibraryChainNode)
        // if (((LibraryChainNode) l).contains((Node) lib)) {
        // lo = ((LibraryChainNode) l).getVersions();
        // break;
        // }
        // }
        // return lo;
    }

    // public ProjectNode getFirstOtherProject(LibraryInterface lib, ProjectNode project) {
    // ProjectNode pn = null;
    // // Search all projects
    // for (Node n : parent.getChildren())
    // if (n instanceof ProjectNode && n != project)
    // for (Node l : n.getChildren()) {
    // if (l instanceof LibraryNavNode)
    // l = (Node) ((LibraryNavNode) l).getThisLib();
    // if (l instanceof LibraryChainNode)
    // if (((LibraryChainNode) l).contains((Node) lib)) {
    // pn = (ProjectNode) n;
    // break;
    // }
    // if (l == lib) {
    // pn = (ProjectNode) n;
    // break;
    // }
    // }
    // return pn;
    // }

    /**
     * Get a sorted set of editable libraries (Static)
     */
    public SortedMap<String,LibraryNode> getEditableLibrarySet() {
        SortedMap<String,LibraryNode> map = new TreeMap<>();
        for (LibraryNode ln : getUserLibraries())
            if (ln.isEditable())
                map.put( ln.getNameWithPrefix(), ln );
        for (Entry<String,LibraryNode> e : map.entrySet())
            LOGGER.debug( "Key = " + e.getValue() );
        return map;
        // FIXME - add junit test
    }

    /**
     * @return a list copy of the managed libraries and chains
     */
    public List<LibraryInterface> getLibraries() {
        return new ArrayList<>( libMap.values() );
        // return new ArrayList<LibraryInterface>(libraries);
    }

    public Collection<LibraryInterface> getListValues() {
        return libraries;
    }

    public Object[] getMapValues() {
        return libMap.values().toArray();
    }

    /**
     * @return new list of all TLLibrary (user) library nodes in the model, including those in a chain
     */
    public List<LibraryChainNode> getUserChains() {
        List<LibraryChainNode> chains = new ArrayList<>();
        // for (LibraryInterface lib : libraries)
        for (LibraryInterface lib : libMap.values())

            if (lib instanceof LibraryChainNode)
                chains.add( ((LibraryChainNode) lib) );
        return chains;
    }

    /**
     * @return new list of all TLLibrary (user) library nodes in the model including those in chains
     */
    public List<LibraryNode> getUserLibraries() {
        List<LibraryNode> libList = new ArrayList<>();
        // for (LibraryInterface lib : libraries)
        for (LibraryInterface lib : libMap.values())
            if (lib instanceof LibraryNode && ((LibraryNode) lib).getTLModelObject() instanceof TLLibrary)
                libList.add( ((LibraryNode) lib) );
            else if (lib instanceof LibraryChainNode)
                libList.addAll( ((LibraryChainNode) lib).getLibraries() );
        return libList;
    }

    /**
     * @return if the library is used in any other projects.
     */
    public boolean isUsedElsewhere(LibraryInterface lib, ProjectNode project) {
        return getFirstOtherProject( lib, project, false ) != null;
    }

    private LibraryNavNode modelLibraryInterface(ProjectItem pi, ProjectNode project) {
        LibraryInterface li = null;
        LibraryNavNode newLNN = null;
        // LOGGER.debug("First time library has been model: " + pi.getLibraryName());

        if (pi.getRepository() == null) {
            // Library is Unmanaged therefore a libraryNode.
            // If already registered then use the existing libraryNode.
            li = libMap.get( getCanonicalName( pi ) );
            if (li == null)
                // create library node.
                li = new LibraryNode( pi.getContent(), project );
            else
                // Create new Library Nav Node associating the library to new project
                li.setParent( new LibraryNavNode( li, project ) );

        } else {
            // Library is managed - make into or add to a chain
            String chainName = project.makeChainIdentity( pi );
            LibraryChainNode chain = getChain( chainName );

            if (chain == null) {
                assert (!libMap.containsKey( getCanonicalName( pi ) ));
                li = createNewChain( pi, project );
            } else {
                assert (libMap.containsKey( getCanonicalName( pi ) ));
                // Managed library that belongs to a chain.
                // First, see if the chain has already been managed here
                // If the chain was not found, try to create one.
                if (!libraries.contains( chain )) {
                    // LOGGER.debug("Create chain for a minor version.");
                    li = new LibraryChainNode( pi, project );
                } else {
                    // LOGGER.debug("Add to existing chain.");
                    li = chain;
                    if (!(li.getParent() instanceof LibraryNavNode) || li.getParent().getParent() != project)
                        newLNN = new LibraryNavNode( chain, project );
                }
            }
        }

        // Add the library to the list
        if (li != null && newLNN == null) {
            if (!libraries.contains( li ))
                libraries.add( li );
            addToMap( pi, li );

            // Get the LibraryNavNode to return
            if (li.getParent() instanceof LibraryNavNode)
                newLNN = (LibraryNavNode) li.getParent();
            // else
            // LOGGER.error("Newly modeled library " + li + " is missing nav node!");
        }
        if (li == null)
            LOGGER.error( "Did not successfully model the library: " + pi.getLibraryName() );

        // assert (newLNN != null);
        return newLNN;
    }

    /**
     * Replace the old library with the new one. Save replacement in list and change in all projects. Used to convert a
     * library to library chain.
     * 
     * @param old
     * @param replacement
     */
    public void replace(LibraryInterface old, LibraryInterface replacement) {
        libraries.remove( old );
        libraries.add( replacement );
        if (findKey( old ) != null)
            libMap.put( findKey( old ), replacement );

        // Update any LibraryNavNodes in other projects
        for (Node n : parent.getChildren())
            if (n instanceof ProjectNode)
                // If project has old nav node in it, update its library
                for (Node l : n.getChildren())
                    if (l instanceof LibraryNavNode)
                        if (((LibraryNavNode) l).getThisLib() == old)
                            ((LibraryNavNode) l).setThisLib( replacement );
    }
}
