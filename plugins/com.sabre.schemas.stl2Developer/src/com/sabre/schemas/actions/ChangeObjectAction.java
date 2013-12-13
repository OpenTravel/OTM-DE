/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

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
