/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for interactions with the repositories.
 * 
 * @author Dave Hollander
 * 
 */
public class RepositoryHandler extends OtmAbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryHandler.class);
    public static String COMMAND_ID = "org.opentravel.schemas.commands.repository";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        sync();
        return null;
    }

    public void sync() {
        RepositoryController rc = mc.getRepositoryController();
        List<Node> selectedNodes = OtmRegistry.getRepositoryView().getSelectedNodes();

        if (selectedNodes.isEmpty()) {
            mc.getRepositoryController().sync(rc.getRoot());
        } else {
            for (RepositoryNode n : getRepositoryNodes(selectedNodes)) {
                mc.getRepositoryController().sync(n);
            }
        }
        mc.postStatus("Syncronized with repositories.");
        LOGGER.debug("Sync Command Handler complete");
    }

    private Set<RepositoryNode> getRepositoryNodes(List<Node> selectedNodes) {
        Set<RepositoryNode> nodes = new HashSet<RepositoryNode>();
        for (Node n : selectedNodes) {
            if (n instanceof RepositoryNode) {
                nodes.add((RepositoryNode) n);
            }
        }
        return nodes;
    }

    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
