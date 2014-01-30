/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import java.util.Collections;
import java.util.List;

import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class RemoveLibrariesAction extends AbstractGlobalSelectionAction {

    private List<Node> toClose = Collections.emptyList();

    public RemoveLibrariesAction() {
        this("action.library.close");
    }

    protected RemoveLibrariesAction(String id) {
        super(id, PlatformUI.getWorkbench(), GlobalSelectionProvider.NAVIGATION_VIEW);
        new ExternalizedStringProperties(getId()).initializeAction(this);
    }

    @Override
    public void run() {
        MainController mc = OtmRegistry.getMainController();
        mc.getLibraryController().remove(toClose);
        toClose = Collections.emptyList();
        mc.refresh();
    }

    @Override
    protected boolean isEnabled(Object object) {
        @SuppressWarnings("unchecked")
        List<Node> newSelection = (List<Node>) object;

        if (selectionSupported(newSelection)) {
            toClose = newSelection;
            return true;
        } else {
            return false;
        }
    }

    protected boolean selectionSupported(List<? extends Node> newSelection) {
        for (Node n : newSelection) {
            if ((!isLibraryNotInChain(n) && !isLibraryChain(n)) || isBuildInLibrary(n)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLibraryNotInChain(Node n) {
        if (n instanceof LibraryNode) {
            return !((LibraryNode) n).isInChain();
        }
        return false;
    }

    private boolean isBuildInLibrary(Node n) {
        if (n instanceof LibraryNode) {
            return n.isBuiltIn();
        }
        return false;
    }

    private boolean isLibraryChain(Node n) {
        return n instanceof LibraryChainNode;
    }

}
