/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.controllers.RepositoryController;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class VersionPatchAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.library.version.patch");

    public VersionPatchAction() {
        super(propsDefault);
    }

    public VersionPatchAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        for (Node node : mc.getSelectedNodes_NavigatorView()) {
            mc.postStatus("Patch Version " + node);
            RepositoryController rc = mc.getRepositoryController();
            node = node.getLibrary();
            if (node != null && node instanceof LibraryNode)
                rc.createPatchVersion((LibraryNode) node);
        }
        mc.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LibraryNode ln = null;
        if (mc.getSelectedNode_NavigatorView() != null)
            ln = mc.getSelectedNode_NavigatorView().getLibrary();
        return ln != null && ln.isManaged();
    }

}
