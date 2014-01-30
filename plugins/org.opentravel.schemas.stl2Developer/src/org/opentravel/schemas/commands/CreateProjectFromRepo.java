
package org.opentravel.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.NamespaceNode;
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
        if (selected instanceof NamespaceNode) {
            NamespaceNode n = (NamespaceNode) selected;
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
        if (selected instanceof NamespaceNode) {
            NamespaceNode n = (NamespaceNode) selected;
            return n.getRootBasename();
        } else if (selected instanceof RepositoryChainNode) {
            return getRootNamespace((RepositoryChainNode) selected);
        }
        return "";
    }

    private String getRootNamespace(RepositoryChainNode node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof NamespaceNode) {
                return ((NamespaceNode) parent).getRootBasename();
            }
            parent = parent.getParent();
        }
        return "";
    }

    private String getNamespaceExt(RepositoryChainNode node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof NamespaceNode) {
                return ((NamespaceNode) parent).getName();
            }
            parent = parent.getParent();
        }
        return "";
    }

}
