package com.sabre.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sabre.schemas.views.DocumentationView;

public class DeleteDocumentationNodeHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof DocumentationView) {
            DocumentationView view = (DocumentationView) activePart;
            if (view != null) {
                view.getDocumentationController().deleteDocItems();
            }
        }
        return null;
    }

}
