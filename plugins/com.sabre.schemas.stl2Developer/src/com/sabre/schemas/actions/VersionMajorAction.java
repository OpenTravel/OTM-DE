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
public class VersionMajorAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.library.version.major");

    public VersionMajorAction() {
        super(propsDefault);
    }

    public VersionMajorAction(final StringProperties props) {
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
            mc.postStatus("Major Version " + node);
            RepositoryController rc = mc.getRepositoryController();
            node = node.getLibrary();
            if (node != null && node instanceof LibraryNode)
                rc.createMajorVersion((LibraryNode) node);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LibraryNode ln = null;
        Node n = mc.getSelectedNode_NavigatorView();
        if (n != null)
            ln = n.getLibrary();
        return ln != null && ln.isManaged();
    }
}
