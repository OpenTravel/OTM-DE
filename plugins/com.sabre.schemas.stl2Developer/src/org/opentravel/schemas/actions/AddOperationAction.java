/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddOperationAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.addOperation");

    /**
	 *
	 */
    public AddOperationAction() {
        super(propDefault);
    }

    public AddOperationAction(final MainWindow mainWindow, final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        mc.runAddProperties(event);
    }

    @Override
    public boolean isEnabled() {
        Node node = getMainController().getCurrentNode_NavigatorView();
        if (node instanceof ServiceNode && node.getEditStatus().equals(NodeEditStatus.FULL))
            return true;
        return false;
    }
}
