/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DownFacetAction extends OtmAbstractAction {

    public DownFacetAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final OtmView view = OtmRegistry.getTypeView();
        if (view != null) {
            view.moveDown();
        }
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
