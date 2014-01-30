
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;

public class SaveLibraryHandler extends OtmAbstractHandler {

    public static String COMMAND_ID = "org.opentravel.schemas.commands.SaveLibrary";

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
