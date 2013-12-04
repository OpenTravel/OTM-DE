package com.sabre.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.stl2developer.OtmRegistry;

public class OpenProjectHandler extends AbstractHandler {

    public static final String COMMAND_ID = "com.sabre.schemas.commands.openProject";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        OtmRegistry.getMainController().getProjectController().open();
        return null;
    }

}
