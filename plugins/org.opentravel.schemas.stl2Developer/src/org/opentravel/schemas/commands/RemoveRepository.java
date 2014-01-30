
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;

public class RemoveRepository extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil
                .getCurrentSelection(event);
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (node != null) {
            boolean ans = DialogUserNotifier.openConfirm("Remove Repository",
                    Messages.getString("OtmW.121") + node.getRepository().getDisplayName());
            if (!ans)
                return null;
        }
        OtmRegistry.getMainController().getRepositoryController().removeRemoteRepository(node);
        return null;
    }

}
