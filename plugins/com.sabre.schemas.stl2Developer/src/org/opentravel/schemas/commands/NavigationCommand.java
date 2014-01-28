/*
 * Copyright (c) 2012, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.navigation.INavigationService;

public class NavigationCommand extends AbstractHandler implements IExecutableExtension {

    private static final String MODE_FORWARD = "FORWARD";
    private static final String MODE_BACKWARD = "BACKWARD";
    private String mode = "";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (MODE_BACKWARD.equalsIgnoreCase(mode))
            getNavigationController().goBackward();
        else if (MODE_FORWARD.equalsIgnoreCase(mode))
            getNavigationController().goForward();
        return null;
    }

    private INavigationService getNavigationController() {
        return (INavigationService) PlatformUI.getWorkbench().getService(INavigationService.class);
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
            throws CoreException {
        mode = (String) data;
    }
}
