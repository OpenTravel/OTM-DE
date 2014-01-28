/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class UpFacetAction extends OtmAbstractAction {

    /**
	 *
	 */
    public UpFacetAction(final MainWindow mainWindow, final StringProperties props) {
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
            view.moveUp();
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
