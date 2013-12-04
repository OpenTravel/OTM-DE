/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

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
