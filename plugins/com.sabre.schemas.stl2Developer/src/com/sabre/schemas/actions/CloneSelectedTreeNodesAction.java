/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * Implements the "Copy" menu action.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class CloneSelectedTreeNodesAction extends OtmAbstractAction {

    /**
	 *
	 */
    public CloneSelectedTreeNodesAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        getMainController().copySelectedNodes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled(com.sabre.schemas
     * .node.Node)
     */
    @Override
    public boolean isEnabled() {
        Node currentNode = mc.getSelectedNode_NavigatorView();
        return currentNode instanceof ComponentNode;
    }

}
