/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.actions;

import java.util.List;

import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.navigation.GlobalSelectionProvider;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Pawel Jedruch
 * 
 */
public class MergeNodesAction extends AbstractGlobalSelectionAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeNodesAction.class);
    private List<Node> toMerge;

    public MergeNodesAction() {
        super("com.sabre.schemas.commands.MergeNodes", PlatformUI.getWorkbench(),
                GlobalSelectionProvider.NAVIGATION_VIEW);
    }

    @Override
    protected boolean isEnabled(Object object) {
        @SuppressWarnings("unchecked")
        List<Node> newSelection = (List<Node>) object;
        if (newSelection.size() > 1 && newSelection.get(0).isMergeSupported()
                && isSameObjects(newSelection)) {
            toMerge = newSelection;
            return true;
        }
        return false;
    }

    private boolean isSameObjects(List<Node> newSelection) {
        Class<? extends Node> first = null;
        for (Node node : newSelection) {
            if (first == null) {
                first = node.getClass();
            } else if (!first.equals(node.getClass())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        if (toMerge != null) {
            MainController mc = OtmRegistry.getMainController();
            Node selectedNode = toMerge.get(0);
            Node newNode = selectedNode.getOwningComponent().clone("_Merged");

            int cnt = 0;
            for (Node node : mc.getSelectedNodes_NavigatorView()) {
                LOGGER.debug("Merge this node: " + node + " into " + newNode);
                if (node != null && node != selectedNode) {
                    newNode.merge(node);
                    cnt++;
                }
            }
            mc.refresh(selectedNode.getParent());

            mc.postStatus("Merged " + cnt + " components.");
            LOGGER.debug("Merge Nodes Command Handler " + selectedNode);
        }
    }

}
