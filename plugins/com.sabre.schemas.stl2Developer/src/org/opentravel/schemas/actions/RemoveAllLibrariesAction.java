/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import static org.opentravel.schemas.node.controllers.NodeUtils.isBuildInProject;
import static org.opentravel.schemas.node.controllers.NodeUtils.isProject;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;

/**
 * @author Dave Hollander
 * 
 */
public class RemoveAllLibrariesAction extends RemoveLibrariesAction {

    public RemoveAllLibrariesAction() {
        super("action.library.removeAll");
    }

    @Override
    protected boolean selectionSupported(List<? extends Node> newSelection) {
        for (Node n : newSelection) {
            if (!isProject(n) || isBuildInProject((ProjectNode) n))
                return false;
        }
        return true;
    }

}
