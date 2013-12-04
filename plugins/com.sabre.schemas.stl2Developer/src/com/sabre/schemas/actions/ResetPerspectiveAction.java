/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * Action that resets the positions of all views to their original state.
 * 
 * @author S. Livezey
 */
public class ResetPerspectiveAction extends OtmAbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPerspectiveAction.class);

    private static final String RESET_PERSPECTIVE_COMMAND_ID = "com.sabre.schemas.stl2Developer.ResetPerspective";

    /**
     * Constructor that supplies the main window and display label properties.
     * 
     * @param mainWindow
     *            the main window of the application
     * @param props
     *            the properties that contain the application display labels
     */
    public ResetPerspectiveAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
                    .getService(IHandlerService.class);
            final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
                    .getService(ICommandService.class);
            final Command resetCommand = commandService.getCommand(RESET_PERSPECTIVE_COMMAND_ID);
            final ExecutionEvent executionEvent = handlerService.createExecutionEvent(resetCommand,
                    new Event());

            resetCommand.executeWithChecks(executionEvent);

        } catch (final Exception e) {
            LOGGER.error("Error resetting views to their default states.", e);
        }
    }

}
