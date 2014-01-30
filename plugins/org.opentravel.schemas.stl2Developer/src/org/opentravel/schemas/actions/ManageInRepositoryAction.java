
package org.opentravel.schemas.actions;

import java.util.Collection;
import java.util.Collections;

import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.trees.repository.RepositoryNode;

import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class ManageInRepositoryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.manage");
    RepositoryNode repository;
    RepositoryController rc;
    private LibraryNode selectedLibrary;

    public ManageInRepositoryAction() {
        super(propDefault);
        rc = mc.getRepositoryController();
    }

    public ManageInRepositoryAction(final StringProperties props, RepositoryNode repository) {
        super(props);
        this.repository = repository;
        rc = mc.getRepositoryController();
    }

    public void setLibrary(LibraryNode n) {
        // TODO: support for multi selection of libraries
        selectedLibrary = n;
    }

    @Override
    public void run() {
        if (repository == null)
            return; // TODO - launch repository selection wizard

        rc.manage(repository, Collections.singletonList(selectedLibrary));
    }

    private Collection<LibraryNode> selectedLibraries() {
        if (selectedLibrary != null) {
            return Collections.singletonList(selectedLibrary);
        } else {
            return mc.getSelectedLibraries();
        }
    }

    private boolean canManaged(LibraryNode n) {
        if (!rc.isInManagedNS(n.getNamespace(), repository))
            return false;
        return n.getProjectItem().getState().equals(RepositoryItemState.UNMANAGED);
    }

    @Override
    public boolean isEnabled() {
        for (LibraryNode n : selectedLibraries()) {
            if (!canManaged(n))
                return false;
        }
        return true;
    }
}
