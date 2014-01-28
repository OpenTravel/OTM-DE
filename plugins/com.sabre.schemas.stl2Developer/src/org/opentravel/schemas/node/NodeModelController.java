/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OBSOLETE - put action semantics directly into action classes. 7/6/2012 - BUT, putting them here
 * makes them available to buttons and other non-action class user events.
 * 
 * TODO 1) add type resolver here 2) replace all static finders using nmc 3) Use this to manage the
 * model node.
 * 
 * Implements methods that implement user driven model node tree manipulations. Node class provides
 * CRUD level control over the model. These methods interact with GUI to prepare for those model
 * operations.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeModelController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeModelController.class);
    protected MainController mc;

    public NodeModelController(MainController mainController) {
        super();
        mc = mainController;
    }

    /**
     * Delete all currently selected nodes. Get node lists from facet table in type view, if that is
     * empty get the navigator view nodes. If that is empty, get the facet view current node.
     * 
     * Confirm delete of node list with the user.
     */
    public void deleteSelectedNodes() {
        final List<Node> facetDelList = mc.getSelectedNodes_TypeView();
        final List<Node> treeDelList = mc.getSelectedNodes_NavigatorView();
        final Node cn = mc.getSelectedNode_TypeView();

        if (facetDelList.isEmpty()) {
            if (treeDelList.isEmpty()) {
                facetDelList.add(cn);
            } else {
                facetDelList.addAll(treeDelList);
            }
        }
        deleteNodes(facetDelList);
    }

    /**
     * Guide the user through deleting the list of nodes. Inform them of which ones will not be
     * deleted. Ask them to confirm deleting the rest. Use node to actually delete the nodes.
     * 
     * @param deleteList
     */
    public void deleteNodes(final List<Node> deleteList) {

        // find out if any of the nodes can not be deleted.
        final List<Node> toDelete = new ArrayList<Node>();
        final StringBuilder doNotDelete = new StringBuilder();
        int i = 0;
        for (final Node n : deleteList) {
            if (n.isDeleteable()) {
                toDelete.add(n);
            } else {
                if (i++ > 0)
                    doNotDelete.append(", ");
                doNotDelete.append(n.getName());
            }
        }

        if (i > 0) {
            DialogUserNotifier.openWarning("Object Delete",
                    "The following nodes are not allowed to be deleted: " + doNotDelete);
        }

        // Make the user confirm the list of nodes to be deleted.
        if (toDelete.size() > 0) {
            final String listing = Messages.getString("OtmW.121"); //$NON-NLS-1$
            final StringBuilder sb = new StringBuilder();
            i = 0;
            for (final INode n : toDelete) {
                if (i++ > 0)
                    sb.append(", ");
                sb.append(n.getName());
            }
            final boolean ans = DialogUserNotifier.openConfirm(Messages.getString("OtmW.120"),
                    listing + sb.toString());

            if (ans) {
                LOGGER.debug("Deleting the objects: " + sb.toString());
                // Determine where to focus when delete is done
                Node focusNode = ((Node) mc.getCurrentNode_TypeView()).getOwningComponent();
                while (toDelete.contains(focusNode)) {
                    focusNode = focusNode.getParent();
                }

                Node.deleteNodeList(toDelete);

                mc.refresh(focusNode);
                mc.selectNavigatorNodeAndRefresh(focusNode);

            }
        }
    }

}
