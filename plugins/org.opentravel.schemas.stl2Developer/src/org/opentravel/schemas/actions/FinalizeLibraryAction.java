
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Finalize a version of a library.
 * 
 * @author Dave Hollander
 * 
 */
public class FinalizeLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.finalize");

    public FinalizeLibraryAction() {
        super(propDefault);
    }

    public FinalizeLibraryAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void run() {
        for (LibraryNode ln : mc.getSelectedLibraries()) {
            mc.getRepositoryController().markFinal(ln);
        }
    }

    // enable when Work-in-progress item.
    @Override
    public boolean isEnabled() {
        INode n = getMainController().getCurrentNode_NavigatorView();
        if (n instanceof LibraryChainNode)
            n = ((LibraryChainNode) n).getLibrary();
        if (n instanceof LibraryNode) {
            RepositoryItemState state = ((LibraryNode) n).getProjectItem().getState();
            TLLibraryStatus status = ((LibraryNode) n).getStatus();
            if (((LibraryNode) n).getStatus().equals(TLLibraryStatus.FINAL))
                return false;
            switch (state) {
                case MANAGED_LOCKED:
                    // TODO - what other behaviors are needed for these states?
                    return true;
                case MANAGED_UNLOCKED:
                    return true;
                case MANAGED_WIP:
                    return true;
                case UNMANAGED:
                    return false;
            }
        }
        return false;
    }

}
