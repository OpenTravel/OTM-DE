/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that launches the workbench preferences dialog.
 * 
 * @author S. Livezey
 */
public class EditPreferencesAction extends OtmAbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPreferencesAction.class);

    private static final String PREFERENCES_COMMAND_ID = "org.eclipse.ui.window.preferences";

    /**
     * Constructor that supplies the main window and display label properties.
     * 
     * @param mainWindow
     *            the main window of the application
     * @param props
     *            the properties that contain the application display labels
     */
    public EditPreferencesAction(final MainWindow mainWindow, final StringProperties props) {
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
            final Command preferencesCommand = commandService.getCommand(PREFERENCES_COMMAND_ID);
            final ExecutionEvent executionEvent = handlerService.createExecutionEvent(
                    preferencesCommand, new Event());

            preferencesCommand.executeWithChecks(executionEvent);

        } catch (final Exception e) {
            LOGGER.error("Error launching workbench preferences dialog.", e);
        }
    }

}
