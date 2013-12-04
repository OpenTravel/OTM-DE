/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.node.Node;

public class SaveLibraryHandler extends OtmAbstractHandler {

    public static String COMMAND_ID = "com.sabre.schemas.commands.SaveLibrary";

    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        for (Node cn : mc.getSelectedNodes_NavigatorView()) {
            if (cn.getLibrary() != null) {
                mc.getLibraryController().saveLibrary(cn.getLibrary(), false);
            }
        }
        return null;
    }

    @Override
    public String getID() {
        return COMMAND_ID;
    }

    @Override
    public boolean isEnabled() {
        Node n = mc.getSelectedNode_NavigatorView();
        return n != null ? !n.isBuiltIn() : false;
    }

}
