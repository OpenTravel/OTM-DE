/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

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
     * org.opentravel.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled(org.opentravel.schemas
     * .node.Node)
     */
    @Override
    public boolean isEnabled() {
        Node currentNode = mc.getSelectedNode_NavigatorView();
        return currentNode instanceof ComponentNode;
    }

}
