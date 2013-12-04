/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.types.TypeNode;
import com.sabre.schemas.views.NavigatorView;
import com.sabre.schemas.views.OtmView;

/**
 * If selection on active view contains node with type, then it will select this node type in
 * Navigator View. It should be only enabled when {@link ComponentNode} is selected and has assigned
 * type.
 * 
 * @author Pawel Jedruch
 * 
 */
public class GoToTypeHandler extends AbstractHandler {

    public final static String COMMAND_ID = "com.sabre.schemas.commands.goto.type";

    private final static Logger LOGGER = LoggerFactory.getLogger(GoToTypeHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart view = HandlerUtil.getActivePart(event);
        if (view instanceof OtmView) {
            selectType((OtmView) view);
            return null;
        }
        return null;
    }

    private void selectType(OtmView view) {
        Node typeNode = getTypeNode(view);
        select(typeNode);
    }

    private void select(Node type) {
        LOGGER.debug("Selecting: " + type.toString());
        NavigatorView view = (NavigatorView) OtmRegistry.getNavigatorView();
        if (view.isReachable(type)) {
            OtmRegistry.getMainController().selectNavigatorNodeAndRefresh(type);
        } else {
            if (view.isFilterActive()) {
                DialogUserNotifier.openInformation("WARNING",
                        Messages.getString("action.goto.unreachable.filter"));
            } else {
                LOGGER.debug("Cannot find node in NavigationView and filter is not activated.");
            }
        }
    }

    /**
     * we can ignore checks because of enableWhen declarations for this handler in plugin.xml
     * 
     * @param typeView
     * @return type node for selected node in view.
     */
    private Node getTypeNode(OtmView typeView) {
        List<Node> nodes = typeView.getSelectedNodes();
        Node n = nodes.get(0);
        return getTypeNode(n);
    }

    private Node getTypeNode(Node node) {
        if (node instanceof TypeNode) {
            return node.getParent();
        }
        return node.getTypeClass().getTypeNode();
    }

}
