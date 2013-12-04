/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

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
