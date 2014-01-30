package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;

public class OpenProjectHandler extends AbstractHandler {

    public static final String COMMAND_ID = "org.opentravel.schemas.commands.openProject";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        OtmRegistry.getMainController().getProjectController().open();
        return null;
    }

}
