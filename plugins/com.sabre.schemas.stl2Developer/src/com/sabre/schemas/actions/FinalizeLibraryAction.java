/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

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
