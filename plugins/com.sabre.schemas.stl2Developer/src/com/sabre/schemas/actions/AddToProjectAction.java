package com.sabre.schemas.actions;

import java.util.List;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryChainNode;

public class AddToProjectAction extends OtmAbstractAction {
    private static final StringProperties propsDefault = new ExternalizedStringProperties(
            "action.repository.addToProject");

    private ProjectNode project = null;

    public AddToProjectAction(StringProperties props, ProjectNode project) {
        super(props);
        this.project = project;
    }

    public AddToProjectAction() {
        super(propsDefault);
    }

    @Override
    public void run() {
        List<Node> libs = OtmRegistry.getRepositoryView().getSelectedNodes();
        if (project == null)
            project = mc.getProjectController().getDefaultProject();

        for (Node lib : libs) {
            if (lib instanceof RepositoryChainNode) {
                mc.getProjectController().add(project, ((RepositoryChainNode) lib).getItem());
            }
        }
    }

    @Override
    public boolean isEnabled(Node node) {
        return true;
    }
}
