/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.swt.widgets.Event;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

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
