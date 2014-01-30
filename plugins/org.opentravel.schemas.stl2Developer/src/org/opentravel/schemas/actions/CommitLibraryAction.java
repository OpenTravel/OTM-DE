
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class CommitLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.commit");

    public CommitLibraryAction() {
        super(propDefault);
    }

    public CommitLibraryAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void run() {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            mc.getRepositoryController().commit(ln);
        }
    }

    @Override
    public boolean isEnabled() {
        INode n = getMainController().getCurrentNode_NavigatorView();
        if (n == null || !(n instanceof LibraryChainNode))
            return false;
        // FIXME - npe
        return ((LibraryChainNode) n).getHead().getProjectItem().getState()
                .equals(RepositoryItemState.MANAGED_WIP);
    }

}
