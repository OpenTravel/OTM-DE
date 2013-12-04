/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import static com.sabre.schemas.node.controllers.NodeUtils.isBuildInProject;
import static com.sabre.schemas.node.controllers.NodeUtils.isDefaultProject;
import static com.sabre.schemas.node.controllers.NodeUtils.isProject;

import java.util.List;

import org.eclipse.ui.PlatformUI;

import com.sabre.schemas.navigation.GlobalSelectionProvider;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class CloseProjectAction extends AbstractGlobalSelectionAction {

    private ProjectNode toClose;

    public CloseProjectAction() {
        super("action.closeProject", PlatformUI.getWorkbench(),
                GlobalSelectionProvider.NAVIGATION_VIEW);
        new ExternalizedStringProperties(getId()).initializeAction(this);
    }

    @Override
    public void run() {
        if (toClose != null) {
            OtmRegistry.getMainController().getProjectController().close(toClose);
            toClose = null;
        }
    }

    @Override
    protected boolean isEnabled(Object object) {
        @SuppressWarnings("unchecked")
        List<Node> newSelection = (List<Node>) object;
        if (newSelection.size() != 1) {
            return false;
        }

        // save ref to make sure run will execute on the same instance
        toClose = getProjectToClose(newSelection.get(0));
        return toClose != null;
    }

    private ProjectNode getProjectToClose(Node n) {
        if (isProject(n) && !(isBuildInProject((ProjectNode) n) || isDefaultProject(n))) {
            return ((ProjectNode) n);
        }
        return null;
    }

}
