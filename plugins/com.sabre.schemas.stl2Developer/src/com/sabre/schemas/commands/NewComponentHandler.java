/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.ComponentNodeType;
import com.sabre.schemas.node.EditNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.wizards.NewComponentWizard;

/**
 * @author Pawel Jedruch
 * 
 */
public class NewComponentHandler extends AbstractHandler {

    public static final String COMMAND_ID = "com.sabre.schemas.commands.newComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(NewComponentHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        newToLibrary();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    // See NodeTester
    // @Override
    // public boolean isEnabled() {
    // Node n = OtmRegistry.getMainController().getCurrentNode_NavigatorView();
    // return n.isEditable();
    // }

    /**
     * Runs new component wizard and creates new node with TL type. Used by New Complex Type Action
     * handler.
     */
    public void newToLibrary() {
        MainController mc = OtmRegistry.getMainController();
        // LOGGER.debug("Adding new component to library");

        final Node selected = mc.getSelectedNode_NavigatorView();

        if (selected == null) {
            LOGGER.debug("No component selected - cannot add new object");
            DialogUserNotifier.openInformation("WARNING", "You must select a library to add to.");
            return;
        }
        if (!selected.isInTLLibrary()) {
            DialogUserNotifier.openInformation("WARNING", "You can only add to a model library.");
            return;
        }

        // Based on version chains, what library do we want to put it into?
        // if the library is locked, new node will go into the head.
        LibraryNode targetLibrary = selected.getLibrary();
        if (!selected.getLibrary().isEditable()) {
            if (selected.getChain() != null) {
                targetLibrary = selected.getChain().getHead();
            }
        }

        final NewComponentWizard wizard = new NewComponentWizard(selected);
        EditNode editNode = null;

        editNode = wizard.postNewComponentWizard(OtmRegistry.getActiveShell());
        if (editNode != null) {
            ComponentNodeType type = ComponentNodeType.fromString(editNode.getUseType());

            Node newOne = editNode.newComponent(type);

            if (editNode.getTLType() != null && newOne instanceof ServiceNode) {
                ((ServiceNode) newOne).addCRUDQ_Operations(editNode.getTLType());
            }
            newOne.setName(NodeNameUtils.fixComplexTypeName(newOne.getName()));
            mc.selectNavigatorNodeAndRefresh(newOne);
        }
    }
}
