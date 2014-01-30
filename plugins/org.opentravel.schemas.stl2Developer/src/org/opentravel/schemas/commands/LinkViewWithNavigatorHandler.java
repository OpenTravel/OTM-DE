
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LinkViewWithNavigatorHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
        boolean oldValue = HandlerUtil.toggleCommandState(command);
        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof OtmView) {
            OtmView view = (OtmView) activePart;
            if (view != null) {
                view.setListening(!oldValue);
                if (view.isListening()) {
                    view.setCurrentNode(getSelectedNode());
                }
            }
        }
        return null;
    }

    private INode getSelectedNode() {
        INode node = OtmRegistry.getNavigatorView().getCurrentNode();
        return node;
    }

}
