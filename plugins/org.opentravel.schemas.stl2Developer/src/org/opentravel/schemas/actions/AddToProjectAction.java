/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;

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
