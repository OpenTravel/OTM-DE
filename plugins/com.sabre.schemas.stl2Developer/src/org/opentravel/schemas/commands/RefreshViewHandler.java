/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class RefreshViewHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof OtmView) {
            OtmView view = (OtmView) activePart;
            INode currentNavigationNode = getSelectedNode();
            view.refresh(currentNavigationNode, true);
        }
        return null;
    }

    private INode getSelectedNode() {
        INode node = OtmRegistry.getNavigatorView().getCurrentNode();
        return node;
    }

}
