/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.TypeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sorting properties of a node.
 * 
 * @author Dave Hollander
 * 
 */
public class SortNodeHandler extends OtmAbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SortNodeHandler.class);
    public static final String COMMAND_ID = "org.opentravel.schemas.commands.Sort";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        IWorkbenchPart activeView = HandlerUtil.getActivePart(exEvent);
        List<Node> selectedNodes = getSelectedNodes(activeView);//
        for (Node node : selectedNodes) {
            node.sort();
        }
        mc.refresh();
        if (selectedNodes.size() > 0) {
            mc.postStatus("Properties of " + selectedNodes + " were sorted.");
            LOGGER.debug("Sort Node Command Handler sorted " + selectedNodes);
        }
        return null;
    }

    private List<Node> getSelectedNodes(IWorkbenchPart activeView) {
        List<Node> selectedNodes = Collections.emptyList();
        ;
        if (activeView instanceof OtmView) {
            OtmView view = (OtmView) activeView;
            if (view instanceof TypeView) {
                selectedNodes = getTypeViewSelection((TypeView) view);
            }
        }
        if (selectedNodes.isEmpty()) {
            selectedNodes = mc.getGloballySelectNodes();
        }
        return selectedNodes;
    }

    private List<Node> getTypeViewSelection(TypeView view) {
        List<Node> selectedNodes = view.getSelectedNodes();
        if (selectedNodes == null || selectedNodes.isEmpty())
            return Collections.singletonList(view.getCurrentNode());
        return selectedNodes;
    }

    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
