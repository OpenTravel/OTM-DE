/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Attached to the Facet View button bar.
 * 
 * @author Dave Hollander
 * 
 */
public class ChangeObjectAction extends OtmAbstractAction {

    public ChangeObjectAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    @Override
    public void run() {
        getMainController().changeSelection();
    }

    @Override
    public boolean isEnabled(Node currentNode) {
        if (currentNode == null)
            return false;
        if (!(currentNode instanceof ComponentNode))
            return false;

        if (currentNode.getChain() != null)
            return currentNode.getChain().getEditStatus().equals(NodeEditStatus.FULL);
        return currentNode.isEditable();
    }

}
