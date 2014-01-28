/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.actions.ActionFactory;
import org.opentravel.schemas.node.Node;

/**
 * Command Handler for deleting nodes from the model .
 * 
 * @author Dave Hollander
 * 
 */
public class DeleteNodesHandler extends OtmAbstractHandler {

    public static final String COMMAND_ID = ActionFactory.DELETE.getCommandId();

    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        List<Node> selectedNodes = mc.getGloballySelectNodes();
        mc.getNodeModelController().deleteNodes(selectedNodes);
        return null;
    }

}
