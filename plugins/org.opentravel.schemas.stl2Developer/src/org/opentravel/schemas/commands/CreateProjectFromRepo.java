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
package org.opentravel.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryRootNsNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;

public class CreateProjectFromRepo extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<Node> selectedNodes = OtmRegistry.getRepositoryView().getSelectedNodes();
        String selectedRoot = getSelectedRoot(selectedNodes);
        String selectedExt = getSelectedExt(selectedNodes);
        OtmRegistry.getMainController().getProjectController()
                .newProject("", selectedRoot, selectedExt);
        return null;
    }

    private String getSelectedExt(List<Node> selectedNodes) {
        if (selectedNodes.isEmpty())
            return "";
        Node selected = selectedNodes.get(0);
        if (selected instanceof RepositoryRootNsNode) {
            RepositoryRootNsNode n = (RepositoryRootNsNode) selected;
            return n.getName();
        } else if (selected instanceof RepositoryChainNode) {
            return getNamespaceExt((RepositoryChainNode) selected);
        }
        return "";
    }

    private String getSelectedRoot(List<Node> selectedNodes) {
        if (selectedNodes.isEmpty())
            return "";
        Node selected = selectedNodes.get(0);
        if (selected instanceof RepositoryRootNsNode) {
            RepositoryRootNsNode n = (RepositoryRootNsNode) selected;
            return n.getRootBasename();
        } else if (selected instanceof RepositoryChainNode) {
            return getRootNamespace((RepositoryChainNode) selected);
        }
        return "";
    }

    private String getRootNamespace(RepositoryChainNode node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof RepositoryRootNsNode) {
                return ((RepositoryRootNsNode) parent).getRootBasename();
            }
            parent = parent.getParent();
        }
        return "";
    }

    private String getNamespaceExt(RepositoryChainNode node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof RepositoryRootNsNode) {
                return ((RepositoryRootNsNode) parent).getName();
            }
            parent = parent.getParent();
        }
        return "";
    }

}
