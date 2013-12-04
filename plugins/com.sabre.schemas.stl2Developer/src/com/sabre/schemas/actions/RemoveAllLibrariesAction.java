/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import static com.sabre.schemas.node.controllers.NodeUtils.isBuildInProject;
import static com.sabre.schemas.node.controllers.NodeUtils.isProject;

import java.util.List;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;

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
