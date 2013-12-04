/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;

import com.sabre.schemas.node.Node;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddNodeForNavigatorPopup extends AddNodeHandler {

    public AddNodeForNavigatorPopup() {
        setBaseEnabled(true);
    }

    @Override
    protected Node getSelectedNode(ExecutionEvent exEvent) {
        return mc.getSelectedNode_NavigatorView();
    }

}
